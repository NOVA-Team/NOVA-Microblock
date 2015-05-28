package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.core.common.BlockComponent;
import nova.core.block.Block;
import nova.core.util.transform.vector.Vector3i;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A component attached to microblocks. When applied to any class that extends Block, it will allow the block to behave as a microblock.
 * @author Calclavia
 */
public class Microblock extends BlockComponent {

	/**
	 * The function that determines how this microblock handles its placement
	 *
	 * A microblock MUST be able to return its local position based on its placement.
	 */
	public final Function<Block.BlockPlaceEvent, Vector3i> onPlace;
	/**
	 * The container holding the block.
	 */
	public MicroblockContainer container;
	/**
	 * A slot mask to define the slots occupied by this microblock.
	 */
	public Supplier<Integer> slotMask = null;

	public Microblock(Block block, Function<Block.BlockPlaceEvent, Vector3i> onPlace) {
		super(block);
		this.onPlace = onPlace;
	}

	public Microblock setSlotMask(Supplier<Integer> slotMask) {
		this.slotMask = slotMask;
		return this;
	}

	/**
	 * The local position occupied by the microblock.
	 */
	public Vector3i position() {
		return container.map().inverse().get(this);
	}

	public void setContainer(MicroblockContainer container) {
		this.container = container;
	}

}
