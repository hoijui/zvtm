/*   AUTHOR :           Benjamin Bach (bbach@lri.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.GPath;
import fr.inria.zvtm.glyphs.Glyph;

public class ExtraEdgeAppearance extends EdgeAppearance {

	private GPath edgePath;
	// start and end point offsets w.r.t respective matrices
	Point2D.Double[] offsets;
	static double CONTROL_POINT_OFFSET = NodeTrixViz.CELL_SIZE * 3;
	private float alpha = 1f;
	private static Color[] gradientColors = new Color[2];
	
	public static void setControlPointOffset(double s){
	    CONTROL_POINT_OFFSET = Math.round(NodeTrixViz.CELL_SIZE * s);
	}
	
	public ExtraEdgeAppearance(NTEdge edge) {
		super(edge);
	}

	public void updateColor(){
		if(edgePath.getColor() != null && edgePath.getColor().equals(ProjectColors.EXTRA_COLOR_FADE_OUT[ProjectColors.COLOR_SCHEME])) return;
		edgePath.setColor(edge.getColor());
	}
	
	@Override
	protected void clearGraphics() 
	{
		if(vs == null) return;
		vs.removeGlyph(edgePath);
	}

	@Override 
	public void createGraphics(VirtualSpace vs){
		this.vs = vs;
		createGraphics();
	}
	
	@Override
//	public void createGraphics(double x1, double y1, double x2, double y2, VirtualSpace vs) {
	public void createGraphics() {
		if(vs == null) return;
		double x1, y1, x2, y2;
		
        offsets = new Point2D.Double[2];
        
     //   System.out.println("Name: " + edge.getTail().getName());
        Point2D.Double tmp = edge.getTail().getMatrix().getPosition();
        Point2D.Double hmp = edge.getHead().getMatrix().getPosition();
        double angle = Math.atan2(edge.tail.getMatrix().bkg.vy-edge.head.getMatrix().bkg.vy, edge.tail.getMatrix().bkg.vx-edge.head.getMatrix().bkg.vx) + Math.PI;       
           
        if (angle > 7*Math.PI/4.0 || angle < Math.PI/4.0){
            // eastward
            x1 = (edge.tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2 : edge.tail.getLabelHalfWidth();
            y1 = edge.tail.wdy;
            x2 = (edge.head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2-2*edge.head.getMatrix().nodes.firstElement().getLabelHalfWidth() : -edge.head.getLabelHalfWidth();
            y2 = edge.head.wdy;
            offsets[0] = new Point2D.Double(x1, y1);
            offsets[1] = new Point2D.Double(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edge.getColor());
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x+CONTROL_POINT_OFFSET, tmp.y+offsets[0].y,
                                hmp.x+offsets[1].x-CONTROL_POINT_OFFSET, hmp.y+offsets[1].y, true);
        }
        else if (angle > 5*Math.PI/4.0){
            // southward
            x1 = edge.tail.ndx;
            y1 = (edge.tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2 : -edge.tail.getHeight();
            x2 = edge.head.ndx;
            y2 = (edge.head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2+2*edge.head.getMatrix().nodes.firstElement().getLabelHalfWidth() : edge.head.getHeight();
            offsets[0] = new Point2D.Double(x1, y1);
            offsets[1] = new Point2D.Double(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edge.getColor());
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x, tmp.y+offsets[0].y-CONTROL_POINT_OFFSET,
                                hmp.x+offsets[1].x, hmp.y+offsets[1].y+CONTROL_POINT_OFFSET, true);
        }
        else if (angle > 3*Math.PI/4.0){
            // westward
            x1 = (edge.tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2-2*edge.tail.getMatrix().nodes.firstElement().getLabelHalfWidth() : -edge.tail.getLabelHalfWidth();
            y1 = edge.tail.wdy;
            x2 = (edge.head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2 : edge.head.getLabelHalfWidth();
            y2 = edge.head.wdy;
            offsets[0] = new Point2D.Double(x1, y1);
            offsets[1] = new Point2D.Double(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edge.getColor());
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x-CONTROL_POINT_OFFSET, tmp.y+offsets[0].y,
                                hmp.x+offsets[1].x+CONTROL_POINT_OFFSET, hmp.y+offsets[1].y, true);
        }
        else {
            // angle >= Math.PI/4.0
            // northward
            x1 = edge.tail.ndx;
            y1 = (edge.tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE * edge.tail.getMatrix().nodes.size()/2+2*edge.tail.getMatrix().nodes.firstElement().getLabelHalfWidth() : edge.tail.getHeight();
            x2 = edge.head.ndx;
            y2 = (edge.head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2 : -edge.head.getHeight();
            offsets[0] = new Point2D.Double(x1, y1);
            offsets[1] = new Point2D.Double(x2, y2);
            edgePath = new GPath(tmp.x+offsets[0].x, tmp.y+offsets[0].y, 0, edge.getColor());
            edgePath.addCbCurve(hmp.x+offsets[1].x, hmp.y+offsets[1].y,
                                tmp.x+offsets[0].x, tmp.y+offsets[0].y+CONTROL_POINT_OFFSET,
                                hmp.x+offsets[1].x, hmp.y+offsets[1].y-CONTROL_POINT_OFFSET, true);
        }
        vs.addGlyph(edgePath);
        edgePath.setColor(edge.getColor());
        edgePath.setStrokeWidth(2);
        edgePath.setOwner(edge);
        assignAlpha();
        onTop();
	}

	@Override
	public void fade() {
		System.out.println("[EXTRA_EDGE] FADE");
//		edgePath.setVisible(false);
		gradientColors[0] = ProjectColors.EXTRA_COLOR_FADE_OUT[ProjectColors.COLOR_SCHEME];
		gradientColors[1] = ProjectColors.EXTRA_COLOR_FADE_OUT[ProjectColors.COLOR_SCHEME];	
//		edgePath.setGradientColors(gradientColors);
		edgePath.setSensitivity(false);
	}
	
	@Override
	public void show(){
		edgePath.setVisible(true);
		edgePath.setColor(edge.getColor());
		edgePath.setSensitivity(true);
		assignAlpha();
	}

	@Override
	public void highlight(Color c) {
		edgePath.setColor(c);
		edgePath.setTranslucencyValue(1);
	}

	@Override
	public void move(double x, double y) {
    	if(this.edgePath == null) return;
    	// x & y are actually ignored, computing new path geometry from matrix position
        Point2D.Double tmp = edge.getTail().getMatrix().getPosition();
        Point2D.Double hmp = edge.getHead().getMatrix().getPosition();
        Point2D.Double[] npos = new Point2D.Double[4];        
        double angle = Math.atan2(edge.tail.getMatrix().bkg.vy-edge.head.getMatrix().bkg.vy, edge.tail.getMatrix().bkg.vx-edge.head.getMatrix().bkg.vx) + Math.PI;        
        if (angle > 7*Math.PI/4.0 || angle < Math.PI/4.0){
            // eastward
            offsets[0].
            setLocation((edge.tail.
            		getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2 : edge.tail.getLabelHalfWidth(),
                                   edge.tail.wdy);
            offsets[1].
            setLocation((edge.head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2-2*edge.head.getMatrix().nodes.firstElement().getLabelHalfWidth() : -edge.head.getLabelHalfWidth(),
                                   edge.head.wdy);
            npos[0] = new Point2D.Double(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new Point2D.Double(tmp.x+offsets[0].x+CONTROL_POINT_OFFSET, tmp.y+offsets[0].y);
            npos[2] = new Point2D.Double(hmp.x+offsets[1].x-CONTROL_POINT_OFFSET, hmp.y+offsets[1].y);
            npos[3] = new Point2D.Double(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else if (angle > 5*Math.PI/4.0){
            // southward
            offsets[0].
            setLocation(edge.tail.ndx,
                                   (edge.tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2 : -edge.tail.getHeight());
            offsets[1].
            setLocation(edge.head.ndx,
                                   (edge.head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2+2*edge.head.getMatrix().nodes.firstElement().getLabelHalfWidth() : edge.head.getHeight());
            npos[0] = new Point2D.Double(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new Point2D.Double(tmp.x+offsets[0].x, tmp.y+offsets[0].y-CONTROL_POINT_OFFSET);
            npos[2] = new Point2D.Double(hmp.x+offsets[1].x, hmp.y+offsets[1].y+CONTROL_POINT_OFFSET);
            npos[3] = new Point2D.Double(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else if (angle > 3*Math.PI/4.0){
            // westward
            offsets[0].
            setLocation((edge.tail.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2-2*edge.tail.getMatrix().nodes.firstElement().getLabelHalfWidth() : -edge.tail.getLabelHalfWidth(),
                                   edge.tail.wdy);
            offsets[1].
            setLocation((edge.head.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2 : edge.head.getLabelHalfWidth(),
                                   edge.head.wdy);
            npos[0] = new Point2D.Double(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new Point2D.Double(tmp.x+offsets[0].x-CONTROL_POINT_OFFSET, tmp.y+offsets[0].y);
            npos[2] = new Point2D.Double(hmp.x+offsets[1].x+CONTROL_POINT_OFFSET, hmp.y+offsets[1].y);
            npos[3] = new Point2D.Double(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        else {
            // angle >= Math.PI/4.0
            // northward
            offsets[0].
            setLocation(edge.tail.ndx,
                                   (edge.tail.getMatrix().nodes.size() > 1) ? NodeTrixViz.CELL_SIZE*edge.tail.getMatrix().nodes.size()/2+2*edge.tail.getMatrix().nodes.firstElement().getLabelHalfWidth() : edge.tail.getHeight());
            offsets[1].
            setLocation(edge.head.ndx,
                                   (edge.head.getMatrix().nodes.size() > 1) ? -NodeTrixViz.CELL_SIZE*edge.head.getMatrix().nodes.size()/2 : -edge.head.getHeight());
            npos[0] = new Point2D.Double(tmp.x+offsets[0].x, tmp.y+offsets[0].y);
            npos[1] = new Point2D.Double(tmp.x+offsets[0].x, tmp.y+offsets[0].y+CONTROL_POINT_OFFSET);
            npos[2] = new Point2D.Double(hmp.x+offsets[1].x, hmp.y+offsets[1].y-CONTROL_POINT_OFFSET);
            npos[3] = new Point2D.Double(hmp.x+offsets[1].x, hmp.y+offsets[1].y);
        }
        edgePath.edit(npos, true);
        assignAlpha();
        
	}
	
	/**Assigns a new alpha value for this curve according to its length.
     */
    private void assignAlpha()
    {
    	alpha = 1 - (float)Math.min(Math.max(edgePath.getSize(), NodeTrixViz.EXTRA_ALPHA_MIN_LENGHT), NodeTrixViz.EXTRA_ALPHA_MAX_LENGHT)/(NodeTrixViz.EXTRA_ALPHA_MAX_LENGHT * (1 + NodeTrixViz.EXTRA_ALPHA_MIN));
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
		edgePath.setColor(edge.getColor());
		edgePath.setTranslucencyValue(alpha);
	}

	@Override
	public void select() {
		// TODO Auto-generated method stub

	}
}
