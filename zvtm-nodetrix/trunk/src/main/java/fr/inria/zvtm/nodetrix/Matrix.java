/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.io.File;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

public class Matrix {
    
    String name;
    NTNode[] nodes;
    
    VRectangle bkg;
    VText matrixLb;
    long matrixLbDY = 0;
    
    public Matrix(String name, NTNode[] nodes){
        this.name = name;
        this.nodes = nodes;
        for (NTNode node : nodes){
            node.setMatrix(this);
        }
    }
    
    public void createNodeGraphics(long x, long y, VirtualSpace vs){
        // matrix background
        bkg = new VRectangle(x, y, 0,
                             nodes.length*NodeTrixViz.CELL_SIZE/2, nodes.length*NodeTrixViz.CELL_SIZE/2,
                             NodeTrixViz.MATRIX_FILL_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
        vs.addGlyph(bkg);
        // matrix label
	    matrixLbDY = Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.length+.5+Math.sqrt(2*nodes.length)));
	    matrixLb = new VText(x, y-matrixLbDY, 0, NodeTrixViz.MATRIX_LABEL_COLOR, name, VText.TEXT_ANCHOR_MIDDLE, (float)Math.sqrt(2*nodes.length));
	    vs.addGlyph(matrixLb);
        for (int i=0;i<nodes.length;i++){
    	    nodes[i].createGraphics(-NodeTrixViz.CELL_SIZE/2*nodes.length-NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER,
    	                            Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.length-2*i-1)),
    	                            Math.round(NodeTrixViz.CELL_SIZE/2*(-nodes.length+2*i+1)),
    	                            NodeTrixViz.CELL_SIZE/2*nodes.length+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER,
    	                            vs);
    	    nodes[i].moveTo(x, y);
        }
    }
    
    public void createEdgeGraphics(VirtualSpace vs){
        for (NTNode node:nodes){
            if (node.getOutgoingEdges() != null){
                for (NTEdge oe:node.getOutgoingEdges()){
                    // values that are 0 is because we do not care (not used)
                    if (oe instanceof NTIntraEdge){
                        oe.createGraphics(0, oe.getTail().hdy, oe.getHead().vdx, 0, vs);
                    }
                    else {
                        // instanceof NTExtraEdge
                        oe.createGraphics(NodeTrixViz.CELL_SIZE*nodes.length/2, oe.getTail().hdy, oe.getHead().vdx,
                                          -NodeTrixViz.CELL_SIZE*oe.getHead().getMatrix().getSize()/2, vs);
                    }
                }
            }
        }
    }
    
    public boolean isConnectedTo(Matrix m){
        for (NTNode node : nodes){
            if (node.getOutgoingEdges() != null){
                for (NTEdge edge : node.getOutgoingEdges()){
                    if (edge.head.getMatrix() == m){
                        return true;
                    }
                }                
            }
        }
        return false;
    }
    
    public void moveTo(long x, long y){
        //TBW move matrix, its nodes, and all edges (inter/intra)
    }
    
    public LongPoint getPosition(){
        return bkg.getLocation();
    }
    
    public int getSize(){
        return nodes.length;
    }
    
    public String getName(){
        return name;
    }
    
    static int CELL_SIZE = 10;
    
    public static void setCellSize(int cs){
        CELL_SIZE = cs;
    }
    
}
