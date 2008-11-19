/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package net.claribole.eval.alphalens;

import com.xerox.VTM.engine.Camera;

class LabelSequence {

	static final String SEP_1 = ";";
	static final String SEP_2 = ",";
	
	static final String OPACITY_OPAQUE_STR = "O";
	static final String OPACITY_TRANSLUCENT_STR = "T";
	static final short OPACITY_OPAQUE = 0;
	static final short OPACITY_TRANSLUCENT = 1;

	short OPACITY;
	String opacityStr;
	short RANK;
	short WORD_LENGTH;
	String[] LABELS;

	LabelSequence(String line){
		String[] split = line.split(SEP_1);
		opacityStr = split[0];
		OPACITY = parseOpacity(opacityStr);
		RANK = Short.parseShort(split[1]);
		WORD_LENGTH = Short.parseShort(split[2]);
		LABELS = split[3].split(SEP_2);
	}
	
	String getTargetWord(){
		return LABELS[RANK-1];
	}
	
	float getOpacity(){
		return (OPACITY == OPACITY_TRANSLUCENT) ? EvalAcqLabel.FURTIVE_TARGET : EvalAcqLabel.OBVIOUS_TARGET;
	}
	
	String getOpacityStr(){
		return (OPACITY == OPACITY_TRANSLUCENT) ? OPACITY_TRANSLUCENT_STR : OPACITY_OPAQUE_STR;
	}
	
	static short parseOpacity(String o){
		if (o.equals(OPACITY_TRANSLUCENT_STR)){
			return OPACITY_TRANSLUCENT;
		}
		else if (o.equals(OPACITY_OPAQUE_STR)){
			return OPACITY_OPAQUE;
		}
		else {
			System.out.println("Opacity parsing error");
			return -1;
		}
	}

	public String toString(){
		return opacityStr + " " + RANK + " " + WORD_LENGTH + " " + getTargetWord();
	}

}
