package com.calclavia.microblock.core;

import com.calclavia.microblock.core.common.BlockContainer;
import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.multi.Multiblock;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.block.BlockManager;
import nova.core.event.EventBus;
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;
import nova.internal.dummy.Wrapper;

import java.util.function.Function;

/**
 * Make sure your mod loads AFTER this mod, if your mod uses microblocks or multiblock.
 * @author Calclavia
 */
@NovaMod(id = "microblock", name = "Microblock", version = "0.0.1", novaVersion = "0.0.1", isPlugin = true)
public class MicroblockAPI implements Loadable {

	public static BlockFactory blockContainer;

	@Override
	public void preInit() {
		blockContainer = Game.instance.blockManager.register(BlockContainer.class);

		//Replace block registration by sneakily providing our own way to put container blocks instead of the actual block.
		Game.instance.blockManager.blockRegisteredListeners.add(this::blockRegisterEvent, EventBus.PRIORITY_HIGH);
	}

	private void blockRegisterEvent(BlockManager.BlockRegisteredEvent evt) {
		BlockFactory blockFactory = evt.blockFactory;

		//Handle microblock registration
		if (blockFactory.getDummy().has(Microblock.class) || blockFactory.getDummy().has(Multiblock.class)) {
			evt.blockFactory = new MicroblockInjectFactory(evt.blockFactory);
			evt.cancel();
		}
	}

	public static class MicroblockInjectFactory extends BlockFactory {
		public final BlockFactory containedFactory;

		public MicroblockInjectFactory(BlockFactory containedFactory) {
			super(blockContainer::makeBlock);
			this.containedFactory = containedFactory;
		}
	}
}
