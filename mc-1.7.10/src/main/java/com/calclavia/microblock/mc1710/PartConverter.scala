package com.calclavia.microblock.mc1710

import codechicken.multipart.MultiPartRegistry.IPartFactory
import codechicken.multipart.{MultiPartRegistry, TMultiPart}
import com.calclavia.microblock.api.micro.Microblock
import com.calclavia.microblock.mc1710.wrapper.FWMultiPart
import nova.core.block.BlockManager.BlockRegisteredEvent
import nova.core.block.{Block, BlockFactory}
import nova.core.event.EventListener
import nova.core.game.Game
import nova.core.loader.Loadable
import nova.core.nativewrapper.NativeConverter
import nova.core.util.exception.NovaException

/**
 * @author Calclavia
 */
object PartConverter extends IPartFactory with NativeConverter[Block, TMultiPart] with Loadable {

	var registered = Map.empty[String, BlockFactory]
	var alreadyInit = false

	override def preInit() {
		//Add custom way to handle registered blocks via events.
		Game.instance.blockManager.whenBlockRegistered(new EventListener[BlockRegisteredEvent] {
			override def onEvent(evt: BlockRegisteredEvent) {
				if (alreadyInit)
					throw new NovaException("Illegal attempt to register a microblock after pre-init.")

				val blockFactory = evt.blockFactory

				//Handle microblock registration
				if (blockFactory.getDummy.has(classOf[Microblock])) {
					registered += blockFactory.getID -> blockFactory
					evt.cancel()
				}
			}
		})
	}

	override def init() {
		alreadyInit = true
		//Register all the parts
		MultiPartRegistry.registerParts(this, registered.keys.toArray)
	}

	override def getNovaSide: Class[Block] = classOf[Block]

	override def getNativeSide: Class[TMultiPart] = classOf[TMultiPart]

	override def toNova(nativeObj: TMultiPart): Block = nativeObj.asInstanceOf[FWMultiPart].wrapped

	override def toNative(novaObj: Block): TMultiPart = new FWMultiPart(novaObj.getID)

	override def createPart(name: String, client: Boolean): TMultiPart = {
		return new FWMultiPart(name)
	}
}
