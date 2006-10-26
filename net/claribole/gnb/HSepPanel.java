/*   FILE: GeonamesBrowser.java
 *   DATE OF CREATION:  Mon Oct 23 08:42:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */ 

package net.claribole.gnb;

import java.awt.Dimension;
import java.awt.Color;
import javax.swing.JPanel;
import java.awt.Graphics;

/**A simple swing component that draws a horizontal line*/

public class HSepPanel extends JPanel {

    int h=1;
    boolean fill=true;
    Color color=Color.black;

    /**
     *@param thick thickness of line
     *@param filled fill the interior of the line when thickness>2
     *@param c color of the line (can be null, default is black)
     */
    public HSepPanel(int thick,boolean filled,Color c){
	this.h=thick;
	this.fill=filled;
	if (c!=null){color=c;}
    }

    public void setHeight(int h){this.h=h;}

    public void setFilled(boolean b){this.fill=b;}

    public void paint(Graphics g) {
	super.paint(g);
	Dimension d=this.getSize();
	g.setColor(color);
	if (fill){g.fillRect(1,(d.height-h)/2,d.width-2,h);}else{g.drawRect(1,(d.height-h)/2,d.width-2,h);}
    }
    

}
