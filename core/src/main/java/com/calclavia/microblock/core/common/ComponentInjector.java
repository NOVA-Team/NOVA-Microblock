package com.calclavia.microblock.core.common;

import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.multi.Multiblock;
import nova.core.block.Block;
import nova.core.block.BlockFactory;

import java.util.function.Function;

/**
 * Inject components across containers from the contained block
 * @author Calclavia
 */
public class ComponentInjector {

	public Function<Object[], Block> construct(BlockFactory containedFactory) {
		return args -> {
			//Check the contained factory's dummy, and inject components.
			BlockContainer blockContainer = new BlockContainer("blockContainer-" + containedFactory.getID());
			inject(containedFactory.getDummy(), blockContainer);
			return blockContainer;
		};
	}

	/**
	 * Marks a block and injects from the block to the block (and all future components)
	 * @param from
	 * @param to
	 */
	public void inject(Block from, Block to) {
		from.components().stream()
			.filter(component -> !(component instanceof Multiblock) && !(component instanceof Microblock))
			.forEach(to::add);

		from.onComponentAdded.add(event -> to.add(event.component));
		from.onComponentRemoved.add(event -> to.add(event.component));
	}
}
