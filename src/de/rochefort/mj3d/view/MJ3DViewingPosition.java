package de.rochefort.mj3d.view;

import de.rochefort.mj3d.math.Quaternion;

public interface MJ3DViewingPosition {
	public float getXPos();
	public float getYPos();
	public float getZPos();
	public Quaternion getOrientation();
}
