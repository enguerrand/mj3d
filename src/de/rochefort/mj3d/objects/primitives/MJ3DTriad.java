package de.rochefort.mj3d.objects.primitives;

import java.awt.Color;
import java.util.Arrays;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.objects.MJ3DObject;

public class MJ3DTriad implements MJ3DObject {
	private MJ3DVector [] points;
	private MJ3DVector normal;
	private Color color;

	public MJ3DTriad(MJ3DVector[] points) {
		this.points = points;
//		System.out.println("New Triad: "+this);
	}
	public MJ3DTriad(MJ3DVector[] points, Color color) {
		this(points);
		this.color = color;
	}

	public MJ3DVector[] getPoints() {
		return points;
	}

	public void setPoints(MJ3DVector[] points) {
		this.points = points;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}
	
	@Override
	public String toString() {
		return "MJ3DTriad [points=" + Arrays.toString(points) + ", color=" + color + "]";
	}

	@Override
	public float getX() {
		return points[0].getX();
	}

	@Override
	public float getY() {
		return points[0].getY();
	}

	@Override
	public float getZ() {
		return points[0].getZ();
	}
	
	public void updateSurfaceNormal(){
		MJ3DVector vect1 = new MJ3DVector(points[1].getX()-points[0].getX(), points[1].getY()-points[0].getY(), points[1].getZ()-points[0].getZ());
		MJ3DVector vect2 = new MJ3DVector(points[2].getX()-points[0].getX(), points[2].getY()-points[0].getY(), points[2].getZ()-points[0].getZ());
		this.normal = MJ3DVector.crossProduct(vect1, vect2);
		this.normal.normalize();
	}

	public MJ3DVector getNormal() {
		return normal;
	}
}
