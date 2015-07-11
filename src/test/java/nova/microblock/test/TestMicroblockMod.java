package nova.microblock.test;

import nova.microblock.MicroblockPlugin;
import nova.microblock.micro.Microblock;
import nova.microblock.multi.Multiblock;
import nova.core.block.BlockFactory;
import nova.core.component.misc.Collider;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;
import nova.core.util.shape.Cuboid;
import nova.testutils.FakeBlock;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

import java.util.Optional;

/**
 * @author Calclavia
 */
@NovaMod(id = "testMicroblock", name = "Test Microblock", version = "1.0", novaVersion = "0.0.1", dependencies = { "microblock" })
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
		singleMicroblock = MicroblockPlugin.instance.blocks.register(args -> {
			FakeBlock fakeBlock = new FakeBlock(singleMicroblockID);
			fakeBlock.add(new Microblock(fakeBlock)).setOnPlace(blockPlaceEvent -> Optional.of(new Vector3D(0, 0, 0)));
			return fakeBlock;
		});

		singleMultiblock = MicroblockPlugin.instance.blocks.register(args -> {
			FakeBlock fakeBlock = new FakeBlock(singleMultiblockID);
			fakeBlock.add(new Multiblock(fakeBlock));
			fakeBlock.add(new Collider()).setBoundingBox(new Cuboid(Vector3D.ZERO, new Vector3D(1, 2, 1)));
			return fakeBlock;
		});

		multiMicroblock1 = MicroblockPlugin.instance.blocks.register(args -> {
			FakeBlock fakeBlock = new FakeBlock(multiMicroblockID);
			fakeBlock.add(new Multiblock(fakeBlock));
			fakeBlock.add(new Microblock(fakeBlock)).setOnPlace(blockPlaceEvent -> Optional.of(Vector3D.ZERO));
			fakeBlock.add(new Collider()).setBoundingBox(new Cuboid(Vector3D.ZERO, new Vector3D(1, 1.5, 1)));
			return fakeBlock;
		});

		MicroblockPlugin.instance.logger.info("Registered blocks");
	}
}
