package com.calclavia.microblock.mc1710.wrapper

import com.calclavia.microblock.api.Microblock
import com.calclavia.microblock.mc1710.PartConverter
import nova.core.block.Block

/**
 * @author Calclavia
 */
class FMPMicroblock(block: Block) extends Microblock(block) {

	override def getMicroblock(slotID: Int): Microblock = {
		//TODO: is BWMultipart implemented?
		return PartConverter.toNova(PartConverter.toNative(block).tile.partMap(slotID)).get(classOf[Microblock])
	}
}
