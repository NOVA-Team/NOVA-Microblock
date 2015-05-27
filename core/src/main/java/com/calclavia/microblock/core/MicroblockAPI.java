package com.calclavia.microblock.core;

import com.calclavia.microblock.core.common.BlockContainer;
import nova.core.block.BlockFactory;
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;

/**
 * @author Calclavia
 */
@NovaMod(id = "microblock", name = "Microblock", version = "0.0.1", novaVersion = "0.0.1", isPlugin = true)
public class MicroblockAPI implements Loadable {

	public static BlockFactory blockContainer;

	@Override
	public void preInit() {
		blockContainer = Game.instance.blockManager.register(BlockContainer.class);
	}
}
