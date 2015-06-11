package com.calclavia.microblock.micro;

import com.calclavia.microblock.common.BlockComponent;
import nova.core.block.Block;
import nova.core.event.EventBus;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * A component attached to microblocks. When applied to any class that extends Block, it will allow the block to behave as a microblock.
 *
 * To send packets through microblocks, you must call:
 * MicroblockPlugin.instance.network().sync(id, microblock)
 * DO NOT, send the block instance, as that will create an error.
 *
 * @author Calclavia
 */
public class Microblock extends BlockComponent {

	public final EventBus<Block.NeighborChangeEvent> microblockChangeEvent = new EventBus<>();

	/**
	 * The containers holding the block.
	 */
	public final Set<MicroblockContainer> containers = new HashSet<>();
	/**
	 * The function that determines how this microblock handles its placement
	 *
	 * A microblock MUST be able to return its local position based on its placement.
	 */
	public Function<Block.PlaceEvent, Optional<Vector3D>> onPlace = evt -> Optional.empty();
	/**
	 * The local position within this microblock subspace. This value is read-only.
	 */
	public Vector3D position;

	/**
	 * @param block The microblock block
	 */
	//TODO: There must be a more elegant way to do this.
	public Microblock(Block block) {
		super(block);
	}

	public Microblock setOnPlace(Function<Block.PlaceEvent, Optional<Vector3D>> onPlace) {
		this.onPlace = onPlace;
		return this;
	}
}
