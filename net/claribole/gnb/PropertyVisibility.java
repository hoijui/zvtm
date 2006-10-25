/*   FILE: PropertyVisibility.java
 *   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2006.
 *  Please first read the full copyright statement in file copyright.html
 *
 * $Id: PropertyVisibility.java,v 1.4 2006/05/16 06:15:14 epietrig Exp $
 */ 

package net.claribole.gnb;

import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class PropertyVisibility {

    abstract void getPropertiesToShow(Resource r, Vector propertiesShown);
    
}