package com.calclavia.microblock.core.micro;

import nova.core.block.Block;
import nova.core.block.Stateful;
import nova.core.network.PacketHandler;
import nova.core.retention.Storable;

/**
 * A NOVA microblock
 * @author Calclavia
 */
public class BlockMicro extends Block implements Stateful, Storable, PacketHandler {

	public BlockMicro() {
		add(new NovaMicroblockContainer(this));

		//TODO: Forward all events to the microblocks
	}

	@Override
	public String getID() {
		return "microblock";
	}
}
