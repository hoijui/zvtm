/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

public class Matrix {
    
    String name;
    NTNode[] nodes;
    HashSet<NTIntraEdge> intraEdges = new HashSet<NTIntraEdge>();
    
    // bkg is both the matrix background and the label background for single node matrices
    VRectangle bkg;
//  VRectangle[] label_bkg;
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
    	    Color c;
        	float b;
    	    int a = this.nodes.length;
        	float min = .7f;
        	float max = 1.0f;
        	float diff = max-min;
        	float step = (1/((float)a-1))*diff;
        	for (int i=0;i<nodes.length;i++)
    	    {
        		b = max - step*i;
        		c = Color.getHSBColor(0.1f, 0.8f, b);
        		nodes[i].createGraphics(-NodeTrixViz.CELL_SIZE/2*nodes.length,
        	                            Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.length-2*i-1)),
        	                            Math.round(NodeTrixViz.CELL_SIZE/2*(-nodes.length+2*i+1)),
        	                            NodeTrixViz.CELL_SIZE/2*nodes.length,
        	                            vs, false, c);
        	    nodes[i].moveTo(x, y);
            }	        
	    }
	    else {
	        // if matrix contains a single node, only show a horizontal label
	        nodes[0].createGraphics(0, 0, 0, 0, vs, true, Color.getHSBColor(0.1f, 0.8f, 1.0f));
    	    nodes[0].moveTo(x, y);
    	    bkg = new VRectangle(x, y, 0, NodeTrixViz.CELL_SIZE/2, 1, Color.white,  Color.white, 0f);
	    }
    }
    
    void finishCreateNodeGraphics(VirtualSpace vs){
        //estimating maximal length of node labels
    	long max_length = nodes[0].getLabelWidth();
        int i =0;
        for (NTNode n : this.nodes){
        	if (nodes[i].getLabelWidth() > max_length){
                max_length = nodes[i].getLabelWidth();
            }
        	i++;
        }
        max_length += NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER * 2;
        
        //creating background boxes for each node
        i = 1;
        for(NTNode n : this.nodes)
        {
        	n.setBackgroundBox(max_length);
        	if(this.nodes.length == 1) break;
        	
        	//GRID PATTERN
        	if(i % 2 == 0)
        	{
        		VRectangle gGridV = new VRectangle(bkg.vx + n.ndx, bkg.vy,0, NodeTrixViz.CELL_SIZE/2, bkg.getWidth(), NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_TRANSLUCENCY);
        		gGridV.setDrawBorder(false);
        		vs.addGlyph(gGridV);
        		gGridV.setSensitivity(false);
        		bkg.stick(gGridV);
        		
        		VRectangle gGridH = new VRectangle(bkg.vx,bkg.vy+ n.wdy,0, bkg.getWidth(), NodeTrixViz.CELL_SIZE/2, NodeTrixViz.GRID_COLOR,  NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_TRANSLUCENCY);
        		gGridH.setDrawBorder(false);
        		vs.addGlyph(gGridH);
        		gGridH.setSensitivity(false);
        		bkg.stick(gGridH);
        		
        		if(i % 10 == 0)
        		{
        			gGridV.setColor(Color.LIGHT_GRAY);
        			gGridV.setBorderColor(Color.LIGHT_GRAY);
        			gGridV.setTranslucencyValue(NodeTrixViz.GRID_TRANSLUCENCY * .7f);
        			gGridH.setColor(Color.LIGHT_GRAY);
        			gGridH.setBorderColor(Color.LIGHT_GRAY);
        			gGridH.setTranslucencyValue(NodeTrixViz.GRID_TRANSLUCENCY * .7f);
                }
        	}   
        	
        	//SYMMETRY AXIS
        	VRectangle r = new VRectangle(bkg.vx + n.wdy, bkg.vy + n.ndx, 0, NodeTrixViz.CELL_SIZE/2, NodeTrixViz.CELL_SIZE/2, Color.LIGHT_GRAY, Color.LIGHT_GRAY, .4f);
        	r.setStrokeWidth(0);
        	vs.addGlyph(r);
        	r.setSensitivity(false);
        	this.bkg.stick(r);
        	i++;
        }
        
//        label_bkg = new VRectangle[2];
//        if (nodes.length > 1){
//            // west
//            label_bkg[0] = new VRectangle(bkg.vx-bkg.getWidth()-max_length/2-NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, bkg.vy, 0,
//                                          max_length/2+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, bkg.getHeight(),
//                                          NodeTrixViz.MATRIX_NODE_LABEL_BKG_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
//            // north
//            label_bkg[1] = new VRectangle(bkg.vx, bkg.vy+bkg.getHeight()+max_length/2+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER, 0,
//                                          bkg.getWidth(), max_length/2+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER,
//                                          NodeTrixViz.MATRIX_NODE_LABEL_BKG_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);            
//            for (VRectangle lb:label_bkg){
//                if (lb != null){
//                    vs.addGlyph(lb);
//                    vs.atBottom(lb);
//                    lb.setOwner(this);
//                    bkg.stick(lb);
//                }
//            }
//            bkg.stick(matrixLb);
//        }
//        else {
//            label_bkg[0] = bkg;
//            bkg.setWidth(max_length/2+NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER);
//        }
    }
    
    
    void createEdgeGraphics(VirtualSpace vs){
        long reflexiveProp4SingleNodeMatOffset = NodeTrixViz.CELL_SIZE / 2;
      	
        int i = 0;
        for (NTNode n : nodes)
        {
        	// FINISH RELATIONS
    		if (n.getOutgoingEdges() != null)
            {
            	HashMap<NTNode, NTIntraEdgeSet> intraEdgeSetMap = new HashMap<NTNode, NTIntraEdgeSet>();
            	
            	//Instantiate
            	for(NTEdge e : n.getOutgoingEdges()){
            		 if (e instanceof NTIntraEdge){
            			 intraEdges.add((NTIntraEdge) e);
            			 NTIntraEdgeSet ies = new NTIntraEdgeSet();
            			 n.addIntraEdgeSet(ies);
            			 intraEdgeSetMap.put(e.head, ies);
            		 }
            	}
            	
            	for (NTEdge e : n.getOutgoingEdges()){
                    // values that are 0 is because we do not care (not used)
                    if (e instanceof NTIntraEdge){
                    	if (e.tail == e.head && nodes.length == 1){
                            e.createGraphics(NodeTrixViz.CELL_SIZE/2, e.getTail().wdy-bkg.getHeight()-NodeTrixViz.CELL_SIZE/2,
                                              e.getHead().ndx-bkg.getWidth()+reflexiveProp4SingleNodeMatOffset, 0, vs);

                            // remember
                            reflexiveProp4SingleNodeMatOffset += NodeTrixViz.CELL_SIZE;
                    	}else {
                    		// add intraedge to edgeset
                        	intraEdgeSetMap.get(e.head).addIntraEdge((NTIntraEdge)e);
                        }
                    }else {
                        // instanceof NTExtraEdge
                        e.createGraphics(0, 0, 0, 0, vs);
                    }
                }
            	
            	// DRAW RELATIONS
            	for(NTNode nRel : intraEdgeSetMap.keySet())
            	{
            		intraEdgeSetMap.get(nRel).createGraphics(0, n.wdy, nRel.ndx, 0 , vs, this);
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
            if (node.intraEdgeSets != null){
                for (NTIntraEdgeSet edge : node.intraEdgeSets){
                    edge.move(x,y);
                }
            }
//            if (node.getIncomingEdges() != null){
//                for (NTEdge edge : node.getIncomingEdges()){
//                    if (edge instanceof NTExtraEdge){
//                        // do it only for extra edges because for intra edges
//                        // we have already moved them in the above loop
//                        // (intra edges connect nodes within the same matrix)
//                        edge.move(x,y);
//                    }
//                }
//            }
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
    
    public HashSet<NTIntraEdge> getIntraEdges()
    {
    	return this.intraEdges;
    }
}
