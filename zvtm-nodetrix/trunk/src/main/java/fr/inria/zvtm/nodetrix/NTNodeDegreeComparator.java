package fr.inria.zvtm.nodetrix;

import java.util.Comparator;

public class NTNodeDegreeComparator implements Comparator<NTNode> {

	public int compare(NTNode o0, NTNode o1) 
	{
		return o1.getDegree() - o0.getDegree();
	}

	
}
