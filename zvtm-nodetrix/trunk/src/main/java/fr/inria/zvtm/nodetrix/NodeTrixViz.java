/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import java.util.Vector;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.RepaintAdapter;

import fr.inria.zvtm.nodetrix.lll.Node;
import fr.inria.zvtm.nodetrix.lll.Edge;
import fr.inria.zvtm.nodetrix.lll.MinimizerBarnesHut;
import fr.inria.zvtm.nodetrix.lll.OptimizerModularity;

public class NodeTrixViz {
    
    /* Matrix appearance */
    static long CELL_SIZE = 20;
    static Color MATRIX_FILL_COLOR = Color.WHITE;
    static Color MATRIX_STROKE_COLOR = Color.BLACK;
    static Color MATRIX_LABEL_COLOR = Color.DARK_GRAY;
    static Color INTRA_LINK_COLOR = new Color(160,202,254);
    static Color EXTRA_LINK_COLOR = new Color(118,98,252);
    static int MATRIX_NODE_LABEL_DIST_BORDER = 2;
    static Color MATRIX_NODE_LABEL_BKG_COLOR = new Color(250,205,155);
    
    /* Links between matrices */
    static Color INTER_LINK_COLOR = Color.BLACK;

    /* Matrices in this visualization */
    Matrix[] matrices;
    
    public NodeTrixViz(){
        matrices = new Matrix[0];
    }
    
    public Matrix addMatrix(String name, Vector<NTNode> nodes){
        Matrix res = new Matrix(name, nodes.toArray(new NTNode[nodes.size()]));
        Matrix[] na = new Matrix[matrices.length+1];
        System.arraycopy(matrices, 0, na, 0, matrices.length);
        na[matrices.length] = res;
        matrices = na;
        return res;
    }
    
    public NTEdge addEdge(NTNode tail, NTNode head){
        if (tail.getMatrix() == head.getMatrix()){
            return addIntraEdge(tail, head);
        }
        else {
            return addExtraEdge(tail, head);
        }
    }
    
    public NTExtraEdge addExtraEdge(NTNode tail, NTNode head){
        NTExtraEdge e = new NTExtraEdge(tail, head);
        tail.addOutgoingEdge(e);
        head.addIncomingEdge(e);
        return e;
    }
    
    public NTIntraEdge addIntraEdge(NTNode tail, NTNode head){
        NTIntraEdge e = new NTIntraEdge(tail, head);
        tail.addOutgoingEdge(e);
        head.addIncomingEdge(e);
        return e;
    }
    
    /**
     *@return all matrices in this visualization
     */
    public Matrix[] getMatrices(){
        return matrices;
    }
    
    // have to find something better than this constant...
    double SCALE = 40;
    
    public void createViz(VirtualSpace vs){
        Map<Matrix,Map<Matrix,Double>> llg = new HashMap<Matrix,Map<Matrix,Double>>();
        // keep trace of matrices tha are not part of the graph ; we still want to display them
        HashMap<Matrix,Object> orphanMatrices = new HashMap();
        for (Matrix m:matrices){
            orphanMatrices.put(m, null);
        }
        // building LLL graph to feed to the layout algorithm
        for (Matrix matrix : matrices){
            for (Matrix matrix2 : matrices){
                if (matrix != matrix2 && matrix.isConnectedTo(matrix2)){
                    if (llg.get(matrix) == null){
                        llg.put(matrix, new HashMap<Matrix,Double>());
                        orphanMatrices.remove(matrix);
                    }
                    llg.get(matrix).put(matrix2, new Double(matrix.nodes.length));
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
		new MinimizerBarnesHut(llnodes, lledges, -1.0, 2.0, 0.05).minimizeEnergy(nodeToPosition, 100);
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
        for (Matrix m:matrices){
            m.finishCreateNodeGraphics(vs);
        }
        for (Matrix m:matrices){
		    m.createEdgeGraphics(vs);
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
	
}
