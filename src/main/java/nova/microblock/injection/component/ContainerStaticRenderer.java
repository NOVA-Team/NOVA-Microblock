package nova.microblock.injection.component;

import nova.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.component.renderer.StaticRenderer;

/**
 * @author Calclavia
 */
public class ContainerStaticRenderer extends StaticRenderer {

	public ContainerStaticRenderer(Block provider) {
		super(provider);

		if (provider.has(MicroblockContainer.class)) {
			setOnRender(model -> provider
					.get(MicroblockContainer.class)
					.microblocks(StaticRenderer.class)
					.forEach(renderer -> renderer.onRender.accept(model))
			);
		}
	}
}
