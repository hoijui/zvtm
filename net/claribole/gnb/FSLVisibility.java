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
import org.w3c.IsaViz.fresnel.FSLJenaEvaluator;

import java.util.Vector;

public class FSLVisibility extends PropertyVisibility {
    
    static final String FSL = "FSL   ";
    
    FSLPath constraint;
    FSLJenaEvaluator fslEvaluator;

    FSLVisibility(FSLPath pathToProperty){
	constraint = pathToProperty;
    }

    void getPropertiesToShow(Resource r, Vector propertiesShown){
	Vector v = fslEvaluator.evaluatePathExpr(constraint, r);
	for (int i=0;i<v.size();i++){
	    propertiesShown.add(((Vector)v.elementAt(i)).lastElement()); // take arc corresponding to last step
	}// because in expressions such as fresnel:showProperties "foaf:knows/foaf:Person/foaf:surname"
	//  we are interested in foaf:surname, not foaf:knows
    }
    
    void setFSLEvaluator(FSLJenaEvaluator fje){
	this.fslEvaluator = fje;
    }

    public String toString(){
	return FSL + constraint.toString();
    }

}