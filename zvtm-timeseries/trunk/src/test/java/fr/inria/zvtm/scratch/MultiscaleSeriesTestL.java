package fr.inria.zvtm.scratch;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.zvtm.timeseries.core.MultiscaleSeries;
import fr.inria.zvtm.timeseries.core.MultiscaleSeriesGroup;
import fr.inria.zvtm.timeseries.core.MultiscaleSeries.DataChunk;
import fr.inria.zvtm.timeseries.core.MultiscaleSeries.IDataStream;

public class MultiscaleSeriesTestL {

	private static final int DATA_SIZE = 16*1024*1024;
	private static final int CHUNK_SIZE = 2048;
	
	private DummyData dummyData = new DummyData();
	
	@Test
	public void testFullChunkAverage() {
		float[] buffer = new float[CHUNK_SIZE];
		System.arraycopy(dummyData.data, 0, buffer, 0, CHUNK_SIZE);
		
		for(int scale=0;scale<10;scale++) {
			for(int offset=-10;offset<10;offset++) {
				checkFullChunkAverage(buffer, scale, offset);
			}
		}
	}
	
	private void checkFullChunkAverage(float[] buffer, int scale, int offset) {
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(CHUNK_SIZE, 1);
		DataChunk chunk = group.get(0)._createChunk(scale, offset, buffer);
		
		long lss = 1L << scale;
		long x1 = offset*CHUNK_SIZE*lss;
		long x2 = (offset+1)*CHUNK_SIZE*lss;
		
		double sum = 0;
		for(int i=0;i<CHUNK_SIZE;i++) sum += buffer[i];
		double avg1 = sum/CHUNK_SIZE;
		
		double avg2 = chunk.getAverageValueL(x1, x2);
		assertEquals("Scale: "+scale+", offset: "+offset, avg1, avg2, Float.MIN_VALUE);
	}
	
	@Test
	public void testPartialChunkIntAverage() {
		float[] buffer = new float[CHUNK_SIZE];
		for(int i=0;i<CHUNK_SIZE;i++) buffer[i] = i;
		
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(CHUNK_SIZE, 1);
		DataChunk chunk = group.get(0)._createChunk(0, 0, buffer);
		
		Random random = new Random(123);
		for (int i=0;i<1000;i++) {
			int x1 = (int) (random.nextFloat()*CHUNK_SIZE);
			int x2 = (int) (random.nextFloat()*CHUNK_SIZE);
			
			if (x1 == x2) continue;
			
			if (x1 > x2) {
				int x = x2;
				x2 = x1;
				x1 = x;
			}
			
			double avg1 = (-1f+x2+x1)/2;
			double avg2 = chunk.getAverageValueL(x1, x2);
			assertEquals("i: "+i+", x1: "+x1+", x2: "+x2, avg1, avg2, Float.MIN_VALUE);
		}
	}
	
	private static class DummyData {
		private float[] data = new float[DATA_SIZE];
		
		public DummyData() {
			Random random = new Random(8); // We want reproductible tests...
			for(int i=0;i<DATA_SIZE;i++) data[i] = random.nextFloat();
		}
		
		public float get(int index) {
			return data[index];
		}
		
		public IDataStream getStream(int offset, int size) {
			return new DummyStream(offset, size);
		}
		
		private class DummyStream implements IDataStream {
			private int offset;
			private int current;
			private int size;
			
			public DummyStream(int offset, int size) {
				this.offset = offset;
				this.size = size;
				this.current = 0;
			}
			
			public long getSize() {
				return size;
			}
			
			public int fillBuffer(float[] buffer, int offset, int len) {
				if (current+len > size) len = size-current;
				for(int i=0;i<len;i++) buffer[offset+i] = data[this.offset+this.current+i];
				this.current += len;
				return len;
			}
		}
	}
}
