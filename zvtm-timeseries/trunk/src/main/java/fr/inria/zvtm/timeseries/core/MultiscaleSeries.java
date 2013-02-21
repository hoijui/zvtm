package fr.inria.zvtm.timeseries.core;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import zz.utils.notification.IEvent;
import zz.utils.notification.SimpleEvent;
import fr.inria.zvtm.timeseries.core.DynamicMultiscaleSeries.Range;
import fr.inria.zvtm.timeseries.core.IChunkCache.ChunkData;
import gnu.trove.map.hash.TIntObjectHashMap;


/**
 * Represents a series of values of which scaled subsets can be obtained efficiently.
 * The underlying data set can be arbitrarily large and can be considered as a function f, 
 * where the value of the series at position x is given by f(x).
 * This class only maintains a cache of the data at different scales. 
 * Scale 0 is the 1:1 scale; positive scales correspond to zooming out, negative scales to zooming in.
 * 
 * The cache is organized as chunks of data; each chunk contains n = {@link #chunkSize} samples 
 * d[0]..d[n-1] that represent an average of the underlying data over a certain range.
 * For instance, for chunks that represent data starting at position x = 0: 
 * At scale 0, d[i] = avg(f, [i, i+1[)
 * At scale 1, d[i] = avg(f, [i*2, (i+1)*2[)
 * At scale -1, d[i] = avg(f, [i*0.5, (i+1)*0.5[)
 * The scale is the log2 of the size of the range represented by a sample, so:
 * At scale s, d[i] = avg(f, [i*2^s, (i+1)*2^s[)
 * 
 * In the general case (i.e. for chunks that do not start at position x = 0), there is 
 * an integer offset o that indicate which part of the underlying data a chunk represent, so:
 * d[i] = avg(f, [(i+o·n)·2^s, (i+1+o·n)·2^s[)   
 * 
 * Clients can request a data buffer to be filled for a certain extent of data. The 
 * {@link MultiscaleSeries} will then find the available cached data at the optimal scale,
 * and resample it to fill the buffer.
 * 
 * Data is added to the series through the {@link #addData(float, float, IDataStream)}
 * method. This method usually handles the addition of large amounts of data at a
 * time. When data is added, it is resampled on the fly to create caches at higher scales.
 * 
 * 5.70 12.15
 * @author gpothier
 */
public class MultiscaleSeries {
	public static final boolean LOG = false;
	
	public static final double log2d = Math.log(2);
	public static final double log2f = (float) log2d;
	
	private static final long SIZE_THRESHOLD = 1024;
	
	private int id;
	protected final int chunkSize;
	
	public final IEvent<Range> eRangeUpdated = new SimpleEvent<Range>();
	
	private Map<Integer, SparseData> dataMap = new HashMap<Integer, SparseData>();
	private int minScale = Integer.MAX_VALUE;
	private int maxScale = Integer.MIN_VALUE;
	
	private final IChunkCache chunkCache;
	
	public MultiscaleSeries(IChunkCache cache, int chunkSize) {
		this.chunkCache = cache;
		
		// Check that the cache's block size is a multiple of the chunk size 
		double sizeRatio = 1.0*MappedDiskCache.BLOCK_SIZE/chunkSize;
		assert Math.ceil(sizeRatio) == sizeRatio;
		this.chunkSize = chunkSize;
	}

	public int getId() {
		return id;
	}
	
	void setId(int id) {
		this.id = id;
	}
	
	public void addData(double x1, double x2, float[] values) {
		addData(x1, x2, new ArrayDataStream(values));
	}
		
	/**
	 * Adds raw data to this series.
	 */
	public synchronized void addData(double x1, double x2, IDataStream stream) {
		assert x2 > x1;
		long size = stream.getSize();
		assert size > 0;
		
		double sampleSize = (x2-x1)/size;
		double dScale = Math.log(sampleSize)/log2d;
		
		// We only accept power of 2 scales (TODO: resample when needed instead)
		assert isInteger(dScale): dScale;
		
		// Determine to range of scales to fill (we take the original scale an zoom out 
		// until we reach SIZE_THRESHOLD samples
		int scale = (int) dScale;
		int maxScale = size > SIZE_THRESHOLD ? scale + (int) Math.ceil(Math.log(size/SIZE_THRESHOLD)/log2d) : scale;
		
		DataChunk[] chunks = new DataChunk[maxScale+1-scale];
		double[] sums = new double[chunks.length];
		int[] counts = new int[chunks.length];
		int[] indexes = new int[chunks.length];
		
		// d[i] = avg(f, [(i+o·n)·2^s, (i+1+o·n)·2^s[)
		
		// Setup chunk & index stack
		// the Js are sample indexes in the coordinate space of each scale
		for (int i=0;i<chunks.length;i++) {
			double ss = Math.pow(2, scale+i);
			
			int j1 = (int) Math.floor(x1/ss);
			int offset1 = (int) Math.floor(1.0*j1/chunkSize);
			indexes[i] = j1-(offset1*chunkSize);
			assert indexes[i] >= 0;

			chunks[i] = getChunk(scale+i, offset1, true);
			chunks[i].setSynthetic(false);
			assert chunks[i].offset == offset1;
		}
		
		float[] buffer = new float[chunkSize];
		long k = 0; // sample index in original data
		while(k < size) {
			int len = Math.min(chunkSize, (int) (size-k));
			len = Math.min(len, chunkSize-indexes[0]);
			int l = stream.fillBuffer(buffer, indexes[0], len);
			chunks[0].set(buffer);
			if (LOG) System.out.println("adding: scale "+scale+", offset "+chunks[0].offset);
			assert len == l;
			
			for(int i=indexes[0];i<indexes[0]+len;i++) {
				float v = buffer[i];
				int c = 2; // Number of original samples per chunk sample for the current scale
				for(int j=1;j<chunks.length;j++) {
					sums[j] += v;
					counts[j] += 1;
					if (counts[j] >= c || k >= size) {
						chunks[j].set(indexes[j], (float) (sums[j] / counts[j]));
						indexes[j] += 1;
						if (indexes[j] >= chunkSize) {
							if (k < size) {
								chunks[j] = getChunk(scale+j, chunks[j].offset+1, true);
								chunks[j].setSynthetic(false);
							}
							indexes[j] = 0;
						}
						sums[j] = 0;
						counts[j] = 0;
					}
					c *= 2;
				}
			}
			
			chunks[0] = getChunk(scale, chunks[0].offset+1, true);
			
			indexes[0] = 0;
			k += len;
		}
		
		((SimpleEvent<Range>)eRangeUpdated).fire(new Range(x1, x2));
		rangeUpdated(x1, x2);
	}
	
	/**
	 * Called when a range of data has been updated. Does nothing by default,
	 * but subclasses can override it to perform a specific action
	 */
	protected void rangeUpdated(double x1, double x2) {
	}
	
	/**
	 * Fills the given buffer with data from the indicated range.
	 * The number of samples to fill is determined by the size of the buffer.
	 * The buffer is always completely filled; 
	 * if data is not available at some position, it is filled with 0 instead.
	 * @return The logScale that was used to render the buffer
	 */
	public int getData(double x1, double x2, float[] buffer) {
		assert x2 > x1;
		assert buffer.length > 0;
		
		// Find data series closest to requested range and scale
		SparseData src = getOptimalData(x1, x2, buffer.length);
		
		// Resample data
		// The is a very slow algorithm, should be replaced by an incremental version
		for(int dK=0;dK<buffer.length;dK++) {
			double sx1 = (dK*(x2-x1)/buffer.length)+x1;
			double sx2 = ((dK+1)*(x2-x1)/buffer.length)+x1;
			buffer[dK] = (float) src.getAverageValue(sx1, sx2);
		}
		
		return src.scale;
	}
	
	/**
	 * Only for tests
	 */
	public double _getAverage(double x1, double x2, int scale) {
		SparseData src = dataMap.get(scale);
		return src.getAverageValue(x1, x2);
	}
	
	public int getMaxScale() {
		return maxScale;
	}
	
	public int getMinScale() {
		return minScale;
	}
	
	/**
	 * Gets the data chunk for the given scale and offset.
	 * If there is no such chunk and create is true, then returns a newly
	 * created chunk.
	 */
	private DataChunk getChunk(int scale, int offset, boolean create) {
		SparseData sd = dataMap.get(scale);
		if (sd == null && !create) return null;
		if (sd == null) {
			if (scale > maxScale) maxScale = scale;
			if (scale < minScale) minScale = scale;
			sd = new SparseData(scale);
			dataMap.put(scale, sd);
		}
		
		DataChunk chunk = sd.getChunk(offset);
		if (chunk == null && !create) return null;
		if (chunk == null) {
			chunk = new DataChunk(scale, offset);
			sd.add(chunk);
		}
		
		return chunk;
	}
	
	protected DataChunk createChunk(int scale, int offset) {
		DataChunk chunk = new DataChunk(scale, offset);
		addChunk(chunk);
		return chunk;
	}
	
	/**
	 * Only for unit tests
	 */
	public DataChunk _createChunk(int scale, int offset, float[] values) {
		return new DataChunk(scale, offset, values);
	}
	
	private void addChunk(DataChunk chunk) {
		SparseData sd = dataMap.get(chunk.scale);
		if (sd == null) {
			sd = new SparseData(chunk.scale);
			dataMap.put(chunk.scale, sd);
		}
		sd.add(chunk);
	}
	
	private static boolean isInteger(double d) {
		return d == Math.round(d);
	}
	
	private SparseData getOptimalData(double x1, double x2, int size) {
		double sampleSize = (x2-x1)/size;
		int scale = (int) Math.floor(Math.log(sampleSize)/Math.log(2));
		if (scale > maxScale) scale = maxScale;

		return getScaleData(scale);
	}
	
	protected SparseData getScaleData(int scale) {
		SparseData sd = dataMap.get(scale);
		if (sd == null) {
			sd = new SparseData(scale);
			dataMap.put(scale, sd);
		}
		return sd;
	}

	/**
	 * Called when the an attempt is made to obtain data from a chunk
	 * that is not available. 
	 * @return A chunk or null if can't be obtained. 
	 */
	protected DataChunk chunkMissing(int scale, int offset) {
		return null;
	}

	/**
	 * Returns the chunk which has the smallest scale whose
	 * span contains the missing chunk at the specified scale and offset.  
	 */
	protected DataChunk getLowestChunk(int scale, int offset) {
		while(scale <= maxScale) {
			SparseData sd = dataMap.get(scale);
			if (sd == null) {
				scale++;
				offset /= 2;
				continue;
			}
			
			DataChunk chunk = sd.getChunk(offset);
			if (chunk == null || chunk.isSynthetic()) {
				scale++;
				offset /= 2;
				continue;
			}
			
			return chunk;
		}
		return null;
	}
	
	/**
	 * A chunk of data representing the underlying data at a given scale.
	 * The actual data is not contained in the chunk but is accessed through
	 * a float buffer.
	 * @author gpothier
	 */
	public class DataChunk {
		public final int scale;
		public final int offset;
		private final double ss;

		public final FloatBuffer buffer;
		public final int bufferOffset;
		
		private boolean synthetic = true;

		private DataChunk(int scale, int offset) {
			this.scale = scale;
			this.offset = offset;
			ChunkData chunkData = chunkCache.getChunkData(id, scale, offset);
			this.buffer = chunkData.buffer;
			this.bufferOffset = chunkData.offset;
			for(int i=0;i<chunkSize;i++) set(i, Float.NaN);
			ss = Math.pow(2, scale);
		}
		
		private DataChunk(int scale, int offset, float[] data) {
			this.scale = scale;
			this.offset = offset;
			ChunkData chunkData = chunkCache.getChunkData(id, scale, offset);
			this.buffer = chunkData.buffer;
			this.bufferOffset = chunkData.offset;
			set(data);
			ss = Math.pow(2, scale);
		}
		
		public float get(int i) {
			return buffer.get(bufferOffset+i);
		}
		
		public void set(int i, float value) {
			buffer.put(bufferOffset+i, value);
		}
		
		public void set(float[] values) {
			assert values.length == chunkSize;
			synchronized(buffer) {
				buffer.position(bufferOffset);
				buffer.put(values);
			}
		}
		
		public boolean contains(double x) {
			double start = ss*offset*chunkSize;
			double end = ss*(offset+1)*chunkSize;
			return x >= start && x <= end;
		}
		
		public boolean isSynthetic() {
			return synthetic;
		}
		
		public void setSynthetic(boolean synthetic) {
			this.synthetic = synthetic;
		}
		
		/**
		 * Computes the average value of the chunk over the interval [x1, x2[.
		 * (absolute coordinates)
		 */
		public double getAverageValue(double x1, double x2) {
			double[] result = new double[2];
			getAverageValue(result, x1, x2);
			return result[0]/result[1];
		}
			

		/**
		 * Computes the average value of the chunk over the interval [x1, x2[.
		 * (absolute coordinates).
		 * @param result a array to store the result: [sum, count]
		 */
		public void getAverageValue(double[] result, double x1, double x2) {
			assert x1 < x2;
			assert contains(x1);
			assert contains(x2);
			
			double dj1 = x1/ss;
			long j1 = (long) Math.floor(dj1);
			double dj2 = x2/ss;
			long j2 = (long) Math.floor(dj2);
			
			assert Math.floor(1.0*j1/chunkSize) == offset: "j1: "+j1+" chunkSize: "+chunkSize+", offset: "+offset+" vs. "+Math.floor(1.0*j1/chunkSize);
					
			int i = (int) (j1-(1L*offset*chunkSize));
			int i2 = (int) (j2-(1L*offset*chunkSize));
			double sum = 0;
			
			if (i == i2) {
				double w = dj2-dj1;
				result[0] = get(i)*w;
				result[1] = w;
			} else {
				// Compute the contribution of the first sample
				double w = 1-(dj1-j1);
				sum += get(i)*w;
				i++;

				// Include the middle samples
				while(i < i2) {
					sum += get(i++);
				}
				
				// Include the last sample
				w = dj2-j2;
				if (w > 0) sum += get(i)*w;
				
				result[0] = sum;
				result[1] = dj2-dj1;
			}
		}
		
		@Override
		public String toString() {
			StringBuilder b = new StringBuilder("Chunk (id: "+id+", scale: "+scale+", offset: "+offset+") = ");
			for(int i=0;i<chunkSize;i++) {
				b.append(get(i));
				b.append(' ');
			}
			return b.toString();
		}
	}
	
	/**
	 * Container for {@link DataChunk}s at a given scale.
	 */
	protected class SparseData {
		private final int scale;
		private final double ss;
		
		/**
		 * Offset to chunk map
		 */
		private TIntObjectHashMap<DataChunk> chunks = new TIntObjectHashMap<DataChunk>();

		public SparseData(int scale) {
			this.scale = scale;
			ss = Math.pow(2, scale);
		}

		public void add(DataChunk chunk) {
			chunks.put(chunk.offset, chunk);
		}
		
		public DataChunk getChunk(int offset) {
			return chunks.get(offset);
		}
		
		public boolean hasChunk(int offset) {
			return chunks.containsKey(offset);
		}
		
		/**
		 * Computes the average value of the serie over the interval [x1, x2[
		 * using data of the current scale
		 */
		public double getAverageValue(double x1, double x2) {
			int offset = (int) Math.floor(x1/(ss*chunkSize));
			
			double sum = 0;
			double count = 0;
			double[] buffer = new double[2];
			while(true) {
				double cx1 = ss*offset*chunkSize;
				double cx2 = ss*(offset+1)*chunkSize;
				if (cx1 >= x2) break;
				DataChunk chunk = chunks.get(offset);
				
				if (chunk == null) {
					// Attempt to obtain the missing data
					chunk = chunkMissing(scale, offset);
					if (chunk != null) add(chunk);
				}
				
				if (chunk == null) {
					// If no data, assume all zeros
					count += (cx2-cx1)/ss;
				} else {
					chunk.getAverageValue(buffer, Math.max(cx1, x1), Math.min(cx2,  x2));
					sum += buffer[0];
					count += buffer[1];
				}
				offset++;
			}
			
			return sum/count;
		}
		
	}
	
	/**
	 * A streaming provider of float values.
	 * @author gpothier
	 */
	public interface IDataStream {
		/**
		 * Returns the total number of samples that can be returned by this stream
		 */
		public long getSize();
		
		/**
		 * Fills the given buffer with the next available samples.
		 * @return The number of samples that have been actually filled. 
		 */
		public int fillBuffer(float[] buffer, int offset, int len);
	}
	
	/**
	 * A simple implementation of {@link IDataStream} that is backed by an
	 * in-memory array.
	 * @author gpothier
	 */
	public static class ArrayDataStream implements IDataStream {
		private final float[] data;
		private int index = 0;

		public ArrayDataStream(float[] data) {
			this.data = data;
		}

		public long getSize() {
			return data.length;
		}

		public int fillBuffer(float[] buffer, int offset, int len) {
			if (len > data.length-index) len = data.length-index;
			System.arraycopy(data, index, buffer, offset, len);
			index += len;
			return len;
		}
		
	}
}
