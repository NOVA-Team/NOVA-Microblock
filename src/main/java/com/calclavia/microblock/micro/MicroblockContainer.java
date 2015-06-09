package com.calclavia.microblock.micro;

import com.calclavia.microblock.MicroblockPlugin;
import com.calclavia.microblock.common.BlockComponent;
import com.calclavia.microblock.common.BlockContainer;
import nova.core.block.Block;
import nova.core.block.Stateful;
import nova.core.component.Component;
import nova.core.component.Updater;
import nova.core.component.transform.BlockTransform;
import nova.core.network.NetworkException;
import nova.core.network.Packet;
import nova.core.network.Syncable;
import nova.core.retention.Data;
import nova.core.retention.Storable;
import nova.core.util.Direction;
import nova.core.util.math.MathUtil;
import nova.core.util.math.Vector3DUtil;
import nova.core.util.shape.Cuboid;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * A component added to microblocks
 * @author Calclavia
 */
public class MicroblockContainer extends BlockComponent implements Syncable, Storable, Updater {

	/**
	 * The amount of subdivisions of the microblock.
	 * Must be 2^n
	 */
	//TOD: Make this variable, or configurable?
	public static final int subdivision = 8;
	private final String saveID = "microblockContainer";

	/**
	 * A sparse block map from (0,0) to (subdivision, subdivision) coordinates
	 * of all the microblocks.
	 */
	private final Map<Vector3D, Microblock> blockMap = new HashMap<>();

	public MicroblockContainer(Block block) {
		super(block);
	}

	public static Vector3D centerPosition() {
		return Vector3DUtil.ONE.scalarMultiply((subdivision - 1) / 2);
	}

	public static Vector3D sidePosition(Direction direction) {
		return Vector3DUtil.floor(
			direction.toVector()
			.add(Vector3DUtil.ONE)
			.scalarMultiply(0.5)
				.scalarMultiply(subdivision - 1)
		);
	}

	/**
	 * Gets a collection of all microblocks.
	 */
	public Collection<Microblock> microblocks() {
		return blockMap.values();
	}

	/**
	 * Gets a collection of all components of the same type in all microblocks.
	 * @param componentClass The component class
	 * @param <C> The component type
	 * @return Gets a stream of components in the microblocks
	 */
	public <C extends Component> Stream<C> microblocks(Class<C> componentClass) {
		return stream()
			.map(microblock -> microblock.block.getOp(componentClass))
			.filter(Optional::isPresent)
			.map(Optional::get);
	}

	public Stream<Microblock> stream() {
		return blockMap.values().stream();
	}

	/**
	 * Puts a new microblock into this container.
	 */
	public boolean putNew(Vector3D localPos, Microblock microblock) {
		if (put(localPos, microblock)) {
			//Invoke load event
			microblock.block.loadEvent.publish(new Stateful.LoadEvent());

			//Invoke neighbor change event
			microblocks().stream()
				.filter(m -> m != microblock)
				.forEach(m -> m.microblockChangeEvent.publish(new Block.NeighborChangeEvent(Optional.of(localPos))));

			block.world().markChange(block.position());

			return true;
		}

		return false;
	}

	/**
	 * Puts a microblock directly into this container.
	 * No events will be invoked.
	 */
	public boolean put(Vector3D localPos, Microblock microblock) {
		assert new Cuboid(0, 0, 0, subdivision, subdivision, subdivision).intersects(localPos);

		if (!has(localPos)) {
			//Place microblock
			microblock.containers.add(this);
			microblock.position = localPos;
			blockMap.put(localPos, microblock);

			//Inject components
			MicroblockPlugin.instance.componentInjection.injectToContained(microblock.block, block);
			MicroblockPlugin.instance.componentInjection.injectToContainer(microblock.block, block);

			if (MicroblockPlugin.instance.network.isServer()) {
				MicroblockPlugin.instance.network.sync((BlockContainer) block);
			}

			return true;
		}

		return false;
	}

	public boolean remove(Vector3D localPos) {
		if (has(localPos)) {
			get(localPos).get().block.unloadEvent.publish(new Stateful.UnloadEvent());
			blockMap.remove(localPos);

			if (microblocks().size() > 0 && MicroblockPlugin.instance.network.isServer()) {
				MicroblockPlugin.instance.network.sync(block);
			}

			//Invoke neighbor change event
			microblocks().stream()
				.forEach(m -> m.microblockChangeEvent.publish(new Block.NeighborChangeEvent(Optional.of(localPos))));

			block.world().markChange(block.position());
			return true;
		}

		return false;
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

	public boolean has(Vector3D localPos) {
		return blockMap.containsKey(localPos);
	}

	/**
	 * Gets the microblock at a specific internal position.
	 * @param localPos Te local position within the microblock space
	 * @return The optional microblock.
	 */
	public Optional<Microblock> get(Vector3D localPos) {
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
	public Map<Vector3D, Microblock> map() {
		return blockMap;
	}

	@Override
	public void update(double deltaTime) {
		stream()
			.filter(m -> m instanceof Updater)
			.map(m -> (Updater) m)
			.forEach(m -> m.update(deltaTime));
	}

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
				Vector3D microPos = idToPos(packet.readInt());
				String microID = packet.readString();

				//Find microblock registered with such ID
				MicroblockPlugin.MicroblockInjectFactory injectionFactory = MicroblockPlugin.instance.containedIDToFactory.get(microID);
				Block microblock = injectionFactory.containedFactory.makeBlock();

				put(microPos, microblock.get(Microblock.class));

				if (microblock instanceof Syncable) {
					((Syncable) microblock).read(packet);
				}
			}
		} else {
			throw new NetworkException("Microblock container reading an invalid packet ID: " + packet.getID() + ". This error may be due to an attempt to send microblock data without passing the microblock component as the packet sender.");
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

				if (v.block instanceof Syncable) {
					((Syncable) v.block).write(packet);
				}
			});
		} else {
			throw new NetworkException("Microblock container writing an invalid packet ID: " + packet.getID() + ". This error may be due to an attempt to send microblock data without passing the microblock component as the packet sender.\"");
		}
	}

	@Override
	public void load(Data data) {
		blockMap.clear();
		((Data) data.get(saveID)).forEach((k, v) -> {
			Block savedBlock = (Block) Data.unserialize((Data) v);
			Microblock microblock = savedBlock.get(Microblock.class);
			put(idToPos(Integer.parseInt(k)), microblock);
			microblock.block.loadEvent.publish(new Stateful.LoadEvent());
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

		data.put(saveID, microblockData);
	}

	public int posToID(Vector3D pos) {
		int shift = MathUtil.log(subdivision, 2);
		return ((int) pos.getX() << (shift * 2)) | ((int) pos.getY() << (shift)) | (int) pos.getZ();
	}

	public Vector3D idToPos(int id) {
		int shift = MathUtil.log(subdivision, 2);
		int propogateReference = 1 << (shift - 1);
		int ones = propogateReference | (propogateReference - 1);
		int z = id & ones;
		int y = (id & (ones << shift)) >> shift;
		int x = id >> (shift * 2);
		return new Vector3D(x, y, z);
	}
}
