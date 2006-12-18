
package net.claribole.eval.alphalens;

class IDSequence {

    static final long D = 12000;

    int[] IDs;
    long[] Ws;
    
    IDSequence(){
	this.IDs = new int[0];
    }

    int length(){
	return IDs.length;
    }

    void addSequence(String[] idseq){
	int[] tmpSeq = new int[IDs.length + idseq.length];
	System.arraycopy(IDs, 0, tmpSeq, 0, IDs.length);
	for (int i=0;i<idseq.length;i++){
	    tmpSeq[i+IDs.length] = Integer.parseInt(idseq[i]);
	}
	IDs = tmpSeq;
    }

    void computeWs(){
	Ws = new long[IDs.length];
	for (int i=0;i<Ws.length;i++){
	    if (IDs[i] == 4){
		Ws[i] = D/16;
	    }
	    else if (IDs[i] == 6){
		Ws[i] = D/64;
	    }
	    else if (IDs[i] == 8){
		Ws[i] = D/256;
	    }
	    else if (IDs[i] == 10){
		Ws[i] = D/1024;
	    }
	    else if (IDs[i] == 12){
		Ws[i] = D/4096;
	    }
	    else {
		System.err.println("Error: ID value not supported: "+IDs[i]);
	    }
	}
    }

    public String toString(){
	String res = "" + IDs[0];
	for (int i=1;i<IDs.length;i++){
	    res += ", " + IDs[i];
	}
	return res;
    }
    
}
