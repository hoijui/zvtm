/*   FILE: FSLVisibility.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: FSLVisibility.java,v 1.3 2006/05/16 06:15:14 epietrig Exp $
 */ 

package net.claribole.gnb;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import org.w3c.IsaViz.fresnel.FSLPath;

import java.util.Vector;

public class FSLVisibility extends PropertyVisibility {
    
    static final String FSL = "FSL   ";
    
    FSLPath constraint;

    FSLVisibility(FSLPath pathToProperty){
	constraint = pathToProperty;
    }

    void getPropertiesToShow(Resource r, Vector propertiesShown){
	
    }
    
    public String toString(){
	return FSL + constraint.toString();
    }

}