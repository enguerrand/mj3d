package de.rochefort.mj3d.objects;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.terrains.EdgeType;

import java.util.List;

public interface Mergeable extends MJ3DObject{

	List<MJ3DPoint3D> getEdgePoints(EdgeType edgeType);
	void merge(Mergeable mergeable, EdgeType ownEdge, EdgeType otherEdge);

}
