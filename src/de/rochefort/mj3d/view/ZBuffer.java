package de.rochefort.mj3d.view;

import java.awt.image.BufferedImage;

public class ZBuffer {
	private final BufferedImage bufferedImage;
	private final int dataElements[];
	private final float[] distanceArray;
	private final int width;
	private final int height;
	public ZBuffer(int width, int height) {
		bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		dataElements = (int[])bufferedImage.getRaster().getDataElements(0, 0, width, height, null);
		distanceArray = new float[dataElements.length];
		java.util.Arrays.fill(distanceArray, Float.MAX_VALUE);
		this.width = width;
		this.height = height;
	}
	
	public BufferedImage getBufferedImage() {
		bufferedImage.getRaster().setDataElements(0, 0, width, height, dataElements);
		return bufferedImage;
	}
	
//	public void fillTriad(Polygon polygon, int color, int bgColor, float[] distance, float maxDistance, boolean fogEffect){
//		int rgb;
//		if(fogEffect){
//			rgb = getColorWithFogEffect(color, bgColor, distance[0], maxDistance);
//		}
//		else {
//			rgb = color;
//		}
//		Rectangle rect = polygon.getBounds();
//		int minX = Math.max(0,(int)rect.getMinX());
//		int minY = Math.max(0,(int)rect.getMinY());
//		int maxX = Math.min(width-1,(int)rect.getMaxX());
//		int maxY = Math.min(height-1,(int)rect.getMaxY());
//		for(int x=minX ; x<=maxX; x++){
//			for(int y= minY; y<=maxY; y++){
//				int index = y*(width)+x;
//				if (distanceArray[index] <= distance[0]){
//					continue;
//				}
//				if(polygon.contains(x, y)){
//					distanceArray[index]=distance[0];
//					dataElements[index] = rgb;
//				}
//			}
//		}
//	}

	/**
	 * Based on http://www.sunshine2k.de/coding/java/TriangleRasterization/TriangleRasterization.html
	 * @param x
	 * @param y
	 * @param pointColors
	 * @param bgColor
	 * @param distance
	 * @param maxDistance
	 * @param fogEffect
	 */
	public void fillTriad(int[]x, int[]y, int[] pointColors, int bgColor, float[] distance, float maxDistance, boolean fogEffect){
//		PerformanceTimer.stopInterimTime("calling to filling Triad");
//		for(int pc=0; pc<pointColors.length; pc++){
//			Color dummy = new Color(pointColors[pc]);
//			if(dummy.getGreen()==255)
//				System.out.println("rgb: "+dummy.getRed()+" "+dummy.getGreen()+" "+dummy.getBlue());
//		}
		int rgb;
		int indexTop = Integer.MIN_VALUE;
		int indexBottom = Integer.MIN_VALUE;
		int indexMid = Integer.MIN_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		for (int i = 0; i < 3; i++) {
			if(x[i]>maxX){
				maxX = x[i];
			}
			if(y[i]>maxY){
				maxY = y[i];
				indexBottom = i;
			}
			else if(y[i]==maxY){
				if(indexBottom<0)
					System.out.println("Komisch");
				if(x[i]>x[indexBottom]){
					maxY = y[i];
					indexBottom = i;
				}
			}
			if(x[i]<minX){
				minX = x[i];
			}
			if(y[i]<minY){
				minY = y[i];
			}
		}
		if(maxX<0 || maxY<0 || minX > width-1 || minY > height -1)
			return;
		
		switch (indexBottom){
			case 0:{
				if(y[1]<y[2]){
					indexTop=1;
					indexMid=2;
				}
				else if(y[1]>y[2]){
					indexMid=1;
					indexTop=2;
				}
				else if(x[1]>x[2]){
					indexTop=2;
					indexMid=1;
				}
				else{
					indexTop=1;
					indexMid=2;
				}
				break;
			}
			case 1:{			
				if(y[0]<y[2]){
					indexMid=2;
					indexTop=0;
				}
				else if(y[0]>y[2]){
					indexMid=0;
					indexTop=2;
				}
				else if(x[0]>x[2]){
					indexTop=2;
					indexMid=0;
				}
				else{
					indexTop=0;
					indexMid=2;
				}
				break;
			}
			case 2:{
				if(y[0]<y[1]){	
					indexMid=1;
					indexTop=0;
				}
				else if(y[0]>y[1]){
					indexMid=0;
					indexTop=1;
				}
				else if(x[0]>x[1]){
					indexTop=1;
					indexMid=0;
				}
				else{
					indexTop=0;
					indexMid=1;
				}
				break;
			}
		}
		for(int pc=0; pc<pointColors.length; pc++){
			pointColors[pc] = getColorWithFogEffect(pointColors[pc], bgColor, distance[pc], maxDistance);
		}
		
		if(indexTop < 0 || indexBottom<0 || indexMid<0){
			System.out.println("Indices: "+indexTop+" "+indexMid+" "+indexBottom);
		}
		if(y[indexTop] == y[indexBottom]){
			int x1 = (int)Math.max(0, Math.min(x[0], Math.min(x[0], x[2])));
			int x2 = (int)Math.min(width-1, Math.max(x[0], Math.max(x[0], x[2])));
			drawHorizontalLine(x1, x2, y[indexBottom], pointColors[indexTop], pointColors[indexBottom], bgColor, distance[indexTop], distance[indexBottom], maxDistance);
		}
		else if (y[indexMid] == y[indexBottom]){
		  fillBottomFlatTriangle(x[indexTop], x[indexMid], x[indexBottom], y[indexTop], y[indexMid], y[indexBottom], pointColors[indexTop], pointColors[indexMid], pointColors[indexBottom], bgColor, distance[indexTop], distance[indexMid], distance[indexBottom], maxDistance);
		}
		else if (y[indexMid] == y[indexTop]){
		  fillTopFlatTriangle(x[indexTop],x[indexMid] , x[indexBottom], y[indexTop], y[indexMid], y[indexBottom], pointColors[indexTop], pointColors[indexMid], pointColors[indexBottom], bgColor, distance[indexTop], distance[indexMid], distance[indexBottom], maxDistance);
		}
		else{
			float ratio = ((float)(y[indexMid] - y[indexTop]) / (float)(y[indexBottom] - y[indexTop]));
			int X4 = (int)(x[indexTop] + ratio * (x[indexBottom] - x[indexTop]));
			int Y4 = y[indexMid];
			if(X4>x[indexMid])
				X4++;
			float dist4 = (distance[indexTop] + ratio * (distance[indexBottom] - distance[indexTop]));
			int color4 = ColorBlender.blendRGB(pointColors[indexTop], pointColors[indexBottom], ratio);
//			dataElements[getDataElementsIndex(X4, Y4)] = 0;
//			distanceArray[getDataElementsIndex(X4, Y4)] = 0;
			if(X4>x[indexMid]){
				fillBottomFlatTriangle(x[indexTop], x[indexMid], X4, y[indexTop], y[indexMid], Y4, pointColors[indexTop], pointColors[indexMid], color4, bgColor, distance[indexTop], distance[indexMid], dist4, maxDistance);
				fillTopFlatTriangle(x[indexMid], X4, x[indexBottom], y[indexMid], Y4, y[indexBottom], pointColors[indexMid], color4, pointColors[indexBottom], bgColor, distance[indexMid], dist4, distance[indexBottom], maxDistance);
			}
			else{
				fillBottomFlatTriangle(x[indexTop], X4, x[indexMid], y[indexTop], Y4, y[indexMid], pointColors[indexTop], color4, pointColors[indexMid], bgColor, distance[indexTop], dist4, distance[indexMid], maxDistance);
				fillTopFlatTriangle(X4, x[indexMid], x[indexBottom], Y4, y[indexMid], y[indexBottom], color4, pointColors[indexMid], pointColors[indexBottom], bgColor, dist4, distance[indexMid], distance[indexBottom], maxDistance);
			}
		}
//		PerformanceTimer.stopInterimTime("filling Triad");
	}

	private void fillBottomFlatTriangle(int x1, int x2, int x3, int y1, int y2, int y3, int color1, int color2, int color3, int bgColor, float distance1, float distance2, float distance3, float maxDistance){
//		if(y1==y2 || y3 == y1){
//		System.out.println("gleiche y werte: "+y1+" "+y2+" "+y3);
//			return;
//		}
		float ratio1 = 1f /(float)(y2 - y1);
		float ratio2 = 1f /(float)(y3 - y1);
		
		float invslope1 = (float)(x2 - x1) * ratio1 ;
		float invslope2 = (float)(x3 - x1) * ratio2 ;
		
		float distSlope1 = (distance2-distance1) * ratio1 ;
		float distSlope2 = (distance3-distance1) * ratio2 ;
		
		float color1Slope = 0;
		float color2Slope = 0;
		
		float curx1 = x1;
		float curx2 = x1;
		y2=Math.min(y2, height-1);
		float dist1 = distance1;
		float dist2 = distance1;
		for (int scanlineY = y1; scanlineY <= y2; scanlineY++){
			if(scanlineY>=0){
				int c1=ColorBlender.blendRGB(color1, color2, color1Slope);
				int c2=ColorBlender.blendRGB(color1, color3, color2Slope);
//				if(scanlineY ==y2){
//					System.out.println("Scanline in bottom flat color2 (left): "+new Color(color2));
//					System.out.println("Scanline in bottom flat color3 (right): "+new Color(color3));
//					System.out.println("Scanline in bottom flat c1: "+new Color(c1));
//					System.out.println("Scanline in bottom flat c2: "+new Color(c2));
//					System.out.println("Scanline in bottom flat c1 input: "+new Color(color1)+" -> "+new Color(color2)+" with slope "+color1Slope);
//					System.out.println("Scanline in bottom flat c2 input: "+new Color(color1)+" -> "+new Color(color3)+" with slope "+color2Slope);
//				}
				drawHorizontalLine((int)curx1, (int)curx2, scanlineY, c1, c2, bgColor, dist1, dist2, maxDistance);
			}
			curx1 += invslope1;
			curx2 += invslope2;
			dist1 += distSlope1;
			dist2 += distSlope2; 
			color1Slope += ratio1;
			color2Slope += ratio2;
		}
	 }
	private void fillTopFlatTriangle(int x1, int x2, int x3, int y1, int y2, int y3, int color1, int color2, int color3, int bgColor, float distance1, float distance2, float distance3, float maxDistance){
//		if(y3==y1 || y3 == y2){
//			System.out.println("gleiche y werte: "+y1+" "+y2+" "+y3);
//			return;
//		}
		float ratio1 = 1f / (float)(y3 - y1);
		float ratio2 = 1f / (float)(y3 - y2);
		
		float invslope1 = (float)(x3 - x1) * ratio1;
		float invslope2 = (float)(x3 - x2) * ratio2;
		
		float curx1 = x3+0.5f;
		float curx2 = x3+0.5f;
		
		float distSlope1 = (distance3-distance1) * ratio1;
		float distSlope2 = (distance3-distance2) * ratio2;
		
		float color1Slope = 1f;
		float color2Slope = 1f;
		
		y1=Math.max(0, y1);
		float dist1 = distance3;
		float dist2 = distance3;
//		y3=Math.min(y3, height-1);
		for (int scanlineY = y3-1; scanlineY >= y1; scanlineY--){
			curx1 -= invslope1;
			curx2 -= invslope2;
			dist1 -= distSlope1;
			dist2 -= distSlope2;
			color1Slope -= ratio1;
			color2Slope -= ratio2;
			if(scanlineY>=height)
				continue;
			int c1=ColorBlender.blendRGB(color1, color3, color1Slope);
			int c2=ColorBlender.blendRGB(color2, color3, color2Slope);
//			if(scanlineY ==y1){
//				System.out.println("Scanline in top    flat color1 (color left): "+color1);
//				System.out.println("Scanline in top    flat color2 (color right): "+color2);
//				System.out.println("Scanline in top    flat c1: "+c1);
//				System.out.println("Scanline in top    flat c2: "+c2);
//				System.out.println("Scanline in top    flat c1 input: "+color1+" -> "+color3+" with slope "+color1Slope);
//				System.out.println("Scanline in top    flat c2 input: "+color2+" -> "+color3+" with slope "+color2Slope);
//			}
			drawHorizontalLine((int)curx1, (int)curx2, scanlineY, c1, c2, bgColor, dist1, dist2, maxDistance);
		}
	}
	
	private void drawHorizontalLine(int x1, int x2, int y, int color1, int color2, int bgColor, float distance1, float distance2, float maxDistance) {
		if(y<0)
			return;
		if(y>height-1)
			return;
		if(x1==x2 && x1>=0 && x1<width){
			int index = getDataElementsIndex(x1, y);
			if(distanceArray[index]<=distance1)
				return;
			distanceArray[index]=distance1;
			dataElements[index]=color1; 
			return;
		}
		int xmin;
		int xmax;
		float dmin;
		float dmax;
		int cMin;
		int cMax;
		if(x1<x2){
			xmin=Math.max(0,x1);
			xmax=Math.min(width-1, x2);
			dmin=distance1;
			dmax=distance2;
			cMin = color1;
			cMax = color2;
		} else if(x2<x1){
			xmin=Math.max(0,x2);;
			xmax=Math.min(width-1, x1);
			dmin=distance2;
			dmax=distance1;
			cMin = color2;
			cMax = color1;
		}
		else{
			return;
		}
		
		float xInv = 1f/(float)(xmax-xmin);
		float distanceSlope = (dmax-dmin)*xInv;
		float colorRatio = 0;
		
		float distance = dmin;
		for(int x = xmin; x <= xmax; x++){
			int index = getDataElementsIndex(x, y);
//			if(index > dataElements.length-1)
//				continue;
			if(distanceArray[index]>distance){
				distanceArray[index]=distance;
				int color = ColorBlender.blendRGB(color1, color2, colorRatio);
				dataElements[index]=color; 
			}
			distance+=distanceSlope;
			colorRatio+=xInv;
		}
	}
	
	private int getDataElementsIndex(int x, int y){
//		if(y==0 || x== 0){
//			System.out.println("x/y="+x+" "+y);
//		}
		return x+y*(width);
	}

	public void setBackgroundColor(int backgroundColor) {
		for(int i=0; i<dataElements.length; i++){
			dataElements[i]=backgroundColor;
		}
	}
	
	
	private int getColorWithFogEffect(int originalColor, int bgColor, float distance, float maxDistance){
		float ratio = (maxDistance-distance)/maxDistance;
		return  ColorBlender.blendRGB(bgColor, originalColor, ratio);
	}
}
