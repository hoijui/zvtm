/*   AUTHOR :           Benjamin Bach (bbach@lri.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.GPath;

public class ExtraEdgeAppearance extends EdgeAppearance {

	GPath edgePath;
	// start and end point offsets w.r.t respective matrices
	LongPoint[] offsets;
	static final long CONTROL_POINT_OFFSET = NodeTrixViz.CELL_SIZE * 3;
	private float alpha = 1f;
	    
	
	public ExtraEdgeAppearance(NTEdge edge) {
		super(edge);
	}

	public void updateColor(){
		edgePath.setColor(edge.edgeColor);
	}
	
	@Override
	protected void clearGraphics() 
	{
		if(vs == null) return;
		vs.removeGlyph(edgePath);
    	edgePath = null;
//    	offsets = new LongPoint[0];
	}

	@Override 
	public void createGraphics(VirtualSpace vs){
		this.vs = vs;
		createGraphics();
	}
	
	@Override
//	public void createGraphics(long x1, long y1, long x2, long y2, VirtualSpace vs) {
	public void createGraphics() {
		if(vs == null) return;

		long x1, y1, x2, y2;
		
        offsets = new LongPoint[2];
        LongPoint tmp = edge.getTail().getMatrix().getPosition();
        LongPoint hmp = edge.getHead().getMatrix().getPosition();
        double angle = Math.atan2(edge.tail.getMatrix().bkg.vy-edge.head.getMatrix().bkg.vy, edge.tail.getMatrix().bkg.vx-edge.head.getMatrix().bkg.vx) + Math.PI;       
           
        if (angle > 7*Math.PI/4.0 || angle < Math.PI/4.0){
            x1 = (edge.tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2 : edge.tail.getWidth();
            y1 = edge.tail.wdy;
            x2 = (edge.head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2-2*edge.head.getMatrix().nodes.firstElement().getBoxWidth(true) : -edge.head.getWidth();
            y2 = edge.head.wdy;
            offsets[0] = new LongPoint(x1, y1);
            offsets[1] = new LongPoint(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edge.edgeColor);
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x+CONTROL_POINT_OFFSET, tmp.y+offsets[0].y,
                                hmp.x+offsets[1].x-CONTROL_POINT_OFFSET, hmp.y+offsets[1].y, true);
        }
        else if (angle > 5*Math.PI/4.0){
            x1 = edge.tail.ndx;
            y1 = (edge.tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2 : -edge.tail.getWidth();
            x2 = edge.head.ndx;
            y2 = (edge.head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2+2*edge.head.getMatrix().nodes.firstElement().getBoxWidth(true) : edge.head.getWidth();
            offsets[0] = new LongPoint(x1, y1);
            offsets[1] = new LongPoint(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edge.edgeColor);
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x, tmp.y+offsets[0].y-CONTROL_POINT_OFFSET,
                                hmp.x+offsets[1].x, hmp.y+offsets[1].y+CONTROL_POINT_OFFSET, true);
        }
        else if (angle > 3*Math.PI/4.0){
            x1 = (edge.tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2-2*edge.tail.getMatrix().nodes.firstElement().getBoxWidth(true) : -edge.tail.getWidth();
            y1 = edge.tail.wdy;
            x2 = (edge.head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2 : edge.head.getWidth();
            y2 = edge.head.wdy;
            offsets[0] = new LongPoint(x1, y1);
            offsets[1] = new LongPoint(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edge.edgeColor);
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x-CONTROL_POINT_OFFSET, tmp.y+offsets[0].y,
                                hmp.x+offsets[1].x+CONTROL_POINT_OFFSET, hmp.y+offsets[1].y, true);
        }
        else {
            // angle >= Math.PI/4.0
            x1 = edge.tail.ndx;
            y1 = (edge.tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE * edge.tail.getMatrix().nodes.size()/2+2*edge.tail.getMatrix().nodes.firstElement().getBoxWidth(true) : edge.tail.getWidth();
            x2 = edge.head.ndx;
            y2 = (edge.head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2 : -edge.head.getWidth();
            offsets[0] = new LongPoint(x1, y1);
            offsets[1] = new LongPoint(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edge.edgeColor);
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x, tmp.y+offsets[0].y+CONTROL_POINT_OFFSET,
                                hmp.x+offsets[1].x, hmp.y+offsets[1].y-CONTROL_POINT_OFFSET, true);
        }
        vs.addGlyph(edgePath);
//        edgePath.setColor(edgeColor);
        edgePath.setStrokeWidth(2);
        edgePath.setOwner(edge);
        assignAlpha();
        this.vs = vs;
        onTop();
	}

	@Override
	public void fade() {
		// TODO Auto-generated method stub

	}

	@Override
	public void highlight(Color c) {
		edgePath.setColor(c);
		edgePath.setTranslucencyValue(1);
	}

	@Override
	public void move(long x, long y) {
    	if(this.edgePath == null) return;
    	// x & y are actually ignored, computing new path geometry from matrix position
        LongPoint tmp = edge.getTail().getMatrix().getPosition();
        LongPoint hmp = edge.getHead().getMatrix().getPosition();
        LongPoint[] npos = new LongPoint[4];        
        double angle = Math.atan2(edge.tail.getMatrix().bkg.vy-edge.head.getMatrix().bkg.vy, edge.tail.getMatrix().bkg.vx-edge.head.getMatrix().bkg.vx) + Math.PI;        
        if (angle > 7*Math.PI/4.0 || angle < Math.PI/4.0){	
            offsets[0].
            setLocation((edge.tail.
            		getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2 : edge.tail.getWidth(),
                                   edge.tail.wdy);
            offsets[1].
            setLocation((edge.head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2-2*edge.head.getMatrix().nodes.firstElement().getBoxWidth(true) : -edge.head.getWidth(),
                                   edge.head.wdy);
            npos[0] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new LongPoint(tmp.x+offsets[0].x+CONTROL_POINT_OFFSET, tmp.y+offsets[0].y);
            npos[2] = new LongPoint(hmp.x+offsets[1].x-CONTROL_POINT_OFFSET, hmp.y+offsets[1].y);
            npos[3] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else if (angle > 5*Math.PI/4.0){
            offsets[0].
            setLocation(edge.tail.ndx,
                                   (edge.tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2 : -edge.tail.getWidth());
            offsets[1].
            setLocation(edge.head.ndx,
                                   (edge.head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2+2*edge.head.getMatrix().nodes.firstElement().getBoxWidth(true) : edge.head.getWidth());
            npos[0] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y-CONTROL_POINT_OFFSET);
            npos[2] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y+CONTROL_POINT_OFFSET);
            npos[3] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else if (angle > 3*Math.PI/4.0){
            offsets[0].
            setLocation((edge.tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2-2*edge.tail.getMatrix().nodes.firstElement().getBoxWidth(true) : -edge.tail.getWidth(),
                                   edge.tail.wdy);
            offsets[1].
            setLocation((edge.head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2 : edge.head.getWidth(),
                                   edge.head.wdy);
            npos[0] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new LongPoint(tmp.x+offsets[0].x-CONTROL_POINT_OFFSET, tmp.y+offsets[0].y);
            npos[2] = new LongPoint(hmp.x+offsets[1].x+CONTROL_POINT_OFFSET, hmp.y+offsets[1].y);
            npos[3] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else {
            // angle >= Math.PI/4.0
            offsets[0].
            setLocation(edge.tail.ndx,
                                   (edge.tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2+2*edge.tail.getMatrix().nodes.firstElement().getBoxWidth(true) : edge.tail.getWidth());
            offsets[1].
            setLocation(edge.head.ndx,
                                   (edge.head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2 : -edge.head.getWidth());
            npos[0] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new LongPoint(tmp.x+offsets[0].x, tmp.y+offsets[0].y+CONTROL_POINT_OFFSET);
            npos[2] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y-CONTROL_POINT_OFFSET);
            npos[3] = new LongPoint(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        edgePath.edit(npos, true);
        assignAlpha();
        
	}
	
	/**Assigns a new alpha value for this curve according to its length.
     */
    private void assignAlpha()
    {
    	alpha = 1 - Math.min(Math.max(edgePath.getSize(), NodeTrixViz.EXTRA_ALPHA_MIN_LENGHT), NodeTrixViz.EXTRA_ALPHA_MAX_LENGHT)/(NodeTrixViz.EXTRA_ALPHA_MAX_LENGHT * (1 + NodeTrixViz.EXTRA_ALPHA_MIN));
    	edgePath.setTranslucencyValue(alpha);
    }
    
    @Override
    public void updatePosition(){
    	move(0,0);	
    }
    
	@Override
	public void onTop() {
		// TODO Auto-generated method stub
	}

	@Override
	public void reset() {
		edgePath.setColor(edge.edgeColor);
		edgePath.setTranslucencyValue(alpha);
	}

	@Override
	public void select() {
		// TODO Auto-generated method stub

	}
}
