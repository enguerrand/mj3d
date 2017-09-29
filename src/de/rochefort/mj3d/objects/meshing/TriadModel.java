package de.rochefort.mj3d.objects.meshing;

public class TriadModel {
    final int[] pts;
    final TriadModel parent;

    TriadModel(int[] pts, TriadModel parent) {
        this.pts = pts;
        this.parent = parent;
    }
}
