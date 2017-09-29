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
    private final TriadContainer triadsContainer;

    public MJ3DIcospericalMesh(float radius, MJ3DPoint3D center, FractalNoiseGenerator noiseGen) {
        this.noiseGen = noiseGen;
        this.radius = radius;
        this.baseShape = new MJ3DSphere(center, radius);
        this.edgeLength = radius / (float) Math.sin(Defines.PI_DOUBLED / 5f);

        final Consumer<MJ3DPoint3D> noiseOffsetGenerator = point ->
            this.baseShape.offsetFromSurface(point, this.noiseGen.fractalNoise3D(point.getX(), point.getY(), point.getZ()));

        BiFunction<MJ3DPoint3D, MJ3DPoint3D, MJ3DPoint3D> midPointBuilder = (point1, point2) ->
            this.baseShape.buildMidPoint(point1, point2);
        this.triadsContainer = new TriadContainer(noiseOffsetGenerator, midPointBuilder);

        initializePoints(center);
        initializeMesh();
    }

    private void addTriad(int index1, int index2, int index3){
        final int[] pts = {index1, index2, index3};
        final TriadContainer.TriadModel triad = new TriadContainer.TriadModel(pts, null);
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

        triadsContainer.addPoint(new MJ3DPoint3D(cx-halfEdgeLength, cy+phi,            cz+0), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx+halfEdgeLength, cy+phi,            cz+0), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx-halfEdgeLength, cy-phi,            cz+0), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx+halfEdgeLength, cy-phi,            cz+0), refinementLevel);

        triadsContainer.addPoint(new MJ3DPoint3D(cx+0,              cy-halfEdgeLength, cz+phi), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx+0,              cy+halfEdgeLength, cz+phi), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx+0,              cy-halfEdgeLength, cz-phi), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx+0,              cy+halfEdgeLength, cz-phi), refinementLevel);

        triadsContainer.addPoint(new MJ3DPoint3D(cx+phi,            cy,                cz-halfEdgeLength), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx+phi,            cy,                cz+halfEdgeLength), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx-phi,            cy,                cz-halfEdgeLength), refinementLevel);
        triadsContainer.addPoint(new MJ3DPoint3D(cx-phi,            cy,                cz+halfEdgeLength), refinementLevel);
    }

    public MJ3DTriad[] buildTriads(Color triadColor, boolean reverseSurfaceNormal, float ambientLight, MJ3DVector vectorOfLight){
        float illuminationFactor = (1f - ambientLight) *0.5f;
        int triadCount = triadsContainer.getTriadCount();
        MJ3DTriad[] triads = new MJ3DTriad[triadCount];
        final int size = triadCount;
        for(int triadIndex = 0; triadIndex < size; triadIndex++){
            MJ3DPoint3D[] pts = new MJ3DPoint3D[3];
            for (int i=0; i<3; i++) {
                pts[i] = triadsContainer.getPointAtIndex(triadsContainer.getTriadAtIndex(triadIndex).pts[i]);
            }
            triads[triadIndex] = new MJ3DTriad(pts, triadColor, reverseSurfaceNormal, ambientLight, illuminationFactor, vectorOfLight);
        }
        return triads;

    }

    public MJ3DPoint3D[] getPoints(){
       return triadsContainer.getPoints();
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
        List<TriadContainer.TriadModel> triadsTooSmall = splitTriads(pointRecursionDepths);
        mergeTriads(triadsTooSmall);
        fixTransitions();
    }

    private void fixTransitions() {
        List<TriadContainer.TriadModel> triadsToAdd = new ArrayList<>();
        Iterator<TriadContainer.TriadModel> triadsIterator = this.triadsContainer.iterator();
        while(triadsIterator.hasNext()) {
            final TriadContainer.TriadModel triad = triadsIterator.next();
            Integer midPoint1 = triadsContainer.getMidPointIfInUse(triad.pts[0], triad.pts[1]);
            Integer midPoint2 = triadsContainer.getMidPointIfInUse(triad.pts[1], triad.pts[2]);
            Integer midPoint3 = triadsContainer.getMidPointIfInUse(triad.pts[2], triad.pts[0]);
            final boolean split1 = midPoint1 != null;
            final boolean split2 = midPoint2 != null;
            final boolean split3 = midPoint3 != null;
            if((split1 && split2) || (split1 && split3) || (split2 && split3)) {
                throw new IllegalStateException("can only split at one edge!");
            } else if (split1) {
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[0], midPoint1, triad.pts[2]};
                int[] newPts2 = {midPoint1, triad.pts[1],triad.pts[2]};
                triadsToAdd.add(new TriadContainer.TriadModel(newPts1, triad));
                triadsToAdd.add(new TriadContainer.TriadModel(newPts2, triad));
            } else if (split2) {
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[1], midPoint2, triad.pts[0]};
                int[] newPts2 = {midPoint2, triad.pts[2],triad.pts[0]};
                triadsToAdd.add(new TriadContainer.TriadModel(newPts1, triad));
                triadsToAdd.add(new TriadContainer.TriadModel(newPts2, triad));
            } else if (split3) {
                triadsIterator.remove();
                int[] newPts1 = {triad.pts[2], midPoint3, triad.pts[1]};
                int[] newPts2 = {midPoint3, triad.pts[0],triad.pts[1]};
                triadsToAdd.add(new TriadContainer.TriadModel(newPts1, triad));
                triadsToAdd.add(new TriadContainer.TriadModel(newPts2, triad));
            }
        }
        for (TriadContainer.TriadModel triad : triadsToAdd) {
            triadsContainer.addTriad(triad);
        }
    }

    public float getDistanceToHorizon(MJ3DViewingPosition viewingPosition) {
        return ((float)Math.sqrt(this.baseShape.getDistanceToHorizonSquared(viewingPosition))) + edgeLength ;
    }


    private List<TriadContainer.TriadModel> splitTriads(int[] pointRecursionDepths) {
        List<TriadContainer.TriadModel> triadsTooSmall = new ArrayList<>();
        List<TriadContainer.TriadModel> triadsToAdd = new ArrayList<>();
        Iterator<TriadContainer.TriadModel> triadsIterator = this.triadsContainer.iterator();
        while(triadsIterator.hasNext()) {
            final TriadContainer.TriadModel nextTriad = triadsIterator.next();
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
                actualMaxDepth = Math.max(actualMaxDepth, triadsContainer.getPointRefinementLevel(ptIndex));
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
        for (TriadContainer.TriadModel triad : triadsToAdd) {
            triadsContainer.addTriad(triad);
        }
        return triadsTooSmall;
    }

    private void mergeTriads(List<TriadContainer.TriadModel> triadsTooSmall) {
        Map<TriadContainer.TriadModel, List<TriadContainer.TriadModel>> parentTriadsToRestore = new HashMap<>();
        for (TriadContainer.TriadModel triad : triadsTooSmall) {
            if(triad.parent == null){
                continue;
            }
            parentTriadsToRestore.computeIfAbsent(triad.parent,
                    t -> new ArrayList<>())
                    .add(triad);
        }
        for (Entry<TriadContainer.TriadModel, List<TriadContainer.TriadModel>> triadToRestoreWithChildren : parentTriadsToRestore.entrySet()) {
            final List<TriadContainer.TriadModel> children = triadToRestoreWithChildren.getValue();
            if(children.size() < 4){
                continue;
            } else {
                for (TriadContainer.TriadModel child : children) {
                    triadsContainer.removeTriad(child);
                }
                triadsContainer.addTriad(triadToRestoreWithChildren.getKey());
            }
        }
    }

    private List<TriadContainer.TriadModel> refineTriad(TriadContainer.TriadModel parent, int refinementLevel, int recursionCount) {
        if(recursionCount < 1){
            throw new IllegalArgumentException("Recursion count < 1 does not make sense!");
        }
        final Integer np1 = triadsContainer.getOrCreateMidPoint(parent.pts[0], parent.pts[1], refinementLevel);
        final Integer np2 = triadsContainer.getOrCreateMidPoint(parent.pts[1], parent.pts[2], refinementLevel);
        final Integer np3 = triadsContainer.getOrCreateMidPoint(parent.pts[2], parent.pts[0], refinementLevel);

        TriadContainer.TriadModel[] resultingTriads = new TriadContainer.TriadModel[4];
        resultingTriads[0] = new TriadContainer.TriadModel(new int[]{np1,             np2,             np3            }, parent);
        resultingTriads[1] = new TriadContainer.TriadModel(new int[]{parent.pts[0], np1,             np3            }, parent);
        resultingTriads[2] = new TriadContainer.TriadModel(new int[]{np3,             np2,             parent.pts[2]}, parent);
        resultingTriads[3] = new TriadContainer.TriadModel(new int[]{np1,             parent.pts[1], np2            }, parent);

        if(recursionCount == 1){
            return Arrays.asList(resultingTriads);
        } else {
            List<TriadContainer.TriadModel> result = new ArrayList<>();
            recursionCount--;
            refinementLevel++;
            for (int t = 0; t<4; t++){
                result.addAll(refineTriad(resultingTriads[t], refinementLevel, recursionCount));
            }
            return result;
        }
    }

}
