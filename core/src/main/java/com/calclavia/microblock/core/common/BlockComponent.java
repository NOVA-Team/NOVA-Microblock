package com.calclavia.microblock.core.common;

import nova.core.block.Block;
import nova.core.component.Component;

/**
 * Components that are being provided by a block.
 * @author Calclavia
 */
public class BlockComponent extends Component {
	/**
	 * The block holding this component.
	 */
	public final Block block;

	public BlockComponent(Block block) {
		this.block = block;
	}
}
