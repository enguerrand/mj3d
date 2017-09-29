package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class LodMachine {
    private final static int RECURSION_DEPTH_INVISIBLE = -1;
    private final PointsContainer pointsContainer;
    private List<LodTriad> triadList;
    LodMachine(Consumer<MJ3DPoint3D> noiseOffsetGenerator, BiFunction<MJ3DPoint3D, MJ3DPoint3D, MJ3DPoint3D> midPointBuilder) {
        this.pointsContainer = new PointsContainer(noiseOffsetGenerator, midPointBuilder);
        this.triadList = new ArrayList<>();
    }

    int getTriadCount(){
        return triadList.size();
    }

    LodTriad getTriadAtIndex(int index){
        return triadList.get(index);
    }

    void addPoint(MJ3DPoint3D point, int refinementLevel){
        pointsContainer.addPoint(point, refinementLevel);
    }

    void addTriad(LodTriad triad) {
        this.pointsContainer.registerTriad(triad);
        this.triadList.add(triad);
    }

    Iterator<LodTriad> iterator(){
        return new Iterator<LodTriad>() {
            Iterator<LodTriad> internalIterator = triadList.iterator();
            LodTriad current = null;
            @Override
            public boolean hasNext() {
                return internalIterator.hasNext();
            }

            @Override
            public LodTriad next() {
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

    /**
     * Refines the whole mesh to the desired recursion depth.
     *
     * @param pointRecursionDepths recursion depth array with the respective depth at the point's index in the map
     */
    public void adjustMesh(int[] pointRecursionDepths){
        List<LodTriad> triadsTooSmall = splitTriads(pointRecursionDepths);
        mergeTriads(triadsTooSmall);
        fixTransitions();
    }

    void removeTriad(LodTriad triad){
        pointsContainer.unregisterTriad(triad);
        triadList.remove(triad);
    }

    Integer getOrCreateMidPoint(int pt1, int pt2, int refinementLevel) {
        return pointsContainer.getOrCreateMidPoint(pt1, pt2, refinementLevel);
    }

    MJ3DPoint3D getPointAtIndex(int index) {
        return pointsContainer.getPointAtIndex(index);
    }

    public MJ3DPoint3D[] getPoints() {
        return pointsContainer.getPoints();
    }

    Integer getMidPointIfInUse(int pt1, int pt2) {
        return pointsContainer.getMidPointIfInUse(pt1, pt2);
    }

    int getPointRefinementLevel(int ptIndex) {
        return pointsContainer.getPointRefinementLevel(ptIndex);
    }

    private void fixTransitions() {
        List<LodTriad> triadsToAdd = new ArrayList<>();
        Iterator<LodTriad> triadsIterator = iterator();
        while(triadsIterator.hasNext()) {
            final LodTriad triad = triadsIterator.next();
            Integer midPoint1 = getMidPointIfInUse(triad.pts[0], triad.pts[1]);
            Integer midPoint2 = getMidPointIfInUse(triad.pts[1], triad.pts[2]);
            Integer midPoint3 = getMidPointIfInUse(triad.pts[2], triad.pts[0]);
            final boolean split1 = midPoint1 != null;
            final boolean split2 = midPoint2 != null;
            final boolean split3 = midPoint3 != null;
            if((split1 && split2) || (split1 && split3) || (split2 && split3)) {
                throw new IllegalStateException("can only split at one edge!");
            } else if (split1) {
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[0], midPoint1, triad.pts[2]};
                int[] newPts2 = {midPoint1, triad.pts[1],triad.pts[2]};
                triadsToAdd.add(new LodTriad(newPts1, triad));
                triadsToAdd.add(new LodTriad(newPts2, triad));
            } else if (split2) {
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[1], midPoint2, triad.pts[0]};
                int[] newPts2 = {midPoint2, triad.pts[2],triad.pts[0]};
                triadsToAdd.add(new LodTriad(newPts1, triad));
                triadsToAdd.add(new LodTriad(newPts2, triad));
            } else if (split3) {
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[2], midPoint3, triad.pts[1]};
                int[] newPts2 = {midPoint3, triad.pts[0],triad.pts[1]};
                triadsToAdd.add(new LodTriad(newPts1, triad));
                triadsToAdd.add(new LodTriad(newPts2, triad));
            }
        }
        for (LodTriad triad : triadsToAdd) {
            addTriad(triad);
        }
    }


    private List<LodTriad> splitTriads(int[] pointRecursionDepths) {
        List<LodTriad> triadsTooSmall = new ArrayList<>();
        List<LodTriad> triadsToAdd = new ArrayList<>();
        Iterator<LodTriad> triadsIterator = iterator();
        while(triadsIterator.hasNext()) {
            final LodTriad nextTriad = triadsIterator.next();
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
                actualMaxDepth = Math.max(actualMaxDepth, getPointRefinementLevel(ptIndex));
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
                triadsIterator.remove();
                triadsToAdd.addAll(
                        refineTriad(nextTriad, actualMaxDepth+1, deltaRefinementDepth));
            } else if (deltaRefinementDepth < 0) {
                triadsTooSmall.add(nextTriad);
            }
        }
        for (LodTriad triad : triadsToAdd) {
            addTriad(triad);
        }
        return triadsTooSmall;
    }

    private List<LodTriad> refineTriad(LodTriad parent, int refinementLevel, int recursionCount) {
        if(recursionCount < 1){
            throw new IllegalArgumentException("Recursion count < 1 does not make sense!");
        }
        final Integer np1 = getOrCreateMidPoint(parent.pts[0], parent.pts[1], refinementLevel);
        final Integer np2 = getOrCreateMidPoint(parent.pts[1], parent.pts[2], refinementLevel);
        final Integer np3 = getOrCreateMidPoint(parent.pts[2], parent.pts[0], refinementLevel);

        LodTriad[] resultingTriads = new LodTriad[4];
        resultingTriads[0] = new LodTriad(new int[]{np1,             np2,             np3            }, parent);
        resultingTriads[1] = new LodTriad(new int[]{parent.pts[0], np1,             np3            }, parent);
        resultingTriads[2] = new LodTriad(new int[]{np3,             np2,             parent.pts[2]}, parent);
        resultingTriads[3] = new LodTriad(new int[]{np1,             parent.pts[1], np2            }, parent);

        if(recursionCount == 1){
            return Arrays.asList(resultingTriads);
        } else {
            List<LodTriad> result = new ArrayList<>();
            recursionCount--;
            refinementLevel++;
            for (int t = 0; t<4; t++){
                result.addAll(refineTriad(resultingTriads[t], refinementLevel, recursionCount));
            }
            return result;
        }
    }


    private void mergeTriads(List<LodTriad> triadsTooSmall) {
        Map<LodTriad, List<LodTriad>> parentTriadsToRestore = new HashMap<>();
        for (LodTriad triad : triadsTooSmall) {
            if(triad.parent == null){
                continue;
            }
            parentTriadsToRestore.computeIfAbsent(triad.parent,
                    t -> new ArrayList<>())
                    .add(triad);
        }
        for (Map.Entry<LodTriad, List<LodTriad>> triadToRestoreWithChildren : parentTriadsToRestore.entrySet()) {
            final List<LodTriad> children = triadToRestoreWithChildren.getValue();
            if(children.size() < 4){
                continue;
            } else {
                for (LodTriad child : children) {
                    removeTriad(child);
                }
                addTriad(triadToRestoreWithChildren.getKey());
            }
        }
    }

    private static final class PointsContainer {
        private final List<MJ3DPoint3D> points;
        private final List<Integer> pointRefinementLevels;
        private final List<List<LodTriad>> triadsUsingPoints;
        private final Map<Long, Integer> pointIndexLookupTable;
        private final Consumer<MJ3DPoint3D> noiseOffsetGenerator;
        private BiFunction<MJ3DPoint3D, MJ3DPoint3D, MJ3DPoint3D> midPointBuilder;

        private PointsContainer(Consumer<MJ3DPoint3D> noiseOffsetGenerator, BiFunction<MJ3DPoint3D, MJ3DPoint3D, MJ3DPoint3D> midPointBuilder) {
            this.noiseOffsetGenerator = noiseOffsetGenerator;
            this.midPointBuilder = midPointBuilder;
            this.points = new ArrayList<>();
            this.pointRefinementLevels = new ArrayList<>();
            this.triadsUsingPoints = new ArrayList<>();
            this.pointIndexLookupTable = new HashMap<>();
        }

        private void registerTriad(LodTriad triad){
            for (int pt : triad.pts) {
                List<LodTriad> triads = triadsUsingPoints.get(pt);
                triads.add(triad);
            }
        }

        private void unregisterTriad(LodTriad triad){
            for (int pt : triad.pts) {
                List<LodTriad> triads = triadsUsingPoints.get(pt);
                triads.remove(triad);
            }
        }

        private int addPoint(MJ3DPoint3D point, int refinementLevel){
            final int index = points.size();
            this.noiseOffsetGenerator.accept(point);
            this.points.add(point);
            this.pointRefinementLevels.add(refinementLevel);
            this.triadsUsingPoints.add(new ArrayList<>());
            point.setMapIndex(index);
            return index;
        }

        private void removePoint(int index){
            this.points.remove(index);
            this.pointRefinementLevels.remove(index);
            this.triadsUsingPoints.remove(index);
        }

        private Integer getMidPointIfInUse(int indexPointA, int indexPointB){
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
                midPoint = this.midPointBuilder.apply(points.get(indexPointA), points.get(indexPointB));
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

        private MJ3DPoint3D getPointAtIndex(int index){
            return points.get(index);
        }

        private int getPointRefinementLevel(int index){
            return pointRefinementLevels.get(index);
        }

        private MJ3DPoint3D[] getPoints(){
            MJ3DPoint3D[] arr = new MJ3DPoint3D[this.points.size()];
            return this.points.toArray(arr);
        }
    }

}
