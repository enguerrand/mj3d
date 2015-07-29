package de.rochefort.mj3d.objects.terrains;

import java.util.List;

import de.rochefort.mj3d.objects.MJ3DObjectCompound;
import de.rochefort.mj3d.objects.primitives.MJ3DPoint3D;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;
import de.rochefort.mj3d.view.MJ3DViewingPosition;

public abstract class MJ3DTerrain implements MJ3DObjectCompound {

	public MJ3DTerrain() {
	}

	@Override
	public float getX() {
		return Float.MIN_VALUE;
	}

	@Override
	public float getY() {
		return Float.MIN_VALUE;
	}

	@Override
	public float getZ() {
		return Float.MIN_VALUE;
	}

	@Override
	public abstract List<MJ3DTriad> getTriads(MJ3DViewingPosition viewingPosition);
	
	public abstract int getPointsCount(MJ3DViewingPosition viewingPosition);

	public abstract void replace(MJ3DPoint3D pointToReplace, MJ3DPoint3D replacement);
	
	public abstract void create();

}
