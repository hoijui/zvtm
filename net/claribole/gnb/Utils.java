/*   FILE: Utils.java
 *   DATE OF CREATION:  Mon Oct 24 09:31:06 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */ 

package net.claribole.gnb;

class Utils {

    static int getItemIndex(Object[] a, Object o){
	for (int i=0;i<a.length;i++){
	    if (a[i] == o){return i;}
	}
	return -1;
    }

}
