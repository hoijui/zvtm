/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.layout.jung;

import java.awt.Color;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;

import com.xerox.VTM.engine.LongPoint;
import com.xerox.VTM.engine.AnimManager;
import com.xerox.VTM.glyphs.VPath;
import com.xerox.VTM.glyphs.VSegment;
import net.claribole.zvtm.glyphs.DPath;

import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeShapeFunction;

import edu.uci.ics.jung.graph.decorators.EllipseVertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexSizeFunction;

public class EdgeTransformer {
	
	public static final short EDGE_LINE = 0;
	public static final short EDGE_QUAD_CURVE = 2;
	public static final short EDGE_CUBIC_CURVE = 3;
	
	protected static VertexShapeFunction vertexShapeFunction =  new EllipseVertexShapeFunction(new ConstantVertexSizeFunction(20), new ConstantVertexAspectRatioFunction(1.0f));

	public static void setVertexShapeFunction(VertexShapeFunction vsf){
		vertexShapeFunction = vsf;
	}
	
	public static VertexShapeFunction getVertexShapeFunction(){
		return vertexShapeFunction;
	}

	/** Get a VPath representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param edgeShape the edge type requested, one of EdgeTransformer.EDGE_*
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static VPath getVPath(Edge e, AbstractLayout l, short edgeShape, Color c){
		switch (edgeShape){
			case EDGE_LINE:{return getLineAsVPath(e, l, c);}
			case EDGE_QUAD_CURVE:{return getQuadCurveAsVPath(e, l, c);}
			case EDGE_CUBIC_CURVE:{return getCubicCurveAsVPath(e, l, c);}
			default:{return null;}
		}
	}
	
	/** Get a DPath representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param edgeShape the edge type requested, one of EdgeTransformer.EDGE_*
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static DPath getDPath(Edge e, AbstractLayout l, short edgeShape, Color c){
		switch (edgeShape){
			case EDGE_LINE:{return getLineAsDPath(e, l, c);}
			case EDGE_QUAD_CURVE:{return getQuadCurveAsDPath(e, l, c);}
			case EDGE_CUBIC_CURVE:{return getCubicCurveAsDPath(e, l, c);}
			default:{return null;}
		}
	}
	
	/** Get a straight VPath representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static VPath getLineAsVPath(Edge e, AbstractLayout l, Color c){
		Line2D curve = (Line2D)(new EdgeShape.Line()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2()};
		double[] tgt = new double[4];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 2);
		VPath p = new VPath(Math.round(tgt[0]), Math.round(tgt[1]), 0, c);
		p.addSegment(Math.round(tgt[2]), Math.round(tgt[3]), true);
		return p;
	}
	
	/** Get a straight DPath representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static DPath getLineAsDPath(Edge e, AbstractLayout l, Color c){
		Line2D curve = (Line2D)(new EdgeShape.Line()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2()};
		double[] tgt = new double[4];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 2);
		DPath p = new DPath(Math.round(tgt[0]), Math.round(tgt[1]), 0, c);
		p.addSegment(Math.round(tgt[2]), Math.round(tgt[3]), true);
		return p;
	}

	/** Get a VSegment representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static VSegment getLineAsVSegment(Edge e, AbstractLayout l, Color c){
		Line2D curve = (Line2D)(new EdgeShape.Line()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2()};
		double[] tgt = new double[4];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 2);
		VSegment s = new VSegment(Math.round(tgt[0]), Math.round(tgt[1]), 0, c, Math.round(tgt[2]), Math.round(tgt[3]));
		return s;
	}
	
	/** Update the position of an existing straight DPath representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param p the DPath representing this edge
	 *@param animDuration the duration of the animation to transition from the old position to the new one. Put 0 if no animation should be run.
	 *@param animator the ZVTM AnimManager instantiated by VirtualSpaceManager. Usually VirtualSpaceManager.animator. Can be null if animDuration == 0.
	 */	
	public static void updateLine(Edge e, AbstractLayout l, DPath p, int animDuration, AnimManager animator){
		Line2D curve = (Line2D)(new EdgeShape.Line()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2()};
		double[] tgt = new double[4];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 2);
		LongPoint[] coords = {new LongPoint(Math.round(tgt[0]),Math.round(tgt[1])),
			                  new LongPoint(Math.round(tgt[2]),Math.round(tgt[3]))};
		if (animDuration > 0){
			animator.createPathAnimation(animDuration, AnimManager.DP_TRANS_SIG_ABS, coords, p.getID(), null);
		}
		else {
			p.edit(coords, true);			
		}
	}
	
	/** Update the position of an existing VSegment representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param p the DPath representing this edge
	 */	
	public static void updateLine(Edge e, AbstractLayout l, VSegment s){
		Line2D curve = (Line2D)(new EdgeShape.Line()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2()};
		double[] tgt = new double[4];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 2);
		s.setEndPoints(Math.round(tgt[0]), Math.round(tgt[1]), Math.round(tgt[2]), Math.round(tgt[3]));
	}
	
	/** Get a VPath composed of a quadratic curve representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static VPath getQuadCurveAsVPath(Edge e, AbstractLayout l, Color c){
		QuadCurve2D curve = (QuadCurve2D)(new EdgeShape.QuadCurve()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2(), curve.getCtrlX(), curve.getCtrlY()};
		double[] tgt = new double[6];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 3);
		VPath p = new VPath(Math.round(tgt[0]), Math.round(tgt[1]), 0, c);
		p.addQdCurve(Math.round(tgt[2]), Math.round(tgt[3]), Math.round(tgt[4]), Math.round(tgt[5]), true);
		return p;
	}
	
	/** Get a DPath composed of a quadratic curve representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static DPath getQuadCurveAsDPath(Edge e, AbstractLayout l, Color c){
		QuadCurve2D curve = (QuadCurve2D)(new EdgeShape.QuadCurve()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2(), curve.getCtrlX(), curve.getCtrlY()};
		double[] tgt = new double[6];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 3);
		DPath p = new DPath(Math.round(tgt[0]), Math.round(tgt[1]), 0, c);
		p.addQdCurve(Math.round(tgt[2]), Math.round(tgt[3]), Math.round(tgt[4]), Math.round(tgt[5]), true);
		return p;
	}
	
	/** Update the position of an existing quadratic curve representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param p the DPath representing this edge
	 *@param animDuration the duration of the animation to transition from the old position to the new one. Put 0 if no animation should be run.
	 *@param animator the ZVTM AnimManager instantiated by VirtualSpaceManager. Usually VirtualSpaceManager.animator. Can be null if animDuration == 0.
	 */	
	public static void updateQuadCurve(Edge e, AbstractLayout l, DPath p, int animDuration, AnimManager animator){
		QuadCurve2D curve = (QuadCurve2D)(new EdgeShape.QuadCurve()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2(), curve.getCtrlX(), curve.getCtrlY()};
		double[] tgt = new double[6];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 3);
		LongPoint[] coords = {new LongPoint(Math.round(tgt[0]), Math.round(tgt[1])),
			                  new LongPoint(Math.round(tgt[4]), Math.round(tgt[5])),
							  new LongPoint(Math.round(tgt[2]), Math.round(tgt[3]))};
   	    if (animDuration > 0){
   	    	animator.createPathAnimation(animDuration, AnimManager.DP_TRANS_SIG_ABS, coords, p.getID(), null);
   	    }
   	    else {
   	    	p.edit(coords, true);			
   	    }        
	}

	/** Get a VPath composed of a cubic curve representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static VPath getCubicCurveAsVPath(Edge e, AbstractLayout l, Color c){
		CubicCurve2D curve = (CubicCurve2D)(new EdgeShape.CubicCurve()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2(),
			            curve.getCtrlX1(), curve.getCtrlY1(), curve.getCtrlX2(), curve.getCtrlY2()};
		double[] tgt = new double[8];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 4);
		VPath p = new VPath(Math.round(tgt[0]), Math.round(tgt[1]), 0, c);
		p.addCbCurve(Math.round(tgt[2]), Math.round(tgt[3]),
		             Math.round(tgt[4]), Math.round(tgt[5]),
		             Math.round(tgt[6]), Math.round(tgt[7]), true);
		return p;
	}
	
	/** Get a DPath composed of a cubic curve representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param c stroke color of VPath object
	 *@return null if edgeShape does not correspond to a known edge shape
	 */
	public static DPath getCubicCurveAsDPath(Edge e, AbstractLayout l, Color c){
		CubicCurve2D curve = (CubicCurve2D)(new EdgeShape.CubicCurve()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2(),
			            curve.getCtrlX1(), curve.getCtrlY1(), curve.getCtrlX2(), curve.getCtrlY2()};
		double[] tgt = new double[8];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 4);
		DPath p = new DPath(Math.round(tgt[0]), Math.round(tgt[1]), 0, c);
		p.addCbCurve(Math.round(tgt[2]), Math.round(tgt[3]),
		             Math.round(tgt[4]), Math.round(tgt[5]),
		             Math.round(tgt[6]), Math.round(tgt[7]), true);
		return p;
	}

	/** Update the position of an existing cubic curve representing a given edge in the graph.
	 *@param e the Jung edge
	 *@param l the layout that produced the graph's geometry
	 *@param p the DPath representing this edge
	 *@param animDuration the duration of the animation to transition from the old position to the new one. Put 0 if no animation should be run.
	 *@param animator the ZVTM AnimManager instantiated by VirtualSpaceManager. Usually VirtualSpaceManager.animator. Can be null if animDuration == 0.
	 */	
	public static void updateCubicCurve(Edge e, AbstractLayout l, DPath p, int animDuration, AnimManager animator){
		CubicCurve2D curve = (CubicCurve2D)(new EdgeShape.CubicCurve()).getShape(e);
		double[] src = {curve.getX1(), curve.getY1(), curve.getX2(), curve.getY2(),
			            curve.getCtrlX1(), curve.getCtrlY1(), curve.getCtrlX2(), curve.getCtrlY2()};
		double[] tgt = new double[8];
		getTransform(e, l, curve).transform(src, 0, tgt, 0, 4);
		LongPoint[] coords = {new LongPoint(Math.round(tgt[0]), Math.round(tgt[1])),
                              new LongPoint(Math.round(tgt[4]), Math.round(tgt[5])),
                              new LongPoint(Math.round(tgt[6]), Math.round(tgt[7])),
							  new LongPoint(Math.round(tgt[2]), Math.round(tgt[3]))};
   	    if (animDuration > 0){
   	    	animator.createPathAnimation(animDuration, AnimManager.DP_TRANS_SIG_ABS, coords, p.getID(), null);
   	    }
   	    else {
   	    	p.edit(coords, true);			
   	    }        
	}
	
	private static AffineTransform getTransform(Edge e, AbstractLayout l, Shape edgeShape){
		/* code inspired by edu.uci.ics.jung.visualization.PluggableRenderer (Jung 1.7.6) */
		Pair ep = e.getEndpoints();		
		Vertex v1 = (Vertex)ep.getFirst();
        Vertex v2 = (Vertex)ep.getSecond();
		Coordinates c1 = l.getCoordinates(v1);
		Coordinates c2 = l.getCoordinates(v2);
		double dx = c2.getX() - c1.getX();
		double dy = c2.getY() - c1.getY();
		AffineTransform xform = AffineTransform.getTranslateInstance(c1.getX(), c1.getY());
		if (v1.equals(v2)){
			// if the edge is a loop
	        Shape s2 = vertexShapeFunction.getShape(v2);
			Rectangle2D s2Bounds = s2.getBounds2D();
			xform.scale(s2Bounds.getWidth(), s2Bounds.getHeight());
			xform.translate(0, -edgeShape.getBounds2D().getWidth()/2);			
		}
		else {
			xform.rotate((float) Math.atan2(dy, dx));
			xform.scale(Math.sqrt(dx*dx + dy*dy), 1.0);				
		}
		return xform;
	}
		
}