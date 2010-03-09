/*   AUTHOR :           Benjamin Bach (bbach@lri.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */
 
package fr.inria.zvtm.nodetrix;

import java.util.Comparator;

public class MatrixSizeComparator implements Comparator<Matrix> {

	public int compare(Matrix m1, Matrix m2) {
		return m2.getSize() - m1.getSize();
	}

}
