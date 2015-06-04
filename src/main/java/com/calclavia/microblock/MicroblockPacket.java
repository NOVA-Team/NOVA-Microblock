package com.calclavia.microblock;

import com.calclavia.microblock.common.BlockContainer;
import com.calclavia.microblock.micro.Microblock;
import com.calclavia.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.network.Packet;
import nova.core.network.PacketHandler;
import nova.core.network.handler.PacketType;
import nova.core.util.exception.NovaException;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

import java.util.Optional;

/**
 * @author Calclavia
 */
public class MicroblockPacket implements PacketType<Microblock> {

	@Override
	public void read(Packet packet) {
		Vector3i globalPos = new Vector3i(packet.readInt(), packet.readInt(), packet.readInt());
		Vector3i localPos = new Vector3i(packet.readInt(), packet.readInt(), packet.readInt());

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

					if (microblock.block instanceof PacketHandler) {
						((PacketHandler) microblock.block).read(packet);
						return;
					}
				}
			}
		}

		throw new NovaException("Microblock packet failed to be read at " + globalPos + " with local position " + localPos);
	}

	@Override
	public void write(Microblock microblock, Packet packet) {
		BlockContainer container = (BlockContainer) microblock.containers.stream().findFirst().get().block;
		Vector3i position = container.position();
		//Global Pos
		packet.writeInt(position.x);
		packet.writeInt(position.y);
		packet.writeInt(position.z);
		//Local pos
		Vector3i localPos = microblock.position;
		packet.writeInt(localPos.x);
		packet.writeInt(localPos.y);
		packet.writeInt(localPos.z);
		//Write the microblock data
		((PacketHandler) microblock.block).write(packet);
	}

	@Override
	public boolean isHandlerFor(Object handler) {
		return handler instanceof Microblock && ((Microblock) handler).block instanceof PacketHandler;
	}
}
