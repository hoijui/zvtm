/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

/**
 * Identifiable objects may be replicated: the master instance
 * has logical equivalents on slave instances.
 */
public interface Identifiable {
    /**
     * Returns the ObjId of this Identifiable.
     */
    public ObjId getObjId();
    public boolean isReplicated();
}

