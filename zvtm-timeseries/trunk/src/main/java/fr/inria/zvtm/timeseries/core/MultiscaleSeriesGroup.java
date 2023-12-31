package fr.inria.zvtm.timeseries.core;

import java.io.File;
import java.io.IOException;

import fr.inria.zvtm.timeseries.core.MappedDiskCache.Block;
import gnu.trove.map.hash.TObjectIntHashMap;

/**
 * A group of series that are intended to be displayed together.
 * It is also the chunk cache for the series, and it takes care
 * to store the all the chunks for a given cluster (ie. offset, scale pair)
 * for all the series together, so as to optimize caching.  
 * @author gpothier
 */
public class MultiscaleSeriesGroup {
	private final Cache cache = new Cache();
	private MultiscaleSeries[] seriesList;
	private final TObjectIntHashMap<ClusterKey> clustersMap = new TObjectIntHashMap<ClusterKey>();
	private final int chunkSize;
	
	private int nextClusterIndex = 1;
	
	public MultiscaleSeriesGroup(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public MultiscaleSeriesGroup(int chunkSize, int size) {
		this.chunkSize = chunkSize;
		seriesList = new MultiscaleSeries[size];
		for(int i=0;i<size;i++) {
			MultiscaleSeries series = new MultiscaleSeries(cache, chunkSize);
			series.setId(i);
			seriesList[i] = series;
		}
	}

	public void setSeries(MultiscaleSeries[] seriesList) {
		if (this.seriesList != null) throw new RuntimeException("Cannot initialize series more than once");
		this.seriesList = seriesList;
		for(int i=0;i<seriesList.length;i++) {
			seriesList[i].setId(i);
		}
	}
	
	public Cache getCache() {
		return cache;
	}

	public MultiscaleSeries get(int index) {
		return seriesList[index];
	}
	
	public int getSize() {
		return seriesList.length;
	}
	
	private class Cache implements IChunkCache {
		private final MappedDiskCache cache;

		public Cache() {
			try {
				File file = File.createTempFile("multiscale", ".bin");
				file.deleteOnExit();
				cache = new MappedDiskCache(file);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public ChunkData getChunkData(int seriesId, int scale, long offset) {
			int index = getClusterIndex(scale, offset);
			long cacheOffset = (1L*index*seriesList.length + seriesId)*chunkSize*(Float.SIZE/8);
			Block block = cache.getBlock(cacheOffset);
			long blockOffset = cacheOffset - block.getOffset();
			assert blockOffset < Integer.MAX_VALUE;
			int floatBufferOffset = (int) (blockOffset/(Float.SIZE/8));
			
			return new ChunkData(floatBufferOffset, block.getFloatView());
		}

		/**
		 * Returns the storage index for the given cluster.
		 */
		private synchronized int getClusterIndex(int scale, long offset) {
			ClusterKey key = getClusterKey(scale, offset);
			int index = clustersMap.putIfAbsent(key, nextClusterIndex);
			if (index == clustersMap.getNoEntryValue()) index = nextClusterIndex++;
			return index;
		}
	}

	
	private static class ClusterKey {
		private final int scale;
		private final long offset;
		
		public ClusterKey(int scale, long offset) {
			this.scale = scale;
			this.offset = offset;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (int) (offset ^ (offset >>> 32));
			result = prime * result + scale;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ClusterKey other = (ClusterKey) obj;
			if (offset != other.offset)
				return false;
			if (scale != other.scale)
				return false;
			return true;
		}
	}
	/**
	 * Returns the mapping key for a given cluster
	 */
	private static ClusterKey getClusterKey(int scale, long offset) {
		return new ClusterKey(scale, offset);
	}
}
