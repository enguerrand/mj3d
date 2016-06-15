package de.rochefort.mj3d.samples;

import de.rochefort.mj3d.math.MJ3DSphere;
import de.rochefort.mj3d.math.randomness.FractalNoiseConfig;
import de.rochefort.mj3d.objects.maps.MJ3DMap;
import de.rochefort.mj3d.objects.maps.MJ3DMapBuilder;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.terrains.MJ3DSimplexNoisePlanetPolar;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

import java.awt.Color;

public class MapFactorySimplexPlanetPolar {
	public static MJ3DMap getMap(long seed, MJ3DViewingPosition cameraPosition, float radius, float triadSize, FractalNoiseConfig config, Color backgroundColor, boolean fogEffect, boolean wireframe, float ambientLight, float seaLevel, ColorScheme terrainColorScheme){
		return MJ3DMapBuilder.newBuilder()
				.setFoggy(fogEffect)
				.setWireframe(wireframe)
				.setBackgroundColor(backgroundColor)
//				.addObjects(getStaticTerrains(seed, cameraPosition, width, triadSize, baseFrequency, baseAmplitude, persistence, ambientLight, seaLevel, terrainColorScheme))
				.setDynamicTerrain(getDynamicTerrain(seed, cameraPosition, radius, triadSize, config, ambientLight, seaLevel, terrainColorScheme))
				.build();
		
		
	}

	private static MJ3DSimplexNoisePlanetPolar getDynamicTerrain(long seed, MJ3DViewingPosition cameraPosition, float radius, float triadSize, FractalNoiseConfig config, float ambientLight, float sealevel, ColorScheme colorScheme) {
		MJ3DSphere planetBaseShape = new MJ3DSphere(new MJ3DPoint3D(0, 0, 0), radius);
		MJ3DSimplexNoisePlanetPolar terrain = new MJ3DSimplexNoisePlanetPolar(cameraPosition, seed, radius * 3, triadSize, config, sealevel, ambientLight, colorScheme, planetBaseShape);
		terrain.create();
		return terrain;
	}
}

