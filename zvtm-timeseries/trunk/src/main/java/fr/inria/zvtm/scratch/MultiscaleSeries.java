package fr.inria.zvtm.scratch;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import zz.utils.cache.MRUBuffer;

public class MultiscaleSeries {
	private static final File CACHE_PATH = new File("/tmp/multiscale");
	private static final int MAX_MEM_CACHE_SIZE = 20*1024*1024;
	private static final long SIZE_THRESHOLD = 1024;
	private static final int MAX_CHUNK_SIZE = 4*1024;
	
	private float rangeStart;
	private float rangeEnd;
	
	private Map<Integer, SparseData> dataMap = new HashMap<Integer, SparseData>();
	private int minLogScale;
	private int maxLogScale;
	
	private ChunkCache chunkCache = new ChunkCache();
	
	public MultiscaleSeries() {
		CACHE_PATH.mkdirs();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		FileUtils.deleteDirectory(CACHE_PATH);
	}
	
	public void addData(float rangeStart, float rangeEnd, DataStream stream) {
		assert rangeEnd > rangeStart;
		long size = stream.getSize();
		assert size > 0;
		
		float sampleSize = (rangeEnd-rangeStart)/size;
		double dLogScale = Math.log(sampleSize)/Math.log(2);
		
		// We only accept power of 2 scales for now (TODO: resample when needed instead)
		assert isInteger(dLogScale);
		
		int logScale = (int) dLogScale;
		int maxLogScale = size > SIZE_THRESHOLD ? (int) Math.ceil(Math.log(size/SIZE_THRESHOLD)/Math.log(2)) : logScale;
		
		float[][] buffers = new float[maxLogScale+1-logScale][MAX_CHUNK_SIZE];
		float[] sums = new float[buffers.length];
		float[] counts = new float[buffers.length];
		int[] indexes = new int[buffers.length];
		long k = 0;
		while(k < size) {
			int l = stream.fillBuffer(buffers[0]);
			float chunkStart = rangeStart + (k*(rangeEnd-rangeStart)/size);
			float chunkEnd = rangeStart + ((k+l)*(rangeEnd-rangeStart)/size);
			
			float[] srcBuffer = buffers[0];
			addChunk(new DataChunk(logScale, srcBuffer, chunkStart, chunkEnd));

			for(int i=0;i<srcBuffer.length;i++) {
				float v = srcBuffer[i];
				int c = 2;
				for(int j=1;j<buffers.length;j++) {
					sums[j] += v;
					counts[j] += 1;
					if (counts[j] > c || k >= size) {
						buffers[j][indexes[j]] = sums[j] / counts[j];
						indexes[j] += 1;
						if (indexes[j] >= MAX_CHUNK_SIZE || k >= size) {
							if (k >= size) for(int m=indexes[j];m<MAX_CHUNK_SIZE;m++) buffers[j][m] = Float.NaN;
							addChunk(new DataChunk(logScale+j, buffers[j], chunkStart, chunkEnd));
							indexes[j] = 0;
						}
						sums[j] = 0;
						counts[j] = 0;
					}
					c *= 2;
				}
			}
			
			k += l;
		}
	}
	
	/**
	 * Fills the given buffer with data from the indicated range.
	 * The number of samples to fill is determined by the size of the buffer.
	 * The buffer is always completely filled; 
	 * if data is not available at some position, it is filled with 0 instead.
	 * @return The logScale that was used to render the buffer
	 */
	public int getData(float rangeStart, float rangeEnd, float[] buffer) {
		assert rangeEnd > rangeStart;
		assert buffer.length > 0;
		
		// Find data series closest to requested range and scale
		SparseData src = getOptimalData(rangeStart, rangeEnd, buffer.length);
		
		// Resample data
		// The is a very crude nearest neighbour algorithm. Could be improved.
		for(int dK=0;dK<buffer.length;dK++) {
			float dX = (dK*(rangeEnd-rangeStart)/buffer.length)+rangeStart;
			buffer[dK] = src.getValue(dX);
		}
		
		return src.logScale;
	}
	
	private void addChunk(DataChunk chunk) {
		SparseData data = dataMap.get(chunk.key.logScale);
		data.add(chunk);
	}
	
	private static boolean isInteger(double d) {
		return d == Math.round(d);
	}
	
	private SparseData getOptimalData(float rangeStart, float rangeEnd, int size) {
		float sampleSize = (rangeEnd-rangeStart)/size;
		int logScale = (int) Math.floor(Math.log(sampleSize)/Math.log(2));
		if (logScale > maxLogScale) logScale = maxLogScale;
		if (logScale < minLogScale) logScale = minLogScale;
		return dataMap.get(logScale);
	}
	
	private void writeFloats(String name, float[] data) {
		try {
			File f = new File(CACHE_PATH, name);
			DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			dos.writeInt(data.length);
			for(int i=0;i<data.length;i++) dos.writeFloat(data[i]);
			dos.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private float[] readFloats(String name) {
		try {
			File f = new File(CACHE_PATH, name);
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(f)));
			int l = dis.readInt();
			float[] buffer = new float[l];
			for(int i=0;i<l;i++) buffer[i] = dis.readFloat();
			dis.close();
			return buffer;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static class DataChunkKey {
		private final int logScale;
		private final float rangeStart;
		private final float rangeEnd;
		private final String uuid = UUID.randomUUID().toString();
		
		public DataChunkKey(int logScale, float rangeStart, float rangeEnd) {
			this.logScale = logScale;
			this.rangeStart = rangeStart;
			this.rangeEnd = rangeEnd;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + logScale;
			result = prime * result + Float.floatToIntBits(rangeEnd);
			result = prime * result + Float.floatToIntBits(rangeStart);
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
			DataChunkKey other = (DataChunkKey) obj;
			if (logScale != other.logScale)
				return false;
			if (Float.floatToIntBits(rangeEnd) != Float.floatToIntBits(other.rangeEnd))
				return false;
			if (Float.floatToIntBits(rangeStart) != Float.floatToIntBits(other.rangeStart))
				return false;
			return true;
		}
	}
	
	private static class DataChunk {
		private final DataChunkKey key;
		private final float[] values;

		public DataChunk(int logScale, float[] data, float rangeStart, float rangeEnd) {
			assert data.length <= MAX_CHUNK_SIZE;
			this.values = data;
			key = new DataChunkKey(logScale, rangeStart, rangeEnd);
		}
		
		public DataChunk(DataChunkKey key, float[] values) {
			this.key = key;
			this.values = values;
		}

		public float getValue(float x) {
			int k = (int)((x-key.rangeStart)*values.length/(key.rangeEnd-key.rangeStart));
			return values[k];
		}
		
		public boolean contains(float x) {
			return x >= key.rangeStart && x < key.rangeEnd;
		}
	}
	
	private class SparseData {
		private int logScale;
		private List<DataChunkKey> values = new ArrayList<DataChunkKey>();
		private float rangeStart;
		private float rangeEnd;

		public void add(DataChunk chunk) {
			if (values.isEmpty()) {
				logScale = chunk.key.logScale;
				rangeStart = chunk.key.rangeStart;
				rangeEnd = chunk.key.rangeEnd;
			} else {
				assert logScale == chunk.key.logScale;
				if (rangeStart > chunk.key.rangeStart) rangeStart = chunk.key.rangeStart;
				if (rangeEnd < chunk.key.rangeEnd) rangeEnd = chunk.key.rangeEnd;
			}
			chunkCache.add(chunk);
			values.add(chunk.key);
			writeFloats(chunk.key.uuid, chunk.values);
		}
		
		public float getValue(float x) {
			if (x < rangeStart || x >= rangeEnd) return Float.NaN;
			// TODO: implement binary search
			for(int i=0;i<values.size();i++) {
				DataChunk d = chunkCache.get(values.get(i));
				if (d.contains(x)) return d.getValue(x);
			}
			return Float.NaN;
		}
	}
	
	private class ChunkCache extends MRUBuffer<DataChunkKey, DataChunk> {
		public ChunkCache() {
			super(MAX_MEM_CACHE_SIZE/MAX_CHUNK_SIZE);
		}

		@Override
		protected DataChunkKey getKey(DataChunk aValue) {
			return aValue.key;
		}

		@Override
		protected DataChunk fetch(DataChunkKey aId) {
			float[] data = readFloats(aId.uuid);
			return new DataChunk(aId, data);
		}
	}
	
	/**
	 * A streaming provider of float values.
	 * @author gpothier
	 */
	public interface DataStream {
		/**
		 * Returns the total number of samples that can be returned by this stream
		 */
		public long getSize();
		
		/**
		 * Fills the given buffer with the next available samples.
		 * @return The number of samples that have been actually filled. 
		 */
		public int fillBuffer(float[] buffer);
	}

}
