package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.core.common.AbstractContainer;
import com.calclavia.microblock.core.common.ContainerWrapper;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.block.Stateful;
import nova.core.entity.Entity;
import nova.core.item.Item;
import nova.core.util.exception.NovaException;
import nova.core.util.transform.vector.Vector3i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A component added to microblocks
 * @author Calclavia
 */
public class MicroblockContainer extends AbstractContainer {

	public final Block block;

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

	public MicroblockContainer(Block block) {
		this.block = block;
	}

	/**
	 * Operates on all microblocks
	 */
	public Collection<Microblock> microblocks() {
		return blockMap.values();
	}

	/**
	 * Adds a block that contains a microblock component to this container.
	 * @param blockFactory The block factory
	 * @return This
	 */
	public void add(BlockFactory blockFactory, Entity entity, Item item) {
		Block newBlock = blockFactory.makeBlock(new ContainerWrapper());
		Microblock microblock = newBlock.get(Microblock.class);
		microblock.setContainer(this);

		if (newBlock instanceof Stateful) {
			//TODO: Is awake in the right place?
			((Stateful) newBlock).awake();

			Block.BlockPlaceEvent event = new Block.BlockPlaceEvent(entity, item);
			newBlock.placeEvent.publish(event);

			((Stateful) newBlock).load();
		}
	}

	public void remove(Block block) {
		Optional<Map.Entry<Vector3i, Microblock>> opEntry = blockMap.entrySet().stream().filter(entry -> block.sameType(entry.getValue())).findAny();

		if (opEntry.isPresent()) {

			if (opEntry.get() instanceof Stateful) {
				((Stateful) opEntry.get()).unload();
			}

			blockMap.remove(opEntry.get().getKey());
		}

		throw new NovaException("Attempt to remove block that does not exist: " + block);
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

	/**
	 * Gets the microblock at a specific internal position.
	 * @param internalPos
	 * @return The optional microblock.
	 */
	public Optional<Microblock> get(Vector3i internalPos) {
		return Optional.ofNullable(blockMap.get(internalPos));
	}
}
