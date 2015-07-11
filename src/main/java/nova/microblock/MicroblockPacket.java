package nova.microblock;

import nova.microblock.common.BlockContainer;
import nova.microblock.micro.Microblock;
import nova.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.network.NetworkException;
import nova.core.network.Packet;
import nova.core.network.Syncable;
import nova.core.network.handler.PacketHandler;
import nova.core.world.World;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;

/**
 * @author Calclavia
 */
public class MicroblockPacket implements PacketHandler<Microblock> {

	@Override
	public void read(Packet packet) {
		Vector3D globalPos = new Vector3D(packet.readInt(), packet.readInt(), packet.readInt());
		Vector3D localPos = new Vector3D(packet.readInt(), packet.readInt(), packet.readInt());

		World world = packet.player().entity().world();
		Optional<Block> opBlock = world.getBlock(globalPos);

		if (opBlock.isPresent()) {
			Block block = opBlock.get();

			Optional<MicroblockContainer> opContainer = block.getOp(MicroblockContainer.class);
			if (opContainer.isPresent()) {
				MicroblockContainer microblockContainer = opContainer.get();
				Optional<Microblock> opMicroblock = microblockContainer.get(localPos);

				if (opMicroblock.isPresent()) {
					Microblock microblock = opMicroblock.get();

					if (microblock.block instanceof Syncable) {
						((Syncable) microblock.block).read(packet);
						return;
					}
				} else {
					throw new NetworkException("Cannot find microblock at " + globalPos + " withPriority local position " + localPos);
				}
			} else {
				throw new NetworkException("Cannot find microblock container at " + globalPos + " withPriority local position " + localPos);
			}
		}

		throw new NetworkException("Microblock packet failed to be read at " + globalPos + " withPriority local position " + localPos);
	}

	@Override
	public void write(Microblock microblock, Packet packet) {
		BlockContainer container = (BlockContainer) microblock.containers.stream().findFirst().get().block;
		Vector3D position = container.position();
		//Global Pos
		packet.writeInt((int) position.getX());
		packet.writeInt((int) position.getY());
		packet.writeInt((int) position.getZ());
		//Local pos
		Vector3D localPos = microblock.position;
		packet.writeInt((int) localPos.getX());
		packet.writeInt((int) localPos.getY());
		packet.writeInt((int) localPos.getZ());
		//Write the microblock data
		((Syncable) microblock.block).write(packet);
	}

	@Override
	public boolean isHandlerFor(Object handler) {
		return handler instanceof Microblock && ((Microblock) handler).block instanceof Syncable;
	}
}
