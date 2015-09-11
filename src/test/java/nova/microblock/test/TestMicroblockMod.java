package nova.microblock.test;

import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.component.misc.Collider;
import nova.core.loader.Loadable;
import nova.core.loader.Mod;
import nova.core.util.shape.Cuboid;
import nova.microblock.MicroblockPlugin;
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
		MicroblockPlugin.instance.logger.info("Preinit on TestMicroblockMod");
		singleMicroblock = MicroblockPlugin.instance.blocks.register(
			singleMicroblockID,
			() -> {
				Block Block = new Block();
				Block.add(new Microblock(Block)).setOnPlace(blockPlaceEvent -> Optional.of(new Vector3D(0, 0, 0)));
				return Block;
			}
		);

		singleMultiblock = MicroblockPlugin.instance.blocks.register(
			singleMultiblockID,
			() -> {
				Block Block = new Block();
				Block.add(new Multiblock(Block));
				Block.add(new Collider(Block)).setBoundingBox(new Cuboid(Vector3D.ZERO, new Vector3D(1, 2, 1)));
				return Block;
			}
		);

		multiMicroblock1 = MicroblockPlugin.instance.blocks.register(
			multiMicroblockID,
			() -> {
				Block Block = new Block();
				Block.add(new Multiblock(Block));
				Block.add(new Microblock(Block)).setOnPlace(blockPlaceEvent -> Optional.of(Vector3D.ZERO));
				Block.add(new Collider(Block)).setBoundingBox(new Cuboid(Vector3D.ZERO, new Vector3D(1, 1.5, 1)));
				return Block;
			}
		);

		MicroblockPlugin.instance.logger.info("Registered blocks");
	}
}
