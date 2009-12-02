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
    
    public NTNode(String name){
        this.name = name;
    }
    
    public void setMatrix(Matrix m){
        this.matrix = m;
    }
    
    public Matrix getMatrix(){
        return this.matrix;
    }

    public String toString(){
        return name;
    }

}
