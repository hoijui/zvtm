/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ProgressListener.java 5512 2015-04-27 13:47:01Z epietrig $
 */

package fr.inria.zuist.event;

/** Show progress loading scene.
 *@author Emmanuel Pietriga
 */

public interface ProgressListener {

    /** A message to be displayed as a caption to the progress indicator. */
    public void setLabel(String s);

    /** A percentage value indicating overall progress. */
    public void setValue(int i);

}
