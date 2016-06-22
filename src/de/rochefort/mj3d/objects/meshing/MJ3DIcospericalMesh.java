package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.math.Defines;
import de.rochefort.mj3d.math.MJ3DSphere;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edr on 6/15/16.
 */
public class MJ3DIcospericalMesh {
    private final MJ3DSphere baseShape;
    private final Map<Long, Integer> pointIndexLoopupTable;
    private List<int[]> triadList;
    private final List<MJ3DPoint3D> points;
    private final float radius;
    private float edgeLength;

    public MJ3DIcospericalMesh(float radius, MJ3DPoint3D center, int initialRecursionCount) {
        this.radius = radius;
        this.pointIndexLoopupTable = new HashMap<>();
        this.triadList = new ArrayList<>();
        this.points = new ArrayList<>();
        this.baseShape = new MJ3DSphere(center, radius);
        this.edgeLength = radius / (float) Math.sin(Defines.PI_DOUBLED / 5f);
        initializePoints(radius, center);
        initializeMesh();
        for (int recursionLevel = 0; recursionLevel < initialRecursionCount; recursionLevel++) {
            refineMesh();
        }
    }

    private void addTriad(int index1, int index2, int index3){
        final int[] triad = {index1, index2, index3};
        triadList.add(triad);
    }

    private int addPoint(MJ3DPoint3D point){
        final int index = points.size();
        this.points.add(point);
        point.setMapIndex(index);
        return index;
    }

    public float getEdgeLength() {
        return edgeLength;
    }

    private void initializePoints(float radius, MJ3DPoint3D center){
        // equations from https://en.wikipedia.org/wiki/Regular_icosahedron#Cartesian_coordinates
        float cx = center.getX();
        float cy = center.getY();
        float cz = center.getZ();
        float halfEdgeLength = 0.5f * edgeLength;
        float phi = halfEdgeLength * (1.0f + (float)Math.sqrt(5.0)) / 2.0f;

        addPoint(new MJ3DPoint3D(cx-halfEdgeLength, cy+phi,            cz+0));
        addPoint(new MJ3DPoint3D(cx+halfEdgeLength, cy+phi,            cz+0));
        addPoint(new MJ3DPoint3D(cx-halfEdgeLength, cy-phi,            cz+0));
        addPoint(new MJ3DPoint3D(cx+halfEdgeLength, cy-phi,            cz+0));

        addPoint(new MJ3DPoint3D(cx+0,              cy-halfEdgeLength, cz+phi));
        addPoint(new MJ3DPoint3D(cx+0,              cy+halfEdgeLength, cz+phi));
        addPoint(new MJ3DPoint3D(cx+0,              cy-halfEdgeLength, cz-phi));
        addPoint(new MJ3DPoint3D(cx+0,              cy+halfEdgeLength, cz-phi));

        addPoint(new MJ3DPoint3D(cx+phi,            cy,                cz-halfEdgeLength));
        addPoint(new MJ3DPoint3D(cx+phi,            cy,                cz+halfEdgeLength));
        addPoint(new MJ3DPoint3D(cx-phi,            cy,                cz-halfEdgeLength));
        addPoint(new MJ3DPoint3D(cx-phi,            cy,                cz+halfEdgeLength));
    }

    private Integer getOrCreateMidPoint(int indexPointA, int indexPointB){
        int firstIndex;
        int secondIndex;
        if(indexPointB >= indexPointA){
            firstIndex = indexPointA;
            secondIndex = indexPointB;
        } else {
            firstIndex = indexPointB;
            secondIndex = indexPointA;
        }
        Long midPointIndexKey = ((long)firstIndex << 32) + secondIndex;
        Integer midPointIndex = pointIndexLoopupTable.get(midPointIndexKey);
        final MJ3DPoint3D midPoint;
        if(midPointIndex != null) {
            midPoint = points.get(pointIndexLoopupTable.get(midPointIndexKey));
        } else {
            midPoint = baseShape.buildMidPoint(points.get(firstIndex), points.get(secondIndex));
            midPointIndex = addPoint(midPoint);
            pointIndexLoopupTable.put(midPointIndexKey, midPointIndex);
        }
        return midPointIndex;
    }

    public MJ3DTriad[] buildTriads(Color triadColor, boolean reverseSurfaceNormal, float ambientLight, MJ3DVector vectorOfLight){
        float illuminationFactor = (1f - ambientLight) *0.5f;
        MJ3DTriad[] triads = new MJ3DTriad[triadList.size()];
        final int size = this.triadList.size();
        for(int index = 0; index < size; index++){
            MJ3DPoint3D[] pts = new MJ3DPoint3D[3];
            for (int i=0; i<3; i++) {
                pts[i] = points.get(triadList.get(index)[i]);
            }
            triads[index] = new MJ3DTriad(pts, triadColor, reverseSurfaceNormal, ambientLight, illuminationFactor, vectorOfLight);
        }
        return triads;

    }

    public MJ3DPoint3D[] getPoints(){
        MJ3DPoint3D[] arr = new MJ3DPoint3D[this.points.size()];
        return this.points.toArray(arr);
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

    public void refineTriad(int triadIndex){

    }

    private void refineMesh(){
        List<int[]> newTriads = new ArrayList<>();
        for(int[] triad : triadList){
            int[][] resultingTriads = refineTriad(triad);
            newTriads.addAll(Arrays.asList(resultingTriads));
        }
        triadList = newTriads;
    }

    /**
     * Refines the whole mesh to the desired recursion depth.
     *
     * @param pointRecursionDepths two dimensional array that has all point indices in the first
     *                             column and the respective recursion depth requested for the point
     *                             in the second column
     */
    public void refineMesh(int[][] pointRecursionDepths){
        for(int row=0; row<pointRecursionDepths.length; row++){
            int pointIndex = pointRecursionDepths[row][0];
            int recursionDepth = pointRecursionDepths[row][1];
            final List<MJ3DTriad> triads = points.get(pointIndex).getTriads();
            // TODO refine triad if needed
            // here?
        }
    }

    private int[][] refineTriad(int[] triad) {
        final Integer np1 = getOrCreateMidPoint(triad[0], triad[1]);
        final Integer np2 = getOrCreateMidPoint(triad[1], triad[2]);
        final Integer np3 = getOrCreateMidPoint(triad[2], triad[0]);
        int[][] resultingTriads = new int[4][3];
        resultingTriads[0] = new int[]{np1,        np2,        np3      };
        resultingTriads[1] = new int[]{triad[0],   np1,        np3      };
        resultingTriads[2] = new int[]{np3,        np2,        triad[2] };
        resultingTriads[3] = new int[]{np1,        triad[1],   np2      };
        return resultingTriads;
    }
}
