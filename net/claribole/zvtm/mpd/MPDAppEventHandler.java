/*   FILE: MPDAppEventHandler.java
 *   DATE OF CREATION:   Jul 28 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 * 
 * $Id: MPDAppEventHandler.java,v 1.3 2005/09/03 11:53:40 epietrig Exp $
 */

package net.claribole.zvtm.mpd;

import com.xerox.VTM.engine.ViewPanel;

import domino.Socket;

/**Extension of AppEventHandler with support for multiple pointing devices - can be used for bi-manual interaction. Works only with Linux for now (see bi-manual interaction mini-HOWTO).
 * @author Emmanuel Pietriga
 */

public interface MPDAppEventHandler {

    /**pointing device button 1 pressed callback
     *@param mod one of NO_MODIFIER, SHIFT_MOD, CTRL_MOD, CTRL_SHIFT_MOD, META_MOD, META_SHIFT_MOD, ALT_MOD, ALT_SHIFT_MOD (see com.xerox.VTM.engine.AppEventHandler)
     *@param pd pointing device socket
     */
    public void press1(ViewPanel v,int mod,int jpx,int jpy, Socket pd);
    /**pointing device button 1 released callback
     *@param mod one of NO_MODIFIER, SHIFT_MOD, CTRL_MOD, CTRL_SHIFT_MOD, META_MOD, META_SHIFT_MOD, ALT_MOD, ALT_SHIFT_MOD (see com.xerox.VTM.engine.AppEventHandler)
     *@param pd pointing device socket
     */
    public void release1(ViewPanel v,int mod,int jpx,int jpy, Socket pd);
    /**pointing device button 1 clicked callback
     *@param mod one of NO_MODIFIER, SHIFT_MOD, CTRL_MOD, CTRL_SHIFT_MOD, META_MOD, META_SHIFT_MOD, ALT_MOD, ALT_SHIFT_MOD (see com.xerox.VTM.engine.AppEventHandler)
     *@param pd pointing device socket
     */
    public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, Socket pd);
    /**pointing device button 2 pressed callback
     *@param mod one of ALT_MOD, ALT_SHIFT_MOD (see com.xerox.VTM.engine.AppEventHandler) (the middle pointing device button by itself is considered as being ALT-modified by default by Java)
     *@param pd pointing device socket
     */
    public void press2(ViewPanel v,int mod,int jpx,int jpy, Socket pd);
    /**pointing device button 2 released callback
     *@param mod one of ALT_MOD, ALT_SHIFT_MOD (see com.xerox.VTM.engine.AppEventHandler) (the middle pointing device button by itself is considered as being ALT-modified by default by Java)
     *@param pd pointing device socket
     */
    public void release2(ViewPanel v,int mod,int jpx,int jpy, Socket pd);
    /**pointing device button 2 clicked callback
     *@param mod one of ALT_MOD, ALT_SHIFT_MOD (see com.xerox.VTM.engine.AppEventHandler) (the middle pointing device button by itself is considered as being ALT-modified by default by Java)
     *@param pd pointing device socket
     */
    public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, Socket pd);
    /**pointing device button 3 pressed callback
     *@param mod one of META_MOD, META_SHIFT_MOD (the right pointing device button by itself is considered as being META-modified by default by Java)
     *@param pd pointing device socket
     */
    public void press3(ViewPanel v,int mod,int jpx,int jpy, Socket pd);
    /**pointing device button 3 released callback
     *@param mod one of META_MOD, META_SHIFT_MOD (the right pointing device button by itself is considered as being META-modified by default by Java)
     *@param pd pointing device socket
     */
    public void release3(ViewPanel v,int mod,int jpx,int jpy, Socket pd);
    /**pointing device button 3 clicked callback
     *@param mod one of META_MOD, META_SHIFT_MOD (the right pointing device button by itself is considered as being META-modified by default by Java)
     *@param pd pointing device socket
     */
    public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, Socket pd);

    /**mouse moved callback (this callback needs to be activated by View.setNotifyMouseMoved() ; it is inactive by default)
     *@param pd pointing device socket
     */
    public void mouseMoved(ViewPanel v,int jpx,int jpy, Socket pd);

    /**mouse dragged callback
     *@param buttonNumber one of 1 (left), 2 (middle) or 3 (right)
     *@param jpx mouse coord in JPanel coord syst
     *@param jpy mouse coord in JPanel coord syst
     *@param mod see pressX/releaseX/clickX with X=buttonNumber for possible values
     *@param pd pointing device socket
     */
    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, Socket pd);

    /**mouse wheel moved callback
     *@param wheelDirection is one of WHEEL_UP, WHEEL_DOWN
     *@param jpx mouse coord in JPanel coord syst
     *@param jpy mouse coord in JPanel coord syst
     *@param pd pointing device socket
     */
    public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, Socket pd);

}
