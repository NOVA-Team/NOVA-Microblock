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
		if (!container.has(component.getClass())) {
			container.add(component);
		}
		return true;
	}

	@Override
	public boolean injectBackward(COMPONENT component, Block contained, Block container) {
		if (!contained.has(component.getClass())) {
			contained.add(component);
		}
		return true;
	}
}