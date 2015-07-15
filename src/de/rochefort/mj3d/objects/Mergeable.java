package de.rochefort.mj3d.objects;

import java.util.List;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.terrains.EdgeType;

public interface Mergeable extends MJ3DObject{

	public List<MJ3DPoint3D> getEdgePoints(EdgeType edgeType);
	public void merge(Mergeable mergeable, EdgeType ownEdge, EdgeType otherEdge);

}
