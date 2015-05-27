package com.calclavia.microblock.api.multi;

import nova.core.block.Block;
import nova.core.component.Component;

/**
 * A component attached to any multiblock.
 * @author Calclavia
 */
public class Multiblock extends Component {

	public final Block block;

	public Multiblock(Block block) {
		this.block = block;
	}
}
