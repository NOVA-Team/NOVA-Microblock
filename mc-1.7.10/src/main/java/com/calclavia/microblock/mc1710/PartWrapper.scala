package com.calclavia.microblock.mc1710

import codechicken.multipart.TMultiPart
import net.minecraft.nbt.NBTTagCompound
import nova.core.block.Block
import nova.core.game.Game
import nova.core.retention.Storable
import nova.core.util.transform.vector.Vector3i
import nova.wrapper.mc1710.wrapper.block.forward.{MCBlockTransform, MCBlockWrapper}

/**
 * @author Calclavia
 */
class PartWrapper(val novaID: String) extends TMultiPart {

	override def load(tag: NBTTagCompound) {
		tag.setString("novaID", wrapped.getID)

		if (wrapped.isInstanceOf[Storable])
			wrapped.asInstanceOf[Storable].load(Game.instance.nativeManager.toNova(tag))
	}

	override def save(tag: NBTTagCompound) {
		val blockID = tag.getString("novaID")

		//wrapped = Game.instance.blockManager.getFactory("blockID").get().makeBlock(new MCBlockWrapper(world))

		if (wrapped.isInstanceOf[Storable])
			wrapped.asInstanceOf[Storable].save(Game.instance.nativeManager.toNova(tag))
	}

	/**
	 * @return The block being wrapped.
	 */
	def wrapped: Block = {
		val makeBlock = Game.instance.blockManager.getFactory(novaID).get().makeBlock(new MCBlockWrapper(Game.instance.nativeManager.toNova(world), new Vector3i(x, y, z)))
		makeBlock.add(new MCBlockTransform(makeBlock))
		return makeBlock
	}

	override def getType: String = novaID
}
