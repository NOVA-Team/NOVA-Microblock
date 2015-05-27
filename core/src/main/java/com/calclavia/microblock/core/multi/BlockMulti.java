package com.calclavia.microblock.core.multi;

import nova.core.block.Block;
import nova.core.block.Stateful;
import nova.core.network.PacketHandler;
import nova.core.retention.Storable;

/**
 * A multiblock
 * @author Calclavia
 */
public class BlockMulti extends Block implements Stateful, Storable, PacketHandler {

	@Override
	public String getID() {
		return "multiblock";
	}
}
