package nova.microblock.operation;

import nova.core.block.Block;
import nova.core.network.NetworkTarget;
import nova.core.util.math.Vector3DUtil;
import nova.core.util.shape.Cuboid;
import nova.core.world.World;
import nova.microblock.MicroblockPlugin;
import nova.microblock.common.BlockContainer;
import nova.microblock.micro.Microblock;
import nova.microblock.micro.MicroblockContainer;
import nova.microblock.multi.Multiblock;
import nova.microblock.multi.MultiblockContainer;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Handles mciroblock set operations.
 *
 * @author Calclavia
 */
public class ContainerPlace extends ContainerOperation {
	//Positions that the operation handled.
	public final Set<Vector3D> handledPositions = new HashSet<>();
	private final MicroblockPlugin.MicroblockInjectFactory injectFactory;
	private final Block newBlock;
	private final Optional<Vector3D> localPos;

	/**
	 * Create a microblock operation handler
	 *
	 * @param world The world
	 * @param injectFactory The factory to injectToContainer
	 * @param globalPos The world position to handle wth block
	 * @param evt The block place event
	 */
	public ContainerPlace(World world, MicroblockPlugin.MicroblockInjectFactory injectFactory, Vector3D globalPos, Block.PlaceEvent evt) {
		super(world, globalPos);
		this.injectFactory = injectFactory;
		this.newBlock = injectFactory.containedFactory.makeBlock();
		this.localPos = newBlock.get(Microblock.class).onPlace.apply(evt);
	}

	public ContainerPlace(World world, MicroblockPlugin.MicroblockInjectFactory injectFactory, Vector3D globalPos) {
		super(world, globalPos);
		this.injectFactory = injectFactory;
		this.newBlock = injectFactory.containedFactory.makeBlock();
		this.localPos = Optional.empty();
	}

	//TODO: What happens when we try to set it to the void? Fail/remove all the blocks.
	@Override
	public boolean operate() {
		if (NetworkTarget.Side.get().isClient()) {
			return true;
		}

		Optional<Block> opContainer = getOrSetContainer(globalPos);

		if (opContainer.isPresent()) {
			Block container = opContainer.get();

			//Add transform component
			//newBlock.add(container.transform());

			if (newBlock.has(Microblock.class)) {
				assert localPos.isPresent();

				//This is a microblock
				//Attach microblock container to the container
				MicroblockContainer microblockContainer = container.getOrAdd(new MicroblockContainer(container));

				if (newBlock.has(Multiblock.class)) {
					/**
					 * Generate multiblock containers to wrap this microblock
					 */
					Multiblock multiblock = newBlock.get(Multiblock.class);

					//A set of world block space that are being occupied
					Set<Vector3D> blockSpace = multiblock.getOccupiedSpace(1)
						.stream()
						.collect(Collectors.toSet());

					Set<Vector3D> occupiedSpace = multiblock.getOccupiedSpace(1f / microblockContainer.subdivision);

					populateBlockSpace(blockSpace,
						(relativeBlockVec, outerContainerBlock) -> {
							MicroblockContainer outerContainer = outerContainerBlock.getOrAdd(new MicroblockContainer(outerContainerBlock));

							//Create a new multiblock inner container that lives inside the microblock structure.
							BlockContainer innerContainer = new BlockContainer("blockContainer");
							innerContainer.add(new MultiblockContainer(innerContainer, newBlock));
							innerContainer.add(new Microblock(innerContainer)).setOnPlace(blockPlaceEvent -> localPos);

							//Add transform component
							innerContainer.add(outerContainerBlock.transform());

							Set<Vector3D> localPositions = occupiedSpace.stream()
								.map(vec -> vec.subtract(relativeBlockVec)) //Maps positions relative to its own block space
								.filter(vec -> new Cuboid(Vector3D.ZERO, Vector3DUtil.ONE).intersects(vec)) //Filters blocks relevant to the relativeBlockVec
								.map(vec -> vec.scalarMultiply(microblockContainer.subdivision)) //Multiply all unit vectors by subdivision size, converting it to local vectors.
								.collect(Collectors.toSet());

							localPositions.forEach(vec -> {
								if (!outerContainer.put(vec, innerContainer.get(Microblock.class))) {
									fail = true;
								}
							});
						}
					);

					newBlock.get(Microblock.class).position = localPos.get();
					//TODO: Handle injection
					return handleFail();
				} else {
					/**
					 * Build microblocks without multiblocks
					 */
					if (!microblockContainer.putNew(localPos.get(), newBlock.get(Microblock.class))) {
						fail = true;
					}

					return handleFail();
				}
			} else if (newBlock.has(Multiblock.class)) {

				Multiblock multiblock = newBlock.get(Multiblock.class);

				//Build multiblock without microblocks
				Set<Vector3D> blockSpace = multiblock.getOccupiedSpace(1)
					.stream()
					.collect(Collectors.toSet());

				populateBlockSpace(blockSpace,
					(relativeBlockVec, outerContainerBlock) -> {
						//Creates the outer container block that will exist in the world.
						outerContainerBlock.getOrAdd(new MultiblockContainer(outerContainerBlock, newBlock));
					}
				);
				//TODO: Handle injection
				return handleFail();
			}
		}

		return false;
	}

	/**
	 * @return False if failed
	 */
	protected boolean handleFail() {
		if (fail) {
			cleanup();
		}
		return !fail;
	}

	protected void cleanup() {
		handledPositions.forEach(vector -> world.removeBlock(vector));
	}

	/**
	 * Populates a block space
	 *
	 * @param blockSpace The set of block positions where we want to populate container blocks
	 * @param func The callback function called after a container block is set.
	 * @return A set of containers actually placed into the world.
	 */
	protected Set<Block> populateBlockSpace(Set<Vector3D> blockSpace, BiConsumer<Vector3D, Block> func) {
		Set<Block> populated = new HashSet<>();

		blockSpace.forEach(relativeBlockVec -> {
			//Note: relativeBlockVec is relative to globalPos.
			Optional<Block> opInnerContainer = getOrSetContainer(globalPos.add(relativeBlockVec));

			if (opInnerContainer.isPresent()) {
				//Creates the outer container block that will exist in the world.
				Block outerContainerBlock = opInnerContainer.get();
				func.accept(relativeBlockVec, outerContainerBlock);
				populated.add(outerContainerBlock);
			}
		});
		return populated;
	}

	/**
	 * Checks a position in the world and either gets or sets the position into {@link BlockContainer}
	 *
	 * @param pos The position to set the container
	 * @return The container block
	 */
	public Optional<Block> getOrSetContainer(Vector3D pos) {

		Optional<Block> opCheckBlock = world.getBlock(pos);

		if (opCheckBlock.isPresent()) {
			Block checkBlock = opCheckBlock.get();
			if (checkBlock.sameType(MicroblockPlugin.instance.blocks.getAirBlock())) {
				//It's air, so let's create a container
				world.setBlock(pos, injectFactory);
				handledPositions.add(pos);
				return world.getBlock(pos);
			} else if (checkBlock.getID().startsWith("blockContainer-")) {
				//There's already a microblock there.
				return Optional.of(checkBlock);
			}
		}

		fail = true;
		return Optional.empty();
	}
}
