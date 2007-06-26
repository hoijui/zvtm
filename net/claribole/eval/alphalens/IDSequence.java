
package net.claribole.eval.alphalens;

class IDSequence {

    int[] MMs;
    double[] IDs;
    long[] Ws;
    
    IDSequence(){
	this.MMs = new int[0];
    }

    int length(){
	return MMs.length;
    }

    void addSequence(String[] idseq){
	int[] tmpSeq = new int[MMs.length + idseq.length];
	System.arraycopy(MMs, 0, tmpSeq, 0, MMs.length);
	for (int i=0;i<idseq.length;i++){
	    tmpSeq[i+MMs.length] = Integer.parseInt(idseq[i]);
	}
	MMs = tmpSeq;
    }

    void computeWsAndIDs(){
	Ws = new long[MMs.length];
	IDs = new double[MMs.length];
	for (int i=0;i<Ws.length;i++){
	    if (MMs[i] == 6){
		Ws[i] = EvalFitts.W2_6;
		IDs[i] = Math.log(EvalFitts.D/((double)(Math.abs(EvalFitts.W1_6-EvalFitts.W2_6))) + 1) / Math.log(2);
	    }
	    else if (MMs[i] == 10){
		Ws[i] = EvalFitts.W2_10;
		IDs[i] = Math.log(EvalFitts.D/((double)(Math.abs(EvalFitts.W1_10-EvalFitts.W2_10))) + 1) / Math.log(2);
	    }
	    else if (MMs[i] == 14){
		Ws[i] = EvalFitts.W2_14;
		IDs[i] = Math.log(EvalFitts.D/((double)(Math.abs(EvalFitts.W1_14-EvalFitts.W2_14))) + 1) / Math.log(2);
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
