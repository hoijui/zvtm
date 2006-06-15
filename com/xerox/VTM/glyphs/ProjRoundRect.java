/*   FILE: ProjRoundRect.java
 *   DATE OF CREATION:   Wed May 28 14:27:38 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ProjRoundRect.java,v 1.3 2005/12/08 09:08:21 epietrig Exp $
 */ 

package com.xerox.VTM.glyphs;

/**project coordinates of a rectangle
 * @author Emmanuel Pietriga
 */

class ProjRoundRect extends RProjectedCoords {

    /**arc width and height in camera space*/
    int aw,ah;

    /**arc width and height in lens space*/
    int law, lah;

}
