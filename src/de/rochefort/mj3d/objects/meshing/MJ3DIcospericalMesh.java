package de.rochefort.mj3d.objects.meshing;

import de.rochefort.mj3d.math.Defines;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edr on 6/15/16.
 */
public class MJ3DIcospericalMesh {
    private final Map<Long, Integer> pointIndexLoopupTable;
    private final List<int[]> triadList;
    private final List<MJ3DPoint3D> points;
    private final float radius;

    public MJ3DIcospericalMesh(float radius) {
        this.radius = radius;
        this.pointIndexLoopupTable = new HashMap<>();
        this.triadList = new ArrayList<>();
        this.points = new ArrayList<>();
        initializePoints();
        initializeMesh();
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

    private void initializePoints(){
        // equations from https://en.wikipedia.org/wiki/Regular_icosahedron#Cartesian_coordinates
        float halfEdgeLength = radius / (float)Math.sin(Defines.PI_DOUBLED / 5f);
        float phi = halfEdgeLength * (1.0f + (float)Math.sqrt(5.0)) / 2.0f;
        addPoint(new MJ3DPoint3D(-halfEdgeLength,  phi,  0));
        addPoint(new MJ3DPoint3D( halfEdgeLength,  phi,  0));
        addPoint(new MJ3DPoint3D(-halfEdgeLength, -phi,  0));
        addPoint(new MJ3DPoint3D( halfEdgeLength, -phi,  0));

        addPoint(new MJ3DPoint3D( 0, -halfEdgeLength,  phi));
        addPoint(new MJ3DPoint3D( 0,  halfEdgeLength,  phi));
        addPoint(new MJ3DPoint3D( 0, -halfEdgeLength, -phi));
        addPoint(new MJ3DPoint3D( 0,  halfEdgeLength, -phi));

        addPoint(new MJ3DPoint3D( phi,  0, -halfEdgeLength));
        addPoint(new MJ3DPoint3D( phi,  0,  halfEdgeLength));
        addPoint(new MJ3DPoint3D(-phi,  0, -halfEdgeLength));
        addPoint(new MJ3DPoint3D(-phi,  0,  halfEdgeLength));
    }

    public MJ3DTriad[] buildTriads(){
        MJ3DTriad[] triads = new MJ3DTriad[triadList.size()];
        final int size = this.triadList.size();
        for(int index = 0; index < size; index++){
            MJ3DPoint3D[] pts = new MJ3DPoint3D[3];
            for (int i=0; i<3; i++) {
                pts[i] = points.get(triadList.get(index)[i]);
            }
            triads[index] = new MJ3DTriad(pts, Color.RED);
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
}
