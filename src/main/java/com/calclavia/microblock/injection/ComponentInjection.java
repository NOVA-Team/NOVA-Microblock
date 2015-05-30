package com.calclavia.microblock.injection;

import com.calclavia.microblock.micro.Microblock;
import com.calclavia.microblock.micro.MicroblockContainer;
import com.calclavia.microblock.multi.Multiblock;
import com.calclavia.microblock.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.util.Factory;
import nova.core.util.Manager;
import nova.core.util.Registry;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Inject components across containers from the contained block
 * @author Calclavia
 */
public class ComponentInjection extends Manager<ComponentInjector, Factory<ComponentInjector>> {

	public ComponentInjection(Registry<Factory<ComponentInjector>> registry) {
		super(registry);
	}

	/**
	 * Marks a block and injects from the block to the block (and all future components)
	 * @param contained The contained
	 * @param container The container
	 */
	public void injectForward(Block contained, Block container) {
		contained.components().stream()
			.forEach(component ->
					registry
						.stream()
						.filter(factory -> factory.getDummy().componentType().equals(component.getClass()))
						.forEach(factory -> factory.getDummy().injectForward(component, contained, container))
			);

		contained.onComponentAdded.add(event -> container.add(event.component));
		contained.onComponentRemoved.add(event -> container.remove(event.component));

		//Forward events to -> from (container -> contained)
		//TODO: Use reflection, auto transfer events?
		container.loadEvent.add(contained.loadEvent::publish);
		container.unloadEvent.add(contained.unloadEvent::publish);
		container.leftClickEvent.add(contained.leftClickEvent::publish);
		container.rightClickEvent.add(contained.rightClickEvent::publish);
		container.removeEvent.add(contained.removeEvent::publish);
		container.placeEvent.add(contained.placeEvent::publish);
		container.neighborChangeEvent.add(contained.neighborChangeEvent::publish);
	}

	public void injectBackward(Block contained, Block container) {
		//Inject the special components from the container to the contained (such as BlockTransform).
		container.components().stream()
			.forEach(component ->
					registry
						.stream()
						.filter(factory -> factory.getDummy().componentType().equals(component.getClass()))
						.forEach(factory -> factory.getDummy().injectBackward(component, contained, container))
			);
	}

	@Override
	public Factory<ComponentInjector> register(Function<Object[], ComponentInjector> constructor) {
		return new Factory<>(constructor);
	}
}
