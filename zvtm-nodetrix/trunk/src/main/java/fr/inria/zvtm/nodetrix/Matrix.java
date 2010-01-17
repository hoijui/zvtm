/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
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
    
    // bkg is both the matrix background and the label background for single node matrices
    VRectangle bkg;
    VRectangle[] label_bkg;
    VText matrixLb;
    long matrixLbDX = 0;
    long matrixLbDY = 0;
    
    public Matrix(String name, NTNode[] nodes){
        this.name = name;
        this.nodes = nodes;
        for (NTNode node : nodes){
            node.setMatrix(this);
        }
    }
    
    void createNodeGraphics(long x, long y, VirtualSpace vs){
	    // nodes
	    if (nodes.length > 1){
            // matrix background
            bkg = new VRectangle(x, y, 0,
                                 nodes.length*NodeTrixViz.CELL_SIZE/2, nodes.length*NodeTrixViz.CELL_SIZE/2,
                                 NodeTrixViz.MATRIX_FILL_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
            vs.addGlyph(bkg);
            bkg.setOwner(this);
            // matrix label
    	    matrixLbDX = -Math.round(NodeTrixViz.CELL_SIZE/2*(1.1*nodes.length));
    	    matrixLbDY = -Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.length+.5+Math.sqrt(2*nodes.length)));
    	    matrixLb = new VText(x+matrixLbDX, y+matrixLbDY, 0, NodeTrixViz.MATRIX_LABEL_COLOR, name, VText.TEXT_ANCHOR_END, (float)Math.sqrt(2*nodes.length));
    	    vs.addGlyph(matrixLb);
    	    matrixLb.setOwner(this);
    	    // node labels
            for (int i=0;i<nodes.length;i++){
        	    nodes[i].createGraphics(-NodeTrixViz.CELL_SIZE/2*nodes.length-NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER,
        	                            Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.length-2*i-1)),
        	                            Math.round(NodeTrixViz.CELL_SIZE/2*(-nodes.length+2*i+1)),
        	                            NodeTrixViz.CELL_SIZE/2*nodes.length+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER,
        	                            vs, false);
        	    nodes[i].moveTo(x, y);
            }	        
	    }
	    else {
	        // bkg is both the matrix background and the label background for single node matrices
            bkg = new VRectangle(x, y, 0,
                                 nodes.length*NodeTrixViz.CELL_SIZE/2, nodes.length*NodeTrixViz.CELL_SIZE/2,
                                 NodeTrixViz.MATRIX_NODE_LABEL_BKG_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
            vs.addGlyph(bkg);
            bkg.setOwner(this);
	        // if matrix contains a single node, only show a horizontal label
	        nodes[0].createGraphics(0, 0, 0, 0, vs, true);
    	    nodes[0].moveTo(x, y);
	    }
    }
    
    void finishCreateNodeGraphics(VirtualSpace vs){
        long max_length = nodes[0].getLabelWidth();
        for (int i=1;i<nodes.length;i++){
            if (nodes[i].getLabelWidth() > max_length){
                max_length = nodes[i].getLabelWidth();
            }
        }
        label_bkg = new VRectangle[2];
        if (nodes.length > 1){
            // west
            label_bkg[0] = new VRectangle(bkg.vx-bkg.getWidth()-max_length/2-NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, bkg.vy, 0,
                                          max_length/2+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, bkg.getHeight(),
                                          NodeTrixViz.MATRIX_NODE_LABEL_BKG_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
            // north
            label_bkg[1] = new VRectangle(bkg.vx, bkg.vy+bkg.getHeight()+max_length/2+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, 0,
                                          bkg.getWidth(), max_length/2+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER,
                                          NodeTrixViz.MATRIX_NODE_LABEL_BKG_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);            
            for (VRectangle lb:label_bkg){
                if (lb != null){
                    vs.addGlyph(lb);
                    vs.atBottom(lb);
                    lb.setOwner(this);
                    bkg.stick(lb);
                }
            }
            bkg.stick(matrixLb);
        }
        else {
            label_bkg[0] = bkg;
            bkg.setWidth(max_length/2+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER);
        }
    }
    
    void createEdgeGraphics(VirtualSpace vs){
        long reflexiveProp4SingleNodeMatOffset = NodeTrixViz.CELL_SIZE / 2;
        for (NTNode node:nodes){
            if (node.getOutgoingEdges() != null){
                for (NTEdge oe:node.getOutgoingEdges()){
                    // values that are 0 is because we do not care (not used)
                    if (oe instanceof NTIntraEdge){
                        if (oe.tail == oe.head && nodes.length == 1){
                            oe.createGraphics(0, oe.getTail().wdy-bkg.getHeight()-NodeTrixViz.CELL_SIZE/2,
                                              oe.getHead().ndx-bkg.getWidth()+reflexiveProp4SingleNodeMatOffset, 0, vs);

                            // remember
                            reflexiveProp4SingleNodeMatOffset += NodeTrixViz.CELL_SIZE;

                        }
                        else {
                            oe.createGraphics(0, oe.getTail().wdy,
                                              oe.getHead().ndx, 0, vs);
                        }
                    }
                    else {
                        // instanceof NTExtraEdge
                        oe.createGraphics(0, 0, 0, 0, vs);
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
    
    public void move(long x, long y){
        bkg.move(x, y);
        for (NTNode node : nodes){
            node.move(x, y);
            if (node.getOutgoingEdges() != null){
                for (NTEdge edge : node.getOutgoingEdges()){
                    edge.move(x,y);
                }
            }
            if (node.getIncomingEdges() != null){
                for (NTEdge edge : node.getIncomingEdges()){
                    if (edge instanceof NTExtraEdge){
                        // do it only for extra edges because for intra edges
                        // we have already moved them in the above loop
                        // (intra edges connect nodes within the same matrix)
                        edge.move(x,y);
                    }
                }
            }
        }
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
