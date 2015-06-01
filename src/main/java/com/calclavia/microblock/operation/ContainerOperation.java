package com.calclavia.microblock.operation;

import nova.core.util.transform.vector.Vector3i;
import nova.core.world.World;

/**
 * @author Calclavia
 */
public abstract class ContainerOperation {
	protected final World world;
	protected final Vector3i globalPos;

	//Did the operation fail?
	public boolean fail = false;

	public ContainerOperation(World world, Vector3i globalPos) {
		this.world = world;
		this.globalPos = globalPos;
	}

	/**
	 * Does the microblock operation.
	 * @return True if the operation was successfully
	 */
	public abstract boolean operate();
}
