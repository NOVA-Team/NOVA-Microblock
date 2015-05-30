package com.calclavia.microblock.multi;

import com.calclavia.microblock.common.BlockComponent;
import nova.core.block.Block;
import nova.core.component.Require;
import nova.core.component.misc.Collider;
import nova.core.util.math.MathUtil;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3d;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * A component attached to any multiblock.
 * @author Calclavia
 */
@Require(Collider.class)
public class Multiblock extends BlockComponent {

	/**
	 * The containers that contain (occupy block space) for this multiblock
	 */
	public final Set<MultiblockContainer> containers = new HashSet<>();

	public Multiblock(Block block) {
		super(block);
	}

	/**
	 * Gets the occupied space within this multiblock that is outside the default blockLength bounds.
	 * @param blockLength The length of each microblock/block. Used to calculate collision subdivision.
	 * @return A set of vectors relative to the center block's bottom corner.
	 */
	public Set<Vector3d> getOccupiedSpace(float blockLength) {
		Set<Cuboid> collisionBoxes = block.get(Collider.class).occlusionBoxes.apply(Optional.empty());

		Set<Vector3d> set = new HashSet<>();
		int truncation = (int) (1 / blockLength);

		collisionBoxes.forEach(
			cuboid -> {
				//Truncate bounds to nearest block length
				for (double x = MathUtil.truncate(cuboid.min.x, truncation); x < cuboid.max.x; x += blockLength) {
					for (double y = MathUtil.truncate(cuboid.min.y, truncation); y < cuboid.max.y; y += blockLength) {
						for (double z = MathUtil.truncate(cuboid.min.z, truncation); z < cuboid.max.z; z += blockLength) {
							set.add(new Vector3d(x, y, z));
						}
					}
				}
			}
		);

		return set;
	}

}
