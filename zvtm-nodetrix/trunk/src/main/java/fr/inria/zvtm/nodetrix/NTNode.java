/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

public class NTNode {

    String name;
    
    /* Owning matrix */
    Matrix matrix;
    
    NTEdge[] outgoingEdges, incomingEdges;
    
    public NTNode(String name){
        this.name = name;
    }
    
    public void addOutgoingEdge(NTEdge e){
        if (outgoingEdges == null){
            outgoingEdges = new NTEdge[1];
            outgoingEdges[0] = e;
        }
        else {
            NTEdge[] na = new NTEdge[outgoingEdges.length+1];
            System.arraycopy(outgoingEdges, 0, na, 0, outgoingEdges.length);
            na[outgoingEdges.length] = e;
            outgoingEdges = na;
        }
    }
    
    public void addIncomingEdge(NTEdge e){
        if (incomingEdges == null){
            incomingEdges = new NTEdge[1];
            incomingEdges[0] = e;
        }
        else {
            NTEdge[] na = new NTEdge[incomingEdges.length+1];
            System.arraycopy(incomingEdges, 0, na, 0, incomingEdges.length);
            na[incomingEdges.length] = e;
            incomingEdges = na;
        }
    }
    
    /**
     *@return null if empty
     */
    public NTEdge[] getIncomingEdges(){
        return incomingEdges;
    }

    /**
     *@return null if empty
     */
    public NTEdge[] getOutgoingEdges(){
        return outgoingEdges;
    }
    
    public void setMatrix(Matrix m){
        this.matrix = m;
    }
    
    public Matrix getMatrix(){
        return this.matrix;
    }

    public String toString(){
        return name+"@"+hashCode();
    }

}
