/*   FILE: GCurveCtrl.java
 *   DATE OF CREATION:   Oct 05 2001
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Tue Mar 26 09:46:54 2002 by Emmanuel Pietriga
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 */

package com.xerox.VTM.engine;

import java.util.Date;

/**
 * Curve control point animation
 * @author Emmanuel Pietriga
 */

abstract class GCurveCtrl extends GAnimation {

    /** step values for (r,theta) polar coords */
    PolarCoords[] steps;


    void start(){
	now=new Date();
	startTime=now.getTime();
	started=true;
    }
    
}
