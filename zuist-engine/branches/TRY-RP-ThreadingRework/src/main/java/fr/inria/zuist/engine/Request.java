/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
package fr.inria.zuist.engine;

import fr.inria.zvtm.engine.VirtualSpace;

/** Load/unload requests.
 * Requests for loading/unloading objects described by ObjectDescription instances are queued
 * and processed in a dedicated thread (GlyphLoader).
 *@author Emmanuel Pietriga
 *@see GlyphLoader
 */

class Request implements Runnable {

    final VirtualSpace target;
    final ObjectDescription od;

    static final short TYPE_LOAD = 0;
    static final short TYPE_UNLOAD = 1;
    final short type;

    final boolean transition;

    Request(VirtualSpace target, short type, ObjectDescription od, boolean transition){
	this.target = target;
	this.type = type;
	this.od = od;
	this.transition = transition;
    }

    public String toString(){
	return ((type == TYPE_LOAD) ? "LOAD " : "UNLOAD ") + od.toString();
    }

    public void run(){
	if(type == TYPE_LOAD){
	    od.createObject(target, transition);
	} else {
	    od.destroyObject(target, transition);
	}
    }

}
