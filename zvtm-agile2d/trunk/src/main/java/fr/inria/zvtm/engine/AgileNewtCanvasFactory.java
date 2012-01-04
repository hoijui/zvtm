/*
 * AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2012.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine;

import java.util.Vector;

import fr.inria.zvtm.engine.PanelFactory;

public class AgileNewtCanvasFactory implements PanelFactory {
	
	public static final String AGILE_NEWT_VIEW = "a2dn";
    
    public ViewPanel getNewInstance(Vector<Camera> cameras, View v, boolean arfome){
        return new AgileNewtViewPanel(cameras, v, arfome);
    }
    
}
