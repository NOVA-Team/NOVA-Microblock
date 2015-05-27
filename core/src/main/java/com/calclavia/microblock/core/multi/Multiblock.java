package com.calclavia.microblock.core.multi;

import com.calclavia.microblock.core.common.ContainedComponent;
import nova.core.block.Block;

/**
 * A component attached to any multiblock.
 * @author Calclavia
 */
public class Multiblock extends ContainedComponent<MultiblockContainer> {

	public Multiblock(Block block) {
		super(block);
	}
}
