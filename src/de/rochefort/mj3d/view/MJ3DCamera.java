package de.rochefort.mj3d.view;

import de.rochefort.mj3d.math.MJ3DMatrix;
import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.Quaternion;
import de.rochefort.mj3d.objects.maps.MJ3DMap;
import de.rochefort.mj3d.util.PerformanceTimer;

public class MJ3DCamera implements MJ3DViewingPosition {
	private MJ3DMap map;
	private MJ3DVector position = new MJ3DVector();
	private MJ3DVector localX = MJ3DVector.X_UNIT_VECTOR;
	private MJ3DVector localY = MJ3DVector.Y_UNIT_VECTOR;
	private MJ3DVector localZ = MJ3DVector.Z_UNIT_VECTOR;
	private int maxTriadCount = Integer.MAX_VALUE;
	private float maxTriadDistance = Float.MAX_VALUE;
	private float[] pointDistances;
	private float[] cachedPointTransformationsX;
	private float[] cachedPointTransformationsY;
	private float[] cachedPointTransformationsZ;
	private int[] cachedPointProjectionsX;
	private int[] cachedPointProjectionsY;

	private float tolerance = 1e-14f;
	private Quaternion orientation = new Quaternion();

	public MJ3DCamera() {
	}
	
	public MJ3DCamera(MJ3DMap map) {
		setMap(map);
	}

	public void setMap(MJ3DMap map) {
		this.map = map;
		int pointCount = map.getPointsCount(this);
		pointDistances = new float[pointCount];
		cachedPointTransformationsX = new float[pointCount]; 
		cachedPointTransformationsY = new float[pointCount]; 
		cachedPointTransformationsZ = new float[pointCount]; 
		cachedPointProjectionsX = new int[pointCount]; 
		cachedPointProjectionsY = new int[pointCount]; 
		clearCacheArrays();
		orientation.normalizeIfNeeded();
	}
	
	private void clearCacheArrays(){
		for(int i=0; i<map.getPointsCount(this); i++){
			pointDistances[i] 				= Float.MIN_VALUE;
			cachedPointTransformationsX[i] 	= Float.MIN_VALUE;
			cachedPointTransformationsY[i] 	= Float.MIN_VALUE;
			cachedPointTransformationsZ[i] 	= Float.MIN_VALUE;
			cachedPointProjectionsX[i] 		= Integer.MIN_VALUE;
			cachedPointProjectionsY[i] 		= Integer.MIN_VALUE;
		}
	}

	public void paintImage(ZBuffer zBuffer, float ex, float ey, float ez, int viewPortWidth, int viewPortHeight) {
		PerformanceTimer.start();
		MJ3DMatrix rotationMatrix = new MJ3DMatrix(orientation);

		map.update(this);
		MJ3DVector[] points = map.getPointsArray(this);
		int[][] triadPoints = map.getTriadPointsArray(this);
		int[] pointColors = map.getPointColorsArray(this);
		clearCacheArrays();
		zBuffer.setBackgroundColor(map.getBackgroundColor(this));
		
//		PerformanceTimer.stopInterimTime("Initial cleanup");
		recalculatePointDistances(points);

//		PerformanceTimer.stopInterimTime("Point Distance Precomputation");
		recalculatePointTransformations(points, rotationMatrix);
//		PerformanceTimer.stopInterimTime("Transforming points ");
		
		recalculate2DProjections(ex, ey, ez);
//		PerformanceTimer.stopInterimTime("Projecting points ");
		int[] x = new int[3];
		int[] y = new int[3];
		int[] c = new int[3];
		for(int triadPointIndexArray=0; triadPointIndexArray<triadPoints.length; triadPointIndexArray++) {
//			PerformanceTimer.stopInterimTime("Overhead in Triad Plotting Loop");
//			PerformanceTimer.stopInterimTime("resetting polygon");
			float [] distances = new float[3];
			int[] triadPts = triadPoints[triadPointIndexArray];
//			PerformanceTimer.stopInterimTime("Getting Triad Points");
			boolean triadInvisible=false;
			for(int triadPointIndex=0; triadPointIndex<triadPts.length; triadPointIndex++){
				int pointIndex = triadPts[triadPointIndex];
				if(pointDistances[pointIndex] > maxTriadDistance || cachedPointProjectionsX[pointIndex] == Integer.MIN_VALUE || cachedPointProjectionsY[pointIndex] == Integer.MIN_VALUE ){
					triadInvisible = true;
					break;
				}
				else{
					distances[triadPointIndex] = pointDistances[pointIndex];
					x[triadPointIndex] = cachedPointProjectionsX[pointIndex];
					y[triadPointIndex] = cachedPointProjectionsY[pointIndex];
					c[triadPointIndex] = pointColors[pointIndex];
				}
			}
//			PerformanceTimer.stopInterimTime("Getting Triad Point distances and filling polygon");
			if(triadInvisible){
//				PerformanceTimer.stopInterimTime("Check visibility");
				continue;
			}
//			PerformanceTimer.stopInterimTime("Check visibility");
			try {
//				for(int pc=0; pc<c.length; pc++){
//					Color dummy = new Color(c[pc]);
//					if(dummy.getGreen()==255)
//						System.out.println("rgb: "+dummy.getRed()+" "+dummy.getGreen()+" "+dummy.getBlue());
//				}
				zBuffer.fillTriad(x, y, c, map.getBackgroundColor(this), distances, maxTriadDistance, map.isFoggy(), map.isWireframe());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				break;
			}
//			PerformanceTimer.stopInterimTime("Paint z-buffer");
		}
		PerformanceTimer.stopAndPrintReport();
	}
	
//	public void paintImagePolygon(ZBuffer zBuffer, float ex, float ey, float ez, int viewPortWidth, int viewPortHeight) {
//		PerformanceTimer.start();
//		MJ3DMatrix rotationMatrix = new MJ3DMatrix(orientation);
//
//		MJ3DVector[] points = map.getPointsArray();
//		int[][] triadPoints = map.getTriadPointsArray();
//		int[] triadColors = map.getTriadColorsArray();
//		clearCacheArrays();
//		zBuffer.setBackgroundColor(map.getBackgroundColor(this));
//		
////		PerformanceTimer.stopInterimTime("Initial cleanup");
//		recalculatePointDistances(points);
//
////		PerformanceTimer.stopInterimTime("Point Distance Precomputation");
//		recalculatePointTransformations(points, rotationMatrix);
////		PerformanceTimer.stopInterimTime("Transforming points ");
//		
//		recalculate2DProjections(ex, ey, ez);
////		PerformanceTimer.stopInterimTime("Projecting points ");
//		Polygon polygon = new Polygon();
//		for(int triadPointIndexArray=0; triadPointIndexArray<triadPoints.length; triadPointIndexArray++) {
////			PerformanceTimer.stopInterimTime("Overhead in Triad Plotting Loop");
//			polygon.reset();
////			PerformanceTimer.stopInterimTime("resetting polygon");
//			float [] distances = new float[3];
//			int[] triadPts = triadPoints[triadPointIndexArray];
////			PerformanceTimer.stopInterimTime("Getting Triad Points");
//			boolean triadInvisible=false;
//			for(int triadPointIndex=0; triadPointIndex<triadPts.length; triadPointIndex++){
//				int pointIndex = triadPts[triadPointIndex];
//				if(pointDistances[pointIndex] > maxTriadDistance || cachedPointProjectionsX[pointIndex] == Integer.MIN_VALUE || cachedPointProjectionsY[pointIndex] == Integer.MIN_VALUE ){
//					triadInvisible = true;
//					break;
//				}
//				else{
//					distances[triadPointIndex] = pointDistances[pointIndex];
//					polygon.addPoint(cachedPointProjectionsX[pointIndex], cachedPointProjectionsY[pointIndex]);
//				}
//			}
////			PerformanceTimer.stopInterimTime("Getting Triad Point distances and filling polygon");
//			if(triadInvisible){
////				PerformanceTimer.stopInterimTime("Check visibility");
//				continue;
//			}
////			PerformanceTimer.stopInterimTime("Check visibility");
//			zBuffer.fillTriad(polygon, triadColors[triadPointIndexArray], map.getBackgroundColor(this), distances, maxTriadDistance, map.isFoggy());
////			PerformanceTimer.stopInterimTime("Paint z-buffer");
//		}
//		PerformanceTimer.stopAndPrintReport();
//	}
//		
	private void recalculatePointDistances(MJ3DVector[] points) {
		for(int i=0; i<pointDistances.length; i++){
			pointDistances[i]=points[i].add(position).getLength();
		}
	}
	
	private void recalculatePointTransformations(MJ3DVector[] points, MJ3DMatrix rotationMatrix) {
		for(int i=0; i<pointDistances.length; i++){
			if(pointDistances[i]>maxTriadDistance){
				continue;
			}
			MJ3DVector pntRotated = points[i].add(position.getX(), position.getY(), position.getZ()).rotate(rotationMatrix);
			cachedPointTransformationsX[i] = pntRotated.getX();
			cachedPointTransformationsY[i] = pntRotated.getY();
			cachedPointTransformationsZ[i] = pntRotated.getZ();
		}
	}
	
	private void recalculate2DProjections(float ex, float ey, float ez){
		for(int i=0; i<pointDistances.length; i++){
			if(pointDistances[i]>maxTriadDistance){
				continue;
			}
			float dx = cachedPointTransformationsX[i];
			if(dx < tolerance){
				continue;
			}
			float dy = cachedPointTransformationsY[i];
			float dz = cachedPointTransformationsZ[i];
			
			if (dx != 0) {
				cachedPointProjectionsX[i] = (int) (ex * dy / dx - ey);
				cachedPointProjectionsY[i] = (int) (ex * dz / dx - ez);
			} else {
				cachedPointProjectionsX[i] = (int) (dy - ey);
				cachedPointProjectionsY[i] = (int) (dz - ez);
			}
		}
		
	}

	public float getXPos() {
		return -position.getX();
	}

	public float getYPos() {
		return -position.getY();
	}

	public float getZPos() {
		return -position.getZ();
	}
	
	public void setPos(MJ3DVector position){
		this.position = position;
	}

	public void incrementX(float delta) {
		this.position = this.position.add(localX.multiply(-delta));
		handlePositionOverflow();
	}

	public void incrementY(float delta) {
		this.position = this.position.add(localY.multiply(-delta));
		handlePositionOverflow();
	}

	public void incrementZ(float delta) {
		this.position = this.position.add(localZ.multiply(-delta));
		handlePositionOverflow();
	}
	
	private void handlePositionOverflow(){
//		if(this.getXPos()>map.getMaxX()){
//			this.position.setX(-getXPos()-(map.getMinX()-map.getMaxX()));
//		}
//		else if(this.getXPos()<map.getMinX()){
//			this.position.setX(-getXPos()-(map.getMaxX()-map.getMinX()));
//		}
//		if(this.getYPos()>map.getMaxY()){
//			this.position.setY(-getYPos()-(map.getMinY()-map.getMaxY()));
//		}
//		else if(this.getYPos()<map.getMinY()){
//			this.position.setY(-getYPos()-(map.getMaxY()-map.getMinY()));
//		}
//		if(this.getZPos()>map.getMaxZ()){
//			this.position.setZ(-getZPos()-(map.getMinZ()-map.getMaxZ()));
//		}
//		else if(this.getZPos()<map.getMinZ()){
//			this.position.setZ(-getZPos()-(map.getMaxZ()-map.getMinZ()));
//		}
	}

	// y-Axis right
	public void incrementPitch(float delta) {
		Quaternion localQuaternion = new Quaternion(-delta, MJ3DVector.Y_UNIT_VECTOR);
		setOrientation(localQuaternion);
		updateLocalCoordinateSystem();
		
	}
	// x-Axis forward
	public void incrementRoll(float delta) {
//		System.out.println(delta);
		Quaternion localQuaternion = new Quaternion(-delta, MJ3DVector.X_UNIT_VECTOR);
		setOrientation(localQuaternion);
		updateLocalCoordinateSystem();
	}

	// z-Axis down
	public void incrementYaw(float delta) {
		Quaternion localQuaternion = new Quaternion(-delta, MJ3DVector.Z_UNIT_VECTOR);
		setOrientation(localQuaternion);
		updateLocalCoordinateSystem();
	}
	
	private void setOrientation(Quaternion globalQuaternion) {
		this.orientation = globalQuaternion.multiply(this.orientation);
		this.orientation.normalizeIfNeeded();
	}

	public float[][] getViewingDirection() {
		float[][] dir = new float[3][1];
		dir[2][0] = -1;
		return dir;
		// return MathUtils.getDirectionVector(rotationMatrix);
	}

	@Override
	public Quaternion getOrientation() {
		return orientation;
	}

	@Override
	public String toString() {
		return "MJ3DCamera [x=" + getXPos() + ", y=" + getYPos() + ", z=" + getZPos() + ", orientation=" + orientation + "]";
	}
	
	private void updateLocalCoordinateSystem(){
		MJ3DMatrix rotationMatrix = new MJ3DMatrix(orientation.conjugate());
		this.localX = MJ3DVector.X_UNIT_VECTOR.rotate(rotationMatrix);
		this.localY = MJ3DVector.Y_UNIT_VECTOR.rotate(rotationMatrix);
		this.localZ = MJ3DVector.Z_UNIT_VECTOR.rotate(rotationMatrix);
//		System.out.println("New Local Coordinate System: X:"+localX+" / Y:"+localY+ " / Z:"+localZ);
	}
	
	public void setMaxTriadCount(int maxTriadCount) {
		this.maxTriadCount = maxTriadCount;
	}
	
	public int getMaxTriadCount() {
		return maxTriadCount;
	}
	
	public void setMaxTriadDistance(float maxTriadDistance) {
		this.maxTriadDistance = maxTriadDistance;
	}
	
	public float getMaxTriadDistance() {
		return maxTriadDistance;
	}
}
