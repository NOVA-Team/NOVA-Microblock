package nova.microblock.test;

import nova.core.block.Block;
import nova.core.util.math.Vector3DUtil;
import nova.core.util.shape.Cuboid;
import nova.microblock.NovaMicroblock;
import nova.microblock.common.BlockContainer;
import nova.microblock.micro.Microblock;
import nova.microblock.micro.MicroblockContainer;
import nova.microblock.multi.MultiblockContainer;
import nova.microblock.operation.ContainerPlace;
import nova.testutils.FakeWorld;
import nova.wrappertests.NovaLauncherTestFactory;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Calclavia
 */
public class MicroblockTest {

	@Before
	public void setUp() throws Exception {
		new NovaLauncherTestFactory(NovaMicroblock.class, TestMicroblockMod.class).createLauncher();
	}

	@Test
	public void testVectorToID() {
		MicroblockContainer microblockContainer = new MicroblockContainer(null);

		new Cuboid(Vector3D.ZERO, Vector3DUtil.ONE.scalarMultiply(microblockContainer.subdivision)).forEach(pos -> {
			int id = microblockContainer.posToID(pos);
			assertThat(microblockContainer.idToPos(id)).isEqualTo(pos);
		});
	}

	@Test
	public void testMicroblockInjection() {
		//Microblock should be replaced withPriority a container.
		assertThat(TestMicroblockMod.singleMicroblock.build() instanceof BlockContainer).isTrue();

		NovaMicroblock.MicroblockInjectFactory injectionFactory = (NovaMicroblock.MicroblockInjectFactory) TestMicroblockMod.singleMicroblock;
		assertThat(injectionFactory.containedFactory.getID()).isEqualTo(TestMicroblockMod.singleMicroblockID);
	}

	@Test
	public void testMicroblockPlacement() {
		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		Vector3D testPosition = new Vector3D(5, 5, 5);

		NovaMicroblock.MicroblockInjectFactory injectionFactory = (NovaMicroblock.MicroblockInjectFactory) TestMicroblockMod.singleMicroblock;
		ContainerPlace microblockPlace = new ContainerPlace(fakeWorld, injectionFactory, testPosition, new Block.PlaceEvent(null, null, null, null));

		assertThat(microblockPlace.operate()).isTrue();

		Block block = fakeWorld.getBlock(testPosition).get();
		assertThat(block.getID()).contains(TestMicroblockMod.containerID);
		assertThat(block.components.has(MicroblockContainer.class)).isTrue();

		MicroblockContainer microblockContainer = block.components.get(MicroblockContainer.class);
		Assertions.assertThat(microblockContainer.get(new Vector3D(0, 0, 0)).get().block.getID()).isEqualTo(TestMicroblockMod.singleMicroblockID);
	}

	@Test
	public void testMultiblockPlacement() {
		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		Vector3D testPosition = new Vector3D(5, 5, 5);

		NovaMicroblock.MicroblockInjectFactory injectionFactory = (NovaMicroblock.MicroblockInjectFactory) TestMicroblockMod.singleMultiblock;
		ContainerPlace microblockPlace = new ContainerPlace(fakeWorld, injectionFactory, testPosition);

		assertThat(microblockPlace.operate()).isTrue();

		Block blockA = fakeWorld.getBlock(testPosition).get();
		Block blockB = fakeWorld.getBlock(testPosition.add(Vector3D.PLUS_J)).get();
		Block[] multiblocks = { blockA, blockB };

		Arrays.stream(multiblocks)
			.forEach(block -> {
					assertThat(block.getID()).contains(TestMicroblockMod.containerID);
					assertThat(block.components.has(MultiblockContainer.class)).isTrue();
					MultiblockContainer container = blockA.components.get(MultiblockContainer.class);
					assertThat(container.containedBlock.getID()).isEqualTo(TestMicroblockMod.singleMultiblockID);
					assertThat(Arrays.stream(multiblocks).allMatch(blockMatch -> blockMatch.components.get(MultiblockContainer.class).containedBlock == container.containedBlock)).isTrue();
				}
			);
	}

	@Test
	public void testMultiMicroblock1() {
		/**
		 * Microblock placement
		 */
		FakeWorld fakeWorld = new FakeWorld();
		Vector3D testPosition = new Vector3D(5, 5, 5);

		NovaMicroblock.MicroblockInjectFactory injectionFactory = (NovaMicroblock.MicroblockInjectFactory) TestMicroblockMod.multiMicroblock1;
		ContainerPlace microblockPlace = new ContainerPlace(fakeWorld, injectionFactory, testPosition, new Block.PlaceEvent(null, null, null, null));

		assertThat(microblockPlace.operate()).isTrue();

		Block blockA = fakeWorld.getBlock(testPosition).get();
		Block blockB = fakeWorld.getBlock(testPosition.add(Vector3D.PLUS_J)).get();
		Block[] multiblocks = { blockA, blockB };

		//Check containers
		Arrays.stream(multiblocks)
			.forEach(block -> {
					assertThat(block.getID()).contains(TestMicroblockMod.containerID);
					assertThat(block.components.has(MicroblockContainer.class)).isTrue();
				}
			);

		//Check bottom part
		MicroblockContainer microblockContainerA = blockA.components.get(MicroblockContainer.class);
		int sub = microblockContainerA.subdivision;
		assertThat(microblockContainerA.map().size()).isEqualTo(sub * sub * sub);

		Microblock sampleA = microblockContainerA.get(new Vector3D(0, 0, 0)).get();
		new Cuboid(Vector3D.ZERO, Vector3DUtil.ONE.scalarMultiply(sub)).forEach(pos -> assertThat(microblockContainerA.get(pos)).contains(sampleA));

		//Check top part
		MicroblockContainer microblockContainerB = blockB.components.get(MicroblockContainer.class);
		Microblock sampleB = microblockContainerB.get(new Vector3D(0, 0, 0)).get();
		assertThat(microblockContainerB.map().size()).isEqualTo(sub * sub / 2 * sub);
		new Cuboid(Vector3D.ZERO, new Vector3D(sub, sub / 2, sub)).forEach(pos -> assertThat(microblockContainerB.get(pos)).contains(sampleB));
		new Cuboid(new Vector3D(sub, sub / 2, sub), Vector3DUtil.ONE.scalarMultiply(sub)).forEach(pos -> assertThat(microblockContainerB.get(pos)).isEmpty());
	}
}
