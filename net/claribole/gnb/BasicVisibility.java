/*   FILE: BasicVisibility.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: BasicVisibility.java,v 1.3 2006/05/16 06:15:14 epietrig Exp $
 */ 

package net.claribole.gnb;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import java.util.Vector;

public class BasicVisibility extends PropertyVisibility {

    static final String BASIC = "BASIC ";

    String constraint;

    BasicVisibility(String propertyURI){
	constraint = propertyURI;
    }

    void getPropertiesToShow(Resource r, Vector propertiesShown){
	StmtIterator si = r.listProperties();
	Statement s;
	while (si.hasNext()){
	    s = si.nextStatement();
	    if (s.getPredicate().toString().equals(constraint) && !propertiesShown.contains(s)){
		propertiesShown.add(s);
	    }
	}
	si.close();
	// incoming properties are not examined here as basic selectors only look at
	// outgoing properties (from the current node)
    }
 
    public String toString(){
	return BASIC + constraint;
    }
   
}