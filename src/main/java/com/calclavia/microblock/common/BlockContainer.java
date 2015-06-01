package com.calclavia.microblock.common;

import com.calclavia.microblock.micro.MicroblockContainer;
import nova.core.block.Block;
import nova.core.block.Stateful;
import nova.core.component.Component;
import nova.core.game.Game;
import nova.core.network.Packet;
import nova.core.network.PacketHandler;
import nova.core.retention.Data;
import nova.core.retention.Storable;

import java.util.Collection;

/**
 * A block container can forward events, components and methods to their respective microblock or multiblocks
 * @author Calclavia
 */
public class BlockContainer extends Block implements Stateful, Storable, PacketHandler {

	public final String id;

	public BlockContainer(String id) {
		this.id = id;

		rightClickEvent.add(event -> {
			System.out.println("--- " + this + " ---");
			printComponents(components());
		});
	}

	private void printComponents(Collection<Component> components) {
		printComponents(components, "::");
	}

	private void printComponents(Collection<Component> components, String prefix) {
		components.forEach(component -> {
			System.out.println(prefix + component.getClass());

			if (component instanceof MicroblockContainer) {
				((MicroblockContainer) component).microblocks()
					.forEach(microblock -> {
						System.out.println("+++ " + microblock.block + " +++");
						printComponents(microblock.block.components(), prefix + "::");
					});
			}

		});
	}

	@Override
	public void onRegister() {
		//Register a custom itemblock
		Game.instance().itemManager().register((args) -> new ItemBlockContainer(factory()));
	}

	@Override
	public void read(Packet packet) {
		if (packet.getID() == 0) {
			if (packet.readBoolean()) {
				getOrAdd(new MicroblockContainer(this));
			}
		}

		PacketHandler.super.read(packet);
	}

	@Override
	public void write(Packet packet) {
		if (packet.getID() == 0) {
			//Write the need to add components
			packet.writeBoolean(has(MicroblockContainer.class));
		}

		PacketHandler.super.write(packet);
	}

	@Override
	public void load(Data data) {
		if (data.containsKey("microblockContainer")) {
			getOrAdd(new MicroblockContainer(this));
		}

		Storable.super.load(data);
	}

	@Override
	public String getID() {
		return id;
	}
}
