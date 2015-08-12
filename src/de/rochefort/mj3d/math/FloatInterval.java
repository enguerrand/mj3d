package de.rochefort.mj3d.math;

public class FloatInterval {
	public static final FloatInterval EMPTY = new FloatInterval(0f, 0f);
	private final float min;
	private final float max;
	private final float size;
	private final boolean signSwitch;
	public FloatInterval(float min, float max) {
		if(min>max)
			throw new IllegalArgumentException("Max cannot be smaller than min!");
		this.min = min;
		this.max = max;
		this.size = max - min;
		this.signSwitch = max*min < 0;
	}

	public float getMax() {
		return max;
	}
	
	public float getMin() {
		return min;
	}
	
	public float getSize() {
		return size;
	}
	
	public boolean isSignSwitch() {
		return signSwitch;
	}
}
