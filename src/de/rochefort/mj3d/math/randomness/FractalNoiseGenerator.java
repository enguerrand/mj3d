package de.rochefort.mj3d.math.randomness;

import java.util.Random;


public class FractalNoiseGenerator {
	private final OpenSimplexNoise[] osn;
	private float[] amplitudes;
	private float[] scalingFactors;
	
	public FractalNoiseGenerator(long seed, FractalNoiseConfig config) {
		Random r = new Random(seed);
		this.osn = new OpenSimplexNoise[config.getOctavesCount()];
		this.amplitudes = new float[config.getOctavesCount()];
		this.scalingFactors = new float[config.getOctavesCount()];
		for(int i=0; i<config.getOctavesCount(); i++){
			this.osn[i] = new OpenSimplexNoise(r.nextLong());
			this.amplitudes[i] = config.getAmplitude(i);
			this.scalingFactors[i] = config.getScalingFactor(i);
			
		}
	}

	public float fractalNoise2D(float x, float y) {
		float result = 0;
		for (int oct = 0; oct < this.osn.length; oct++) {
			result += (float) (this.osn[oct].eval(x * this.scalingFactors[oct], y * this.scalingFactors[oct]) * this.amplitudes[oct]);
		}
		return result;
	}
	
	public float fractalNoise3D(float x, float y, float z) {
		float result = 0;
		for (int oct = 0; oct < this.osn.length; oct++) {
			result += (float) (this.osn[oct].eval(x * this.scalingFactors[oct], y * this.scalingFactors[oct], z * this.scalingFactors[oct]) * this.amplitudes[oct]);
		}
		return result;
	}

}
