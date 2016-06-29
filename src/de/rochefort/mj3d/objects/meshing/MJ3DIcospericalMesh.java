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
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by edr on 6/15/16.
 */
public class MJ3DIcospericalMesh {
    private final MJ3DSphere baseShape;
    private List<int[]> triadList;
    private final float radius;
    private float edgeLength;
    private final PointsContainer pointsContainer;

    public MJ3DIcospericalMesh(float radius, MJ3DPoint3D center, int initialRecursionCount) {
        this.pointsContainer = new PointsContainer();
        this.radius = radius;
        this.triadList = new ArrayList<>();
        this.baseShape = new MJ3DSphere(center, radius);
        this.edgeLength = radius / (float) Math.sin(Defines.PI_DOUBLED / 5f);
        initializePoints(radius, center);
        initializeMesh();
//        for (int recursionLevel = 1; recursionLevel <= initialRecursionCount; recursionLevel++) {
//            refineMesh(recursionLevel);
//        }
    }

    private void addTriad(int index1, int index2, int index3){
        final int[] triad = {index1, index2, index3};
        triadList.add(triad);
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
        int refinementLevel = 0;

        pointsContainer.addPoint(new MJ3DPoint3D(cx-halfEdgeLength, cy+phi,            cz+0), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx+halfEdgeLength, cy+phi,            cz+0), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx-halfEdgeLength, cy-phi,            cz+0), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx+halfEdgeLength, cy-phi,            cz+0), refinementLevel);

        pointsContainer.addPoint(new MJ3DPoint3D(cx+0,              cy-halfEdgeLength, cz+phi), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx+0,              cy+halfEdgeLength, cz+phi), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx+0,              cy-halfEdgeLength, cz-phi), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx+0,              cy+halfEdgeLength, cz-phi), refinementLevel);

        pointsContainer.addPoint(new MJ3DPoint3D(cx+phi,            cy,                cz-halfEdgeLength), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx+phi,            cy,                cz+halfEdgeLength), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx-phi,            cy,                cz-halfEdgeLength), refinementLevel);
        pointsContainer.addPoint(new MJ3DPoint3D(cx-phi,            cy,                cz+halfEdgeLength), refinementLevel);
    }

    public MJ3DTriad[] buildTriads(Color triadColor, boolean reverseSurfaceNormal, float ambientLight, MJ3DVector vectorOfLight){
        float illuminationFactor = (1f - ambientLight) *0.5f;
        MJ3DTriad[] triads = new MJ3DTriad[triadList.size()];
        final int size = this.triadList.size();
        for(int triadIndex = 0; triadIndex < size; triadIndex++){
            MJ3DPoint3D[] pts = new MJ3DPoint3D[3];
            for (int i=0; i<3; i++) {
                pts[i] = pointsContainer.getPointAtIndex(triadList.get(triadIndex)[i]);
            }
            triads[triadIndex] = new MJ3DTriad(pts, triadColor, reverseSurfaceNormal, ambientLight, illuminationFactor, vectorOfLight);
        }
        return triads;

    }

    public MJ3DPoint3D[] getPoints(){
       return pointsContainer.getPoints();
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
//
//    private void refineMesh(int refinementLevel){
//        List<int[]> newTriads = new ArrayList<>();
//        for(int[] triad : triadList){
//            newTriads.addAll(refineTriad(triad, refinementLevel, 1));
//        }
//        triadList = newTriads;
//    }

    /**
     * Refines the whole mesh to the desired recursion depth.
     *
     * @param pointRecursionDepths recursion depth array with the respective depth at the point's index in the map
     */
    public void refineMesh(int[] pointRecursionDepths){
        List<int[]> triadsToAdd = new ArrayList<>();
        Iterator<int[]> triadsIterator = this.triadList.iterator();
        while(triadsIterator.hasNext()) {
            int[] ptIndices = triadsIterator.next();
            int[] verticeRecursionDepths = new int[3];
            int actualMaxDepth = 0;
            int minDepth = Integer.MAX_VALUE;
            int maxDepth = 0;
            for (int verticeIndex = 0; verticeIndex < 3; verticeIndex++) {
                final int ptIndex = ptIndices[verticeIndex];
                actualMaxDepth = Math.max(actualMaxDepth, pointsContainer.getPointRefinementLevel(ptIndex));
                final int depth = pointRecursionDepths[ptIndex];
                verticeRecursionDepths[verticeIndex] = depth;
                maxDepth = Math.max(maxDepth, depth);
                minDepth = Math.min(minDepth, depth);
            }
            int deltaRefinementDepth = Math.max(0, maxDepth - actualMaxDepth);
            if(deltaRefinementDepth > 0) {
                triadsIterator.remove();
                triadsToAdd.addAll(
                        refineTriad(ptIndices, actualMaxDepth+1, deltaRefinementDepth));
            }
        }
        this.triadList.addAll(triadsToAdd);
    }

    private List<int[]> refineTriad(int[] triad, int refinementLevel, int recursionCount) {
        if(recursionCount < 1){
            throw new IllegalArgumentException("Recursion count < 1 does not make sense!");
        }
        final Integer np1 = pointsContainer.getOrCreateMidPoint(triad[0], triad[1], refinementLevel);
        final Integer np2 = pointsContainer.getOrCreateMidPoint(triad[1], triad[2], refinementLevel);
        final Integer np3 = pointsContainer.getOrCreateMidPoint(triad[2], triad[0], refinementLevel);

        int[][] resultingTriads = new int[4][3];
        resultingTriads[0] = new int[]{np1,        np2,        np3      };
        resultingTriads[1] = new int[]{triad[0],   np1,        np3      };
        resultingTriads[2] = new int[]{np3,        np2,        triad[2] };
        resultingTriads[3] = new int[]{np1,        triad[1],   np2      };

        if(recursionCount == 1){
            return Arrays.asList(resultingTriads);
        } else {
            List<int[]> result = new ArrayList<>();
            recursionCount--;
            refinementLevel++;
            for (int t = 0; t<4; t++){
                result.addAll(refineTriad(resultingTriads[t], refinementLevel, recursionCount));
            }
            return result;
        }

    }

    private final class PointsContainer {
        private final List<MJ3DPoint3D> points;
        private final List<Integer> pointRefinementLevels;
        private final Map<Long, Integer> pointIndexLoopupTable;

        PointsContainer() {
            this.points = new ArrayList<>();
            this.pointRefinementLevels = new ArrayList<>();
            this.pointIndexLoopupTable = new HashMap<>();
        }

        int addPoint(MJ3DPoint3D point, int refinementLevel){
            final int index = points.size();
            this.points.add(point);
            this.pointRefinementLevels.add(refinementLevel);
            point.setMapIndex(index);
            return index;
        }

        void removePoint(int index){
            this.points.remove(index);
            this.pointRefinementLevels.remove(index);
        }

        private Integer getOrCreateMidPoint(int indexPointA, int indexPointB, int refinementLevel){
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
                midPointIndex = addPoint(midPoint, refinementLevel);
                pointIndexLoopupTable.put(midPointIndexKey, midPointIndex);
            }
            return midPointIndex;
        }

        MJ3DPoint3D getPointAtIndex(int index){
            return points.get(index);
        }

        int getPointRefinementLevel(int index){
            return pointRefinementLevels.get(index);
        }

        public MJ3DPoint3D[] getPoints(){
            MJ3DPoint3D[] arr = new MJ3DPoint3D[this.points.size()];
            return this.points.toArray(arr);
        }
    }
}
