/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.awt.geom.Point2D;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.SwingUtilities;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator2;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTextOr;

public class Matrix {
    
    String name;
    Vector<NTNode> nodes = new Vector<NTNode>();
	static int CELL_SIZE = 10;

    //GLYPHS
    VRectangle bkg;
    private VText matrixLabel;
    VRectangle[] gridBarsH, gridBarsV;
    Vector<Glyph> groupLabelsW = new Vector<Glyph>();
    Vector<Glyph> groupLabelsN = new Vector<Glyph>();
    
    double matrixLabel_DX = 0;
    double matrixLabel_DY = 0;
	
	private double maxLabelWidth; // maximal length of label in pixel
	private boolean nodesUnvisibleN = false;
	private boolean nodesUnvisibleW = false;
    private boolean exploringModeGlobal = false;
	private Glyph gOverview;
	
//	private AnimationManager am;
	private VirtualSpace vs;
	private boolean grouped = false;
	
	
	
	
	/**
	 * This class represents the logical as well as visual matrix. There is a simple Vector
	 * within storing the containing nodes and their order.
	 * The nodes also know their matrix, as soon as they are told explizitly or as soon as
	 * they are added to a matrix.<br/>
	 * The matrix should generally be considered as a sub graph for extensibility reasons.
	 * So there should be no matrix-specific calls to the matrix and everything that is 
	 * matrix-specific happens within the matrix.<br/>
	 * <br/>
	 * The matrix is responsible for:<br/>
	 * - 
	 *  
	 * */
    public Matrix(String name, Vector<NTNode> nodes){
        this.name = name;
        for(NTNode n : nodes)
        {
        	this.addNode(n);
        	n.setMatrix(this);
        }
    }
    
    public void cleanNodeGraphics()
    {
    	//wrap by thread
    	//SwingUtilities.invokeLater(new Runnable()
    	//{
    	//	public void run()
    	//	{    	
	    		for (int i=0 ; i < nodes.size() ; i++ )
			    {	
		    		nodes.get(i).cleanGraphics();
			    }
    	//	}
    	//});
    	
    }
    
    /**
     * */
    void createGraphics(final double mx, final double my, final VirtualSpace vs)
    {
    	this.vs = vs;
		bkg = new VRectangle(mx, my, 0,
    			nodes.size()*NodeTrixViz.CELL_SIZE, nodes.size()*NodeTrixViz.CELL_SIZE,
    			ProjectColors.MATRIX_BACKGROUND[ProjectColors.COLOR_SCHEME], ProjectColors.MATRIX_BACKGROUND[ProjectColors.COLOR_SCHEME]);
    	gridBarsH = new VRectangle[nodes.size()];
        gridBarsV = new VRectangle[nodes.size()];
        bkg.setDrawBorder(false);
        bkg.setOwner(this);

        if(!(name.startsWith("[") && nodes.size() == 1))
        {
        	matrixLabel_DX = -Math.round(NodeTrixViz.CELL_SIZE/2*(1.1 * nodes.size()));
        	matrixLabel_DY = -Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.size() + .7 + Math.sqrt(2*nodes.size())));
        	matrixLabel = new VText(mx + matrixLabel_DX, my + matrixLabel_DY, 0, ProjectColors.NODE_TEXT[ProjectColors.COLOR_SCHEME], name, VText.TEXT_ANCHOR_END, (float)Math.sqrt(4*nodes.size()));
        	bkg.stick(matrixLabel);
        	vs.addGlyph(matrixLabel);
        	matrixLabel.setSensitivity(false);
        }
        
		if (nodes.size() > 1)
		{
    	    vs.addGlyph(bkg);
    	    		for (int i=0 ; i < nodes.size() ; i++ )
		    	    {	
    	    			double wdx = -NodeTrixViz.CELL_SIZE * nodes.size()/2;
    	    			double wdy = Math.round(NodeTrixViz.CELL_SIZE * (nodes.size()-2*i-1) /2);
    	    			double ndx = Math.round(NodeTrixViz.CELL_SIZE * (-nodes.size()+2*i+1) /2);
    	    			double ndy = NodeTrixViz.CELL_SIZE * nodes.size()/2;
    	    			
		    	    	nodes.get(i).createGraphics(wdx, wdy, ndx, ndy, vs, false, ProjectColors.NODE_BACKGROUND[ProjectColors.COLOR_SCHEME]);
		        		nodes.get(i).moveTo(mx, my);
		        		
		        		//GRID PATTERN
			        	VRectangle gGridV = new VRectangle(bkg.vx + ndx, bkg.vy, 0, NodeTrixViz.CELL_SIZE, bkg.getWidth(), ProjectColors.MATRIX_GRID[ProjectColors.COLOR_SCHEME], ProjectColors.MATRIX_GRID[ProjectColors.COLOR_SCHEME], ProjectColors.MATRIX_GRID_TRANSLUCENCY);
			        	gGridV.setDrawBorder(false);
			        	gGridV.setOwner(this);
			        	vs.above(gGridV, bkg);
			        	bkg.stick(gGridV);
			        	gridBarsV[i] = gGridV;
			        	
			        	final VRectangle gGridH = new VRectangle(bkg.vx, bkg.vy + wdy, 0, bkg.getWidth(), NodeTrixViz.CELL_SIZE, ProjectColors.MATRIX_GRID[ProjectColors.COLOR_SCHEME], ProjectColors.MATRIX_GRID[ProjectColors.COLOR_SCHEME], ProjectColors.MATRIX_GRID_TRANSLUCENCY);
			        	gGridH.setDrawBorder(false);
			        	gGridH.setSensitivity(false);
			        	gGridH.setOwner(this);
			        	vs.above(gGridH, bkg);
			        	bkg.stick(gGridH);
			        	gridBarsH[i] = gGridH;
			        	
			        	if(i % 2 == 0)
			        	{
			        		gGridV.setVisible(false);
			        		gGridH.setVisible(false);
			        	}
			        	
			        	vs.addGlyph(gGridV);
					    vs.addGlyph(gGridH);
					}	
    	    		
	    }
	    else {
	    	nodes.firstElement().createGraphics(0, 0, 0, 0, vs, true, ProjectColors.NODE_BACKGROUND[ProjectColors.COLOR_SCHEME]);
	    	nodes.firstElement().moveTo(mx, my);
	    }
		
    	bkg.setOwner(this);
    	//Creating and disabling the overview glyph
    	gOverview = new VRectangle(0,0,0, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH/2, Color.white );
    	gOverview.setSensitivity(false);
    	gOverview.setTranslucencyValue(0);
    	
    			vs.addGlyph(gOverview);
    }
    
    /**Has to be called when node order has changed
     */
    public void updateNodePositions()
    {
    	if(vs == null) return;
    	
    	for(int i=0 ; i < nodes.size() ; i++ )
	    {
    		nodes.get(i).updateLabelPosition(	Math.round(NodeTrixViz.CELL_SIZE/2*(nodes.size()-2*i-1)),
    											Math.round(NodeTrixViz.CELL_SIZE/2*(-nodes.size()+2*i+1))	);
    		nodes.get(i).updataRelationPositions();
 	    }
    }
    
    void finishCreateNodeGraphics(final VirtualSpace vs){
        //estimating maximal length of node labels
    	maxLabelWidth = 0;
        for (NTNode n : nodes){
        	if (n.getTextWidth() > maxLabelWidth){
                maxLabelWidth = n.getTextWidth();
            }
        }
        maxLabelWidth += (NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER * 2);
        
        for (NTNode n : nodes)
        {
        	n.setLabelWidth(maxLabelWidth);
        }
  }
    
    
    void createEdgeGraphics(VirtualSpace vs){
    	
    	 double reflexiveProp4SingleNodeMatOffset = NodeTrixViz.CELL_SIZE / 2;
       	
         for (NTNode n : nodes)
         {
         	// FINISH RELATIONS
     		if (n.getOutgoingEdges() != null)
             {
     			HashMap<NTNode, Vector<NTEdge>> intraEdgeSetMap = new HashMap<NTNode, Vector<NTEdge>>();
             	//Instantiate
             	for(NTEdge edge : n.getOutgoingEdges()){
             		 if (edge.getState() == NodeTrixViz.APPEARANCE_INTRA_EDGE){
             			 Vector<NTEdge> ies = new Vector<NTEdge>();
             			 intraEdgeSetMap.put(edge.head, ies);
             		 }
             	}
             	
             	for (NTEdge e : n.getOutgoingEdges()){
                     // values that are 0 is because we do not care (not used)
             		if (e.getState() == NodeTrixViz.APPEARANCE_INTRA_EDGE)
             		{
                 		if (e.tail == e.head && nodes.size() == 1){
                 			e.createGraphics(vs);
                            reflexiveProp4SingleNodeMatOffset += NodeTrixViz.CELL_SIZE;
                     	}else {
                     		// add intraedge to edgeset
                         	intraEdgeSetMap.get(e.head).add(e);
                         }
                     }else {
                         // instanceof NTExtraEdge
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
         }
    }
    

    public void enableExploringMode(double[] p){
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
		double offset = Math.min(this.maxLabelWidth, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH);
    	nodesUnvisibleN = (bkg.vy + bkg.getHeight() > p[1] - offset);
		nodesUnvisibleW = (bkg.vx - bkg.getHeight() < p[0] + offset);
		for (NTNode node : nodes){
        	if(nodesUnvisibleN) {
        		node.shiftNorthernLabels((p[1] - offset) + maxLabelWidth/2, true);
        	}
        	if(nodesUnvisibleW) {
        		node.shiftWesternLabels((p[0] + offset) - maxLabelWidth/2, true);
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
     * highlights all relations and related nodes of the given one.
     */
//    public void highlightNodeContext(NTNode n, boolean relations)
//    {
//    	resetNodeContext();
//
//    	n.setNewState(NodeTrixViz.IA_STATE_HIGHLIGHT, true, true);
//    	
//    	if(relations){
//    		//hi outgoing relations and nodes.
//    		highlightedNodes = new Vector<NTNode>();
//    		highlightedEdges = new Vector<NTEdge>();
//    		for(NTEdge e : n.getOutgoingEdges()){
//    			if(!e.isVisible()) continue;
//    			e.getHead().setNewState(NodeTrixViz.IA_STATE_RELATED, false, true);
//    			e.setInteractionState(NodeTrixViz.IA_STATE_HIGHLIGHT_OUTGOING);
//    			e.performInteractionStateChange();
//    			highlightedNodes.add(e.getHead());
//    			highlightedEdges.add(e);
//    		}
//    		//highlight incomming relations and nodes.
//    		for(NTEdge e : n.getIncomingEdges()){
//    			if(!e.isVisible()) continue;
//    			e.getTail().setNewState(NodeTrixViz.IA_STATE_RELATED, true, false);
//    			e.setInteractionState(NodeTrixViz.IA_STATE_HIGHLIGHT_INCOMING);
//    			e.performInteractionStateChange();
//    			highlightedNodes.add(e.getHead());
//    			highlightedEdges.add(e);
//    		}
//    		
//    		for(NTNode nn : highlightedNodes){
//    			highlightGrid(n, nn, NodeTrixViz.COLOR_MATRIX_NODE_RELATED_COLOR);
//    			nn.perfomStateChange();
//    		}
//
//    		highlightGrid(n, n, NodeTrixViz.COLOR_MATRIX_NODE_HIGHLIGHT_COLOR);
//    	}
//    	n.perfomStateChange();
//    }
    
//    public void resetNodeContext()
//    {
//    	for(NTNode n : nodes){
//    		resetGrid(n,n);
//    		n.setNewInteractionState(NodeTrixViz.IA_STATE_DEFAULT, true, true);
//    		n.perfomStateChange();
//    		for(NTEdge e : n.outgoingEdges){
//    			if(!e.isVisible()) continue;
//    			resetGrid(n,e.head);
//    			e.setNewInteractionState(NodeTrixViz.IA_STATE_DEFAULT);
//    			e.performInteractionStateChange();
//    		}
//    		for(NTEdge e : n.incomingEdges){
//    			if(!e.isVisible()) continue;
//    			resetGrid(e.tail, n);
//        		e.setNewInteractionState(NodeTrixViz.IA_STATE_DEFAULT);
//    			e.performInteractionStateChange();
//    		}
//    	}
//    }
    
    
    /** Brings all glyphs of this matrix to the top of the drawing stack*/
    public void onTop(final VirtualSpace vs)
    {	
    	SwingUtilities.invokeLater(new Runnable()
    	{
    		public void run()
    		{
    			vs.onTop(bkg);
    			
    			for(Glyph g : groupLabelsW){
    				if (g!=null){vs.onTop(g); }
    			}
    			for(Glyph g : groupLabelsN){
    				if (g!=null){vs.onTop(g); }
    			}
    			for(Glyph g : gridBarsH){
    				if (g!=null){vs.onTop(g); }
    			}
    			for(Glyph g : gridBarsV){
    				if (g!=null){vs.onTop(g); }
    			}
    			for(NTNode n : nodes){
    				n.onTop(); //Virtual space is already known in NTNode
    				for(NTEdge e : n.getOutgoingEdges())
    				{
    					e.onTop(); 
    				}
    			}
    			vs.onTop(gOverview);
    		}
    	});

    }
   
    public void highlightGrid(NTNode tail, NTNode head, Color c)
    {
    	if(nodes.size() == 1) return;
    	if(nodes.contains(tail))
    	{
    		int i1 = nodes.indexOf(tail);
    		Glyph g1 = gridBarsH[i1];
    		g1.setColor(c);
    		g1.setVisible(true);
    	}
    	if(nodes.contains(head))
    	{
    		int i2 = nodes.indexOf(head);
    		Glyph g2 = gridBarsV[i2];
    		g2.setColor(c);
    		g2.setVisible(true);
    	}
    }
    
    
    public void resetGrid(NTNode tail, NTNode head)
    {
    	if(nodes.contains(tail))
    	{
    		int i1 = nodes.indexOf(tail);
    		Glyph g1 = tail.matrix.gridBarsH[i1];
    		g1.setColor(ProjectColors.MATRIX_GRID[ProjectColors.COLOR_SCHEME]);
    		if(i1 % 2 == 0) g1.setVisible(false);
    	}
    	if(nodes.contains(head))
    	{
    		int i2 = nodes.indexOf(head);
    		Glyph g2 = tail.matrix.gridBarsV[i2];
    		g2.setColor(ProjectColors.MATRIX_GRID[ProjectColors.COLOR_SCHEME]);
    		if(i2 % 2 == 0) g2.setVisible(false);
    	}
    }
    
    public void resetGrid()
    {
    	int i = 0;
    	if(nodes.size() == 1) return;
    	
    	for(Glyph g : gridBarsH)
    	{
    		g.setColor(ProjectColors.MATRIX_GRID[ProjectColors.COLOR_SCHEME]);
    		if(i % 2 == 0) g.setVisible(false);
    		i++;
    	}
    	i = 0;
    	for(Glyph g : gridBarsV)
    	{
    		g.setColor(ProjectColors.MATRIX_GRID[ProjectColors.COLOR_SCHEME]);
    		if(i % 2 == 0) g.setVisible(false);
    		i++;
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
    
    /**
     * Moves the matrix relatively
     * 
     * @param x
     * @param y
     */
    public void move(double x, double y){

    	bkg.move(x, y);
        double[] p = new double[2];
        double offset = 0;
        p = VirtualSpaceManager.INSTANCE.getActiveView().getVisibleRegion(VirtualSpaceManager.INSTANCE.getActiveCamera());
        offset = Math.min(this.maxLabelWidth, NodeTrixViz.MATRIX_NODE_LABEL_OCCLUSION_WIDTH);
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
    				node.shiftNorthernLabels((p[1] - offset) + maxLabelWidth/2, false);
    				node.matrixMoved(x, 0);
    			}else{
       				node.resetNorthernLabels(false);
       			}
    			if(nodesUnvisibleW){
    				node.shiftWesternLabels((p[0] + offset) - maxLabelWidth/2, false);
    				node.matrixMoved(0, y);
    			}else{
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
            if (xr.getHead().getMatrix().equals(this))
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
			double dx = - this.bkg.getWidth()/2 - maxLabelWidth - NodeTrixViz.GROUP_LABEL_HALF_WIDTH;
			VRectangle groupLabelW = new VRectangle(bkg.vx + dx, bkg.vy + v.firstElement().wdy - (v.size()-1)*NodeTrixViz.CELL_SIZE_HALF, 0, NodeTrixViz.GROUP_LABEL_HALF_WIDTH*2, v.size()*NodeTrixViz.CELL_SIZE_HALF*2,ProjectColors.MATRIX_GROUP_LABEL_BACKGROUND[ProjectColors.COLOR_SCHEME]);
			VTextOr groupTextW = new VTextOr(bkg.vx + dx, bkg.vy + v.firstElement().wdy - (v.size()-1)*NodeTrixViz.CELL_SIZE_HALF, 0, ProjectColors.MATRIX_GROUP_LABEL_TEXT[ProjectColors.COLOR_SCHEME], label, 0);
			groupTextW.setTextAnchor(VText.TEXT_ANCHOR_MIDDLE);
			vs.addGlyph(groupLabelW);
			vs.addGlyph(groupTextW);
			bkg.stick(groupLabelW);
			bkg.stick(groupTextW);
			this.groupLabelsW.add(groupLabelW);
			this.groupLabelsW.add(groupTextW);
				
			double dy = this.bkg.getHeight()/2 + maxLabelWidth + NodeTrixViz.GROUP_LABEL_HALF_WIDTH;
			VRectangle groupLabelN = new VRectangle(bkg.vx + v.firstElement().ndx + (v.size()-1)*NodeTrixViz.CELL_SIZE_HALF, bkg.vy + dy, 0, v.size()*NodeTrixViz.CELL_SIZE, NodeTrixViz.GROUP_LABEL_HALF_WIDTH*2, ProjectColors.MATRIX_GROUP_LABEL_BACKGROUND[ProjectColors.COLOR_SCHEME]);
			VTextOr groupTextN = new VTextOr(bkg.vx + v.firstElement().ndx + (v.size()-1)*NodeTrixViz.CELL_SIZE_HALF , bkg.vy + dy, 0, ProjectColors.MATRIX_GROUP_LABEL_TEXT[ProjectColors.COLOR_SCHEME], label, (float)Math.PI/2);
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
		
		NTNode nodeStart;
		while(!initialOrdering.isEmpty())
		{
			nodeStart = initialOrdering.remove(0);
			finalOrdering.add(nodeStart);
			initialOrdering.remove(nodeStart);
			addChildrenToQueue(nodeStart, queue, initialOrdering);
				
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
	
	
	/**Reorganises the nodes in this matrix according their group description.
	 * */
	public void group(){
		grouped = true;
		
		//removing old groupLabels
		if(vs == null) return;
		
		//grouping Nodes
		HashMap<String, Vector<NTNode>> groups = new HashMap<String, Vector<NTNode>>();
		for(NTNode n : nodes){
			String name = n.getGroupName();
			if(name == null) name = "...";
			Vector<NTNode> v = groups.get(name); 
			if(v == null){
				v = new Vector<NTNode>();
				groups.put(name, v);
			}
			v.add(n);
		}

		//putting nodes back into matrix
		List<String> orderedGroups = new ArrayList<String>();
		orderedGroups.addAll(groups.keySet());
		Collections.sort(orderedGroups);
		nodes = new Vector<NTNode>();
		for(String s : orderedGroups){
			nodes.addAll(groups.get(s));
		}
		
		
		//repositioning nodes
		updateNodePositions();
		
		//add new group labels
		for(Vector<NTNode> v : groups.values()){
			addGroupLabel(v, v.firstElement().getGroupName());
		}

	}
	
	public Vector<Matrix> splitMatrix(AnimationManager am)
	{
		if(nodes.size() <= 1 || !grouped) return new Vector<Matrix>();
		try{
			Vector<Matrix> newMatrices = new Vector<Matrix>();
			String groupname = "";
			Matrix mNew = null;
			double x = this.getPosition().x - bkg.vw /2;
			double y = this.getPosition().y + bkg.vh /2;
			double mNew_width = 0;
			for(NTNode n : nodes){
				n.cleanGraphics();
				if(!n.getGroupName().equals(groupname)){
					if(mNew != null){
						//finish old matrix
						mNew_width = mNew.getNodeAmount() * CELL_SIZE;
						mNew.reorderCutHillMcKee();
						mNew.createGraphics(x + mNew_width, y - mNew_width, vs);
						mNew.finishCreateNodeGraphics(vs);
						x += mNew_width*2;
						y -= mNew_width*2;
					}
					//-- create new matrix
					groupname = n.getGroupName();
					mNew = new Matrix(groupname, new Vector<NTNode>());
					newMatrices.add(mNew);
				}
				mNew.addNode(n);
				n.setMatrix(mNew);
			}
			mNew.reorderCutHillMcKee();
			mNew.createGraphics(x + mNew.getNodeAmount() * CELL_SIZE, y - mNew.getNodeAmount() * CELL_SIZE , vs);
			mNew.finishCreateNodeGraphics(vs);
			
			cleanGraphics(am);
			
			for(Matrix mNew1 : newMatrices){
				mNew1.adjustEdgeAppearance();
				mNew1.performEdgeAppearanceChange();
			}
			for(Matrix mNew1 : newMatrices)
			{
				mNew1.createEdgeGraphics(vs);
				mNew1.onTop(vs);
			}
	
			return newMatrices;
	
		} catch(Exception e) 
		{
			e.printStackTrace();
		}
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
		}
	}
	
	
	
	//----------------------------GETTER-SETTER------------------------------------GETTER-SETTER------------------------------------GETTER-SETTER--------
	
	public Point2D.Double getPosition(){
		if(this.nodes.size() < 2)
			return new Point2D.Double(nodes.firstElement().matrixX, nodes.firstElement().matrixY);
		return bkg.getLocation();
	}
	
	public int getNodeAmount(){
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
	}
	
	
	public void setNodesOrdered(Vector<NTNode> finalOrdering) {
		nodes = finalOrdering;
	}
	
	/**
	 * Removes all matrix relevant glyphs that do not belong to nodes nor relations.
	 * */
	public void cleanGraphics(final AnimationManager am){
		
    	SwingUtilities.invokeLater(new Runnable()
    	{
    		public void run()
    		{
    			final Animation a1, a2;
    			final int duration = 3000;
				if(vs.getAllGlyphs().contains(matrixLabel))
				{
					a1 = am.getAnimationFactory().createTranslucencyAnim(duration, matrixLabel, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
							new EndAction(){public void execute(Object o, Animation.Dimension dimension){
								vs.removeGlyph((Glyph)o);}});
					am.startAnimation(a1, true);
				}
				
				if(nodes.size() < 2) return;
				
				cleanGroupLabels();
		
				a2 = am.getAnimationFactory().createTranslucencyAnim(duration, bkg, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
						new EndAction(){
					public void execute(Object o, Animation.Dimension dimension){
						vs.removeGlyph((Glyph)o);}});
				am.startAnimation(a2, true);
					
				
				SwingUtilities.invokeLater(new Runnable()
				{
					public void run()
					{
						for(Glyph g : gridBarsH)
						{
							am.startAnimation(am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
									new EndAction(){public void execute(Object o, Animation.Dimension dimension){
										vs.removeGlyph((Glyph)o);}}), true);
						}
						for(Glyph g : gridBarsV){
							am.startAnimation(am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
									new EndAction(){public void execute(Object o, Animation.Dimension dimension){
										vs.removeGlyph((Glyph)o);}}), true);
						}
						for(Glyph g : groupLabelsN){
							am.startAnimation(am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
									new EndAction(){public void execute(Object o, Animation.Dimension dimension){
										vs.removeGlyph((Glyph)o);}}), true);
						}
						for(Glyph g : groupLabelsW){
							am.startAnimation(am.getAnimationFactory().createTranslucencyAnim(duration, g, 0, false, SlowInSlowOutInterpolator2.getInstance(), 
									new EndAction(){public void execute(Object o, Animation.Dimension dimension){
										vs.removeGlyph((Glyph)o);}}), true);
						}
					}
				});
    		}
		});
	}
	
	
	public void applyRandomOffset(){
		double v = (double)(Math.random() * 100);
		move(v,v);
	}
	
	public double getMaxLabelWidth(){
		return maxLabelWidth;
	}
	public double getBackgroundWidth(){
		if(nodes.size() < 2)
			return NodeTrixViz.CELL_SIZE_HALF;
		return bkg.getWidth();
	}
	public Vector<NTNode> getNodes(){
		return this.nodes;
	}
	
}
