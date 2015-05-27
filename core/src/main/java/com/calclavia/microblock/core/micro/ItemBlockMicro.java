package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.core.MicroblockAPI;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.game.Game;
import nova.core.item.ItemBlock;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

import java.util.Optional;

/**
 * ItemBlocks for microblocks
 * @author Calclavia
 */
public class ItemBlockMicro extends ItemBlock {

	public ItemBlockMicro(BlockFactory blockFactory) {
		super(blockFactory);
	}

	@Override
	protected boolean onPrePlace(World world, Vector3i placePos) {
		Optional<Block> checkBlock = world.getBlock(placePos);
		if (checkBlock.isPresent()) {
			//Instantiate microblock
			NovaMicroblockContainer container = getOrSetMicroblockContainer(checkBlock.get());

			if (container != null) {
				//TODO: Add microblock
				return true;
			}
		}
		return false;
	}

	protected NovaMicroblockContainer getOrSetMicroblockContainer(Block checkBlock) {
		if (checkBlock.factory().equals(Game.instance.blockManager.getAirBlockFactory())) {
			//It's air, so let's create a container
			checkBlock.world().setBlock(checkBlock.position(), MicroblockAPI.microblock);
			return checkBlock.world().getBlock(checkBlock.position()).get().get(NovaMicroblockContainer.class);
		} else if (checkBlock.factory().equals(MicroblockAPI.microblock)) {
			//There's already a microblock there.
			return checkBlock.get(NovaMicroblockContainer.class);
		}

		return null;
	}
}
