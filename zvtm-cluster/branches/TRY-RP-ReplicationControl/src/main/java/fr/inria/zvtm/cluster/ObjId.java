/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.io.Serializable;

/**
 * An object identifier. 
 * A master program can request new ObjectId instances
 * through an ObjIdFactory. Slaves, however, should never
 * create them except through deserialization.
 *
 * ObjId instances may be tested for equality and validity.
 * If two ObjectId instances are 
 * equal, then the owning objects are considered identical
 * (even though they are not in the Java sense, as they are
 * mirrored on different machines).
 */
public class ObjId<T> implements Serializable {
	public static final ObjId INVALID_ID = new ObjId(-1);

	private final long id;
    
	ObjId(long id){
		this.id = id;
	}

	/** 
	 * Equality comparison. If two ObjectId instances are 
	 * equal, then the owning objects are considered identical
	 * (even though they are not in the Java sense, as they are
	 * mirrored on different machines). 
	 * @InheritDoc
	 */	
	@Override public boolean equals(Object other){
		if(null == other){
			return false;
		}

		if(!(other instanceof ObjId)){
			return false;
		}

		return (((ObjId)other).id == id);
	}

	/**
	 * @InheritDoc
	 */
	@Override public int hashCode(){
		return (int)(id % Integer.MAX_VALUE);
	}

	/**
	 * Tests this identifier for validity.
	 */
	public boolean isValid(){
		return !this.equals(INVALID_ID);
	}

	@Override public String toString(){
		return "ObjId@" + id;
	}
}
