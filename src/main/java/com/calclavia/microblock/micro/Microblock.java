package com.calclavia.microblock.micro;

import com.calclavia.microblock.common.BlockComponent;
import nova.core.block.Block;
import nova.core.util.transform.vector.Vector3i;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A component attached to microblocks. When applied to any class that extends Block, it will allow the block to behave as a microblock.
 * @author Calclavia
 */
public class Microblock extends BlockComponent {

	/**
	 * The containers holding the block.
	 */
	public final Set<MicroblockContainer> containers = new HashSet<>();
	/**
	 * The function that determines how this microblock handles its placement
	 *
	 * A microblock MUST be able to return its local position based on its placement.
	 */
	public Function<Block.BlockPlaceEvent, Optional<Vector3i>> onPlace = evt -> Optional.empty();
	/**
	 * The local position within this microblock subspace. This value is read-only.
	 */
	public Vector3i position;

	/**
	 * @param block The microblock block
	 */
	//TODO: There must be a more elegant way to do this.
	public Microblock(Block block) {
		super(block);
	}

	public Microblock setOnPlace(Function<Block.BlockPlaceEvent, Optional<Vector3i>> onPlace) {
		this.onPlace = onPlace;
		return this;
	}
}
