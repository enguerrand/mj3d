package de.rochefort.mj3d.objects.primitives;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.objects.MJ3DObject;
import de.rochefort.mj3d.objects.terrains.MJ3DTerrain;

public class MJ3DPoint3D extends MJ3DVector implements MJ3DObject {
	private int index;
	private Set<MJ3DTriad> triads = new HashSet<MJ3DTriad>();
	private Map<MJ3DTerrain, int[]> terrainPointArrayPositions = new HashMap<MJ3DTerrain, int[]>();
	public MJ3DPoint3D() {
		this(-1);
	}
	public MJ3DPoint3D(int index) {
		super();
		this.index = index;
	}
	public MJ3DPoint3D(UnitVectorType type) {
		super(type);
	}
	
	public MJ3DPoint3D(float x, float y, float z) {
		super(x,y,z);
	}
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setTerrainPointPosition(MJ3DTerrain terrain, int row, int col){
		this.terrainPointArrayPositions.put(terrain, new int []{row, col});
	}
	
	private int getTerrainPointPos(MJ3DTerrain terrain, int index){
		if(!this.terrainPointArrayPositions.containsKey(terrain))
			return -1;
		return this.terrainPointArrayPositions.get(terrain)[index];
	}
	
	public int getTerrainPointRow(MJ3DTerrain terrain){
		return getTerrainPointPos(terrain, 0);
	}

	public int getTerrainPointCol(MJ3DTerrain terrain){
		return getTerrainPointPos(terrain, 1);
	}
	
	public void addTriad(MJ3DTriad triad){
		this.triads.add(triad);
	}
	
	public void removeTriad(MJ3DTriad triad){
		this.triads.remove(triad);
	}
	
	public Set<MJ3DTriad> getTriads() {
		return triads;
	}
}
