package nova.microblock.common;

import nova.core.block.Block;
import nova.core.block.Stateful;
import nova.core.component.Component;
import nova.core.component.Updater;
import nova.core.event.bus.Event;
import nova.core.network.Packet;
import nova.core.network.Syncable;
import nova.core.retention.Data;
import nova.core.retention.Storable;
import nova.microblock.micro.MicroblockContainer;
import nova.microblock.operation.ContainerRemove;

import java.util.Collection;

/**
 * A block container can forward events, components and methods to their respective microblock or multiblocks
 * @author Calclavia
 */
public class BlockContainer extends Block implements Updater, Stateful, Storable, Syncable {

	public BlockContainer() {
		events.on(RemoveEvent.class).bind(evt -> ContainerRemove.interactEventHandler(this, evt));
		events.on(Event.class).bind(evt ->
				getOp(MicroblockContainer.class).ifPresent(cont -> cont.microblocks().forEach(uB -> uB.block.events.publish(evt)))
		);
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
	public void read(Packet packet) {
		if (packet.getID() == 0) {
			if (packet.readBoolean()) {
				getOrAdd(new MicroblockContainer(this));
			}
		}

		getOp(MicroblockContainer.class).ifPresent(c -> c.read(packet));
	}

	@Override
	public void write(Packet packet) {
		if (packet.getID() == 0) {
			//Write the need to add components
			packet.writeBoolean(has(MicroblockContainer.class));
		}

		getOp(MicroblockContainer.class).ifPresent(c -> c.write(packet));
	}

	@Override
	public void load(Data data) {
		if (data.containsKey("microblockContainer")) {
			getOrAdd(new MicroblockContainer(this));
		}

		getOp(MicroblockContainer.class).ifPresent(c -> c.load(data));
	}

	@Override
	public void save(Data data) {
		getOp(MicroblockContainer.class).ifPresent(c -> c.save(data));
	}
}
