package de.rochefort.mj3d.samples;

import de.rochefort.mj3d.math.MJ3DVector;
import de.rochefort.mj3d.math.randomness.FractalNoiseConfig;
import de.rochefort.mj3d.objects.maps.MJ3DMap;
import de.rochefort.mj3d.objects.terraingen.MapFactoryDiamondSquare;
import de.rochefort.mj3d.objects.terraingen.MapFactorySimplexNoise;
import de.rochefort.mj3d.objects.terraingen.MapFactorySimplexPlanetIcospherical;
import de.rochefort.mj3d.objects.terraingen.MapFactorySimplexPlanetPolar;
import de.rochefort.mj3d.objects.terrains.colorschemes.ColorScheme;
import de.rochefort.mj3d.view.MJ3DCamera;
import de.rochefort.mj3d.view.MJ3DView;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
	
	public class MainWindow extends JFrame {
        private enum SampleType {
            DIAMOND_SQUARE, SIMPLEX_NOISE, SIMPLEX_PLANET_POLAR, SIMPLEX_PLANET_ICOSPHERICAL
        }
		private static final long serialVersionUID = 1L;
		private static final float DELTA_TRANS=200;
		private static final float DELTA_ROT=(float)Math.PI/20.0f;
		private MJ3DView view;
		private MJ3DCamera camera;
	
		public MainWindow() {
			super("3D World");
			this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			Dimension screenSize = getToolkit().getScreenSize();
			setLocation(new Point((int) (screenSize.width * 0.2),
					(int) (screenSize.height * 0.1)));
			setSize(500,500);
			loadViewerDesign(SampleType.SIMPLEX_PLANET_ICOSPHERICAL);
		 	addKeyboardShortCuts();
		 	this.setVisible(true);
			view.initialize();
			this.validate();
		 	this.repaint();
		}
	
		private void loadViewerDesign(SampleType sampleType) {
			JPanel viewerMainPanel = new JPanel();
	
			viewerMainPanel.setLayout(new GridLayout(1, 1));
			boolean fog = false;
			boolean wireframe = true;
			Color backgroundColor = new Color(170,185,215);
			float ambientLight = 0.2f;
			float visibility = 50_000;
//			ColorScheme terrainColorScheme = ColorScheme.newDesertScheme();
			ColorScheme terrainColorScheme = ColorScheme.newGrassAndBlueWaterScheme();

			this.camera = new MJ3DCamera();

			MJ3DMap map;
            switch (sampleType){
                case DIAMOND_SQUARE:
        			this.camera.setPos(new MJ3DVector(0f,0f,500f));
                    map = getDiamondSquareMap(fog, wireframe, backgroundColor, ambientLight, terrainColorScheme);
                    break;
                case SIMPLEX_NOISE:
        			this.camera.setPos(new MJ3DVector(0f,0f,500f));
        			visibility = 5_000;
                    map = getSimplexNoiseMap(fog, wireframe, backgroundColor, ambientLight, visibility, terrainColorScheme);
                    break;
                case SIMPLEX_PLANET_POLAR:
                    map = getSimplexPlanetMapPolar(fog, wireframe, backgroundColor, ambientLight, terrainColorScheme);
                    break;
				case SIMPLEX_PLANET_ICOSPHERICAL:
                    map = getSimplexPlanetMapIcospherical(fog, wireframe, backgroundColor, ambientLight, terrainColorScheme);
                    break;
                default:
                    throw new IllegalStateException("Unknown SampleType "+sampleType);
            }
			this.camera.setMap(map);

			this.camera.setMaxTriadDistance(visibility);
			view = new MJ3DView(this, map, camera);
			viewerMainPanel.add(view); // put viewer on main panel
			this.setContentPane(viewerMainPanel);
			viewerMainPanel.requestFocusInWindow();
		}

        private MJ3DMap getDiamondSquareMap(boolean fog, boolean wireframe, Color backgroundColor, float ambientLight, ColorScheme terrainColorScheme) {
            MJ3DMap map;
            long seed=8;
            int detail = 6;
            int repeatCount = 5;
            map = MapFactoryDiamondSquare.getMap(seed, detail, backgroundColor, fog, wireframe, ambientLight, repeatCount, terrainColorScheme);
            return map;
        }

        private MJ3DMap getSimplexNoiseMap(boolean fog, boolean wireframe, Color backgroundColor, float ambientLight, float visibility, ColorScheme terrainColorScheme) {
            MJ3DMap map;
            long seed=700000;
            float triadSize = 100;
            float seaLevel = 100;
            int width = (int)(2*visibility/triadSize)+1;
            FractalNoiseConfig config = new FractalNoiseConfig(1500, 500, 500, 100);
            map = MapFactorySimplexNoise.getMap(seed, this.camera, width, triadSize, config, backgroundColor, fog, wireframe, ambientLight, seaLevel, terrainColorScheme);
            return map;
        }

        private MJ3DMap getSimplexPlanetMapPolar(boolean fog, boolean wireframe, Color backgroundColor, float ambientLight, ColorScheme terrainColorScheme) {
            MJ3DMap map;
            long seed=700000;
            float triadSize = 100;
            float seaLevel = 500;
            float radius = 15000;
//				FractalNoiseConfig config = new FractalNoiseConfig(5000, 2500, 1000, 500);
            FractalNoiseConfig config = new FractalNoiseConfig(100, 0, 20, 0);
            this.camera.setPos(new MJ3DVector(radius + 7000f,0f,0f));
            map = MapFactorySimplexPlanetPolar.getMap(seed, this.camera, radius, triadSize, config, backgroundColor, fog, wireframe, ambientLight, seaLevel, terrainColorScheme);
            return map;
        }

        private MJ3DMap getSimplexPlanetMapIcospherical(boolean fog, boolean wireframe, Color backgroundColor, float ambientLight, ColorScheme terrainColorScheme) {
            MJ3DMap map;
            long seed=700000;
            float triadSize = 100;
            float seaLevel = 500;
            float radius = 15_000;
//            FractalNoiseConfig config = new FractalNoiseConfig(radius, 0, 20, 0);
            FractalNoiseConfig config = new FractalNoiseConfig(200, 150, 100, 50);
            this.camera.setPos(new MJ3DVector(radius + 2000f,0f,0f));
            map = MapFactorySimplexPlanetIcospherical.getMap(seed, this.camera, radius, triadSize, config, backgroundColor,
					fog, wireframe, ambientLight, seaLevel, terrainColorScheme);
            return map;
        }

        public static void main(String[] args) {
			new MainWindow();
		}
		
		private void addKeyboardShortCuts() {
			view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "keyRight");
		 	view.getActionMap().put("keyRight", new YawRightAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "keyLeft");
		 	view.getActionMap().put("keyLeft", new YawLeftAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), "numPad2");
		 	view.getActionMap().put("numPad2", new PitchUpAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), "numPad8");
		 	view.getActionMap().put("numPad8", new PitchDownAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('l'), "numPad2");
		 	view.getActionMap().put("numPad2", new PitchUpAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('o'), "numPad8");
		 	view.getActionMap().put("numPad8", new PitchDownAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), "numPad4");
		 	view.getActionMap().put("numPad4", new RollRightAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), "numPad6");
		 	view.getActionMap().put("numPad6", new RollLeftAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), "keyUp");
		 	view.getActionMap().put("keyUp", new upAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "keyDown");
		 	view.getActionMap().put("keyDown", new downAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('a'), "keyStrafeLeft");
		 	view.getActionMap().put("keyStrafeLeft", new strafeLeftAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('s'), "keyStrafeRight");
		 	view.getActionMap().put("keyStrafeRight", new strafeRightAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('e'), "keyMoveUp");
		 	view.getActionMap().put("keyMoveUp", new moveUpAction());
		 	view.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke('d'), "keyMoveDown");
		 	view.getActionMap().put("keyMoveDown", new moveDownAction());
		}
		
		class PitchUpAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
		    public void actionPerformed(ActionEvent event) {
		    	camera.incrementPitch(DELTA_ROT);
				repaint();	
		    }
		}
		
		class PitchDownAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
		    public void actionPerformed(ActionEvent event) {
		    	camera.incrementPitch(-DELTA_ROT);
				repaint();	
		    }
		}	
		
		
		class RollLeftAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
		    public void actionPerformed(ActionEvent event) {
		    	camera.incrementRoll(DELTA_ROT);
				repaint();	
		    }
		}
		
		class RollRightAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
		    public void actionPerformed(ActionEvent event) {
		    	camera.incrementRoll(-DELTA_ROT);
				repaint();	
		    }
		}	
		
		class YawLeftAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
			public void actionPerformed(ActionEvent event) {
				camera.incrementYaw(-DELTA_ROT);
				repaint();	
			}
		}
		
		class YawRightAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
			public void actionPerformed(ActionEvent event) {
				camera.incrementYaw(DELTA_ROT);
				repaint();	
			}
		}	
	
		class upAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
		    public void actionPerformed(ActionEvent event) {
		    	camera.incrementX(DELTA_TRANS);
				repaint();	
		    }
		}
		
		class downAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
		    public void actionPerformed(ActionEvent event) {
		    	camera.incrementX(-DELTA_TRANS);
				repaint();	
		    }
		}
		
		class strafeLeftAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
		    public void actionPerformed(ActionEvent event) {
		    	camera.incrementY(-DELTA_TRANS);
				repaint();	
		    }
		}
		
		class strafeRightAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
		    public void actionPerformed(ActionEvent event) {
		    	camera.incrementY(DELTA_TRANS);
				repaint();	
		    }
		}
		
		class moveUpAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
			public void actionPerformed(ActionEvent event) {
				camera.incrementZ(-DELTA_TRANS);
				repaint();	
			}
		}
		
		class moveDownAction extends AbstractAction 
		{
			private static final long serialVersionUID = 100;
			public void actionPerformed(ActionEvent event) {
				camera.incrementZ(DELTA_TRANS);
				repaint();	
			}
		}
		
	}
