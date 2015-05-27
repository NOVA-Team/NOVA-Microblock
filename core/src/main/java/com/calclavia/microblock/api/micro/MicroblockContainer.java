package com.calclavia.microblock.api.micro;

import nova.core.block.Block;
import nova.core.component.Component;

import java.util.Optional;

/**
 * A component applied to blocks that can hold microblocks within them.
 * @author Calclavia
 */
public abstract class MicroblockContainer extends Component {

	public final Block block;

	public MicroblockContainer(Block block) {
		this.block = block;
	}

	/**
	 * Gets a microblock that exists in a certain slot.
	 * @param slotID
	 * @return
	 */
	public abstract Optional<Microblock> getMicroblock(int slotID);
}
