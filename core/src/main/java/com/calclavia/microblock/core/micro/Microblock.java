package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.core.common.ContainedComponent;
import com.calclavia.microblock.core.multi.Multiblock;
import nova.core.block.Block;
import nova.core.component.Require;
import nova.core.component.misc.Collider;
import nova.core.util.exception.NovaException;
import nova.core.util.transform.vector.Vector3d;
import nova.core.util.transform.vector.Vector3i;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A component attached to microblocks. When applied to any class that extends Block, it will allow the block to behave as a microblock.
 * @author Calclavia
 */
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

	/**
	 * The local position occupied by the microblock.
	 */
	public Vector3i position() {
		return container.blockMap.inverse().get(this);
	}

}
