package nova.microblock.injection.component;

import nova.core.block.Block;
import nova.core.component.renderer.DynamicRenderer;
import nova.microblock.micro.MicroblockContainer;

/**
 * @author Calclavia
 */
public class ContainerDynamicRenderer extends DynamicRenderer {

	public ContainerDynamicRenderer(Block provider) {
		if (provider.components.has(MicroblockContainer.class)) {
			onRender(model -> provider
					.components.get(MicroblockContainer.class)
					.microblocks(DynamicRenderer.class)
					.forEach(renderer -> renderer.onRender.accept(model))
			);
		}
	}
}
