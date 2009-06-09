/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.wild.zuist;

public class ViewPort {
    
    int dx, dy;
    short device;
    int port;
    ClusterNode node;
    
    public ViewPort(int dx, int dy, short device, int port, ClusterNode cn){
        this.dx = dx;
        this.dy = dy;
        this.device = device;
        this.port = port;
        this.node = cn;
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
    
    public short getDevice(){
        return device;
    }
    
}
