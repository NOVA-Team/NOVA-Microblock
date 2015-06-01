package com.calclavia.microblock.common;

import com.calclavia.microblock.MicroblockPlugin;
import com.calclavia.microblock.micro.Microblock;
import com.calclavia.microblock.micro.MicroblockContainer;
import com.calclavia.microblock.multi.Multiblock;
import com.calclavia.microblock.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.game.Game;
import nova.core.network.NetworkTarget;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3d;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Handles mciroblock set operations.
 * @author Calclavia
 */
public class MicroblockOperation {

	public final Set<Vector3i> handledPositions = new HashSet<>();
	private final World world;
	private final MicroblockPlugin.MicroblockInjectFactory injectFactory;
	private final Block newBlock;
	private final Vector3i globalPos;
	private final Optional<Vector3i> localPos;
	//Positions that the operation handled.
	public boolean fail = false;

	/**
	 * Create a microblock operation handler
	 * @param world The world
	 * @param injectFactory The factory to injectToContainer
	 * @param globalPos The world position to handle wth block
	 * @param evt The block place event
	 */
	public MicroblockOperation(World world, MicroblockPlugin.MicroblockInjectFactory injectFactory, Vector3i globalPos, Block.BlockPlaceEvent evt) {
		this.world = world;
		this.injectFactory = injectFactory;
		this.newBlock = injectFactory.containedFactory.makeBlock();
		this.globalPos = globalPos;
		this.localPos = newBlock.get(Microblock.class).onPlace.apply(evt);
	}

	public MicroblockOperation(World world, MicroblockPlugin.MicroblockInjectFactory injectFactory, Vector3i globalPos) {
		this.world = world;
		this.injectFactory = injectFactory;
		this.newBlock = injectFactory.containedFactory.makeBlock();
		this.globalPos = globalPos;
		this.localPos = Optional.empty();
	}

	//TODO: What happens when we try to set it to the void? Fail/remove all the blocks.

	/**
	 * Sets a block to be either a microblock or a multiblock, or both.
	 * @return True if the block was successfully set
	 */
	public boolean setBlock() {
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
					Set<Vector3i> blockSpace = multiblock.getOccupiedSpace(1)
						.stream()
						.map(Vector3d::toInt)
						.collect(Collectors.toSet());

					Set<Vector3d> occupiedSpace = multiblock.getOccupiedSpace(1f / microblockContainer.subdivision);

					populateBlockSpace(blockSpace,
						(relativeBlockVec, outerContainerBlock) -> {
							MicroblockContainer outerContainer = outerContainerBlock.getOrAdd(new MicroblockContainer(outerContainerBlock));

							//Create a new multiblock inner container that lives inside the microblock structure.
							BlockContainer innerContainer = new BlockContainer("blockContainer");
							innerContainer.add(new MultiblockContainer(innerContainer, newBlock));
							innerContainer.add(new Microblock(innerContainer)).setOnPlace(blockPlaceEvent -> localPos);

							//Add transform component
							innerContainer.add(outerContainerBlock.transform());

							Set<Vector3i> localPositions = occupiedSpace.stream()
								.map(vec -> vec.subtract(relativeBlockVec.toDouble())) //Maps positions relative to its own block space
								.filter(vec -> new Cuboid(Vector3i.zero, Vector3i.one).intersects(vec)) //Filters blocks relevant to the relativeBlockVec
								.map(vec -> vec.multiply(microblockContainer.subdivision))//Multiply all unit vectors by subdivision size, converting it to local vectors.
								.map(Vector3d::toInt)
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
				Set<Vector3i> blockSpace = multiblock.getOccupiedSpace(1)
					.stream()
					.map(Vector3d::toInt)
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
	 * @param blockSpace The set of block positions where we want to populate container blocks
	 * @param func The callback function called after a container block is set.
	 * @return A set of containers actually placed into the world.
	 */
	protected Set<Block> populateBlockSpace(Set<Vector3i> blockSpace, BiConsumer<Vector3i, Block> func) {
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
	 * @param pos The position to set the container
	 * @return The container block
	 */
	public Optional<Block> getOrSetContainer(Vector3i pos) {

		Optional<Block> opCheckBlock = world.getBlock(pos);

		if (opCheckBlock.isPresent()) {
			Block checkBlock = opCheckBlock.get();
			if (checkBlock.sameType(Game.instance().blockManager().getAirBlockFactory())) {
				//It's air, so let's create a container
				world.setBlock(pos, injectFactory);
				handledPositions.add(pos);
				return world.getBlock(pos);
			} else if (checkBlock.sameType(injectFactory)) {
				//There's already a microblock there.
				return Optional.of(checkBlock);
			}
		}

		fail = true;
		return Optional.empty();
	}
}
