package com.calclavia.microblock.core.common;

import nova.core.block.Block;
import nova.core.game.Game;

/**
 * A block container can forward events, components and methods to their respective microblock or multiblocks
 * @author Calclavia
 */
public class BlockContainer extends Block {

	public final String id;

	public BlockContainer(String id) {
		this.id = id;
		//Debug
		rightClickEvent.add(event -> System.out.println("#: " + components().size()));
	}

	@Override
	public void onRegister() {
		//Register a custom itemblock
		Game.instance.itemManager.register((args) -> new ItemBlockContainer(factory()));
	}

	@Override
	public String getID() {
		return id;
	}
}
