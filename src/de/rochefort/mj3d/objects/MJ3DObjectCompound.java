package de.rochefort.mj3d.objects;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;

public interface MJ3DObjectCompound extends MJ3DObject {
	public MJ3DTriad[] getTriads();
	public MJ3DPoint3D[] getPoints();
}
