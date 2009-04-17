/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zvtm.lens;

public interface ParameterableFocusOffset  {

    public void setXfocusOffset(int x);

    public void setYfocusOffset(int y);
    
    public int getXfocusOffset();
    
    public int getYfocusOffset();

}
