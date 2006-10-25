/*   FILE: FresnelGroup.java
 *   DATE OF CREATION:  Mon Oct 23 16:22:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */

package net.claribole.gnb;

public class FresnelGroup {

    String uri;

    /* holds the group's name/label/caption/whatever (used to denote the lens in the GUI) */
    String caption;

    FresnelLens[] lenses;
    FresnelFormat[] formats;

    public FresnelGroup(String uri, String baseURI){
	this.uri = uri;
	if (uri.startsWith(baseURI)){
	    caption = uri.substring(baseURI.length());
	}
	else {
	    caption = uri;
	}
	if (caption.startsWith("#")){caption = caption.substring(1);}
    }

    void addLens(FresnelLens l){
	if (lenses == null){
	    lenses = new FresnelLens[1];
	    lenses[0] = (FresnelLens)l;
	    }
	else {
	    FresnelLens[] tmpA = new FresnelLens[lenses.length+1];
	    System.arraycopy(lenses, 0, tmpA, 0, lenses.length);
	    tmpA[lenses.length] = (FresnelLens)l;
	    lenses = tmpA;
	}
    }

    void addFormat(FresnelFormat f){
	if (formats == null){
	    formats = new FresnelFormat[1];
	    formats[0] = (FresnelFormat)f;
	    }
	else {
	    FresnelFormat[] tmpA = new FresnelFormat[formats.length+1];
	    System.arraycopy(formats, 0, tmpA, 0, formats.length);
	    tmpA[formats.length] = (FresnelFormat)f;
	    formats = tmpA;
	}	
    }

    public String toString(){
	return caption;
    }

    /* debugging */
    
    void printItems(){
	System.out.println("---------------------\nGROUP " + uri);
	if (lenses != null){
	    System.out.println("LENSES");
	    for (int i=0;i<lenses.length;i++){
		System.out.println(lenses[i]);
	    }
	}
	if (lenses != null){
	    System.out.println("FORMATS");
	    for (int i=0;i<formats.length;i++){
		System.out.println(formats[i]);
	    }
	}	
    }

}