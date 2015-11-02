package fr.inria.zuist.cluster.aviewer;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.Component;
import java.awt.event.ComponentListener;
import java.awt.event.ComponentEvent;
import java.awt.BasicStroke;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Location;
import fr.inria.zvtm.engine.portals.CameraPortal;
import fr.inria.zvtm.engine.portals.DraggableCameraPortal;

import fr.inria.zvtm.event.PortalListener;
import fr.inria.zvtm.event.CameraListener;

import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VSegment;

//import fr.inria.zuist.engine.PseudoView;

public class DragMag extends CameraPortal implements ComponentListener, CameraListener {

    public static final int DM_TYPE_MANATTHAN = 0;
    public static final int DM_TYPE_INDEPENDANT = 1;
    public static final int DM_TYPE_DRAGMAG = 2;

    private int type;
    private int saveDragBarHeight;
    private double scaleFactor;
 
    public final Camera extCam;
    private Component comp;
    public final VirtualSpace glyphsSpace;
    //public final PseudoView pv;
    //private int pvwidth, pvheight;

    private VRectangle visRect = null;
    private VSegment[] visSegs = null;
    private VSegment[] jointSegs = null;
    private double appWidth, appHeight;
    private BasicStroke bs;
    private float lineWidth=1.0f;

    public DragMag(int x, int y, int width, int height,  Camera c, Camera extc, VirtualSpace gs, double scaleFactor, int type)
    {
        super(x, y, width, height, c);
    	this.extCam = extc;
        this.glyphsSpace = gs;
        this.scaleFactor = scaleFactor;
        this.type = type;
        if (type < 0 || type >2) { this.type = DM_TYPE_DRAGMAG; }
        //this.pvwidth = width;
        //this.pvheight = height;
        //VirtualSpace vs = c.getOwningSpace();
        //this.pv = new PseudoView(vs, c, this.pvwidth, this.pvheight);
        //d this.saveDragBarHeight = getDragBarHeight();
        if (type == DM_TYPE_MANATTHAN) {
            //d setDragBarHeight(0);
        }
        this.extCam.addListener(this);
        comp = extCam.getOwningView().getPanel().getComponent();
        comp.addComponentListener(this);
        this.appWidth = comp.getWidth();
        this.appHeight = comp.getHeight();
        this.bs = new BasicStroke(lineWidth); 
        setupCamera();
    }

    public void setupCamera(){
        setupCamera(scaleFactor);
    }

    public void setupCamera(double scale_factor){
        Camera cam = super.getCamera();
        scaleFactor = scale_factor;
        cam.setLocation(extCam.getLocation());
        Location cgl = cam.getLocation();
        double a = (cam.focal + cam.altitude) / cam.focal;
        double newz = cam.focal * a * (1/scaleFactor) - cam.focal;
        if (newz < 0) newz = 0;
        cam.setAltitude(newz);
        updateVis();
    }

    public int getType() { return type; }

    public boolean isManatthan() { return (type == DM_TYPE_MANATTHAN); }

    public void setType(int t)
    {
        if (t < 0 || t > 2) t=DM_TYPE_DRAGMAG;
        if (type == t) return;

        if (type == DM_TYPE_MANATTHAN){
            type = t;
            //d setDragBarHeight(saveDragBarHeight);
            updateVis();
            return;
        }
        if (type == DM_TYPE_DRAGMAG){
            if (visRect != null){
                glyphsSpace.removeGlyph(visRect);
                visRect = null;
            }
            if (visSegs != null){
                for(int i=0; i<8;i++){
                    if (visSegs[i]!=null){glyphsSpace.removeGlyph(visSegs[i]);visSegs[i] = null;}
                }
            }
            if (jointSegs != null){
                for(int i=0; i<2;i++){
                    if (jointSegs[i]!=null){glyphsSpace.removeGlyph(jointSegs[i]);jointSegs[i] = null;}
                }
            }
        }
        if (t == DM_TYPE_MANATTHAN){
            //d saveDragBarHeight = getDragBarHeight();
            //d setDragBarHeight(0);
            type = t;
            cameraMoved(extCam, new Point2D.Double(extCam.vx, extCam.vy), extCam.altitude);
        }
        type = t;
    }

    public void updatePanelSize(int w, int h){
            appWidth = w; appHeight = h;
            comp.removeComponentListener(this); // so we want to take care of the panel size
            updateVis();
    }

    public void updateVis(){
        if (type == DM_TYPE_MANATTHAN || type == DM_TYPE_INDEPENDANT){
            return;
        }
        Camera cam = super.getCamera();
        double a = (cam.focal + cam.altitude) / cam.focal;
        Location cgl = cam.getLocation();
        double exta = (extCam.focal + extCam.altitude) / extCam.focal;
        double rvx = -(extCam.vx - cam.vx)/exta;
        double rvy = -(extCam.vy - cam.vy)/exta;
        double scale = a/exta;
        //System.out.println("scale: "+ a+" "+exta+" "+scale+ " "+ (1/scaleFactor));
        double rw = w * scale + 2*lineWidth; 
        double rh = h * scale + 2*lineWidth;
        // a min size for rw and rh ...
        double tlx = rvx-rw/2;
        double tly = rvy+rh/2;
        double cvx = x - appWidth/2 + w/2;
        double cvy = -y + appHeight/2 - h/2; 
        double vvx = x - appWidth/2;
        double vvy = -y + appHeight/2; 
        double jvx = rvx;
        double jvy = rvy;
        double jdx = 1, jdy = 1;
        double d = Math.sqrt(Math.pow(rvx - cvx, 2)+Math.pow(rvy - cvy, 2));
        if (d!=0)   {
            double t1 = Math.abs((cvx-rvx)/d);
            double t2 = Math.abs((cvy-rvy)/d);
            double tt = 1;
            if (t1>Math.sqrt(2)/2) { tt = 1; jdy = 1; } else { tt = t1/t2; jdy = 0; }
            if (rvx < cvx) { jvx = rvx+2*lineWidth+tt*rw/2; }
            else if (rvx > cvx) {jvx = rvx-2*lineWidth-tt*rw/2;}
            //
            if (t2>Math.sqrt(2)/2) { tt = 1; jdx = 1; } else { tt = t2/t1; jdx = 0;}
            if (rvy < cvy) {jvy = rvy+2*lineWidth+tt*rh/2;}
            else if (rvy > cvy) {jvy = rvy-2*lineWidth-tt*rh/2; }
        }   
        if (visRect==null){
            visRect = new VRectangle(rvx, rvy, 10,  rw, rh, Color.BLACK,  Color.WHITE, 0.0f);
            glyphsSpace.addGlyph(visRect);
            visSegs = new VSegment[8];
            jointSegs = new VSegment[2];
            Color[] col = {Color.BLACK,Color.WHITE};
            for(int i=0;i<2;i++){
                visSegs[0+i*4] = new VSegment(tlx, tly, tlx+rw, tly, 11, col[i]);
                visSegs[1+i*4] = new VSegment(tlx+rw, tly, tlx+rw, tly-rh, 11, col[i]);
                visSegs[2+i*4] = new VSegment(tlx+rw, tly-rh, tlx, tly-rh, 11, col[i]);
                visSegs[3+i*4] = new VSegment(tlx, tly-rh, tlx, tly, 11, col[i]);
                tlx=tlx-lineWidth; tly=tly+lineWidth;
                rh=rh+2*lineWidth; rw=rw+2*lineWidth;
            }
            for(int i=0; i<8; i++) { visSegs[i].setStroke(bs); glyphsSpace.addGlyph(visSegs[i]); }
            //  
            jointSegs[0] = new VSegment(jvx, jvy, cvx, cvy, 11, col[0]);
            jointSegs[1] = new VSegment(
                jvx + jdx*lineWidth, jvy + jdy*lineWidth,
                cvx + jdx*lineWidth, cvy + jdy*lineWidth, 11, col[1]);
            for(int i=0; i<2; i++) { jointSegs[i].setStroke(bs); glyphsSpace.addGlyph(jointSegs[i]); }
        }
        else{
            //for(int i=0; i<4; i++) { visSegs[i].moveTo(rvx, rvy); }
            visRect.moveTo(rvx, rvy);
            visRect.setWidth(rw);
            visRect.setHeight(rh);
            Color[] col = {Color.BLACK,Color.WHITE};
            for(int i=0;i<2;i++){   
                visSegs[0+i*4].setEndPoints(tlx, tly, tlx+rw, tly);
                visSegs[1+i*4].setEndPoints(tlx+rw, tly, tlx+rw, tly-rh);
                visSegs[2+i*4].setEndPoints(tlx+rw, tly-rh, tlx, tly-rh);
                visSegs[3+i*4].setEndPoints(tlx, tly-rh, tlx, tly);
                //tlx=tlx-1; tly=tly+1; rh=rh+2; rw=rw+2;
                tlx=tlx-lineWidth; tly=tly+lineWidth;
                rh=rh+2*lineWidth; rw=rw+2*lineWidth;
            }
            //
            jointSegs[0].setEndPoints(jvx + jdx, jvy, cvx, cvy);
            jointSegs[1].setEndPoints(
                jvx + jdx*lineWidth, jvy + jdy*lineWidth,
                cvx + jdx*lineWidth, cvy + jdy*lineWidth); 
        }

    }
  
    public boolean coordInsideVis(int cx, int cy){
        if (type == DM_TYPE_MANATTHAN || type == DM_TYPE_INDEPENDANT) {
            return false;
        }
        double aa = 1;
        double cw = aa*((visRect.vx - visRect.vw/2) + appWidth/2.0);
        double cn = aa*(-(visRect.vy + visRect.vh/2) + appHeight/2.0);
        double ce = aa*((visRect.vx + visRect.vw/2) + appWidth/2.0);
        double cs = aa*(-(visRect.vy - visRect.vh/2) + appHeight/2.0);
        //System.out.println("ci: "+cx+" "+cy+" "+cw+" "+ce+" "+cn+" "+cs+" ");
        return (cx >= cw && cx <= ce && cy >= cn && cy <= cs);
    }

    public void moveTo(int x, int y){
        super.moveTo(x,y);
        updateVis();
    }
    
    public void move(int dx, int dy){
        super.move(dx,dy);
        updateVis();
    }

    public void resize(int dw, int dh){ 
    	super.resize(2*dw, 2*dh);
        super.moveTo(x-dw,y-dh);
    	//pv.sizeTo(w, h);
        updateVis();
    }

    /* Camera listener (extCam) */
    public void cameraMoved(Camera cam, Point2D.Double coord, double alt)
    {
        if (type == DM_TYPE_MANATTHAN){
            Location cgl = extCam.getLocation();
            //Location dmloc =  super.getCamera().getLocation();
            double a = (extCam.focal + extCam.altitude) / extCam.focal;
            double xx = cgl.getX()+ a*(x + w/2 - appWidth/2.0);
            double yy = cgl.getY()+ a*(-y - h/2 + appHeight/2.0);
            double newz = extCam.focal * a * (1/scaleFactor) - extCam.focal;
            if (newz < 0) newz = 0;
            getCamera().setLocation(new Location(xx,yy, newz));
        }
        else{
            updateVis();
        }
    }

    /* ComponentListener */
    public void componentHidden(ComponentEvent e){}
    public void componentMoved(ComponentEvent e){}
    public void componentResized(ComponentEvent e){
        appWidth = e.getComponent().getWidth();
        appHeight = e.getComponent().getHeight();
        cameraMoved(extCam, new Point2D.Double(extCam.vx, extCam.vy), extCam.altitude);
    }
    public void componentShown(ComponentEvent e){}

}