package fr.inria.zvtm.nodetrix;

import java.util.Comparator;

public class MatrixSizeComparator implements Comparator<Matrix> {

	public int compare(Matrix m1, Matrix m2) {
		return m2.getSize() - m1.getSize();
	}

}
