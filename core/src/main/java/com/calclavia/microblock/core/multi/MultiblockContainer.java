package com.calclavia.microblock.core.multi;

import com.calclavia.microblock.core.common.AbstractContainer;
import com.calclavia.microblock.core.common.ContainerWrapper;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.component.Component;

/**
 * A component attached to any block that is a multiblock container.
 * @author Calclavia
 */
public class MultiblockContainer extends AbstractContainer {

	public final Block block;

	public MultiblockContainer(Block block) {
		this.block = block;
	}

	/**
	 * Adds a block that contains a microblock component to this container.
	 * @param blockFactory The block factory
	 * @return This
	 */
	public MultiblockContainer add(BlockFactory blockFactory) {
		Block newBlock = blockFactory.makeBlock(new ContainerWrapper());
		newBlock.get(Multiblock.class)
			.setContainer(this);

		return this;
	}
}
