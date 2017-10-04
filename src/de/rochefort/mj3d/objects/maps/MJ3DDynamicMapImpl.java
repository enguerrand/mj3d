package de.rochefort.mj3d.objects.maps;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.objects.terrains.MJ3DTerrain;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.Color;
import java.util.List;

public class MJ3DDynamicMapImpl implements MJ3DMap{

	private final boolean wireframe;
	private int backgroundColor;
	private final MJ3DTerrain terrain;
	private final boolean fog;
	public MJ3DDynamicMapImpl(MJ3DTerrain terrain, Color backgroundColor, boolean wireframe, boolean fog) {
		this.terrain = terrain;
		this.wireframe = wireframe;
		this.backgroundColor = backgroundColor.getRGB();
		this.fog = fog;
	}

	public void update(MJ3DViewingPosition viewingPosition, float cameraFocalDistance){
		terrain.update(viewingPosition, cameraFocalDistance);
	}
	
	@Override
	public int getPointsCount() {
		return terrain.getPointsCount();
	}

	@Override
	public int getBackgroundColor() {
		return backgroundColor;
	}

	@Override
	public boolean isFoggy() {
		return fog;
	}

	@Override
	public boolean isWireframe() {
		return wireframe;
	}

	@Override
	public int getTriadCount() {
		return terrain.getTriads().size();
	}

	@Override
	public List<MJ3DPoint3D> getPoints() {
		return terrain.getPoints();
	}

	@Override
	public int[][] getTriadPointIndices() {
		int[][] triadPointsArray = new int[terrain.getTriads().size()][3];  // To store the indices the respective vertices in the pointsArray
		// iterate over all triads and add their color to all their respective vertices
		// as a result each point receives color from its three adjacent triads.
		// Finally, increment the triad count for each point to be able to average in the next loop.
		for(int triadIndex=0; triadIndex<triadPointsArray.length; triadIndex++){
			for(int vertice=0; vertice<3; vertice++){
				MJ3DPoint3D currentPoint = terrain.getTriads().get(triadIndex).getPoints()[vertice];
				int pointIndex = currentPoint.getMapIndex();
				triadPointsArray[triadIndex][vertice]=pointIndex;
			}
		}
		return triadPointsArray;
	}

	@Override
	public int[] getPointColors() {
		int[][] triadPointsArray = getTriadPointIndices();
		List<MJ3DPoint3D> pointsList = terrain.getPoints();
		final int triadsSize = terrain.getTriads().size();
		int[] triadColorsArray = new int[triadsSize];
		final int pointsSize = pointsList.size();
		int[] pointsRedArray = new int[pointsSize];
		int[] pointsGreenArray = new int[pointsSize];
		int[] pointsBlueArray = new int[pointsSize];
		int[] pointsTriadCountArray = new int[pointsSize];

		int[] pointColorsArray = new int[pointsSize];
		// iterate over all triads and add their color to all their respective vertices
		// as a result each point receives color from its three adjacent triads.
		// Finally, increment the triad count for each point to be able to average in the next loop.
		for(int triadIndex=0; triadIndex<triadPointsArray.length; triadIndex++){
			final MJ3DTriad mj3DTriad = terrain.getTriads().get(triadIndex);
			triadColorsArray[triadIndex]= mj3DTriad.getColor().getRGB();
			for(int vertice=0; vertice<3; vertice++){
				MJ3DPoint3D currentPoint = mj3DTriad.getPoints()[vertice];
				int pointIndex = currentPoint.getMapIndex();
				pointsRedArray[pointIndex]+=mj3DTriad.getColor().getRed();
				pointsGreenArray[pointIndex]+=mj3DTriad.getColor().getGreen();
				pointsBlueArray[pointIndex]+=mj3DTriad.getColor().getBlue();
				pointsTriadCountArray[pointIndex]++;
			}
		}
		
		// now build the mean color for each point using the point count remembered in pointsTriadCountArray previously
		for(int i=0; i<pointColorsArray.length; i++){
			int red = (int)(pointsRedArray[i]/(float)pointsTriadCountArray[i]);
			int green = (int)(pointsGreenArray[i]/(float)pointsTriadCountArray[i]);
			int blue = (int)(pointsBlueArray[i]/(float)pointsTriadCountArray[i]);
			pointColorsArray[i]=(new Color(red, green, blue)).getRGB();
		}
		
		for(int i = 0; i< triadsSize; i++){

			final MJ3DTriad mj3DTriad = terrain.getTriads().get(i);
			triadColorsArray[i]= mj3DTriad.getColor().getRGB();
			for(int p=0; p<3; p++)
				triadPointsArray[i][p]=mj3DTriad.getPoints()[p].getMapIndex();
		}
		return pointColorsArray;
	}

}
