package com.calclavia.microblock.core.injection;

import nova.core.block.Block;
import nova.core.component.Component;
import nova.core.util.Identifiable;

/**
 * Handles how a component is injected from a container to the contained.
 * @author Calclavia
 */
public abstract class ComponentInjector<COMPONENT extends Component> implements Identifiable {

	//The component being injected
	public final COMPONENT component;

	public ComponentInjector(COMPONENT component) {
		this.component = component;
	}

	/**
	 * Handles how a component is injected from the contained to the container.
	 * @param contained The contained block
	 * @param container The container block
	 */
	public abstract void injectForward(Block contained, Block container);

	/**
	 * Handles how a component is injected backwards from load
	 * @param contained The contained block
	 * @param container The container block
	 */
	public abstract void injectBackward(Block contained, Block container);

}
