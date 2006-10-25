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

    FresnelLens[] lenses;
    FresnelFormat[] formats;

    public FresnelGroup(String uri){
	this.uri = uri;
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
	return uri;
    }

    /* debugging */
    
//     void printItems(){
// 	System.out.println("---------------------\nGROUP " + uri);
// 	if (lenses != null){
// 	    System.out.println("LENSES");
// 	    for (int i=0;i<lenses.length;i++){
// 		System.out.println(lenses[i]);
// 	    }
// 	}
// 	if (lenses != null){
// 	    System.out.println("FORMATS");
// 	    for (int i=0;i<lenses.length;i++){
// 		System.out.println(lenses[i]);
// 	    }
// 	}	
//     }

}