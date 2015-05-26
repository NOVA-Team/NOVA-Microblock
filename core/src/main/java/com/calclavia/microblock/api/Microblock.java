package com.calclavia.microblock.api;

import nova.core.block.Block;
import nova.core.component.Component;

/**
 * A microblock component. When applied to blocks, it will allow the block to work as a microblock
 * @author Calclavia
 */
public class Microblock extends Component {

	public final Block block;

	public Microblock(Block block) {
		this.block = block;
	}
}
