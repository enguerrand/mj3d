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
//		ex=-1.1f*(ez+ey);
		ex=-0.7f*(ez+ey);  //FIXME only for testing. 
	}
	/**
	 * for testing
	 */
//	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);
//		ZBuffer zBuffer = new ZBuffer(currentWidth, currentHeight);
////		int[] xpoints = {0, 190, 190};
////		int[] ypoints = {100, 0, 200};
////		int[] xpoints = {0, 190, 200};
////		int[] ypoints = {100, 0, 150};
////		int[] xpoints = {0, 10, 200};
////		int[] ypoints = {100, 0, 100};
////		int[] xpoints = {1, 10, 200};//!
////		int[] ypoints = {1, 100, 1};
////		int[] xpoints = {-10, 10, 200};//!
////		int[] ypoints = {-10, 100, -10};
////		int[] xpoints = {-10, 10, 200};
////		int[] ypoints = {100, -10, 100};
////		int[] xpoints = {10, 10, 200};//!
////		int[] ypoints = {100, 10, 300};
////		int[] xpoints = {190, 200, 10};
////		int[] ypoints = {10, 10, 300};
////		int[] xpoints = {0, 0, 100};
////		int[] ypoints = {100, 0, 0}; 
////		int[] xpoints = {100, 0, 100};
////		int[] ypoints = {0, 100, 0}; 
////		int[] xpoints = {0, 100, 100};
////		int[] ypoints = {0, 0, 100}; 
//		int[] xpoints = { 120, 100,0};
//		int[] ypoints = { 100, 10  ,0}; 
////		int[] xpoints = {0, 100, 100};
////		int[] ypoints = {0, 100, 0}; 
////		int[] xpoints = {100, 0, 100};
////		int[] ypoints = {0, 0, 100}; 
//		Polygon p = new Polygon(xpoints, ypoints, 3);
//		float d1 = 100f;
//		float dPol =101f;
//		float maxDistance = 1000f;
//		
//		float[] distances1 = {d1,d1,d1};
//		float[] distancesPolygon = {dPol,dPol,dPol};
//		int[] colors1 = new int[3];
//		colors1[0] = Color.BLUE.getRGB();   // SOLLTE OBEN LINKS SEIN
//		colors1[1] = Color.GREEN.getRGB(); // SOLLTE UNTEN LINKS SEIN
//		colors1[2] = Color.BLUE.getRGB(); //SOLLTE UNTEN RECHTS SEIN
//		int color2 = Color.RED.getRGB();
//		int colorPolygon = Color.YELLOW.getRGB();
//		PerformanceTimer.start();
//		zBuffer.setBackgroundColor(Color.BLACK.getRGB());
//		zBuffer.fillTriad(xpoints, ypoints, colors1, 0, distances1, maxDistance,  false);
////		zBuffer.fillTriad(p, colorPolygon, 0, distancesPolygon, maxDistance,  false);
//		g.drawImage(zBuffer.getBufferedImage(), 0, 0, null);
//		PerformanceTimer.stopAndPrintReport();
//	}
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		ZBuffer zBuffer = new ZBuffer(currentWidth, currentHeight);
//		this.camera.paintImagePolygon(zBuffer, ex, ey, ez, currentWidth, currentHeight);
		this.camera.paintImage(zBuffer, ex, ey, ez, currentWidth, currentHeight);
		g.drawImage(zBuffer.getBufferedImage(), 0, 0, null);
	}
	public void initialize() {
//		this.addComponentListener(new ComponentListener() {
//			@Override
//			public void componentShown(ComponentEvent arg0) {}
//			@Override
//			public void componentResized(ComponentEvent arg0) {
//				adjustViewPort();
//			}
//			@Override
//			public void componentMoved(ComponentEvent arg0) {}
//			@Override
//			public void componentHidden(ComponentEvent arg0) {}
//		});
		adjustViewPort();
		
	}


}
