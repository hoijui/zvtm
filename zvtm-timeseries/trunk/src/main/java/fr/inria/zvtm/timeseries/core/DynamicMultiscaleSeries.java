package fr.inria.zvtm.timeseries.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * En extension of {@link MultiscaleSeries} that fetches missing data when needed
 * 
 * @author gpothier
 */
public abstract class DynamicMultiscaleSeries extends MultiscaleSeries {
	/**
	 * When data is missing at a given scale S, fetch actual data
	 * at scale S-scaleDelta
	 */
	private int scaleDelta = 2;
	
	/**
	 * When data is missing for a given range, fetch data for a range
	 * rangeMultiplier times larger (adjusted to chunk boundaries)
	 */
	private int rangeMultiplier = 0;
	
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	
	public DynamicMultiscaleSeries(IChunkCache cache, int chunkSize) {
		super(cache, chunkSize);
	}

	@Override
	protected DataChunk chunkMissing(int scale, int offset) {
		if (LOG) System.out.println("chunkMissing: "+scale+", "+offset);
		
		// Immediately create a chunk with extrapolated data
		DataChunk lowestChunk = getLowestChunk(scale, offset);
		DataChunk chunk = createChunk(scale, offset);
		chunk.setSynthetic(true);
		
		if (lowestChunk != null) {
			assert lowestChunk.scale > scale;
			int n = 1 << (lowestChunk.scale - scale);
			int d = (int) (1.0*chunkSize*(1.0*offset/n - lowestChunk.offset));
			for(int i=0;i<chunkSize;i++) {
				chunk.set(i, lowestChunk.get(d+i/n));
			}
		}
		
		// Schedule a fetch of actual data at a lower scale
		scheduleFetch(scale-scaleDelta, (offset << scaleDelta)-rangeMultiplier/2, ((offset+1) << scaleDelta)-1+rangeMultiplier/2);
		
		return chunk;
	}
	
	/**
	 * Schedules the fetching of actual data at a given scale for a range of chunk offsets
	 * (both inclusive)
	 */
	private void scheduleFetch(int scale, int o1, int o2) {
		if (LOG) System.out.println("scheduleFetch "+scale+", range: "+o1+", "+o2);
		SparseData sparseData = getScaleData(scale);
		
		// Schedule a fetch for each missing range
		int lastO = o1;
		for(int o=o1;o<=o2;o++) {
			if (sparseData.hasChunk(o)) {
				int ro1 = lastO;
				int ro2 = o-1;
				if (ro1 <= ro2) submitFetch(scale, ro1, ro2);
				lastO = o+1;
			}
		}
		int ro1 = lastO;
		int ro2 = o2;
		if (ro1 <= ro2) submitFetch(scale, ro1, ro2);
	}
	
	private void submitFetch(int scale, int o1, int o2) {
		if (LOG) System.out.println("submitFetch "+scale+", range: "+o1+", "+o2);
		executor.submit(new FetchAction(scale, o1, o2));
	}
	
	/**
	 * Subclasses should implement this method to retrieve the data for
	 * a given scale and range. This method might be called concurrently
	 * from multiple threads.
	 */
	protected abstract IDataStream fetch(int scale, double x1, double x2);

	/**
	 * The action that calls the fetch method 
	 * @author gpothier
	 */
	private class FetchAction implements Runnable {
		private final int scale;
		private final double x1;
		private final double x2;
		
		public FetchAction(int scale, int o1, int o2) {
			this.scale = scale;
			double ss = Math.pow(2, scale);
			
			this.x1 = ss*o1*chunkSize;
			this.x2 = ss*(o2+1)*chunkSize;
		}
		
		@Override
		public void run() {
			try {
				IDataStream stream = fetch(scale, x1, x2);
				if (stream != null) addData(x1, x2, stream);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * A generic range with double values
	 * @author gpothier
	 */
	public static class Range {
		public final double x1;
		public final double x2;
		
		public Range(double x1, double x2) {
			this.x1 = x1;
			this.x2 = x2;
		}
	}
}
