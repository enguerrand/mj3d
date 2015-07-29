package de.rochefort.mj3d.objects.maps;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public interface MJ3DMap {
	public int getPointsCount(MJ3DViewingPosition viewingPosition);
	public int getBackgroundColor(MJ3DViewingPosition viewingPosition);
	public void update(MJ3DViewingPosition newPosition);
	public boolean isFoggy();
	public boolean isWireframe();
	public int getTriadCount(MJ3DViewingPosition viewingPosition);
	public MJ3DPoint3D[] getPointsArray(MJ3DViewingPosition viewingPosition);
	public int[][] getTriadPointsArray(MJ3DViewingPosition viewingPosition);
	public int[] getPointColorsArray(MJ3DViewingPosition viewingPosition);
}
