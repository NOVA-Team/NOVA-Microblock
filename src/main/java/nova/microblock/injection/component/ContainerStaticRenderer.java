package nova.microblock.injection.component;

import nova.core.block.Block;
import nova.core.component.renderer.StaticRenderer;
import nova.microblock.micro.MicroblockContainer;

/**
 * @author Calclavia
 */
public class ContainerStaticRenderer extends StaticRenderer {

	public ContainerStaticRenderer(Block provider) {
		if (provider.components.has(MicroblockContainer.class)) {
			onRender(model -> provider
					.components.get(MicroblockContainer.class)
					.microblocks(StaticRenderer.class)
					.forEach(renderer -> renderer.onRender.accept(model))
			);
		}
	}
}
