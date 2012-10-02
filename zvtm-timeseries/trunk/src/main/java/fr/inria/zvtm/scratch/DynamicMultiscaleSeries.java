package fr.inria.zvtm.scratch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import zz.utils.properties.IProperty;
import zz.utils.properties.SimpleRWProperty;
import fr.inria.zvtm.scratch.MultiscaleSeries.DataStream;

/**
 * Similar to {@link MultiscaleSeries}, but:
 * - Provides view buffers that can emit change events
 * - Automatically requests data to providers as needed 
 * @author gpothier
 */
public class DynamicMultiscaleSeries {
	private Provider provider;
	private MultiscaleSeries source;
	
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	
	public ViewBuffer getData(float rangeStart, float rangeEnd, int bufferSize) {
		float sampleSize = (rangeEnd-rangeStart)/bufferSize;
		int logScale = (int) Math.floor(Math.log(sampleSize)/Math.log(2));

		float[] buffer = new float[bufferSize];
		int srcLogScale = source.getData(rangeStart, rangeEnd, buffer);
		
		ViewBuffer vb = new ViewBuffer(buffer);
		if (Math.abs(logScale-srcLogScale) > 2) {
			executor.execute(new FetchTask(vb, rangeStart, rangeEnd, logScale));
		}
		
		return vb;
	}

	public interface Provider {
		public int getMinLogScale();
		public int getMaxLogScale();
		public float getRangeStart();
		public float getRangeEnd();
		public DataStream get(float rangeStart, float rangeEnd, int bufferSize);
	}
	
	public class ViewBuffer {
		private float[] data;
		private final SimpleRWProperty<Boolean> pFetching = new SimpleRWProperty<Boolean>(false);
		
		public ViewBuffer(float[] data) {
			this.data = data;
		}
		
		/**
		 * This property is true whenever data for this buffer is currently being fetched.
		 * When it becomes false, updated data is available.
		 */
		public IProperty<Boolean> pFetching() {
			return pFetching;
		}
		
		public float[] getData() {
			return data;
		}
	}
	
	private class FetchTask implements Runnable {
		private final ViewBuffer viewBuffer;
		private final float rangeStart;
		private final float rangeEnd;
		private final int logScale;
		
		public FetchTask(ViewBuffer buffer, float rangeStart, float rangeEnd, int logScale) {
			this.viewBuffer = buffer;
			this.rangeStart = rangeStart;
			this.rangeEnd = rangeEnd;
			this.logScale = logScale;
		}

		public void run() {
			try {
				viewBuffer.pFetching.set(true);
				DataStream dataStream = provider.get(rangeStart, rangeEnd, logScale);
				source.addData(rangeStart, rangeEnd, dataStream);
				float[] buffer = new float[viewBuffer.data.length];
				source.getData(rangeStart, rangeEnd, buffer);
				viewBuffer.data = buffer;
				viewBuffer.pFetching.set(false);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

}
