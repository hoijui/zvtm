package fr.inria.zvtm.dazibao;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import fr.inria.zvtm.cluster.ClusteredImage;
import fr.inria.zvtm.cluster.ClusteredView;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewEventHandler;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import java.util.Vector;

/**
 * Main Dazibao class.
 * Creates a VirtualSpace and a Camera, listens
 * to page add requests over HTTP and adds requested 
 * pages to the virtual space.
 */
public class DazBoard {
    private DazHttpServer server;
    public static final int DEFAULT_SERVER_PORT = 3444;

    private VirtualSpace space;
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE; //shortcut
    private final long IMG_DIM_MAX_A4=842; //pixels
    private final long INCR = IMG_DIM_MAX_A4 + 60;
    private long currX = 0;
    private long currY = 0;
    private int nbRows = 4;
    private int imgIndex = 0;


    DazBoard(DazOptions options) throws IOException{
        server = new DazHttpServer(DEFAULT_SERVER_PORT);

        vsm.setMaster("DazBoard");
        space = vsm.addVirtualSpace("DazBoard");
        Camera cam = space.addCamera();
        Vector<Camera> cams = new Vector<Camera>();
        cams.add(cam);
        //a local view allows easier debugging, pan and zoom
        //but is not suitable on a headless server
        if(options.localView){
            View view = vsm.addFrameView(cams, "Dazibao view", 
                    View.STD_VIEW, 640, 480, false, true, true, null);
            view.setEventHandler(new PanZoomEventHandler());
        }
        ClusteredView clusteredView = new ClusteredView(options.viewOrigin,
                options.blockWidth,
                options.blockHeight,
                options.numRows, options.numCols,
                options.viewRows, options.viewCols,
                cams);
        vsm.addClusteredView(clusteredView);
        cam.moveTo(0,0);
    }

    public static void main(String[] args){
        DazOptions options = new DazOptions();
		CmdLineParser parser = new CmdLineParser(options);
		try{
			parser.parseArgument(args);
		} catch(CmdLineException ex){
			System.err.println(ex.getMessage());
			parser.printUsage(System.err);
			return;
		}

		if(options.help){
			System.err.println("Usage: DazBoard [options] where options are: ");
			parser.printUsage(System.err);
			return;
		}

        try{
            new DazBoard(options);
        }catch(IOException ex){
            System.err.println("Couldn't start server:\n" + ex);
            System.exit(-1);
        }
        System.out.println( "Dazboard server listening. Hit Enter to stop.\n" );
        try { System.in.read(); } catch( Throwable t ) {};
    }

    private class DazHttpServer extends NanoHTTPD {
        public DazHttpServer(int port) throws IOException{
            super(port);
        }

        public Response serve(String uri, String method, Properties header, Properties parms)
        {
            //crude query handling. we only serve one type
            //of request, so no need for sophistication here
            //TODO: improve (how? patterns?)
            System.out.println("uri: " + uri);
            System.out.println("method: " + method);
            System.out.println("header: " + header);
            System.out.println("parms: " + parms);

            if(!uri.equals("/addpage")){
                return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "operation not supported\n");
            }

            if(!method.equals("POST")){
                return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "operation not supported\n");
            }

            String image = parms.getProperty("image");
            System.out.println("image: " + image);
            if(image == null){
                return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "missing parameter 'image'\n");
            }

            URL imgUrl;
            try{
                imgUrl = new URL(image);
            } catch (MalformedURLException ex){
                return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "image URL error (required image: " + image + ")\n");
            } 

            ClusteredImage cImg = new ClusteredImage(currX, currY, 0, imgUrl, 1f);
            space.addGlyph(cImg, true);
            if(imgIndex % nbRows == 0){
                currY = 0;
                currX += INCR;
            } else {
                currY -= INCR;
            }
            imgIndex++;

            return new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, "addpage successful\n");
        }
    }
    private class PanZoomEventHandler implements ViewEventHandler{
        private int lastJPX;
        private int lastJPY;

        public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

        public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

        public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

        public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

        public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

        public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

        public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            lastJPX=jpx;
            lastJPY=jpy;
            v.setDrawDrag(true);
            vsm.activeView.mouse.setSensitivity(false);
            //because we would not be consistent  (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed, and if the mouse is not moving, this list is not computed - so here we choose to disable this computation when dragging the mouse with button 3 pressed)
        }

        public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
            vsm.getAnimationManager().setXspeed(0);
            vsm.getAnimationManager().setYspeed(0);
            vsm.getAnimationManager().setZspeed(0);
            v.setDrawDrag(false);
            vsm.activeView.mouse.setSensitivity(true);
        }

        public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

        public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

        public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
            if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
                Camera c=vsm.getActiveCamera();
                float a=(c.focal+Math.abs(c.altitude))/c.focal;
                if (mod == META_SHIFT_MOD) {
                    vsm.getAnimationManager().setXspeed(0);
                    vsm.getAnimationManager().setYspeed(0);
                    vsm.getAnimationManager().setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/4.0f)) : (long)((lastJPY-jpy)/(a*4)));

                }
                else {
                    vsm.getAnimationManager().setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/4.0f)) : (long)((jpx-lastJPX)/(a*4)));
                    vsm.getAnimationManager().setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/4.0f)) : (long)((lastJPY-jpy)/(a*4)));
                }
            }
        }

        public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){}

        public void enterGlyph(Glyph g){
        }

        public void exitGlyph(Glyph g){
        }

        public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){}

        public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        }

        public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

        public void viewActivated(View v){}

        public void viewDeactivated(View v){}

        public void viewIconified(View v){}

        public void viewDeiconified(View v){}

        public void viewClosing(View v){System.exit(0);}
    }
  }

class DazOptions {
    @Option(name = "-bw", aliases = {"--block-width"}, usage = "clustered view block width")
        int blockWidth = 400;
    @Option(name = "-bh", aliases = {"--block-height"}, usage = "clustered view block height")
        int blockHeight = 300;
    @Option(name = "-r", aliases = {"--num-rows"}, usage = "number of rows in the cluster")
        int numRows = 2;
    @Option(name = "-c", aliases = {"--num-cols"}, usage = "number of columns in the cluster")
        int numCols = 3;
    int viewRows = numRows;
    int viewCols = numCols;
    int viewOrigin = numRows - 1;
    @Option(name = "-v", aliases = {"--local-view"}, usage = "create local view")
        boolean localView = false;
    @Option(name = "-h", aliases = {"--help"}, usage = "print this help message and exit")
        boolean help = false;
}

