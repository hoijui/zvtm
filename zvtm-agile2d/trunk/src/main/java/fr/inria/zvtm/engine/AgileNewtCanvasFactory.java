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

/**
 * Factory for NewtCanvas
 * Uses OpenGL acceletation provided by the Agile2D rendering pipeline (itself based upon JOGL 2.0).<br>
 * <a href="http://agile2d.sourceforge.net/">Agile2D homepage</a><br>
 * <a href="http://download.java.net/media/jogl/jogl-2.x-docs/">JOGL 2 javadoc</a><br>
 * <a href="http://jogamp.org/deployment/jogamp-next/javadoc/jogl/javadoc">JogAmp javadoc</a><br>
 * <a href="http://jogamp.org/jogl/doc/NEWT-Overview.html">NEWT</a><br>
 * Before instantiating an Agile2D ZVTM View, one must register the new view type:<br>
 * View.registerViewPanelType(AgileGLJPanelFactory.AGILE_NEWT_VIEW, new AgileNewtCanvasFactory());<br><br>
 * Then the view gets created as any other view:<br>
 * View v = VirtualSpaceManager.INSTANCE.addFrameView(cameras, View.ANONYMOUS, AgileNewtCanvasFactory.AGILE_NEWT_VIEW, 800, 600, true);
 * @author Emmanuel Pietriga, Rodrigo A. B. de Almeida
 */
 
public class AgileNewtCanvasFactory implements PanelFactory {
	
	public static final String AGILE_NEWT_VIEW = "a2dn";
    
    public ViewPanel getNewInstance(Vector<Camera> cameras, View v, boolean arfome){
        return new AgileNewtViewPanel(cameras, v, arfome);
    }
    
}
