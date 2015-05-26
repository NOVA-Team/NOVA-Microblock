package com.calclavia.microblock.mc1710;

import codechicken.multipart.MultiPartRegistry;
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NativeLoader;

/**
 * @author Calclavia
 */
@NativeLoader(forGame = "minecraft")
public class MCMicroblock implements Loadable {
	@Override
	public void preInit() {
		Game.instance.nativeManager.registerConverter(PartConverter$.MODULE$);
	}
}
