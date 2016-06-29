package de.rochefort.mj3d.objects.terrains;

import de.rochefort.mj3d.math.Defines;
import de.rochefort.mj3d.math.FloatInterval;
import de.rochefort.mj3d.math.MJ3DSphere;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.FractalNoiseConfig;
import de.rochefort.mj3d.math.randomness.FractalNoiseGenerator;
import de.rochefort.mj3d.objects.meshing.MJ3DPointsRow;
import de.rochefort.mj3d.objects.meshing.MJ3DPointsRow.WrappingPolicy;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.util.PerformanceTimer;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class MJ3DSimplexNoisePlanetPolar extends MJ3DTerrain {
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
	private float triadSize;
	private float maxTriadSize;
	private float sizeFactor = 3;
	private float seaLevel;
	private float ambientLight;
	private float maxZ = Float.MIN_VALUE;

	public MJ3DSimplexNoisePlanetPolar(MJ3DViewingPosition initialViewingPosition, long seed, float visibility, float triadSize, FractalNoiseConfig fractalNoiseConfig, float seaLevel, float ambientLight, ColorScheme colorScheme, MJ3DSphere planetBaseShape) {
		super();
		this.seed = seed;
		this.planetBaseShape = planetBaseShape;
		this.viewingPosition = initialViewingPosition;
		this.seaLevel = seaLevel;
		this.colorShade = colorScheme.getEarthColor();
		this.seaColorShallow = colorScheme.getSeaColorShallow().getRGB();
		this.seaColorDeep = colorScheme.getSeaColorDeep().getRGB();
		this.ambientLight = ambientLight;
		setTriadSize(triadSize);
		this.visibility = visibility;
		this.noiseGen = new FractalNoiseGenerator(this.seed, fractalNoiseConfig);
	}
	
	@Override
	public void create(){
		PerformanceTimer.stopInterimTime("enter create method");

		float deltaAngle = planetBaseShape.getAngle(triadSize);
		
		List<MJ3DPointsRow> rows = new ArrayList<>();
		float minLat = - Defines.PI * 0.5f;
		float maxLat = Defines.PI * 0.5f;
		for (float latitudeRad = minLat; latitudeRad <= maxLat; latitudeRad = Math.min(latitudeRad+deltaAngle, maxLat)) {
			float circumference = planetBaseShape.getCirumference(latitudeRad);
//			final FloatInterval longitudeInterval = new FloatInterval(0f, Defines.PI_DOUBLED);
			final FloatInterval longitudeInterval = planetBaseShape.getHorizonLongitudeInterval(viewingPosition, latitudeRad);
			final float circumferenceSegment = circumference * (longitudeInterval.getSize()) / Defines.PI_DOUBLED;
			final float lat = latitudeRad;
			Function<Float, MJ3DPoint3D> p = relativeLengthOnRow -> {
//				System.out.println(relativeLengthOnRow);
                MJ3DPoint3D pt;
                float longitude;
                if(circumferenceSegment < Defines.ALMOST_ZERO){
                    longitude = 0f;
                }
                else {
                    longitude = longitudeInterval.getMin() + relativeLengthOnRow * longitudeInterval.getSize();
                }
                pt = planetBaseShape.getPoint(lat, longitude);
                float offset = noiseGen.fractalNoise3D(pt.getX(), pt.getY(), pt.getZ());
                return planetBaseShape.getPoint(lat, longitude, offset);
            };
			MJ3DPointsRow row = new MJ3DPointsRow(triadSize, maxTriadSize, circumferenceSegment, p);
			rows.add(row);
			if(latitudeRad == maxLat){
				break;
			}
		}
		
		PerformanceTimer.stopInterimTime("build rows");
		
		this.points = MJ3DPointsRow.getPoints(rows);
		PerformanceTimer.stopInterimTime("get points from rows");
		for(int tmpPtIndex = 0; tmpPtIndex < this.points.length; tmpPtIndex++){
			points[tmpPtIndex].setMapIndex(tmpPtIndex);
		}
		PerformanceTimer.stopInterimTime("set points map indices");
		
		this.visibleTriads = MJ3DPointsRow.getTriads(rows, WrappingPolicy.ALWAYS, colorShade, ambientLight, vectorOfLight);
		PerformanceTimer.stopInterimTime("Mesh triads from point rows");
	}

	
	// Test case for plane
//	@Override
//	public void create(){
//		float delta = 50;
//
//		List<MJ3DPointsRow> rows = new ArrayList<>();
//		for (int i=0; i<10; i++) {
//			float x = -6000f + delta * i;
//			Function<Float, MJ3DPoint3D> p = relativeLengthOnRow -> {
////					System.out.println(relativeLengthOnRow);
//					return new MJ3DPoint3D(x, relativeLengthOnRow*10*delta, 0f);
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
	
	private void setTriadSize(float newSize){
		this.triadSize = newSize;
		this.maxTriadSize = newSize * sizeFactor;
	}
	
	@Override
	public void update(MJ3DViewingPosition viewingPosition, float cameraFocalDistance){
		PerformanceTimer.stopInterimTime("enter update method");
		float distanceToSurface = this.planetBaseShape.getDistanceToSurface(this.viewingPosition);
//		System.out.println("Distance to surface: "+distanceToSurface);
		float newTriadSize;
		if(distanceToSurface > 5000)
			newTriadSize = distanceToSurface / 10f;
		else if(distanceToSurface > 2500)
			newTriadSize = 250f;
		else if(distanceToSurface > 1000)
			newTriadSize = 100f;
		else 
			newTriadSize = 50f;
		setTriadSize(newTriadSize);
		PerformanceTimer.stopInterimTime("calculate and set new triad size");
		create();
		PerformanceTimer.stopInterimTime("exit create method");
//		System.out.println("====");
//		System.out.println("Own Pos: "+this.viewingPosition.getPositionVector());
//		LongLatPosition llPos = planetBaseShape.getLongLatPosition(this.viewingPosition.getPositionVector());
//		System.out.println("Long lat: "+llPos);
//		System.out.println("Verification pos: "+planetBaseShape.getPoint(llPos.getLatitude(), llPos.getLongitude()));
		
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
