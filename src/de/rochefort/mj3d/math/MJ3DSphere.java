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
		return getPoint(latitudeRad, longitudeRad, 0f);
	}
	
	public MJ3DPoint3D getPoint(float latitudeRad, float longitudeRad, float radialOffset){
		float correctedRadius = radius + radialOffset;
		float x = center.getX() + (float) (correctedRadius * Math.cos(latitudeRad) * Math.sin(longitudeRad));
		float y = center.getY() + (float) (correctedRadius * Math.cos(latitudeRad) * Math.sin(longitudeRad + 0.5f*Defines.PI));
		float z = center.getZ() + (float) (correctedRadius * Math.sin(latitudeRad));
		return new MJ3DPoint3D(x, y, z);
	}
	
	public float getCirumference(){
		return this.aequatorialCircumference;
	}
	
	public float getCirumference(float latitudeRad){
		float cos = (float)Math.cos(latitudeRad);
		if(cos < 0){
			cos = 0f;
		}
		return aequatorialCircumference * cos;
	}
	
	public float getAngle(float segmentLength){
		return 2 * Defines.PI * segmentLength / aequatorialCircumference;
	}
	
	public float getSegmentCount(float latitudeRad, float segmentLength){
		return getCirumference(latitudeRad) / segmentLength;
	}

}
