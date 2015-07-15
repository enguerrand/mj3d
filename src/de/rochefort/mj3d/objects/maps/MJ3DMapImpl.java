package de.rochefort.mj3d.objects.maps;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import de.rochefort.mj3d.objects.MJ3DObject;
import de.rochefort.mj3d.objects.MJ3DObjectCompound;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

class MJ3DMapImpl implements MJ3DMap {
	private final int bg;
	private final float minX;
	private final float maxX;
	private final float minY;
	private final float maxY;
	private final float minZ;
	private final float maxZ;
	private final boolean foggy;
	private final boolean wireframe;
	private final MJ3DPoint3D[] pointsArray;
	private final int[][] triadPointsArray;
	private final int[] triadColorsArray;
	private final int[] pointColorsArray;
	
	MJ3DMapImpl(Collection<MJ3DObject> mj3dObjects, int backgroundColor, boolean foggy, boolean wireframe){
		this.bg = backgroundColor;
		this.foggy = foggy;
		this.wireframe = wireframe;
		List<MJ3DPoint3D> pointsList = new ArrayList<MJ3DPoint3D>();
		List<MJ3DTriad> triadList = new ArrayList<MJ3DTriad>();
		LinkedList<MJ3DObjectCompound> objectsCompounds = new LinkedList<MJ3DObjectCompound>();
		for(MJ3DObject obj : mj3dObjects){
			if(obj instanceof MJ3DObjectCompound){
				MJ3DObjectCompound comp = (MJ3DObjectCompound)obj;
				objectsCompounds.add(comp);
				triadList.addAll(comp.getTriads());
				pointsList.addAll(comp.getPoints());
			} else if (obj instanceof MJ3DTriad){
				MJ3DTriad triad = (MJ3DTriad)obj;
				triadList.add(triad);
				pointsList.addAll(Arrays.asList(triad.getPoints()));
			}
		}
		

		pointsArray = new MJ3DPoint3D[pointsList.size()];
		pointColorsArray = new int[pointsList.size()];
		triadPointsArray = new int[triadList.size()][3];  // To store the indices the respective vertices in the pointsArray
		triadColorsArray = new int[triadList.size()];
		int[] pointsRedArray = new int[pointsList.size()];
		int[] pointsGreenArray = new int[pointsList.size()];
		int[] pointsBlueArray = new int[pointsList.size()];
		int[] pointsTriadCountArray = new int[pointsList.size()];
		// Fill the pointsarray
		
		
		float maxTmpX = Float.MIN_VALUE;
		float maxTmpY = Float.MIN_VALUE;
		float maxTmpZ = Float.MIN_VALUE;
		float minTmpX = Float.MAX_VALUE;
		float minTmpY = Float.MAX_VALUE;
		float minTmpZ = Float.MAX_VALUE;
		for(int i=0; i<pointsArray.length; i++){
			pointsList.get(i).setMapIndex(i);
			pointsArray[i]=pointsList.get(i);
			maxTmpX = Math.max(maxTmpX, pointsArray[i].getX());
			maxTmpY = Math.max(maxTmpY, pointsArray[i].getY());
			maxTmpZ = Math.max(maxTmpZ, pointsArray[i].getZ());
			minTmpX = Math.min(minTmpX, pointsArray[i].getX());
			minTmpY = Math.min(minTmpY, pointsArray[i].getY());
			minTmpZ = Math.min(minTmpZ, pointsArray[i].getZ());
		}
		
		maxX = maxTmpX; 
		maxY = maxTmpY; 
		maxZ = maxTmpZ; 
		minX = minTmpX; 
		minY = minTmpY; 
		minZ = minTmpZ; 
		
		// iterate over all triads and add their color to all their respective vertices
		// as a result each point receives color from its three adjacent triads.
		// Finally, increment the triad count for each point to be able to average in the next loop.
		for(int triadIndex=0; triadIndex<triadPointsArray.length; triadIndex++){
			triadColorsArray[triadIndex]=triadList.get(triadIndex).getColor().getRGB();
			for(int vertice=0; vertice<3; vertice++){
//				int pointIndex = triadList.get(triadIndex).getPoints()[vertice].getIndex();
//				triadPointsArray[triadIndex][vertice]=pointIndex;
//				pointsRedArray[pointIndex]+=triadList.get(triadIndex).getColor().getRed();
//				pointsGreenArray[pointIndex]+=triadList.get(triadIndex).getColor().getGreen();
//				pointsBlueArray[pointIndex]+=triadList.get(triadIndex).getColor().getBlue();
//				pointsTriadCountArray[pointIndex]++;

				MJ3DPoint3D currentPoint = triadList.get(triadIndex).getPoints()[vertice];
				int pointIndex = currentPoint.getMapIndex();
				triadPointsArray[triadIndex][vertice]=pointIndex;
				pointsRedArray[pointIndex]+=triadList.get(triadIndex).getColor().getRed();
				pointsGreenArray[pointIndex]+=triadList.get(triadIndex).getColor().getGreen();
				pointsBlueArray[pointIndex]+=triadList.get(triadIndex).getColor().getBlue();
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
		for(int i=0; i<triadList.size(); i++){
			
			triadColorsArray[i]=triadList.get(i).getColor().getRGB();
			for(int p=0; p<3; p++)
				triadPointsArray[i][p]=triadList.get(i).getPoints()[p].getMapIndex();
		}
		
		
	}

	@Override
	public int getBackgroundColor(MJ3DViewingPosition viewingPosition) {
		return bg;
	}

	@Override
	public float getMinX() {
		return minX;
	}

	@Override
	public float getMaxX() {
		return maxX;
	}

	@Override
	public float getMinY() {
		return minY;
	}

	@Override
	public float getMaxY() {
		return maxY;
	}

	@Override
	public float getMinZ() {
		return minZ;
	}

	@Override
	public float getMaxZ() {
		return maxZ;
	}

	@Override
	public boolean isFoggy() {
		return foggy;
	}
	
	@Override
	public boolean isWireframe() {
		return wireframe;
	}

	@Override
	public int getPointsCount() {
		return pointsArray.length;
	}

	@Override
	public int getTriadCount() {
		return triadPointsArray.length;
	}

	@Override
	public MJ3DPoint3D[] getPointsArray() {
		return pointsArray;
	}

	@Override
	public int[][] getTriadPointsArray() {
		return triadPointsArray;
	}

	@Override
	public int[] getPointColorsArray() {
		return pointColorsArray;
	}
}
