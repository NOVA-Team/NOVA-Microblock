package nova.microblock.injection.prefab;

import nova.microblock.injection.ComponentInjector;
import nova.core.component.Component;

/**
 * Default component injection
 * @author Calclavia
 */
public class DefaultInjector<COMPONENT extends Component> extends ComponentInjector<COMPONENT> {

	private final Class<COMPONENT> type;

	public DefaultInjector(Class<COMPONENT> type) {
		this.type = type;
	}

	@Override
	public Class<COMPONENT> componentType() {
		return type;
	}
}
