/*   FILE: PortalEventHandler.java
 *   DATE OF CREATION:  Sat Jun 17 10:22:59 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package fr.inria.zvtm.engine.portals;

/**Interface to handle events happening in a Portal.
 * @author Emmanuel Pietriga
 */

public interface PortalEventHandler {
    
    /** Cursor enters portal. */
    public void enterPortal(Portal p);

    /** Cursor exits portal. */
    public void exitPortal(Portal p);

}
