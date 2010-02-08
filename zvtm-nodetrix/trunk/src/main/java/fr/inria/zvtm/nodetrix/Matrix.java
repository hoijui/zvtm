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
    Vector<NTNode> nodes = new Vector<NTNode>();
    Vector<NTIntraEdgeSet> intraEdgeSets = new Vector<NTIntraEdgeSet>();
 
    //GLYPHS
    VRectangle bkg;
    VText matrixLabel;
    VRectangle[] gridBarsH, gridBarsV, gridReflexiveSquares;
    
    long matrixLbDX = 0;
    long matrixLbDY = 0;
    
    public Matrix(String name, Vector<NTNode> nodes){
        this.name = name;
        this.nodes = nodes;
        for (NTNode node : nodes){
            node.setMatrix(this);
        }
    }
    
    
    void createNodeGraphics(long x, long y, VirtualSpace vs){
	    // nodes
    	if (nodes.size() > 1){
            // matrix background
            bkg = new VRectangle(x, y, 0,
                                 nodes.size()*NodeTrixViz.CELL_SIZE/2, nodes.size()*NodeTrixViz.CELL_SIZE/2,
                                 NodeTrixViz.MATRIX_FILL_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
            vs.addGlyph(bkg);
            bkg.setOwner(this);
            // matrix label
    	    matrixLbDX = -Math.round(NodeTrixViz.CELL_SIZE/2*(1.1*nodes.size()));
    	    matrixLbDY = -Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.size()+.5+Math.sqrt(2*nodes.size())));
    	    matrixLabel = new VText(x+matrixLbDX, y+matrixLbDY, 0, NodeTrixViz.MATRIX_LABEL_COLOR, name, VText.TEXT_ANCHOR_END, (float)Math.sqrt(2*nodes.size()));
    	    vs.addGlyph(matrixLabel);
    	    matrixLabel.setOwner(this);
    	    
    	    // node labels
    	    Color c;
        	float b;
    	    int a = this.nodes.size();
        	float min = .7f;
        	float max = 1.0f;
        	float diff = max-min;
        	float step = (1/((float)a-1))*diff;
        	for (int i=0;i<nodes.size();i++)
    	    {
        		b = max - step*i;
        		c = Color.getHSBColor(0.1f, 0.8f, b);
        		nodes.get(i).createGraphics(-NodeTrixViz.CELL_SIZE/2*nodes.size(),
        	                            Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.size()-2*i-1)),
        	                            Math.round(NodeTrixViz.CELL_SIZE/2*(-nodes.size()+2*i+1)),
        	                            NodeTrixViz.CELL_SIZE/2*nodes.size(),
        	                            vs, false, c);
        	    nodes.get(i).moveTo(x, y);
            }	        
	    }
	    else {
	        // if matrix contains a single node, only show a horizontal label
	        nodes.firstElement().createGraphics(0, 0, 0, 0, vs, true, Color.getHSBColor(0.1f, 0.8f, 1.0f));
    	    nodes.firstElement().moveTo(x, y);
    	    bkg = new VRectangle(x, y, 0, NodeTrixViz.CELL_SIZE/2, 1, Color.white,  Color.white, 0f);
	    }
    }
    
    void finishCreateNodeGraphics(VirtualSpace vs){
        //estimating maximal length of node labels
    	long max_length = nodes.firstElement().getLabelWidth();
        for (NTNode n : this.nodes){
        	if (n.getLabelWidth() > max_length){
                max_length = n.getLabelWidth();
            }
        }
        max_length += NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER * 2;
        
        //creating background boxes for each node
        int i = 0;
        gridBarsH = new VRectangle[nodes.size()];
        gridBarsV = new VRectangle[nodes.size()];
        gridReflexiveSquares = new VRectangle[nodes.size()];
        
        for(NTNode n : this.nodes)
        {
        	n.setBackgroundBox(max_length);
        	if(this.nodes.size() == 1) break;
        	
        	//GRID PATTERN
        	VRectangle gGridV = new VRectangle(bkg.vx + n.ndx, bkg.vy,0, NodeTrixViz.CELL_SIZE/2, bkg.getWidth(), NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_TRANSLUCENCY);
        	gGridV.setDrawBorder(false);
        	gGridV.setSensitivity(false);
        	gGridV.setVisible(false);
        	vs.addGlyph(gGridV);
        	vs.above(gGridV, bkg);
        	bkg.stick(gGridV);
        	gridBarsV[i] = gGridV;
        	
        	VRectangle gGridH = new VRectangle(bkg.vx,bkg.vy+ n.wdy,0, bkg.getWidth(), NodeTrixViz.CELL_SIZE/2, NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_COLOR, NodeTrixViz.GRID_TRANSLUCENCY);
        	gGridH.setDrawBorder(false);
        	gGridH.setSensitivity(false);
           	gGridH.setVisible(false);
            vs.addGlyph(gGridH);
        	vs.above(gGridH, bkg);
        	bkg.stick(gGridH);
        	gridBarsH[i] = gGridH;
        	
        	if(i % 2 == 0)
        	{
        		gGridV.setColor(NodeTrixViz.GRID_COLOR);
        		gGridV.setVisible(true);
        		gGridH.setColor(NodeTrixViz.GRID_COLOR);
        		gGridH.setVisible(true);
        		
        	}else if(i % 10 == 0){
        		gGridV.setColor(Color.LIGHT_GRAY);
        		gGridV.setVisible(true);
        		gGridH.setColor(Color.LIGHT_GRAY);
        		gGridH.setVisible(true);
        	}   
        	
        	//SYMMETRY AXIS
        	VRectangle r = new VRectangle(bkg.vx + n.wdy, bkg.vy + n.ndx, 0, NodeTrixViz.CELL_SIZE/2, NodeTrixViz.CELL_SIZE/2, Color.LIGHT_GRAY, Color.LIGHT_GRAY, .4f);
        	r.setDrawBorder(false);
        	vs.addGlyph(r);
        	r.setSensitivity(false);
        	this.bkg.stick(r);
        	gridReflexiveSquares[i] = r;

        	i++;
        }
        
//        label_bkg = new VRectangle[2];
//        if (nodes.size() > 1){
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
            			 NTIntraEdgeSet ies = new NTIntraEdgeSet();
            			 intraEdgeSets.add(ies);
            			 n.addIntraEdgeSet(ies);
            			 intraEdgeSetMap.put(e.head, ies);
            		 }
            	}
            	
            	for (NTEdge e : n.getOutgoingEdges()){
                    // values that are 0 is because we do not care (not used)
                    if (e instanceof NTIntraEdge){
                    	if (e.tail == e.head && nodes.size() == 1){
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
    
    /** Brings all glyphs of this matrix to the top of the drawing stack*/
    public void bringToFront(VirtualSpace vs)
    {	
    	vs.onTop(bkg);
    	for(Glyph g : gridBarsH){ vs.onTop(g); }
    	for(Glyph g : gridBarsV){ vs.onTop(g); }
    	for(Glyph g : gridReflexiveSquares){ vs.onTop(g); }
    
    	for(NTIntraEdgeSet e : intraEdgeSets){
    		e.onTop(vs); //virtual space is not known in NTEdge
    	}
    	for(NTNode n : this.nodes){
    		n.onTop(); //Vitrtual space is already known in NTNode
    	}
    }
    
    public void highlightGrid(NTNode tail, NTNode head)
    {
    	Glyph g1 = gridBarsH[nodes.indexOf(tail)];
    	g1.setColor(Color.yellow);
//    	g1.setTranslucencyValue(0.2f);
    	g1.setVisible(true);
    	
    	Glyph g2 = gridBarsV[nodes.indexOf(head)];
    	g2.setColor(Color.yellow);
//    	g2.setTranslucencyValue(0.2f);
    	g2.setVisible(true);
    }
    
    public void resetGrid(NTNode tail, NTNode head)
    {
    	int i1 = nodes.indexOf(tail);
    	Glyph g1 = gridBarsH[i1];
    	g1.setColor(NodeTrixViz.GRID_COLOR);
    	if(i1 % 2 != 0) g1.setVisible(false);

    	int i2 = nodes.indexOf(head);
    	Glyph g2 = gridBarsV[i2];
    	g2.setColor(NodeTrixViz.GRID_COLOR);
    	if(i2 % 2 != 0) g2.setVisible(false);
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
        return nodes.size();
    }
    
    public String getName(){
        return name;
    }
    
    static int CELL_SIZE = 10;
    
    public static void setCellSize(int cs){
        CELL_SIZE = cs;
    }

}
