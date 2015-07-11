package nova.microblock.multi;

import nova.microblock.common.BlockComponent;
import nova.core.block.Block;
import nova.core.component.Require;
import nova.core.component.misc.Collider;
import nova.core.util.math.MathUtil;
import nova.core.util.shape.Cuboid;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

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
	public Set<Vector3D> getOccupiedSpace(float blockLength) {
		Set<Cuboid> collisionBoxes = block.get(Collider.class).occlusionBoxes.apply(Optional.empty());

		Set<Vector3D> set = new HashSet<>();
		int truncation = (int) (1 / blockLength);

		collisionBoxes.forEach(
			cuboid -> {
				//Truncate bounds to nearest block length
				for (double x = MathUtil.truncate(cuboid.min.getX(), truncation); x < cuboid.max.getX(); x += blockLength) {
					for (double y = MathUtil.truncate(cuboid.min.getY(), truncation); y < cuboid.max.getY(); y += blockLength) {
						for (double z = MathUtil.truncate(cuboid.min.getZ(), truncation); z < cuboid.max.getZ(); z += blockLength) {
							set.add(new Vector3D(x, y, z));
						}
					}
				}
			}
		);

		return set;
	}

}
