/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator2;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;

public class Matrix {
    
    String name;
    Vector<NTNode> nodes = new Vector<NTNode>();
//    Vector<NTIntraEdgeSet> intraEdgeSets = new Vector<NTIntraEdgeSet>();
	static int CELL_SIZE = 10;

    //GLYPHS
    VRectangle bkg;
    VText matrixLabel;
    VRectangle[] gridBarsH, gridBarsV, gridReflexiveSquares;
    Vector<Glyph> groupLabelsW = new Vector<Glyph>();
    Vector<Glyph> groupLabelsN = new Vector<Glyph>();
    
    long matrixLbDX = 0;
    long matrixLbDY = 0;
	
	private long labelWidth; // maximal length of label in pixel
	private boolean nodesUnvisibleN = false;
	private boolean nodesUnvisibleW = false;
    private boolean exploringModeGlobal = false;
	private Glyph gOverview;
	
	//TEMPORARY STUFF
	private Vector<NTNode> highlightedNodes = new Vector<NTNode>();
	private Vector<NTEdge> highlightedEdges = new Vector<NTEdge>();
	private AnimationManager am;
	private VirtualSpace vs;
	private boolean grouped = false;
	
	
	
	
	
    public Matrix(String name, Vector<NTNode> nodes){
//    	this.am = am;
        this.name = name;
        this.nodes = nodes;
        for (NTNode node : nodes){
            node.setMatrix(this);
        }
    }
    
    void createNodeGraphics(long x, long y, VirtualSpace vs){
    	// nodes
    	this.vs = vs;
    	
    	if (nodes.size() > 1){
    		
            // matrix background
            bkg = new VRectangle(x, y, 0,
                                 nodes.size()*NodeTrixViz.CELL_SIZE/2, nodes.size()*NodeTrixViz.CELL_SIZE/2,
                                 NodeTrixViz.MATRIX_FILL_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
            vs.addGlyph(bkg);
            
            // matrix label
    	    matrixLbDX = -Math.round(NodeTrixViz.CELL_SIZE/2*(1.1 * nodes.size()));
    	    matrixLbDY = -Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.size() + .5 + Math.sqrt(2*nodes.size())));
    	    matrixLabel = new VText(x+matrixLbDX, y+matrixLbDY, 0, NodeTrixViz.COLOR_MATRIX_NODE_LABEL_COLOR, name, VText.TEXT_ANCHOR_END, (float)Math.sqrt(4*nodes.size()));
    	    vs.addGlyph(matrixLabel);
//    	    matrixLabel.setOwner(this);
    	    matrixLabel.setSensitivity(false);
    	    bkg.stick(matrixLabel);
    	    // node labels
    	    Color c;
        	float b;
    	    int a = this.nodes.size();
        	float min = .7f;
        	float max = 1.0f;
        	float diff = max-min;
        	float step = (1/((float)a-1))*diff;
        	for (int i=0 ; i < nodes.size() ; i++ )
    	    {
        		b = max - step*i;
        		c = Color.getHSBColor(0.1f, 0.8f, b);
        		nodes.get(i).createGraphics(-NodeTrixViz.CELL_SIZE/2*nodes.size(),
        	                            Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.size()-2*i-1)),
        	                            Math.round(NodeTrixViz.CELL_SIZE/2*(-nodes.size()+2*i+1)),
        	                            NodeTrixViz.CELL_SIZE/2*nodes.size(),
        	                            vs, false, c);
        	    nodes.get(i).moveTo(x, y);
        	    nodes.get(i).getInfoBox().createGraphics(vs); // not yet on the right position. 
    	    }	        
	    }
	    else {
	        // if matrix contains a single node, only show a horizontal label
	        nodes.firstElement().createGraphics(0, 0, 0, 0, vs, true, Color.getHSBColor(0.1f, 0.8f, 1.0f));
    	    nodes.firstElement().moveTo(x, y);
//    	    nodes.firstElement().getInfoBox().createGraphics(vs); // not yet on the right position. 
    	    bkg = new VRectangle(x, y, 0, NodeTrixViz.CELL_SIZE/2, 1, Color.white,  Color.white, 0f);
	    }
    	bkg.setOwner(this);
    	//Creating and disabling the overview glyph
    	gOverview = new VRectangle(0,0,0, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2, Color.white );
    	vs.addGlyph(gOverview);
    	gOverview.setSensitivity(false);
    	gOverview.setTranslucencyValue(0);
    }
    
    /**Has to be called when node order has changed
     * 
     */
    public void repositionNodes()
    {
    	if(vs == null) return;
    	for(int i=0 ; i < nodes.size() ; i++ )
	    {
    		nodes.get(i).repositionLabels(Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.size()-2*i-1)),
                    				 Math.round(NodeTrixViz.CELL_SIZE/2*(-nodes.size()+2*i+1)));
    		nodes.get(i).repositionRelations();
 	    }
    	
//    	for(NTIntraEdgeSet ies : this.intraEdgeSets){
//    		ies.reposition();
//    	}
    }
    
    void finishCreateNodeGraphics(VirtualSpace vs){
        //estimating maximal length of node labels
    	labelWidth = 0;
        for (NTNode n : nodes){
        	if (n.getLabelWidth() > labelWidth){
                labelWidth = n.getLabelWidth();
            }
        }
        System.out.println("[MATRIX] MAX_LENGHT " + labelWidth);
        labelWidth += (NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER * 2);
        
        //creating grid for each node
        int i = 0;
        gridBarsH = new VRectangle[nodes.size()];
        gridBarsV = new VRectangle[nodes.size()];
        gridReflexiveSquares = new VRectangle[nodes.size()];
        
        for(NTNode n : nodes)
        {
//        	System.out.println("[MATRIX] FINISH NODE GRAPHICS " + name);
        	n.setBackgroundBox(labelWidth);
        	if(this.nodes.size() == 1) break;
        	
        	//GRID PATTERN
        	VRectangle gGridV = new VRectangle(bkg.vx + n.ndx, bkg.vy,0, NodeTrixViz.CELL_SIZE/2, bkg.getWidth(), NodeTrixViz.COLOR_GRID, NodeTrixViz.COLOR_GRID, NodeTrixViz.GRID_TRANSLUCENCY);
        	gGridV.setDrawBorder(false);
        	gGridV.setSensitivity(false);
        	gGridV.setVisible(false);
        	vs.addGlyph(gGridV);
        	vs.above(gGridV, bkg);
        	bkg.stick(gGridV);
        	gridBarsV[i] = gGridV;
        	
        	VRectangle gGridH = new VRectangle(bkg.vx,bkg.vy+ n.wdy,0, bkg.getWidth(), NodeTrixViz.CELL_SIZE/2, NodeTrixViz.COLOR_GRID, NodeTrixViz.COLOR_GRID, NodeTrixViz.GRID_TRANSLUCENCY);
        	gGridH.setDrawBorder(false);
        	gGridH.setSensitivity(false);
           	gGridH.setVisible(false);
            vs.addGlyph(gGridH);
        	vs.above(gGridH, bkg);
        	bkg.stick(gGridH);
        	gridBarsH[i] = gGridH;
        	
        	if(i % 2 == 0)
        	{
        		gGridV.setColor(NodeTrixViz.COLOR_GRID);
        		gGridV.setVisible(true);
        		gGridH.setColor(NodeTrixViz.COLOR_GRID);
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
//        	System.out.println("[MATRIX] FINISHED NODE GRAPHICS " + name);

        }
    }
    
    
    void createEdgeGraphics(VirtualSpace vs){
    	
    	 long reflexiveProp4SingleNodeMatOffset = NodeTrixViz.CELL_SIZE / 2;
       	
         for (NTNode n : nodes)
         {
         	// FINISH RELATIONS
     		if (n.getOutgoingEdges() != null)
             {
     			HashMap<NTNode, Vector<NTEdge>> intraEdgeSetMap = new HashMap<NTNode, Vector<NTEdge>>();
             	//Instantiate
             	for(NTEdge edge : n.getOutgoingEdges()){
//             		if(!edge.isVisible()) continue;
             		 if (edge.getState() == NodeTrixViz.APPEARANCE_INTRA_EDGE){
             			 Vector<NTEdge> ies = new Vector<NTEdge>();
//             			 intraEdgeSets.add(ies);
//             			 n.addIntraEdgeSet(ies);
             			 intraEdgeSetMap.put(edge.head, ies);
             		 }
             	}
             	
             	for (NTEdge e : n.getOutgoingEdges()){
                     // values that are 0 is because we do not care (not used)
             		if (e.getState() == NodeTrixViz.APPEARANCE_INTRA_EDGE){
                 		if (e.tail == e.head && nodes.size() == 1){
//                             e.createGraphics(NodeTrixViz.CELL_SIZE/2, e.getTail().wdy-bkg.getHeight()-NodeTrixViz.CELL_SIZE/2,
//                                               e.getHead().ndx-bkg.getWidth()+reflexiveProp4SingleNodeMatOffset, 0, vs);
                 			e.createGraphics(vs);

                             // remember
                             reflexiveProp4SingleNodeMatOffset += NodeTrixViz.CELL_SIZE;
                     	}else {
                     		// add intraedge to edgeset
                         	intraEdgeSetMap.get(e.head).add(e);
                         }
                     }else {
                         // instanceof NTExtraEdge
//                         e.createGraphics(0, 0, 0, 0, vs);
                     	e.createGraphics(vs);
                     }
                 }
             	
             	// DRAW RELATIONS
             	for(NTNode nRel : intraEdgeSetMap.keySet())
             	{
             		Vector<NTEdge> v = intraEdgeSetMap.get(nRel);
             		int size = v.size();
             		int i = 0;
             		for(NTEdge e : v){
             			e.setEdgeSetPosition(i++, size);
             			e.createGraphics(vs);
             		}
             	}
             }
     		
     		//draw Edge Sets

          }
//        long reflexiveProp4SingleNodeMatOffset = NodeTrixViz.CELL_SIZE / 2;
//      	
//        for (NTNode n : nodes)
//        {
//        	// FINISH RELATIONS
//    		if (n.getOutgoingEdges() != null)
//            {
//    			HashMap<NTNode, NTIntraEdgeSet> intraEdgeSetMap = new HashMap<NTNode, NTIntraEdgeSet>();
//            	//Instantiate
//            	for(NTEdge edge : n.getOutgoingEdges()){
//            		 if (edge.getState() == NodeTrixViz.APPEARANCE_INTRA_EDGE){
//            			 NTIntraEdgeSet ies = new NTIntraEdgeSet();
//            			 intraEdgeSets.add(ies);
//            			 n.addIntraEdgeSet(ies);
//            			 intraEdgeSetMap.put(edge.head, ies);
//            		 }
//            	}
//            	
//            	for (NTEdge e : n.getOutgoingEdges()){
//                    // values that are 0 is because we do not care (not used)
//            		if (e.getState() == NodeTrixViz.APPEARANCE_INTRA_EDGE){
//                		if (e.tail == e.head && nodes.size() == 1){
////                            e.createGraphics(NodeTrixViz.CELL_SIZE/2, e.getTail().wdy-bkg.getHeight()-NodeTrixViz.CELL_SIZE/2,
////                                              e.getHead().ndx-bkg.getWidth()+reflexiveProp4SingleNodeMatOffset, 0, vs);
//                			e.createGraphics(vs);
//
//                            // remember
//                            reflexiveProp4SingleNodeMatOffset += NodeTrixViz.CELL_SIZE;
//                    	}else {
//                    		// add intraedge to edgeset
//                        	intraEdgeSetMap.get(e.head).addEdge(e);
//                        }
//                    }else {
//                        // instanceof NTExtraEdge
////                        e.createGraphics(0, 0, 0, 0, vs);
//                    	e.createGraphics(vs);
//                    }
//                }
//            	
//            	// DRAW RELATIONS
//            	for(NTNode nRel : intraEdgeSetMap.keySet())
//            	{
//////            		intraEdgeSetMap.get(nRel).createGraphics(0, n.wdy, nRel.ndx, 0 , vs, this);
//            		intraEdgeSetMap.get(nRel).createGraphics(vs, this);
//            	}
//            }
//    		
//    		//draw Edge Sets
//
//         }
    }
    

    public void enableExploringMode(long[] p){
//    	if(selectedEdge != null)
//    	{
//	    	selectedEdge.setState(NodeTrixViz.IA_STATE_HIGHLIGHTED);
//	    	selectedEdge.perfomStateChange();
//	    	
//			NTNode head = selectedEdge.getHead();
//			NTNode tail = selectedEdge.getTail();
//			head.setState(NodeTrixViz.IA_STATE_HIGHLIGHTED, false, true);
//			tail.setState(NodeTrixViz.IA_STATE_HIGHLIGHTED, true, false);
//    	}
    	
		//SHIFT NODES TO SCREEN
//		long labelOcclusion = Math.min(maxLabelLength, NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH);
//		presentNodesN = new Vector<NTNode>();
//		presentNodesW = new Vector<NTNode>();
//		for(NTNode n : nodes){
//			if(n.gBackgroundN.vx >= p[0] + labelOcclusion){
//				presentNodesN.add(n);
//				n.setPermanentN(true);
//			}else if(n.gBackgroundN.vx > p[2])
//				break;
//		}
//		for(NTNode n : nodes){
//			if(n.gBackgroundW.vy <= p[1] - labelOcclusion){
//				presentNodesW.add(n);
//				n.setPermanentW(true);
//			}else if(n.gBackgroundW.vy < p[3])
//				break;
//		}
    	//performing movement
//    	for(NTNode n : presentNodesN){	n.performSurf();}
//    	for(NTNode n : presentNodesW){	n.performSurf();}
//    	
    	//SHIFT NODES TO SCREEN BORDERS
		exploringModeGlobal = true;
		long offset = Math.min(this.labelWidth, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH);
    	nodesUnvisibleN = (bkg.vy + bkg.getHeight() > p[1] - offset);
		nodesUnvisibleW = (bkg.vx - bkg.getHeight() < p[0] + offset);
		for (NTNode node : nodes){
        	if(nodesUnvisibleN) {
        		node.shiftNorthernLabels((p[1] - offset) + labelWidth/2, true);
        	}
        	if(nodesUnvisibleW) {
        		node.shiftWesternLabels((p[0] + offset) - labelWidth/2, true);
        	}
		}
		if(nodesUnvisibleN || nodesUnvisibleW){
			gOverview.moveTo(p[0] + NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2, p[1] - NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2);
			gOverview.setTranslucencyValue(1);
		}
	}
    
    public void disableExploringMode()
    {
    	for (NTNode node : nodes){
        	if(nodesUnvisibleN) {
//        		node.resetNorthernLabels(bkg.vy + bkg.getHeight() + labelWidth/2, true);
        		node.resetNorthernLabels(true);
        	}
        	if(nodesUnvisibleW) {
//        		node.resetWesternLabels(bkg.vx - bkg.getHeight() - labelWidth/2, true);
        		node.resetWesternLabels(true);
            }
		}
    	gOverview.setTranslucencyValue(0);

    	nodesUnvisibleN = false;
		nodesUnvisibleW = false;
		exploringModeGlobal = false;
    }
    
    /**
     * highlights all relations and related nodes of te given one.
     */
    public void highlightNodeContext(NTNode n)
    {
    	//set related nodes
//    	for(NTNode nn : nodes){
//    		nn.setNewState(NodeTrixViz.IA_STATE_DEFAULT, true, true);
//    		nn.perfomStateChange();
//    		for(NTEdge e : nn.outgoingEdges){
//    			if(!e.isVisible()) continue;
//    			e.setInteractionState(NodeTrixViz.IA_STATE_DEFAULT);
//    			e.performInteractionStateChange();
//    		}
//    		for(NTEdge e : nn.incomingEdges){
//    			if(!e.isVisible()) continue;
//    			e.setInteractionState(NodeTrixViz.IA_STATE_DEFAULT);
//    			e.performInteractionStateChange();
//    		}
//    	}
    	resetNodeContext();

    	n.setNewState(NodeTrixViz.IA_STATE_HIGHLIGHT, true, true);
    	highlightedNodes = new Vector<NTNode>();
    	highlightedEdges = new Vector<NTEdge>();
    	for(NTEdge e : n.getOutgoingEdges()){
    		if(!e.isVisible()) continue;
    		e.getHead().setNewState(NodeTrixViz.IA_STATE_RELATED, false, true);
    		e.setInteractionState(NodeTrixViz.IA_STATE_HIGHLIGHT_OUTGOING);
    		e.performInteractionStateChange();
    		highlightedNodes.add(e.getHead());
    		highlightedEdges.add(e);
    	}
    	
    	for(NTEdge e : n.getIncomingEdges()){
    		if(!e.isVisible()) continue;
    		e.getTail().setNewState(NodeTrixViz.IA_STATE_RELATED, true, false);
			e.setInteractionState(NodeTrixViz.IA_STATE_HIGHLIGHT_INCOMING);
			e.performInteractionStateChange();
    		highlightedNodes.add(e.getHead());
    		highlightedEdges.add(e);
    	}
    	    
    	for(NTNode nn : highlightedNodes){
    		highlightGrid(n, nn, NodeTrixViz.COLOR_MATRIX_NODE_RELATED_COLOR);
    		nn.perfomStateChange();
    	}
    
    	n.perfomStateChange();
    	highlightGrid(n, n, NodeTrixViz.COLOR_MATRIX_NODE_HIGHLIGHT_COLOR);
    }
    
    public void resetNodeContext()
    {
    	for(NTNode n : nodes){
    		resetGrid(n,n);
    		n.setNewState(NodeTrixViz.IA_STATE_DEFAULT, true, true);
    		n.perfomStateChange();
    		for(NTEdge e : n.outgoingEdges){
    			if(!e.isVisible()) continue;
    			resetGrid(n,e.head);
    			e.setInteractionState(NodeTrixViz.IA_STATE_DEFAULT);
    			e.performInteractionStateChange();
    		}
    		for(NTEdge e : n.incomingEdges){
    			if(!e.isVisible()) continue;
    			resetGrid(e.tail, n);
        		e.setInteractionState(NodeTrixViz.IA_STATE_DEFAULT);
    			e.performInteractionStateChange();
    		}
    	}
    }
    
    
    /** Brings all glyphs of this matrix to the top of the drawing stack*/
    public void onTop(VirtualSpace vs)
    {	
    	vs.onTop(bkg);
    	for(Glyph g : gridBarsH){ vs.onTop(g); }
    	for(Glyph g : gridBarsV){ vs.onTop(g); }
    	for(Glyph g : gridReflexiveSquares){ vs.onTop(g); }
    	
    	for(NTNode n : this.nodes){
    		n.onTop(); //Virtual space is already known in NTNode
    		for(NTEdge e : n.getOutgoingEdges()){ e.onTop(); }
    	}
    	
    	vs.onTop(gOverview);
    }
   
    
    public void highlightGrid(NTNode tail, NTNode head, Color c)
    {
    	int i1 = tail.matrix.nodes.indexOf(tail);
    	if(i1 > -1){
    		Glyph g1 = tail.matrix.gridBarsH[i1];
    		g1.setColor(c);
    		g1.setVisible(true);
    	}
 
    	int i2 = head.matrix.nodes.indexOf(head);
    	if(i2 > -1){
    		Glyph g2 = head.matrix.gridBarsV[i2];
    		g2.setColor(c);
    		g2.setVisible(true);
    	}
    	
    }
    
    
    public void resetGrid(NTNode tail, NTNode head)
    {
    	int i1 = tail.matrix.nodes.indexOf(tail);
    	if(i1 > -1){
    		Glyph g1 = tail.matrix.gridBarsH[i1];
    		g1.setColor(NodeTrixViz.COLOR_GRID);
    		if(i1 % 2 != 0) g1.setVisible(false);
    	}

    	int i2 = head.matrix.nodes.indexOf(head);
    	if(i2 > -1){
    		Glyph g2 = head.matrix.gridBarsV[i2];
    		g2.setColor(NodeTrixViz.COLOR_GRID);
    		if(i2 % 2 != 0) g2.setVisible(false);
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
//    	if(nodes.size() <= 1) {
////    		nodes.firstElement().moveTo(x, y);
//    		return;
//    	}

//    	if(animated){
//    		Animation a = am.getAnimationFactory().createGlyphTranslatei
//    	}
    	bkg.move(x, y);
        long[] p = new long[2];
        long offset = 0;
        p = VirtualSpaceManager.INSTANCE.getActiveView().getVisibleRegion(VirtualSpaceManager.INSTANCE.getActiveCamera());
        offset = Math.min(this.labelWidth, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH);
        nodesUnvisibleN = (bkg.vy + bkg.getHeight() > p[1] - offset);
        nodesUnvisibleW = (bkg.vx - bkg.getHeight() < p[0] + offset);
    	if(exploringModeGlobal && (nodesUnvisibleN || nodesUnvisibleW)){
    		gOverview.moveTo(p[0] + NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2, p[1] - NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2);
    		gOverview.setTranslucencyValue(1);
    	}else{
   			gOverview.setTranslucencyValue(0);
   		}
   	
    	for (NTNode node : nodes){
    		if(exploringModeGlobal)
    		{
    			if(nodesUnvisibleN){
    				node.shiftNorthernLabels((p[1] - offset) + labelWidth/2, false);
    				node.matrixMoved(x, 0);
    			}else{
//    				node.resetNorthernLabels(bkg.vy + bkg.getHeight() + labelWidth/2, false);
       				node.resetNorthernLabels(false);
       			}
    			if(nodesUnvisibleW){
    				node.shiftWesternLabels((p[0] + offset) - labelWidth/2, false);
    				node.matrixMoved(0, y);
    			}else{
//    				node.resetWesternLabels(bkg.vx - bkg.getHeight() - labelWidth/2, false);
    				node.resetWesternLabels(false);
    			}
    		}else{
    			node.matrixMoved(x, y);
    		}
        	
    		if (node.getOutgoingEdges() != null){
                for (NTEdge edge : node.getOutgoingEdges()){
//                    edge.move(x, y);
                	edge.updatePosition();
                }
            }
//        	if (node.getIntraEdgeSets() != null){
//                for (NTIntraEdgeSet edge : node.getIntraEdgeSets()){
//                    edge.move(x, y);
//                }
//            }
            
        	if (node.getIncomingEdges() != null){
                for (NTEdge edge : node.getIncomingEdges()){
//                    if (edge.getState() == NodeTrixViz.APPEARANCE_EXTRA_EDGE){
                        // do it only for extra edges because for intra edges
                        // we have already moved them in the above loop
                        // (intra edges connect nodes within the same matrix)
//                    	System.out.println("[MATRIX] move incomming edge HEAD " + edge.head.getMatrix().name);
//                    	System.out.println("[MATRIX] move incomming edge HEAD " + edge.tail.getMatrix().name);
//                    	edge.move(x,y);
                    	edge.updatePosition();
                    }
                }
            }
            
//        	if (node.getOutgoingEdges() != null){
//                for (NTEdge edge : node.getOutgoingEdges()){
//                    if (edge.getState() == NodeTrixViz.APPEARANCE_EXTRA_EDGE){
//                    	// do it only for extra edges because for intra edges
//                        // we have already moved them in the above loop
//                        // (intra edges connect nodes within the same matrix)
////                    	System.out.println("[MATRIX] move outgoing edge");
//                        edge.move(x,y);
//                    }
//                }
//            }
//        }
    }
    

    public void cleanGroupLabels(){
    	if(groupLabelsN.size() > 0){
    		for(Glyph g : groupLabelsN){
    			vs.removeGlyph(g);
    		}
    		groupLabelsN = new Vector<Glyph>();
    	}
    	if(groupLabelsW.size() > 0){
        	for(Glyph g : groupLabelsW){
        		vs.removeGlyph(g);
        	}
        	groupLabelsW = new Vector<Glyph>();
    	}
	}
	
	
    public void addChildrenToQueue(NTNode xn, Vector<NTNode> queue, Vector<NTNode> initialOrdering)
	{
		Vector<NTNode> orderedChildren = new Vector<NTNode>();
		NTNode xnRel;
		for(NTEdge xr : xn.getOutgoingEdges())
		{
            if (xr.getState() == NodeTrixViz.APPEARANCE_EXTRA_EDGE)
            {
				xnRel = xr.getHead();
				if(xn.equals(xnRel)) continue;
				if(!initialOrdering.contains(xnRel)) continue;
				orderedChildren.add(xnRel);
				initialOrdering.remove(xnRel);
			}
		}
		
		Collections.sort(orderedChildren, new NTNodeDegreeComparator());
		queue.addAll(orderedChildren);
	}
	
    
	public void addGroupLabel(Vector<NTNode> v, String label){
		if(vs == null) return;
		if(nodes.size() > 1){
			long dx = - this.bkg.getWidth() - labelWidth - NodeTrixViz.GROUP_LABEL_HALF_WIDTH;
			VRectangle groupLabelW = new VRectangle(bkg.vx + dx, bkg.vy + v.firstElement().wdy - (v.size()-1)*NodeTrixViz.CELL_SIZE_HALF, 0, NodeTrixViz.GROUP_LABEL_HALF_WIDTH, v.size()*NodeTrixViz.CELL_SIZE_HALF,Color.DARK_GRAY);
			VTextOr groupTextW = new VTextOr(bkg.vx + dx, bkg.vy + v.firstElement().wdy - (v.size()-1)*NodeTrixViz.CELL_SIZE_HALF, 0, Color.white, label, 0);
			groupTextW.setTextAnchor(VText.TEXT_ANCHOR_MIDDLE);
			vs.addGlyph(groupLabelW);
			vs.addGlyph(groupTextW);
			bkg.stick(groupLabelW);
			bkg.stick(groupTextW);
			this.groupLabelsW.add(groupLabelW);
			this.groupLabelsW.add(groupTextW);
				
			long dy = this.bkg.getWidth() + labelWidth + NodeTrixViz.GROUP_LABEL_HALF_WIDTH;
			VRectangle groupLabelN = new VRectangle(bkg.vx + v.firstElement().ndx + (v.size()-1)*NodeTrixViz.CELL_SIZE_HALF,bkg.vy + dy, 0, v.size()*NodeTrixViz.CELL_SIZE_HALF, NodeTrixViz.GROUP_LABEL_HALF_WIDTH,Color.DARK_GRAY);
			VTextOr groupTextN = new VTextOr(bkg.vx + v.firstElement().ndx + (v.size()-1)*NodeTrixViz.CELL_SIZE_HALF , bkg.vy + dy, 0, Color.white, label, (float)Math.PI/2);
			groupTextN.setTextAnchor(VText.TEXT_ANCHOR_MIDDLE);
			vs.addGlyph(groupLabelN);
			vs.addGlyph(groupTextN);
			bkg.stick(groupLabelN);
			bkg.stick(groupTextN);
			this.groupLabelsN.add(groupLabelN);
			this.groupLabelsN.add(groupTextN);
		}
	}

	public void setLabelsTo(){
		
	}
	
	
	//-----ORGANISING MATRIX------------------------ORGANISING MATRIX------------------------ORGANISING MATRIX-------------------
	
	public void reorderCutHillMcKee(){
		Vector<NTNode> queue = new Vector<NTNode>();
		Vector<NTNode> finalOrdering = new Vector<NTNode>();
		Vector<NTNode> initialOrdering  = nodes;
		if(initialOrdering.size() == 1) return;
		Collections.sort(initialOrdering, new NTNodeDegreeComparator());
		
		NTNode xnStart;
		while(!initialOrdering.isEmpty())
		{
			xnStart = initialOrdering.remove(0);
//			System.out.println("[NTV] " + xnStart.getDegree());
			finalOrdering.add(xnStart);
			initialOrdering.remove(xnStart);
			addChildrenToQueue(xnStart, queue, initialOrdering);

			while(!queue.isEmpty())
			{
				NTNode xn = queue.remove(0);
				finalOrdering.add(xn);
				initialOrdering.remove(xn);
				addChildrenToQueue(xn, queue, initialOrdering);
			}
		}
		nodes = finalOrdering;
	}
	
	public void group(int limitLevel){
		grouped = true;
		//removing old groupLabels
		if(vs == null) return;
		cleanGroupLabels();
		
		//grouping Nodes
		HashMap<String, Vector<NTNode>> groups = new HashMap<String, Vector<NTNode>>();
		for(NTNode n : nodes){
			String name = n.getGroupName();
			if(name == null) name = "Thing";
			Vector<NTNode> v = groups.get(name); 
			if(v == null){
				v = new Vector<NTNode>();
				groups.put(name, v);
			}
			v.add(n);
		}

//		if the current hierarchyLevel is 0, that dont show any labels and regroup the matrices 
////		using an algorithm
//		if(limitLevel == 0){
////			this.reorderCutHillMcKee();
//			this.
//			return;
//		} 
		
		//putting nodes back into matrix
		List<String> orderedGroups = new ArrayList<String>();
		orderedGroups.addAll(groups.keySet());
		Collections.sort(orderedGroups);
		nodes = new Vector<NTNode>();
		for(String s : orderedGroups){
			nodes.addAll(groups.get(s));
		}
		
		//repositioning nodes
		repositionNodes();
		
		//add new group labels
		for(Vector<NTNode> v : groups.values()){
			addGroupLabel(v, v.firstElement().getGroupName());
		}
	}
	
	public Vector<Matrix> splitMatrix(AnimationManager am)
	{
		if(nodes.size() <= 1 || !grouped) return new Vector<Matrix>();
		try{
		
		// 1. CREATE MATRICES
		Vector<Matrix> newMatrices = new Vector<Matrix>();
		String groupname = "";
		Matrix mNew = null;
		long x = 0, y = 0;
		for(NTNode n : nodes){
			if(!n.getGroupName().equals(groupname)){
				//finish old matrix
				if(mNew != null){
//					System.out.println("[MATRIX] NEW MATRIX " + mNew.name + " CONTAINING " + mNew.getSize() + " NODES");
					mNew.createNodeGraphics(bkg.vx + x/mNew.getSize(), bkg.vy + y/mNew.getSize(), vs);
					mNew.finishCreateNodeGraphics(vs);
					x = 0;y = 0;
				}
				//-- create new matrix
				groupname = n.getGroupName();
				mNew = new Matrix(groupname, new Vector<NTNode>());
				newMatrices.add(mNew);
			}
			mNew.addNode(n);
			x += n.ndx;
			y += n.wdy;
		}
		mNew.createNodeGraphics(bkg.vx + x/mNew.getSize(), bkg.vy + y/mNew.getSize(), vs);
		mNew.finishCreateNodeGraphics(vs);
		
		// 2. SHIFT LABELS TO OLD PLACES
		for(Matrix m : newMatrices){
			for(NTNode n : m.nodes){
				n.shiftNorthernLabels(bkg.vy + bkg.vw, false);
				n.shiftWesternLabels(bkg.vx - bkg.vw, false);
			}
			// 3. ADJUST EDGES
			m.adjustEdgeAppearance();
		}
		
		// 4. FADE OUT GRAPHICS OF OLD MATRIX
		cleanGraphics(am);
		
		// 6. RESET NEW NODE LABELS 
		for(Matrix m : newMatrices){
			for(NTNode n : m.nodes){
				n.resetNorthernLabels(true);
				n.resetWesternLabels(true);
			}
			m.performEdgeAppearanceChange();
		}
		
		// 6. DISPLACE MATRICES 
		for(Matrix m : newMatrices)
		{
			// 7. CREATE NEW EDGE GRAPHICS
			m.createEdgeGraphics(vs);
			m.onTop(vs);
		}
		return newMatrices;
		} catch(Exception e) {e.printStackTrace();}
		return null;
	}
	
	
	/** Sets a new appearance of the edges according to their context. The new appearance is 
	 * not yet set, just calculated.
	 */
	public void adjustEdgeAppearance(){
		for(NTNode nn : nodes){
			for(NTEdge e : nn.getOutgoingEdges()){
				e.adjustAppearanceState(); 
			}
//			for(NTEdge e : nn.getIncomingEdges()){
//				e.adjustAppearanceState(); 
//			}
		}
	}

	/** Sets the new appearance to the current appearance, but does not draw the edge.
	 * */
	public void performEdgeAppearanceChange()
	{
		for(NTNode nn : nodes){
			nn.cleanInternalRelations();
			for(NTEdge e : nn.getOutgoingEdges()){
				e.performAppearanceStateChange(); 
			}
//			for(NTEdge e : nn.getIncomingEdges()){
//				e.performAppearanceStateChange(); 
//			}
		}
	}
	
	
	
	//----------------------------GETTER-SETTER------------------------------------GETTER-SETTER------------------------------------GETTER-SETTER--------
	
	public LongPoint getPosition(){
		if(this.nodes.size() < 2)
			return new LongPoint( nodes.firstElement().mx, nodes.firstElement().my);
		return bkg.getLocation();
	}
	
	public int getSize(){
		return nodes.size();
	}
	
	public String getName(){
		return name;
	}
	
	
	public static void setCellSize(int cs){
		CELL_SIZE = cs;
	}
//	public Vector<NTIntraEdgeSet>getNTIntraEdgeSets()
//	{
//		return this.intraEdgeSets;
//	}
	
	public boolean isExploringMode(){return exploringModeGlobal;}
	
	public boolean isNodeVisibleNorth(){return !this.nodesUnvisibleN;}
	public boolean isNodesVisibleWest(){return !this.nodesUnvisibleW;}
	
	public void addNode(NTNode n){
		nodes.add(n);
		n.setMatrix(this);
	}
	
	
	public void setNodesOrdered(Vector<NTNode> finalOrdering) {
		nodes = finalOrdering;
	}
	
	public void cleanGraphics(AnimationManager am){
		if(nodes.size() < 2) return;
		
		cleanGroupLabels();
	
		
		Animation a;
		int duration = 3000;
		a = am.getAnimationFactory().createTranslucencyAnim(duration, bkg, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
				new EndAction(){
			public void execute(Object o, Animation.Dimension dimension){
				vs.removeGlyph((Glyph)o);}});
		am.startAnimation(a, true);
		a = am.getAnimationFactory().createTranslucencyAnim(duration, matrixLabel, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
				new EndAction(){public void execute(Object o, Animation.Dimension dimension){
					vs.removeGlyph((Glyph)o);}});
		am.startAnimation(a, true);
		
		for(Glyph g : this.gridBarsH){a = am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
				new EndAction(){public void execute(Object o, Animation.Dimension dimension){
					vs.removeGlyph((Glyph)o);}});
			am.startAnimation(a, true);}
		for(Glyph g : this.gridBarsV){a = am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
				new EndAction(){public void execute(Object o, Animation.Dimension dimension){
					vs.removeGlyph((Glyph)o);}});
			am.startAnimation(a, true);}
		for(Glyph g : this.groupLabelsN){a = am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
				new EndAction(){public void execute(Object o, Animation.Dimension dimension){
					vs.removeGlyph((Glyph)o);}});
			am.startAnimation(a, true);}
		for(Glyph g : this.groupLabelsW){a = am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
				new EndAction(){public void execute(Object o, Animation.Dimension dimension){
					vs.removeGlyph((Glyph)o);}});
			am.startAnimation(a, true);}
		for(Glyph g : this.gridReflexiveSquares){a = am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
				new EndAction(){public void execute(Object o, Animation.Dimension dimension){
					vs.removeGlyph((Glyph)o);}});
			am.startAnimation(a, true);}
	}
	
	
	public void applyRandomOffset(){
		long v = (long)(Math.random() * 100);
		move(v,v);
	}
	
	public long getLabelWidth(){
		return labelWidth;
	}
	public long getBackgroundWidth(){
		if(nodes.size() < 2)
			return NodeTrixViz.CELL_SIZE_HALF;
		return bkg.getWidth();
	}
	
}
