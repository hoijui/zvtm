package fr.inria.zvtm.cluster.examples;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;

import java.awt.Color;
import java.util.Vector;

/**
 * Sample master application.
 */
public class ColorRectCluster {
	//shortcut
	private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; 

	ColorRectCluster(CROptions options){
		vsm.setMaster("ColorRectCluster");
		VirtualSpace vs = vsm.addVirtualSpace("testSpace");
		Camera cam = vs.addCamera();
		Vector<Camera> cameras = new Vector<Camera>();
		cameras.add(cam);	
		ClusteredView cv = 
			new ClusteredView(0, //origin (block number)
					400, //block width
					300, //block height
					2, //rows,
					3, //cols
					cameras);
		cv.setBackgroundColor(Color.LIGHT_GRAY);

		//the view below is just a standard, non-clustered view
		//that lets an user navigate the scene
		View view = vsm.addFrameView(cameras, "Master View",
			   View.STD_VIEW, 800, 600, false, true, true, null);	

		long xOffset = 0;
		long yOffset = 0;
		long rectWidth = options.width / options.xNum;
		long rectHeight = options.height / options.yNum;
		for(int i=0; i<options.xNum; ++i){
			for(int j=0; j<options.yNum; ++j){
				VRectangle rect = 
					new VRectangle(xOffset+i*rectWidth,
							yOffset+j*rectHeight,
							0,
							rectWidth/2,
							rectHeight/2,
							Color.getHSBColor((float)(i*j/(float)(options.xNum*options.yNum)), 1f, 1f));
				rect.setDrawBorder(false);
				vs.addGlyph(rect, false);
			}
		}
	}

	public static void main(String[] args){
		CROptions options = new CROptions();
		CmdLineParser parser = new CmdLineParser(options);
		try{
			parser.parseArgument(args);
		} catch(CmdLineException ex){
			System.err.println(ex.getMessage());
			parser.printUsage(System.err);
			return;
		}

		new ColorRectCluster(options);
	}
}

class CROptions {
	@Option(name = "-x", aliases = {"--xnum"}, usage = "number of subdivisions along x axis")
	int xNum = 50;

	@Option(name = "-y", aliases = {"--ynum"}, usage = "number of subdivisions along y axis")
	int yNum = 20;

	@Option(name = "-w", aliases = {"--width"}, usage = "color rect width")
	int width = 800;

	@Option(name = "-h", aliases = {"--height"}, usage = "color rect height")
	int height = 600;
}

