package com.calclavia.microblock.injection;

import nova.core.block.Block;
import nova.core.component.Component;
import nova.core.util.Identifiable;

/**
 * Handles how a component is injected from a container to the contained.
 * @author Calclavia
 */
public abstract class ComponentInjector<COMPONENT extends Component> implements Identifiable {

	//The component componentType being injected
	public abstract Class<COMPONENT> componentType();

	/**
	 * Handles how a component is injected from the contained to the container.
	 * @param contained The contained block
	 * @param container The container block
	 * @return True if the injection was successful
	 */
	public boolean injectForward(COMPONENT component, Block contained, Block container) {
		if (!container.has(component.getClass())) {
			container.add(component);
			return true;
		}
		return false;
	}

	/**
	 * Handles how a component is injected backwards from load
	 * @param contained The contained block
	 * @param container The container block
	 * @return True if the injection was successful
	 */
	public boolean injectBackward(COMPONENT component, Block contained, Block container) {
		if (!contained.has(component.getClass())) {

			contained.add(component);
			return true;
		}
		return false;
	}

	@Override
	public String getID() {
		return componentType().getCanonicalName();
	}
}
