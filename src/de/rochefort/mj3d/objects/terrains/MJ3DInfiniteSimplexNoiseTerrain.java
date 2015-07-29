package de.rochefort.mj3d.objects.terrains;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.PerlinNoiseGenerator;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.view.ColorBlender;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public class MJ3DInfiniteSimplexNoiseTerrain extends MJ3DTerrain {
	private final long seed;
	private final Color colorShade;
	private final int seaColorShallow;
	private final int seaColorDeep;
	private MJ3DVector vectorOfLight = MJ3DVector.Y_UNIT_VECTOR;
	private MJ3DTriad[] visibleTriads;
	private MJ3DTriad[] visibleTriadBuffer;
	private MJ3DPoint3D[] points;
	private MJ3DPoint3D[] pointBuffer;
	private MJ3DPoint3D[][] pointsMatrix;
	private MJ3DPoint3D[][] pointsMatrixBuffer;
	private MJ3DViewingPosition lastViewingPosition;
	private final PerlinNoiseGenerator pnGen;
	private final int width;
	private final float visibility;
	private final float triadSize;
	private final float baseFrequency;
	private final float baseAmplitude;
	private final float persistence;
	private float seaLevel;
	private float ambientLight;
	private float maxZ = Float.MIN_VALUE;

	public MJ3DInfiniteSimplexNoiseTerrain(MJ3DViewingPosition initialViewingPosition, long seed, float visibility, float triadSize, float baseFrequency, float baseAmplitude, float persistence, float seaLevel, float ambientLight, ColorScheme colorScheme) {
		super();
		this.seed = seed;
		this.lastViewingPosition = initialViewingPosition;
		this.seaLevel = seaLevel;
		this.colorShade = colorScheme.getEarthColor();
		this.seaColorShallow = colorScheme.getSeaColorShallow().getRGB();
		this.seaColorDeep = colorScheme.getSeaColorDeep().getRGB();
		this.ambientLight = ambientLight;
		this.triadSize = triadSize;
		this.baseFrequency = baseFrequency;
		this.baseAmplitude = baseAmplitude;
		this.persistence = persistence;
		this.visibility = visibility;
		this.width = (int)(2*visibility/triadSize)+1;
		this.points = new MJ3DPoint3D[width*width];
		this.pointBuffer = new MJ3DPoint3D[width*width];
		this.pointsMatrix = new MJ3DPoint3D[width][width];
		this.pointsMatrixBuffer = new MJ3DPoint3D[width][width];
		int triadCount = (width-1)*(width-1)*2;
		this.visibleTriads = new MJ3DTriad[triadCount];
		this.visibleTriadBuffer = new MJ3DTriad[triadCount];
		this.pnGen = new PerlinNoiseGenerator(this.seed, 3, 1f);
	}

	@Override
	public void create(){
		create(this.lastViewingPosition);
		createTriads();
	}
	
	public void update(MJ3DViewingPosition newPosition){
		create(newPosition);
		createTriads();
	}
	
	public void create(MJ3DViewingPosition position){
		maxZ = Float.MIN_VALUE;
		float xMin = position.getXPos() - visibility;
		float yMin = position.getYPos() - visibility;
		for(int xIndex = 0; xIndex < width; xIndex++){
			for(int yIndex = 0; yIndex < width; yIndex++){
				float x = xMin + xIndex * triadSize;
				float y = yMin + yIndex * triadSize;
				float z = (pnGen.perlinNoise2D(x, y, 10, persistence, baseFrequency, baseAmplitude));
				MJ3DPoint3D pt = new MJ3DPoint3D(x, y, z);
				pt.setTerrainPointPosition(this, xIndex, yIndex);
				pointsMatrix[xIndex][yIndex] = pt;
				points[xIndex + yIndex*width] = pt;
				pt.setMapIndex(xIndex + yIndex*width);
				maxZ = Math.max(maxZ, z);
			}
		}
	}
	

	private void createTriads() {
		int triadIndex = 0;
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
				this.visibleTriads[triadIndex++]=newTriad1;
				this.visibleTriads[triadIndex++]=newTriad2;
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
	
	@Override
	public List<MJ3DTriad> getTriads(MJ3DViewingPosition viewingPosition) {
		return Arrays.asList(visibleTriads);
	}
	
	@Override
	public List<MJ3DPoint3D> getPoints(MJ3DViewingPosition viewingPosition) {
		return Arrays.asList(this.points);
	}
	
	public MJ3DTriad[] getTriadsArray(MJ3DViewingPosition viewingPosition) {
		return visibleTriads;
	}
	
	public MJ3DPoint3D[] getPointsArray(MJ3DViewingPosition viewingPosition) {
		return this.points;
	}

	@Override
	public int getPointsCount(MJ3DViewingPosition viewingPosition) {
		return points.length;
	}

	@Override
	// TODO Untested!
	public void replace(MJ3DPoint3D pointToReplace, MJ3DPoint3D replacement) {
		throw new UnsupportedOperationException("Replacement is not supported in dynamic map! (And should not be needed either!)");
	}
}
