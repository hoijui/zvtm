package fr.inria.zvtm.nodetrix.lll;
//Copyright (C) 2008 Andreas Noack
//
//This library is free software; you can redistribute it and/or
//modify it under the terms of the GNU Lesser General Public
//License as published by the Free Software Foundation; either
//version 2.1 of the License, or (at your option) any later version.
//
//This library is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public
//License along with this library; if not, write to the Free Software
//Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA 

import java.util.*;

import fr.inria.zvtm.nodetrix.NodeTrixViz;
import fr.inria.zvtm.nodetrix.lll.*;


/**
 * Optimizer for a generalization of Newman and Girvan's Modularity measure,
 *   for computing graph clusterings.
 * The Modularity measure is generalized to arbitrary node weights;
 *   it is recommended to set the weight of each node to its degree,
 *   i.e. the total weight of its edges, as Newman and Girvan did.
 * For more information on the (used version of the) Modularity measure, see
 *   M. E. J. Newman: "Analysis of weighted networks", 
 *   Physical Review E 70, 056131, 2004.
 * For the relation of Modularity to the LinLog energy model, see
 *   Andreas Noack: <a href="http://arxiv.org/abs/0807.4052">
 *   "Modularity clustering is force-directed layout"</a>,
 *   Preprint arXiv:0807.4052, 2008.
 *   
 * @author Andreas Noack (an@informatik.tu-cottbus.de)
 * @version 13.11.2008
 */
public class LinLogOptimizerModularity {

	/**
     * Returns the negative modularity.
     * @param interAtedges  edge weight between different clusters
     * @param interAtpairs  weighted node pairs between different clusters
     * @param atedges  total edge weight of the graph
     * @param atpairs  total weighted node pairs of the graph
     * @return negative modularity
     */
    private double quality(final double interAtedges, final double interAtpairs, 
            final double atedges, final double atpairs) {
        return interAtedges/atedges - interAtpairs/atpairs;
    }
    
    
    /**
     * Improves a graph clustering by greedily moving nodes between clusters.
     * @param nodeToCluster  graph nodes with their current clusters 
     *   (input and output parameter)
     * @param nodeToEdges  graph nodes with their incident edges
     * @param atedges  total edge weight of the g
     * raph
     * @param atpairs  total weighted node pairs of the graph
     */
    private void refine(final Map<LinLogNode,Integer> nodeToCluster, final Map<LinLogNode,List<LinLogEdge>> nodeToEdges, 
            final double atedges, final double atpairs) {
        int maxCluster = 0;
        for (int cluster : nodeToCluster.values()) {
            maxCluster = Math.max(maxCluster, cluster);
        }

        // compute clusterToAtnodes, interAtedges, interAtpairs
        double[] clusterToAtnodes = new double[nodeToCluster.keySet().size()+1];
        for (LinLogNode node : nodeToCluster.keySet()) {
            clusterToAtnodes[nodeToCluster.get(node)] += node.weight;
        }
        double interAtedges = 0.0;
        for (List<LinLogEdge> edges : nodeToEdges.values()) {
            for (LinLogEdge edge : edges) {
                if ( !nodeToCluster.get(edge.from).equals(nodeToCluster.get(edge.to)) ) {
                    interAtedges += edge.weight;
                }
            }
        }
        double interAtpairs = 0.0;
        for (LinLogNode node : nodeToCluster.keySet()) interAtpairs += node.weight;
        interAtpairs *= interAtpairs; 
        for (double clusterAtnodes : clusterToAtnodes) interAtpairs -= clusterAtnodes * clusterAtnodes;

        // greedily move nodes between clusters 
        double prevQuality = Double.MAX_VALUE;
        double quality = quality(interAtedges, interAtpairs, atedges, atpairs);
//        System.out.println("Refining " + nodeToCluster.keySet().size() 
//                                       + " nodes, initial modularity " + -quality);
        while (quality < prevQuality) {
            prevQuality = quality;
            for (LinLogNode node : nodeToCluster.keySet()) {
                int bestCluster = 0; 
                double bestQuality = quality, bestInterAtedges = interAtedges, bestInterAtpairs = interAtpairs;
                double[] clusterToAtedges = new double[nodeToCluster.keySet().size()+1];
                for (LinLogEdge edge : nodeToEdges.get(node)) {
                    if (!edge.to.equals(node)) {
                        // count weight twice to include reverse edge
                        clusterToAtedges[nodeToCluster.get(edge.to)] += 2*edge.weight;
                    }
                }
                int cluster = nodeToCluster.get(node);
                for (int newCluster = 0; newCluster <= maxCluster+1; newCluster++) {
                    if (cluster == newCluster) continue;
                    double newInterPairs = interAtpairs
                        + clusterToAtnodes[cluster] * clusterToAtnodes[cluster]
                        - (clusterToAtnodes[cluster]-node.weight) * (clusterToAtnodes[cluster]-node.weight)
                        + clusterToAtnodes[newCluster] * clusterToAtnodes[newCluster]
                        - (clusterToAtnodes[newCluster]+node.weight) * (clusterToAtnodes[newCluster]+node.weight);
                    double newInterEdges = interAtedges 
                        + clusterToAtedges[cluster]
                        - clusterToAtedges[newCluster];
                    double newQuality = quality(newInterEdges, newInterPairs, atedges, atpairs); 
                    if (bestQuality - newQuality > 1e-8) {
                        bestCluster = newCluster;
                        bestQuality = newQuality; bestInterAtedges = newInterEdges; bestInterAtpairs = newInterPairs;
                    }
                }
                if ((bestQuality < quality) && (-quality <= NodeTrixViz.LINLOG_QUALITY)) {
                    clusterToAtnodes[cluster] -= node.weight;
                    clusterToAtnodes[bestCluster] += node.weight;
                    nodeToCluster.put(node, bestCluster);
                    maxCluster = Math.max(maxCluster, bestCluster);
                    quality = bestQuality; interAtedges = bestInterAtedges; interAtpairs = bestInterAtpairs;
//                    System.out.println(" Moving " + node + " to " + bestCluster + ", " 
//                            + "new modularity " + -quality);
                }
            }
        }
    }

    
    /**
     * Computes a graph clustering with a multi-scale algorithm.
     * @param nodes  graph nodes
     * @param edges  graph edges
     * @param atedges  total edge weight of the graph
     * @param atpairs  total weighted node pairs of the graph
     * @return clustering with large Modularity,
     *   as map from graph nodes to cluster IDs. 
     */
    private Map<LinLogNode,Integer> cluster(final Collection<LinLogNode> nodes, final List<LinLogEdge> edges, 
            final double atedges, final double atpairs) {
//        System.out.println("Contracting " + nodes.size() + " nodes, " + edges.size() + " edges");
        
        // contract nodes
        Collections.sort(edges, new Comparator<LinLogEdge>() { 
            public int compare(LinLogEdge e1, LinLogEdge e2) {
                if (e1.density == e2.density) return 0;
                return e1.density < e2.density ? +1 : -1;
            }
        });
        Map<LinLogNode,LinLogNode> nodeToContr = new HashMap<LinLogNode,LinLogNode>();
        List<LinLogNode> contrNodes = new ArrayList<LinLogNode>();
        for (LinLogEdge edge : edges) {
            if (edge.density < atedges/atpairs) break;
            if (edge.from.equals(edge.to)) continue;
            if (nodeToContr.containsKey(edge.from) || nodeToContr.containsKey(edge.to)) continue;
            // randomize contraction
            // if (!nodeToContr.isEmpty() && Math.random() < 0.5) continue;
            
//            System.out.println(" Contracting " + edge);
            LinLogNode contrNode = new LinLogNode(
                    edge.from.name + " " + edge.to.name,
                    edge.from.weight + edge.to.weight);
            nodeToContr.put(edge.from, contrNode);
            nodeToContr.put(edge.to, contrNode);
            contrNodes.add(contrNode);
        }
        // terminal case: no nodes to contract
        if (nodeToContr.isEmpty()) {
            Map<LinLogNode,Integer> nodeToCluster = new HashMap<LinLogNode,Integer>();
            int clusterId = 0;
            for (LinLogNode node : nodes) nodeToCluster.put(node, clusterId++);
            return nodeToCluster;
        }
        // "contract" singleton clusters
        for (LinLogNode node : nodes) {
            if (!nodeToContr.containsKey(node)) {
                LinLogNode contrNode = new LinLogNode(node.name, node.weight);
                nodeToContr.put(node, contrNode);
                contrNodes.add(contrNode);
            }
        }
        
        // contract edges
        Map<LinLogNode,Map<LinLogNode,Double>> startToEndToWeight = new HashMap<LinLogNode,Map<LinLogNode,Double>>();
        for (LinLogNode contrNode : contrNodes) {
            startToEndToWeight.put(contrNode, new HashMap<LinLogNode,Double>());
        }
        for (LinLogEdge edge : edges) {
            LinLogNode contrStart = nodeToContr.get(edge.from);
            LinLogNode contrEnd   = nodeToContr.get(edge.to);
            double contrWeight = 0.0;
            Map<LinLogNode,Double> endToWeight = startToEndToWeight.get(contrStart); 
            if (endToWeight.containsKey(contrEnd)) {
                contrWeight = endToWeight.get(contrEnd);
            }
            endToWeight.put(contrEnd, contrWeight + edge.weight);
        }   
        List<LinLogEdge> contrEdges = new ArrayList<LinLogEdge>();
        for (LinLogNode contrStart : startToEndToWeight.keySet()) {
            Map<LinLogNode,Double> endToWeight = startToEndToWeight.get(contrStart);
            for (LinLogNode contrEnd : endToWeight.keySet()) {
                LinLogEdge contrEdge = new LinLogEdge(contrStart, contrEnd, endToWeight.get(contrEnd));
                contrEdges.add(contrEdge);
            }
        }

        // cluster contracted graph
        Map<LinLogNode,Integer> contrNodeToCluster 
            = cluster(contrNodes, contrEdges, atedges, atpairs);
        
        // decontract clustering
        Map<LinLogNode,Integer> nodeToCluster = new HashMap<LinLogNode,Integer>();
        for (LinLogNode node : nodeToContr.keySet()) {
            nodeToCluster.put(node, contrNodeToCluster.get(nodeToContr.get(node)));
        }

        // refine decontracted clustering
        Map<LinLogNode,List<LinLogEdge>> nodeToEdge = new HashMap<LinLogNode,List<LinLogEdge>>();
        for (LinLogNode node : nodes) nodeToEdge.put(node, new ArrayList<LinLogEdge>());
        for (LinLogEdge edge : edges) nodeToEdge.get(edge.from).add(edge);
        refine(nodeToCluster, nodeToEdge, atedges, atpairs);

        return nodeToCluster;
    }
    
    
    /**
     * Computes a clustering of a given graph by maximizing the Modularity.
     * @param nodes  weighted nodes of the graph.
     *   It is recommended to set the weight of each node to the sum 
     *   of the weights of its edges.  Weights must not be negative.   
     * @param edges  weighted edges of the graph.
     *   Omit edges with weight 0.0 (i.e. non-edges).  
     *   For unweighted graphs use weight 1.0 for all edges.
     *   Weights must not be negative.   
     *   Weights must be symmetric, i.e. the weight  
     *   from node <code>n1</code> to node <code>n2</code> must be equal to
     *   the weight from node <code>n2</code> to node <code>n1</code>. 
     * @param ignoreLoops  set to <code>true</code> to use an adapted version
     *   of Modularity for graphs without loops (edges whose start node
     *   equals the end node)
     * @return clustering with large Modularity,
     *   as map from graph nodes to cluster IDs. 
     */
    public Map<LinLogNode,Integer> execute(
            final List<LinLogNode> nodes, final List<LinLogEdge> edges, 
            final boolean ignoreLoops) {

        // compute atedgeCnt and atpairCnt
        double atedgeCnt = 0.0; 
        for (LinLogEdge edge : edges) {
            if (!ignoreLoops || !edge.from.equals(edge.to)) { 
                atedgeCnt += edge.weight;
            }
        }
        double atpairCnt = 0.0; 
        for (LinLogNode node : nodes) atpairCnt += node.weight;
        atpairCnt *= atpairCnt;
        if (ignoreLoops) { 
            for (LinLogNode node : nodes) atpairCnt -= node.weight*node.weight;
        }
        
        // compute clustering
        return cluster(nodes, edges, atedgeCnt, atpairCnt);
    }
    
}
