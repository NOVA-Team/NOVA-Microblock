package com.calclavia.microblock.injection.prefab;

import nova.core.block.Block;
import nova.core.component.Component;

/**
 * @author Calclavia
 */
public class ForwardAllInjector<COMPONENT extends Component> extends DefaultInjector<COMPONENT> {
	public ForwardAllInjector(Class<COMPONENT> type) {
		super(type);
	}

	@Override
	public boolean injectForward(COMPONENT component, Block contained, Block container) {
		return true;
	}
}
