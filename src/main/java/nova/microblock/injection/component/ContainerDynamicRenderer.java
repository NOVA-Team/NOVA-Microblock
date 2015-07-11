package nova.microblock.injection.component;

import nova.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.component.renderer.DynamicRenderer;

/**
 * @author Calclavia
 */
public class ContainerDynamicRenderer extends DynamicRenderer {

	public ContainerDynamicRenderer(Block provider) {
		if (provider.has(MicroblockContainer.class)) {
			setOnRender(model -> provider
					.get(MicroblockContainer.class)
					.microblocks(DynamicRenderer.class)
					.forEach(renderer -> renderer.onRender.accept(model))
			);
		}
	}
}
