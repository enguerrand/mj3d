package de.rochefort.mj3d.math;


public class MJ3DMatrix {
	private float[][] values;
	public MJ3DMatrix(int rowCount, int colCount) {
		values = new float[rowCount][colCount];
	}
	public MJ3DMatrix(Quaternion quaternionForRotationMatrix) {
		this(3,3);
		float w = quaternionForRotationMatrix.getW();
		float x = quaternionForRotationMatrix.getX();
		float y = quaternionForRotationMatrix.getY();
		float z = quaternionForRotationMatrix.getZ();
		float ww = w*w;
		float wx = w*x;
		float xx = x*x;
		float xy = x*y;
		float xz = x*z;
		float wy = w*y;
		float yy = y*y;
		float yz = y*z;
		float wz = w*z;
		float zz = z*z;
		
		values[0][0] = ww + xx - yy - zz;
		values[0][1] = 2 * xy - 2 * wz;
		values[0][2] = 2 * xz + 2 * wy;

		values[1][0] = 2 * xy + 2 * wz;
		values[1][1] = ww - xx + yy - zz;
		values[1][2] = 2 * yz - 2 * wx;

		values[2][0] = 2 * xz - 2 * wy;
		values[2][1] = 2 * yz + 2 * wx;
		values[2][2] = ww - xx - yy + zz;
	}
	
	public MJ3DVector multiply(MJ3DVector vector){
		float [][] vect = new float [3][1];
		vect[0][0] = vector.getX();
		vect[1][0] = vector.getY();
		vect[2][0] = vector.getZ();
		float [][] result = multiplyMatrices(values, vect);
		return new MJ3DVector(result[0][0], result[1][0], result[2][0]);
	}
	@Override
	public String toString() {
		StringBuffer  sb = new StringBuffer("MJ3DMatrix = ");
		for(int row = 0 ; row<values.length; row++){
			sb.append ("[ ");
			for(int col = 0 ; col<values[0].length; col++){
				sb.append(Double.toString(values[row][col]));
				if(col<values.length-1){
					sb.append(" | ");
				}
			}	
			sb.append("] ");
		}
		return sb.toString();
	}

	/**
	 * Matrix Multiplication
	 * 
	 * @param m1
	 *            Matrix 1
	 * @param m2
	 *            Matrix 2
	 * @return result Matirx
	 */
	public static float[][] multiplyMatrices(float[][] m1, float[][] m2) {
		float[][] result = null;

		if (m1[0].length == m2.length) {
			int rowCount1 = m1.length;
			int colCount1 = m1[0].length;
			int rowCount2 = m2[0].length;

			result = new float[rowCount1][rowCount2];

			for (int i = 0; i < rowCount1; i++) {
				for (int j = 0; j < rowCount2; j++) {
					result[i][j] = 0;
					for (int k = 0; k < colCount1; k++) {
						result[i][j] += m1[i][k] * m2[k][j];
					}
				}
			}
		} else {
			int rowCount = m1.length;
			int colCount = m1[0].length;

			result = new float[rowCount][colCount];
			for (int i = 0; i < m1.length; i++) {
				for (int j = 0; j < m1[0].length; j++) {
					result[i][j] = 0;
				}
			}
		}
		return result;
	}
	
}
