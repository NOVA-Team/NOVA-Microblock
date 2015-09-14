package nova.microblock.multi;

import nova.core.block.Block;
import nova.microblock.common.BlockComponent;

/**
 * A component attached to any block that is a multiblock container.
 * @author Calclavia
 */
public class MultiblockContainer extends BlockComponent {

	public final Block containedBlock;

	//Used for data retention and ticking?
	public boolean isPrimary;

	/**
	 * Creates a new multiblock container
	 * @param containerBlock The container block
	 * @param containedBlock The block being contained.
	 */
	public MultiblockContainer(Block containerBlock, Block containedBlock) {
		super(containerBlock);
		assert containedBlock.components.has(Multiblock.class);
		this.containedBlock = containedBlock;
		containedBlock.components.get(Multiblock.class).containers.add(this);
	}
}
