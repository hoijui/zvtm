/*
 * AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2011.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine;

import java.util.Vector;

import fr.inria.zvtm.engine.PanelType;

public class AgilePanelType implements PanelType {
	
	public static final String AGILE_VIEW = "a2d";
    
    public ViewPanel getNewInstance(Vector<Camera> cameras, View v, boolean arfome){
        return new AgileViewPanel(cameras, v, arfome);
    }
    
}
