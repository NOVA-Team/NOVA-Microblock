package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.core.common.BlockComponent;
import nova.core.block.Block;
import nova.core.util.transform.vector.Vector3i;

import java.util.HashSet;
import java.util.Set;
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
	 * The containers holding the block.
	 */
	public final Set<MicroblockContainer> containers = new HashSet<>();
	/**
	 * The local position within this microblock subspace. This value is read-only.
	 */
	public Vector3i position;
	/**
	 * A slot mask to define the slots occupied by this microblock.
	 */
	public Supplier<Integer> slotMask = null;

	/**
	 * @param block The microblock block
	 * @param onPlace The method that defines the local position the microblock is placed in, based on a blockPlaceEvent.
	 */
	//TODO: There must be a more elegant way to do this.
	public Microblock(Block block, Function<Block.BlockPlaceEvent, Vector3i> onPlace) {
		super(block);
		this.onPlace = onPlace;
	}

	public Microblock setSlotMask(Supplier<Integer> slotMask) {
		this.slotMask = slotMask;
		return this;
	}

}
