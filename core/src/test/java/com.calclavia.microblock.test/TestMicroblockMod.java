package com.calclavia.microblock.test;

import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.multi.Multiblock;
import nova.core.block.BlockFactory;
import nova.core.component.misc.Collider;
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3i;
import nova.testutils.FakeBlock;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Calclavia
 */
@NovaMod(id = "testMicroblock", name = "Test Microblock", version = "1.0", novaVersion = "0.0.1", dependencies = { "microblock" })
public class TestMicroblockMod implements Loadable {

	public static final String containerID = "blockContainer";
	public static final String singleMicroblockID = "singleMicroblock";
	public static final String singleMultiblockID = "singleMultiblock";

	public static BlockFactory singleMicroblock;
	public static BlockFactory singleMultiblock;

	@Override
	public void preInit() {
		Game.instance.logger.info("Preinit on TestMicroblockMod");
		singleMicroblock = Game.instance.blockManager.register(args -> {
			FakeBlock fakeBlock = new FakeBlock(singleMicroblockID);
			fakeBlock.add(new Microblock(fakeBlock, blockPlaceEvent -> new Vector3i(0, 0, 0)));
			return fakeBlock;
		});

		singleMultiblock = Game.instance.blockManager.register(args -> {
			FakeBlock fakeBlock = new FakeBlock(singleMultiblockID);
			fakeBlock.add(new Multiblock(fakeBlock));
			fakeBlock.add(new Collider()).collisionBoxes = Arrays.asList(new Cuboid(Vector3i.zero, new Vector3i(1, 2, 1)));
			return fakeBlock;
		});

		Game.instance.logger.info("Registered blocks");
	}
}
