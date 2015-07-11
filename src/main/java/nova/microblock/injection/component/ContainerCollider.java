package nova.microblock.injection.component;

import nova.microblock.MicroblockPlugin;
import nova.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.component.misc.Collider;
import nova.core.entity.Entity;
import nova.core.network.NetworkTarget;
import nova.core.util.RayTracer;
import nova.core.util.shape.Cuboid;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Calclavia
 */
public class ContainerCollider extends Collider {
	public final Block blockContainer;

	public ContainerCollider(Block container) {
		this.blockContainer = container;
		setBoundingBox(this::getBoundingBox);
		setOcclusionBoxes(this::getOcclusionBoxes);
		setSelectionBoxes(this::getSelectionBoxes);
		isCube(false);
		isOpaqueCube(false);
	}

	public Cuboid getBoundingBox() {
		if (blockContainer.has(MicroblockContainer.class)) {
			//Do ray trace to see which microblock is being looked at.
			if (NetworkTarget.Side.get().isClient()) {
				Entity player = MicroblockPlugin.instance.client.getPlayer();

				Optional<RayTracer.RayTraceBlockResult> result = new RayTracer(player)
					.setDistance(7)
					.rayTraceBlocks(Collections.singleton(blockContainer))
					.findFirst();

				return result.map(res -> res.hitCuboid.subtract(blockContainer.transform().position())).orElseGet(() -> Cuboid.ZERO);
			}
		}
		//TODO: Is this the right thing to return?
		return Cuboid.ONE;
	}

	public Set<Cuboid> getOcclusionBoxes(Optional<Entity> entity) {
		if (blockContainer.has(MicroblockContainer.class)) {
			MicroblockContainer microblockContainer = blockContainer.get(MicroblockContainer.class);
			return microblockContainer.microblocks(Collider.class)
				.flatMap(collider -> collider.occlusionBoxes.apply(entity).stream())
				.collect(Collectors.toSet());
		}

		return Collections.emptySet();
	}

	public Set<Cuboid> getSelectionBoxes(Optional<Entity> entity) {
		if (blockContainer.has(MicroblockContainer.class)) {
			MicroblockContainer microblockContainer = blockContainer.get(MicroblockContainer.class);
			return microblockContainer.microblocks(Collider.class)
				.flatMap(collider -> collider.selectionBoxes.apply(entity).stream())
				.collect(Collectors.toSet());
		}

		return Collections.emptySet();
	}
}
