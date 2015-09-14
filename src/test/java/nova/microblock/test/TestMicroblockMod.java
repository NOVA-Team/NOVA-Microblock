package nova.microblock.test;

import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.component.misc.Collider;
import nova.core.loader.Loadable;
import nova.core.loader.Mod;
import nova.core.util.shape.Cuboid;
import nova.microblock.NovaMicroblock;
import nova.microblock.micro.Microblock;
import nova.microblock.multi.Multiblock;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;

/**
 * @author Calclavia
 */
@Mod(id = "testMicroblock", name = "Test Microblock", version = "1.0", novaVersion = "0.0.1", dependencies = { "microblock" })
public class TestMicroblockMod implements Loadable {

	public static final String containerID = "blockContainer";
	public static final String singleMicroblockID = "singleMicroblock";
	public static final String singleMultiblockID = "singleMultiblock";
	public static final String multiMicroblockID = "multiMicroblock";

	public static BlockFactory singleMicroblock;
	public static BlockFactory singleMultiblock;
	public static BlockFactory multiMicroblock1;

	@Override
	public void preInit() {
		NovaMicroblock.instance.logger.info("Preinit on TestMicroblockMod");
		singleMicroblock = NovaMicroblock.instance.blocks.register(
			singleMicroblockID,
			() -> {
				Block block = new Block();
				block.components.add(new Microblock(block)).setOnPlace(blockPlaceEvent -> Optional.of(new Vector3D(0, 0, 0)));
				return block;
			}
		);

		singleMultiblock = NovaMicroblock.instance.blocks.register(
			singleMultiblockID,
			() -> {
				Block block = new Block();
				block.components.add(new Multiblock(block));
				block.components.add(new Collider(block)).setBoundingBox(new Cuboid(Vector3D.ZERO, new Vector3D(1, 2, 1)));
				return block;
			}
		);

		multiMicroblock1 = NovaMicroblock.instance.blocks.register(
			multiMicroblockID,
			() -> {
				Block block = new Block();
				block.components.add(new Multiblock(block));
				block.components.add(new Microblock(block)).setOnPlace(blockPlaceEvent -> Optional.of(Vector3D.ZERO));
				block.components.add(new Collider(block)).setBoundingBox(new Cuboid(Vector3D.ZERO, new Vector3D(1, 1.5, 1)));
				return block;
			}
		);

		NovaMicroblock.instance.logger.info("Registered blocks");
	}
}
