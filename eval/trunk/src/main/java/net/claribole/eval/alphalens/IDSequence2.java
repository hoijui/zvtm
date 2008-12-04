/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.eval.alphalens;

import com.xerox.VTM.engine.Camera;

class IDSequence2 {

    int[] MMs;
    double[] IDs;
    long[] Ws;
    float[] TAs;
    
    IDSequence2(){
	this.MMs = new int[0];
	this.TAs = new float[0];
    }

    int length(){
	return MMs.length;
    }

    void addSequence(String[] idseq){
	int[] tmpSeq = new int[MMs.length + idseq.length];
	float[] tmpSeqA = new float[TAs.length + idseq.length];
	System.arraycopy(MMs, 0, tmpSeq, 0, MMs.length);
	System.arraycopy(TAs, 0, tmpSeqA, 0, TAs.length);
	for (int i=0;i<idseq.length;i++){
 	    tmpSeq[i+MMs.length] = Integer.parseInt(idseq[i].substring(1));
	    tmpSeqA[i+TAs.length] = (idseq[i].charAt(0) == 'o') ? EvalAcq.OBVIOUS_TARGET : EvalAcq.FURTIVE_TARGET; // o (obvious) or f (furtive)
	}
	MMs = tmpSeq;
	TAs = tmpSeqA;
    }

    void computeWsAndIDs(){
	Ws = new long[MMs.length];
	IDs = new double[MMs.length];
	for (int i=0;i<Ws.length;i++){
	    if (MMs[i] == 2){
		Ws[i] = EvalFitts.W2_2;
		IDs[i] = Math.log(EvalFitts.D * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL/((double)(Math.abs(EvalFitts.W1_2-EvalFitts.W2_2))) + 1) / Math.log(2);
	    }
	    else if (MMs[i] == 4){
		Ws[i] = EvalFitts.W2_4;
		IDs[i] = Math.log(EvalFitts.D * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL/((double)(Math.abs(EvalFitts.W1_4-EvalFitts.W2_10))) + 1) / Math.log(2);
	    }
	    else if (MMs[i] == 6){
		Ws[i] = EvalFitts.W2_6;
		IDs[i] = Math.log(EvalFitts.D * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL/((double)(Math.abs(EvalFitts.W1_6-EvalFitts.W2_6))) + 1) / Math.log(2);
	    }
	    else if (MMs[i] == 10){
		Ws[i] = EvalFitts.W2_10;
		IDs[i] = Math.log(EvalFitts.D * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL/((double)(Math.abs(EvalFitts.W1_10-EvalFitts.W2_10))) + 1) / Math.log(2);
	    }
	    else if (MMs[i] == 14){
		Ws[i] = EvalFitts.W2_14;
		IDs[i] = Math.log(EvalFitts.D * (Camera.DEFAULT_FOCAL+EvalFitts.CAM_ALT)/Camera.DEFAULT_FOCAL/((double)(Math.abs(EvalFitts.W1_14-EvalFitts.W2_14))) + 1) / Math.log(2);
	    }
	    else if (MMs[i] == 8){
		Ws[i] = EvalAcq.W2_8;
		IDs[i] = Math.log(EvalAcq.D * (Camera.DEFAULT_FOCAL+EvalAcq.CAM_ALT)/Camera.DEFAULT_FOCAL/((double)(Math.abs(EvalAcq.W1_8-EvalAcq.W2_8))) + 1) / Math.log(2);
	    }
	    else if (MMs[i] == 12){
		Ws[i] = EvalAcq.W2_12;
		IDs[i] = Math.log(EvalAcq.D * (Camera.DEFAULT_FOCAL+EvalAcq.CAM_ALT)/Camera.DEFAULT_FOCAL/((double)(Math.abs(EvalAcq.W1_12-EvalAcq.W2_12))) + 1) / Math.log(2);
	    }
	    else {
		System.err.println("Error: MM value not supported: "+MMs[i]);
	    }
	}
    }

    public String toString(){
	String res = "" + MMs[0];
	for (int i=1;i<MMs.length;i++){
	    res += ", " + MMs[i];
	}
	res += "\n" + IDs[0];
	for (int i=1;i<IDs.length;i++){
	    res += ", " + IDs[i];
	}
	return res;
    }
    
}
