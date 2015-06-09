package com.calclavia.microblock.injection.component;

import com.calclavia.microblock.MicroblockPlugin;
import com.calclavia.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.component.misc.Collider;
import nova.core.entity.Entity;
import nova.core.network.NetworkTarget;
import nova.core.util.RayTracer;
import nova.core.util.shape.Cuboid;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Calclavia
 */
public class ContainerCollider extends Collider {
	public final Block blockContainer;

	public ContainerCollider(Block container) {
		this.blockContainer = container;

		if (blockContainer.has(MicroblockContainer.class)) {
			MicroblockContainer microblockContainer = blockContainer.get(MicroblockContainer.class);

			setBoundingBox(() -> {
				//Do ray trace to see which microblock is being looked at.
				if (NetworkTarget.Side.get().isClient()) {
					Entity player = MicroblockPlugin.instance.client.getPlayer();

					Optional<RayTracer.RayTraceBlockResult> result = new RayTracer(player)
						.setDistance(7)
						.rayTraceBlocks(Collections.singleton(blockContainer))
						.findFirst();

					return result.map(res -> res.hitCuboid.subtract(container.transform().position())).orElseGet(() -> Cuboid.ZERO);
				}

				//TODO: Is this the right thing to return?
				return Cuboid.ONE;
			});

			setOcclusionBoxes(entity ->
					microblockContainer.microblocks(Collider.class)
						.flatMap(collider -> collider.occlusionBoxes.apply(entity).stream())
						.collect(Collectors.toSet())
			);

			setSelectionBoxes(entity ->
					microblockContainer.microblocks(Collider.class)
						.flatMap(collider -> collider.selectionBoxes.apply(entity).stream())
						.collect(Collectors.toSet())
			);
		}
		isCube(false);
		isOpaqueCube(false);
	}
}
