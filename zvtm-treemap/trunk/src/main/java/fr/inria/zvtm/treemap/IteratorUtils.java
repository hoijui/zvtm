package fr.inria.zvtm.treemap;

import java.util.List;
import java.util.ListIterator;

//Utilities needed to support various feketeries
class IteratorUtils {
    //let's not instantiate this
    private IteratorUtils(){}

    /**
     * Returns null if !it.hasNext()
     */
    public static <E> E peek(ListIterator<E> it){
        if(!it.hasNext()){
            return null;
        }
        E retval = it.next();
        it.previous();
        return retval;
    }

    public static <E> ListIterator<E> copy(ListIterator<E> srcIter, List<E> srcList){
        return srcList.listIterator(srcIter.nextIndex());
    }
}

