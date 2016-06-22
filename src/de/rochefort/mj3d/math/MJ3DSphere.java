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
		float z = getZ(latitudeRad, correctedRadius);
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
	
	public float getZ(float latitude, float radius){
		return center.getZ() + (float) (radius * Math.sin(latitude));
	}
	
	public float getZ(float latitude){
		return getZ(latitude, this.radius);
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
		float  sineLongitude = (float)(vectorFromOrigin.getX() / Math.cos(latitude));
		if(sineLongitude > 1f)
			sineLongitude = 1f;
		if(sineLongitude < -1f)
			sineLongitude = -1f;
		float longitude = (float) Math.asin(sineLongitude);
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
			return FloatInterval.EMPTY;
		}
		float latitudeMin = longLatPos.getLatitude() - alpha * ALPHA_SCALING;
		if(latitude < latitudeMin){
			return FloatInterval.EMPTY;
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
		float horizonZ = getZ(latitude);
//		float distanceToCenterSquared = getDistanceToCenterSquared(position);
		
		//Center to horizon
//		float centerToHorizonDeltaZ = horizonZ - center.getZ();
//		float centerToHorizonDeltaZSquared = centerToHorizonDeltaZ * centerToHorizonDeltaZ;
//		float centerToHorizonDeltaXYSquared = radiusSquared - centerToHorizonDeltaZSquared;
		float centerToHorizonXY = (float) (radius * Math.cos(latitude));
//		float centerToHorizonXY = (float) Math.sqrt(centerToHorizonDeltaXYSquared);
		float centerToHorizonDeltaXYSquared = centerToHorizonXY * centerToHorizonXY;
		
		// Center to viewer
		MJ3DVector vectorFromCenterToViewer = getVectorFromCenter(position);
		float centerToViewerDeltaX = vectorFromCenterToViewer.getX();
		float centerToViewerDeltaY = vectorFromCenterToViewer.getY();
		float centerToViewerDeltaXYSquared = centerToViewerDeltaX * centerToViewerDeltaX + centerToViewerDeltaY * centerToViewerDeltaY;
		float centerToViewerXY =(float)Math.sqrt(centerToViewerDeltaXYSquared);
		
		// Viewer to horizon
		float viewerToHorizonSquared = getDistanceToHorizonSquared(position);
		float viewerToHorizonDeltaZ = horizonZ - position.getZPos();
		float viewerToHorizonDeltaXYSquared = viewerToHorizonSquared - viewerToHorizonDeltaZ * viewerToHorizonDeltaZ;
		
		
		// cosine rule
		float cosineDeltaAngleToHorizon = (centerToHorizonDeltaXYSquared + centerToViewerDeltaXYSquared 
				- viewerToHorizonDeltaXYSquared) / (2 * centerToHorizonXY * centerToViewerXY);
		if(cosineDeltaAngleToHorizon > 1)
			cosineDeltaAngleToHorizon = 1f;
		else if(cosineDeltaAngleToHorizon < -1)
			cosineDeltaAngleToHorizon = -1;
//		else 
//			System.out.println(cosineDeltaAngleToHorizon);
		float deltaAngleToHorizon = (float) Math.acos(cosineDeltaAngleToHorizon);
		float min = longLatPos.getLongitude() - deltaAngleToHorizon;
		float max = longLatPos.getLongitude() + deltaAngleToHorizon;
//		
//		float min = longLatPos.getLongitude() - Defines.PI * 0.5f;
//		float max = longLatPos.getLongitude() + Defines.PI * 0.5f;
		
		return new FloatInterval(min, max);
	}

	public MJ3DPoint3D buildMidPoint(MJ3DPoint3D pointA, MJ3DPoint3D pointB) {
		MJ3DVector directMidVector = MJ3DVector.mean(pointA, pointB);
		MJ3DVector directMidVectorRelativeToCenter = directMidVector.substract(center);
        directMidVectorRelativeToCenter.scale(radius / directMidVectorRelativeToCenter.getLength());
        MJ3DVector midPointVector=center.add(directMidVectorRelativeToCenter);
        MJ3DPoint3D midPoint = new MJ3DPoint3D(midPointVector);
        return midPoint;
	}
}
