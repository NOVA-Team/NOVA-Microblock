package com.calclavia.microblock.api.multi;

import nova.core.block.Block;
import nova.core.component.Component;

/**
 * A component attached to any block that is a multiblock container.
 * @author Calclavia
 */
public abstract class MultiblockContainer extends Component {

	public final Block block;

	public MultiblockContainer(Block block) {
		this.block = block;
	}
}
