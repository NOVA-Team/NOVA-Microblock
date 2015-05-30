package com.calclavia.microblock.core.common;

import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.micro.MicroblockContainer;
import com.calclavia.microblock.core.multi.Multiblock;
import com.calclavia.microblock.core.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.block.BlockFactory;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Inject components across containers from the contained block
 * @author Calclavia
 */
public class ComponentInjector {

	private static final Class[] injectionBlacklist = { Multiblock.class, Microblock.class, MultiblockContainer.class, MicroblockContainer.class };

	/**
	 * Marks a block and injects from the block to the block (and all future components)
	 * @param contained The contained
	 * @param container The container
	 */
	public static void inject(Block contained, Block container) {
		contained.components().stream()
			.filter(component -> !Arrays.stream(injectionBlacklist).anyMatch(aClass -> aClass.isAssignableFrom(component.getClass())))
			.filter(component -> !container.has(component.getClass()))
			.forEach(container::add);

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

	public static void backInject(Block contained, Block container) {
		//Inject the special components from the container to the contained (such as BlockTransform).
		container.components().stream()
			.filter(component -> !Arrays.stream(injectionBlacklist).anyMatch(aClass -> aClass.isAssignableFrom(component.getClass())))
			.filter(component -> !contained.has(component.getClass()))
			.forEach(contained::add);
	}
}
