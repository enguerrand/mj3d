package de.rochefort.mj3d.math;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Comparator;

import de.rochefort.mj3d.objects.MJ3DObject;

public class MJ3DVector implements MJ3DObject {
	public enum UnitVectorType {X, Y, Z};
	public static final MJ3DVector X_UNIT_VECTOR = new MJ3DVector(UnitVectorType.X);
	public static final MJ3DVector Y_UNIT_VECTOR = new MJ3DVector(UnitVectorType.Y);
	public static final MJ3DVector Z_UNIT_VECTOR = new MJ3DVector(UnitVectorType.Z);
	protected float x=0;
	protected float y=0;
	protected float z=0;

	public MJ3DVector(){
	}
	
	public MJ3DVector(UnitVectorType type) {
		this();
		switch (type){
			case X:
				x=1.0f;
				break;
			case Y:
				y=1.0f;
				break;
			case Z:
				z=1.0f;
				break;
		}
	}
	
	public MJ3DVector(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public MJ3DVector rotate(MJ3DMatrix rotationMatrix){
		return rotationMatrix.multiply(this);
	}
	
	public float getX() {
		return x;
	}
	public float getY() {
		return y;
	}
	public float getZ() {
		return z;
	}
	
	public void setX(float x) {
		this.x = x;
	}
	
	public void setY(float y) {
		this.y = y;
	}
	
	public void setZ(float z) {
		this.z = z;
	}
	
	public float getLengthSquared(){
		return x*x+y*y+z*z;
	}
	
	public float getLength(){
		return (float)Math.sqrt(getLengthSquared());
	}
	
	public MJ3DVector add(float x, float y, float z){
		return new MJ3DVector(this.x+x, this.y+y, this.z+z);
	}
	public MJ3DVector add(MJ3DVector vect) {
		return new MJ3DVector(this.x+vect.x, this.y+vect.y, this.z+vect.z);
	}
	public MJ3DVector substract(MJ3DVector vect) {
		return new MJ3DVector(this.x-vect.x, this.y-vect.y, this.z-vect.z);
	}
	public void translate(MJ3DVector vect) {
		this.x += vect.x;
		this.y += vect.y;
		this.z += vect.z;
	}
	public MJ3DVector multiply(float factor) {
		return new MJ3DVector(this.x*factor, this.y*factor, this.z*factor);
	}
	public void scale(float factor) {
		this.x*=factor;
		this.y*=factor;
		this.z*=factor;
	}
	public void scaleToUnitLength() {
		this.scale(1f / getLength());
	}
	@Override
	public String toString() {
		NumberFormat df = DecimalFormat.getNumberInstance();
		return "MJ3DVector [x=" + df.format(x) + ", y=" + df.format(y) + ", z=" + df.format(z) + "]";
	}
	
	public boolean merge(MJ3DVector targetVector, float mergeTolerance){
		if(Math.abs(this.x-targetVector.x) > mergeTolerance || Math.abs(this.y-targetVector.y) > mergeTolerance || Math.abs(this.z-targetVector.z) > mergeTolerance){
			return false;
		}
		else {
			this.x=targetVector.x;
			this.y=targetVector.y;
			this.z=targetVector.z;
			return true;
		}
	}
	
	public void normalize() {
	    this.scale(1f/this.getLength());
	}
	
	public static MJ3DVector crossProduct (MJ3DVector vect1, MJ3DVector vect2) {
		return new MJ3DVector (
			vect1.y * vect2.z - vect1.z * vect2.y, 
			vect1.z * vect2.x - vect1.x * vect2.z,
			vect1.x * vect2.y - vect1.y * vect2.x
		);
	}
	public static float dotProduct (MJ3DVector vect1, MJ3DVector vect2) {
		return vect1.x * vect2.x + vect1.y * vect2.y + vect1.z * vect2.z;
	}
	
	public static Comparator<MJ3DVector> getXyzComparator(){
		return new Comparator<MJ3DVector>() {
			@Override
			public int compare(MJ3DVector v1, MJ3DVector v2) {
				if(v1==null && v2==null) return 0;
				if(v1!=null && v2==null) return -1;
				if(v1==null && v2!=null) return 1;
				if(v1.x==v2.x && v1.y==v2.y && v1.z==v2.z) return 0;
				if(v1.x==v2.x && v1.y==v2.y) return Float.compare(v1.z, v2.z);
				if(v1.x==v2.x) return Float.compare(v1.y, v2.y);
				return Float.compare(v1.x, v2.x);
			}
		};
	}
}
