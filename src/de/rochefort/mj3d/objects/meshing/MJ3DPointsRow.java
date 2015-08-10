package de.rochefort.mj3d.objects.meshing;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.ColorBlender;
import sun.awt.AWTAccessor.SystemColorAccessor;

public class MJ3DPointsRow {
	private final MJ3DPoint3D[] points;
	private final float[] relativeLengths;
	private final float minTriadSize;
	private final float maxTriadSize;
	public MJ3DPointsRow(float minDistance, float maxDistance, float totalLength, PointsProducer producer) {
		if(minDistance > maxDistance){
			throw new IllegalArgumentException("minDistance ("+minDistance+") cannot be larger than maxDistance ("+maxDistance+")!");
		}
		if(2 * maxDistance + minDistance <= totalLength){
			float unroundedPointsCount = 2 * ((totalLength-maxDistance) / (minDistance + maxDistance)) + 1;
			int pointsCount = (int)unroundedPointsCount;
			if(pointsCount % 2 == 0){
				pointsCount++;
			}
			float factor = totalLength / ((0.5f * (minDistance + maxDistance) * (pointsCount - 2)) + maxDistance );
			this.minTriadSize = minDistance * factor;
			this.maxTriadSize = maxDistance * factor;
			points = new MJ3DPoint3D[pointsCount];
			relativeLengths = new float[pointsCount];
			int maxIndex = Math.round(0.5f * pointsCount);
			float increment = (maxTriadSize - minTriadSize) / (maxIndex * totalLength);
			float relativeLength = 0;
			float relativeTriadSize = maxTriadSize / totalLength;
			float delta = (minTriadSize - maxTriadSize) / totalLength;
			for(int i=0; i<pointsCount; i++){
				points[i] = producer.create(relativeLength);
				relativeLengths[i] = relativeLength;
				delta = Math.abs(i - maxIndex) * increment; 
				relativeLength = relativeLength + relativeTriadSize - delta;
			}
		} else {
			int pointsCount;
			if(totalLength > minDistance){
				pointsCount = Math.round(totalLength / minDistance);
				if(pointsCount < 2){
					pointsCount = 2;
				}
			}
			else{
				pointsCount = 1;
			}
			if(pointsCount > 1){
				float correctionFactor = totalLength / (pointsCount * minDistance);
				this.maxTriadSize = correctionFactor * minDistance;
			} else {
				this.maxTriadSize = 0f;
			}
			float relativeTriadSize = this.maxTriadSize / totalLength;
			this.minTriadSize = this.maxTriadSize;
			points = new MJ3DPoint3D[pointsCount];
			relativeLengths = new float[pointsCount];
			float rel = 0f;
			for(int i=0; i<pointsCount; i++){
				points[i] = producer.create(rel);
				relativeLengths[i] = rel;
				rel += relativeTriadSize;
			}
		}
	}
	
	public static MJ3DTriad[] mesh(MJ3DPointsRow row1, MJ3DPointsRow row2, boolean wrapEnds, Color triadColor, float ambientLight, MJ3DVector vectorOfLight){
		MJ3DPointsRow rowLarge;
		MJ3DPointsRow rowSmall;
		boolean alwaysBothTriads = false;
		boolean reversePointsOrder = false;
		if(row1.points.length > row2.points.length){
			rowLarge = row1;
			rowSmall = row2;
		} else if(row1.points.length < row2.points.length){
			rowLarge = row2;
			rowSmall = row1;
			reversePointsOrder = true;
		} else {
			alwaysBothTriads = true;
			rowLarge = row2;
			rowSmall = row1;
			reversePointsOrder = true;
		}
		
		int triadCount = rowLarge.points.length -1 + rowSmall.points.length - 1;
		if(wrapEnds && rowLarge.points.length > 1){
			triadCount += 1;
		}
		if(wrapEnds && rowSmall.points.length > 1){
			triadCount += 1;
		}
		MJ3DTriad[] triads = new MJ3DTriad[triadCount];
			
		int rowLargeIndex = 0;
		int rowSmallIndex = 0;
		int triadIndex = 0;
		
		float illuminationFactor = (1f - ambientLight) *0.5f;
		for(rowLargeIndex=0; rowLargeIndex<rowLarge.points.length-1; rowLargeIndex++){
			if(alwaysBothTriads || rowLarge.relativeLengths[rowLargeIndex] >= rowSmall.relativeLengths[rowSmallIndex] && rowSmallIndex < rowSmall.points.length-1){
				MJ3DPoint3D [] pts = {rowLarge.points[rowLargeIndex],rowSmall.points[rowSmallIndex+1],rowSmall.points[rowSmallIndex]};
				triads[triadIndex++] = createTriad(pts, triadColor, ambientLight, vectorOfLight, illuminationFactor, reversePointsOrder); 
				if(rowSmallIndex < rowSmall.points.length-1)
					rowSmallIndex++;
			}
			MJ3DPoint3D [] pts = {rowLarge.points[rowLargeIndex],rowLarge.points[rowLargeIndex+1],rowSmall.points[rowSmallIndex]};
			triads[triadIndex++] = createTriad(pts, triadColor, ambientLight, vectorOfLight, illuminationFactor, reversePointsOrder);
		}
		if(wrapEnds){
			if(rowLarge.points.length > 1){
				MJ3DPoint3D [] pts1 = {rowLarge.points[rowLargeIndex],rowLarge.points[0],rowSmall.points[0]};
				triads[triadIndex++] = createTriad(pts1, triadColor, ambientLight, vectorOfLight, illuminationFactor, reversePointsOrder); 
			}
			
			if(rowSmall.points.length > 1){
				MJ3DPoint3D [] pts2 = {rowLarge.points[rowLargeIndex],rowSmall.points[0],rowSmall.points[rowSmallIndex]};
				triads[triadIndex++] = createTriad(pts2, triadColor, ambientLight, vectorOfLight, illuminationFactor, reversePointsOrder);
			}
			
		}
		int nulls = 0;
		for(MJ3DTriad t : triads){
			if (t == null){
//				throw new IllegalStateException("Triads cannot be null!");
				nulls++;
			}
		}
		MJ3DTriad[] reduced = new MJ3DTriad[triads.length - nulls];
		System.arraycopy(triads, 0, reduced, 0, reduced.length);
		return reduced;
	}

	private static MJ3DTriad createTriad(MJ3DPoint3D[] pts, Color triadColor, float ambientLight, MJ3DVector vectorOfLight, float illuminationFactor, boolean reverseSurfaceNormal) {
		MJ3DTriad t =new MJ3DTriad(pts);
		t.updateSurfaceNormal(reverseSurfaceNormal);
		float lighting = ambientLight - illuminationFactor * (MJ3DVector.dotProduct(vectorOfLight, t.getNormal()) -1);
		t.setColor(ColorBlender.scaleColor(triadColor, lighting));
		return t;
	}
	
	public static MJ3DPoint3D[] getPoints(List<MJ3DPointsRow> rows){
		int totalCount = 0;
		for(MJ3DPointsRow r : rows){
			totalCount+=r.points.length;
		};
		MJ3DPoint3D[] pts = new MJ3DPoint3D[totalCount];
		int currentIndex = 0;
		for(MJ3DPointsRow r : rows){
			System.arraycopy(r.points, 0, pts, currentIndex, r.points.length);
			currentIndex+=r.points.length;
		}
		return pts;
	}
	
	public static MJ3DTriad[] getTriads(List<MJ3DPointsRow> rows, boolean wrapEnds, Color triadColor, float ambientLight, MJ3DVector vectorOfLight){
		MJ3DTriad[][] triadsList = new MJ3DTriad[rows.size()-1][];
		int currentIndex = 0;
		for(int rowIndex=1; rowIndex < rows.size(); rowIndex++){
			triadsList[currentIndex++] = mesh(rows.get(rowIndex-1), rows.get(rowIndex), wrapEnds, triadColor, ambientLight, vectorOfLight);
		}
		int totalTriadCount = 0;
		for(int i=0; i<triadsList.length; i++){
			totalTriadCount+=triadsList[i].length;
		}
		MJ3DTriad[] triads = new MJ3DTriad[totalTriadCount];
		int index = 0;
		for(int i=0; i<triadsList.length; i++){
			System.arraycopy(triadsList[i], 0, triads, index, triadsList[i].length);
			index += triadsList[i].length;
		}
		return triads;
	}

}
