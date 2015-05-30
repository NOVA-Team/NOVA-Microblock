package com.calclavia.microblock.core.injection;

import se.jbee.inject.bind.BinderModule;
import se.jbee.inject.util.Scoped;

/**
 * @author Calclavia
 */
public class ComponentInjectionModule extends BinderModule {

	@Override
	protected void declare() {
		per(Scoped.APPLICATION).bind(ComponentInjection.class).toConstructor();
	}

}
