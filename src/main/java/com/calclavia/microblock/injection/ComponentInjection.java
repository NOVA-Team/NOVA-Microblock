package com.calclavia.microblock.injection;

import nova.core.block.Block;
import nova.core.component.Component;
import nova.core.util.Factory;
import nova.core.util.Manager;
import nova.core.util.Registry;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Inject components across containers from the contained block
 * @author Calclavia
 */
public class ComponentInjection extends Manager<ComponentInjector, Factory<ComponentInjector>> {

	public ComponentInjection(Registry<Factory<ComponentInjector>> registry) {
		super(registry);
	}

	/**
	 * Injects components from the contained block to the container.
	 * Injection should occur AFTER the contained block is placed.
	 * @param contained The contained
	 * @param container The container
	 */
	public void injectToContainer(Block contained, Block container) {
		contained
			.components()
			.stream()
			.forEach(component -> findInjectors(component.getClass()).forEach(injector -> injector.injectForward(component, contained, container)));

		//TODO: Test component added after constructor.
		//When future components are added to the contained, it will auto-inject to the container.
		contained.onComponentAdded.add(evt -> findInjectors(evt.component.getClass()).forEach(injector -> injector.injectForward(evt.component, contained, container)));
		contained.onComponentRemoved.add(event -> container.remove(event.component));

		//TODO: Maybe events should not be injected this way.
		//Forward events to -> from (container -> contained)
		container.events.add(contained.events::publish);
	}

	/**
	 * Injects components from the container block to the contained.
	 * @param contained The contained
	 * @param container The container
	 */
	public void injectToContained(Block contained, Block container) {
		//Inject the special components from the container to the contained (such as BlockTransform).
		container
			.components()
			.stream()
			.forEach(component -> findInjectors(component.getClass()).forEach(injector -> injector.injectBackward(component, contained, container)));
	}

	public <C extends Component> Set<ComponentInjector> findInjectors(Class<C> clazz) {
		return registry
			.stream()
			.map(Factory::getDummy)
			.filter(injector -> injector.componentType().isAssignableFrom(clazz))
			.collect(Collectors.toSet());
	}

	@Override
	public Factory<ComponentInjector> register(Function<Object[], ComponentInjector> constructor) {
		return register(new Factory<>(constructor));
	}

}
