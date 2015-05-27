package com.calclavia.microblock.api;

import nova.core.block.Block;
import nova.core.component.Component;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A microblock component. When applied to blocks, it will allow the block to work as a microblock and handle microblock interaction
 * @author Calclavia
 */
public abstract class Microblock extends Component {

	public final Block block;

	/**
	 * A slot mask to define where
	 */
	public Supplier<Integer> slotMask = () -> -1;


	public Microblock(Block block) {
		this.block = block;
	}

	public Microblock setSlotMask(Supplier<Integer> slotMask) {
		this.slotMask = slotMask;
		return this;
	}

	/**
	 * Gets a microblock that exists in a certain slot.
	 * @param slotID
	 * @return
	 */
	public abstract Microblock getMicroblock(int slotID);
}
