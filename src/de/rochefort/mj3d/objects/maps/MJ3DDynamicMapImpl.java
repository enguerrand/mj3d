package de.rochefort.mj3d.objects.maps;

import java.awt.Color;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.terrains.MJ3DTerrain;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public class MJ3DDynamicMapImpl implements MJ3DMap{

	private final boolean wireframe;
	private int backgroundColor;
	private final MJ3DTerrain terrain;
	public MJ3DDynamicMapImpl(MJ3DTerrain terrain, Color backgroundColor, boolean wireframe) {
		this.terrain = terrain;
		this.wireframe = wireframe;
		this.backgroundColor = backgroundColor.getRGB();
	}

	public void update(MJ3DViewingPosition newPosition){
		terrain.update(newPosition);
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
		return true;
	}

	@Override
	public boolean isWireframe() {
		return wireframe;
	}

	@Override
	public int getTriadCount() {
		return terrain.getTriads().length;
	}

	@Override
	public MJ3DPoint3D[] getPoints() {
		return terrain.getPoints();
	}

	@Override
	public int[][] getTriadPointIndices() {
		int[][] triadPointsArray = new int[terrain.getTriads().length][3];  // To store the indices the respective vertices in the pointsArray
		// iterate over all triads and add their color to all their respective vertices
		// as a result each point receives color from its three adjacent triads.
		// Finally, increment the triad count for each point to be able to average in the next loop.
		for(int triadIndex=0; triadIndex<triadPointsArray.length; triadIndex++){
			for(int vertice=0; vertice<3; vertice++){
				MJ3DPoint3D currentPoint = terrain.getTriads()[triadIndex].getPoints()[vertice];
				int pointIndex = currentPoint.getMapIndex();
				triadPointsArray[triadIndex][vertice]=pointIndex;
			}
		}
		return triadPointsArray;
	}

	@Override
	public int[] getPointColors() {
		int[][] triadPointsArray = getTriadPointIndices();
		MJ3DPoint3D[] pointsList = terrain.getPoints();
		int[] triadColorsArray = new int[terrain.getTriads().length];
		int[] pointsRedArray = new int[pointsList.length];
		int[] pointsGreenArray = new int[pointsList.length];
		int[] pointsBlueArray = new int[pointsList.length];
		int[] pointsTriadCountArray = new int[pointsList.length];

		int[] pointColorsArray = new int[pointsList.length];
		// iterate over all triads and add their color to all their respective vertices
		// as a result each point receives color from its three adjacent triads.
		// Finally, increment the triad count for each point to be able to average in the next loop.
		for(int triadIndex=0; triadIndex<triadPointsArray.length; triadIndex++){
			triadColorsArray[triadIndex]=terrain.getTriads()[triadIndex].getColor().getRGB();
			for(int vertice=0; vertice<3; vertice++){
				MJ3DPoint3D currentPoint = terrain.getTriads()[triadIndex].getPoints()[vertice];
				int pointIndex = currentPoint.getMapIndex();
				pointsRedArray[pointIndex]+=terrain.getTriads()[triadIndex].getColor().getRed();
				pointsGreenArray[pointIndex]+=terrain.getTriads()[triadIndex].getColor().getGreen();
				pointsBlueArray[pointIndex]+=terrain.getTriads()[triadIndex].getColor().getBlue();
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
		
		for(int i=0; i<terrain.getTriads().length; i++){
			
			triadColorsArray[i]=terrain.getTriads()[i].getColor().getRGB();
			for(int p=0; p<3; p++)
				triadPointsArray[i][p]=terrain.getTriads()[i].getPoints()[p].getMapIndex();
		}
		return pointColorsArray;
	}

}
