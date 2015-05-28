package com.calclavia.microblock.test;

import com.calclavia.microblock.core.MicroblockAPI;
import com.calclavia.microblock.core.common.MicroblockOperation;
import com.calclavia.microblock.core.micro.MicroblockContainer;
import com.calclavia.microblock.core.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.util.transform.vector.Vector3i;
import nova.internal.launch.NovaLauncher;
import nova.testutils.FakeWorld;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Calclavia
 */
public class MicroblockTest extends nova.wrappertests.NovaLauncherTest {
	@Override
	public Set<Class<?>> getTestModClasses() {
		return new HashSet<>(Arrays.asList(MicroblockAPI.class, TestMicroblockMod.class));
	}

	@Override
	public void doLaunchAssert(NovaLauncher launcher) {
		assertThat(launcher.getModClasses())
			.hasSize(2)
			.containsValue(MicroblockAPI.class)
			.containsValues(TestMicroblockMod.class);
	}

	@Test
	public void testMicroblockInjection() {
		NovaLauncher launcher = createLauncher();
		//Microblock should be replaced with a container.
		assertThat(TestMicroblockMod.singleMicroblock.makeBlock().sameType(MicroblockAPI.blockContainer)).isTrue();

		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.singleMicroblock;
		assertThat(injectionFactory.containedFactory.getID()).isEqualTo(TestMicroblockMod.singleMicroblockID);
	}

	@Test
	public void testMicroblockPlacement() {
		NovaLauncher launcher = createLauncher();

		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		Vector3i testPosition = new Vector3i(5, 5, 5);

		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.singleMicroblock;
		MicroblockOperation microblockOperation = new MicroblockOperation(fakeWorld, injectionFactory.containedFactory.makeBlock(), testPosition, new Vector3i(0, 0, 0));

		assertThat(microblockOperation.setBlock()).isTrue();

		Block block = fakeWorld.getBlock(testPosition).get();
		assertThat(block.getID()).isEqualTo(TestMicroblockMod.containerID);
		assertThat(block.has(MicroblockContainer.class)).isTrue();

		MicroblockContainer microblockContainer = block.get(MicroblockContainer.class);
		assertThat(microblockContainer.get(new Vector3i(0, 0, 0)).get().block.getID()).isEqualTo(TestMicroblockMod.singleMicroblockID);
	}

	@Test
	public void testMultiblockPlacement() {
		NovaLauncher launcher = createLauncher();

		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		Vector3i testPosition = new Vector3i(5, 5, 5);

		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.singleMultiblock;
		MicroblockOperation microblockOperation = new MicroblockOperation(fakeWorld, injectionFactory.containedFactory.makeBlock(), testPosition);

		assertThat(microblockOperation.setBlock()).isTrue();

		Block blockA = fakeWorld.getBlock(testPosition).get();
		Block blockB = fakeWorld.getBlock(testPosition.add(Vector3i.yAxis)).get();
		Block[] multiblocks = { blockA, blockB };

		Arrays.stream(multiblocks)
			.forEach(block -> {
					assertThat(block.getID()).isEqualTo(TestMicroblockMod.containerID);
					assertThat(block.has(MultiblockContainer.class)).isTrue();
					MultiblockContainer container = blockA.get(MultiblockContainer.class);
					assertThat(container.containedBlock.getID()).isEqualTo(TestMicroblockMod.singleMultiblockID);
					assertThat(Arrays.stream(multiblocks).allMatch(blockMatch -> blockMatch.get(MultiblockContainer.class).containedBlock == container.containedBlock)).isTrue();
				}
			);

	}
}
