package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.util.Vector;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.gui.Viewer;




public class MetisseViewer extends Viewer {
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
				ClusterGeometry clGeom = new ClusterGeometry(600,400,2,2);
				//                options.blockWidth,
				//                options.blockHeight,
				//                options.numCols,
				//                options.numRows);
				ClusteredView cv = new ClusteredView(clGeom,1,2,2,cameras);
				cv.setBackgroundColor(Color.LIGHT_GRAY);
				vsm.addClusteredView(cv);
			}
			else{
				ClusterGeometry clGeom = new ClusterGeometry(2560,1600,8,4);
				ClusteredView cv = new ClusteredView(clGeom,3,8,4,cameras);
				//                    options.numRows-1, //origin (block number)
				//                    options.numCols, //use complete
				//                    options.numRows, //cluster surface
				//                    cameras);
				cv.setBackgroundColor(Color.LIGHT_GRAY);
				vsm.addClusteredView(cv);
			}
		}

	}
}

