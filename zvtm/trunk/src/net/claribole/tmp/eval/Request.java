/*   FILE: Request.java
 *   DATE OF CREATION:  Thu Mar 10 10:55:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import com.xerox.VTM.glyphs.VImage;

public class Request {

    static final short TYPE_LOAD = 0;
    static final short TYPE_UNLOAD = 1;

    Integer ID;

    short type;
    int mapIndex;
    String mapID;
    /* images at a given level */
    VImage[] imagesAtLevel;
    /* image status (loaded or not) for images at a given level */
    boolean[] requestsStatusAtLevel;
    /* load or unload request status for images at a given level */
    Request[] requestsAtLevel;

    public Request(Integer id, short t, int mi, String md, VImage[] il, boolean[] rs, Request[] r){
	this.ID = id;
	type = t;
	mapIndex = mi;
	mapID = md;
	imagesAtLevel = il;
	requestsStatusAtLevel = rs;
	requestsAtLevel = r;
    }

}