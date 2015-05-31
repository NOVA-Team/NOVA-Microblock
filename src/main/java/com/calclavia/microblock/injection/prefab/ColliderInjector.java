package com.calclavia.microblock.injection.prefab;

import nova.core.block.Block;
import nova.core.component.misc.Collider;
import nova.core.entity.Entity;
import nova.core.game.Game;
import nova.core.network.NetworkTarget;
import nova.core.util.RayTracer;
import nova.core.util.transform.shape.Cuboid;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Calclavia
 */
public class ColliderInjector extends DefaultInjector<Collider> {
	public ColliderInjector() {
		super(Collider.class);
	}

	@Override
	public boolean injectForward(Collider component, Block contained, Block container) {
		if (container.has(Collider.class)) {
			Collider collider = container.get(Collider.class);
			final Supplier<Cuboid> boundingBox = collider.boundingBox;
			collider.setBoundingBox(() -> {
					//Do ray trace to see which microblock is being looked at.
					if (NetworkTarget.Side.get().isClient()) {
						Entity player = Game.instance().clientManager().getPlayer();

						Optional<RayTracer.RayTraceBlockResult> result = new RayTracer(player)
							.setDistance(7)
							.rayTraceBlocks(player.world())
							.findFirst();

						if (result.isPresent()) {
							return result.get().hitCuboid.subtract(result.get().block.position());
						}
					}

					return boundingBox.get();
				}
			);
			final Function<Optional<Entity>, Set<Cuboid>> occlusionBoxes = collider.occlusionBoxes;
			collider.setOcclusionBoxes(entity -> {
				Set<Cuboid> cuboidHashSet = new HashSet<>();
				cuboidHashSet.addAll(occlusionBoxes.apply(entity));
				cuboidHashSet.addAll(component.occlusionBoxes.apply(entity));
				return cuboidHashSet;
			});

			final Function<Optional<Entity>, Set<Cuboid>> selectionBoxes = collider.selectionBoxes;
			collider.selectionBoxes = entity -> {
				Set<Cuboid> cuboidHashSet = new HashSet<>();
				cuboidHashSet.addAll(selectionBoxes.apply(entity));
				cuboidHashSet.addAll(component.selectionBoxes.apply(entity));
				return cuboidHashSet;
			};
		} else {
			Collider collider = container.add(new Collider());
			collider.setBoundingBox(component.boundingBox::get);
			collider.setOcclusionBoxes(component.occlusionBoxes::apply);
			collider.selectionBoxes = component.selectionBoxes;
			collider.isCube(false);
			collider.isOpaqueCube(false);
		}

		//TODO: Set bounding box to change based on look.
		return true;
	}

	@Override
	public boolean injectBackward(Collider component, Block contained, Block container) {
		return super.injectBackward(component, contained, container);
	}
}