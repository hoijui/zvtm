/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;
import javax.swing.SwingUtilities;
import java.lang.reflect.InvocationTargetException;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;

/** Description of any ZVTM glyph sibclass of ClosedShape to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public class ClosedShapeDescription extends ObjectDescription {

    private volatile ClosedShape glyph;
    boolean inSpace = false;

    /** Constructs the description of a ZVTM ClosedShape.
        *@param id ID of object in scene.
        *@param g any ZVTM ClosedShape instance. Must implement fr.inria.zvtm.glyphs.Translucent if fade in/out transitions are used in the parent region.
        *@param z z-index (layer). Feed 0 if you don't know.
        *@param pr parent Region in scene.
        *@param sensitive should the glyph be made sensitive to cursor events or not.
        */
    ClosedShapeDescription(String id, ClosedShape g, int z, Region pr, boolean sensitive){
        this.id = id;
        this.glyph = g;
        this.zindex = z;
		this.glyph.setZindex(this.zindex);
        this.parentRegion = pr;
        this.sensitive = sensitive;
    }

    /** Called automatically by scene manager, but can be called by client application to force loading of objects not actually visible. */
    @Override
    public void createObject(final SceneManager sm, final VirtualSpace vs, boolean fadeIn){
        if (!inSpace){
            if (fadeIn){
                glyph.setTranslucencyValue(0.0f);
                if (!sensitive){glyph.setSensitivity(false);}
                //XXX:TBW FADE_ANIM_DATA should actually have a translucency value that equals the glyph's original value,
                //        not necessarily 1.0f
//                VirtualSpaceManager.INSTANCE.animator.createGlyphAnimation(GlyphLoader.FADE_IN_DURATION, AnimManager.GL_COLOR_LIN,
//                    GlyphLoader.FADE_IN_ANIM_DATA, glyph.getID());
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_IN_DURATION, glyph,
                    1.0f, false, IdentityInterpolator.getInstance(), null);
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
            else {
                if (!sensitive){glyph.setSensitivity(false);}
            }
            inSpace = true;
            try{
                SwingUtilities.invokeAndWait(new Runnable(){
                    public void run(){
                        vs.addGlyph(glyph);
                        glyph.setOwner(ClosedShapeDescription.this);
                        sm.objectCreated(ClosedShapeDescription.this);
                    }
                });
            } catch(InterruptedException ie) {
                /* swallowed */
            } catch(InvocationTargetException ite) {
                /* swallowed */
            }
        }
    }

    /** Called automatically by scene manager, but can be called by client application to force unloading of objects still visible. */
    @Override
    public void destroyObject(final SceneManager sm, final VirtualSpace vs, boolean fadeOut){
        if (inSpace){
            if (fadeOut){
//                VirtualSpaceManager.INSTANCE.animator.createGlyphAnimation(GlyphLoader.FADE_OUT_DURATION, AnimManager.GL_COLOR_LIN,
//                    GlyphLoader.FADE_OUT_ANIM_DATA, glyph.getID(),
//                    new ClosedShapeHideAction(vs));
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(GlyphLoader.FADE_OUT_DURATION, glyph,
                    0.0f, false, IdentityInterpolator.getInstance(), new ClosedShapeHideAction(sm, vs));
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
            else {
                try{
                    SwingUtilities.invokeAndWait(new Runnable(){
                        public void run(){
                            vs.removeGlyph(glyph);
                            sm.objectDestroyed(ClosedShapeDescription.this);
                        }
                    });
                } catch(InterruptedException ie) {
                    /* swallowed */
                } catch(InvocationTargetException ite) {
                    /* swallowed */
                }
            }
            inSpace = false;
        }
    }

    /** Get actual ClosedShape instance wrapped in this ZUIST object description. */
    public Glyph getGlyph(){
	    return glyph;
    }
    
    /** Get x-coordinate of object in virtual space. */
    public long getX(){
        return glyph.vx;
    }
    
    /** Get y-coordinate of object in virtual space. */
    public long getY(){
        return glyph.vy;
    }
    
}

class ClosedShapeHideAction implements EndAction {
    
    VirtualSpace vs;
    SceneManager sm;
    
    ClosedShapeHideAction(SceneManager sm, VirtualSpace vs){
	    this.sm = sm;
	    this.vs = vs;
    }
    
    public void	execute(Object subject, Animation.Dimension dimension) {
        try {
            vs.removeGlyph((Glyph)subject);
            sm.objectDestroyed((ClosedShapeDescription)((Glyph)subject).getOwner());
        }
        catch(ArrayIndexOutOfBoundsException ex){
            if (SceneManager.getDebugMode()){System.err.println("Warning: attempt at destroying rectangle " + ((Glyph)subject).hashCode() + " failed. Trying one more time.");}
            recoverFailingAnimationEnded(subject, dimension);
        }
    }

    public void recoverFailingAnimationEnded(Object subject, Animation.Dimension dimension){
        try {
            vs.removeGlyph((Glyph)subject);
            sm.objectDestroyed((ClosedShapeDescription)((Glyph)subject).getOwner());
        }
        catch(ArrayIndexOutOfBoundsException ex){
            if (SceneManager.getDebugMode()){System.err.println("Warning: attempt at destroying rectangle " + ((Glyph)subject).hashCode() + " failed. Giving up.");}
            recoverFailingAnimationEnded(subject, dimension);
        }
    }
    
}
