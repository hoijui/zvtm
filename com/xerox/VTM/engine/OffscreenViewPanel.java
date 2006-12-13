/*   FILE: OffscreenViewPanel.java
 *   DATE OF CREATION:  Thu Jan 20 09:06:13 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: OffscreenViewPanel.java,v 1.3 2005/12/05 15:25:26 epietrig Exp $
 */
 
package com.xerox.VTM.engine;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Vector;

/**
 * Each view runs in its own thread - uses double buffering
 * @author Emmanuel Pietriga
 */
public class OffscreenViewPanel extends ViewPanel {

    /**for Double Buffering*/
    BufferedImage buffImg;

    public BufferedImage getImage(){
	return this.buffImg;
    }

    public OffscreenViewPanel(Vector cameras){
	//init of camera array
	cams = new Camera[cameras.size()];  //array of Camera
	for (int nbcam=0;nbcam<cameras.size();nbcam++){
	    cams[nbcam] = (Camera)(cameras.get(nbcam));
	}
	//init other stuff
	setBackground(Color.white);
    }

    public synchronized void stop(){}

    public Dimension getSize(){
	return (size != null) ? size : new Dimension(0,0);
    }

    public BufferedImage rasterize(int w, int h){
	return rasterize(w, h, backColor);
    }

    public BufferedImage rasterize(int w, int h, Color backgroundColor){
	backColor = backgroundColor;
	size = new Dimension(w, h);
	Graphics2D BufferG2D = null;
	Graphics2D g2d = null;
	if (buffImg == null){
	    buffImg = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
	}
	if (BufferG2D == null) {
	    BufferG2D = buffImg.createGraphics();
	}
	BufferG2D.setFont(VirtualSpaceManager.mainFont);
	if (antialias){BufferG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);}
	else {BufferG2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF);}
	g2d = BufferG2D;
	standardStroke=g2d.getStroke();
	standardTransform=g2d.getTransform();
	g2d.setPaintMode();
	g2d.setBackground(backColor);
	g2d.clearRect(0,0,size.width,size.height);
	//begin actual drawing here
	for (int nbcam=0;nbcam<cams.length;nbcam++){
	    if ((cams[nbcam]!=null) && (cams[nbcam].enabled)){
		camIndex=cams[nbcam].getIndex();
		drawnGlyphs=cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
		synchronized(drawnGlyphs){
		    drawnGlyphs.removeAllElements();
		    uncoef=(float)((cams[nbcam].focal+cams[nbcam].altitude)/cams[nbcam].focal);
		    viewW=this.getSize().width;//compute region's width and height
		    viewH=this.getSize().height;
		    viewEC = (long)(cams[nbcam].posx+viewW/2*uncoef);
		    viewNC = (long)(cams[nbcam].posy+viewH/2*uncoef);
		    viewWC = (long)(cams[nbcam].posx-viewW/2*uncoef);
		    viewSC = (long)(cams[nbcam].posy-viewH/2*uncoef);
		    gll = cams[nbcam].parentSpace.getVisibleGlyphList();
		    for (int i=0;i<gll.length;i++){
			if (gll[i].visibleInRegion(viewWC, viewNC, viewEC, viewSC, camIndex)){
			    synchronized(gll[i]){
				gll[i].project(cams[nbcam], size);
				if (gll[i].isVisible()){
				    gll[i].draw(g2d,size.width,size.height,cams[nbcam].getIndex(),standardStroke,standardTransform, 0, 0);
				    cams[nbcam].parentSpace.drewGlyph(gll[i],camIndex);
				}
			    }
			}
		    }
		}
	    }
	}
	//end drawing here
	return buffImg;
    }

}
