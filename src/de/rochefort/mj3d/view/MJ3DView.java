package de.rochefort.mj3d.view;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

import de.rochefort.mj3d.objects.maps.MJ3DMap;

public class MJ3DView extends JPanel{
	private static final long serialVersionUID = 1L;
	private MJ3DCamera camera;
	private float ex=0;
	private float ey=0;
	private float ez=0;
	private int currentWidth=0;
	private int currentHeight=0;

	public MJ3DView(Component parentComponent,  MJ3DMap map, MJ3DCamera camera) {
		this.camera = camera;
		parentComponent.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent arg0) {}
			@Override
			public void componentResized(ComponentEvent arg0) {
				adjustViewPort();
			}
			@Override
			public void componentMoved(ComponentEvent arg0) {}
			@Override
			public void componentHidden(ComponentEvent arg0) {}
		});
		adjustViewPort();
	}
	private void adjustViewPort() {
		currentWidth = getVisibleRect().width;
		currentHeight = getVisibleRect().height;
		ey=-currentWidth/2f;
		ez=-currentHeight/2f;
		ex=-0.75f*(ez+ey); 
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		ZBuffer zBuffer = new ZBuffer(currentWidth, currentHeight);
		this.camera.paintImage(zBuffer, ex, ey, ez, currentWidth, currentHeight);
		g.drawImage(zBuffer.getBufferedImage(), 0, 0, null);
	}
	public void initialize() {
		adjustViewPort();
		
	}


}
