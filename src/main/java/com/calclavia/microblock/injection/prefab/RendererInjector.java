package com.calclavia.microblock.injection.prefab;

import nova.core.block.Block;
import nova.core.block.component.StaticBlockRenderer;
import nova.core.component.renderer.DynamicRenderer;
import nova.core.component.renderer.ItemRenderer;
import nova.core.component.renderer.Renderer;
import nova.core.component.renderer.StaticRenderer;

/**
 * @author Calclavia
 */
public class RendererInjector<COMPONENT extends Renderer> extends DefaultInjector<COMPONENT> {
	public RendererInjector(Class<COMPONENT> type) {
		super(type);
	}

	@Override
	public boolean injectForward(COMPONENT component, Block contained, Block container) {
		Renderer renderer = null;

		if (component instanceof DynamicRenderer) {
			renderer = container.getOrAdd(new DynamicRenderer(container));
		} else if (component instanceof StaticBlockRenderer) {
			renderer = container.getOrAdd(new StaticBlockRenderer(container));
		} else if (component instanceof StaticRenderer) {
			renderer = container.getOrAdd(new StaticRenderer(container));
		} else if (component instanceof ItemRenderer) {
			renderer = container.getOrAdd(new ItemRenderer(container));
		}

		if (renderer != null) {
			renderer.onRender = renderer.onRender.andThen(component.onRender);
			return true;
		}

		return false;
	}
}
