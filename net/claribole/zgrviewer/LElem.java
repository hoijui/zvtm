/*   FILE: LNode.java
 *   DATE OF CREATION:  Thu Mar 15 19:18:17 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */ 

package net.claribole.zgrviewer;

import com.xerox.VTM.svg.Metadata;

class LElem {

    String title;
    String url;

    LElem(){}

    LElem(Metadata md){
	this.title = md.getTitle();
	this.url = md.getURL();
    }

    String getTitle(){
	return title;
    }

    String getURL(){
	return url;
    }

}
