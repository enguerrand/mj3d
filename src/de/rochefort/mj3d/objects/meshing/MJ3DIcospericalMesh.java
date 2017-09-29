package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.math.Defines;
import de.rochefort.mj3d.math.MJ3DSphere;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.FractalNoiseGenerator;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.*;
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
    private final float radius;
    private float edgeLength;
    private final TriadContainer triadsContainer;
    private final PointsContainer pointsContainer;

    public MJ3DIcospericalMesh(float radius, MJ3DPoint3D center, FractalNoiseGenerator noiseGen) {
        this.noiseGen = noiseGen;
        this.pointsContainer = new PointsContainer();
        this.triadsContainer = new TriadContainer(pointsContainer);
        this.radius = radius;
        this.baseShape = new MJ3DSphere(center, radius);
        this.edgeLength = radius / (float) Math.sin(Defines.PI_DOUBLED / 5f);
        initializePoints(center);
        initializeMesh();
    }

    private void addTriad(int index1, int index2, int index3){
        final int[] pts = {index1, index2, index3};
        final Triad triad = new Triad(pts, null);
        triadsContainer.addTriad(triad);
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
        int triadCount = triadsContainer.getTriadCount();
        MJ3DTriad[] triads = new MJ3DTriad[triadCount];
        final int size = triadCount;
        for(int triadIndex = 0; triadIndex < size; triadIndex++){
            MJ3DPoint3D[] pts = new MJ3DPoint3D[3];
            for (int i=0; i<3; i++) {
                pts[i] = pointsContainer.getPointAtIndex(triadsContainer.getTriadAtIndex(triadIndex).pts[i]);
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
        fixTransitions();
    }

    private void fixTransitions() {
        List<Triad> triadsToAdd = new ArrayList<>();
        Iterator<Triad> triadsIterator = this.triadsContainer.iterator();
        while(triadsIterator.hasNext()) {
            final Triad triad = triadsIterator.next();
            Integer midPoint1 = pointsContainer.getMidPointIfInUse(triad.pts[0], triad.pts[1]);
            Integer midPoint2 = pointsContainer.getMidPointIfInUse(triad.pts[1], triad.pts[2]);
            Integer midPoint3 = pointsContainer.getMidPointIfInUse(triad.pts[2], triad.pts[0]);
            final boolean split1 = midPoint1 != null;
            final boolean split2 = midPoint2 != null;
            final boolean split3 = midPoint3 != null;
            if((split1 && split2) || (split1 && split3) || (split2 && split3)) {
                throw new IllegalStateException("can only split at one edge!");
            } else if (split1) {
                pointsContainer.unregisterTriad(triad);
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[0], midPoint1, triad.pts[2]};
                int[] newPts2 = {midPoint1, triad.pts[1],triad.pts[2]};
                triadsToAdd.add(new Triad(newPts1, triad));
                triadsToAdd.add(new Triad(newPts2, triad));
            } else if (split2) {
                pointsContainer.unregisterTriad(triad);
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[1], midPoint2, triad.pts[0]};
                int[] newPts2 = {midPoint2, triad.pts[2],triad.pts[0]};
                triadsToAdd.add(new Triad(newPts1, triad));
                triadsToAdd.add(new Triad(newPts2, triad));
            } else if (split3) {
                pointsContainer.unregisterTriad(triad);
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[2], midPoint3, triad.pts[1]};
                int[] newPts2 = {midPoint3, triad.pts[0],triad.pts[1]};
                triadsToAdd.add(new Triad(newPts1, triad));
                triadsToAdd.add(new Triad(newPts2, triad));
            }
        }
        for (Triad triad : triadsToAdd) {
            triadsContainer.addTriad(triad);
        }
    }

    public float getDistanceToHorizon(MJ3DViewingPosition viewingPosition) {
        return ((float)Math.sqrt(this.baseShape.getDistanceToHorizonSquared(viewingPosition))) + edgeLength ;
    }


    private List<Triad> splitTriads(int[] pointRecursionDepths) {
        List<Triad> triadsTooSmall = new ArrayList<>();
        List<Triad> triadsToAdd = new ArrayList<>();
        Iterator<Triad> triadsIterator = this.triadsContainer.iterator();
        while(triadsIterator.hasNext()) {
            final Triad nextTriad = triadsIterator.next();
            int[] verticeRecursionDepths = new int[3];
            int actualMaxDepth = 0;
            int minDepth = Integer.MAX_VALUE;
            int maxDepth = 0;
            boolean renderTriad = true;
            for (int verticeIndex = 0; verticeIndex < 3; verticeIndex++) {
                final int ptIndex = nextTriad.pts[verticeIndex];
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
                triadsTooSmall.add(nextTriad);
                continue;
            }
            int deltaRefinementDepth = maxDepth - actualMaxDepth;
            if(deltaRefinementDepth > 0) {
                pointsContainer.unregisterTriad(nextTriad);
                triadsIterator.remove();
                triadsToAdd.addAll(
                        refineTriad(nextTriad, actualMaxDepth+1, deltaRefinementDepth));
            } else if (deltaRefinementDepth < 0) {
                triadsTooSmall.add(nextTriad);
            }
        }
        for (Triad triad : triadsToAdd) {
            triadsContainer.addTriad(triad);
        }
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
                for (Triad child : children) {
                    triadsContainer.removeTriad(child);
                }
                triadsContainer.addTriad(triadToRestoreWithChildren.getKey());
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

    private final class TriadContainer {
        private final PointsContainer pointsContainer;
        private List<Triad> triadList;
        private TriadContainer(PointsContainer pointsContainer) {
            this.pointsContainer = pointsContainer;
            this.triadList = new ArrayList<>();
        }

        int getTriadCount(){
            return triadList.size();
        }

        Triad getTriadAtIndex(int index){
            return triadList.get(index);
        }

        private void addTriad(Triad triad) {
            this.pointsContainer.registerTriad(triad);
            this.triadList.add(triad);
        }

        Iterator<Triad> iterator(){
            return new Iterator<Triad>() {
                Iterator<Triad> internalIterator = triadList.iterator();
                Triad current = null;
                @Override
                public boolean hasNext() {
                    return internalIterator.hasNext();
                }

                @Override
                public Triad next() {
                    current = internalIterator.next();
                    return current;
                }

                @Override
                public void remove() {
                    pointsContainer.unregisterTriad(current);
                    internalIterator.remove();
                }
            };
        }

        void removeTriad(Triad triad){
            pointsContainer.unregisterTriad(triad);
            triadList.remove(triad);
        }
    }

    private final class PointsContainer {
        private final List<MJ3DPoint3D> points;
        private final List<Integer> pointRefinementLevels;
        private final List<List<Triad>> triadsUsingPoints;
        private final Map<Long, Integer> pointIndexLookupTable;

        PointsContainer() {
            this.points = new ArrayList<>();
            this.pointRefinementLevels = new ArrayList<>();
            this.triadsUsingPoints = new ArrayList<>();
            this.pointIndexLookupTable = new HashMap<>();
        }

        void registerTriad(Triad triad){
            for (int pt : triad.pts) {
                List<Triad> triads = triadsUsingPoints.get(pt);
                triads.add(triad);
            }
        }

        void unregisterTriad(Triad triad){
            for (int pt : triad.pts) {
                List<Triad> triads = triadsUsingPoints.get(pt);
                triads.remove(triad);
            }
        }

        int addPoint(MJ3DPoint3D point, int refinementLevel){
            final int index = points.size();
            baseShape.offsetFromSurface(point, noiseGen.fractalNoise3D(point.getX(), point.getY(), point.getZ()));
            this.points.add(point);
            this.pointRefinementLevels.add(refinementLevel);
            this.triadsUsingPoints.add(new ArrayList<>());
            point.setMapIndex(index);
            return index;
        }

        void removePoint(int index){
            this.points.remove(index);
            this.pointRefinementLevels.remove(index);
            this.triadsUsingPoints.remove(index);
        }

        Integer getMidPointIfInUse(int indexPointA, int indexPointB){
            final Integer midPointIndex = getMidPointIfPresent(indexPointA, indexPointB);
            if(midPointIndex == null || triadsUsingPoints.get(midPointIndex).isEmpty()){
                return null;
            }
            return midPointIndex;
        }

        private Integer getMidPointIfPresent(int indexPointA, int indexPointB){
            return pointIndexLookupTable.get(buildMidPointLookupKey(indexPointA, indexPointB));
        }

        private Integer getOrCreateMidPoint(int indexPointA, int indexPointB, int refinementLevel){
            Long midPointIndexKey = buildMidPointLookupKey(indexPointA, indexPointB);
            Integer midPointIndex = pointIndexLookupTable.get(midPointIndexKey);
            final MJ3DPoint3D midPoint;
            if(midPointIndex == null) {
                midPoint = baseShape.buildMidPoint(points.get(indexPointA), points.get(indexPointB));
                midPointIndex = addPoint(midPoint, refinementLevel);
                pointIndexLookupTable.put(midPointIndexKey, midPointIndex);
            }
            return midPointIndex;
        }

        private Long buildMidPointLookupKey(int indexPointA, int indexPointB) {
            int firstIndex;
            int secondIndex;
            if(indexPointB >= indexPointA){
                firstIndex = indexPointA;
                secondIndex = indexPointB;
            } else {
                firstIndex = indexPointB;
                secondIndex = indexPointA;
            }
            return ((long)firstIndex << 32) + secondIndex;
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
