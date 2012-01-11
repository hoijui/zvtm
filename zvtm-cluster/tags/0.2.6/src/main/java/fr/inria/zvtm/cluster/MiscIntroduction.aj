/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.Location;

import java.awt.BasicStroke;
import java.io.Serializable;

/**
 * Miscellaneous introductions
 */
public aspect MiscIntroduction {
	declare parents: Location implements java.io.Serializable;
    //declare parents: BasicStroke implements java.io.Serializable;
}

