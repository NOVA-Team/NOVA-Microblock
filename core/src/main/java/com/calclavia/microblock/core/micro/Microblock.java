package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.core.common.ContainedComponent;
import nova.core.block.Block;
import nova.core.component.Require;
import nova.core.component.misc.Collider;
import nova.core.util.transform.vector.Vector3i;

import java.util.function.Supplier;

/**
 * A component attached to microblocks. When applied to any class that extends Block, it will allow the block to behave as a microblock.
 * @author Calclavia
 */
@Require(Collider.class)
public class Microblock extends ContainedComponent<MicroblockContainer> {

	/**
	 * A slot mask to define the slots occupied by this microblock.
	 */
	public Supplier<Integer> slotMask = null;

	public Microblock(Block block) {
		super(block);
	}

	public Microblock setSlotMask(Supplier<Integer> slotMask) {
		this.slotMask = slotMask;
		return this;
	}
}
