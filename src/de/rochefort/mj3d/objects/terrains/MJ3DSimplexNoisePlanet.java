package de.rochefort.mj3d.objects.terrains;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

import de.rochefort.mj3d.math.Defines;
import de.rochefort.mj3d.math.MJ3DSphere;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.FractalNoiseConfig;
import de.rochefort.mj3d.math.randomness.FractalNoiseGenerator;
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
	private MJ3DTriad[] visibleTriadBuffer;
	private MJ3DPoint3D[] points;
	private MJ3DPoint3D[] pointBuffer;
	private MJ3DPoint3D[][] pointsMatrix;
	private MJ3DPoint3D[][] pointsMatrixBuffer;
	private MJ3DViewingPosition viewingPosition;
	private final FractalNoiseGenerator noiseGen;
	private final int width;
	private final float visibility;
	private final float triadSize;
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
		this.visibility = visibility;
		this.width = (int)(2*visibility/triadSize)+1;
		this.noiseGen = new FractalNoiseGenerator(this.seed, fractalNoiseConfig);
	}

	@Override
	public void create(){
		List<MJ3DPoint3D> pointsList = new LinkedList<MJ3DPoint3D>();
		List<MJ3DTriad> triadList = new LinkedList<MJ3DTriad>();
		float deltaAngle = planetBaseShape.getAngle(triadSize);
		
		for (float latitudeRad = -Defines.PI*.5f; latitudeRad < Defines.PI * 0.5f; latitudeRad += deltaAngle) {
			pointsList.add(planetBaseShape.getPoint(latitudeRad, 0));
			pointsList.add(planetBaseShape.getPoint(latitudeRad + deltaAngle, 0));
			for (float longitude = deltaAngle; longitude <= 2 * Defines.PI; longitude += deltaAngle) {
				pointsList.add(planetBaseShape.getPoint(latitudeRad, longitude));
				pointsList.add(planetBaseShape.getPoint(latitudeRad + deltaAngle, longitude));

				MJ3DPoint3D[] pts = new MJ3DPoint3D[3];
				int tmpIndex = 0;
				for (int i = pointsList.size() - 3; i < pointsList.size(); i++) {
					pts[tmpIndex++] = pointsList.get(i);
				}
				triadList.add(new MJ3DTriad(pts, Color.RED));
			} 
		}
		this.points = new MJ3DPoint3D[pointsList.size()];		
		this.visibleTriads = new MJ3DTriad[triadList.size()];
		
		for(int tmpPtIndex = 0; tmpPtIndex < this.points.length; tmpPtIndex++){
			points[tmpPtIndex] = pointsList.get(tmpPtIndex);
			points[tmpPtIndex].setMapIndex(tmpPtIndex);
		}

		float illuminationFactor = (1f - ambientLight) *0.5f;
		for(int tmpTriadIndex = 0; tmpTriadIndex < this.visibleTriads.length; tmpTriadIndex++){
			visibleTriads[tmpTriadIndex] = triadList.get(tmpTriadIndex);
			visibleTriads[tmpTriadIndex].updateSurfaceNormal();
			float lighting = ambientLight - illuminationFactor * (MJ3DVector.dotProduct(vectorOfLight, visibleTriads[tmpTriadIndex].getNormal())-1);
			visibleTriads[tmpTriadIndex].setColor(ColorBlender.scaleColor(colorShade, lighting));
		}
	}
	
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
