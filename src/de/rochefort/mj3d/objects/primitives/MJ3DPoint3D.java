package de.rochefort.mj3d.objects.primitives;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.objects.MJ3DObject;
import de.rochefort.mj3d.objects.terrains.MJ3DTerrain;

public class MJ3DPoint3D extends MJ3DVector implements MJ3DObject {
	private int mapIndex;
	private Set<MJ3DTriad> triads = new HashSet<MJ3DTriad>();
	private Map<MJ3DTerrain, int[]> terrainPointArrayPositions = new HashMap<MJ3DTerrain, int[]>();
	private float originalZ;

	public MJ3DPoint3D(float x, float y, float z) {
		super(x,y,z);
		this.originalZ = z;
		
	}
	
	public void setMapIndex(int index) {
		this.mapIndex = index;
	}
	
	public int getMapIndex() {
		return mapIndex;
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
	
	public Collection<MJ3DTerrain> getTerrains(){
		return this.terrainPointArrayPositions.keySet();
	}
	
	public static MJ3DPoint3D fromTwoPoints(MJ3DPoint3D point1, MJ3DPoint3D point2){
		MJ3DPoint3D result = new MJ3DPoint3D(point1.x, point1.y, point1.z);
		result.originalZ = point1.originalZ;
		result.triads.addAll(point1.triads);
		result.triads.addAll(point2.triads);
		for(Entry<MJ3DTerrain, int[]> e : point1.terrainPointArrayPositions.entrySet() ){
			result.terrainPointArrayPositions.put(e.getKey(), e.getValue());
		}
		for(Entry<MJ3DTerrain, int[]> e : point2.terrainPointArrayPositions.entrySet() ){
			result.terrainPointArrayPositions.put(e.getKey(), e.getValue());
		}
		return result;
	}
	
	public float getOriginalZ() {
		return originalZ;
	}
	
	public void setOriginalZ(float originalZ) {
		this.originalZ = originalZ;
	}
}
