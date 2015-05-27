package com.calclavia.microblock.core.common;

import nova.core.block.Block;
import nova.core.component.Component;

/**
 * @author Calclavia
 */
public class ContainedComponent<CONTAINER extends AbstractContainer> extends Component {

	/**
	 * The block holding this component.
	 */
	public final Block block;

	/**
	 * The container holding the block.
	 */
	public CONTAINER container;

	public ContainedComponent(Block block) {
		this.block = block;
	}

	public void setContainer(CONTAINER container) {
		this.container = container;
	}
}
