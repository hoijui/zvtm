/*   FILE: RepaintListener.java
 *   DATE OF CREATION:   Thu Nov 30 08:30:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id$
 */

package fr.inria.zvtm.engine;

import fr.inria.zvtm.engine.View;

public interface RepaintListener {
    
    public void viewRepainted(View v);
    
}
