package com.calclavia.microblock.mc1710

import codechicken.multipart.TMultiPart
import com.calclavia.microblock.core.Microblock
import net.minecraft.nbt.NBTTagCompound
import nova.core.game.Game
import nova.core.retention.Storable

/**
 * @author Calclavia
 */
class PartWrapper extends TMultiPart {
	val wrapped: Microblock = null

	override def load(tag: NBTTagCompound) {
		tag.setString("novaID", wrapped.block.getID)

		if (wrapped.block.isInstanceOf[Storable])
			wrapped.block.asInstanceOf[Storable].load(Game.instance.nativeManager.toNova(tag))
	}

	override def save(tag: NBTTagCompound) {
		val blockID = tag.getString("novaID")

		//wrapped = Game.instance.blockManager.getFactory("blockID").get().makeBlock(new MCBlockWrapper(world))

		if (wrapped.block.isInstanceOf[Storable])
			wrapped.block.asInstanceOf[Storable].save(Game.instance.nativeManager.toNova(tag))
	}

	override def getType: String = wrapped.block.getID
}
