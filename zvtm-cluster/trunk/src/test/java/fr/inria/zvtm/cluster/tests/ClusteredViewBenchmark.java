
/*   AUTHOR : Olivier Gladin (olivier.gladin@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster.tests;


import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Point2D;

import java.util.Random;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.Set;

import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.cluster.ClusterGeometry;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Location;


import fr.inria.zvtm.event.ViewListener;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangleOr;


public class ClusteredViewBenchmark implements Runnable, ViewListener {
	
	protected VirtualSpace vs;
	protected VirtualSpaceManager vsm;
	private View localView;
	
	private ArrayList<VRectangleOr> shapes;
	
	protected ClusteredViewBenchmarkOptions options;
	

	private int vsWidth, vsHeight;
	private double defaultShapeSize;
	private double defaultShapeDelta;
	private double defaultShapeSpeed = Math.PI/25;
	private final float defaultShapeAlpha = 0.5f;

	private long lastUpdateTime = 0L;
	private volatile boolean running;
	private Thread t;
	private boolean rotating=false;
	private final double cameraOffset = 10;
	private fr.inria.zvtm.engine.Camera camCluster;
	private fr.inria.zvtm.engine.Camera camCluster2;
	private fr.inria.zvtm.engine.Camera masterCam;





	public ClusteredViewBenchmark(ClusteredViewBenchmarkOptions options) {
		
		this.options = options;

		vsWidth = options.blockWidth*options.numCols;
		vsHeight = options.blockHeight*options.numRows;

		shapes = new ArrayList<VRectangleOr>();
		
		vsm = VirtualSpaceManager.INSTANCE;
		vsm.setMaster("Benchmark");
		vs = vsm.addVirtualSpace("Benchmark");

		masterCam = vs.addCamera();

		Vector<fr.inria.zvtm.engine.Camera> masterCameras = new Vector<fr.inria.zvtm.engine.Camera>();
		masterCameras.add(masterCam);			

		localView = VirtualSpaceManager.INSTANCE.addFrameView(masterCameras, "Master View",
			   View.STD_VIEW, options.masterWidth, options.masterHeight, false, true, true, null);	
		localView.setListener(this);
		//localView.getFrame().setLocation(800, 800);
		localView.setBackgroundColor(Color.white);

	
		Vector<fr.inria.zvtm.engine.Camera> clusterCameras = new Vector<fr.inria.zvtm.engine.Camera>();
		camCluster = vs.addCamera();
		clusterCameras.add(camCluster);
		
		Vector<fr.inria.zvtm.engine.Camera> clusterCameras2 = new Vector<fr.inria.zvtm.engine.Camera>();
		camCluster2 = vs.addCamera();
		clusterCameras2.add(camCluster2);
		
		ClusterGeometry clGeom = new ClusterGeometry(
                options.blockWidth,
                options.blockHeight,
                options.numCols,
                options.numRows);
		ClusteredView cv;
		if (!options.doubleView) {
			cv = new ClusteredView(
	                    clGeom,
	                    options.numRows-1, //origin (block number)
	                    options.numCols, //use complete
	                    options.numRows, //cluster surface
	                    clusterCameras);
			cv.setBackgroundColor(Color.white);
			vsm.addClusteredView(cv);
		}
		else {
			ClusteredView cv2;
			System.out.println("1st ClusteredView");
			cv = new ClusteredView(
                    clGeom,
                    options.numRows-1, //origin (block number)
                    (options.numCols/2)+(options.numCols%2), //use 1st half of the surface splitted vertically including the middle column
                    options.numRows, 
                    clusterCameras);
			cv.setBackgroundColor(Color.white);
			vsm.addClusteredView(cv);

			System.out.println("2nd ClusteredView");
			int SecondHalfOrigin = (((options.numCols/2)+(options.numCols%2)+1)*options.numRows)-1;
			System.out.println("SecondHalfOrigin :" + SecondHalfOrigin );
			System.out.println("options.numCols :" + options.numCols );
			
			cv2 = new ClusteredView(
                    clGeom,
                    SecondHalfOrigin, //origin (block number)
                    options.numCols/2, //use 2nd half without the middle column 
                    options.numRows, 
                    clusterCameras2);
			cv2.setBackgroundColor(Color.black);
			vsm.addClusteredView(cv2);
			
			//Set cameras					
	        Location loc = cv.centerOnRegion(camCluster, -vsWidth/2, -vsHeight/2, (options.numCols%2)*(options.blockWidth/2), vsHeight/2);
	        camCluster.setLocation(loc);
	        
	        Location loc2 = cv2.centerOnRegion(camCluster2, (options.numCols%2)*(options.blockWidth/2), -vsHeight/2, vsWidth/2, vsHeight/2);
	        camCluster2.setLocation(loc2);
						
		}
		

		//Zoom out the master view					
        Location loc = localView.centerOnRegion(masterCam, 0, -vsWidth/2, -vsHeight/2, vsWidth/2, vsHeight/2);
        masterCam.setLocation(loc);
				
		
		defaultShapeSize = options.blockHeight/3;

		defaultShapeDelta = options.blockHeight/10;

		Random r = new Random();

		for (int i=0;i<options.glyphNumber;i++) {

			double posX, posY, width, height;
			posX = -vsWidth/2 + r.nextDouble()*vsWidth;
			posY = -vsHeight/2 + r.nextDouble()*vsHeight/2; //On 
			height = width = defaultShapeSize + r.nextDouble()*defaultShapeDelta;
			Color color = new Color(r.nextFloat(), r.nextFloat(), r.nextFloat());
			VRectangleOr shape = new VRectangleOr(posX, posY, i, width, height, color, color, 0, defaultShapeAlpha);
			vs.addGlyph(shape);
			shapes.add(shape);
		}
		
		// Start new thread
		running = true;
		t = new Thread(this);
		t.start();

	}

	
	@Override
	public void run() {
		long currentUpdateTime = System.nanoTime();
		while (running) {

			long startLoopTime = System.nanoTime();


			lastUpdateTime = currentUpdateTime;
			currentUpdateTime = System.nanoTime();

			//Do something
			if (rotating) {
				int trigo = 1;
				for(VRectangleOr shape: shapes){
					shape.orientTo(shape.getOrient()-(defaultShapeSpeed*trigo));
					trigo *=-1;
				}
			}

			// 1000000000ns/60 = 16666667,
			long endLoopTime = System.nanoTime();
			long sleepRate = 16666667L - (endLoopTime - startLoopTime); 

			while (System.nanoTime() - endLoopTime < sleepRate) {
				Thread.yield();
			}
		}
	}	
	
	public static void main (String [] args) {

		ClusteredViewBenchmarkOptions options = new ClusteredViewBenchmarkOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try{
			parser.parseArgument(args);
		} catch(CmdLineException ex){  
			System.err.println(ex.getMessage());
			parser.printUsage(System.err);
			return;
		}
		
		new ClusteredViewBenchmark(options);		
	}


	@Override
	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e) {
		switch (code) {
			case KeyEvent.VK_ESCAPE:
				System.exit(0);
			case KeyEvent.VK_SPACE:
				rotating=!rotating;
				break;
			case KeyEvent.VK_UP:
				camCluster.move(0,-cameraOffset);
				masterCam.move(0,-cameraOffset);
				break;
			case KeyEvent.VK_DOWN:
				camCluster.move(0,cameraOffset);
				masterCam.move(0,cameraOffset);
				break;
			case KeyEvent.VK_LEFT:
				camCluster.move(-cameraOffset,0);
				masterCam.move(-cameraOffset,0);
				break;
			case KeyEvent.VK_RIGHT:
				camCluster.move(cameraOffset,0);
				masterCam.move(cameraOffset,0);
				break;
		}
	}


	@Override
	public void Krelease(ViewPanel arg0, char arg1, int arg2, int arg3,
			KeyEvent arg4) {}


	@Override
	public void Ktype(ViewPanel arg0, char arg1, int arg2, int arg3,
			KeyEvent arg4) {}


	@Override
	public void click1(ViewPanel arg0, int arg1, int arg2, int arg3, int arg4,
			MouseEvent arg5) {}


	@Override
	public void click2(ViewPanel arg0, int arg1, int arg2, int arg3, int arg4,
			MouseEvent arg5) {}


	@Override
	public void click3(ViewPanel arg0, int arg1, int arg2, int arg3, int arg4,
			MouseEvent arg5) {}


	@Override
	public void mouseDragged(ViewPanel arg0, int arg1, int arg2, int arg3,
			int arg4, MouseEvent arg5) {}


	@Override
	public void mouseMoved(ViewPanel arg0, int arg1, int arg2, MouseEvent arg3) {}


	@Override
	public void mouseWheelMoved(ViewPanel arg0, short arg1, int arg2, int arg3,
			MouseWheelEvent arg4) {}


	@Override
	public void press1(ViewPanel arg0, int arg1, int arg2, int arg3,
			MouseEvent arg4) {}


	@Override
	public void press2(ViewPanel arg0, int arg1, int arg2, int arg3,
			MouseEvent arg4) {}


	@Override
	public void press3(ViewPanel arg0, int arg1, int arg2, int arg3,
			MouseEvent arg4) {}


	@Override
	public void release1(ViewPanel arg0, int arg1, int arg2, int arg3,
			MouseEvent arg4) {}


	@Override
	public void release2(ViewPanel arg0, int arg1, int arg2, int arg3,
			MouseEvent arg4) {}


	@Override
	public void release3(ViewPanel arg0, int arg1, int arg2, int arg3,
			MouseEvent arg4) {}


	@Override
	public void viewActivated(View arg0) {}


	@Override
	public void viewClosing(View arg0) {}


	@Override
	public void viewDeactivated(View arg0) {}


	@Override
	public void viewDeiconified(View arg0) {}


	@Override
	public void viewIconified(View arg0) {}

}




