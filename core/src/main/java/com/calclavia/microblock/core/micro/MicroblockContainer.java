package com.calclavia.microblock.core.micro;

import com.calclavia.microblock.core.MicroblockAPI;
import com.calclavia.microblock.core.common.BlockComponent;
import nova.core.block.Block;
import nova.core.network.Packet;
import nova.core.network.PacketHandler;
import nova.core.util.Direction;
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
public class MicroblockContainer extends BlockComponent implements PacketHandler {

	/**
	 * The amount of subdivisions of the microblock.
	 * Must be 2^n
	 */
	//TOD: Make this variable, or configurable?
	public static final int subdivision = 8;
	/**
	 * A sparse block map from (0,0) to (subdivision, subdivision) coordinates
	 * of all the microblocks.
	 */
	private final Map<Vector3i, Microblock> blockMap = new HashMap<>();

	public MicroblockContainer(Block block) {
		super(block);
	}

	public static Vector3i centerPosition() {
		return Vector3i.one.multiply(subdivision / 2);
	}

	public static Vector3i sidePosition(Direction direction) {
		return direction.toVector()
			.add(Vector3i.one)
			.toDouble()
			.divide(2d)
			.multiply(subdivision)
			.toInt();
	}

	/**
	 * Operates on all microblocks
	 */
	public Collection<Microblock> microblocks() {
		return blockMap.values();
	}

	public boolean put(Vector3i localPos, Microblock microblock) {
		assert !new Cuboid(0, 0, 0, subdivision, subdivision, subdivision).intersects(localPos);

		if (!has(localPos)) {
			microblock.containers.add(this);
			blockMap.put(localPos, microblock);
			return true;
		}

		return false;
	}

	/**
	 * Gets a microblock based on the slot.
	 * @param side See {@link MicroblockSlot}
	 * @return
	 */
	public Optional<Microblock> get(int side) {
		return get(Direction.fromOrdinal(side));
	}

	public Optional<Microblock> get(Direction side) {
		return get(sidePosition(side));
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

	//TODO: Create a custom ID for each microblock, send based on coordinates.
	@Override
	public void read(Packet packet) {
		System.out.println("Received MIcroblock Packet");
		//Description Packet
		if (packet.getID() == 0) {
			blockMap.clear();
			int size = packet.readInt();

			for (int i = 0; i < size; i++) {
				Vector3i microPos = packet.read(Vector3i.class);
				String microID = packet.readString();

				//Find microblock registered with such ID
				MicroblockAPI.MicroblockInjectFactory injectionFactory = MicroblockAPI.containedIDToFactory.get(microID);
				Block microblock = injectionFactory.makeBlock();

				if (microblock instanceof PacketHandler) {
					((PacketHandler) microblock).read(packet);
				}

				put(microPos, microblock.get(Microblock.class));
			}
		}
	}

	@Override
	public void write(Packet packet) {
		//Description Packet
		if (packet.getID() == 0) {
			packet.writeInt(microblocks().size());

			//Write all microblocks over
			map().forEach((k, v) -> {
				packet.write(k);
				packet.writeString(v.block.getID());

				if (v.block instanceof PacketHandler) {
					((PacketHandler) v.block).write(packet);
				}
			});
		}
	}
}
