package com.calclavia.microblock.test;

import com.calclavia.microblock.core.micro.Microblock;
import nova.core.block.Block;
import nova.core.util.transform.vector.Vector3i;

/**
 * @author Calclavia
 */
public class BlockSmallMicroblock extends Block {

	public BlockSmallMicroblock() {
		add(new Microblock(this, blockPlaceEvent -> new Vector3i(0, 0, 0)));
	}

	@Override
	public String getID() {
		return "smallMicroblock";
	}
}
