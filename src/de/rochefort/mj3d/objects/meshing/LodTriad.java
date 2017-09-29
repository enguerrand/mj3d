package de.rochefort.mj3d.objects.meshing;

class LodTriad {
    final int[] pts;
    final LodTriad parent;

    LodTriad(int[] pts, LodTriad parent) {
        this.pts = pts;
        this.parent = parent;
    }
}
