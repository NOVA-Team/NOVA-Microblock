package nova.microblock.injection;

import nova.core.block.Block;
import nova.core.component.Component;
import nova.core.component.ComponentProvider;
import nova.core.util.Factory;
import nova.core.util.Manager;
import nova.core.util.Registry;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Inject components across containers from the contained block
 * @author Calclavia
 */
public class ComponentInjection extends Manager<ComponentInjector, ComponentInjection.ComponentInjectionFactory> {

	public ComponentInjection(Registry<ComponentInjectionFactory> registry) {
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
		contained.events.on(ComponentProvider.ComponentAdded.class).bind(evt -> findInjectors(evt.component.getClass()).forEach(injector -> injector.injectForward(evt.component, contained, container)));
		contained.events.on(ComponentProvider.ComponentRemoved.class).bind(evt -> container.components.remove(evt.component));

		//TODO: Maybe events should not be injected this way.
		//Forward events to -> from (container -> contained)
		container.events.on().bind(contained.events::publish);
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
			.map(Factory::build)
			.filter(injector -> injector.componentType().isAssignableFrom(clazz))
			.collect(Collectors.toSet());
	}

	@Override
	public ComponentInjectionFactory register(String id, Supplier<ComponentInjector> constructor) {
		return register(new ComponentInjectionFactory(id, constructor));
	}

	public static class ComponentInjectionFactory extends Factory<ComponentInjectionFactory, ComponentInjector> {
		public ComponentInjectionFactory(String id, Supplier<ComponentInjector> constructor, Function<ComponentInjector, ComponentInjector> processor) {
			super(id, constructor, processor);
		}

		public ComponentInjectionFactory(String id, Supplier<ComponentInjector> constructor) {
			super(id, constructor);
		}

		@Override
		protected ComponentInjectionFactory selfConstructor(String id, Supplier<ComponentInjector> constructor, Function<ComponentInjector, ComponentInjector> processor) {
			return new ComponentInjectionFactory(id, constructor, processor);
		}

		@Override
		public ComponentInjector build() {
			ComponentInjector build = super.build();
			build.factory = this;
			return build;
		}
	}
}
