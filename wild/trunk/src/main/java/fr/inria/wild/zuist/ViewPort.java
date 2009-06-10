/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.wild.zuist;

import java.awt.Dimension;

public class ViewPort {
    
    // all following data in pixels
    
    // offset x and y w.r.t wall center
    int dx, dy;
    // actual viewport width and height
    int w,h;
    // bezel with, height (on each side, assumes that they are symmetric)
    int bw, bh;
    // column and row number from top left, starting at 0,0 for bottom-most leftmost viewport
    int col,row;
    
    // graphcis device ID
    short device;
    
    // OSC port that should control this viewport
    int port;
    
    ClusterNode node;
    
    double[] wnes = new double[4];
    
    public ViewPort(int dx, int dy, int w, int h, int bw, int bh, int col, int row, short device, int port, ClusterNode cn){
        this.dx = dx;
        this.dy = dy;
        this.w = w;
        this.h = h;
        this.bw = bw;
        this.bh = bh;
        this.col = col;
        this.row = row;
        this.device = device;
        this.port = port;
        this.node = cn;
    }
    
    void computeRelativeBounds(double nbCols, double nbRows){
        wnes[0] = nbCols * col;
        wnes[2] = nbCols * (col+1);
        wnes[1] = nbRows * (row+1);
        wnes[3] = nbRows * row;
    }
    
    public ClusterNode getNode(){
        return node;
    }
    
    public int getPort(){
        return port;
    }
    
    public int getX(){
        return dx;
    }
    
    public int getY(){
        return dy;
    }

    public int getW(){
        return w;
    }
    
    public int getH(){
        return h;
    }

    public int getBW(){
        return bw;
    }
    
    public int getBH(){
        return bh;
    }
    
    public int getColumn(){
        return col;
    }
    
    public int getRow(){
        return row;
    }
    
    public short getDevice(){
        return device;
    }
    
}
