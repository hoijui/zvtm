/*   FILE: FresnelLens.java
 *   DATE OF CREATION:  Tue Oct 24 11:51:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.gnb;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import org.w3c.IsaViz.fresnel.FSLPath;

import java.util.Vector;

class FresnelLens {

    /* the lens' URI */
    String uri;

    /* holds the lens's name/label/caption/whatever (used to denote the lens in the GUI) */
    String caption;

    /* domains expressed with various selector languages */
    String[] basicInstanceDomains = null;
    String[] basicClassDomains = null;
    FSLPath[] fslInstanceDomains = null;

    int apIndex = -1;

    /* properties to show (an items can be a property URI or an FSL path expr
       starting with and ending by an arc step) */
    PropertyVisibility[] p2s;
    
    /* properties to hide (an items can be a property URI or an FSL path expr
       starting with and ending by an arc step) */
    PropertyVisibility[] p2h;

    FresnelFormat[] associatedFormats;

    public FresnelLens(String uri, String baseURI){
	this.uri = uri;
	if (uri.startsWith(baseURI)){
	    caption = uri.substring(baseURI.length());
	}
	else {
	    caption = uri;
	}
	if (caption.startsWith("#")){caption = caption.substring(1);}
    }

    // expr is a String for basic selectors, an FSLPath for FSL selectors, and ? for SPARQL selectors
    void addInstanceDomain(Object expr, short selectorLanguage){
	if (selectorLanguage == FresnelManager._FSL_SELECTOR){// FSL selector
	    if (fslInstanceDomains == null){
		fslInstanceDomains = new FSLPath[1];
		fslInstanceDomains[0] = (FSLPath)expr;
	    }
	    else {
		FSLPath[] tmpA = new FSLPath[fslInstanceDomains.length+1];
		System.arraycopy(fslInstanceDomains, 0, tmpA, 0, fslInstanceDomains.length);
		tmpA[fslInstanceDomains.length] = (FSLPath)expr;
		fslInstanceDomains = tmpA;
	    }
	}
	else {// basic selector
	    if (basicInstanceDomains == null){
		basicInstanceDomains = new String[1];
		basicInstanceDomains[0] = (String)expr;
	    }
	    else {
		String[] tmpA = new String[basicInstanceDomains.length+1];
		System.arraycopy(basicInstanceDomains, 0, tmpA, 0, basicInstanceDomains.length);
		tmpA[basicInstanceDomains.length] = (String)expr;
		basicInstanceDomains = tmpA;
	    }
	}
    }

    void addClassDomain(String expr){
	// we only support basic selectors for class lens domains
	if (basicClassDomains == null){
	    basicClassDomains = new String[1];
	    basicClassDomains[0] = (String)expr;
	}
	else {
	    String[] tmpA = new String[basicClassDomains.length+1];
	    System.arraycopy(basicClassDomains, 0, tmpA, 0, basicClassDomains.length);
	    tmpA[basicClassDomains.length] = (String)expr;
	    basicClassDomains = tmpA;
	}
    }

    void setPropertiesVisibility(Vector ts, Vector th, int api){
	apIndex = api;
	p2s = new PropertyVisibility[ts.size()];
	Object o;
	for (int i=0;i<p2s.length;i++){
	    o = ts.elementAt(i);
	    if (o instanceof FSLPath){p2s[i] = new FSLVisibility((FSLPath)o);}
	    else {p2s[i] = new BasicVisibility((String)o);}
	}
	if (apIndex != -1){
	    p2h = new PropertyVisibility[th.size()];
	    for (int i=0;i<p2s.length;i++){
		o = ts.elementAt(i);
		if (o instanceof FSLPath){p2s[i] = new FSLVisibility((FSLPath)o);}
		else {p2s[i] = new BasicVisibility((String)o);}
	    }
	}
    }

    void addAssociatedFormats(FresnelFormat[] f){
	if (associatedFormats == null){
	    associatedFormats = f;
	}
	else {
	    FresnelFormat[] tmpA = new FresnelFormat[associatedFormats.length + f.length];
	    System.arraycopy(associatedFormats,0,tmpA,0,associatedFormats.length);
	    System.arraycopy(f,0,tmpA,associatedFormats.length,f.length);
	    associatedFormats = tmpA;
	}
    }

    FresnelFormat[] getAssociatedFormats(){
	return (associatedFormats != null) ? associatedFormats : new FresnelFormat[0];
    }


   void printVisibility(){
	System.out.println("VISIBILITY, allProperties at "+apIndex);
	if (p2s != null){
	    System.out.println("-------------------\nShow properties\n-------------------");
	    for (int i=0;i<p2s.length;i++){
		System.out.println(p2s[i]);
	    }
	}
	if (p2h != null){
	    System.out.println("-------------------\nHide properties\n-------------------");
	    for (int i=0;i<p2h.length;i++){
		System.out.println(p2h[i]);
	    }
	}
    }

    public String toString(){
	return caption;
    }
    
}
