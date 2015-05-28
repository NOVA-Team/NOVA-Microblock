package com.calclavia.microblock.core.common;

import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.multi.Multiblock;
import nova.core.block.Block;
import nova.core.block.BlockFactory;

import java.util.function.Function;

/**
 * Inject components across containers from the contained block
 * @author Calclavia
 */
public class ComponentInjector {

	public Function<Object[], Block> construct(BlockFactory containedFactory) {
		return args -> {
			//Check the contained factory's dummy, and inject components.
			BlockContainer blockContainer = new BlockContainer("blockContainer-" + containedFactory.getID());
			inject(containedFactory.getDummy(), blockContainer);
			System.out.println("Makeblock called");
			return blockContainer;
		};
	}

	/**
	 * Marks a block and injects from the block to the block (and all future components)
	 * @param from The contained
	 * @param to The container
	 */
	public void inject(Block from, Block to) {
		from.components().stream()
			.filter(component -> !(component instanceof Multiblock) && !(component instanceof Microblock))
			.forEach(to::add);

		from.onComponentAdded.add(event -> to.add(event.component));
		from.onComponentRemoved.add(event -> to.remove(event.component));

		//Forward events to -> from (container -> contained)
		//TODO: Use reflection, auto transfer events?
		to.loadEvent.add(from.loadEvent::publish);
		to.unloadEvent.add(from.unloadEvent::publish);
		to.leftClickEvent.add(from.leftClickEvent::publish);
		to.rightClickEvent.add(from.rightClickEvent::publish);
		to.removeEvent.add(from.removeEvent::publish);
		to.placeEvent.add(from.placeEvent::publish);
		to.neighborChangeEvent.add(from.neighborChangeEvent::publish);
	}
}
