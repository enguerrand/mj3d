package de.rochefort.mj3d.objects.maps;

import java.util.List;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.objects.MJ3DObjectCompound;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public interface MJ3DMap {
	public List<MJ3DObjectCompound> getObjects(MJ3DViewingPosition viewingPosition);
	public int getPointsCount();
	public int getBackgroundColor(MJ3DViewingPosition viewingPosition);
	public float getMinX();
	public float getMaxX();
	public float getMinY();
	public float getMaxY();
	public float getMinZ();
	public float getMaxZ();
	public boolean isFoggy();
	public int getTriadCount();
	public MJ3DVector[] getPointsArray();
	public int[][] getTriadPointsArray();
	public int[] getTriadColorsArray();
	public int[] getPointColorsArray();
}
