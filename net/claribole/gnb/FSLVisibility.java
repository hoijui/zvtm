/*   FILE: FSLVisibility.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLVisibility.java,v 1.3 2006/05/16 06:15:14 epietrig Exp $
 */ 

package net.claribole.gnb;

// import org.w3c.IsaViz.IResource;
import org.w3c.IsaViz.fresnel.FSLPath;

import java.util.Vector;

public class FSLVisibility extends PropertyVisibility {
    
    static final String FSL = "FSL   ";
    
    FSLPath constraint;

    FSLVisibility(FSLPath pathToProperty){
	constraint = pathToProperty;
    }

//     void getPropertiesToShow(IResource r, Vector propertiesShown, Vector incomingPredicates, Vector outgoingPredicates){

//     }
    
    public String toString(){
	return FSL + constraint.toString();
    }

}