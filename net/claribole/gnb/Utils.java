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

    public static String delLeadingAndTrailingSpaces(String s){
	StringBuffer sb=new StringBuffer(s);
	Utils.delLeadingSpaces(sb);
	Utils.delTrailingSpaces(sb);
	return sb.toString();
    }

    static void delLeadingAndTrailingSpaces(StringBuffer sb){
	Utils.delLeadingSpaces(sb);
	Utils.delTrailingSpaces(sb);
    }

    static String delLeadingSpaces(String s){
	StringBuffer sb=new StringBuffer(s);
	Utils.delLeadingSpaces(sb);
	return sb.toString();
    }

    static String delTrailingSpaces(String s){
	StringBuffer sb=new StringBuffer(s);
	Utils.delTrailingSpaces(sb);
	return sb.toString();
    }

    /**
     *@param sb a StringBuffer from which leading whitespaces should be removed
     */
    static void delLeadingSpaces(StringBuffer sb){
	while ((sb.length()>0) && (Character.isWhitespace(sb.charAt(0)))){
	    sb.deleteCharAt(0);
	}
    }

    /**
     *@param sb a StringBuffer from which trailing whitespaces should be removed
     */
    static void delTrailingSpaces(StringBuffer sb){
	while ((sb.length()>0) && (Character.isWhitespace(sb.charAt(sb.length()-1)))){
	    sb.deleteCharAt(sb.length()-1);
	}
    }

    public static boolean isWhiteSpaceCharsOnly(String s){
	for (int i=0;i<s.length();i++){
	    if (!Character.isWhitespace(s.charAt(i))){return false;}
	}
	return true;
    }


}
