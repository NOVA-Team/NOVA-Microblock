package com.calclavia.microblock.test;

import nova.core.block.BlockFactory;
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;

/**
 * @author Calclavia
 */
@NovaMod(id = "testMicroblock", name = "Test Microblock", version = "1.0", novaVersion = "0.0.1", dependencies = { "microblock" })
public class TestMicroblockMod implements Loadable {

	public static BlockFactory smallMicroblock;

	@Override
	public void preInit() {
		Game.instance.logger.info("Preinit on TestMicroblockMod");
		smallMicroblock = Game.instance.blockManager.register(BlockSmallMicroblock.class);
		Game.instance.logger.info("Registered blocks");
	}
}
