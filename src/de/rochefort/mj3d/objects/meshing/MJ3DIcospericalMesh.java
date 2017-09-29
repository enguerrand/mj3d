package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.math.Defines;
import de.rochefort.mj3d.math.MJ3DSphere;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.FractalNoiseGenerator;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.Color;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created by edr on 6/15/16.
 */
public class MJ3DIcospericalMesh {
    public final static int RECURSION_DEPTH_INVISIBLE = -1;
    private final MJ3DSphere baseShape;
    private final FractalNoiseGenerator noiseGen;
    private final float radius;
    private float edgeLength;
    private final LodMachine lodMachine;

    public MJ3DIcospericalMesh(float radius, MJ3DPoint3D center, FractalNoiseGenerator noiseGen) {
        this.noiseGen = noiseGen;
        this.radius = radius;
        this.baseShape = new MJ3DSphere(center, radius);
        this.edgeLength = radius / (float) Math.sin(Defines.PI_DOUBLED / 5f);

        final Consumer<MJ3DPoint3D> noiseOffsetGenerator = point ->
            this.baseShape.offsetFromSurface(point, this.noiseGen.fractalNoise3D(point.getX(), point.getY(), point.getZ()));

        BiFunction<MJ3DPoint3D, MJ3DPoint3D, MJ3DPoint3D> midPointBuilder = (point1, point2) ->
            this.baseShape.buildMidPoint(point1, point2);
        this.lodMachine = new LodMachine(noiseOffsetGenerator, midPointBuilder);

        initializePoints(center);
        initializeMesh();
    }

    private void addTriad(int index1, int index2, int index3){
        final int[] pts = {index1, index2, index3};
        final LodTriad triad = new LodTriad(pts, null);
        lodMachine.addTriad(triad);
    }


    public float getEdgeLength() {
        return edgeLength;
    }

    private void initializePoints(MJ3DPoint3D center){
        // equations from https://en.wikipedia.org/wiki/Regular_icosahedron#Cartesian_coordinates
        float cx = center.getX();
        float cy = center.getY();
        float cz = center.getZ();
        float halfEdgeLength = 0.5f * edgeLength;
        float phi = halfEdgeLength * (1.0f + (float)Math.sqrt(5.0)) / 2.0f;
        int refinementLevel = 0;

        lodMachine.addPoint(new MJ3DPoint3D(cx-halfEdgeLength, cy+phi,            cz+0), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx+halfEdgeLength, cy+phi,            cz+0), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx-halfEdgeLength, cy-phi,            cz+0), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx+halfEdgeLength, cy-phi,            cz+0), refinementLevel);

        lodMachine.addPoint(new MJ3DPoint3D(cx+0,              cy-halfEdgeLength, cz+phi), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx+0,              cy+halfEdgeLength, cz+phi), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx+0,              cy-halfEdgeLength, cz-phi), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx+0,              cy+halfEdgeLength, cz-phi), refinementLevel);

        lodMachine.addPoint(new MJ3DPoint3D(cx+phi,            cy,                cz-halfEdgeLength), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx+phi,            cy,                cz+halfEdgeLength), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx-phi,            cy,                cz-halfEdgeLength), refinementLevel);
        lodMachine.addPoint(new MJ3DPoint3D(cx-phi,            cy,                cz+halfEdgeLength), refinementLevel);
    }

    public MJ3DTriad[] buildTriads(Color triadColor, boolean reverseSurfaceNormal, float ambientLight, MJ3DVector vectorOfLight){
        float illuminationFactor = (1f - ambientLight) *0.5f;
        int triadCount = lodMachine.getTriadCount();
        MJ3DTriad[] triads = new MJ3DTriad[triadCount];
        final int size = triadCount;
        for(int triadIndex = 0; triadIndex < size; triadIndex++){
            MJ3DPoint3D[] pts = new MJ3DPoint3D[3];
            for (int i=0; i<3; i++) {
                pts[i] = lodMachine.getPointAtIndex(lodMachine.getTriadAtIndex(triadIndex).pts[i]);
            }
            triads[triadIndex] = new MJ3DTriad(pts, triadColor, reverseSurfaceNormal, ambientLight, illuminationFactor, vectorOfLight);
        }
        return triads;

    }

    public MJ3DPoint3D[] getPoints(){
       return lodMachine.getPoints();
    }


    private void initializeMesh(){
        addTriad(0, 11, 5);
        addTriad(0, 5, 1);
        addTriad(0, 1, 7);
        addTriad(0, 7, 10);
        addTriad(0, 10, 11);
        addTriad(1, 5, 9);
        addTriad(5, 11, 4);
        addTriad(11, 10, 2);
        addTriad(10, 7, 6);
        addTriad(7, 1, 8);
        addTriad(3, 9, 4);
        addTriad(3, 4, 2);
        addTriad(3, 2, 6);
        addTriad(3, 6, 8);
        addTriad(3, 8, 9);
        addTriad(4, 9, 5);
        addTriad(2, 4, 11);
        addTriad(6, 2, 10);
        addTriad(8, 6, 7);
        addTriad(9, 8, 1);
    }

    /**
     * Refines the whole mesh to the desired recursion depth.
     *
     * @param pointRecursionDepths recursion depth array with the respective depth at the point's index in the map
     */
    public void adjustMesh(int[] pointRecursionDepths){
        lodMachine.adjustMesh(pointRecursionDepths);
    }

    public float getDistanceToHorizon(MJ3DViewingPosition viewingPosition) {
        return ((float)Math.sqrt(this.baseShape.getDistanceToHorizonSquared(viewingPosition))) + edgeLength ;
    }
}
