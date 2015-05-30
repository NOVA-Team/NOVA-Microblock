package com.calclavia.microblock.micro;

import com.calclavia.microblock.MicroblockPlugin;
import com.calclavia.microblock.common.BlockComponent;
import com.calclavia.microblock.common.BlockContainer;
import nova.core.block.Block;
import nova.core.component.transform.BlockTransform;
import nova.core.game.Game;
import nova.core.network.NetworkTarget;
import nova.core.network.Packet;
import nova.core.network.PacketHandler;
import nova.core.retention.Data;
import nova.core.retention.Storable;
import nova.core.util.Direction;
import nova.core.util.math.MathUtil;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3i;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

/**
 * A component added to microblocks
 * @author Calclavia
 */
public class MicroblockContainer extends BlockComponent implements PacketHandler, Storable {

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
		assert new Cuboid(0, 0, 0, subdivision, subdivision, subdivision).intersects(localPos);

		if (!has(localPos)) {
			microblock.containers.add(this);
			microblock.position = localPos;
			blockMap.put(localPos, microblock);

			if (NetworkTarget.Side.get().isServer()) {
				Game.instance().networkManager().sync((BlockContainer) block);
			}
			return true;
		}

		return false;
	}

	@Deprecated
	public Optional<Microblock> get(int side) {
		return get(Direction.fromOrdinal(side));
	}

	/**
	 * Gets a microblock based on the slot.
	 * @param side The side of the microblock
	 * @return The microblock that is occupying this specific side.
	 */
	//TODO: Change to region
	public Optional<Microblock> get(Direction side) {
		return get(sidePosition(side));
	}

	public boolean has(Vector3i localPos) {
		return blockMap.containsKey(localPos);
	}

	/**
	 * Gets the microblock at a specific internal position.
	 * @param localPos Te local position within the microblock space
	 * @return The optional microblock.
	 */
	public Optional<Microblock> get(Vector3i localPos) {
		return Optional.ofNullable(blockMap.get(localPos));
	}

	/**
	 * Gets a single microblock that converts a specific region within the microblock space.
	 * @param region A region in the microblock space
	 * @return A single microblock if that microblock occupies the entire region.
	 */
	public Optional<Microblock> get(Cuboid region) {
		//NO-OP. TODO: IMPLEMENT
		return Optional.empty();
	}

	/**
	 * @return A man of local positions to their microblocks.
	 */
	public Map<Vector3i, Microblock> map() {
		return blockMap;
	}

	//TODO: Create a custom ID for each microblock, send based on coordinates.
	//TODO: Consider component syncer?
	@Override
	public void read(Packet packet) {
		//Description Packet
		if (packet.getID() == 0) {
			//Reset microblocks
			blockMap.clear();
			//Reset container components

			new HashSet<>(block.components())
				.stream()
				.filter(c -> !(c instanceof BlockTransform) && !(c instanceof MicroblockContainer))
				.forEach(block::remove);

			int size = packet.readInt();

			for (int i = 0; i < size; i++) {
				Vector3i microPos = idToPos(packet.readInt());
				String microID = packet.readString();

				//Find microblock registered with such ID
				MicroblockPlugin.MicroblockInjectFactory injectionFactory = MicroblockPlugin.containedIDToFactory.get(microID);
				Block microblock = injectionFactory.containedFactory.makeBlock();

				MicroblockPlugin.instance.componentInjection.injectBackward(microblock, block);
				MicroblockPlugin.instance.componentInjection.injectForward(microblock, block);

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
				packet.write(posToID(k));
				packet.writeString(v.block.getID());

				if (v.block instanceof PacketHandler) {
					((PacketHandler) v.block).write(packet);
				}
			});
		}
	}

	@Override
	public void load(Data data) {
		blockMap.clear();
		((Data) data.get("microblockContainer")).forEach((k, v) -> {
			Block savedBlock = (Block) Data.unserialize((Data) v);
			Microblock microblock = savedBlock.get(Microblock.class);
			put(idToPos(Integer.parseInt(k)), microblock);
			MicroblockPlugin.instance.componentInjection.injectBackward(savedBlock, block);
			MicroblockPlugin.instance.componentInjection.injectForward(savedBlock, block);
		});
	}

	@Override
	public void save(Data data) {
		Data microblockData = new Data();

		map().forEach((k, v) -> {
				if (v.block instanceof Storable) {
					microblockData.put(posToID(k) + "", v.block);
				}
			}
		);

		data.put("microblockContainer", microblockData);
	}

	public int posToID(Vector3i pos) {
		int shift = MathUtil.log(subdivision, 2);
		return (pos.x << (shift * 2)) | (pos.y << (shift)) | pos.z;
	}

	public Vector3i idToPos(int id) {
		int shift = MathUtil.log(subdivision, 2);
		int propogateReference = 1 << (shift - 1);
		int ones = propogateReference | (propogateReference - 1);
		int z = id & ones;
		int y = (id & (ones << shift)) >> shift;
		int x = id >> (shift * 2);
		return new Vector3i(x, y, z);
	}
}
