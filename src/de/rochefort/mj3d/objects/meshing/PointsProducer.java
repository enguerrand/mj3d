package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;

public interface PointsProducer {
	public MJ3DPoint3D create(float relativeLengthOnRow); 
}
