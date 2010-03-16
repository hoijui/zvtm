/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

/**
 * An Identifiable implementation that provides reasonable defaults.
 * Can be inherited by classes that should be replicated by ZVTM-cluster.
 */
public class DefaultIdentifiable implements Identifiable {
    private final ObjId objId = ObjIdFactory.next();
    private boolean replicated = false;

    public ObjId getObjId(){ return objId; }
    public boolean isReplicated() { return replicated; }
    public void setReplicated(boolean val) { this.replicated = val; }
}

