package com.calclavia.microblock.operation;

import com.calclavia.microblock.injection.component.ContainerCollider;
import com.calclavia.microblock.micro.Microblock;
import com.calclavia.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.entity.Entity;
import nova.core.event.Event;
import nova.core.event.GlobalEvents;
import nova.core.util.Direction;
import nova.core.util.RayTracer;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3d;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Removes a container
 * @author Calclavia
 */
public class ContainerRemove extends ContainerOperation {
	private final Vector3i localPos;

	public ContainerRemove(World world, Vector3i globalPos, Vector3i localPos) {
		super(world, globalPos);
		this.localPos = localPos;
	}

	public static void interactEventHandler(GlobalEvents.PlayerInteractEvent evt) {
		if (evt.action == GlobalEvents.PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
			Entity player = evt.player;
			Optional<Block> opBlock = evt.world.getBlock(evt.position);

			if (opBlock.isPresent()) {
				Block block = opBlock.get();

				if (block.has(MicroblockContainer.class) && block.has(ContainerCollider.class)) {
					MicroblockContainer microblockContainer = block.get(MicroblockContainer.class);

					//Ray trace through each microblock
					Stream<RayTraceMicroblockResult> traces = Stream.empty();

					for (Microblock microblock : microblockContainer.microblocks()) {
						RayTracer rayTracer = new RayTracer(player).setDistance(7);

						traces = Stream.concat(traces,
							rayTracer
								.rayTraceCollider(microblock.block, (pos, cuboid) -> new RayTraceMicroblockResult(pos, rayTracer.ray.origin.distance(pos), cuboid.sideOf(pos), cuboid, microblock))
						);
					}

					Optional<RayTraceMicroblockResult> result = traces.findFirst();

					if (result.isPresent()) {
						if (new ContainerRemove(evt.world, evt.position, result.get().microblock.position).operate()) {
							evt.cancel();
						}
					}
				}
			}
		}
	}

	@Override
	public boolean operate() {
		Optional<Block> opBlock = world.getBlock(globalPos);

		if (opBlock.isPresent()) {
			Block block = opBlock.get();

		}

		return false;
	}

	public static class RayTraceMicroblockResult extends RayTracer.RayTraceResult {
		public final Microblock microblock;

		public RayTraceMicroblockResult(Vector3d hit, double distance, Direction side, Cuboid hitCuboid, Microblock microblock) {
			super(hit, distance, side, hitCuboid);
			this.microblock = microblock;
		}

	}
}
