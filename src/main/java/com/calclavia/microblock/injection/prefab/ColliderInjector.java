package com.calclavia.microblock.injection.prefab;

import nova.core.block.Block;
import nova.core.component.misc.Collider;
import nova.core.util.transform.shape.Cuboid;

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
		Collider collider = container.getOrAdd(new Collider());
		collider.setOcclusionBoxes(entity -> {
			Set<Cuboid> allBounds = collider.occlusionBoxes.apply(entity);
			allBounds.addAll(component.occlusionBoxes.apply(entity));
			return allBounds;
		});

		//TODO: Set bounding box to change based on look.
		return true;
	}

	@Override
	public boolean injectBackward(Collider component, Block contained, Block container) {
		return super.injectBackward(component, contained, container);
	}
}