package de.rochefort.mj3d.objects.maps;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import java.awt.Color;

import de.rochefort.mj3d.objects.MJ3DObject;

public class MJ3DMapBuilder {
	private final AtomicBoolean finalized = new AtomicBoolean(false);
	private final List<MJ3DObject> mj3dObjects = new LinkedList<MJ3DObject>();
	private boolean foggy = true;
	private boolean wireframe = false;
	private Color backgroundColor = Color.BLACK;
	
	private MJ3DMapBuilder(){
		
	}

	public static MJ3DMapBuilder newBuilder(){
		return new MJ3DMapBuilder();
	}
	
	public MJ3DMapBuilder setFoggy(boolean foggy){
		this.foggy = foggy;
		return this;
	}
	
	public MJ3DMapBuilder setWireframe(boolean wireframe){
		this.wireframe = wireframe;
		return this;
	}
	
	public MJ3DMapBuilder setBackgroundColor(Color backgroundColor){
		this.backgroundColor = backgroundColor;
		return this;
	}
	
	public MJ3DMapBuilder addObject(MJ3DObject mj3dObject){
		mj3dObjects.add(mj3dObject);
		return this;
	}
	
	public MJ3DMapBuilder addObjects(Collection<MJ3DObject> mj3dObject){
		mj3dObjects.addAll(mj3dObject);
		return this;
	}
	
	public MJ3DMap build() {
		if(!finalized.compareAndSet(false, true)){
			throw new IllegalStateException("MJ3DMapBuilder: build() Method may only be called once!");
		}
		return new MJ3DMapImpl(mj3dObjects, backgroundColor.getRGB(), foggy, wireframe);
	}
}
