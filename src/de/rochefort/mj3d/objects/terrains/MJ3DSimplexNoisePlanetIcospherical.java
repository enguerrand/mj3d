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
	private float sizeFactor = 3;
	private float seaLevel;
	private float ambientLight;
	private float maxZ = Float.MIN_VALUE;
    private final float desiredRenderedTriadSize;

	public MJ3DSimplexNoisePlanetIcospherical(MJ3DViewingPosition initialViewingPosition, long seed, FractalNoiseConfig fractalNoiseConfig, float seaLevel, float ambientLight, ColorScheme colorScheme, float radius, MJ3DPoint3D center, float desiredRenderedTriadSize) {
		super();
		this.seed = seed;
        this.desiredRenderedTriadSize = desiredRenderedTriadSize;
        this.planetBaseShape = new MJ3DIcospericalMesh(radius, center, 2);
		this.viewingPosition = initialViewingPosition;
		this.seaLevel = seaLevel;
		this.colorShade = colorScheme.getEarthColor();
		this.seaColorShallow = colorScheme.getSeaColorShallow().getRGB();
		this.seaColorDeep = colorScheme.getSeaColorDeep().getRGB();
		this.ambientLight = ambientLight;
		this.noiseGen = new FractalNoiseGenerator(this.seed, fractalNoiseConfig);
	}
	
	@Override
	public void create(){
        this.visibleTriads = this.planetBaseShape.buildTriads(colorShade, false, ambientLight, vectorOfLight);
	}

	@Override
	public void update(){
        int[][] pointRecursionDepths = new int[getPoints().length][2];
        for(int i=0; i<pointRecursionDepths.length; i++) {
            final MJ3DPoint3D mj3DPoint3D = getPoints()[i];
            float distance = mj3DPoint3D.substract(viewingPosition.getPositionVector()).getLength();
            int recursionDepth = computeRecursionDepth(distance);
            //TODO:
            // iterate over all triads and merge triads that are refined to a higher recursion depth than each of their
            //      respective points
            // iterate over all triads and refine them to the maximum recursion depth of their respective points
        }
        this.planetBaseShape.refineMesh(pointRecursionDepths);
	}

    private int computeRecursionDepth(float distance){
        // TODO apply sensible formula to calculate scalingFactor
        int scalingFactor = (int)(this.planetBaseShape.getEdgeLength() / this.desiredRenderedTriadSize)+1;
        int recursion = 0;
        while (1<<recursion < scalingFactor){
            ++recursion;
        }
        return recursion;
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
