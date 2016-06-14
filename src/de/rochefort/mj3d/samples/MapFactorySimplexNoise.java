package de.rochefort.mj3d.samples;

import de.rochefort.mj3d.math.randomness.FractalNoiseConfig;
import de.rochefort.mj3d.objects.maps.MJ3DMap;
import de.rochefort.mj3d.objects.maps.MJ3DMapBuilder;
import de.rochefort.mj3d.objects.terrains.MJ3DInfiniteSimplexNoiseTerrain;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.Color;

public class MapFactorySimplexNoise {
	public static MJ3DMap getMap(long seed, MJ3DViewingPosition cameraPosition, int width, float triadSize, FractalNoiseConfig config, Color backgroundColor, boolean fogEffect, boolean wireframe, float ambientLight, float seaLevel, ColorScheme terrainColorScheme){
		return MJ3DMapBuilder.newBuilder()
				.setFoggy(fogEffect)
				.setWireframe(wireframe)
				.setBackgroundColor(backgroundColor)
//				.addObjects(getStaticTerrains(seed, cameraPosition, width, triadSize, baseFrequency, baseAmplitude, persistence, ambientLight, seaLevel, terrainColorScheme))
				.setDynamicTerrain(getDynamicTerrain(seed, cameraPosition, width, triadSize, config, ambientLight, seaLevel, terrainColorScheme))
				.build();
		
		
	}

	private static MJ3DInfiniteSimplexNoiseTerrain getDynamicTerrain(long seed, MJ3DViewingPosition cameraPosition, int width, float triadSize, FractalNoiseConfig config, float ambientLight, float sealevel, ColorScheme colorScheme) {
		MJ3DInfiniteSimplexNoiseTerrain terrain = new MJ3DInfiniteSimplexNoiseTerrain(cameraPosition, seed, (width-1)*triadSize*0.5f, triadSize, config, sealevel, ambientLight, colorScheme);
		terrain.create();
		return terrain;
	}
}

