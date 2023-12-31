/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Image;
import java.awt.Color;
import java.awt.RenderingHints;
import javax.swing.ImageIcon;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;

/** Description of external resource objects to be loaded/unloaded in the scene.
 * Supports URL with the following protocols: file, http, jar (syntax jar:file:!/path/to/resource/in/jar)
 *@author Emmanuel Pietriga
 */

public abstract class ResourceDescription extends ObjectDescription {

    static final String FILE_PROTOCOL = "file";
    static final String JAR_PROTOCOL = "jar";
    static final String JAR_HEADER = "jar:file:!";

    /** URL identifying this resource. */
    protected URL src;

    /* necessary info about a resource for instantiation */
    protected double vx, vy;

    @Override
    public double getX(){
        return vx;
    }

    @Override
    public double getY(){
        return vy;
    }

    @Override
    public void moveTo(double x, double y){
        this.vx = x;
        this.vy = y;
    }

    /** Type of resource.
     *@return type of resource.
     */
    public abstract String getType();

    /** Set URL of this resource. */
    public void setURL(URL url){
        this.src = url;
    }

    /** Get the URL of this resource. */
    public URL getURL(){
        return src;
    }

    /** Returns true if the resource is on the computer running this application. */
    public boolean isLocal(){
        return src.getProtocol().equals(FILE_PROTOCOL);
    }

    public boolean isInJAR(){
        return src.getProtocol().startsWith(JAR_PROTOCOL);
    }

    /*------------------------- Visual feedback while fetching ----------------------- */

    /* Visual feedback w.r.t resource fetching */
    static boolean DEFAULT_SHOW_FEEDBACK_WHEN_FETCHING = false;
    boolean showFeedbackWhenFetching = DEFAULT_SHOW_FEEDBACK_WHEN_FETCHING;

    /** Set whether, by default, some visual feedback is given in the interface while loading the resource.
     *  The feedback appears as soon as the object's request starts being processed, and disappears
     *  when the resource is visually instantiated. Changes to this default value apply only to descriptions
     *  instantiated after the call to this method.
     */
    public static void setDefaultFeedbackWhenFetching(boolean b){
        DEFAULT_SHOW_FEEDBACK_WHEN_FETCHING = b;
    }

    /** Set whether some visual feedback is given in the interface while loading the resource.
     *  The feedback appears as soon as the object's request starts being processed, and disappears
     *  when the resource is visually instantiated.
     */
    public void setFeedbackWhenFetching(boolean b){
        showFeedbackWhenFetching = b;
    }

    /** Get whether some visual feedback is given in the interface while loading the resource.
     *  The feedback appears as soon as the object's request starts being processed, and disappears
     *  when the resource is visually instantiated.
     */
    public boolean getFeedbackWhenFetching(){
        return showFeedbackWhenFetching;
    }

    /*------------------------- VRectProgress Colors parameters ----------------------- */

    static Color DEFAULT_BAR_BKG_COLOR = Color.WHITE;
    static Color DEFAULT_BAR_COLOR = Color.BLACK;
    Color bgColor = DEFAULT_BAR_BKG_COLOR;
    Color barColor = DEFAULT_BAR_COLOR;

    public static void setProgressBarDefaultColors(Color bkg, Color bar) {
        DEFAULT_BAR_BKG_COLOR = bkg;
        DEFAULT_BAR_COLOR = bar;
    }

    public void setProgressBarBackgroundColor(Color c) {
        bgColor = c;
    }

    public void setProgressBarColor(Color c) {
        barColor = c;
    }

}
