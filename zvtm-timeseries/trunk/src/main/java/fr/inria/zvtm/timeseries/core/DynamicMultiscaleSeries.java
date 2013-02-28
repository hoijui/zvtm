package fr.inria.zvtm.timeseries.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An extension of {@link MultiscaleSeries} that fetches missing data when needed.
 * Users should actually extend from one of {@link Double} or {@link Long}, which
 * differ in the type they use for representing ranges of data to fetch.
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
	protected DataChunk chunkMissing(int scale, long offset) {
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
	private void scheduleFetch(int scale, long o1, long o2) {
		if (LOG) System.out.println("scheduleFetch "+scale+", range: "+o1+", "+o2);
		SparseData sparseData = getScaleData(scale);
		
		// Schedule a fetch for each missing range
		long lastO = o1;
		for(long o=o1;o<=o2;o++) {
			if (sparseData.hasChunk(o)) {
				long ro1 = lastO;
				long ro2 = o-1;
				if (ro1 <= ro2) submitFetch(scale, ro1, ro2);
				lastO = o+1;
			}
		}
		long ro1 = lastO;
		long ro2 = o2;
		if (ro1 <= ro2) submitFetch(scale, ro1, ro2);
	}
	
	private void submitFetch(int scale, long o1, long o2) {
		if (LOG) System.out.println("submitFetch "+scale+", range: "+o1+", "+o2);
		executor.submit(createFetchAction(scale, o1, o2));
	}
	
	protected abstract Runnable createFetchAction(int scale, long o1, long o2);
	
	/**
	 * A version that use double coordinates to represent ranges
	 * @author gpothier
	 */
	public static abstract class Double extends DynamicMultiscaleSeries {
		public Double(IChunkCache cache, int chunkSize) {
			super(cache, chunkSize);
		}

		/**
		 * Subclasses should implement this method to retrieve the data for
		 * a given scale and range. This method might be called concurrently
		 * from multiple threads.
		 */
		protected abstract IDataStream fetch(int scale, double x1, double x2);

		@Override
		protected Runnable createFetchAction(int scale, long o1, long o2) {
			return new FetchAction(scale, o1, o2);
		}

		/**
		 * The action that calls the fetch method 
		 * @author gpothier
		 */
		private class FetchAction implements Runnable {
			private final int scale;
			private final double x1;
			private final double x2;
			
			public FetchAction(int scale, long o1, long o2) {
				this.scale = scale;
				double ss = Math.pow(2, scale);
				
				this.x1 = ss*o1*chunkSize;
				this.x2 = ss*(o2+1)*chunkSize;
			}
			
			@Override
			public void run() {
				try {
					IDataStream stream = fetch(scale, x1, x2);
					if (stream != null) addDataD(x1, x2, stream);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * A version that use long coordinates to represent ranges
	 * @author gpothier
	 */
	public static abstract class Long extends DynamicMultiscaleSeries {
		public Long(IChunkCache cache, int chunkSize) {
			super(cache, chunkSize);
		}
		
		/**
		 * Subclasses should implement this method to retrieve the data for
		 * a given scale and range. This method might be called concurrently
		 * from multiple threads.
		 */
		protected abstract IDataStream fetch(int scale, long x1, long x2);
		
		@Override
		protected Runnable createFetchAction(int scale, long o1, long o2) {
			return new FetchAction(scale, o1, o2);
		}
		
		/**
		 * The action that calls the fetch method 
		 * @author gpothier
		 */
		private class FetchAction implements Runnable {
			private final int scale;
			private final long x1;
			private final long x2;
			
			public FetchAction(int scale, long o1, long o2) {
				this.scale = scale;
				long lss = 1L << scale;
				
				this.x1 = lss*o1*chunkSize;
				this.x2 = lss*(o2+1)*chunkSize;
			}
			
			@Override
			public void run() {
				try {
					IDataStream stream = fetch(scale, x1, x2);
					if (stream != null) addDataL(x1, x2, stream);
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}
	}
}
