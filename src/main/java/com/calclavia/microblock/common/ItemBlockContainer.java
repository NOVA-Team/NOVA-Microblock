package com.calclavia.microblock.common;

import com.calclavia.microblock.MicroblockPlugin;
import com.calclavia.microblock.micro.Microblock;
import com.calclavia.microblock.micro.MicroblockContainer;
import com.calclavia.microblock.multi.Multiblock;
import com.calclavia.microblock.multi.MultiblockContainer;
import com.calclavia.microblock.operation.ContainerPlace;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.entity.Entity;
import nova.core.inventory.InventoryException;
import nova.core.item.ItemBlock;
import nova.core.network.NetworkTarget;
import nova.core.util.Direction;
import nova.core.util.RayTracer;
import nova.core.util.math.Vector3DUtil;
import nova.core.world.World;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;

/**
 * ItemBlocks for microblocks and multiblocks
 *
 * @author Calclavia
 */
public class ItemBlockContainer extends ItemBlock {

	public ItemBlockContainer(BlockFactory blockFactory) {
		super(blockFactory);

		rightClickEvent.add(
			evt -> {
				if (NetworkTarget.Side.get().isServer()) {
					//Do ray trace to find which block it hit
					RayTracer rayTracer = new RayTracer(evt.entity).setDistance(7);
					Optional<RayTracer.RayTraceBlockResult> hit = rayTracer.rayTraceBlocks(evt.entity.world()).findFirst();
					if (hit.isPresent()) {
						RayTracer.RayTraceBlockResult result = hit.get();
						Vector3D placePos = result.block.position().add(result.side.toVector());
						Optional<Block> opBlock = evt.entity.world().getBlock(placePos);
						opBlock.ifPresent(
							block ->
							{
								if (block.has(MicroblockContainer.class) || block.has(MultiblockContainer.class)) {
									placeContainer(evt.entity, evt.entity.world(), result.block.position(), result.side, Vector3DUtil.floor(result.hit.subtract(result.block.position())));
								}
							}
						);
					}
				}
			}
		);

		useEvent.add(evt -> evt.action = placeContainer(evt.entity, evt.entity.world(), evt.position, evt.side, evt.hit));
	}

	public boolean placeContainer(Entity entity, World world, Vector3D position, Direction side, Vector3D hit) {
		if (NetworkTarget.Side.get().isServer()) {
			Vector3D placePos = position.add(side.toVector());

			Optional<Block> checkBlock = world.getBlock(placePos);
			if (checkBlock.isPresent()) {

				MicroblockPlugin.MicroblockInjectFactory injectFactory = (MicroblockPlugin.MicroblockInjectFactory) this.blockFactory;
				BlockFactory containedFactory = injectFactory.containedFactory;
				Block dummy = containedFactory.getDummy();

				if (dummy.has(Microblock.class)) {
					//Ask the microblock about how it would like to be placed.
					Block.BlockPlaceEvent evt = new Block.BlockPlaceEvent(entity, side, hit, this);
					boolean b = new ContainerPlace(world, injectFactory, placePos, evt).operate();
					System.out.println("Attempt to place microblock: " + b);
					return b;
				} else if (dummy.has(Multiblock.class)) {
					return new ContainerPlace(world, injectFactory, placePos).operate();
				} else {
					throw new InventoryException("Invalid blockFactory contained in ItemBlockContainer: " + containedFactory);
				}
			}
		}
		return false;
	}
}
