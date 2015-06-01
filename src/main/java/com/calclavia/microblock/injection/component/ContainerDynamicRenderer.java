package com.calclavia.microblock.injection.component;

import com.calclavia.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.block.component.StaticBlockRenderer;
import nova.core.component.renderer.DynamicRenderer;

/**
 * @author Calclavia
 */
public class ContainerDynamicRenderer extends DynamicRenderer {

	public ContainerDynamicRenderer(Block provider) {
		super(provider);

		if (provider.has(MicroblockContainer.class)) {
			setOnRender(model -> provider
					.get(MicroblockContainer.class)
					.microblocks(DynamicRenderer.class)
					.forEach(renderer -> renderer.onRender.accept(model))
			);
		}
	}
}
