package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class TriadContainer {
    private final PointsContainer pointsContainer;
    private List<TriadModel> triadList;
    TriadContainer(Consumer<MJ3DPoint3D> noiseOffsetGenerator, BiFunction<MJ3DPoint3D, MJ3DPoint3D, MJ3DPoint3D> midPointBuilder) {
        this.pointsContainer = new PointsContainer(noiseOffsetGenerator, midPointBuilder);
        this.triadList = new ArrayList<>();
    }

    int getTriadCount(){
        return triadList.size();
    }

    TriadModel getTriadAtIndex(int index){
        return triadList.get(index);
    }

    void addPoint(MJ3DPoint3D point, int refinementLevel){
        pointsContainer.addPoint(point, refinementLevel);
    }

    void addTriad(TriadModel triad) {
        this.pointsContainer.registerTriad(triad);
        this.triadList.add(triad);
    }

    Iterator<TriadModel> iterator(){
        return new Iterator<TriadModel>() {
            Iterator<TriadModel> internalIterator = triadList.iterator();
            TriadModel current = null;
            @Override
            public boolean hasNext() {
                return internalIterator.hasNext();
            }

            @Override
            public TriadModel next() {
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

    void removeTriad(TriadModel triad){
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

    private static final class PointsContainer {
        private final List<MJ3DPoint3D> points;
        private final List<Integer> pointRefinementLevels;
        private final List<List<TriadModel>> triadsUsingPoints;
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

        private void registerTriad(TriadModel triad){
            for (int pt : triad.pts) {
                List<TriadModel> triads = triadsUsingPoints.get(pt);
                triads.add(triad);
            }
        }

        private void unregisterTriad(TriadModel triad){
            for (int pt : triad.pts) {
                List<TriadModel> triads = triadsUsingPoints.get(pt);
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

    private static class TriadModel {
        final int[] pts;
        final TriadModel parent;

        TriadModel(int[] pts, TriadModel parent) {
            this.pts = pts;
            this.parent = parent;
        }
    }
}
