package nova.microblock.injection.prefab;

import nova.core.block.Block;
import nova.core.component.Component;

/**
 * @author Calclavia
 */
public class CopyInjector<COMPONENT extends Component> extends DefaultInjector<COMPONENT> {
	public CopyInjector(Class<COMPONENT> type) {
		super(type);
	}

	@Override
	public boolean injectForward(COMPONENT component, Block contained, Block container) {
		if (!container.components.has(component.getClass())) {
			container.components.add(component);
		}
		return true;
	}

	@Override
	public boolean injectBackward(COMPONENT component, Block contained, Block container) {
		if (!contained.components.has(component.getClass())) {
			contained.components.add(component);
		}
		return true;
	}
}