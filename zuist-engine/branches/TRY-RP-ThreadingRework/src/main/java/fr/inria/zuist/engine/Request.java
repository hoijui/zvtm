/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

/** Load/unload requests.
 * Requests for loading/unloading objects described by ObjectDescription instances are queued
 * and processed in a dedicated thread (GlyphLoader).
 *@author Emmanuel Pietriga
 *@see GlyphLoader
 */

class Request {

    final Integer ID;

    final ObjectDescription od;

    static final short TYPE_LOAD = 0;
    static final short TYPE_UNLOAD = 1;
    final short type;

    static final short NO_TRANSITION = 0;
    static final short TRANSITION_FADE = 1;
    final short transition;

    Request(Integer id, short type, ObjectDescription od, boolean transition){
	this.ID = id;
	this.type = type;
	this.od = od;
	this.transition = (transition) ? TRANSITION_FADE : NO_TRANSITION;
    }

    public String toString(){
	return ((type == TYPE_LOAD) ? "LOAD " : "UNLOAD ") + od.toString();
    }

}
