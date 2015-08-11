package de.rochefort.mj3d.math;

public class LongLatPosition {
	private final float longitude;
	private final float latitude;
	public LongLatPosition(float longitude, float latitude) {
		this.longitude = getCorrectedLongitude(longitude);
		this.latitude = latitude;
	}
	
	private float getCorrectedLongitude(float input){
		if(input < 0)
			return Defines.PI_DOUBLED + input;
		if(input > Defines.PI_DOUBLED)
			return input - Defines.PI_DOUBLED;
		return input;
	}

	public float getLatitude() {
		return latitude;
	}
	
	public float getLongitude() {
		return longitude;
	}

	@Override
	public String toString() {
		return "LongLatPosition [longitude=" + longitude + ", latitude=" + latitude + "]";
	}
	
	
}
