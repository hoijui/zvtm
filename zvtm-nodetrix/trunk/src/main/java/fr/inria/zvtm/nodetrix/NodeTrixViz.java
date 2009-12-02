/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.util.Vector;

public class NodeTrixViz {
    
    /* Matrices in this visualization */
    Matrix[] matrices;
    
    public NodeTrixViz(int ic){
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
    
    public void addEdge(NTNode tail, NTNode head){
        if (tail.getMatrix() == head.getMatrix()){
            addIntraEdge(tail, head);
        }
        else {
            addExtraEdge(tail, head);
        }
    }
    
    public void addExtraEdge(NTNode tail, NTNode head){
        System.out.println("Adding extra edge "+tail+" -> "+head);
    }
    
    public void addIntraEdge(NTNode tail, NTNode head){
        System.out.println("Adding intra edge "+tail+" -> "+head);        
    }
    
    /**
     *@return all matrices in this visualization
     */
    public Matrix[] getMatrices(){
        return matrices;
    }
    
}
