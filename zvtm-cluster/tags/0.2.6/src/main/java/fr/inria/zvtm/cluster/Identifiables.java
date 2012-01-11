/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience methods related to Identifiable objects.
 */
public class Identifiables {
    private Identifiables(){}

    /**
     * Returns a list of ObjId references, given a list of
     * Identifiable objects.
     */
    public static <T extends Identifiable> ArrayList<ObjId<T>> getRefList(List<T> objects){
        ArrayList<ObjId<T>> retval = new ArrayList<ObjId<T>>();
        for(Identifiable obj: objects){
            retval.add(obj.getObjId());
        }
        return retval;
    } 
}

