package de.rochefort.mj3d.math.randomness;

import java.util.Random;


public class PerlinNoiseGenerator {
	private final OpenSimplexNoise[] osn;

	public PerlinNoiseGenerator(long seed, short octavesCount, float persistence) {
		Random r = new Random(seed);
		this.osn = new OpenSimplexNoise[octavesCount];
		for(int i=0; i<octavesCount; i++){
			this.osn[i] = new OpenSimplexNoise(r.nextLong());
		}
	}

	private float noise(float x, float y, short octave) {
		return (float) osn[octave].eval(x, y);
	}

	private float smoothedNoise(int x, int y, short octave) {
		float corners = (noise(x - 1, y - 1, octave) + noise(x + 1, y - 1, octave)
				+ noise(x - 1, y + 1, octave) + noise(x + 1, y + 1, octave)) / 16f;
		float sides = (noise(x - 1, y, octave) + noise(x + 1, y, octave) + noise(x, y - 1, octave) + noise(
				x, y + 1, octave)) / 8f;
		float center = noise(x, y, octave) / 4f;
		return corners + sides + center;
	}

	private float linearInterpolate(float a, float b, float x) {
		return a * (1 - x) + b * x;
	}

	private float interpolatedNoise(short octave, float x, float y) {
		int integer_X = (int) x;
		float fractional_X = x - integer_X;

		int integer_Y = (int) y;
		float fractional_Y = y - integer_Y;

		float v1 = smoothedNoise(integer_X, integer_Y, octave);
		float v2 = smoothedNoise(integer_X + 1, integer_Y, octave);
		float v3 = smoothedNoise(integer_X, integer_Y + 1, octave);
		float v4 = smoothedNoise(integer_X + 1, integer_Y + 1, octave);

		float i1 = linearInterpolate(v1, v2, fractional_X);
		float i2 = linearInterpolate(v3, v4, fractional_X);

		return linearInterpolate(i1, i2, fractional_Y);
	}

	public float perlinNoise2D(float x, float y, int octavesCount,
			float persistence, float baseFrequency, float baseAmplitude) {
		float total = 0;
		short n = 0;

		for (int octave = 0; octave < octavesCount; octave++) {
			float frequency = baseFrequency * (float) Math.pow(2, octave);
			float amplitude = baseAmplitude
					* (float) Math.pow(persistence, octave);
			total = total + interpolatedNoise(n, x * frequency, y * frequency)
					* amplitude;
		}
		return total;
	}
}
