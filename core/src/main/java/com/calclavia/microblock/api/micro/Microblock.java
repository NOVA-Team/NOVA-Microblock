package com.calclavia.microblock.api.micro;

import nova.core.block.Block;
import nova.core.component.Component;

import java.util.function.Supplier;

/**
 * A component attached to microblocks. When applied to any class that extends Block, it will allow the block to behave as a microblock.
 * @author Calclavia
 */
public class Microblock extends Component {

	public final Block block;

	/**
	 * A slot mask to define the slots occupied by this microblock.
	 */
	public Supplier<Integer> slotMask = null;

	public Microblock(Block block) {
		this.block = block;
	}

	public Microblock setSlotMask(Supplier<Integer> slotMask) {
		this.slotMask = slotMask;
		return this;
	}
}
