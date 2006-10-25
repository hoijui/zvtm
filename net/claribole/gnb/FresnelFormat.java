/*   FILE: FresnelFormat.java
 *   DATE OF CREATION:  Mon Oct 23 16:22:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */

package net.claribole.gnb;

import org.w3c.IsaViz.fresnel.FSLPath;

public class FresnelFormat {

    static final short NOT_SPECIFIED = 0;
    static final short VALUE_NONE = 1;
    static final short VALUE_IMAGE = 2;
    static final short VALUE_URI = 3;
    static final short VALUE_EXTERNAL_LINK = 4;

    String uri;

    String[] basicPropertyDomains;
    FSLPath[] fslPropertyDomains;

    short value = NOT_SPECIFIED;

    public FresnelFormat(String uri){
	this.uri = uri;
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

//     boolean selectsByBPS(IProperty p){
// 	if (basicPropertyDomains != null){
// 	    for (int i=0;i<basicPropertyDomains.length;i++){
// 		if (p.getIdent().equals(basicPropertyDomains[i])){return true;}
// 	    }
// 	}
// 	return false;
//     }

//     boolean selectsByFPS(IProperty p){
// 	if (fslPropertyDomains != null){
// 	    for (int i=0;i<fslPropertyDomains.length;i++){
// 		//XXX: TBW if (){return true;}
// 	    }
// 	}
// 	return false;
//     }

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

    public String toString(){
	return uri;
    }

}