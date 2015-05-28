package com.calclavia.microblock.core.common;

import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.multi.Multiblock;
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
		rightClickEvent.add(event -> Game.instance.logger.info(components().toString()));
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
