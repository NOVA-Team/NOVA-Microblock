package com.calclavia.microblock.test;

import com.calclavia.microblock.core.MicroblockAPI;
import com.calclavia.microblock.core.common.MicroblockOperation;
import nova.core.util.transform.vector.Vector3i;
import nova.internal.dummy.Wrapper;
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
		assertThat(TestMicroblockMod.smallMicroblock.makeBlock(new Wrapper()).sameType(MicroblockAPI.blockContainer)).isTrue();

		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.smallMicroblock;
		assertThat(injectionFactory.getID()).isEqualTo("smallMicroblock");
	}

	@Test
	public void testMicroblockPlacement() {
		NovaLauncher launcher = createLauncher();

		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		MicroblockAPI.MicroblockInjectFactory injectionFactory = (MicroblockAPI.MicroblockInjectFactory) TestMicroblockMod.smallMicroblock;
		MicroblockOperation microblockOperation = new MicroblockOperation(fakeWorld, injectionFactory.makeBlock(new Wrapper()), new Vector3i(5, 5, 5));

		assertThat(microblockOperation.setBlock()).isTrue();
	}
}
