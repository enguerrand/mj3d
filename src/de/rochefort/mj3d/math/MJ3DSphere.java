package de.rochefort.mj3d.math;

import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public class MJ3DSphere {
	private static final float ALPHA_SCALING = 1.1f;
	private final MJ3DPoint3D center;
	private final float radius;
	private final float radiusSquared;
	private final float aequatorialCircumference;
	public MJ3DSphere(MJ3DPoint3D center, float radius) {
		this.center = center;
		this.radius = radius;
		this.radiusSquared = radius*radius;
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
	
	public MJ3DVector getVectorFromCenter(MJ3DViewingPosition position){
		return new MJ3DVector(position.getXPos()-center.getX(), 
				position.getYPos()-center.getY(), 
				position.getZPos()-center.getZ());
	}
	
	public float getDistanceToCenterSquared(MJ3DViewingPosition position){
		return getVectorFromCenter(position).getLengthSquared();
	}
	
	public float getDistanceToHorizonSquared(MJ3DViewingPosition position){
		return getDistanceToCenterSquared(position) - this.radiusSquared;
	}
	
	public float getDistanceToSurface(MJ3DViewingPosition position) {
		return (float) Math.sqrt(getDistanceToCenterSquared(position)) - this.radius;
	}
	
	public float getHorizonAngle(MJ3DViewingPosition position){
		float dHorizonSquared = getDistanceToHorizonSquared(position);
		float dSquared = getDistanceToCenterSquared(position);
		float d = (float)Math.sqrt(dSquared);
		float cosAlpha = (this.radiusSquared + dSquared - dHorizonSquared) / (2f * this.radius * d);
		return (float) Math.acos(cosAlpha);
	}
	
	public LongLatPosition getLongLatPosition(MJ3DVector vector){
		MJ3DVector vectorFromOrigin = vector.substract(center);
		vectorFromOrigin.scaleToUnitLength();
		float latitude = (float) Math.asin(vectorFromOrigin.getZ());
		float longitude = (float) Math.asin(vectorFromOrigin.getX() / Math.cos(latitude));
		if(vectorFromOrigin.getY() < 0){
			longitude = Defines.PI * 3f  - longitude ;
		}
		return new LongLatPosition(longitude, latitude);
	}
	
	public FloatInterval getHorizonLongitudeInterval(MJ3DViewingPosition position, float latitude){
		LongLatPosition longLatPos = getLongLatPosition(position.getPositionVector());
		float alpha = getHorizonAngle(position);
		float latitudeMax = longLatPos.getLatitude() + alpha * ALPHA_SCALING;
		if(latitude > latitudeMax){
			return new FloatInterval(0f, 0f);
		}
		float latitudeMin = longLatPos.getLatitude() - alpha * ALPHA_SCALING;
		if(latitude < latitudeMin){
			return new FloatInterval(0f, 0f);
		}
		if(longLatPos.getLatitude() + alpha * ALPHA_SCALING > 0.5f * Defines.PI){
			float lowestFullLongitude = Defines.PI - longLatPos.getLatitude() + alpha * ALPHA_SCALING;
			if(latitude > lowestFullLongitude){
				return new FloatInterval(0f, Defines.PI_DOUBLED);
			}
		}
		if(longLatPos.getLatitude() - alpha * ALPHA_SCALING < -0.5f * Defines.PI){
			float highestFullLongitude = Defines.PI + longLatPos.getLatitude() - alpha * ALPHA_SCALING;
			if(latitude < highestFullLongitude){
				return new FloatInterval(0f, Defines.PI_DOUBLED);
			}
		}
		float min = longLatPos.getLongitude() - Defines.PI * 0.5f;
		float max = longLatPos.getLongitude() + Defines.PI * 0.5f;
//		if(min > Defines.PI_DOUBLED)
//			min -= Defines.PI_DOUBLED;
//		else if(min < 0f)
//			min += Defines.PI_DOUBLED;
//		if(max > Defines.PI_DOUBLED)
//			max -= Defines.PI_DOUBLED;
//		else if(max < 0f)
//			max += Defines.PI_DOUBLED;
		
		return new FloatInterval(min, max);
	}
}
