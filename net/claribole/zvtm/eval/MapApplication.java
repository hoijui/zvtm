/*   FILE: ZLWorldDemo.java
 *   DATE OF CREATION:  Tue Nov 22 09:36:06 2005
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: MapApplication.java,v 1.1 2006/05/13 07:45:23 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import com.xerox.VTM.engine.VirtualSpace;

import javax.swing.text.Style;

public interface MapApplication {

    public void writeOnConsole(String s);
    public void writeOnConsole(String s, Style st);
    
}