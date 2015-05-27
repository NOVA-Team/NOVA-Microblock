package com.calclavia.microblock.mc1710.wrapper

import java.util.Optional

import com.calclavia.microblock.api.micro.{Microblock, MicroblockContainer}
import com.calclavia.microblock.mc1710.PartConverter
import nova.core.block.Block

/**
 * @author Calclavia
 */
class FMPMicroblockContainer(block: Block) extends MicroblockContainer(block) {

	override def getMicroblock(slotID: Int): Optional[Microblock] = {
		//TODO: is BWMultipart implemented?
		return Optional.ofNullable(PartConverter.toNova(PartConverter.toNative(block).tile.partMap(slotID)).get(classOf[Microblock]))
	}
}
