/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import java.util.concurrent.atomic.AtomicLong;

public class ObjIdFactory {
	private static final AtomicLong currId = new AtomicLong(0);

	/**
	 * Disallow instanciation
	 */
	private ObjIdFactory(){}

	public static final ObjId next(){
		return new ObjId(currId.incrementAndGet());
	}
}

