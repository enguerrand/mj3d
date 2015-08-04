package de.rochefort.mj3d.math.randomness;

public class FractalNoiseConfig {
	public static int MOUNTAIN = 0;
	public static int HILL = 1;
	public static int ROCK = 2;
	public static int STONE = 3;
	
	private float [] featureSizes;
	private float [] featureAmplitudes;
	
	public FractalNoiseConfig(int octavesCount) {
		featureSizes = new float[octavesCount];
		featureAmplitudes = new float[octavesCount];
		for(int i=0; i<octavesCount; i++){
			featureSizes[i]=0;
			featureAmplitudes[i]=0;
		}
	}
	public FractalNoiseConfig(float mountainSize, float mountainHeight, float hillSize, float hillHeight) {
		this(2);
		this.featureSizes[MOUNTAIN] = mountainSize;
		this.featureAmplitudes[MOUNTAIN]= mountainHeight;
		this.featureSizes[HILL] = hillSize;
		this.featureAmplitudes[HILL]= hillHeight;
	}
	
	public int getOctavesCount(){
		return featureSizes.length;
	}
	
	public float getAmplitude(int octave){
		return featureAmplitudes[octave];
	}

	public float getSize(int octave){
		return featureSizes[octave];
	}
	
	public float getScalingFactor(int octave){
		return 1f / featureSizes[octave];
	}

	public void setAmplitude(int octave, float amplitude){
		featureAmplitudes[octave] = amplitude;
	}
	
	public void getSize(int octave, float size){
		featureSizes[octave] = size;
	}
	
}
