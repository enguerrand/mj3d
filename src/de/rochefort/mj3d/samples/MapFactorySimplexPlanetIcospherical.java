package de.rochefort.mj3d.samples;

import de.rochefort.mj3d.math.randomness.FractalNoiseConfig;
import de.rochefort.mj3d.objects.maps.MJ3DMap;
import de.rochefort.mj3d.objects.maps.MJ3DMapBuilder;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.terrains.MJ3DSimplexNoisePlanetIcospherical;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.Color;

public class MapFactorySimplexPlanetIcospherical {
	public static MJ3DMap getMap(long seed, MJ3DViewingPosition cameraPosition, float radius, float triadSize, FractalNoiseConfig config, Color backgroundColor, boolean fogEffect, boolean wireframe, float ambientLight, float seaLevel, ColorScheme terrainColorScheme){
		return MJ3DMapBuilder.newBuilder()
				.setFoggy(fogEffect)
				.setWireframe(wireframe)
				.setBackgroundColor(backgroundColor)
				.setDynamicTerrain(getDynamicTerrain(seed, cameraPosition, radius, triadSize, config, ambientLight, seaLevel, terrainColorScheme, new MJ3DPoint3D(0,0,0)))
				.build();
		
		
	}

	private static MJ3DSimplexNoisePlanetIcospherical getDynamicTerrain(long seed, MJ3DViewingPosition cameraPosition, float radius, float triadSize, FractalNoiseConfig config, float ambientLight, float sealevel, ColorScheme colorScheme, MJ3DPoint3D center) {
        MJ3DSimplexNoisePlanetIcospherical terrain = new MJ3DSimplexNoisePlanetIcospherical(cameraPosition, 0L, config, 0, ambientLight, colorScheme, radius, center, 10);
		terrain.create();
		return terrain;
	}
}

