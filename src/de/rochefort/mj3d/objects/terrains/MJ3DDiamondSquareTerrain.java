package de.rochefort.mj3d.objects.terrains;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.RandomNumberGenerator;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.ColorBlender;

public class MJ3DDiamondSquareTerrain extends MJ3DTerrain {

	private final Color colorShade;
	private final int seaColorShallow;
	private final int seaColorDeep;
	private List<MJ3DTriad> visibleTriads = new ArrayList<MJ3DTriad>();
	private List<MJ3DVector> points = new ArrayList<MJ3DVector>();
	private List<MJ3DVector> edgePoints = new ArrayList<MJ3DVector>();
	private MJ3DVector vectorOfLight = MJ3DVector.Y_UNIT_VECTOR;
	private float[][] heights;
	private final float roughness;
	private final float width;
	private int steps;
	private RandomNumberGenerator randGen;
	private float seaLevel;
	private float ambientLight;
	private float initialAmplitude;

	private MJ3DDiamondSquareTerrain(float roughness, float width, int steps, float[][] heights, float seaLevel, float initialAmplitude, Color shadeColor, int seaColorDeep, int seaColorShallow, float ambientLight){
		this.roughness = roughness;
		this.width = width;
		this.seaLevel = seaLevel;
		this.colorShade = shadeColor;
		this.seaColorShallow = seaColorShallow;
		this.seaColorDeep = seaColorDeep;
		this.heights = heights;
		this.initialAmplitude = initialAmplitude;
		this.steps = steps;
		this.ambientLight = ambientLight;
		createTriads();
	}
	
	public MJ3DDiamondSquareTerrain(long seed, float width, int steps, float roughness, float initialAmplitude, Color shadeColor, float seaLevel, int seaColorDeep,  int seaColorShallow, float ambientLight) {
		super();
		this.seaLevel = seaLevel;
		this.colorShade = shadeColor;
		this.seaColorShallow = seaColorShallow;
		this.seaColorDeep = seaColorDeep;
		this.ambientLight = ambientLight;
		randGen = new RandomNumberGenerator(seed);
		this.roughness = roughness;
		this.width = width;
		this.steps = steps;
		this.initialAmplitude = initialAmplitude;
		int rows = (int) Math.pow(2, steps) + 1;
		heights = new float[rows][rows];
		for (int i = 0; i < heights.length; i++) {
			for (int j = 0; j < heights.length; j++) {
				heights[i][j]=Float.MAX_VALUE;
			}
		}
	}
	
	public void preSeed(int row, int column, float height){
		this.heights[row][column] = height;
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
		float maxZ = Float.MIN_VALUE;
		for(int r = 0; r<heights.length; r++){
			for(int c = 0; c<heights.length; c++){
				if(heights[r][c] > maxZ){
					maxZ = heights[r][c];
				}
				
			}
		}
		
		float deltaX = width / (heights.length - 1);
		MJ3DVector[][] tmpPoints = new MJ3DVector[heights.length][heights.length];
		for (int r = 0; r < heights.length; r++) {
			for (int c = 0; c < heights.length; c++) {
				tmpPoints[r][c] = new MJ3DVector(deltaX * r, deltaX * c, heights[r][c]);
				this.points.add(tmpPoints[r][c]);
				if(r==0||c==0||r==heights.length-1||c==heights.length-1){
					edgePoints.add(tmpPoints[r][c]);
				}
			}
		}		

		float illuminationFactor = (1f - ambientLight) *0.5f;
		for (int r = 0; r < heights.length - 1; r++) {
			for (int c = 0; c < heights.length - 1; c++) {
				MJ3DVector[] points1 = new MJ3DVector[3];
				MJ3DVector[] points2 = new MJ3DVector[3];
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
				
				float seaDepthScaling = 0.5f;
				if(p1sl && p2sl && p3sl){
					float midDepth = (points1[0].getZ() + points1[1].getZ() + points1[2].getZ()) / 3f  -  seaLevel;
//					float depthRatio = 1f - (midDepth / (maxZ - seaLevel)*seaDepthScaling+(1f-seaDepthScaling)); 
					float depthRatio = midDepth / (maxZ - seaLevel); 
					if(depthRatio > 1f || depthRatio < 0)
						System.out.println(depthRatio);
//					newTriad1.setColor(ColorBlender.scaleColor(colorSea, depthRatio));
					newTriad1.setColor(new Color(ColorBlender.blendRGB(seaColorShallow, seaColorDeep, depthRatio)));
				}
				else{
					newTriad1.setColor(ColorBlender.scaleColor(colorShade, lighting1));
				}
				if(p2sl && p3sl && p4sl){
					float midDepth = (points2[0].getZ() + points2[1].getZ() + points2[2].getZ()) / 3f  -  seaLevel;
//					float depthRatio = 1f - (midDepth / (maxZ - seaLevel)*seaDepthScaling+(1f-seaDepthScaling)); 
					float depthRatio = midDepth / (maxZ - seaLevel); 
					if(depthRatio > 1f || depthRatio < 0)
						System.out.println(depthRatio);
					newTriad2.setColor(new Color(ColorBlender.blendRGB(seaColorShallow, seaColorDeep, depthRatio)));
//					newTriad2.setColor(ColorBlender.scaleColor(colorSea, depthRatio));
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
		for (MJ3DVector pt : this.points){
			if(pt.getZ() > seaLevel){
				pt.setZ(seaLevel);
			}
		}
//		for (int row = 0; row < heights.length; row++) {
//			for (int col = 0; col < heights.length; col++) {
//				if(heights[row][col] > seaLevel){
//					heights[row][col] = seaLevel;
//				}
//			}
//		}
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
		float amplitude = initialAmplitude;
		heights[0][0] = 0;
		heights[0][squareEdgeLength] = 0;
		heights[totalRowCount - 1][0] = 0;
		heights[totalRowCount - 1][squareEdgeLength] = 0;

		for (int i = 0; i < steps; i++) {
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
			amplitude *= this.roughness;
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
		List<MJ3DVector> points = getPoints();
		for(MJ3DVector p : points){
			p.translate(translationVector);
		}
	}

	private List<MJ3DVector> getEdgePoints() {
		return edgePoints;
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
	
	public void mergeEdges(MJ3DDiamondSquareTerrain destinationTerrain){
		float tolerance = 0.5f * width / (heights.length - 1);
		int pointsMerged=0;
		for(MJ3DVector sourcePoint : getEdgePoints()){
			for(MJ3DVector destinationPoint : destinationTerrain.getEdgePoints()){
				if(sourcePoint.merge(destinationPoint, tolerance)){
					pointsMerged++;
				}
			}
		}
//		System.out.println("Points merged: "+pointsMerged);
	}

	@Override
	public List<MJ3DVector> getPoints() {
		return this.points;
	}

	@Override
	public int getPointsCount() {
		return points.size();
	}

}
