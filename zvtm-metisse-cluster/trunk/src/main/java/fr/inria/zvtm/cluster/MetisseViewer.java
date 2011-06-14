package fr.inria.zvtm.cluster;

import java.awt.Color;

import fr.inria.zvtm.master.gui.MasterViewer;





public class MetisseViewer extends MasterViewer {
	
	private ClusteredView cv;
	private ClusterGeometry clGeom;

	@Override protected void preInitHook(){
		vsm.setMaster("intothewild");
	}

	@Override protected void viewCreatedHook(){
		
			if(!MetisseMain.CLUSTERMODE)return;
			if(MetisseMain.SMALLMODE){
				clGeom = new ClusterGeometry(400,300,2,2);
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
	
	@Override
	protected void addBackground() {
	
	}
	
	public double getVisibleRegionWidth(){
		return clGeom.getWidth()*mCamera.focal/(mCamera.focal+mCamera.altitude);
	}
	public double getVisibleRegionHeight(){
		return clGeom.getHeight()*mCamera.focal/(mCamera.focal+mCamera.altitude);
	}
}

