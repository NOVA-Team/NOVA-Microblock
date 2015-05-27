package com.calclavia.microblock.core.common;

import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.multi.Multiblock;
import com.calclavia.microblock.core.MicroblockAPI;
import com.calclavia.microblock.core.micro.MicroblockContainer;
import com.calclavia.microblock.core.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.entity.Entity;
import nova.core.game.Game;
import nova.core.item.ItemBlock;
import nova.core.util.Direction;
import nova.core.util.exception.NovaException;
import nova.core.util.transform.vector.Vector3d;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

import java.util.Optional;

/**
 * ItemBlocks for microblocks
 * @author Calclavia
 */
public class ItemBlockContainer extends ItemBlock {

	public ItemBlockContainer(BlockFactory blockFactory) {
		super(blockFactory);
	}

	@Override
	public boolean onUse(Entity entity, World world, Vector3i position, Direction side, Vector3d hit) {
		Vector3i placePos = position.add(side.toVector());

		Optional<Block> checkBlock = world.getBlock(placePos);
		if (checkBlock.isPresent()) {
			//Instantiate microblock
			Optional<Block> opBlockContainer = getOrSetMicroblockContainer(checkBlock.get());

			if (opBlockContainer.isPresent()) {
				Block blockContainer = opBlockContainer.get();

				boolean used = false;
				Block dummy = blockFactory.getDummy();

				if (dummy.has(Microblock.class)) {
					//This is a microblock!
					blockContainer
						.getOrAdd(new MicroblockContainer(blockContainer))
						.add(blockFactory);
					used = true;
				}

				if (dummy.has(Multiblock.class)) {
					//This is a multiblock!
					blockContainer
						.getOrAdd(new MultiblockContainer(blockContainer))
						.add(blockFactory);
					used = true;
				}

				if (used) {
					return true;
				}

				throw new NovaException("A block is using ItemBlockContainer without attaching a microblock or multiblock component!");
			}
		}
		//TODO: Post add operation?
		return false;
	}

	protected Optional<Block> getOrSetMicroblockContainer(Block checkBlock) {
		if (checkBlock.factory().equals(Game.instance.blockManager.getAirBlockFactory())) {
			//It's air, so let's create a container
			checkBlock.world().setBlock(checkBlock.position(), MicroblockAPI.blockContainer);
			return checkBlock.world().getBlock(checkBlock.position());
		} else if (checkBlock.factory().equals(MicroblockAPI.blockContainer)) {
			//There's already a microblock there.
			return Optional.of(checkBlock);
		}

		return Optional.empty();
	}
}
