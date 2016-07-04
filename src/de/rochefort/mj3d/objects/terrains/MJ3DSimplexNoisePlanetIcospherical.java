package de.rochefort.mj3d.objects.terrains;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.FractalNoiseConfig;
import de.rochefort.mj3d.math.randomness.FractalNoiseGenerator;
import de.rochefort.mj3d.objects.meshing.MJ3DIcospericalMesh;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.Color;

public class MJ3DSimplexNoisePlanetIcospherical extends MJ3DTerrain {
    private final long seed;
	private final MJ3DIcospericalMesh planetBaseShape;
	private final Color colorShade;
	private final int seaColorShallow;
	private final int seaColorDeep;
	private MJ3DVector vectorOfLight = MJ3DVector.Y_UNIT_VECTOR;
	private MJ3DTriad[] visibleTriads;
	private MJ3DViewingPosition viewingPosition;
	private final FractalNoiseGenerator noiseGen;
    private float seaLevel;
	private float ambientLight;
	private float maxZ = Float.MIN_VALUE;
    private final float desiredRenderedTriadSize;
    private final int maxRecursionDepth;
    private final int minRecursionDepth;

	public MJ3DSimplexNoisePlanetIcospherical(MJ3DViewingPosition initialViewingPosition, long seed, FractalNoiseConfig fractalNoiseConfig,
                                              float seaLevel, float ambientLight, ColorScheme colorScheme, float radius, MJ3DPoint3D center,
                                              int minRecursionDepth, float triadSize, float desiredRenderedTriadSize) {
		super();
		this.seed = seed;
        this.minRecursionDepth = minRecursionDepth;
        this.desiredRenderedTriadSize = desiredRenderedTriadSize;
        this.planetBaseShape = new MJ3DIcospericalMesh(radius, center, 2);
		this.viewingPosition = initialViewingPosition;
		this.seaLevel = seaLevel;
		this.colorShade = colorScheme.getEarthColor();
		this.seaColorShallow = colorScheme.getSeaColorShallow().getRGB();
		this.seaColorDeep = colorScheme.getSeaColorDeep().getRGB();
		this.ambientLight = ambientLight;
		this.noiseGen = new FractalNoiseGenerator(this.seed, fractalNoiseConfig);
        float edgeLength = this.planetBaseShape.getEdgeLength();
        this.maxRecursionDepth = 1 + (int)(Math.log(triadSize/edgeLength) / Math.log(0.5));
	}
	
	@Override
	public void create(){
	}

	@Override
	public void update(MJ3DViewingPosition viewingPosition, float cameraFocalDistance){
        int[] pointRecursionDepths = new int[getPoints().length];
        float maxVisiblePointDistance = this.planetBaseShape.getDistanceToHorizon(viewingPosition);

        for(int i=0; i<pointRecursionDepths.length; i++) {
            final MJ3DPoint3D mj3DPoint3D = getPoints()[i];
            float distance = mj3DPoint3D.substract(viewingPosition.getPositionVector()).getLength();
            if(distance > maxVisiblePointDistance) {
                pointRecursionDepths[i] = MJ3DIcospericalMesh.RECURSION_DEPTH_INVISIBLE;
            } else {
                pointRecursionDepths[i] = computeRecursionDepth(distance, cameraFocalDistance);
            }
        }
        this.planetBaseShape.adjustMesh(pointRecursionDepths);
        this.visibleTriads = this.planetBaseShape.buildTriads(colorShade, false, ambientLight, vectorOfLight);
	}

    private int computeRecursionDepth(float distance, float cameraFocalDistance){
        float renderedTriadSize = this.planetBaseShape.getEdgeLength() * cameraFocalDistance / (distance - cameraFocalDistance);
        int scalingFactor = (int)(renderedTriadSize/ this.desiredRenderedTriadSize)+1;
        int recursion = minRecursionDepth;
        while (1<<recursion < scalingFactor){
            ++recursion;
        }
        return Math.min(this.maxRecursionDepth, recursion);
    }

	@Override
	public MJ3DTriad[] getTriads() {
		return visibleTriads;
	}
	
	@Override
	public MJ3DPoint3D[] getPoints() {
		return this.planetBaseShape.getPoints();
	}

	@Override
	public int getPointsCount() {
		return this.planetBaseShape.getPoints().length;
	}

	@Override
	public void replace(MJ3DPoint3D pointToReplace, MJ3DPoint3D replacement) {
		throw new UnsupportedOperationException("Replacement is not supported in dynamic maps! (And should not be needed either!)");
	}
}
