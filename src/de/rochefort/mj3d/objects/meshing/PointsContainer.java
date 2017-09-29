package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class PointsContainer {
    private final List<MJ3DPoint3D> points;
    private final List<Integer> pointRefinementLevels;
    private final List<List<TriadModel>> triadsUsingPoints;
    private final Map<Long, Integer> pointIndexLookupTable;
    private final Consumer<MJ3DPoint3D> noiseOffsetGenerator;
    private BiFunction<MJ3DPoint3D, MJ3DPoint3D, MJ3DPoint3D> midPointBuilder;

    PointsContainer(Consumer<MJ3DPoint3D> noiseOffsetGenerator, BiFunction<MJ3DPoint3D, MJ3DPoint3D, MJ3DPoint3D> midPointBuilder) {
        this.noiseOffsetGenerator = noiseOffsetGenerator;
        this.midPointBuilder = midPointBuilder;
        this.points = new ArrayList<>();
        this.pointRefinementLevels = new ArrayList<>();
        this.triadsUsingPoints = new ArrayList<>();
        this.pointIndexLookupTable = new HashMap<>();
    }

    void registerTriad(TriadModel triad){
        for (int pt : triad.pts) {
            List<TriadModel> triads = triadsUsingPoints.get(pt);
            triads.add(triad);
        }
    }

    void unregisterTriad(TriadModel triad){
        for (int pt : triad.pts) {
            List<TriadModel> triads = triadsUsingPoints.get(pt);
            triads.remove(triad);
        }
    }

    int addPoint(MJ3DPoint3D point, int refinementLevel){
        final int index = points.size();
        this.noiseOffsetGenerator.accept(point);
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

    Integer getOrCreateMidPoint(int indexPointA, int indexPointB, int refinementLevel){
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
