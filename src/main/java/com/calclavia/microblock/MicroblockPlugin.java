package com.calclavia.microblock;

import com.calclavia.microblock.common.BlockContainer;
import com.calclavia.microblock.injection.ComponentInjection;
import com.calclavia.microblock.injection.ComponentInjectionModule;
import com.calclavia.microblock.injection.component.ContainerCollider;
import com.calclavia.microblock.injection.prefab.CopyInjector;
import com.calclavia.microblock.injection.prefab.ForwardInjector;
import com.calclavia.microblock.injection.prefab.RendererInjector;
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
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;

import java.util.HashMap;
import java.util.Map;

/**
 * Make sure your mod loads AFTER this mod, if your mod uses microblocks or multiblock.
 * @author Calclavia
 */
@NovaMod(id = "microblock", name = "Microblock", version = "0.0.1", novaVersion = "0.0.1", modules = { ComponentInjectionModule.class }, isPlugin = true)
public class MicroblockPlugin implements Loadable {

	public static MicroblockPlugin instance;
	public static Map<String, MicroblockInjectFactory> containedIDToFactory = new HashMap<>();
	public static Map<BlockFactory, MicroblockInjectFactory> containedFactoryToFactory = new HashMap<>();
	public final ComponentInjection componentInjection;

	public MicroblockPlugin(ComponentInjection componentInjection) {
		this.componentInjection = componentInjection;
		instance = this;
	}

	@Override
	public void preInit() {
		componentInjection.register(args -> new ForwardInjector<>(Collider.class, ContainerCollider::new));
		componentInjection.register(args -> new RendererInjector<>(DynamicRenderer.class));
		componentInjection.register(args -> new RendererInjector<>(ItemRenderer.class));
		componentInjection.register(args -> new RendererInjector<>(StaticRenderer.class));
		componentInjection.register(args -> new CopyInjector<>(BlockTransform.class));
		componentInjection.register(args -> new CopyInjector<>(Category.class));

		//Replace block registration by sneakily providing our own way to put container blocks instead of the actual block.
		Game.instance.blockManager.blockRegisteredListeners.add(this::blockRegisterEvent, EventBus.PRIORITY_HIGH);
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
			//Check the contained factory's dummy, and injectForward components.
			dummy = new BlockContainer("blockContainer-" + containedFactory.getID());
			MicroblockPlugin.instance.componentInjection.injectForward(containedFactory.getDummy(), dummy);
		}

		@Override
		public Block getDummy() {
			return dummy;
		}
	}
}
