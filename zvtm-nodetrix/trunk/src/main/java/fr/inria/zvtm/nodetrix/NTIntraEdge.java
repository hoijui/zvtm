/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.nodetrix;

import java.awt.Color;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator2;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.VTriangleOr;

public class NTIntraEdge extends NTEdge {
    
    Glyph glyph, glyphTranslucent;
	LongPoint offset;
	AnimationManager animManager;
	
    public NTIntraEdge(NTNode t, NTNode h, Color c){
    	super(t,h,1);
        this.tail = t;
        this.head = h;
        this.edgeColor = c;
    }
    
    @Override
    protected void createGraphics(long height, long y, long x, long index, VirtualSpace vs) 
    {
    	this.animManager = VirtualSpaceManager.INSTANCE.getAnimationManager();
    	this.offset = new LongPoint(x, y);
    	LongPoint mp = tail.getMatrix().getPosition();
    	
    	long west = mp.x + x - NodeTrixViz.CELL_SIZE_HALF;
		long north =  mp.y + y + NodeTrixViz.CELL_SIZE_HALF;
		long east = mp.x + x + NodeTrixViz.CELL_SIZE_HALF;
		
		//non translucent glyph part
    	LongPoint[] p = new LongPoint[4];
    	p[0] = new LongPoint(east, north - index*height);
    	p[1] = new LongPoint(east, north - (index+1)*height );
    	p[2] = new LongPoint(west + (index+1)*height, north - (index+1)*height );
    	p[3] = new LongPoint(west + index*height, north - index*height );
    	glyph = new VPolygon(p, 0, edgeColor, edgeColor);
    	glyph.setStrokeWidth(0);
//    	glyph.setOwner(this);
    	glyph.setSensitivity(false);
    	vs.addGlyph(glyph);
    	
    	//translucent glyph part
    	p = new LongPoint[4];
    	p[0] = new LongPoint(west, north - index*height);
    	p[1] = new LongPoint(west + index*height, north - index*height);
    	p[2] = new LongPoint(west + (index+1)*height, north - (index+1)*height);
    	p[3] = new LongPoint(west, north - (index+1)*height );
    	glyphTranslucent = new VPolygon(p, 0, edgeColor, edgeColor, NodeTrixViz.INTRA_TRANSLUCENCY);
    	glyphTranslucent.setStrokeWidth(0);
//    	glyphTranslucent.setOwner(this);
    	glyphTranslucent.setSensitivity(false);
    	glyph.stick(glyphTranslucent);
    	vs.addGlyph(glyphTranslucent);
    }
    
    @Override 
    public void cleanGraphics(VirtualSpace vs){
    	System.out.println("GLYPH: "+ glyph.vx);
    	vs.removeGlyph(glyph);
    	vs.removeGlyph(glyphTranslucent);
    }
    
    //INTERACTION----------------------------------------------INTERACTION-----------------------------------------------INTERACTION-----------------------------------
    
    
    protected void reset()
    {
//    	Animation a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.DURATION_GENERAL,
//    			glyph,
//      			1,
//    			false, 
//    			SlowInSlowOutInterpolator2.getInstance(), 
//    			null);	
//    	animManager.startAnimation(a, true);
//    	a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.DURATION_GENERAL,
//    			glyphTranslucent,
//       			NodeTrixViz.INTRA_TRANSLUCENCY,
//    			false, 
//    			SlowInSlowOutInterpolator2.getInstance(), 
//    			null);	
//    	animManager.startAnimation(a, true);

    	glyphTranslucent.setColor(edgeColor);
    }
    
    protected void highlight(Color c)
    {	
    	glyphTranslucent.setColor(c);
    }
    
    protected void select()
    {
    }
    
    protected void fade()
    {
    	Animation a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.DURATION_GENERAL,
				glyph,
				NodeTrixViz.INTRA_TRANSLUCENCY_DIMMFACTOR,
				false, 
				SlowInSlowOutInterpolator2.getInstance(), 
				null);	
		animManager.startAnimation(a, true);
    	a = animManager.getAnimationFactory().createTranslucencyAnim(NodeTrixViz.DURATION_GENERAL,
				glyphTranslucent,
				NodeTrixViz.INTRA_TRANSLUCENCY * NodeTrixViz.INTRA_TRANSLUCENCY_DIMMFACTOR,
				false, 
				SlowInSlowOutInterpolator2.getInstance(), 
				null);	
		animManager.startAnimation(a, true);
	}
    
    
    
    
    
  //MOVING----------------------------------------------MOVING-----------------------------------------------MOVING-----------------------------------
    void moveTo(long x, long y){
        LongPoint mp = tail.getMatrix().getPosition();
        glyph.moveTo(mp.x +x, mp.y + y);
    }
    
    void move(long x, long y){
        glyph.move(x, y);
    }

	public void onTop(VirtualSpace vs) {
		vs.onTop(this.glyph);
		vs.onTop(this.glyphTranslucent);
	}
	
	public void reposition(){
	}
    
   

}
