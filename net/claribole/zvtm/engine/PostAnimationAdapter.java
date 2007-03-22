/*   FILE: PostAnimationAdapter.java
 *   DATE OF CREATION:  Thu Mar 22 15:32:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: $
 */ 

package net.claribole.zvtm.engine;

/**
 * An abstract adapter class for receiving post animation action events.
 * The methods in this class are empty. This class exists as convenience for creating post animation action objects.
 * @author Emmanuel Pietriga
 */

public class PostAnimationAdapter implements PostAnimationAction {
    
    public PostAnimationAdapter(){}

    public void animationEnded(Object target, short type, String dimension){}

}
