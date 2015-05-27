package com.calclavia.microblock.core;

import com.calclavia.microblock.core.micro.BlockMicro;
import com.calclavia.microblock.core.multi.BlockMulti;
import nova.core.block.BlockFactory;
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;

/**
 * @author Calclavia
 */
@NovaMod(id = "microblock", name = "Microblock", version = "0.0.1", novaVersion = "0.0.1", isPlugin = true)
public class MicroblockAPI implements Loadable {

	public static BlockFactory microblock;
	public static BlockFactory multiblock;

	@Override
	public void preInit() {
		microblock = Game.instance.blockManager.register(BlockMicro.class);
		multiblock = Game.instance.blockManager.register(BlockMulti.class);
	}
}
