package de.rochefort.mj3d.objects.maps;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public interface MJ3DMap {
	public int getPointsCount();
	public int getBackgroundColor();
	public void update(MJ3DViewingPosition newPosition);
	public boolean isFoggy();
	public boolean isWireframe();
	public int getTriadCount();
	public MJ3DPoint3D[] getPoints();
	public int[][] getTriadPointIndices();
	public int[] getPointColors();
}
