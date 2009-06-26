package fr.inria.zvtm.clustering;

import java.util.concurrent.atomic.AtomicLong;

public class ObjIdFactory {
	private static final AtomicLong currId = new AtomicLong(0);

	/**
	 * Disallow instanciation
	 */
	private ObjIdFactory(){}

	public static final ObjId next(){
		return new ObjId(currId.incrementAndGet());
	}
}

