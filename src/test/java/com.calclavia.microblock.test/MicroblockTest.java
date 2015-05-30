package com.calclavia.microblock.test;

import com.calclavia.microblock.core.MicroblockAPI;
import com.calclavia.microblock.core.common.BlockContainer;
import com.calclavia.microblock.core.common.MicroblockOperation;
import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.micro.MicroblockContainer;
import com.calclavia.microblock.core.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3i;
import nova.internal.launch.NovaLauncher;
import nova.testutils.FakeWorld;
import nova.wrappertests.NovaLauncherTest;
import nova.wrappertests.NovaLauncherTestFactory;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Calclavia
 */
public class MicroblockTest {

	@Test
	public void testVectorToID() {
		MicroblockContainer microblockContainer = new MicroblockContainer(null);

		new Cuboid(Vector3i.zero, Vector3i.one.multiply(microblockContainer.subdivision)).forEach(pos -> {
			int id = microblockContainer.posToID(pos);
			assertThat(microblockContainer.idToPos(id)).isEqualTo(pos);
		});
	}

	@Test
	public void testMicroblockInjection() {
		NovaLauncher launcher = new NovaLauncherTestFactory(MicroblockAPI.class, TestMicroblockMod.class).createLauncher();
		//Microblock should be replaced with a container.
		assertThat(TestMicroblockMod.singleMicroblock.makeBlock() instanceof BlockContainer).isTrue();

		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.singleMicroblock;
		assertThat(injectionFactory.containedFactory.getID()).isEqualTo(TestMicroblockMod.singleMicroblockID);
	}

	@Test
	public void testMicroblockPlacement() {
		NovaLauncher launcher = new NovaLauncherTestFactory(MicroblockAPI.class, TestMicroblockMod.class).createLauncher();

		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		Vector3i testPosition = new Vector3i(5, 5, 5);

		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.singleMicroblock;
		MicroblockOperation microblockOperation = new MicroblockOperation(fakeWorld, injectionFactory, testPosition, new Block.BlockPlaceEvent(null, null, null, null));

		assertThat(microblockOperation.setBlock()).isTrue();

		Block block = fakeWorld.getBlock(testPosition).get();
		assertThat(block.getID()).contains(TestMicroblockMod.containerID);
		assertThat(block.has(MicroblockContainer.class)).isTrue();

		MicroblockContainer microblockContainer = block.get(MicroblockContainer.class);
		assertThat(microblockContainer.get(new Vector3i(0, 0, 0)).get().block.getID()).isEqualTo(TestMicroblockMod.singleMicroblockID);
	}

	@Test
	public void testMultiblockPlacement() {
		NovaLauncher launcher = new NovaLauncherTestFactory(MicroblockAPI.class, TestMicroblockMod.class).createLauncher();

		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		Vector3i testPosition = new Vector3i(5, 5, 5);

		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.singleMultiblock;
		MicroblockOperation microblockOperation = new MicroblockOperation(fakeWorld, injectionFactory, testPosition);

		assertThat(microblockOperation.setBlock()).isTrue();

		Block blockA = fakeWorld.getBlock(testPosition).get();
		Block blockB = fakeWorld.getBlock(testPosition.add(Vector3i.yAxis)).get();
		Block[] multiblocks = { blockA, blockB };

		Arrays.stream(multiblocks)
			.forEach(block -> {
					assertThat(block.getID()).contains(TestMicroblockMod.containerID);
					assertThat(block.has(MultiblockContainer.class)).isTrue();
					MultiblockContainer container = blockA.get(MultiblockContainer.class);
					assertThat(container.containedBlock.getID()).isEqualTo(TestMicroblockMod.singleMultiblockID);
					assertThat(Arrays.stream(multiblocks).allMatch(blockMatch -> blockMatch.get(MultiblockContainer.class).containedBlock == container.containedBlock)).isTrue();
				}
			);
	}

	@Test
	public void testMultiMicroblock1() {
		NovaLauncher launcher = new NovaLauncherTestFactory(MicroblockAPI.class, TestMicroblockMod.class).createLauncher();

		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		Vector3i testPosition = new Vector3i(5, 5, 5);

		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.multiMicroblock1;
		MicroblockOperation microblockOperation = new MicroblockOperation(fakeWorld, injectionFactory, testPosition, new Block.BlockPlaceEvent(null, null, null, null));

		assertThat(microblockOperation.setBlock()).isTrue();

		Block blockA = fakeWorld.getBlock(testPosition).get();
		Block blockB = fakeWorld.getBlock(testPosition.add(Vector3i.yAxis)).get();
		Block[] multiblocks = { blockA, blockB };

		//Check containers
		Arrays.stream(multiblocks)
			.forEach(block -> {
					assertThat(block.getID()).contains(TestMicroblockMod.containerID);
					assertThat(block.has(MicroblockContainer.class)).isTrue();
				}
			);

		//Check bottom part
		MicroblockContainer microblockContainerA = blockA.get(MicroblockContainer.class);
		int sub = microblockContainerA.subdivision;
		assertThat(microblockContainerA.map().size()).isEqualTo(sub * sub * sub);

		Microblock sampleA = microblockContainerA.get(new Vector3i(0, 0, 0)).get();
		new Cuboid(Vector3i.zero, Vector3i.one.multiply(sub)).forEach(pos -> assertThat(microblockContainerA.get(pos)).contains(sampleA));

		//Check top part
		MicroblockContainer microblockContainerB = blockB.get(MicroblockContainer.class);
		Microblock sampleB = microblockContainerB.get(new Vector3i(0, 0, 0)).get();
		assertThat(microblockContainerB.map().size()).isEqualTo(sub * sub / 2 * sub);
		new Cuboid(Vector3i.zero, new Vector3i(sub, sub / 2, sub)).forEach(pos -> assertThat(microblockContainerB.get(pos)).contains(sampleB));
		new Cuboid(new Vector3i(sub, sub / 2, sub), Vector3i.one.multiply(sub)).forEach(pos -> assertThat(microblockContainerB.get(pos)).isEmpty());
	}
}
