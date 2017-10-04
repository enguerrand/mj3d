package de.rochefort.mj3d.objects;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;

import java.util.List;

public interface MJ3DObjectCompound extends MJ3DObject {
	List<MJ3DTriad> getTriads();
	List<MJ3DPoint3D> getPoints();
}
