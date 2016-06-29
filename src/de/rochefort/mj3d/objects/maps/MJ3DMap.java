package de.rochefort.mj3d.objects.maps;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public interface MJ3DMap {
	int getPointsCount();
	int getBackgroundColor();
	void update(MJ3DViewingPosition newPosition, float cameraFocalDistance);
	boolean isFoggy();
	boolean isWireframe();
	int getTriadCount();
	MJ3DPoint3D[] getPoints();
	int[][] getTriadPointIndices();
	int[] getPointColors();
}
