package de.rochefort.mj3d.math;


@Deprecated
public class MathUtils {

	public MathUtils() {
	}

	
	public static long getNextPrime(long previousPrime, long maxCycleCount){
		while(!isPrime(++previousPrime) && maxCycleCount > 0){
			maxCycleCount--;
		}
		return previousPrime;
	}
	
	private static boolean isPrime(long n) {
	    //check if n is a multiple of 2
	    if (n%2==0) return false;
	    //if not, then just check the odds
	    for(int i=3;i*i<=n;i+=2) {
	        if(n%i==0)
	            return false;
	    }
	    return true;
	}
	
//	public static float randomFloat(float min, float max, long seed){							// create random float between min and max
//		if(min == max){
//			return min;
//		}
//		if(min>max){
//			throw new IllegalArgumentException("Error on invokation of randomDouble: Min value "+min +" is larger than max value "+max+"!");
//		}
//		byte[] value = new byte[1];
//		Random r = new Random();
//		value[0]=(byte)(seed+r.nextDouble());
//		SecureRandom rs = new SecureRandom(value);
//		float result = min + Math.abs(rs.nextFloat()) * (max-min);		// TODO check correct max
//	//	System.out.println("m.randomNrRepr: returning "+result);
//		return result;
//	}

	// http://en.wikipedia.org/wiki/Perspective_transform
//	public static MJ3DPoint2D projectPoint(float pointX, float pointY, float pointZ, float cameraX, float cameraY, float cameraZ, float yaw, float pitch, float roll, float ex, float ey, float ez) {
//		if (cameraX == 0 && cameraY == 0 && cameraZ == 0 && yaw == 0 && pitch == 0 && roll == 0) {
//			result[0] = (int)pointX;
//			result[1] = (int)pointY;
//			return result;
//		}
//		float dx = 0;
//		float dy = 0;
//		float dz = 0;
//		float x = pointX - cameraX;
//		float y = pointY - cameraY;
//		float z = pointZ - cameraZ;
//		if (yaw == 0 && pitch == 0 && roll == 0) {
//			dx = x;
//			dy = y;
//			dz = z;
//		} else {
//			float sx = Math.sin(pitch);
//			float cx = Math.cos(pitch);
//			float sy = Math.sin(yaw);
//			float cy = Math.cos(yaw);
//			float sz = Math.sin(roll);
//			float cz = Math.cos(roll);
//			dx = cy * (sz * y + cz * x) - sy * z;
//			dy = sx * (cy * z + sy * (sz * y + cz * x)) + cx * (cz * y - sz * x);
//			dz = cx * (cy * z + sy * (sz * y + cz * x)) - sx * (cz * y - sz * x);
//		}
//		return new MJ3DPoint2D((int)(ez*dx/dz-ex), (int)(ez*dy/dz-ey));
//	}
//	
//	public static float [][] getRotationMatrix(float yaw, float pitch, float roll){
//		float[][] rm = new float[3][3];
//		if(yaw == 0 && pitch == 0 && roll == 0){
//			rm[0][0] = 1;
//			rm[1][1] = 1;
//			rm[2][2] = 1;
//			return rm;
//		}
//		float c1 = Math.cos(yaw);
//		float c2 = Math.cos(pitch);
//		float c3 = Math.cos(roll);
//		float s1 = Math.sin(yaw);
//		float s2 = Math.sin(pitch);
//		float s3 = Math.sin(roll);
//		rm[0][0] = c2*c3;
//		rm[0][1] = -c2*s3;
//		rm[0][2] = s2;
//		rm[1][0] = c1*s3+c3*s1*s2;
//		rm[1][1] = c1*c3-s1*s2*s3;
//		rm[1][2] = -c2*s1;
//		rm[2][0] = s1*s3-c1*c3*s2;
//		rm[2][1] = c3*s1+c1*s2*s3;
//		rm[2][2] = c1*c2;
//		return rm;
//	}
//	
//	public static float[][] getDirectionVector(float[][] rotationMatrix){
//		float[][] unitVector = new float[3][1];
//		unitVector[2][0]=1;
//		float[][] dirVect = multiplyMatrices(rotationMatrix, unitVector);
//		return dirVect;
//	}
//	
//	static public float dotProd(float vectorA[], float vectorB[]) {
//		float result;
//		result = vectorA[0] * vectorB[0] + vectorA[1] * vectorB[1] + vectorA[2] * vectorB[2];
//		return result;
//	}
}
