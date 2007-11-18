/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zslideshow;

/** Load/unload requests.
 * Requests for loading/unloading objects described by ObjectDescription instances are queued
 * and processed in a dedicated thread (GlyphLoader).
 *@author Emmanuel Pietriga
 *@see GlyphLoader
 */

class Request {

    Integer ID;

    ImageDescription od;

    static final short TYPE_LOAD = 0;
    static final short TYPE_UNLOAD = 1;
    short type;

    static final short NO_TRANSITION = 0;
    static final short TRANSITION_FADE = 1;
    short transition;
    
    boolean showImmediately = false;

    Request(Integer id, short type, ImageDescription od, boolean transition, boolean si){
        this.ID = id;
        this.type = type;
        this.od = od;
        this.transition = (transition) ? TRANSITION_FADE : NO_TRANSITION;
        this.showImmediately = si;
    }

    public String toString(){
        return ((type == TYPE_LOAD) ? "LOAD " : "UNLOAD ") + od.toString();
    }

}
