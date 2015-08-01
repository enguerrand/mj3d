package de.rochefort.mj3d.objects.terrains;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.PerlinNoiseGenerator;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.view.ColorBlender;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public class MJ3DSimplexNoiseTerrain extends MJ3DTerrain {
	private final long seed;
	private final Color colorShade;
	private final int seaColorShallow;
	private final int seaColorDeep;
	private MJ3DTriad[] visibleTriads;
	private MJ3DPoint3D[] points;
	private MJ3DVector vectorOfLight = MJ3DVector.Y_UNIT_VECTOR;
	private MJ3DPoint3D[][] pointsMatrix;
	private final int width;
	private final float triadSize;
	private final float baseFrequency;
	private final float baseAmplitude;
	private final float persistence;
	private float seaLevel;
	private float ambientLight;
	private float maxZ = Float.MIN_VALUE;

	public MJ3DSimplexNoiseTerrain(long seed, int width, float triadSize, float baseFrequency, float baseAmplitude, float persistence, float seaLevel, float ambientLight, ColorScheme colorScheme) {
		super();
		this.seed = seed;
		this.seaLevel = seaLevel;
		this.colorShade = colorScheme.getEarthColor();
		this.seaColorShallow = colorScheme.getSeaColorShallow().getRGB();
		this.seaColorDeep = colorScheme.getSeaColorDeep().getRGB();
		this.ambientLight = ambientLight;
		this.triadSize = triadSize;
		this.baseFrequency = baseFrequency;
		this.baseAmplitude = baseAmplitude;
		this.persistence = persistence;
		this.width = width;
		this.pointsMatrix = new MJ3DPoint3D[width][width];

	}

	@Override
	public void create(){
		createTerrain();
		createTriads();
	}
	

	private void createTriads() {
		List<MJ3DTriad> tmpTriads = new ArrayList<MJ3DTriad>();
		
		maxZ = Float.MIN_VALUE;
		for(int i=0; i<points.length; i++){
			maxZ = Math.max(maxZ, points[i].getZ());
		}
		
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
				tmpTriads.add(newTriad1);
				tmpTriads.add(newTriad2);
			}
		}

		createSeaLevel();
		
		this.visibleTriads = new MJ3DTriad[tmpTriads.size()];
		for(int i=0; i< tmpTriads.size(); i++){
			this.visibleTriads[i]=tmpTriads.get(i);
		}
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
		PerlinNoiseGenerator g = new PerlinNoiseGenerator(this.seed, (short) 3, 1f);
		points = new MJ3DPoint3D[width*width];
		for(int xIndex = 0; xIndex < width; xIndex++){
			for(int yIndex = 0; yIndex < width; yIndex++){
				float x = xIndex * triadSize;
				float y = yIndex * triadSize;
				float z = (g.perlinNoise2D(x, y, 10, persistence, baseFrequency, baseAmplitude));
				MJ3DPoint3D pt = new MJ3DPoint3D(x, y, z);
				pt.setTerrainPointPosition(this, xIndex, yIndex);
				pointsMatrix[xIndex][yIndex] = pt;
				points[xIndex + yIndex*width] = pt;
			}
		}
	}

	
	@Override
	public MJ3DTriad[] getTriads() {
		return visibleTriads;
	}
	
	@Override
	public MJ3DPoint3D[] getPoints() {
		return this.points;
	}

	@Override
	public int getPointsCount() {
		return points.length;
	}

	@Override
	// TODO Untested!
	public void replace(MJ3DPoint3D pointToReplace, MJ3DPoint3D replacement) {
		int r = pointToReplace.getTerrainPointRow(this);
		int c = pointToReplace.getTerrainPointCol(this);
		replacement.setTerrainPointPosition(this, r, c);
		for(int i=0; i<points.length; i++){
			if(points[i]==pointToReplace){
				points[i]=replacement;
			}
		}
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
	}

	@Override
	public void update(MJ3DViewingPosition viewingPosition) {
		
	}
}
