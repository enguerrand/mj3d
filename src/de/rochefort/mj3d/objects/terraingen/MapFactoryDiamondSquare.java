package de.rochefort.mj3d.objects.terraingen;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.objects.MJ3DObject;
import de.rochefort.mj3d.objects.maps.MJ3DMap;
import de.rochefort.mj3d.objects.maps.MJ3DMapBuilder;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.terrains.EdgeType;
import de.rochefort.mj3d.objects.terrains.MJ3DDiamondSquareTerrain;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class MapFactoryDiamondSquare {
	public static MJ3DMap getMap(long seed, int detail, Color backgroundColor, boolean fogEffect, boolean wireframe, float ambientLight, int repeatCount, ColorScheme colorScheme){
		return MJ3DMapBuilder.newBuilder()
				.setFoggy(fogEffect)
				.setWireframe(wireframe)
				.setBackgroundColor(backgroundColor)
				.addObjects(getTerrains(seed, detail, ambientLight, repeatCount, colorScheme))
				.build();
		
		
	}

	private static List<MJ3DObject> getTerrains(long seed, int detail, float ambientLight, int repeatCountPerDirection, ColorScheme colorScheme) {
		List<MJ3DObject> objects = new ArrayList<MJ3DObject>();
		int sealevel = 100;
		int edgeLength = 1000;
		Color earthColor = new Color(10, 105, 10);
		int seaColorDeep=new Color(0, 0, 35).getRGB();
		int seaColorShallow=new Color(30, 30, 135).getRGB();
		MJ3DDiamondSquareTerrain[][] terrains = new MJ3DDiamondSquareTerrain[repeatCountPerDirection][repeatCountPerDirection];
		for(int i=0; i<repeatCountPerDirection; i++){
			for(int j=0; j<repeatCountPerDirection; j++){
				MJ3DDiamondSquareTerrain terrain = new MJ3DDiamondSquareTerrain(seed++, edgeLength, detail, 0.35f, 300, earthColor, sealevel, seaColorDeep, seaColorShallow,ambientLight, colorScheme);
				terrains[i][j]=terrain;
				if(i>0){
					List<MJ3DPoint3D> preseedList=terrains[i-1][j].getEdgePoints(EdgeType.SOUTH);
					for(int k=0; k<preseedList.size(); k++){
						int c = preseedList.get(k).getTerrainPointCol(terrains[i-1][j]);
						terrain.preSeed(0, c, preseedList.get(k).getOriginalZ());
					}
				}
				if(j>0){
					List<MJ3DPoint3D> preseedList=terrains[i][j-1].getEdgePoints(EdgeType.EAST);
					for(int k=0; k<preseedList.size(); k++){
						int r = preseedList.get(k).getTerrainPointRow(terrains[i][j-1]);
						terrain.preSeed(r, 0, preseedList.get(k).getOriginalZ());
					}
				}
				terrain.create(false);
				terrain.translate(new MJ3DVector(i*edgeLength,j*edgeLength,0));
				if(i>0){
					terrain.merge(terrains[i-1][j], EdgeType.NORTH, EdgeType.SOUTH);
				}
				if(j>0){
					terrain.merge(terrains[i][j-1], EdgeType.WEST, EdgeType.EAST);
				}
				objects.add(terrain);
			}
		}
		return objects;
	}
}

