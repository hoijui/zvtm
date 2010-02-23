	/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.GPath;

public class NTExtraEdge extends NTEdge {
    
    GPath edgePath;
    // start and end point offsets w.r.t respective matrices
    LongPoint[] offsets;
    private int state = NodeTrixViz.IA_STATE_DEFAULT;
    static final long CONTROL_POINT_OFFSET = NodeTrixViz.CELL_SIZE * 3;
    private float alpha = 1f;
    private boolean isdrawn = false;
    
    public NTExtraEdge(NTNode t, NTNode h, Color c){
    	super(t,h,1);
        this.tail = t;
        this.head = h;
        this.edgeColor = c;
    }
    
    @Override
    protected void fade() {
    	// TODO Auto-generated method stub
    	
    }
    
    @Override
    protected void highlight(Color c) {
    	edgePath.setColor(c);
    	edgePath.setTranslucencyValue(1);
    }
    
    @Override
    protected void reset() {
    	edgePath.setColor(edgeColor);
    	edgePath.setTranslucencyValue(alpha);
    }
    
    @Override
    protected void select() {
    	// TODO Auto-generated method stub
    	
    }

    /**Assigns a new alpha value for this curve according to its length.
     */
    public void assignAlpha()
    {
    	alpha = 1 - Math.min(Math.max(edgePath.getSize(), NodeTrixViz.EXTRA_ALPHA_MIN_LENGHT), NodeTrixViz.EXTRA_ALPHA_MAX_LENGHT)/(NodeTrixViz.EXTRA_ALPHA_MAX_LENGHT * (1 + NodeTrixViz.EXTRA_ALPHA_MIN));
    	edgePath.setTranslucencyValue(alpha);
    }
    
    


    void createGraphics(long x1, long y1, long x2, long y2, VirtualSpace vs){
    	System.out.println("DRAW EXTRA EDGE");

    	this.isdrawn = true;
        // initial values of x1, y1, x2, y2 are ignored (should be 0 anyway)
    	
//    	System.out.println("[NT_EXTRA_EDGE] create graphics ");
    	
        offsets = new LongPoint[2];
        LongPoint tmp = this.getTail().getMatrix().getPosition();
        LongPoint hmp = this.getHead().getMatrix().getPosition();
        double angle = Math.atan2(tail.getMatrix().bkg.vy-head.getMatrix().bkg.vy, tail.getMatrix().bkg.vx-head.getMatrix().bkg.vx) + Math.PI;       
           
        if (angle > 7*Math.PI/4.0 || angle < Math.PI/4.0){
            x1 = (tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.size()/2 : tail.getWidth();
            y1 = tail.wdy;
            x2 = (head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*head.getMatrix().nodes.size()/2-2*head.getMatrix().nodes.firstElement().getBoxWidth(true) : -head.getWidth();
            y2 = head.wdy;
            offsets[0] = new LongPoint(x1, y1);
            offsets[1] = new LongPoint(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edgeColor);
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x+CONTROL_POINT_OFFSET, tmp.y+offsets[0].y,
                                hmp.x+offsets[1].x-CONTROL_POINT_OFFSET, hmp.y+offsets[1].y, true);
        }
        else if (angle > 5*Math.PI/4.0){
            x1 = tail.ndx;
            y1 = (tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.size()/2 : -tail.getWidth();
            x2 = head.ndx;
            y2 = (head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*head.getMatrix().nodes.size()/2+2*head.getMatrix().nodes.firstElement().getBoxWidth(true) : head.getWidth();
            offsets[0] = new LongPoint(x1, y1);
            offsets[1] = new LongPoint(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edgeColor);
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x, tmp.y+offsets[0].y-CONTROL_POINT_OFFSET,
                                hmp.x+offsets[1].x, hmp.y+offsets[1].y+CONTROL_POINT_OFFSET, true);
        }
        else if (angle > 3*Math.PI/4.0){
            x1 = (tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.size()/2-2*tail.getMatrix().nodes.firstElement().getBoxWidth(true) : -tail.getWidth();
            y1 = tail.wdy;
            x2 = (head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*head.getMatrix().nodes.size()/2 : head.getWidth();
            y2 = head.wdy;
            offsets[0] = new LongPoint(x1, y1);
            offsets[1] = new LongPoint(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edgeColor);
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x-CONTROL_POINT_OFFSET, tmp.y+offsets[0].y,
                                hmp.x+offsets[1].x+CONTROL_POINT_OFFSET, hmp.y+offsets[1].y, true);
        }
        else {
            // angle >= Math.PI/4.0
            x1 = tail.ndx;
            y1 = (tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE * tail.getMatrix().nodes.size()/2+2*tail.getMatrix().nodes.firstElement().getBoxWidth(true) : tail.getWidth();
            x2 = head.ndx;
            y2 = (head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*head.getMatrix().nodes.size()/2 : -head.getWidth();
            offsets[0] = new LongPoint(x1, y1);
            offsets[1] = new LongPoint(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edgeColor);
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x, tmp.y+offsets[0].y+CONTROL_POINT_OFFSET,
                                hmp.x+offsets[1].x, hmp.y+offsets[1].y-CONTROL_POINT_OFFSET, true);
        }
        vs.addGlyph(edgePath);
//        edgePath.setColor(edgeColor);
        edgePath.setStrokeWidth(3);
        edgePath.setOwner(this);
        assignAlpha();
    }
    
	@Override
    public void cleanGraphics(VirtualSpace vs){
    	vs.removeGlyph(edgePath);
    	edgePath = null;
//    	offsets = new LongPoint[0];
    }
    
    void moveTo(long x, long y){
        // does not make sense, moving either head or tail
        // see moveHeadTo() and moveTailTo()
    }
    
    void move(long x, long y){
    	System.out.println("[NT_EXTRA_EDGE] HEAD " + head.getMatrix().name + ", "+ head.name);
    	System.out.println("[NT_EXTRA_EDGE] TAIL " + tail.getMatrix().name + ", "+ tail.name);
    	System.out.println("[NT_EXTRA_EDGE] is Drawn: " + this.isdrawn );
    	try{
    		
    	// x & y are actually ignored, computing new path geometry from matrix position
        LongPoint tmp = this.getTail().getMatrix().getPosition();
        LongPoint hmp = this.getHead().getMatrix().getPosition();
        LongPoint[] npos = new LongPoint[4];        
        double angle = Math.atan2(tail.getMatrix().bkg.vy-head.getMatrix().bkg.vy, tail.getMatrix().bkg.vx-head.getMatrix().bkg.vx) + Math.PI;        
        if (angle > 7*Math.PI/4.0 || angle < Math.PI/4.0){
            offsets[0].
            setLocation((tail.
            		getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.size()/2 : tail.getWidth(),
                                   tail.wdy);
            offsets[1].
            setLocation((head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*head.getMatrix().nodes.size()/2-2*head.getMatrix().nodes.firstElement().getBoxWidth(true) : -head.getWidth(),
                                   head.wdy);
            npos[0] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new LongPoint(tmp.x+offsets[0].x+CONTROL_POINT_OFFSET, tmp.y+offsets[0].y);
            npos[2] = new LongPoint(hmp.x+offsets[1].x-CONTROL_POINT_OFFSET, hmp.y+offsets[1].y);
            npos[3] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else if (angle > 5*Math.PI/4.0){
            offsets[0].
            setLocation(tail.ndx,
                                   (tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.size()/2 : -tail.getWidth());
            offsets[1].
            setLocation(head.ndx,
                                   (head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*head.getMatrix().nodes.size()/2+2*head.getMatrix().nodes.firstElement().getBoxWidth(true) : head.getWidth());
            npos[0] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y-CONTROL_POINT_OFFSET);
            npos[2] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y+CONTROL_POINT_OFFSET);
            npos[3] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else if (angle > 3*Math.PI/4.0){
            offsets[0].
            setLocation((tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.size()/2-2*tail.getMatrix().nodes.firstElement().getBoxWidth(true) : -tail.getWidth(),
                                   tail.wdy);
            offsets[1].
            setLocation((head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*head.getMatrix().nodes.size()/2 : head.getWidth(),
                                   head.wdy);
            npos[0] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new LongPoint(tmp.x+offsets[0].x-CONTROL_POINT_OFFSET, tmp.y+offsets[0].y);
            npos[2] = new LongPoint(hmp.x+offsets[1].x+CONTROL_POINT_OFFSET, hmp.y+offsets[1].y);
            npos[3] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else {
            // angle >= Math.PI/4.0
            offsets[0].
            setLocation(tail.ndx,
                                   (tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*tail.getMatrix().nodes.size()/2+2*tail.getMatrix().nodes.firstElement().getBoxWidth(true) : tail.getWidth());
            offsets[1].
            setLocation(head.ndx,
                                   (head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*head.getMatrix().nodes.size()/2 : -head.getWidth());
            npos[0] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y+CONTROL_POINT_OFFSET);
            npos[2] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y-CONTROL_POINT_OFFSET);
            npos[3] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        edgePath.edit(npos, true);
    	}catch(Exception e){e.printStackTrace();}
    }
    
    void moveHeadTo(long x, long y){
        // TBW
    }

    void moveTailTo(long x, long y){
        // TBW
    }


    
}
