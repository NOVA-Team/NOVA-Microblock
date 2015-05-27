package com.calclavia.microblock.core;

import com.calclavia.microblock.core.common.BlockContainer;
import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.micro.MicroblockContainer;
import com.calclavia.microblock.core.multi.Multiblock;
import com.calclavia.microblock.core.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;
import nova.core.util.exception.NovaException;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3d;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Calclavia
 */
@NovaMod(id = "microblock", name = "Microblock", version = "0.0.1", novaVersion = "0.0.1", isPlugin = true)
public class MicroblockAPI implements Loadable {

	public static BlockFactory blockContainer;

	//TODO: What happens when we try to set it to the void? Fail/remove all the blocks.
	public static boolean setBlock(World world, Block newBlock, Vector3i globalPos, Vector3i localPos) {
		Optional<Block> opContainer = getOrSetContainer(world, globalPos);

		if (opContainer.isPresent()) {
			Block container = opContainer.get();

			//Add transform component
			newBlock.add(container.transform());

			if (newBlock.has(Microblock.class)) {
				//This is a microblock
				//Attach microblock container to the container
				MicroblockContainer microblockContainer = container.add(new MicroblockContainer(container));

				if (!new Cuboid(0, 0, 0, microblockContainer.subdivisions, microblockContainer.subdivisions, microblockContainer.subdivisions).intersects(localPos)) {
					throw new NovaException("Invalid local position for microblock: " + localPos);
				}

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

					blockSpace.forEach(relativeBlockVec -> {
						//Note: relativeBlockVec is relative to globalPos.
						Optional<Block> opInnerContainer = getOrSetContainer(world, relativeBlockVec.add(globalPos));

						if (opInnerContainer.isPresent()) {
							//Creates the outer container block that will exist in the world.
							Block outerContainerBlock = opInnerContainer.get();
							MicroblockContainer outerContainer = outerContainerBlock.getOrAdd(new MicroblockContainer(outerContainerBlock));

							//Create a new multiblock inner container that lives inside the microblock structure.
							BlockContainer innerContainer = new BlockContainer();
							multiblock.setContainer(innerContainer.add(new MultiblockContainer(innerContainer)));
							innerContainer.add(new Microblock(innerContainer)).setContainer(outerContainer);

							//Add transform component
							innerContainer.add(outerContainerBlock.transform());

							Set<Vector3i> localPositions = occupiedSpace.stream()
								.filter(vec -> new Cuboid(relativeBlockVec, relativeBlockVec.add(1)).intersects(vec)) //Filters blocks relevant to the relativeBlockVec
								.map(vec -> vec.subtract(relativeBlockVec.toDouble())) //Maps positions relative to its own block space
								.map(vec -> vec.multiply(microblockContainer.subdivisions))//Multiply all unit vectors by subdivision size, converting it to local vectors.
								.map(Vector3d::toInt)
								.collect(Collectors.toSet());

							localPositions.forEach(vec -> outerContainer.blockMap.put(vec, innerContainer.get(Microblock.class)));
						}
						//TODO: What happens when we try to set it to the void? Fail/remove all the blocks.
					});
				} else {
					/**
					 * Build microblocks without multiblocks
					 */
					microblockContainer.blockMap.put(localPos, newBlock.get(Microblock.class));
				}
			} else if (newBlock.has(Multiblock.class)) {

				Multiblock multiblock = newBlock.get(Multiblock.class);

				//Build multiblock without microblocks
				Set<Vector3i> blockSpace = multiblock.getOccupiedSpace(1)
					.stream()
					.map(Vector3d::toInt)
					.collect(Collectors.toSet());

				blockSpace.forEach(relativeBlockVec -> {
					//Note: relativeBlockVec is relative to globalPos.
					Optional<Block> opInnerContainer = getOrSetContainer(world, relativeBlockVec.add(globalPos));

					if (opInnerContainer.isPresent()) {
						//Creates the outer container block that will exist in the world.
						Block outerContainerBlock = opInnerContainer.get();
						MultiblockContainer outerContainer = outerContainerBlock.getOrAdd(new MultiblockContainer(outerContainerBlock));
						multiblock.setContainer(outerContainer);
					}
				});
			}

		}

		return false;
	}

	/**
	 * Checks a position in the world and either gets or sets the position into {@link BlockContainer}
	 * @param world The world
	 * @param globalPos The global world position
	 * @return The container block
	 */
	public static Optional<Block> getOrSetContainer(World world, Vector3i globalPos) {

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

	@Override
	public void preInit() {
		blockContainer = Game.instance.blockManager.register(BlockContainer.class);

		//TODO: Replace block factory with a sneaky factory that switches all blocks to BlockContainer.
	}
}
