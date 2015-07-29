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

class MJ3DStaticMapImpl implements MJ3DMap {
	private final int bg;
	private final boolean foggy;
	private final boolean wireframe;
	private final MJ3DPoint3D[] pointsArray;
	private final int[][] triadPointsArray;
	private final int[] triadColorsArray;
	private final int[] pointColorsArray;
	
	MJ3DStaticMapImpl(Collection<MJ3DObject> mj3dObjects, int backgroundColor, boolean foggy, boolean wireframe){
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
				for(MJ3DTriad t : comp.getTriads()){
					triadList.add(t);
				}
				for(MJ3DPoint3D p : comp.getPoints()){
					pointsList.add(p);
				}
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
		for(int i=0; i<pointsArray.length; i++){
			pointsList.get(i).setMapIndex(i);
			pointsArray[i]=pointsList.get(i);
		}
		
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
	public int getBackgroundColor() {
		return bg;
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
	public MJ3DPoint3D[] getPoints() {
		return pointsArray;
	}

	@Override
	public int[][] getTriadPointIndices() {
		return triadPointsArray;
	}

	@Override
	public int[] getPointColors() {
		return pointColorsArray;
	}

	@Override
	public void update(MJ3DViewingPosition newPosition) {
		
	}
}
