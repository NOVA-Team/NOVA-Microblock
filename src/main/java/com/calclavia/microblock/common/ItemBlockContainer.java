package com.calclavia.microblock.common;

import com.calclavia.microblock.MicroblockAPI;
import com.calclavia.microblock.micro.Microblock;
import com.calclavia.microblock.micro.MicroblockContainer;
import com.calclavia.microblock.multi.Multiblock;
import com.calclavia.microblock.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.entity.Entity;
import nova.core.item.ItemBlock;
import nova.core.network.NetworkTarget;
import nova.core.util.Direction;
import nova.core.util.RayTracer;
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

		rightClickEvent.add(
			evt -> {
				if (NetworkTarget.Side.get().isServer()) {
					//Do ray trace to find which block it hit
					//TODO: Check server ray trace inaccurancy?
					Optional<RayTracer.RayTraceBlockResult> hit = RayTracer.rayTraceBlock(evt.entity, 7).stream().findFirst();
					if (hit.isPresent()) {
						RayTracer.RayTraceBlockResult result = hit.get();
						//TODO: WHy shift one block?
						Vector3i placePos = result.block.position().add(result.side.toVector()).subtract(new Vector3i(0, 1, 0));
						Optional<Block> opBlock = evt.entity.world().getBlock(placePos);
						System.out.println(placePos);
						opBlock.ifPresent(
							block ->
							{
								if (block.has(MicroblockContainer.class) || block.has(MultiblockContainer.class)) {
									placeContainer(evt.entity, evt.entity.world(), result.block.position(), result.side, result.hit.subtract(result.block.position().toDouble()));
								}
							}
						);
					}
				}
			}
		);

		useEvent.add(evt -> evt.action = placeContainer(evt.entity, evt.entity.world(), evt.position, evt.side, evt.hit));
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
