package de.rochefort.mj3d.view;

import java.awt.Color;

public class ColorBlender {

	@Deprecated
	public static int getBlendedRGB(Color sourceColor, Color blendColor, float blendRatio){
		int blendRed = blendColor.getRed();
		int blendGreen = blendColor.getGreen();
		int blendBlue = blendColor.getBlue();
		int srcRed   = sourceColor.getRed();
		int srcGreen = sourceColor.getGreen();
		int srcBlue  = sourceColor.getBlue();
		int newRed = blendRed + (int)(blendRatio*(srcRed-blendRed));
		int newGreen = blendGreen + (int)(blendRatio*(srcGreen-blendGreen));
		int newBlue  = blendBlue + (int)(blendRatio*(srcBlue -blendBlue ));
//		int test = new Color(newRed, newGreen, newBlue).getRGB();
		return (newBlue + (newGreen << 8) + (newRed << 16)) - 16777216;// + (255 << 24);
//		if(newRed >255 || newRed <0)
//			System.out.println("OrigRed = "+origRed+" bgRed = "+bgRed+" ratio = "+ratio+" newRed = "+newRed+"distance: "+distance+" maxDistance "+maxDistance);
		
	}
	
	/**
	 * Derived from http://tech-algorithm.com/articles/linear-interpolation/
	 * 
	 * Linear interpolation between two points.
	 * Return interpolated color Y at distance l.
	 * 
	 * @param colorA ARGB for point A.
	 * @param colorB ARGB for point B.
	 * @param ratio the ratio (0 for full A, 1.0 for full B)
	 * @return Interpolated color Y.
	 */
	public static int blendRGB(int colorA, int colorB, float ratio) {
	    // extract r, g, b information
	    // A and B is a ARGB-packed int so we use bit operation to extract
	    int Ar = (colorA >> 16) & 0xff ;
	    int Ag = (colorA >> 8) & 0xff ;
	    int Ab = colorA & 0xff ;
	    int Br = (colorB >> 16) & 0xff ;
	    int Bg = (colorB >> 8) & 0xff ;
	    int Bb = colorB & 0xff ;
	    // now calculate Y. convert float to avoid early rounding
	    // There are better ways but this is for clarity's sake
	    int Yr = (int)(Ar + ratio*(Br - Ar)) ;
	    int Yg = (int)(Ag + ratio*(Bg - Ag)) ;
	    int Yb = (int)(Ab + ratio*(Bb - Ab)) ;
	    // pack ARGB with hardcoded alpha
	    return 0xff000000 | // alpha
	            ((Yr << 16) & 0xff0000) |
	            ((Yg << 8) & 0xff00) |
	            (Yb & 0xff) ;
	}
	
	public static Color scaleColor(Color sourceColor, float scaleFactor){
		int newRed =   (int)(sourceColor.getRed()*scaleFactor);
		int newGreen = (int)(sourceColor.getGreen()*scaleFactor);
		int newBlue  = (int)(sourceColor.getBlue()*scaleFactor);
		return new Color(newRed, newGreen, newBlue);
	}

}
