package com.calclavia.microblock.operation;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import nova.core.world.World;

/**
 * @author Calclavia
 */
public abstract class ContainerOperation {
	protected final World world;
	protected final Vector3D globalPos;

	//Did the operation fail?
	public boolean fail = false;

	public ContainerOperation(World world, Vector3D globalPos) {
		this.world = world;
		this.globalPos = globalPos;
	}

	/**
	 * Does the microblock operation.
	 * @return True if the operation was successfully
	 */
	public abstract boolean operate();
}
