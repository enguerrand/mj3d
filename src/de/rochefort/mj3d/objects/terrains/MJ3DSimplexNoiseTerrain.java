package de.rochefort.mj3d.objects.terrains;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.SimplexNoise;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.ColorBlender;

public class MJ3DSimplexNoiseTerrain extends MJ3DTerrain {

	private final Color colorShade;
	private final int seaColorShallow;
	private final int seaColorDeep;
	private List<MJ3DTriad> visibleTriads = new ArrayList<MJ3DTriad>();
	private List<MJ3DPoint3D> points = new ArrayList<MJ3DPoint3D>();
	private Map<EdgeType, List<MJ3DPoint3D>> edgePoints = new HashMap<EdgeType, List<MJ3DPoint3D>>();
	private MJ3DVector vectorOfLight = MJ3DVector.Y_UNIT_VECTOR;
	private MJ3DPoint3D[][] pointsMatrix;
	private final float width;
	private final float triadSize;
	private float seaLevel;
	private float ambientLight;
	private float maxZ = Float.MIN_VALUE;

	public MJ3DSimplexNoiseTerrain(int width, float triadSize, Color shadeColor, float seaLevel, int seaColorDeep, int seaColorShallow, float ambientLight) {
		super();
		this.seaLevel = seaLevel;
		this.colorShade = shadeColor;
		this.seaColorShallow = seaColorShallow;
		this.seaColorDeep = seaColorDeep;
		this.ambientLight = ambientLight;
		this.triadSize = triadSize;
		this.width = width;
		this.pointsMatrix = new MJ3DPoint3D[width][width];

	}

	@Override
	public void create(){
		createTerrain();
		createTriads();
	}
	

	private void createTriads() {
		maxZ = Float.MIN_VALUE;
		points.forEach(p-> maxZ = Math.max(maxZ, p.getZ()));
		
		float illuminationFactor = (1f - ambientLight) *0.5f;
		for (int r = 0; r < pointsMatrix.length - 1; r++) {
			for (int c = 0; c < pointsMatrix.length - 1; c++) {
				MJ3DPoint3D[] points1 = new MJ3DPoint3D[3];
				MJ3DPoint3D[] points2 = new MJ3DPoint3D[3];
				points1[0] = pointsMatrix[r][c]; 		// p1
				points1[1] = pointsMatrix[r][c+1]; 	// p2
				points1[2] = pointsMatrix[r+1][c]; 	// p3
				points2[0] = pointsMatrix[r+1][c]; 	// p3
				points2[1] = pointsMatrix[r][c+1]; 	// p2
				points2[2] = pointsMatrix[r+1][c+1]; 	// p4
				boolean p1sl = pointsMatrix[r][c].getZ()>=seaLevel;
				boolean p2sl = pointsMatrix[r][c+1].getZ()>=seaLevel;
				boolean p3sl = pointsMatrix[r+1][c].getZ()>=seaLevel;
				boolean p4sl = pointsMatrix[r+1][c+1].getZ()>=seaLevel;
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
	private void createTerrain() {
		for(int xIndex = 0; xIndex < width; xIndex++){
			for(int yIndex = 0; yIndex < width; yIndex++){
				float x = xIndex * triadSize;
				float y = yIndex * triadSize;
				float z = ((float)SimplexNoise.noise(x, y))*100f;
				MJ3DPoint3D pt = new MJ3DPoint3D(x, y, z);
				pointsMatrix[xIndex][yIndex] = pt;
				points.add(pt);
			}
		}
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
	public List<MJ3DPoint3D> getPoints() {
		return this.points;
	}

	@Override
	public int getPointsCount() {
		return points.size();
	}

	
	@Override
	public void replace(MJ3DPoint3D pointToReplace, MJ3DPoint3D replacement) {
		//FIXME
//		int r = pointToReplace.getTerrainPointRow(this);
//		int c = pointToReplace.getTerrainPointCol(this);
//		replacement.setTerrainPointPosition(this, r, c);
//		points.remove(pointToReplace);
//		points.add(replacement);
//		List<MJ3DTriad> triads = new ArrayList<MJ3DTriad>();
//		triads.addAll(pointToReplace.getTriads());
//		for(MJ3DTriad t : triads){
//			t.replacePoint(pointToReplace, replacement);
//			if(t.getMinOriginalZ() >= seaLevel){
//				applySeaColor(maxZ, t);
//			}
//			else{
//				t.setColor(ColorBlender.scaleColor(colorShade, ambientLight - (1f - ambientLight) *0.5f * (MJ3DVector.dotProduct(vectorOfLight, t.getNormal())-1)));
//			}
//		}
	}
}
