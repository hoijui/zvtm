/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
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

/** Description of image objects to be loaded/unloaded in the scene.
 *@author Emmanuel Pietriga
 */

public abstract class ResourceDescription extends ObjectDescription {
	
	static final String FILE_PROTOCOL = "file";
	
	/** URL identifying this resource. */
	URL src;

    /* necessary info about a resource for instantiation */
    long vx, vy;

    public long getX(){
        return vx;
    }
    
    public long getY(){
        return vy;
    }

	/** Type of resource.
	 *@return type of resource.
	 */
	public abstract String getType();

	/** Set URL of this resource. */
	public void setURL(URL url){
		this.src = url;
	}
	
	///** Set URL of this resource. */
	//public void setURL(String path){
	//	if (path.indexOf(URL_PROTOCOL_SEQ) != -1){
	//		//patch fixed
	//		//XXX:make sure we support file:/, http:/, https:/, ftp:/
	//		path = "http"+path.substring(path.indexOf(URL_PROTOCOL_SEQ));
	//		try {
	//			this.src  = new URL(path);
	//		}
	//		catch(MalformedURLException ex){System.err.println("Error: malformed resource URL: "+path);}
	//	}
	//	else {
	//		// probably a relative file URL
	//		try {
	//			this.src  = (new File(path)).toURI().toURL();	
	//		}
	//		catch(MalformedURLException ex){System.err.println("Error: malformed resource URL: "+path);}			
	//	}
	//	
	//}

	/** Get the URI of this resource. */
	public URL getURL(){
		return src;
	}
	
	/** Returns true if the resource is on the computer running this application. */
	public boolean isLocal(){
	    return src.getProtocol().equals(FILE_PROTOCOL);
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
	
	/*------------------------- VrectProgress Colors parameters ----------------------- */
	
	static Color DEFAULT_BG_COLOR = Color.LIGHT_GRAY;
	static Color DEFAULT_BAR_COLOR = Color.DARK_GRAY;
	static Color DEFAULT_PERCENT_FONT_COLOR = Color.BLACK;
	Color bgColor = DEFAULT_BG_COLOR;
	Color barColor = DEFAULT_BAR_COLOR;
	Color percentFontColor = DEFAULT_PERCENT_FONT_COLOR;
	
	public static void setBarProgressColor(Color bg, Color bar, Color font) {
		
		DEFAULT_BG_COLOR = bg;
		DEFAULT_BAR_COLOR = bar;
		DEFAULT_PERCENT_FONT_COLOR = font;
		
	}
	
	public void setBgColor(Color c) {
		bgColor = c;
	}
	
	public void setBarColor(Color c) {
		barColor = c;
	}
	
	public void setpercentFontColor(Color c) {
		percentFontColor = c;
	}
	
}
