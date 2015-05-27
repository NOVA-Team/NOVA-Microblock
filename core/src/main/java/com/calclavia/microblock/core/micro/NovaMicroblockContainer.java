package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.api.micro.Microblock;
import com.calclavia.microblock.api.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.util.transform.vector.Vector3i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Implementation of the NovaMicroblockContainer
 * @author Calclavia
 */
public class NovaMicroblockContainer extends MicroblockContainer {

	/**
	 * A sparse block map from (0,0) to (subdivision, subdivision) coordinates
	 * of all the microblocks.
	 */
	public final Map<Vector3i, Microblock> blockMap = new HashMap<>();
	/**
	 * The amount of subdivisions of the microblock.
	 * Must be 2^n
	 */
	public int subdivisions = 16;

	public NovaMicroblockContainer(Block block) {
		super(block);
	}

	/**
	 * Operates on all microblocks
	 */
	public Collection<Microblock> microblocks() {
		return blockMap.values();
	}

	/**
	 * Gets a microblock based on the slot.
	 * @param slotID See {@link MicroblockSlot}
	 * @return
	 */
	@Override
	public Optional<Microblock> getMicroblock(int slotID) {
		return null;
	}
}
