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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

public class Matrix {
    
    String name;
    Vector<NTNode> nodes = new Vector<NTNode>();
    Vector<NTIntraEdgeSet> intraEdgeSets = new Vector<NTIntraEdgeSet>();
	static int CELL_SIZE = 10;

    //GLYPHS
    VRectangle bkg;
    VText matrixLabel;
    VRectangle[] gridBarsH, gridBarsV, gridReflexiveSquares;
    
    long matrixLbDX = 0;
    long matrixLbDY = 0;
	
	private long labelWidth; // maximal length of label in pixel
	private boolean shiftNodesN = false;
	private boolean shiftNodesW = false;
    private boolean exploringModeGlobal = false;
	private Glyph gOverview;
	
	
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
    	    bkg.stick(matrixLabel);
    	    
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
    	
    	//Creating and disabling the overview glyph
    	gOverview = new VRectangle(0,0,0, NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH/2, NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH/2, Color.white );
    	vs.addGlyph(gOverview);
    	gOverview.setSensitivity(false);
    	gOverview.setTranslucencyValue(0);
    }
    
    void finishCreateNodeGraphics(VirtualSpace vs){
        //estimating maximal length of node labels
    	labelWidth = nodes.firstElement().getLabelWidth();
        for (NTNode n : this.nodes){
        	if (n.getLabelWidth() > labelWidth){
                labelWidth = n.getLabelWidth();
            }
        }
        labelWidth += (NodeTrixViz.MATRIX_NODE_LABEL_DIST_BORDER * 2);
        
        //creating background boxes for each node
        int i = 0;
        gridBarsH = new VRectangle[nodes.size()];
        gridBarsV = new VRectangle[nodes.size()];
        gridReflexiveSquares = new VRectangle[nodes.size()];
        
        for(NTNode n : this.nodes)
        {
        	n.setBackgroundBox(labelWidth);
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
		long offset = Math.min(this.labelWidth, NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH);
    	shiftNodesN = (bkg.vy + bkg.getHeight() > p[1] - offset);
		shiftNodesW = (bkg.vx - bkg.getHeight() < p[0] + offset);
		for (NTNode node : nodes){
        	if(shiftNodesN) {
        		node.shiftNorth((p[1] - offset) + labelWidth/2, true);
        	}
        	if(shiftNodesW) {
        		node.shiftWest((p[0] + offset) - labelWidth/2, true);
        	}
		}
		if(shiftNodesN || shiftNodesW){
			gOverview.moveTo(p[0] + NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH/2, p[1] - NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH/2);
			gOverview.setTranslucencyValue(1);
		}
	}
    
    public void disableExploringMode()
    {
    	for (NTNode node : nodes){
        	if(shiftNodesN) {
        		node.surfBackNorth(bkg.vy + bkg.getHeight() + labelWidth/2, true);
        	}
        	if(shiftNodesW) {
        		node.surfBackWest(bkg.vx - bkg.getHeight() - labelWidth/2, true);
            }
		}
    	gOverview.setTranslucencyValue(0);

    	shiftNodesN = false;
		shiftNodesW = false;
		exploringModeGlobal = false;
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
    	vs.onTop(gOverview);
    }
   
    
    public void highlightGrid(NTNode tail, NTNode head)
    {
    	Glyph g1 = gridBarsH[nodes.indexOf(tail)];
    	g1.setColor(Color.yellow);
    	g1.setVisible(true);
    	
    	Glyph g2 = gridBarsV[nodes.indexOf(head)];
    	g2.setColor(Color.yellow);
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
        long[] p = new long[2];
        long offset = 0;
        if(exploringModeGlobal){
        	p = VirtualSpaceManager.INSTANCE.getActiveView().getVisibleRegion(VirtualSpaceManager.INSTANCE.getActiveCamera());
        	offset = Math.min(this.labelWidth, NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH);
        	shiftNodesN = (bkg.vy + bkg.getHeight() > p[1] - offset);
    		shiftNodesW = (bkg.vx - bkg.getHeight() < p[0] + offset);
 
    		if(shiftNodesN || shiftNodesW){
    			gOverview.moveTo(p[0] + NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH/2, p[1] - NodeTrixViz.MATRIX_LABEL_OCCLUSION_WIDTH/2);
    			gOverview.setTranslucencyValue(1);
    		}else{
    			gOverview.setTranslucencyValue(0);
    		}
        }
		
    	for (NTNode node : nodes){
    		if(!(shiftNodesN || shiftNodesW))
    		{
    			node.move(x, y);
    		}else{
    			if(shiftNodesN){
    				node.shiftNorth((p[1] - offset) + labelWidth/2, false);
    				node.move(x, 0);
    			}else{
    				node.surfBackNorth(bkg.vy + bkg.getHeight() + labelWidth/2, false);
    			}
    			if(shiftNodesW){
    				node.shiftWest((p[0] + offset) - labelWidth/2, false);
    				node.move(0, y);
    	    	}else{
    				node.surfBackWest(bkg.vx - bkg.getHeight() - labelWidth/2, false);
    			}
    		}

        	
        	if (node.intraEdgeSets != null){
                for (NTIntraEdgeSet edge : node.intraEdgeSets){
                    edge.move(x, y);
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
            
        	if (node.getOutgoingEdges() != null){
                for (NTEdge edge : node.getOutgoingEdges()){
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
    
    
    public static void setCellSize(int cs){
        CELL_SIZE = cs;
    }
    public Vector<NTIntraEdgeSet>getNTIntraEdgeSets()
    {
    	return this.intraEdgeSets;
    }

    public boolean isExploringMode(){return exploringModeGlobal;}

    
}
