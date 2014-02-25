/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

/** Show progress loading scene.
 *@author Emmanuel Pietriga
 */

public interface ProgressListener {

    /** A message to be displayed as a caption to the progress indicator. */
    public void setLabel(String s);

    /** A percentage value indicating overall progress. */
    public void setValue(int i);

}
