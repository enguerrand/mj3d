package de.rochefort.mj3d.objects.maps;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public interface MJ3DMap {
	public int getPointsCount();
	public int getBackgroundColor(MJ3DViewingPosition viewingPosition);
	public float getMinX();
	public float getMaxX();
	public float getMinY();
	public float getMaxY();
	public float getMinZ();
	public float getMaxZ();
	public boolean isFoggy();
	public boolean isWireframe();
	public int getTriadCount();
	public MJ3DPoint3D[] getPointsArray();
	public int[][] getTriadPointsArray();
	public int[] getPointColorsArray();
}
