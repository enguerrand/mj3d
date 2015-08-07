package de.rochefort.mj3d.math;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;

public class MJ3DSphere {
	private final MJ3DPoint3D center;
	private final float radius;
	private final float aequatorialCircumference;
	public MJ3DSphere(MJ3DPoint3D center, float radius) {
		this.center = center;
		this.radius = radius;
		this.aequatorialCircumference = 2f * Defines.PI * radius;
	}
	
	public MJ3DPoint3D getPoint(float latitudeRad, float longitudeRad){
		float x = center.getX() + (float) (radius * Math.cos(latitudeRad) * Math.sin(longitudeRad));
		float y = center.getY() + (float) (radius * Math.cos(latitudeRad) * Math.sin(longitudeRad + 0.5f*Defines.PI));
		float z = center.getZ() + (float) (radius * Math.sin(latitudeRad));
		return new MJ3DPoint3D(x, y, z);
	}
	
	public float getCirumference(){
		return this.aequatorialCircumference;
	}
	
	public float getCirumference(float latitudeRad){
		return aequatorialCircumference * (float)Math.cos(latitudeRad);
	}
	
	public float getAngle(float segmentLength){
		return 2 * Defines.PI * segmentLength / aequatorialCircumference;
	}
	
	public float getSegmentCount(float latitudeRad, float segmentLength){
		return getCirumference(latitudeRad) / segmentLength;
	}

}
