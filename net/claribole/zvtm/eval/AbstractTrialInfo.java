/*   FILE: AbstractTrialInfo.java
 *   DATE OF CREATION:  Fri Apr 21 16:00:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: AbstractTrialInfo.java,v 1.4 2006/05/23 14:36:00 epietrig Exp $
 */ 

package net.claribole.zvtm.eval;

import java.awt.Color;

import java.util.Vector;

import com.xerox.VTM.glyphs.Glyph;
import com.xerox.VTM.glyphs.VRectangle;
import com.xerox.VTM.glyphs.ZRoundRect;
import com.xerox.VTM.engine.VirtualSpaceManager;
import com.xerox.VTM.engine.VirtualSpace;

class AbstractTrialInfo {
    
    int trial;
    int density;

    AbstractRegion root; // region at level 0

    AbstractTrialInfo(int t, int d, Vector lines){
	trial = t;
	density = d;
	String line = null;
	Vector distractors = new Vector();
	ZRoundRect target = null;
	VRectangle distractor = null;
	String[] info;
	long vx, vy, vw, vh;
	AbstractRegion lastRegion = null;
	for (int i=lines.size()-1;i>=0;i--){
	    line = (String)lines.elementAt(i);
	    if (line.startsWith("#")){
		lastRegion = buildLevel(Integer.parseInt(line.substring(8)), target, distractors, lastRegion);  // 8 = card("# Level ")
		distractors.clear();
	    }
	    else {
		info = line.split(AbstractWorldGenerator.CSV_SEP);
		vx = Long.parseLong(info[0]);
		vy = Long.parseLong(info[1]);
		vw = Long.parseLong(info[2]);
		vh = Long.parseLong(info[3]);
		if (info.length == 6){
		    int rcw = Integer.parseInt(info[4]);
		    int rch = Integer.parseInt(info[5]);
		    target = new ZRoundRect(vx, vy, 0, vw, vh, Color.WHITE, rcw, rch);
		    target.setType(ZLAbstractTask.GLYPH_TYPE_WORLD);
		}
		else {// info.length == 4
		    distractor = new VRectangle(vx, vy, 0, vw, vh, Color.WHITE);
		    distractor.setType(ZLAbstractTask.GLYPH_TYPE_WORLD);
		    distractors.add(distractor);
		}
	    }
	}
	root = lastRegion;
	root.setBounds(new ZRoundRect(root.target.vx, root.target.vy, 0,
				      root.target.getWidth()*10, root.target.getHeight()*10,
				      Color.RED, 1, 1));
    }

    AbstractRegion buildLevel(int depth, ZRoundRect target, Vector distractors, AbstractRegion cr){
	if (depth == 0){
	    if (!distractors.isEmpty()){
		System.err.println("Error building level "+depth+" for trial "+trial+": "+distractors.size()+" distractors instead of "+(density-1));
	    }
	}
	else {
	    if (distractors.size() + 1 != density){
		System.err.println("Error building level "+depth+" for trial "+trial+": "+distractors.size()+" distractors instead of "+(density-1));
	    }
	}
	AbstractRegion ar = new AbstractRegion(depth);
	ar.setTarget(target);
	VRectangle[] ds = new VRectangle[distractors.size()];
	for (int i=0;i<ds.length;i++){
	    ds[i] = (VRectangle)distractors.elementAt(i);
	    ds[i].setPaintBorder(false);
	    ds[i].setColor(AbstractWorldGenerator.COLOR_BY_LEVEL[depth]);
	}
	ar.setDistractors(ds);
	target.setColor(AbstractWorldGenerator.COLOR_BY_LEVEL[depth]);
	target.setPaintBorder(false);
	ar.setChildRegion(cr);
	return ar;
    }

    void addToVirtualSpace(VirtualSpaceManager vsm, VirtualSpace vs){
	root.addToVirtualSpace(vsm, vs, null);
    }

    void removeFromVirtualSpace(VirtualSpace vs){
	root.removeFromVirtualSpace(vs);
    }

    public String toString(){
	return trial+" (density= "+density+")";
    }

}