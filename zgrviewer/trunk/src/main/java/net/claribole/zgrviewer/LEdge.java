/*   FILE: LEdge.java
 *   DATE OF CREATION:  Thu Mar 15 19:18:17 2007
 *   Copyright (c) INRIA, 2007-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zgrviewer;

import java.util.Vector;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VShape;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.svg.Metadata;

public class LEdge extends LElem {

    static final short UNDIRECTED = 0;
    static final short INCOMING = 1;
    static final short OUTGOING = 2;

    static final String UNDIRECTED_STR = "--";
    static final String DIRECTED_STR = "->";
    
    static final short GLYPH_SPLINE = 0;
    static final short GLYPH_LABEL = 1;
    static final short GLYPH_HEAD = 2;
    static final short GLYPH_TAIL = 3;

    short[] glyphCat;

    boolean directed = false;

    LNode tail;
    LNode head;

    LEdge(String title, Vector<Glyph> glyphs){
		this.title = title;
        this.glyphs = new Glyph[glyphs.size()];
        this.URLs = new String[glyphs.size()];
        this.tooltips = new String[glyphs.size()];
        for (int i=0;i<this.glyphs.length;i++){
            this.glyphs[i] = glyphs.elementAt(i);
            // URL associated with each glyph (there might be different URLs associated with
            // the various glyphs constituting a node or edge)
            if (this.glyphs[i].getOwner() != null){
                URLs[i] = ((Metadata)this.glyphs[i].getOwner()).getURL();
                tooltips[i] = ((Metadata)this.glyphs[i].getOwner()).getURLTitle();
            }
        }
		if (this.glyphs.length > 0){
	        this.groupID = ((Metadata)this.glyphs[0].getOwner()).getClosestAncestorGroupID();			
		}
		else {
		    this.groupID = Messages.EMPTY_STRING;
		}
		for (int i=0;i<this.glyphs.length;i++){
            this.glyphs[i].setOwner(this);
        }
        categorizeGlyphs();
    }

    LEdge(Vector<Glyph> glyphs){
		this.title = "";
        this.glyphs = new Glyph[glyphs.size()];
        this.URLs = new String[glyphs.size()];
        this.tooltips = new String[glyphs.size()];
		for (int i=0;i<this.glyphs.length;i++){
		    this.glyphs[i] = glyphs.elementAt(i);
            this.glyphs[i].setOwner(this);
            this.URLs[i] = "";
            this.tooltips[i] = "";
        }
        this.groupID = Messages.EMPTY_STRING;
        categorizeGlyphs();
    }
    
    void categorizeGlyphs(){
        glyphCat = new short[glyphs.length];
        for (int i=0;i<glyphs.length;i++){
            if (glyphs[i] instanceof DPath){
                // the spline itself
                glyphCat[i] = GLYPH_SPLINE;
            }
            else if (glyphs[i] instanceof VText){
                // probably a label
                glyphCat[i] = GLYPH_LABEL;
            }
            else if (glyphs[i] instanceof ClosedShape){
                if (true){//XXX: test TBW
                    // probably a head glyph
                    glyphCat[i] = GLYPH_HEAD;
                }
                else {
                    // probably a tail glyph
                    glyphCat[i] = GLYPH_TAIL;
                }
            }
        }
    }

    public String getURL(Glyph g){
        for (int i=0;i<glyphs.length;i++){
            if (g == glyphs[i]){
                return URLs[i];
            }
        }
        return null;
    }

    public String getTooltip(Glyph g){
        for (int i=0;i<glyphs.length;i++){
            if (g == glyphs[i]){
                return tooltips[i];
            }
        }
        return null;
    }

    void setDirected(boolean b){
	directed = b;
    }

    boolean isDirected(){
	return directed;
    }

	public boolean isLoop(){
		return tail == head;
	}

    void setTail(LNode n){
	tail = n;
	if (tail != null){
	    tail.addArc(this, (directed) ? LEdge.OUTGOING : LEdge.UNDIRECTED);
	}
    }

    void setHead(LNode n){
	head = n;
	if (head != null){
	    head.addArc(this, (directed) ? LEdge.INCOMING : LEdge.UNDIRECTED);
	}
    }

    public LNode getTail(){
		return tail;
    }

    public LNode getHead(){
		return head;
    }

    public LNode getOtherEnd(LNode n){
		return (n == tail) ? head : tail;
    }

    public DPath getSpline(){
		for (int i=0;i<glyphs.length;i++){
			if (glyphs[i] instanceof DPath){return (DPath)glyphs[i];}
		}
		return null;
	}
	
	/**
	 *@return null if none or could not be identified.
     *@see #getHeadGlyph()
	 */
	public ClosedShape getArrowHead(){
	    //XXX: FIXME: does not differentiate head and tail right now
	    // Will return the first thing it finds, no matter whether
	    // at tail or head
		for (int i=0;i<glyphs.length;i++){
		    if (glyphs[i] instanceof VShape){return (VShape)glyphs[i];}
			else if (glyphs[i] instanceof VPolygon){return (VPolygon)glyphs[i];}
		}
		return null;	    
	}

    /** NOT IMPLEMENTED YET.
	 *@return null if none or could not be identified.
     *@see #getArrowHead()
     *@see #getTailGlyph()
	 */
	public ClosedShape getHeadGlyph(){
	    return null;
	}
	
    /** NOT IMPLEMENTED YET.
	 *@return null if none or could not be identified.
     *@see #getHeadGlyph()
	 */
	public ClosedShape getTailGlyph(){
	    return null;
	}

	public boolean hasVShapeArrowHead(){
		for (int i=0;i<glyphs.length;i++){
		    if (glyphs[i] instanceof VShape){return true;}
        }
        return false;
    }
    
    public boolean hasTailAndHeadGlyphs(){
        int count = 0;
        for (int i=0;i<glyphs.length;i++){
		    if (glyphs[i] instanceof VShape){count++;}
			else if (glyphs[i] instanceof VPolygon){count++;}
		}
		return (count > 1);
    }
    
	/**
	 *@return the old polygon if replace was successful.
	 */	
	public ClosedShape replaceArrowHead(VShape s){
		for (int i=0;i<glyphs.length;i++){
		    if (glyphs[i] instanceof VPolygon || glyphs[i] instanceof VShape){
                ClosedShape old = (ClosedShape)glyphs[i];
                glyphs[i] = s;
		        return old;
		    }
		}
		return null;
	}

    public String toString(){
	return title + "@" + hashCode() + " [" + 
	    ((tail != null) ? tail.getTitle() + "@" + tail.hashCode() : "NULL")+
	    ((directed) ? LEdge.DIRECTED_STR : LEdge.UNDIRECTED_STR) +
	    ((head != null) ? head.getTitle() + "@" + head.hashCode() : "NULL") +
	    "]";
    }

}
