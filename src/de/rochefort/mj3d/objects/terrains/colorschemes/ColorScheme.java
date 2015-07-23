package de.rochefort.mj3d.objects.terrains.colorschemes;

import java.awt.Color;

public class ColorScheme {
	private Color earthColor;
	private Color seaColorDeep;
	private Color seaColorShallow;
	public ColorScheme(Color earthColor, Color seaColorDeep, Color seaColorShallow) {
		this.earthColor = earthColor;
		this.seaColorDeep = seaColorDeep;
		this.seaColorShallow = seaColorShallow;
	}
	public Color getEarthColor() {
		return earthColor;
	}
	public void setEarthColor(Color earthColor) {
		this.earthColor = earthColor;
	}
	public Color getSeaColorDeep() {
		return seaColorDeep;
	}
	public void setSeaColorDeep(Color seaColorDeep) {
		this.seaColorDeep = seaColorDeep;
	}
	public Color getSeaColorShallow() {
		return seaColorShallow;
	}
	public void setSeaColorShallow(Color seaColorShallow) {
		this.seaColorShallow = seaColorShallow;
	}
	
	public static ColorScheme newGrassAndBlueWaterScheme(){
		return new ColorScheme(
				new Color(10, 105, 10), 
				new Color(0, 0, 35), 
				new Color(30, 30, 135));
	}
	
	public static ColorScheme newArcticScheme(){
		return new ColorScheme(
				new Color(250, 250, 255), 
				new Color(10, 10, 14), 
				new Color(70, 70, 80));
	}
	
	public static ColorScheme newVolcanoColorScheme(){
		return new ColorScheme(
				new Color(50, 50, 50), 
				new Color(160, 180, 10),
				new Color(180, 40, 40)); 
	}
}
