package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.math.Defines;
import de.rochefort.mj3d.math.MJ3DSphere;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.FractalNoiseGenerator;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by edr on 6/15/16.
 */
public class MJ3DIcospericalMesh {
    public final static int RECURSION_DEPTH_INVISIBLE = -1;
    private final MJ3DSphere baseShape;
    private final FractalNoiseGenerator noiseGen;
    private List<Triad> triadList;
    private final float radius;
    private float edgeLength;
    private final PointsContainer pointsContainer;

    public MJ3DIcospericalMesh(float radius, MJ3DPoint3D center, FractalNoiseGenerator noiseGen) {
        this.noiseGen = noiseGen;
        this.pointsContainer = new PointsContainer();
        this.radius = radius;
        this.triadList = new ArrayList<>();
        this.baseShape = new MJ3DSphere(center, radius);
        this.edgeLength = radius / (float) Math.sin(Defines.PI_DOUBLED / 5f);
        initializePoints(radius, center);
        initializeMesh();
    }

    private void addTriad(int index1, int index2, int index3){
        final int[] triad = {index1, index2, index3};
        triadList.add(new Triad(triad, null));
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
                pts[i] = pointsContainer.getPointAtIndex(triadList.get(triadIndex).pts[i]);
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

    /**
     * Refines the whole mesh to the desired recursion depth.
     *
     * @param pointRecursionDepths recursion depth array with the respective depth at the point's index in the map
     */
    public void adjustMesh(int[] pointRecursionDepths){
        List<Triad> triadsTooSmall = splitTriads(pointRecursionDepths);
        mergeTriads(triadsTooSmall);
    }

    public float getDistanceToHorizon(MJ3DViewingPosition viewingPosition) {
        return ((float)Math.sqrt(this.baseShape.getDistanceToHorizonSquared(viewingPosition))) + edgeLength ;
    }

    private List<Triad> splitTriads(int[] pointRecursionDepths) {
        List<Triad> triadsTooSmall = new ArrayList<>();
        List<Triad> triadsToAdd = new ArrayList<>();
        Iterator<Triad> triadsIterator = this.triadList.iterator();
        while(triadsIterator.hasNext()) {
            final Triad next = triadsIterator.next();
            int[] ptIndices = next.pts;
            int[] verticeRecursionDepths = new int[3];
            int actualMaxDepth = 0;
            int minDepth = Integer.MAX_VALUE;
            int maxDepth = 0;
            boolean renderTriad = true;
            for (int verticeIndex = 0; verticeIndex < 3; verticeIndex++) {
                final int ptIndex = ptIndices[verticeIndex];
                final int depth = pointRecursionDepths[ptIndex];
                if(depth == RECURSION_DEPTH_INVISIBLE){
                    renderTriad = false;
                    break;
                }
                actualMaxDepth = Math.max(actualMaxDepth, pointsContainer.getPointRefinementLevel(ptIndex));
                verticeRecursionDepths[verticeIndex] = depth;
                maxDepth = Math.max(maxDepth, depth);
                minDepth = Math.min(minDepth, depth);
            }
            if(!renderTriad){
                triadsTooSmall.add(next);
                continue;
            }
            int deltaRefinementDepth = maxDepth - actualMaxDepth;
            if(deltaRefinementDepth > 0) {
                triadsIterator.remove();
                triadsToAdd.addAll(
                        refineTriad(next, actualMaxDepth+1, deltaRefinementDepth));
            } else if (deltaRefinementDepth < 0) {
                triadsTooSmall.add(next);
            }
        }
        this.triadList.addAll(triadsToAdd);
        return triadsTooSmall;
    }

    private void mergeTriads(List<Triad> triadsTooSmall) {
        Map<Triad, List<Triad>> parentTriadsToRestore = new HashMap<>();
        for (Triad triad : triadsTooSmall) {
            if(triad.parent == null){
                continue;
            }
            parentTriadsToRestore.computeIfAbsent(triad.parent,
                    t -> new ArrayList<>())
                    .add(triad);
        }
        for (Entry<Triad, List<Triad>> triadToRestoreWithChildren : parentTriadsToRestore.entrySet()) {
            final List<Triad> children = triadToRestoreWithChildren.getValue();
            if(children.size() < 4){
                continue;
            } else {
                this.triadList.removeAll(children);
                this.triadList.add(triadToRestoreWithChildren.getKey());
            }
        }
    }

    private List<Triad> refineTriad(Triad parent, int refinementLevel, int recursionCount) {
        if(recursionCount < 1){
            throw new IllegalArgumentException("Recursion count < 1 does not make sense!");
        }
        final Integer np1 = pointsContainer.getOrCreateMidPoint(parent.pts[0], parent.pts[1], refinementLevel);
        final Integer np2 = pointsContainer.getOrCreateMidPoint(parent.pts[1], parent.pts[2], refinementLevel);
        final Integer np3 = pointsContainer.getOrCreateMidPoint(parent.pts[2], parent.pts[0], refinementLevel);

        Triad[] resultingTriads = new Triad[4];
        resultingTriads[0] = new Triad(new int[]{np1,             np2,             np3            }, parent);
        resultingTriads[1] = new Triad(new int[]{parent.pts[0], np1,             np3            }, parent);
        resultingTriads[2] = new Triad(new int[]{np3,             np2,             parent.pts[2]}, parent);
        resultingTriads[3] = new Triad(new int[]{np1,             parent.pts[1], np2            }, parent);

        if(recursionCount == 1){
            return Arrays.asList(resultingTriads);
        } else {
            List<Triad> result = new ArrayList<>();
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
            baseShape.offsetFromSurface(point, noiseGen.fractalNoise3D(point.getX(), point.getY(), point.getZ()));
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

    private static class Triad {
        final int[] pts;
        final Triad parent;

        Triad(int[] pts, Triad parent) {
            this.pts = pts;
            this.parent = parent;
        }
    }
}
