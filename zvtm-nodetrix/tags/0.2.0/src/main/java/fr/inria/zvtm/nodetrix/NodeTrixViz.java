/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.nodetrix.lll.Edge;
import fr.inria.zvtm.nodetrix.lll.LinLogEdge;
import fr.inria.zvtm.nodetrix.lll.LinLogNode;
import fr.inria.zvtm.nodetrix.lll.LinLogOptimizerModularity;
import fr.inria.zvtm.nodetrix.lll.MinimizerBarnesHut;
import fr.inria.zvtm.nodetrix.lll.Node;
import fr.inria.zvtm.nodetrix.MatrixSizeComparator;

public class NodeTrixViz {
    
    public static final Color GRID_COLOR = Color.getHSBColor(1f, 0.0f, 0.9f);
	public static final float GRID_TRANSLUCENCY = .5f;
	public static final float INTRA_TRANSLUCENCY = .7f;
	public static final float INTRA_TRANSLUCENCY_DIMMFACTOR = .5f;
    public static final long CELL_SIZE = 20;
    public static final long CELL_SIZE_HALF = CELL_SIZE/2;
    public static final int LINLOG_ITERATIONS = 20;
    public static final Color MATRIX_FILL_COLOR = Color.WHITE;
    public static final Color MATRIX_STROKE_COLOR = Color.BLACK;
    public static final Color INTRA_LINK_COLOR = new Color(160,202,254);
    public static final Color EXTRA_LINK_COLOR = new Color(118,98,252);

    public static final int MATRIX_NODE_LABEL_DIST_BORDER = 3;
    public static final int MATRIX_NODE_LABEL_OCCLUSION_WIDTH = 150; //half of the width/lenght in pixel that can be occupied by labels when enabling local exploration
    public static final Color MATRIX_NODE_LABEL_COLOR = Color.DARK_GRAY;
    public static final Color MATRIX_NODE_BKG_COLOR = new Color(250,205,155);
    public static final float MATRIX_NODE_BKG_TRANSLUCENCY = .8f;
    public static final Color MATRIX_NODE_HIGHLIGHT_COLOR = Color.yellow;
    public static final Color MATRIX_NODE_RELATED_COLOR = Color.orange;
     
    
    //ANIMATION DURATIOS in msec
    public static final int DURATION_GENERAL = 300;
	public static final int DURATION_NODEMOVE = 300;    

	//INTERACTION STATES
	public static final int IA_STATE_DEFAULT = 0;
	public static final int IA_STATE_HIGHLIGHTED = 1;
	public static final int IA_STATE_SELECTED = 2;
	public static final int IA_STATE_FADE = 3;
	public static final int IA_STATE_RELATED = 4;

    /* Links between matrices */
    static Color INTER_LINK_COLOR = Color.BLACK;
	
    public final static Color EXTRA_EDGE_HIGHLIGHT_COLOR = Color.orange;
    public final static Color EXTRA_EDGE_RELATED_COLOR = Color.yellow;
	public static final float EXTRA_ALPHA_MAX_LENGHT = 1500;  
	public static final float EXTRA_ALPHA_MIN_LENGHT = 100;  
	public static final float EXTRA_ALPHA_MIN = .25f;
	
	public static final double LINLOG_QUALITY = 10;
	
	public static final int APPEARANCE_EXTRA_EDGE = 0;  
	public static final int APPEARANCE_INTRA_EDGE = 1;  
    /* Matrices in this visualization */
//    Matrix[] matrices;
	Vector<Matrix> matrices;
	/**Vector that stores all edges considered by the linLog cluster algorithm*
     * @author benjamin bach bbach@lri.fr
     */
    
    public NodeTrixViz(){
//        matrices = new Matrix[0];
    	matrices = new Vector<Matrix>();
    }
    
    //-------------BUILDING COMPONENTS----------BUILDING COMPONENTS----------BUILDING COMPONENTS----------BUILDING COMPONENTS----------
    
    public Matrix addMatrix(String name, Vector<NTNode> nodes){
//        Matrix res = new Matrix(name, nodes.toArray(new NTNode[nodes.size()]));
        Matrix res = new Matrix(name, nodes);
//    	Matrix[] na = new Matrix[matrices.length+1];
//        System.arraycopy(matrices, 0, na, 0, matrices.length);
//        na[matrices.length] = res;
//        matrices = na;
        matrices.add(res);
        return res;
    }
    
    public Matrix addMatrix(Matrix res){
      matrices.add(res);
      return res;
    }
	
	/**This method causes the NTNodes to be clustered using the LinLog Algorithm. 
	 * It returns a HashMap mapping each NTNode to an integer depicting its cluster.
	 * Use this method as an alternative to <code>addMatrix(String name, Vector<NTNode> nodes)</code>
	 **/
	public void createMatricesByClustering(Collection<NTNode> nodes, List<LinLogEdge> edges)
	{ 
		LinLogOptimizerModularity llalgo = new LinLogOptimizerModularity();
		ArrayList<LinLogNode> llNodes = new ArrayList<LinLogNode>();
		for(NTNode nn : nodes){llNodes.add(nn);}
		Map<LinLogNode, Integer> resultMap = llalgo.execute(llNodes, edges , false);
		int i = 0;
		//Obtaining clusters and creating matrices
		HashMap<Integer, Vector<NTNode>> temp = new HashMap<Integer, Vector<NTNode>>();
		for(LinLogNode lln : resultMap.keySet()){
			NTNode nn = (NTNode)lln;
			int cluster = resultMap.get(lln);
			Vector<NTNode> v = temp.get(cluster);
			if(v == null){
				v = new Vector<NTNode>();
				temp.put(cluster, v);
			}
			v.add(nn);
			i++;
		}
		
//		System.out.println("[NODETRIXVIZ] " + i + " MATRICES CREATED");
		i = 0;
		for(Vector<NTNode> v : temp.values()){
			matrices.add(new Matrix("[" + i + "]", v));
			i++;
		}
		
//		System.out.println("[NODETRIXVIZ] " + matrices.size() + " MATRICES IN TOTAL");
		//Create Internal Edges
		for(Matrix m : matrices){
			m.adjustEdgeAppearance();
			m.performEdgeAppearanceChange();
		}
	}
	
//    public NTExtraEdge addExtraEdge(NTNode tail, NTNode head){
//        return addExtraEdge(tail, head, EXTRA_LINK_COLOR, null);
//    }
//    
//    public NTIntraEdge addIntraEdge(NTNode tail, NTNode head){
//        return addIntraEdge(tail, head, INTRA_LINK_COLOR, null);
//    }
    
    /** Method is used if inputfile is passed directly to zvtm-ontotrix*/
    public NTEdge addEdge(NTNode tail, NTNode head){
    	return addEdge(tail, head, INTRA_LINK_COLOR, null);
//        if (tail.getMatrix() == head.getMatrix()){
//            return addIntraEdge(tail, head);
//        }
//        else {
//            return addExtraEdge(tail, head);
//        }
    }
    
    public NTEdge addEdge(NTNode tail, NTNode head, Color c, Object owner){
    	NTEdge e = new NTEdge(tail, head, c);
    	e.setOwner(owner);
    	tail.addOutgoingEdge(e);
    	head.addIncomingEdge(e);
    	return e;
    }
//    /**Creates a new External edge between the two nodes.
//     * @param NTNode tail, NTNode head
//     * @pararm Color c - colour of the edge
//     * @param Object owner - can be null, but can be used to attach an arbitrary object to this edge.
//     **/
//    public NTExtraEdge addExtraEdge(NTNode tail, NTNode head, Color c, Object owner){
//        NTExtraEdge e = new NTExtraEdge(tail, head, c);
//        e.setOwner(owner);
//        tail.addOutgoingEdge(e);
//        head.addIncomingEdge(e);
//        return e;
//    }
//    
//    /**Creates a new matrix-internal edge between the two nodes.
//     * @param NTNode tail, NTNode head
//     * @pararm Color c - colour of the edge
//     * @param Object owner - can be null, but can be used to attach an arbitrary object to this edge.
//     **/
//    public NTIntraEdge addIntraEdge(NTNode tail, NTNode head, Color c, Object owner){
//        NTIntraEdge e = new NTIntraEdge(tail, head, c);
//        e.setOwner(owner);
//       tail.addOutgoingEdge(e);
//       	head.addIncomingEdge(e);
//        return e;
//    }
    
    /**
     *@return all matrices in this visualization
     */
//    public Matrix[] getMatrices(){
//        return matrices;
//    }
    public Vector<Matrix> getMatrices(){
        return matrices;
    }
    
    // have to find something better than this constant...
    double SCALE = 40;
    
    //---------------------VISUALISE---------------------VISUALISE---------------------VISUALISE---------------------VISUALISE---------------------VISUALISE
    
    public void createViz(VirtualSpace vs){
        Map<Matrix,Map<Matrix,Double>> llg = new HashMap<Matrix,Map<Matrix,Double>>();
        // keep trace of matrices tha are not part of the graph ; we still want to display them
        HashMap<Matrix,Object> orphanMatrices = new HashMap();
        for (Matrix m:matrices){
            orphanMatrices.put(m, null);
        }
        // building LLL graph to feed to the layout algorithm
        for (Matrix matrix : matrices){
//        	if(matrix == null) continue;
            for (Matrix matrix2 : matrices){
//            	if(matrix2 == null) continue;
            	if (matrix != matrix2 && matrix.isConnectedTo(matrix2)){
                    if (llg.get(matrix) == null){
                        llg.put(matrix, new HashMap<Matrix,Double>());
                        orphanMatrices.remove(matrix);
                    }
                    llg.get(matrix).put(matrix2, new Double(matrix.nodes.size()));
                }
            }
        }
        // for each orphan matrix, create an artifical link (that will not be displayed)
        // between orphan matrix and one random matrix that is part of the main graph
        // give it a weak weight (0.1) 
		for (Matrix m:orphanMatrices.keySet()){
		    llg.put(m, new HashMap<Matrix,Double>());
		    llg.get(m).put(llg.keySet().iterator().next(), new Double(0.1));
		}
        llg = makeSymmetricGraph(llg);
        Map<Matrix,Node> matrixToLLNode = makeNodes(llg);
        List<Node> llnodes = new ArrayList<Node>(matrixToLLNode.values());
        List<Edge> lledges = makeEdges(llg, matrixToLLNode);
		Map<Node,double[]> nodeToPosition = makeInitialPositions(llnodes);
		// see class MinimizerBarnesHut for a description of the parameters;
		// for classical "nice" layout (uniformly distributed nodes), use
		new MinimizerBarnesHut(llnodes, lledges, -1.0, 2.0, 0.05).minimizeEnergy(nodeToPosition, LINLOG_ITERATIONS);
		// following might actually be useless, not sure yet...
		//Map<Node,Integer> nodeToCluster = new OptimizerModularity().execute(llnodes, lledges, false);
		// EOU		
        for (Node node : nodeToPosition.keySet()) {
			double[] position = nodeToPosition.get(node);
			node.getMatrix().createNodeGraphics(Math.round(position[0]*SCALE), Math.round(position[1]*SCALE), vs);
		}
    }

	/** Finish creating the visualization.
	 *  Make sure the view has been painted once so that we have access to VText bounding boxes
	 *  before instantiating the remaining graphical elements
	 */
    public void finishCreateViz(VirtualSpace vs){
        for(Matrix m : matrices){
			m.adjustEdgeAppearance();
			m.performEdgeAppearanceChange();
		}
        for (Matrix m:matrices){
            m.finishCreateNodeGraphics(vs);
        }
        for (Matrix m:matrices){
		    m.createEdgeGraphics(vs);
		}
        
        Collections.sort(matrices, new MatrixSizeComparator());
        for (Matrix m:matrices){
		    m.onTop(vs);
		}
    }
    
    
    private static Map<Matrix,Map<Matrix,Double>> makeSymmetricGraph(Map<Matrix,Map<Matrix,Double>> graph){
		Map<Matrix,Map<Matrix,Double>> result = new HashMap<Matrix,Map<Matrix,Double>>();
		for (Matrix m1 : graph.keySet()) {
			for (Matrix m2 : graph.get(m1).keySet()) {
				double weight = graph.get(m1).get(m2);
				double revWeight = 0.0f;
				if (graph.get(m2) != null && graph.get(m2).get(m1) != null) {
					revWeight = graph.get(m2).get(m1);
				}
				if (result.get(m1) == null){
				    result.put(m1, new HashMap<Matrix,Double>());
				}
				result.get(m1).put(m2, weight+revWeight);
				if (result.get(m2) == null){
				    result.put(m2, new HashMap<Matrix,Double>());
				}
				result.get(m2).put(m1, weight+revWeight);
			}
		}
		return result;
	}
	
	private static Map<Matrix,Node> makeNodes(Map<Matrix,Map<Matrix,Double>> graph){
		Map<Matrix,Node> result = new HashMap<Matrix,Node>();
		for (Matrix m : graph.keySet()){
            double nodeWeight = 0.0;
            for (double edgeWeight : graph.get(m).values()) {
                nodeWeight += edgeWeight;
            }
			result.put(m, new Node(m, nodeWeight));
		}
		return result;
	}
	
	private static List<Edge> makeEdges(Map<Matrix,Map<Matrix,Double>> graph, Map<Matrix,Node> matrixToLLNode){
        List<Edge> result = new ArrayList<Edge>();
        for (Matrix m1 : graph.keySet()) {
            for (Matrix m2 : graph.get(m1).keySet()) {
                Node m1Node = matrixToLLNode.get(m1);
                Node m2Node = matrixToLLNode.get(m2);
                double weight = graph.get(m1).get(m2);
                result.add(new Edge(m1Node, m2Node, weight));
            }
        }
        return result;
    }
    
    private static Map<Node,double[]> makeInitialPositions(List<Node> nodes){
        Map<Node,double[]> result = new HashMap<Node,double[]>();
		for (Node node : nodes) {
            double[] position = { Math.random() - 0.5,
                                  Math.random() - 0.5,
                                  0.0 };
            result.put(node, position);
		}
		return result;
	}
    
    
    //---------------ORGANISING COMPONENTS---------------ORGANISING COMPONENTS---------------ORGANISING COMPONENTS---------------ORGANISING COMPONENTS---------------ORGANISING COMPONENTS---------------ORGANISING COMPONENTS
    
    public void reorganiseAllMatrices(AnimationManager am)
    {
    	regroupMatrices();
    	splitAllMatrices(am);
    	mergeAllMatrices();
    }
    
    public void splitAllMatrices(AnimationManager am){
    	HashMap<String, Matrix> newMatrices = new HashMap<String, Matrix>();
    	Vector<Matrix> toRemove = new Vector<Matrix>();
    	for(Matrix m : matrices){
    		HashMap<String, Matrix> currentNew = m.splitMatrix(am);
    		if(currentNew == null) continue;
    		newMatrices.putAll(currentNew);
    		toRemove.add(m);
    	}
 
    	for(Matrix m : toRemove){
    		matrices.remove(m);
    	}

    	matrices.addAll(newMatrices.values());
    	reorderMatricesCMK();
    }
    
    public void mergeAllMatrices()
    {
    	HashMap<String, Vector<Matrix>> mergeMap = new HashMap<String, Vector<Matrix>>();
    	for(Matrix m : matrices){
    		String name = m.getName();
    		if(!mergeMap.containsKey(name)){
    			mergeMap.put(name, new Vector<Matrix>());
    		}
    		mergeMap.get(name).add(m);
    	}	
    	
    	for(Vector<Matrix> vector : mergeMap.values()){
    		if(vector.size() < 2) continue;
    		mergeParticulaMatrices((Vector<Matrix>)vector.clone());
    	}
    }
    
    public void mergeParticulaMatrices(Vector<Matrix> mergeMatrices)
    {
    	long xCenter = 0;
    	long yCenter = 0;
    	for(Matrix m : mergeMatrices){
    		xCenter += m.getPosition().x;
       		yCenter += m.getPosition().y;
    	}
    	xCenter /= mergeMatrices.size();
       	yCenter /= mergeMatrices.size();
           	
       	long offset = 0;
    	for(Matrix m : mergeMatrices){
    		m.move(xCenter + offset, yCenter- offset);
    		offset += m.bkg.getWidth();
    	}
    }
    
    public void mergeMatrices(VirtualSpace vs, AnimationManager am){
    	//TODO
    }
    
    /**
     * Iterates over all matrices and group their nodes according to their assigned
     * groupname.
     */
    public void regroupMatrices()
    {
    	for(Matrix m : matrices){
    		m.regroup();
    	}
    }
    
    public void reorderMatricesCMK()
	{
		for(Matrix m : matrices)
		{	
			m.reorderCutHillMcKee();
		}	
	}
    
}
