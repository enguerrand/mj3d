package de.rochefort.mj3d.math;

//http://www.cprogramming.com/tutorial/3d/quaternions.html
public class Quaternion {
	public static final float NORMALIZATION_LOWER_TOLERANCE = 1 - 1e-4f;
	public static final float NORMALIZATION_UPPER_TOLERANCE = 1 + 1e-4f;
	private float w = 1.0f;
	private float x = 0.0f;
	private float y = 0.0f;
	private float z = 0.0f;

	public Quaternion() {

	}
	
	public Quaternion(float w, float x, float y, float z) {
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Quaternion(float angle, MJ3DVector vector){
		float halfAngle = angle / 2;
		float sin = (float)Math.sin(halfAngle);
		this.w = (float)Math.cos(halfAngle);
		this.x = vector.getX()*sin;
		this.y = vector.getY()*sin;
		this.z = vector.getZ()*sin;
	}

	public boolean normalizeIfNeeded() {
		float sum = w * w + x * x + y * y + z * z;
		if (NORMALIZATION_LOWER_TOLERANCE < sum && sum < NORMALIZATION_UPPER_TOLERANCE) {
			return false;
		}
		float magnitude = (float)Math.sqrt(sum);
		w /= magnitude;
		x /= magnitude;
		y /= magnitude;
		z /= magnitude;
		return true;
	}

	public Quaternion multiply(Quaternion q2) {
		Quaternion result = new Quaternion();
		result.w = w * q2.w - x * q2.x - y * q2.y - z * q2.z;
		result.x = w * q2.x + x * q2.w + y * q2.z - z * q2.y;
		result.y = w * q2.y - x * q2.z + y * q2.w + z * q2.x;
		result.z = w * q2.z + x * q2.y - y * q2.x + z * q2.w;
		return result;
	}
	
	public Quaternion conjugate() {
		return new Quaternion(w, -x, -y, -z);
	}
	
	public Quaternion invert() {
		return new Quaternion(-w, x, y, z);
	}

	public float getW() {
		return w;
	}

	public void setW(float w) {
		this.w = w;
	}

	public float getX() {
		return x;
	}

	public void setX(float x) {
		this.x = x;
	}

	public float getY() {
		return y;
	}

	public void setY(float y) {
		this.y = y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	@Override
	public String toString() {
		return "Quaternion [w=" + w + ", x=" + x + ", y=" + y + ", z=" + z + "]";
	}
	
	public static void main(String[] args) {
//		Quaternion test1 = new Quaternion ( 0.3, 0.1, -2, 3.2323);
//		Quaternion test2 = new Quaternion ( -0.7560, 1.654, -2.666, -6.564);
//		test1.normalizeIfNeeded();
//		test2.normalizeIfNeeded();
//		
//		Quaternion test3 = test2.multiply(test2.invert()).multiply(test1);
//		Quaternion test4 = test1.multiply(test2.invert()).multiply(test2);
//		System.out.println(test1.equals(test3));
//		System.out.println(test1);
//		System.out.println(test3);
//		System.out.println(test4);
		System.out.println("### rotation test");
		Quaternion rotZ = new Quaternion((float)Math.PI/2, MJ3DVector.Z_UNIT_VECTOR);
		Quaternion rotX = new Quaternion((float)Math.PI/2, MJ3DVector.X_UNIT_VECTOR);
		MJ3DMatrix rotationMatrixZ = new MJ3DMatrix(rotZ);
		MJ3DMatrix rotationMatrixX = new MJ3DMatrix(rotX);
		System.out.println("Initial Local Coordinate System:                X:"+MJ3DVector.X_UNIT_VECTOR+" / Y:"+MJ3DVector.Y_UNIT_VECTOR+ " / Z:"+MJ3DVector.Z_UNIT_VECTOR);
		MJ3DVector localX = MJ3DVector.X_UNIT_VECTOR.rotate(rotationMatrixZ);
		MJ3DVector localY = MJ3DVector.Y_UNIT_VECTOR.rotate(rotationMatrixZ);
		MJ3DVector localZ = MJ3DVector.Z_UNIT_VECTOR.rotate(rotationMatrixZ);
		System.out.println("New Local Coordinate System after Yaw:          X:"+localX+" / Y:"+localY+ " / Z:"+localZ);
		localX = localX.rotate(rotationMatrixX);
		localY = localY.rotate(rotationMatrixX);
		localZ = localZ.rotate(rotationMatrixX);
		System.out.println("New Local Coordinate System after Yaw and Roll: X:"+localX+" / Y:"+localY+ " / Z:"+localZ);
	}
	
}
