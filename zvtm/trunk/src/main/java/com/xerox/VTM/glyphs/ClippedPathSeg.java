/*   FILE: ClippedPathSeg.java
 *   DATE OF CREATION:   Wed Feb 05 12:47:14 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Wed Feb 05 13:45:46 2003 by Emmanuel Pietriga
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package com.xerox.VTM.glyphs;

  /**
   * Used to decompose a VClippedPath in segments.
   * @author Emmanuel Pietriga
   **/

public class ClippedPathSeg extends PathSeg {

    public static short SEG_TYPE_SEG=0;
    public static short SEG_TYPE_QD1=1;
    public static short SEG_TYPE_QD2=2;
    public static short SEG_TYPE_CB1=3;
    public static short SEG_TYPE_CB2=4;
    public static short SEG_TYPE_CB3=5;
    public static short SEG_TYPE_JMP=6;

    protected boolean wasVisible=false;
    protected boolean visible=false;

    protected short type;

    protected float java2Dx,java2Dy;

    protected ClippedPathSeg(long xc,long yc,long wc,long hc,short segType,float x2d,float y2d){
	super(xc,yc,wc,hc);
	java2Dx=x2d;
	java2Dy=y2d;
	type=segType;
    }

    protected void setVisible(boolean b){
	wasVisible=visible;
	visible=b;
    }

}
