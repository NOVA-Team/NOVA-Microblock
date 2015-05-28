package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.core.common.BlockComponent;
import nova.core.block.Block;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A component added to microblocks
 * @author Calclavia
 */
public class MicroblockContainer extends BlockComponent {

	/**
	 * The amount of subdivisions of the microblock.
	 * Must be 2^n
	 */
	//TOD: Make this variable, or configurable
	public final int subdivisions = 16;
	/**
	 * A sparse block map from (0,0) to (subdivision, subdivision) coordinates
	 * of all the microblocks.
	 */
	private final Map<Vector3i, Microblock> blockMap = new HashMap<>();

	public MicroblockContainer(Block block) {
		super(block);
	}

	/**
	 * Operates on all microblocks
	 */
	public Collection<Microblock> microblocks() {
		return blockMap.values();
	}

	public boolean put(Vector3i localPos, Microblock microblock) {
		assert !new Cuboid(0, 0, 0, subdivisions, subdivisions, subdivisions).intersects(localPos);

		if (!has(localPos)) {
			microblock.containers.add(this);
			blockMap.put(localPos, microblock);
			return true;
		}

		return false;
	}

	/**
	 * Gets a microblock based on the slot.
	 * @param slotID See {@link MicroblockSlot}
	 * @return
	 */
	public Optional<Microblock> get(int slotID) {
		//TODO: Implement me
		return Optional.empty();
	}

	public boolean has(Vector3i localPos) {
		return blockMap.containsKey(localPos);
	}

	/**
	 * Gets the microblock at a specific internal position.
	 * @param localPos
	 * @return The optional microblock.
	 */
	public Optional<Microblock> get(Vector3i localPos) {
		return Optional.ofNullable(blockMap.get(localPos));
	}

	/**
	 * Gets a single microblock that converts a specific region within the microblock space.
	 * @param region
	 * @return
	 */
	public Optional<Microblock> get(Cuboid region) {
		//TOD: IMPLEMENT
		return Optional.empty();
	}

	public Map<Vector3i, Microblock> map() {
		return blockMap;
	}
}
