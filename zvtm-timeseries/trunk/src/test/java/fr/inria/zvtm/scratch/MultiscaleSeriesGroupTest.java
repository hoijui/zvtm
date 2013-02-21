package fr.inria.zvtm.scratch;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import fr.inria.zvtm.timeseries.core.MultiscaleSeries;
import fr.inria.zvtm.timeseries.core.MultiscaleSeriesGroup;

public class MultiscaleSeriesGroupTest {
	private static final int CHUNK_SIZE = 1024;
	
	/**
	 * Tests a multi-chunk add at scale 0, and just read the buffer back
	 * to check if the values are the same. 
	 */
	@Test 
	public void testLongAddSimple() {
		MultiscaleSeries[] series = new MultiscaleSeries[1000];
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup();
		for(int i=0;i<series.length;i++) series[i] = new MultiscaleSeries(group.getCache(), CHUNK_SIZE);
		group.setSeries(series);
		
		float[] data = new float[64*1024];
		for(int i=0;i<series.length;i++) {
			for(int j=0;j<data.length;j++) data[j] = i;
			series[i].addData(0, 1, data);
		}

		float[] buffer = new float[1];
		for(int i=0;i<series.length;i++) {
			series[i].getData(0, 1, buffer);
			assertEquals(i, (int) buffer[0]); 
		}		
	}
}
