/*   AUTHOR :           Benjamin Bach (bbach@lri.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zvtm.nodetrix;

import java.util.Comparator;

public class NTNodeDegreeComparator implements Comparator<NTNode> {

	public int compare(NTNode o0, NTNode o1) 
	{
		return o1.getIntraEdgeOutDegree() - o0.getIntraEdgeOutDegree();
	}

	
}
