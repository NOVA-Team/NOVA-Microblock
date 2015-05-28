package com.calclavia.microblock.core;

import com.calclavia.microblock.core.common.BlockContainer;
import com.calclavia.microblock.core.common.ComponentInjector;
import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.multi.Multiblock;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.block.BlockManager;
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
@NovaMod(id = "microblock", name = "Microblock", version = "0.0.1", novaVersion = "0.0.1", isPlugin = true)
public class MicroblockAPI implements Loadable {

	public static Map<String, BlockFactory> containedIDToContainer = new HashMap<>();
	public static Map<BlockFactory, BlockFactory> containedFactoryToContainer = new HashMap<>();

	@Override
	public void preInit() {
		//Replace block registration by sneakily providing our own way to put container blocks instead of the actual block.
		Game.instance.blockManager.blockRegisteredListeners.add(this::blockRegisterEvent, EventBus.PRIORITY_HIGH);
	}

	private void blockRegisterEvent(BlockManager.BlockRegisteredEvent evt) {
		BlockFactory blockFactory = evt.blockFactory;

		if (blockFactory.getDummy().has(Microblock.class) || blockFactory.getDummy().has(Multiblock.class)) {
			//Sneaky block factory replacement
			MicroblockInjectFactory microblockInjectFactory = new MicroblockInjectFactory(evt.blockFactory);
			containedIDToContainer.put(evt.blockFactory.getID(), microblockInjectFactory);
			containedFactoryToContainer.put(evt.blockFactory, microblockInjectFactory);
			evt.blockFactory = microblockInjectFactory;
		}
	}

	public static class MicroblockInjectFactory extends BlockFactory {
		public final BlockFactory containedFactory;

		public MicroblockInjectFactory(BlockFactory containedFactory) {
			super(new ComponentInjector().construct(containedFactory));
			this.containedFactory = containedFactory;
		}
	}
}
