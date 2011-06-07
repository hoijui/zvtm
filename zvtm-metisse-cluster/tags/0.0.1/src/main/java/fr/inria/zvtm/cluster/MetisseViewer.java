package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.util.Vector;

import javax.swing.ImageIcon;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.gui.Viewer;




public class MetisseViewer extends Viewer {
	
	private ClusteredView cv;
	private ClusterGeometry clGeom;
	
	public MetisseViewer(boolean isClient){
		super(isClient);
	}

	@Override protected void preInitHook(){
		if(!isClient)vsm.setMaster("intothewild");
	}

	@Override protected void viewCreatedHook(Vector<Camera> cameras){
		if(!isClient){
			if(!MetisseMain.CLUSTERMODE)return;
			if(MetisseMain.SMALLMODE){
				clGeom = new ClusterGeometry(600,400,2,2);
				//                options.blockWidth,
				//                options.blockHeight,
				//                options.numCols,
				//                options.numRows);
				cv = new ClusteredView(clGeom,1,2,2,cameras);
				cv.setBackgroundColor(Color.LIGHT_GRAY);
				vsm.addClusteredView(cv);
			}
			else{
				clGeom = new ClusterGeometry(2760,1840,8,4);
				cv = new ClusteredView(clGeom,3,8,4,cameras);
				//                    options.numRows-1, //origin (block number)
				//                    options.numCols, //use complete
				//                    options.numRows, //cluster surface
				//                    cameras);
				cv.setBackgroundColor(Color.LIGHT_GRAY);
				vsm.addClusteredView(cv);
			}
		}

	}
	

	protected void addBackground() {
		ImageIcon img = (new ImageIcon("src/main/java/fr/inria/zvtm/resources/bg.jpg"));
		wallSpace.addGlyph(new VImage(img.getImage()));
	}
	
	public double getVisibleRegionWidth(){
		return clGeom.getWidth()*mCamera.focal/(mCamera.focal+mCamera.altitude);
	}
	public double getVisibleRegionHeight(){
		return clGeom.getHeight()*mCamera.focal/(mCamera.focal+mCamera.altitude);
	}
	
	@Override
	protected void backgroundHook(){
		
	}
}

