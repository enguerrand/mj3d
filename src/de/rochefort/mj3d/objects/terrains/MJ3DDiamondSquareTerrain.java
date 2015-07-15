package de.rochefort.mj3d.objects.terrains;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.rochefort.mj3d.exceptions.IncompatibleMergeException;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.RandomNumberGenerator;
import de.rochefort.mj3d.objects.Mergeable;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.ColorBlender;

public class MJ3DDiamondSquareTerrain extends MJ3DTerrain implements Mergeable {

	private final Color colorShade;
	private final int seaColorShallow;
	private final int seaColorDeep;
	private List<MJ3DTriad> visibleTriads = new ArrayList<MJ3DTriad>();
	private List<MJ3DPoint3D> points = new ArrayList<MJ3DPoint3D>();
	private Map<EdgeType, List<MJ3DPoint3D>> edgePoints = new HashMap<EdgeType, List<MJ3DPoint3D>>();
	private MJ3DVector vectorOfLight = MJ3DVector.Y_UNIT_VECTOR;
	private float[][] heights;
	private final float roughness;
	private final float width;
	private int steps;
	private RandomNumberGenerator randGen;
	private float seaLevel;
	private float ambientLight;
	private float initialAmplitude;
	private float maxZ = Float.MIN_VALUE;

	private MJ3DDiamondSquareTerrain(float width, int steps, float roughness, Color shadeColor, float seaLevel, int seaColorDeep, int seaColorShallow, float ambientLight) {
		super();
		this.seaLevel = seaLevel;
		this.colorShade = shadeColor;
		this.seaColorShallow = seaColorShallow;
		this.seaColorDeep = seaColorDeep;
		this.ambientLight = ambientLight;
		this.roughness = roughness;
		this.width = width;
		this.steps = steps;
		for(EdgeType e : EdgeType.values()){
			edgePoints.put(e, new ArrayList<MJ3DPoint3D>());
		}
	}
	
	private MJ3DDiamondSquareTerrain(float roughness, float width, int steps, float[][] heights, float seaLevel, float initialAmplitude, Color shadeColor, int seaColorDeep, int seaColorShallow, float ambientLight){
		this(width, steps, roughness, shadeColor, seaLevel, seaColorDeep, seaColorShallow, ambientLight);
		this.heights = heights;
		createTriads();
	}
	
	public MJ3DDiamondSquareTerrain(long seed, float width, int steps, float roughness, float initialAmplitude, Color shadeColor, float seaLevel, int seaColorDeep,  int seaColorShallow, float ambientLight) {
		this(width, steps, roughness, shadeColor, seaLevel, seaColorDeep, seaColorShallow, ambientLight);
		randGen = new RandomNumberGenerator(seed);
		this.initialAmplitude = initialAmplitude;
		int rows = (int) Math.pow(2, steps) + 1;
		heights = new float[rows][rows];
		for (int i = 0; i < heights.length; i++) {
			for (int j = 0; j < heights.length; j++) {
				heights[i][j]=Float.MAX_VALUE;
			}
		}
	}

	public void create(boolean wrappable){
		createDiamondSquareTerrain(wrappable);
		smoothOutSpikes();
		createTriads();
	}
	
	
	public MJ3DDiamondSquareTerrain createCopy(){
		return new MJ3DDiamondSquareTerrain(roughness, width, steps, heights, seaLevel, initialAmplitude, colorShade, seaColorDeep, seaColorShallow, ambientLight);
	}
	
	public float[][] getHeights() {
		return heights;
	}

	private void createTriads() {
		float deltaX = width / (heights.length - 1);
		MJ3DPoint3D[][] tmpPoints = new MJ3DPoint3D[heights.length][heights.length];
		for (int r = 0; r < heights.length; r++) {
			for (int c = 0; c < heights.length; c++) {
				tmpPoints[r][c] = new MJ3DPoint3D(deltaX * r, deltaX * c, heights[r][c]);
				tmpPoints[r][c].setTerrainPointPosition(this, r, c);
				this.points.add(tmpPoints[r][c]);
				
				if(r==0)
					edgePoints.get(EdgeType.NORTH).add(tmpPoints[r][c]);
				if(r==heights.length-1)
					edgePoints.get(EdgeType.SOUTH).add(tmpPoints[r][c]);
				if(c==0)
					edgePoints.get(EdgeType.WEST).add(tmpPoints[r][c]);
				if(c==heights.length-1)
					edgePoints.get(EdgeType.EAST).add(tmpPoints[r][c]);
			}
		}		

		maxZ = Float.MIN_VALUE;
		for(int r = 0; r<heights.length; r++){
			for(int c = 0; c<heights.length; c++){
				if(heights[r][c] > maxZ){
					maxZ = heights[r][c];
				}
				
			}
		}
		
		float illuminationFactor = (1f - ambientLight) *0.5f;
		for (int r = 0; r < heights.length - 1; r++) {
			for (int c = 0; c < heights.length - 1; c++) {
				MJ3DPoint3D[] points1 = new MJ3DPoint3D[3];
				MJ3DPoint3D[] points2 = new MJ3DPoint3D[3];
				points1[0] = tmpPoints[r][c]; 		// p1
				points1[1] = tmpPoints[r][c+1]; 	// p2
				points1[2] = tmpPoints[r+1][c]; 	// p3
				points2[0] = tmpPoints[r+1][c]; 	// p3
				points2[1] = tmpPoints[r][c+1]; 	// p2
				points2[2] = tmpPoints[r+1][c+1]; 	// p4
				boolean p1sl = tmpPoints[r][c].getZ()>=seaLevel;
				boolean p2sl = tmpPoints[r][c+1].getZ()>=seaLevel;
				boolean p3sl = tmpPoints[r+1][c].getZ()>=seaLevel;
				boolean p4sl = tmpPoints[r+1][c+1].getZ()>=seaLevel;
				MJ3DTriad newTriad1 = new MJ3DTriad(points1);
				MJ3DTriad newTriad2 = new MJ3DTriad(points2);
				newTriad1.updateSurfaceNormal();
				newTriad2.updateSurfaceNormal();
				float lighting1 = ambientLight - illuminationFactor * (MJ3DVector.dotProduct(vectorOfLight, newTriad1.getNormal())-1);
				float lighting2 = ambientLight - illuminationFactor * (MJ3DVector.dotProduct(vectorOfLight, newTriad1.getNormal())-1);
				
				if(p1sl && p2sl && p3sl){
					applySeaColor(maxZ, newTriad1);
				}
				else{
					newTriad1.setColor(ColorBlender.scaleColor(colorShade, lighting1));
				}
				if(p2sl && p3sl && p4sl){
					applySeaColor(maxZ, newTriad2);
				}
				else{
					newTriad2.setColor(ColorBlender.scaleColor(colorShade, lighting2));
				}
				this.visibleTriads.add(newTriad1);
				this.visibleTriads.add(newTriad2);
			}
		}

		createSeaLevel();
	}

	private void applySeaColor(float maxZ, MJ3DTriad triad) {
		MJ3DPoint3D[] pts = triad.getPoints();
		float midDepth = (pts[0].getOriginalZ() + pts[1].getOriginalZ() + pts[2].getOriginalZ()) / 3f  -  seaLevel;
		float depthRatio = midDepth / (maxZ - seaLevel); 
		triad.setColor(new Color(ColorBlender.blendRGB(seaColorShallow, seaColorDeep, depthRatio)));
	}

	private void smoothOutSpikes() {
		for (int row = 0; row < heights.length; row++) {
			for (int col = 0; col < heights.length; col++) {
				Diamond diamond = getDiamond(row, col, heights.length, 1);
				float h = heights[row][col];
				float hLeft = heights[row][diamond.getColLeft()];
				float hRight = heights[row][diamond.getColRight()];
				float hTop = heights[diamond.getRowTop()][col];
				float hBottom = heights[diamond.getRowBottom()][col];
				float maxEnv = Math.max(Math.max(hLeft, hRight), Math.max(hTop, hBottom));
				float minEnv = Math.min(Math.min(hLeft, hRight), Math.min(hTop, hBottom));
				if (h > maxEnv) {
					heights[row][col] = maxEnv + 0.1f * (h - maxEnv);
				} else if (h < minEnv) {
					heights[row][col] = minEnv - 0.1f * (h - minEnv);
				}
			}
		}

	}
	
	private void createSeaLevel() {
		for (MJ3DPoint3D pt : this.points){
			if(pt.getZ() > seaLevel){
				pt.setZ(seaLevel);
			}
		}
	}

	/*
	 * Arrangement of input points:
	 * 
	 * P0 P1 P2 P3
	 */
	private void createDiamondSquareTerrain(boolean wrappable) {
		int totalRowCount = heights.length;
		int squareEdgeLength = totalRowCount - 1;
		int halfSquareEdgeLength = (int) (0.5 * squareEdgeLength);
		if(heights[0][0] == Float.MAX_VALUE)
			heights[0][0] = 0;
		if(heights[0][squareEdgeLength] == Float.MAX_VALUE)
			heights[0][squareEdgeLength] = 0;
		if(heights[totalRowCount - 1][0] == Float.MAX_VALUE)
			heights[totalRowCount - 1][0] = 0;
		if(heights[totalRowCount - 1][squareEdgeLength] == Float.MAX_VALUE)
			heights[totalRowCount - 1][squareEdgeLength] = 0;

		for (int i = 0; i < steps; i++) {
			float amplitude = getAmplitude(i);
			// Diamond Step
			for (int diamondRow = halfSquareEdgeLength; diamondRow < totalRowCount; diamondRow += squareEdgeLength) {
				for (int diamondCol = halfSquareEdgeLength; diamondCol < totalRowCount; diamondCol += squareEdgeLength) {
					float h1 = heights[diamondRow - halfSquareEdgeLength][diamondCol - halfSquareEdgeLength];
					float h2 = heights[diamondRow - halfSquareEdgeLength][diamondCol + halfSquareEdgeLength];
					float h3 = heights[diamondRow + halfSquareEdgeLength][diamondCol - halfSquareEdgeLength];
					float h4 = heights[diamondRow + halfSquareEdgeLength][diamondCol + halfSquareEdgeLength];
					heights[diamondRow][diamondCol] = 0.25f * (h1 + h2 + h3 + h4) + randGen.randomFloatRepeatable(-amplitude, amplitude);
//					if(wrappable){
//						if(diamondCol==0){
//							heights[diamondRow][totalRowCount-1] = heights[diamondRow][diamondCol];
//						}
//						if(diamondRow == 0){
//							heights[totalRowCount-1][diamondCol] = heights[diamondRow][diamondCol];
//						}
//					}
				}
			}
			// Square Step
//			int maxCount = wrappable ? totalRowCount-1: totalRowCount;
			for (int diamondRow = halfSquareEdgeLength; diamondRow < totalRowCount; diamondRow += squareEdgeLength) {
				for (int diamondCol = halfSquareEdgeLength; diamondCol < totalRowCount; diamondCol += squareEdgeLength) {
					if(heights[diamondRow][diamondCol - halfSquareEdgeLength] == Float.MAX_VALUE)
						heights[diamondRow][diamondCol - halfSquareEdgeLength] = getDiamondHeight(diamondRow, diamondCol - halfSquareEdgeLength, squareEdgeLength, totalRowCount, amplitude);
					if(heights[diamondRow][diamondCol + halfSquareEdgeLength] == Float.MAX_VALUE)
						heights[diamondRow][diamondCol + halfSquareEdgeLength] = getDiamondHeight(diamondRow, diamondCol + halfSquareEdgeLength, squareEdgeLength, totalRowCount, amplitude);
					if(heights[diamondRow - halfSquareEdgeLength][diamondCol] == Float.MAX_VALUE)
						heights[diamondRow - halfSquareEdgeLength][diamondCol] = getDiamondHeight(diamondRow - halfSquareEdgeLength, diamondCol, squareEdgeLength, totalRowCount, amplitude);
					if(heights[diamondRow + halfSquareEdgeLength][diamondCol] == Float.MAX_VALUE)
						heights[diamondRow + halfSquareEdgeLength][diamondCol] = getDiamondHeight(diamondRow + halfSquareEdgeLength, diamondCol, squareEdgeLength, totalRowCount, amplitude);
					if(wrappable){
						if (diamondRow == halfSquareEdgeLength) {
									// r					//c											// r				//c
							heights[totalRowCount - 1][diamondCol] = heights[0][diamondCol];
//							heights[totalRowCount - 1][diamondCol + halfSquareEdgeLength] = heights[0][diamondCol + halfSquareEdgeLength];
						}
						if (diamondCol == halfSquareEdgeLength) {
							heights[diamondRow][totalRowCount - 1] = heights[diamondRow][0];
//							heights[diamondRow + halfSquareEdgeLength][totalRowCount - 1] = heights[diamondRow + halfSquareEdgeLength][0];
						}
					}
				}
			}
			squareEdgeLength = halfSquareEdgeLength;
			halfSquareEdgeLength *= 0.5;
		}
	}

	private float getDiamondHeight(int row, int col, int squareEdgeLength, int totalRowCount, float amplitude) {
		int delta = (int) (0.5 * squareEdgeLength);
		Diamond diamond = getDiamond(row, col, totalRowCount, delta);
		float result = 0.25f * (heights[diamond.getRowTop()][col] + heights[diamond.getRowBottom()][col] + heights[row][diamond.getColLeft()] + heights[row][diamond.getColRight()]);
		return result+this.randGen.randomFloatRepeatable(-amplitude, amplitude);
	}

	private Diamond getDiamond(int row, int col, int totalRowCount, int delta) {
		int rowTop = row - delta;
		int rowBottom = row + delta;
		int colLeft = col - delta;
		int colRight = col + delta;
		if (rowTop < 0) {
			rowTop = totalRowCount - delta - 1;
		}
		if (rowBottom > totalRowCount - 1) {
			rowBottom = delta;
		}
		if (colLeft < 0) {
			colLeft = totalRowCount - delta - 1;
		}
		if (colRight > totalRowCount - 1) {
			colRight = delta;
		}
		Diamond diamond = new Diamond(rowTop, rowBottom, colLeft, colRight);
		return diamond;
	}

	@Override
	public List<MJ3DTriad> getTriads() {
		return visibleTriads;
	}
	
	public void translate(MJ3DVector translationVector){
		List<MJ3DPoint3D> points = getPoints();
		for(MJ3DPoint3D p : points){
			p.translate(translationVector);
		}
	}

	@Override
	public List<MJ3DPoint3D> getEdgePoints(EdgeType edgeType) {
		return edgePoints.get(edgeType);
	}

	private class Diamond {
		int rowTop;
		int rowBottom;
		int colLeft;
		int colRight;

		public Diamond(int rowTop, int rowBottom, int colLeft, int colRight) {
			super();
			this.rowTop = rowTop;
			this.rowBottom = rowBottom;
			this.colLeft = colLeft;
			this.colRight = colRight;
		}

		public int getRowTop() {
			return rowTop;
		}

		public int getRowBottom() {
			return rowBottom;
		}

		public int getColLeft() {
			return colLeft;
		}

		public int getColRight() {
			return colRight;
		}
	}
	

	public void preSeed(int row, int column, float height){
		this.heights[row][column] = height;
	}
	
	@Override
	public void merge(Mergeable mergeable, EdgeType ownEdge, EdgeType otherEdge){
		List<MJ3DPoint3D> ownEdgePoints = new ArrayList<MJ3DPoint3D>();
		List<MJ3DPoint3D> otherEdgePoints = new ArrayList<MJ3DPoint3D>();
		ownEdgePoints.addAll(getEdgePoints(ownEdge));
		otherEdgePoints.addAll(mergeable.getEdgePoints(otherEdge));
		if(ownEdgePoints.size() != otherEdgePoints.size()){
			throw new IncompatibleMergeException("Own edge points count "+ownEdgePoints.size()+" does not match other edge points count "+otherEdgePoints.size());
		}
		Comparator<MJ3DVector> comp = MJ3DVector.getXyzComparator();
		Collections.sort(ownEdgePoints, comp);
		Collections.sort(otherEdgePoints, comp);
		for(int i=0; i<ownEdgePoints.size(); i++){
			mergeAndSmooth(ownEdgePoints.get(i), otherEdgePoints.get(i), mergeable);
		}
	}
	
	@Override
	public void replace(MJ3DPoint3D pointToReplace, MJ3DPoint3D replacement) {
		int r = pointToReplace.getTerrainPointRow(this);
		int c = pointToReplace.getTerrainPointCol(this);
		replacement.setTerrainPointPosition(this, r, c);
		points.remove(pointToReplace);
		points.add(replacement);
		heights[r][c]=Math.min(replacement.getZ(), this.seaLevel);
		List<MJ3DTriad> triads = new ArrayList<MJ3DTriad>();
		triads.addAll(pointToReplace.getTriads());
		for(MJ3DTriad t : triads){
			t.replacePoint(pointToReplace, replacement);
			if(t.getMinOriginalZ() >= seaLevel){
				applySeaColor(maxZ, t);
			}
			else{
				t.setColor(ColorBlender.scaleColor(colorShade, ambientLight - (1f - ambientLight) *0.5f * (MJ3DVector.dotProduct(vectorOfLight, t.getNormal())-1)));
			}
		}
		for(EdgeType e : EdgeType.values()){
			List<MJ3DPoint3D> ep = getEdgePoints(e);
			if(ep.contains(pointToReplace)){
				ep.remove(pointToReplace);
				ep.add(replacement);
			}
		}
	}

	private void mergeAndSmooth(MJ3DPoint3D ownPoint, MJ3DPoint3D otherPoint, Mergeable otherMergeable){
		if(otherMergeable instanceof MJ3DDiamondSquareTerrain){
			MJ3DDiamondSquareTerrain otherTerrain = (MJ3DDiamondSquareTerrain) otherMergeable;
			float amplitude = getAmplitude(this.steps);
			float baseHeight = 0.5f * (findAdjacentInnerPointHeight(ownPoint)+otherTerrain.findAdjacentInnerPointHeight(otherPoint));
			float newHeight = baseHeight + this.randGen.randomFloatRepeatable(-amplitude, amplitude);
			MJ3DPoint3D newPoint = MJ3DPoint3D.fromTwoPoints(ownPoint, otherPoint);
			newPoint.setZ(Math.min(seaLevel, newHeight));
			replace(ownPoint, newPoint);
			for(MJ3DTerrain otherPointTerrain : otherPoint.getTerrains()){
				otherPointTerrain.replace(otherPoint, newPoint);
			}
		} else {
			replace(ownPoint, otherPoint);
		}
		
	}
	
	private float getAmplitude(int step){
		return (float)(this.initialAmplitude * Math.pow(this.roughness, step));
	}
	
	private float findAdjacentInnerPointHeight(MJ3DPoint3D point){
		if(point.getOriginalZ()>=this.seaLevel){
			return point.getOriginalZ();		// Good enough for under water
		}
		int r = point.getTerrainPointRow(this);
		int c = point.getTerrainPointCol(this);
		// No plausibility checks here. If heights array size is < 2 this may lead to index out of bounds exceptions, but 
		// RuntimeExceptions would have to be thrown here anyways  in this case so no point in wasting cpu on checking here.
		if(r==0 && c == 0) return heights[1][1];
		int max = heights.length-1;
		if(r==max && c==max) return heights[max-1][max-1];
		if(r==0) return heights[1][c];
		if(c==0) return heights[r][1];
		if(r==max) return heights[max-1][c];
		if(c==max) return heights[r][max-1];
		throw new IllegalStateException("Row: "+r+", Col: "+c+" - Not an edge point!");
	}

	@Override
	public List<MJ3DPoint3D> getPoints() {
		return this.points;
	}

	@Override
	public int getPointsCount() {
		return points.size();
	}

}
