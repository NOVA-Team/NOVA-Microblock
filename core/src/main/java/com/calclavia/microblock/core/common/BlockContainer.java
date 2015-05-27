package com.calclavia.microblock.core.common;

import nova.core.block.Block;

/**
 * A block container can forward events, components and methods to their respective microblock or multiblocks
 * @author Calclavia
 */
public class BlockContainer extends Block {

	public BlockContainer() {
	}

	@Override
	public String getID() {
		return "blockContainer";
	}
}
