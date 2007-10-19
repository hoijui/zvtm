/*   FILE: PostAnimationAction.java
 *   DATE OF CREATION:  Wed Dec 21 10:10:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.engine;

/**
 * Actions to perform when an animation ends
 * @author Emmanuel Pietriga
 **/

public interface PostAnimationAction {

    public static short GLYPH = 0;
    public static short CAMERA = 1;
    public static short PORTAL = 3;
    public static short LENS = 2;

    /**method called when animation ends (callback)
     *@param target object on which animation was performed
     *@param type object type, one of PostAnimationAction.{GLYPH,CAMERA,LENS}
     *@param dimension what dimension of the object was animated, one of AnimManager.{GL_TRANS, GL_SZ, GL_ROT, GL_COLOR, GL_CTRL, CA_TRANS, CA_ALT, CA_BOTH, LS_MM, LS_RD, LS_BOTH, PT_TRANS, PT_SZ, PT_BOTH} 
     */
    public void animationEnded(Object target, short type, String dimension);

}