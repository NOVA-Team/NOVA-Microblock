package com.calclavia.microblock.core.common;

import com.calclavia.microblock.core.MicroblockAPI;
import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.micro.MicroblockContainer;
import com.calclavia.microblock.core.multi.Multiblock;
import com.calclavia.microblock.core.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.game.Game;
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
 * @author Calclavia
 */
public class MicroblockOperation {

	//Positions that the operation handled.
	public final Set<Vector3i> appliedPositions = new HashSet<>();
	private final World world;
	private final Block newBlock;
	private final Vector3i globalPos;
	private final Optional<Vector3i> localPos;

	/**
	 * Create a microblock operation handler
	 * @param world
	 * @param newBlock
	 * @param globalPos
	 * @param localPos
	 */
	public MicroblockOperation(World world, Block newBlock, Vector3i globalPos, Vector3i localPos) {
		this.world = world;
		this.newBlock = newBlock;
		this.globalPos = globalPos;
		this.localPos = Optional.of(localPos);
	}

	public MicroblockOperation(World world, Block newBlock, Vector3i globalPos) {
		this.world = world;
		this.newBlock = newBlock;
		this.globalPos = globalPos;
		this.localPos = Optional.empty();
	}

	//TODO: What happens when we try to set it to the void? Fail/remove all the blocks.

	/**
	 * Sets a block to be either a microblock or a multiblock, or both.
	 * @return
	 */
	public boolean setBlock() {
		Optional<Block> opContainer = getOrSetContainer();

		if (opContainer.isPresent()) {
			Block container = opContainer.get();

			//Add transform component
			newBlock.add(container.transform());

			if (newBlock.has(Microblock.class)) {
				assert localPos.isPresent();

				//This is a microblock
				//Attach microblock container to the container
				MicroblockContainer microblockContainer = container.add(new MicroblockContainer(container));

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

					Set<Vector3d> occupiedSpace = multiblock.getOccupiedSpace(microblockContainer.subdivisions);

					populateBlockSpace(blockSpace,
						(relativeBlockVec, outerContainerBlock) -> {
							MicroblockContainer outerContainer = outerContainerBlock.getOrAdd(new MicroblockContainer(outerContainerBlock));

							//Create a new multiblock inner container that lives inside the microblock structure.
							BlockContainer innerContainer = new BlockContainer();
							innerContainer.add(new MultiblockContainer(innerContainer, newBlock));
							innerContainer.add(new Microblock(innerContainer, blockPlaceEvent -> localPos.get())).setContainer(outerContainer);

							//Add transform component
							innerContainer.add(outerContainerBlock.transform());

							Set<Vector3i> localPositions = occupiedSpace.stream()
								.filter(vec -> new Cuboid(relativeBlockVec, relativeBlockVec.add(1)).intersects(vec)) //Filters blocks relevant to the relativeBlockVec
								.map(vec -> vec.subtract(relativeBlockVec.toDouble())) //Maps positions relative to its own block space
								.map(vec -> vec.multiply(microblockContainer.subdivisions))//Multiply all unit vectors by subdivision size, converting it to local vectors.
								.map(Vector3d::toInt)
								.collect(Collectors.toSet());

							localPositions.forEach(vec -> outerContainer.put(vec, innerContainer.get(Microblock.class)));
						});
					//TODO: What happens when we try to set it to the void? Fail/remove all the blocks.

				} else {
					/**
					 * Build microblocks without multiblocks
					 */
					microblockContainer.put(localPos.get(), newBlock.get(Microblock.class));
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
					})
				;
			}

		}

		return false;
	}

	/**
	 * Populates a block space
	 * @param blockSpace
	 * @param func
	 * @return A set of containers actually placed into the world.
	 */
	protected Set<Block> populateBlockSpace(Set<Vector3i> blockSpace, BiConsumer<Vector3i, Block> func) {
		Set<Block> populated = new HashSet<>();

		blockSpace.forEach(relativeBlockVec -> {
			//Note: relativeBlockVec is relative to globalPos.
			Optional<Block> opInnerContainer = getOrSetContainer();

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
	 * @return The container block
	 */
	public Optional<Block> getOrSetContainer() {

		Optional<Block> opCheckBlock = world.getBlock(globalPos);

		if (opCheckBlock.isPresent()) {
			Block checkBlock = opCheckBlock.get();
			if (checkBlock.factory().equals(Game.instance.blockManager.getAirBlockFactory())) {
				//It's air, so let's create a container
				checkBlock.world().setBlock(checkBlock.position(), MicroblockAPI.blockContainer);
				return checkBlock.world().getBlock(checkBlock.position());
			} else if (checkBlock.factory().equals(MicroblockAPI.blockContainer)) {
				//There's already a microblock there.
				return Optional.of(checkBlock);
			}
		}

		return Optional.empty();
	}
}
