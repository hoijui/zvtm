/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.atc;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;

import com.xerox.VTM.engine.VirtualSpaceManager;
import net.claribole.zvtm.glyphs.VTextST;

public class BText extends VTextST {

	Color borderColor = Color.BLACK;

	/**
		*@param t text string
		*@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
		*/
	public BText(String t, float a){
		super(t, a);
	}

	/**
		*@param x coordinate in virtual space
		*@param y coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param c fill color
		*@param t text string
		*@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
		*/
	public BText(long x,long y, int z,Color c,String t, float a){
		super(x, y, z, c, t, a);
	}

	/**
		*@param x coordinate in virtual space
		*@param y coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param c fill color
		*@param t text string
		*@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
		*@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
		*/
	public BText(long x,long y, int z,Color c,String t,short ta, float a){
		super(x, y, z, c, t, ta, a);
	}

	/**
		*@param x coordinate in virtual space
		*@param y coordinate in virtual space
		*@param z z-index (pass 0 if you do not use z-ordering)
		*@param c fill color
		*@param t text string
		*@param ta text-anchor (for alignment: one of TEXT_ANCHOR_*)
		*@param a alpha channel value in [0;1.0] 0 is fully transparent, 1 is opaque
		*@param scale scaleFactor w.r.t original image size
		*/
	public BText(long x,long y, int z,Color c,String t,short ta, float a, float scale){
		super(x, y, z, c, t, ta, a, scale);
	}

	public void draw(Graphics2D g,int vW,int vH,int i,Stroke stdS,AffineTransform stdT, int dx, int dy){
		trueCoef = scaleFactor * coef;
		if (trueCoef*fontSize > vsm.getTextDisplayedAsSegCoef() || !zoomSensitive){
			//if this value is < to about 0.5, AffineTransform.scale does not work properly (anyway, font is too small to be readable)
			g.setFont((font!=null) ? font : VirtualSpaceManager.getMainFont());
			if (!pc[i].valid){
				bounds = g.getFontMetrics().getStringBounds(text,g);
				// cw and ch actually hold width and height of text *in virtual space*
				pc[i].cw = (int)Math.round(bounds.getWidth() * scaleFactor);
				pc[i].ch = (int)Math.round(bounds.getHeight() * scaleFactor);
				pc[i].valid=true;
			}
			if (text_anchor==TEXT_ANCHOR_START){at=AffineTransform.getTranslateInstance(dx+pc[i].cx,dy+pc[i].cy);}
			else if (text_anchor==TEXT_ANCHOR_MIDDLE){at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef/2.0f,dy+pc[i].cy);}
			else {at=AffineTransform.getTranslateInstance(dx+pc[i].cx-pc[i].cw*coef,dy+pc[i].cy);}
			if (zoomSensitive){at.concatenate(AffineTransform.getScaleInstance(trueCoef, trueCoef));}
			g.setTransform(at);
			if (alpha < 1.0f){
				g.setComposite(acST);

//				g.setColor(Color.BLACK);
//				g.fillRect(dx-2, dy-pc[i].ch+1, pc[i].cw+4, pc[i].ch+1);
				g.setColor(this.color);

				g.drawString(text, 0.0f, 0.0f);
				g.setComposite(acO);
			}
			else {
//				g.setColor(Color.BLACK);
//				g.fillRect(dx-2, dy-pc[i].ch+6, pc[i].cw+4, pc[i].ch);
				g.setColor(this.color);
				g.drawString(text, 0.0f, 0.0f);
			}
			g.setTransform(stdT);
		}
		else {
			if (alpha < 1.0f){
				g.setComposite(acST);
				g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
				g.setComposite(acO);
			}
			else {
				g.fillRect(dx+pc[i].cx,dy+pc[i].cy,1,1);
			}
		}
	}

}
