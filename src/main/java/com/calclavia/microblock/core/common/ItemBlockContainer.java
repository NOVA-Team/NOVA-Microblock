package com.calclavia.microblock.core.common;

import com.calclavia.microblock.core.MicroblockAPI;
import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.multi.Multiblock;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.entity.Entity;
import nova.core.item.ItemBlock;
import nova.core.network.NetworkTarget;
import nova.core.util.Direction;
import nova.core.util.exception.NovaException;
import nova.core.util.transform.vector.Vector3d;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

import java.util.Optional;

/**
 * ItemBlocks for microblocks and multiblocks
 * @author Calclavia
 */
public class ItemBlockContainer extends ItemBlock {

	public ItemBlockContainer(BlockFactory blockFactory) {
		super(blockFactory);
	}

	@Override
	public void onRightClick(Entity entity) {
		if (NetworkTarget.Side.get().isServer()) {
			//Do Raycast

		}
	}

	@Override
	public boolean onUse(Entity entity, World world, Vector3i position, Direction side, Vector3d hit) {
		return placeContainer(entity, world, position, side, hit);
	}

	public boolean placeContainer(Entity entity, World world, Vector3i position, Direction side, Vector3d hit) {
		if (NetworkTarget.Side.get().isServer()) {
			Vector3i placePos = position.add(side.toVector());

			Optional<Block> checkBlock = world.getBlock(placePos);
			if (checkBlock.isPresent()) {

				MicroblockAPI.MicroblockInjectFactory injectFactory = (MicroblockAPI.MicroblockInjectFactory) this.blockFactory;
				BlockFactory containedFactory = injectFactory.containedFactory;
				Block dummy = containedFactory.getDummy();

				if (dummy.has(Microblock.class)) {
					//Ask the microblock about how it would like to be placed.
					Block.BlockPlaceEvent evt = new Block.BlockPlaceEvent(entity, side, hit, this);
					return new MicroblockOperation(world, injectFactory, placePos, evt).setBlock();
				} else if (dummy.has(Multiblock.class)) {
					return new MicroblockOperation(world, injectFactory, placePos).setBlock();
				} else {
					throw new NovaException("Invalid blockFactory contained in ItemBlockContainer: " + containedFactory);
				}
			}
		}
		return false;
	}
}
