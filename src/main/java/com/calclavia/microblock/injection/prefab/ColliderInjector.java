package com.calclavia.microblock.injection.prefab;

import nova.core.block.Block;
import nova.core.component.misc.Collider;
import nova.core.entity.Entity;
import nova.core.game.Game;
import nova.core.network.NetworkTarget;
import nova.core.util.RayTracer;
import nova.core.util.transform.shape.Cuboid;

import java.util.Optional;
import java.util.Set;

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
			collider.setBoundingBox(() -> {
					//Do ray trace to see which microblock is being looked at.
					if (NetworkTarget.Side.get().isClient()) {
						Entity player = Game.instance().clientManager().getPlayer();
						Optional<RayTracer.RayTraceBlockResult> result = RayTracer.rayTraceBlock(player, 7).stream().findFirst();

						if (result.isPresent()) {
							return result.get().hitCuboid.subtract(result.get().block.position());
						}
					}

					return collider.boundingBox.get();
				}
			);
			collider.setOcclusionBoxes(entity -> {
				Set<Cuboid> allBounds = collider.occlusionBoxes.apply(entity);
				allBounds.addAll(component.occlusionBoxes.apply(entity));
				return allBounds;
			});
		} else {
			Collider collider = container.add(new Collider());
			collider.setBoundingBox(component.boundingBox);
			collider.setOcclusionBoxes(component.occlusionBoxes::apply);
			collider.selectionBoxes = component.selectionBoxes;
		}

		//TODO: Set bounding box to change based on look.
		return true;
	}

	@Override
	public boolean injectBackward(Collider component, Block contained, Block container) {
		return super.injectBackward(component, contained, container);
	}
}