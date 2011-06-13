package fr.inria.zvtm.glyphs;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import fr.inria.zvtm.glyphs.projection.RProjectedCoordsP;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartChangeEvent;

/**
 * A Glyph that embeds a JFreeChart instance.
 */
public class VChart extends ClosedShape implements RectangularShape {

    /** Width in virtual space. For internal use. Made public for easier outside package subclassing. */
    public double vw;
    /** Height in virtual space. For internal use. Made public for easier outside package subclassing. */
    public double vh;
    /* For internal use. Made public for easier outside package subclassing. Aspect ratio (width divided by height). */
    public double ar;

    private JFreeChart chart;
    private transient ChartRenderingInfo chartRenderingInfo;
    private volatile transient boolean updateRenderingInfo = true;

    /** For internal use. Made public for easier outside package subclassing. */
    public RProjectedCoordsP[] pc;

    /**
     *@param chart chart to draw
     */
    public VChart(JFreeChart chart){
        this(chart, 0, 0, 0, 100, 40);
    }

    /**
     *@param chart chart to draw
     *@param x coordinate in virtual space
     *@param y coordinate in virtual space
     *@param z z-index (pass 0 if you do not use z-ordering)
     *@param w width in virtual space
     *@param h height in virtual space
     */
    public VChart(JFreeChart chart, double x, double y, int z, double w, double h){
        vx=x;
        vy=y;
        vz=z;
        vw=w;
        vh=h;
        computeSize();
        if (vw==0 && vh==0){ar=1.0f;}
        else {ar = vw / vh;}
        orient=0;
        this.chart = chart;
        this.chartRenderingInfo = new ChartRenderingInfo();
        chart.addChangeListener(new ChartChangeListener(){
            public void chartChanged(ChartChangeEvent event){
                updateRenderingInfo = true;
                VirtualSpaceManager.INSTANCE.repaint();
            }
        });
    }

    @Override
        public void initCams(int nbCam){
            pc=new RProjectedCoordsP[nbCam];
            for (int i=0;i<nbCam;i++){
                pc[i]=new RProjectedCoordsP();
            }
        }

    @Override
        public void addCamera(int verifIndex){
            if (pc!=null){
                if (verifIndex==pc.length){
                    RProjectedCoordsP[] ta=pc;
                    pc=new RProjectedCoordsP[ta.length+1];
                    for (int i=0;i<ta.length;i++){
                        pc[i]=ta[i];
                    }
                    pc[pc.length-1]=new RProjectedCoordsP();
                }
                else {System.err.println("VChart:Error while adding camera "+verifIndex);}
            }
            else {
                if (verifIndex==0){
                    pc=new RProjectedCoordsP[1];
                    pc[0]=new RProjectedCoordsP();
                }
                else {System.err.println("VChart:Error while adding camera "+verifIndex);}
            }
        }

    @Override
        public void removeCamera(int index){
            pc[index]=null;
        }

    @Override
        public void resetMouseIn(){
            for (int i=0;i<pc.length;i++){
                resetMouseIn(i);
            }
        }

    @Override
        public void resetMouseIn(int i){
            if (pc[i]!=null){pc[i].prevMouseIn=false;}
            borderColor = bColor;
        }

    @Override
        public double getOrient(){return orient;}

    /** Cannot be reoriented. */
    @Override
        public void orientTo(double angle){}

    @Override
        public double getSize(){return size;}

    public double getWidth(){return vw;}

    public double getHeight(){return vh;}

    public ChartRenderingInfo getRenderingInfo(){ 
        return chartRenderingInfo; 
    }

    void computeSize(){
        size = Math.sqrt(Math.pow(vw,2)+Math.pow(vh,2));
    }

    @Override
        public void sizeTo(double s){
            // return diameter of bounding circle
            size = s;
            vw = (size*ar) / Math.sqrt(Math.pow(ar,2)+1);
            vh = size / Math.sqrt(Math.pow(ar,2)+1);
            VirtualSpaceManager.INSTANCE.repaint();
        }

    public void setWidth(double w){ 
        vw = w;
        ar = vw / vh;
        computeSize();
        VirtualSpaceManager.INSTANCE.repaint();
    }

    public void setHeight(double h){
        vh = h;
        ar = vw / vh;
        computeSize();
        VirtualSpaceManager.INSTANCE.repaint();
    }

    @Override
        public void reSize(double factor){ //resizing factor
            size *= factor;
            vw = (size*ar) / Math.sqrt(Math.pow(ar,2)+1);
            vh = size / Math.sqrt(Math.pow(ar,2)+1);
            VirtualSpaceManager.INSTANCE.repaint();
        }

    @Override
        public boolean fillsView(double w,double h,int camIndex){
            return ((alphaC == null) &&
                    (w<=pc[camIndex].cx+pc[camIndex].cw) && (0>=pc[camIndex].cx-pc[camIndex].cw) &&
                    (h<=pc[camIndex].cy+pc[camIndex].ch) && (0>=pc[camIndex].cy-pc[camIndex].ch));
        }

    @Override
        public boolean coordInside(int jpx, int jpy, int camIndex, double cvx, double cvy){
            return ((jpx>=(pc[camIndex].cx-pc[camIndex].cw)) && (jpx<=(pc[camIndex].cx+pc[camIndex].cw)) &&
                    (jpy>=(pc[camIndex].cy-pc[camIndex].ch)) && (jpy<=(pc[camIndex].cy+pc[camIndex].ch)));
        }

    @Override
        public boolean visibleInRegion(double wb, double nb, double eb, double sb, int i){
            if ((vx>=wb) && (vx<=eb) && (vy>=sb) && (vy<=nb)){
                /* Glyph hotspot is in the region. The glyph is obviously visible */
                return true;
            }
            else if (((vx-vw/2d)<=eb) && ((vx+vw/2d)>=wb) && ((vy-vh/2d)<=nb) && ((vy+vh/2d)>=sb)){
                /* Glyph is at least partially in region.
                   We approximate using the glyph bounding box, meaning that some glyphs not
                   actually visible can be projected and drawn (but they won't be displayed)) */
                return true;
            }
            return false;
        }

    @Override
        public boolean visibleInDisc(double dvx, double dvy, double dvr, Shape dvs, int camIndex, int jpx, int jpy, int dpr){
            return dvs.intersects(vx-vw/2d, vy-vh/2d, vw, vh);
        }

    /** Get the bounding box of this Glyph in virtual space coordinates.
     *@return west, north, east and south bounds in virtual space.
     */
    @Override
        public double[] getBounds(){
            double[] res = {vx-vw/2d,vy+vh/2d,vx+vw/2d,vy-vh/2d};
            return res;
        }

    @Override
        public short mouseInOut(int jpx, int jpy, int camIndex, double cvx, double cvy){
            if (coordInside(jpx, jpy, camIndex, cvx, cvy)){
                //if the mouse is inside the glyph
                if (!pc[camIndex].prevMouseIn){
                    //if it was not inside it last time, mouse has entered the glyph
                    pc[camIndex].prevMouseIn=true;
                    return Glyph.ENTERED_GLYPH;
                }
                //if it was inside last time, nothing has changed
                else {return Glyph.NO_CURSOR_EVENT;}  
            }
            else{
                //if the mouse is not inside the glyph
                if (pc[camIndex].prevMouseIn){
                    //if it was inside it last time, mouse has exited the glyph
                    pc[camIndex].prevMouseIn=false;
                    return Glyph.EXITED_GLYPH;
                }//if it was not inside last time, nothing has changed
                else {return Glyph.NO_CURSOR_EVENT;}
            }
        }

    @Override
        public void project(Camera c, Dimension d){
            int i=c.getIndex();
            coef = c.focal/(c.focal+c.altitude);
            //find coordinates of object's geom center wrt to camera center and project and translate in JPanel coords
            //translate in JPanel coords
            pc[i].cx = (int)Math.round((d.width/2) + (vx-c.vx)*coef);
            pc[i].cy = (int)Math.round((d.height/2) - (vy-c.vy)*coef);
            //project width and height
            pc[i].cw = (int)Math.round(Math.ceil(vw/2d*coef));
            pc[i].ch = (int)Math.round(Math.ceil(vh/2d*coef));
        }

    @Override
        public void projectForLens(Camera c, int lensWidth, int lensHeight, float lensMag, double lensx, double lensy){
            int i = c.getIndex();
            coef = c.focal/(c.focal+c.altitude) * lensMag;
            //find coordinates of object's geom center wrt to camera center and project and translate in JPanel coords
            //translate in JPanel coords
            pc[i].lcx = (int)Math.round(lensWidth/2 + (vx-lensx)*coef);
            pc[i].lcy = (int)Math.round(lensHeight/2 - (vy-lensy)*coef);
            //project width and height
            pc[i].lcw = (int)Math.round(Math.ceil(vw/2d*coef));
            pc[i].lch = (int)Math.round(Math.ceil(vh/2d*coef));
        }

    @Override
        public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
            if (alphaC != null && alphaC.getAlpha()==0){return;}
            g.setTransform(AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw, dy+pc[i].cy-pc[i].ch));
            g.transform(AffineTransform.getScaleInstance(coef, coef));
            if(updateRenderingInfo){
                chart.draw(g, new Rectangle2D.Double(0,0,vw,vh), chartRenderingInfo);
                updateRenderingInfo = false;
            } else {
                chart.draw(g, new Rectangle2D.Double(0,0,vw,vh));
            }
            g.setTransform(stdT);
        }

    @Override
        public void drawForLens(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
            draw(g, vW, vH, i, stdS, stdT, dx, dy);
        }

    /**
     * Convenience mouse click redispatch method, to be called from
     * e.g. a click1() handler.
     */
    public static void pickAndForward(ViewPanel v, MouseEvent e){
        Glyph[] pickList = v.getVCursor().getPicker().getPickedGlyphList();
        if(pickList.length == 0){
            return;
        }
        Glyph pickedGlyph = pickList[pickList.length - 1];
        if(pickedGlyph instanceof VChart){
        Point2D vsCoords = v.viewToSpaceCoords(v.cams[v.activeLayer], e.getX(), e.getY()); //XXX replace v.cams[v.activeLayer] by v.getActiveCamera
        VChart vchart = (VChart)pickedGlyph;
        ChartRenderingInfo cri = vchart.getRenderingInfo();
        Point2D pt = spaceToChart(vchart, vsCoords);
        if(cri != null){
            System.err.println("FOO!");
            vchart.chart.handleClick((int)(pt.getX()), (int)(pt.getY()), cri);
        }
        }
    }

    private static Point2D spaceToChart(VChart chart, Point2D vsCoords){
        return new Point2D.Double(vsCoords.getX() - (chart.vx - chart.getWidth()/2),
                (chart.vy + chart.getHeight()/2) - vsCoords.getY());
    }

	@Override
	public Shape getJava2DShape(){
		return new Rectangle2D.Double(vx-vw/2.0, vy-vh/2.0, vw, vh);
	}

}

