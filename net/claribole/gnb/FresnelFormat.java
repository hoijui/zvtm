/*   FILE: FresnelFormat.java
 *   DATE OF CREATION:  Mon Oct 23 16:22:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */

package net.claribole.gnb;

import java.util.Vector;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import org.w3c.IsaViz.fresnel.FSLPath;
import org.w3c.IsaViz.fresnel.FSLJenaEvaluator;

public class FresnelFormat {

    static final short NOT_SPECIFIED = 0;
    static final short VALUE_NONE = 1;
    static final short VALUE_IMAGE = 2;
    static final short VALUE_URI = 3;
    static final short VALUE_EXTERNAL_LINK = 4;

    String uri;

    /* holds the format's name/label/caption/whatever (used to denote the lens in the GUI) */
    String caption;

    String[] basicPropertyDomains;
    FSLPath[] fslPropertyDomains;

    short value = NOT_SPECIFIED;

    /* label of the property (prepended to the property value) */
    String label = null;


    /* strings to preppend/append to values (null if none) */
    String contentFirstV, contentBeforeV, contentAfterV, contentLastV;

    public FresnelFormat(String uri, String baseURI){
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
    void addPropertyDomain(Object expr, short selectorLanguage){
	if (selectorLanguage == FresnelManager._FSL_SELECTOR){// FSL selector
	    if (fslPropertyDomains == null){
		fslPropertyDomains = new FSLPath[1];
		fslPropertyDomains[0] = (FSLPath)expr;
	    }
	    else {
		FSLPath[] tmpA = new FSLPath[fslPropertyDomains.length+1];
		System.arraycopy(fslPropertyDomains, 0, tmpA, 0, fslPropertyDomains.length);
		tmpA[fslPropertyDomains.length] = (FSLPath)expr;
		fslPropertyDomains = tmpA;
	    }
	}
	else {// basic selector
	    if (basicPropertyDomains == null){
		basicPropertyDomains = new String[1];
		basicPropertyDomains[0] = (String)expr;
	    }
	    else {
		String[] tmpA = new String[basicPropertyDomains.length+1];
		System.arraycopy(basicPropertyDomains, 0, tmpA, 0, basicPropertyDomains.length);
		tmpA[basicPropertyDomains.length] = (String)expr;
		basicPropertyDomains = tmpA;
	    }
	}
    }

    boolean selectsByBPS(Property p){
	if (basicPropertyDomains != null){
	    for (int i=0;i<basicPropertyDomains.length;i++){
		if (p.getURI().equals(basicPropertyDomains[i])){return true;}
	    }
	}
	return false;
    }

    boolean selectsByFPS(Statement s, FSLJenaEvaluator fje){
	if (fslPropertyDomains != null){
	    Vector startSet = new Vector();
	    startSet.add(s);
	    for (int i=0;i<fslPropertyDomains.length;i++){
		if (fje.evaluatePath(fslPropertyDomains[i], startSet).size() > 0){return true;}
	    }
	}
	return false;
    }

    /* how the value will be handled for display */
    void setValue(String expr){
	if (expr.equals(FresnelManager._image)){
	    value = VALUE_IMAGE;
	}
	else if (expr.equals(FresnelManager._none)){
	    value  = VALUE_NONE;
	}
	else if (expr.equals(FresnelManager._uri)){
	    value = VALUE_URI;
	}
	else if (expr.equals(FresnelManager._externalLink)){
	    value = VALUE_EXTERNAL_LINK;
	}
	else {
	    value = NOT_SPECIFIED;
	}
    }
    
    /* what label will be used to describe the property (null if none) */
    void setLabel(RDFNode n){
	if (n instanceof Literal){
	    String l = ((Literal)n).getLexicalForm();
	    label = (l.length() > 0) ? l : null;
	}
	else {
	    String uri = ((Resource)n).toString();
	    if (uri.equals(FresnelManager._none)){label = null;}
	    else if (uri.equals(FresnelManager._show)){/*XXX: should get the property's RDFS label*/}
	}
    }

    /* process instructions such as contentAfter, contentLast, etc. */
    void addValueFormattingInstruction(Resource r){
	StmtIterator si = r.listProperties();
	Statement s;
	String pred;
	while (si.hasNext()){
	    s = si.nextStatement();
	    pred = s.getPredicate().getURI();
	    if (pred.equals(FresnelManager.FRESNEL_NAMESPACE_URI+FresnelManager._contentAfter)){
		contentAfterV = s.getLiteral().getLexicalForm();
	    }
	    else if (pred.equals(FresnelManager.FRESNEL_NAMESPACE_URI+FresnelManager._contentBefore)){
		contentBeforeV = s.getLiteral().getLexicalForm();
	    }
	    else if (pred.equals(FresnelManager.FRESNEL_NAMESPACE_URI+FresnelManager._contentLast)){
		contentLastV = s.getLiteral().getLexicalForm();
	    }
	    else if (pred.equals(FresnelManager.FRESNEL_NAMESPACE_URI+FresnelManager._contentFirst)){
		contentFirstV = s.getLiteral().getLexicalForm();
	    }
	}
    }

    boolean hasValueFormattingInstructions(){
	return (contentFirstV != null || contentBeforeV != null || contentAfterV != null || contentLastV != null);
    }

    String format(Statement s, boolean firstItem, boolean lastItem){
	String res = (!firstItem && contentBeforeV != null) ? contentBeforeV : "";
	res += (s.getObject() instanceof Literal) ? s.getLiteral().getLexicalForm() : s.getResource().toString();
	if (!lastItem && contentAfterV != null){res += contentAfterV;}
	return res;
    }

    static String defaultFormat(Statement s){
	return (s.getObject() instanceof Literal) ? s.getLiteral().getLexicalForm() : s.getResource().toString();
    }

    public String toString(){
	return caption;
    }

}