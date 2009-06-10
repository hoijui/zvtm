/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.wild.zuist;

public class ClusterNode {

    String hostName;
    ViewPort[] viewports = new ViewPort[0];
    
    ClusterNode(String name){
        this.hostName = name;
    }
    
    public void addViewPort(int dx, int dy, int w, int h, int bw, int bh, int col, int row, short device, int port){
        if (viewports.length <= device){
            ViewPort[] tmpA = new ViewPort[device+1];
            System.arraycopy(viewports, 0, tmpA, 0, viewports.length);
            viewports = tmpA;            
        }
        viewports[viewports.length-1] = new ViewPort(dx, dy, w, h, bw, bh, col, row, device, port, this);
    }
    
    public ViewPort[] getViewPorts(){
        return viewports;
    }
    
    public ViewPort getViewPort(short device){
        return viewports[device];
    }
    
    public String getHostName(){
        return hostName;
    }
    
}
