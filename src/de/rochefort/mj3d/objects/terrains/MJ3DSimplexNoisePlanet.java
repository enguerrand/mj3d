package de.rochefort.mj3d.objects.terrains;

import java.awt.Color;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import de.rochefort.mj3d.math.Defines;
import de.rochefort.mj3d.math.MJ3DSphere;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.FractalNoiseConfig;
import de.rochefort.mj3d.math.randomness.FractalNoiseGenerator;
import de.rochefort.mj3d.objects.meshing.MJ3DPointsRow;
import de.rochefort.mj3d.objects.meshing.PointsProducer;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.view.ColorBlender;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public class MJ3DSimplexNoisePlanet extends MJ3DTerrain {
	private final long seed;
	private final MJ3DSphere planetBaseShape;
	private final Color colorShade;
	private final int seaColorShallow;
	private final int seaColorDeep;
	private MJ3DVector vectorOfLight = MJ3DVector.Y_UNIT_VECTOR;
	private MJ3DTriad[] visibleTriads;
	private MJ3DPoint3D[] points;
	private MJ3DViewingPosition viewingPosition;
	private final FractalNoiseGenerator noiseGen;
	private final float visibility;
	private final float triadSize;
	private final float maxTriadSize;
	private float seaLevel;
	private float ambientLight;
	private float maxZ = Float.MIN_VALUE;

	public MJ3DSimplexNoisePlanet(MJ3DViewingPosition initialViewingPosition, long seed, float visibility, float triadSize, FractalNoiseConfig fractalNoiseConfig, float seaLevel, float ambientLight, ColorScheme colorScheme, MJ3DSphere planetBaseShape) {
		super();
		this.seed = seed;
		this.planetBaseShape = planetBaseShape;
		this.viewingPosition = initialViewingPosition;
		this.seaLevel = seaLevel;
		this.colorShade = colorScheme.getEarthColor();
		this.seaColorShallow = colorScheme.getSeaColorShallow().getRGB();
		this.seaColorDeep = colorScheme.getSeaColorDeep().getRGB();
		this.ambientLight = ambientLight;
		this.triadSize = triadSize;
		this.maxTriadSize = this.triadSize * 3;
		this.visibility = visibility;
		this.noiseGen = new FractalNoiseGenerator(this.seed, fractalNoiseConfig);
	}
	
	@Override
	public void create(){
		float deltaAngle = planetBaseShape.getAngle(triadSize);
		float maxDeltaAngle = planetBaseShape.getAngle(maxTriadSize);
		
		List<MJ3DPointsRow> rows = new ArrayList<>();
		float minLat = - Defines.PI * 0.5f;
		float maxLat = Defines.PI * 0.5f;
		for (float latitudeRad = minLat; latitudeRad <= maxLat; latitudeRad = Math.min(latitudeRad+deltaAngle, maxLat)) {
			float circumference = planetBaseShape.getCirumference(latitudeRad);
			final float lat = latitudeRad;
			PointsProducer p = new PointsProducer() {
				@Override
				public MJ3DPoint3D create(float relativeLengthOnRow) {
//					System.out.println(relativeLengthOnRow);
					MJ3DPoint3D pt;
					float longitude;
					if(circumference < Defines.ALMOST_ZERO){
						longitude = 0f;
					}
					else {
						longitude = relativeLengthOnRow * (2 * Defines.PI - maxDeltaAngle);
					}
					pt = planetBaseShape.getPoint(lat, longitude);
					float offset = noiseGen.fractalNoise3D(pt.getX(), pt.getY(), pt.getZ());
					return planetBaseShape.getPoint(lat, longitude, offset);
				}
			};
			MJ3DPointsRow row = new MJ3DPointsRow(triadSize, maxTriadSize, circumference-maxTriadSize, p);
			rows.add(row);
			if(latitudeRad == maxLat){
				break;
			}
		}
		
		this.points = MJ3DPointsRow.getPoints(rows);
		for(int tmpPtIndex = 0; tmpPtIndex < this.points.length; tmpPtIndex++){
			points[tmpPtIndex].setMapIndex(tmpPtIndex);
		}
		
		this.visibleTriads = MJ3DPointsRow.getTriads(rows, true, colorShade, ambientLight, vectorOfLight);
	}

	
	// Test case for plane
//	@Override
//	public void create(){
//		float delta = 50;
//		
//		List<MJ3DPointsRow> rows = new ArrayList<>();
//		for (int i=0; i<10; i++) {
//			float x = -6000f + delta * i;
//			PointsProducer p = new PointsProducer() {
//				@Override
//				public MJ3DPoint3D create(float relativeLengthOnRow) {
////					System.out.println(relativeLengthOnRow);
//					return new MJ3DPoint3D(x, relativeLengthOnRow*10*delta, 0f);
//				}
//			};
//			MJ3DPointsRow row = new MJ3DPointsRow(delta, delta*1.1f, 10*delta, p);
//			rows.add(row);
//		}
//		
//		this.points = MJ3DPointsRow.getPoints(rows);
//		for(int tmpPtIndex = 0; tmpPtIndex < this.points.length; tmpPtIndex++){
//			points[tmpPtIndex].setMapIndex(tmpPtIndex);
//		}
//		
//		this.visibleTriads = MJ3DPointsRow.getTriads(rows, true, colorShade, ambientLight, vectorOfLight);
//	}
	
	@Override
	public void update(){
		create();
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
	public void replace(MJ3DPoint3D pointToReplace, MJ3DPoint3D replacement) {
		throw new UnsupportedOperationException("Replacement is not supported in dynamic map! (And should not be needed either!)");
	}
}
