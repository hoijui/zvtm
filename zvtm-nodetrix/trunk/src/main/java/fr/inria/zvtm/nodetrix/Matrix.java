/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.io.File;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VRectangle;

public class Matrix {
    
    String name;
    NTNode[] nodes;
    
    VRectangle bkg;
    
    public Matrix(String name, NTNode[] nodes){
        this.name = name;
        this.nodes = nodes;
        for (NTNode node : nodes){
            node.setMatrix(this);
        }
    }
    
    public void createGraphics(long x, long y, VirtualSpace vs){
        bkg = new VRectangle(x, y, 0,
                             nodes.length*NodeTrixViz.CELL_SIZE/2, nodes.length*NodeTrixViz.CELL_SIZE/2,
                             NodeTrixViz.MATRIX_FILL_COLOR, NodeTrixViz.MATRIX_STROKE_COLOR);
        vs.addGlyph(bkg);
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
    
    public String getName(){
        return name;
    }
    
    static int CELL_SIZE = 10;
    
    public static void setCellSize(int cs){
        CELL_SIZE = cs;
    }
    
}
