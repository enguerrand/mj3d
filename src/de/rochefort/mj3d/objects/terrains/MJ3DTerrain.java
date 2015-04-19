package de.rochefort.mj3d.objects.terrains;

import java.util.List;

import de.rochefort.mj3d.objects.MJ3DObjectCompound;
import de.rochefort.mj3d.objects.primitives.MJ3DTriad;

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
	public abstract List<MJ3DTriad> getTriads();
	
	public abstract int getPointsCount();

}
