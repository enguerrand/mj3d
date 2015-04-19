package de.rochefort.mj3d.math.randomness;

import java.util.Random;

public class RandomNumberGenerator {
	private Random random;
	public RandomNumberGenerator(long seed) {
		random = new Random(seed);
	}
	
	public float randomFloatRepeatable(float min, float max){							// create random float between min and max
		if(min == max){
			return min;
		}
		if(min>max){
			throw new IllegalArgumentException("Error on invokation of randomDouble: Min value "+min +" is larger than max value "+max+"!");
		}
		
		float result = min + Math.abs(random.nextFloat()) * (max-min);		// TODO check correct max
	//	System.out.println("m.randomNrRepr: returning "+result);
		return result;
	}

	public static void main(String[] args) {
		RandomNumberGenerator gen = new RandomNumberGenerator(1);
		for(int i=0; i<10; i++){
			System.out.println(gen.randomFloatRepeatable(0, 10));
		}
	}
	
}
