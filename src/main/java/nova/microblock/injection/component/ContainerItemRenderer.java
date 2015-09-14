package nova.microblock.injection.component;

import nova.core.block.Block;
import nova.core.component.renderer.ItemRenderer;
import nova.core.render.texture.Texture;
import nova.microblock.micro.MicroblockContainer;

import java.util.Optional;

/**
 * @author Calclavia
 */
public class ContainerItemRenderer extends ItemRenderer {

	public ContainerItemRenderer(Block provider) {
		if (provider.components.has(MicroblockContainer.class)) {
			onRender(model -> provider
					.components.get(MicroblockContainer.class)
					.microblocks(ItemRenderer.class)
					.forEach(renderer -> renderer.onRender.accept(model))
			);
		}
	}

	public ContainerItemRenderer(Block provider, Block contained) {
		onRender(contained.components.get(ItemRenderer.class).onRender::accept);
		Optional<Texture> texture = contained.components.get(ItemRenderer.class).texture;
		if (texture.isPresent()) {
			setTexture(texture.get());
		}
	}
}
