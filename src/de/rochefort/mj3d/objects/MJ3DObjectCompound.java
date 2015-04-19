package de.rochefort.mj3d.objects;

import java.util.List;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;

public interface MJ3DObjectCompound extends MJ3DObject {
	public List<MJ3DTriad> getTriads();
	public List<MJ3DVector> getPoints();
}
