package com.calclavia.microblock.test;

import com.calclavia.microblock.core.MicroblockAPI;
import nova.internal.launch.NovaLauncher;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Calclavia
 */
public class NovaLauncherTest extends nova.wrappertests.NovaLauncherTest {
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
	public void testMicroblockPlacement() {

		/**
		 * Basic launch test
		 */
		NovaLauncher launcher = createLauncher();
		/**
		 * Microblock placement
		 */

	}
}
