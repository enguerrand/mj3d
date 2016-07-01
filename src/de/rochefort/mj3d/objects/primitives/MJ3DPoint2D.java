package de.rochefort.mj3d.objects.primitives;

public class MJ3DPoint2D {
	private int[] pos = new int[2];
	public MJ3DPoint2D(int x, int y) {
		pos[0]=x;
		pos[1]=y;
	}
	
	public int[] getPos() {
		return pos;
	}
	
	public int getX(){
		return pos[0];
	}
	public int getY(){
		return pos[1];
	}
}
