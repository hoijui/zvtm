/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LevelListener.java 5512 2015-04-27 13:47:01Z epietrig $
 */

package fr.inria.zuist.event;

/** Listen to level-related camera events.
 *@author Emmanuel PIetriga
 */

public interface LevelListener {

    /**Fired when a camera declared in SceneManager enters a level.
       In other words, when the camera is in the corresponding altitude range.
     *@param depth depth/index of corresponding level.
     */
    public void enteredLevel(int depth);

    /**Fired when a camera declared in SceneManager leaves a level.
       In other words, when the camera is no longer in the corresponding altitude range.
     *@param depth depth/index of corresponding level.
     */
    public void exitedLevel(int depth);

}
