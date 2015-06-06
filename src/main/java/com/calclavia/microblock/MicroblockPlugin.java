package com.calclavia.microblock;

import com.calclavia.microblock.common.BlockContainer;
import com.calclavia.microblock.injection.ComponentInjection;
import com.calclavia.microblock.injection.ComponentInjectionModule;
import com.calclavia.microblock.injection.component.ContainerCollider;
import com.calclavia.microblock.injection.component.ContainerDynamicRenderer;
import com.calclavia.microblock.injection.component.ContainerItemRenderer;
import com.calclavia.microblock.injection.component.ContainerStaticRenderer;
import com.calclavia.microblock.injection.prefab.CopyInjector;
import com.calclavia.microblock.injection.prefab.ForwardInjector;
import com.calclavia.microblock.micro.Microblock;
import com.calclavia.microblock.multi.Multiblock;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.block.BlockManager;
import nova.core.component.Category;
import nova.core.component.misc.Collider;
import nova.core.component.renderer.DynamicRenderer;
import nova.core.component.renderer.ItemRenderer;
import nova.core.component.renderer.StaticRenderer;
import nova.core.component.transform.BlockTransform;
import nova.core.event.EventBus;
import nova.core.game.ClientManager;
import nova.core.item.ItemManager;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;
import nova.core.network.NetworkManager;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Make sure your mod loads AFTER this mod, if your mod uses microblocks or multiblock.
 *
 * @author Calclavia
 */
@NovaMod(id = "microblock", name = "Microblock", version = "0.0.1", novaVersion = "0.0.1", modules = { ComponentInjectionModule.class }, isPlugin = true)
public class MicroblockPlugin implements Loadable {

	public static MicroblockPlugin instance;

	public final ComponentInjection componentInjection;
	public final ClientManager client;
	public final NetworkManager network;
	public final ItemManager items;
	public final BlockManager blocks;
	public final Logger logger;

	public final Map<String, MicroblockInjectFactory> containedIDToFactory = new HashMap<>();
	public final Map<BlockFactory, MicroblockInjectFactory> containedFactoryToFactory = new HashMap<>();

	public MicroblockPlugin(ComponentInjection componentInjection, ClientManager client, NetworkManager network, ItemManager items, BlockManager blocks, Logger logger) {
		this.componentInjection = componentInjection;
		this.client = client;
		this.network = network;
		this.items = items;
		this.blocks = blocks;
		this.logger = logger;
		instance = this;
	}

	@Override
	public void preInit() {
		MicroblockPlugin.instance.network.register(new MicroblockPacket());

		componentInjection.register(args -> new ForwardInjector<>(Collider.class, ContainerCollider::new));
		componentInjection.register(args -> new ForwardInjector<>(DynamicRenderer.class, ContainerDynamicRenderer::new));
		componentInjection.register(args -> new ForwardInjector<>(ItemRenderer.class, ContainerItemRenderer::new));
		componentInjection.register(args -> new ForwardInjector<>(StaticRenderer.class, ContainerStaticRenderer::new));
		componentInjection.register(args -> new CopyInjector<>(BlockTransform.class));
		componentInjection.register(args -> new CopyInjector<>(Category.class));

		//Replace block registration by sneakily providing our own way to put container blocks instead of the actual block.
		MicroblockPlugin.instance.blocks.blockRegisteredListeners.add(this::blockRegisterEvent, EventBus.PRIORITY_HIGH);
	}

	private void blockRegisterEvent(BlockManager.BlockRegisteredEvent evt) {
		BlockFactory blockFactory = evt.blockFactory;

		if (blockFactory.getDummy().has(Microblock.class) || blockFactory.getDummy().has(Multiblock.class)) {
			//Sneaky block factory replacement
			MicroblockInjectFactory microblockInjectFactory = new MicroblockInjectFactory(evt.blockFactory);
			containedIDToFactory.put(evt.blockFactory.getID(), microblockInjectFactory);
			containedFactoryToFactory.put(evt.blockFactory, microblockInjectFactory);
			evt.blockFactory = microblockInjectFactory;
		}
	}

	public static class MicroblockInjectFactory extends BlockFactory {
		public final BlockFactory containedFactory;
		private final BlockContainer dummy;

		public MicroblockInjectFactory(BlockFactory containedFactory) {
			super(args -> new BlockContainer("blockContainer-" + containedFactory.getID()));
			this.containedFactory = containedFactory;
			//Check the contained factory's dummy, and injectToContainer components.
			dummy = new BlockContainer("blockContainer-" + containedFactory.getID());

			//Inject item renderer
			if (containedFactory.getDummy().has(ItemRenderer.class)) {
				dummy.add(new ContainerItemRenderer(dummy, containedFactory.getDummy()));
			}
			MicroblockPlugin.instance.componentInjection.injectToContainer(containedFactory.getDummy(), dummy);
		}

		@Override
		public Block getDummy() {
			return dummy;
		}
	}
}
