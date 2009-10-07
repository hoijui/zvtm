/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ImageDescription.java,v 1.9 2007/10/04 13:59:00 pietriga Exp $
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

	public static final String RESOURCE_TYPE_IMG_STR = "img";
	public static final String RESOURCE_TYPE_PDF_STR = "pdf";

	/** Resource of unknown type. */
	public static final short RESOURCE_TYPE_UNKNOWN = -1;
	/** Resource of type image. */
	public static final short RESOURCE_TYPE_IMG = 0;
	/** Resource of type PDF document. */
	public static final short RESOURCE_TYPE_PDF = 1;
	short type = RESOURCE_TYPE_UNKNOWN;
	
	static final String URL_PROTOCOL_SEQ = ":/";
	
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
	 *@return one of RESOURCE_TYPE_{IMAGE,PDF,UNKNOWN}
	 */
	public short getType(){
		return type;
	}

	/** Set URL of this resource. */
	public void setURL(URL url){
		this.src = url;
		System.out.println("url");
	}
	
	/** Set URL of this resource. */
	public void setURL(String path){
		if (path.indexOf(URL_PROTOCOL_SEQ) != -1){
			//patch fixed
			path = "http"+path.substring(path.indexOf(URL_PROTOCOL_SEQ));
			try {
				this.src  = new URL(path);
			}
			catch(MalformedURLException ex){System.err.println("Error: malformed resource URL: "+path);}
		}
		else {
			// probably a relative file URL
			try {
				this.src  = (new File(path)).toURI().toURL();	
			}
			catch(MalformedURLException ex){System.err.println("Error: malformed resource URL: "+path);}			
		}
		
	}

	/** Get the URI of this resource. */
	public URL getURL(){
		return src;
	}
	
}
