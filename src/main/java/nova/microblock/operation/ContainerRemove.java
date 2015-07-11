package nova.microblock.operation;

import nova.microblock.MicroblockPlugin;
import nova.microblock.injection.component.ContainerCollider;
import nova.microblock.micro.Microblock;
import nova.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.entity.Entity;
import nova.core.util.Direction;
import nova.core.util.RayTracer;
import nova.core.util.shape.Cuboid;
import nova.core.world.World;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * Removes a container
 *
 * @author Calclavia
 */
public class ContainerRemove extends ContainerOperation {
	private final Vector3D localPos;

	public ContainerRemove(World world, Vector3D globalPos, Vector3D localPos) {
		super(world, globalPos);
		this.localPos = localPos;
	}

	public static void interactEventHandler(Block block, Block.RemoveEvent evt) {
		if (evt.entity.isPresent()) {
			Entity player = evt.entity.get();

			if (block.has(MicroblockContainer.class) && block.has(ContainerCollider.class)) {
				MicroblockContainer microblockContainer = block.get(MicroblockContainer.class);

				//Ray trace through each microblock
				Stream<RayTraceMicroblockResult> traces = Stream.empty();

				for (Microblock microblock : microblockContainer.microblocks()) {
					RayTracer rayTracer = new RayTracer(player).setDistance(7);

					traces = Stream.concat(traces,
						rayTracer.rayTraceCollider(microblock.block, (pos, cuboid) -> new RayTraceMicroblockResult(pos, rayTracer.ray.origin.distance(pos), cuboid.sideOf(pos), cuboid, microblock))
					);
				}

				Optional<RayTraceMicroblockResult> result = traces.sorted().findFirst();
				if (result.isPresent()) {
					if (new ContainerRemove(block.world(), block.position(), result.get().microblock.position).operate()) {
						evt.result = false;
					}
					//If single player
					//TODO: HACKS TEMPORARY
					MicroblockPlugin.instance.client.getPlayer().world().setBlock(block.position(), block.factory());
					//System.out.println("client: " + MicroblockPlugin.instance.clientManager().getPlayer().world().getBlock(block.position()).get().getID());
				}
			}
		}
	}

	@Override
	public boolean operate() {
		Optional<Block> opBlock = world.getBlock(globalPos);

		if (opBlock.isPresent()) {
			Block block = opBlock.get();

			if (block.has(MicroblockContainer.class)) {
				MicroblockContainer microblockContainer = block.get(MicroblockContainer.class);

				if (microblockContainer.remove(localPos)) {
					if (microblockContainer.microblocks().size() == 0) {
						return false;
					}
					return true;
				}

			}
			//TODO: Deal withPriority multiblocks
		}

		return false;
	}

	public static class RayTraceMicroblockResult extends RayTracer.RayTraceResult {
		public final Microblock microblock;

		public RayTraceMicroblockResult(Vector3D hit, double distance, Direction side, Cuboid hitCuboid, Microblock microblock) {
			super(hit, distance, side, hitCuboid);
			this.microblock = microblock;
		}

	}
}
