package com.calclavia.microblock.core;

import com.calclavia.microblock.core.common.BlockContainer;
import com.calclavia.microblock.core.micro.Microblock;
import com.calclavia.microblock.core.micro.MicroblockContainer;
import com.calclavia.microblock.core.multi.Multiblock;
import com.calclavia.microblock.core.multi.MultiblockContainer;
import nova.core.block.Block;
import nova.core.block.BlockFactory;
import nova.core.game.Game;
import nova.core.loader.Loadable;
import nova.core.loader.NovaMod;
import nova.core.util.exception.NovaException;
import nova.core.util.transform.shape.Cuboid;
import nova.core.util.transform.vector.Vector3d;
import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * @author Calclavia
 */
@NovaMod(id = "microblock", name = "Microblock", version = "0.0.1", novaVersion = "0.0.1", isPlugin = true)
public class MicroblockAPI implements Loadable {

	public static BlockFactory blockContainer;


	@Override
	public void preInit() {
		blockContainer = Game.instance.blockManager.register(BlockContainer.class);

		//TODO: Replace block factory with a sneaky factory that switches all blocks to BlockContainer.
	}
}
