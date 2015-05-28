package com.calclavia.microblock.test;

import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;

/**
 * @author Calclavia
 */
@NovaMod(id = "testMicroblock", name = "Test Microblock", version = "1.0", novaVersion = "0.0.1", dependencies = { "microblock" })
public class TestMicroblockMod implements Loadable {

	@Override
	public void preInit() {
		System.out.println("test");
	}
}
