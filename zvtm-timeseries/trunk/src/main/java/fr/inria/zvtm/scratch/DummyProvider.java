package fr.inria.zvtm.scratch;

import fr.inria.zvtm.scratch.MultiscaleSeries.DataStream;

public class DummyProvider implements DynamicMultiscaleSeries.Provider{

	public int getMinLogScale() {
		return -10;
	}

	public int getMaxLogScale() {
		return 10;
	}

	public DataStream get(float rangeStart, float rangeEnd, int logScale) {
		// TODO Auto-generated method stub
		return null;
	}

}
