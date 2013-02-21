package fr.inria.zvtm.scratch;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.BeforeClass;
import org.junit.Test;

import fr.inria.zvtm.timeseries.core.MultiscaleSeries;
import fr.inria.zvtm.timeseries.core.MultiscaleSeriesGroup;
import fr.inria.zvtm.timeseries.core.MultiscaleSeries.DataChunk;
import fr.inria.zvtm.timeseries.core.MultiscaleSeries.IDataStream;

public class MultiscaleSeriesTest {

	private static final int DATA_SIZE = 16*1024*1024;
	private static final int CHUNK_SIZE = 1024;
	
	private DummyData dummyData = new DummyData();
	
	@Test
	public void testFullChunkAverage() {
		float[] buffer = new float[CHUNK_SIZE];
		System.arraycopy(dummyData.data, 0, buffer, 0, CHUNK_SIZE);
		
		for(int scale=-10;scale<10;scale++) {
			for(int offset=-10;offset<10;offset++) {
				checkFullChunkAverage(buffer, scale, offset);
			}
		}
	}
	
	private void checkFullChunkAverage(float[] buffer, int scale, int offset) {
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(1);
		DataChunk chunk = group.get(0)._createChunk(scale, offset, buffer);
		
		double ss = Math.pow(2, scale);
		float x1 = (float) (offset*CHUNK_SIZE*ss);
		float x2 = (float) ((offset+1)*CHUNK_SIZE*ss);
		
		double sum = 0;
		for(int i=0;i<CHUNK_SIZE;i++) sum += buffer[i];
		double avg1 = sum/CHUNK_SIZE;
		
		double avg2 = chunk.getAverageValue(x1, x2);
		assertEquals("Scale: "+scale+", offset: "+offset, avg1, avg2, Float.MIN_VALUE);
	}
	
	@Test
	public void testPartialChunkIntAverage() {
		float[] buffer = new float[CHUNK_SIZE];
		for(int i=0;i<CHUNK_SIZE;i++) buffer[i] = i;
		
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(1);
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
			double avg2 = chunk.getAverageValue(x1, x2);
			assertEquals("i: "+i+", x1: "+x1+", x2: "+x2, avg1, avg2, Float.MIN_VALUE);
		}
	}
	
	@Test
	public void testPartialChunkFloatAverage() {
		float[] buffer = new float[CHUNK_SIZE];
		for(int i=0;i<CHUNK_SIZE;i++) buffer[i] = i;
		
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(1);
		DataChunk chunk = group.get(0)._createChunk(0, 0, buffer);
		
		Random random = new Random(123);
		for (int i=0;i<1000;i++) {
			double x1 = random.nextDouble()*CHUNK_SIZE;
			double x2 = random.nextDouble()*CHUNK_SIZE;
			
			if (x1 > x2) {
				double x = x2;
				x2 = x1;
				x1 = x;
			}
			
			double avg1 = (x2+x1)/2;
			double avg2 = chunk.getAverageValue(x1, x2);
			assertEquals("i: "+i+", x1: "+x1+", x2: "+x2, avg1, avg2, 1.0);
		}
	}
	
	/**
	 * Tests a multi-chunk add at scale 0, and just read the buffer back
	 * to check if the values are the same. 
	 */
	@Test 
	public void testLongAddSimple() {
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(1);
		MultiscaleSeries series = group.get(0);
		series.addData(0, dummyData.data.length, dummyData.getStream(0, dummyData.data.length));
	
		float[] buffer = new float[CHUNK_SIZE];
		for(int j=0;j<dummyData.data.length;j+=CHUNK_SIZE) {
			int scale = series.getData(j, j+CHUNK_SIZE, buffer);
			assertEquals(0, scale);
			
			for(int i=0;i<CHUNK_SIZE;i++) {
				assertEquals(""+(j+i), dummyData.get(j+i), buffer[i], Float.MIN_VALUE);
			}
		}
	}
	
	@Test
	public void testSparseAverage() {
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(1);
		MultiscaleSeries series = group.get(0);
		series.addData(0, dummyData.data.length, dummyData.getStream(0, dummyData.data.length));
		
		Random random = new Random(109384);
		double sum = 0;
		int count = 0;
		for(int i=10;i<dummyData.data.length;i += random.nextInt(dummyData.data.length/100)) {
			while(count < i) sum += dummyData.data[count++];
			double avg1 = sum/count;
			
			int scale = 0;
			int mask = 1;
			while(scale <= series.getMaxScale()) {
				if (i == 4235264) {
					System.out.println("MultiscaleSeriesTest.testSparseAverage()");
				}
				double avg2 = series._getAverage(0, count, scale);
				double err = 0.000001*Math.pow(2, scale);
				double r = avg1/avg2;
				assertTrue("i: "+i+", scale: "+scale+" r: "+r+", err: "+err, Math.abs(1-r) < err);
				scale++;
				
				// For scale > 0, ensure we include all the base samples in the average
				i = ((i-1) | mask)+1;  
				while(count < i) sum += dummyData.data[count++];
				avg1 = sum/count;
				mask *= 2;
			}
			
			System.out.println(i);
		}
	}
	
	@Test
	public void testShortAddAtScale0() {
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(1);
		MultiscaleSeries series = group.get(0);
		series.addData(0, CHUNK_SIZE, dummyData.getStream(0, CHUNK_SIZE));
		
		float[] buffer = new float[CHUNK_SIZE];
		int scale = series.getData(0, CHUNK_SIZE, buffer);
		assertEquals(0, scale);
		
		for(int i=0;i<CHUNK_SIZE;i++) {
			assertEquals(""+i, dummyData.get(i), buffer[i], Float.MIN_VALUE);
		}
		
		buffer = new float[CHUNK_SIZE/2];
		scale = series.getData(0, CHUNK_SIZE, buffer);
		assertEquals(1, scale);

		for(int i=0;i<CHUNK_SIZE/2;i++) {
			float d1 = dummyData.get(i*2);
			float d2 = dummyData.get(i*2+1);
			assertEquals("@"+i+" d1: "+d1+" d2: "+d2, (d1+d2)/2, buffer[i], Float.MIN_VALUE);
		}
		
		buffer = new float[CHUNK_SIZE/3];
		scale = series.getData(0, buffer.length*3, buffer);
		assertEquals(1, scale);

		for(int i=0;i<buffer.length;i++) {
			double d1 = dummyData.get(i*3);
			double d2 = dummyData.get(i*3+1);
			double d3 = dummyData.get(i*3+2);
			double d4 = dummyData.get(i*3+3);
			
			float avg2 = (float) ((d1+d2)/3.0 + (d3+d4)/6.0);
			assertEquals("@"+i, avg2, buffer[i], 0.00001);

			i++;
			if (i >= buffer.length) break;
			
			double d0 = dummyData.get(i*3-1);
			d1 = dummyData.get(i*3);
			d2 = dummyData.get(i*3+1);
			d3 = dummyData.get(i*3+2);
			
			avg2 = (float) ((d0+d1)/6.0 + (d2+d3)/3.0);
			assertEquals("@"+i, avg2, buffer[i], 0.00001);
		}
	}
	
	/**
	 * Test for an {@link ArrayIndexOutOfBoundsException}
	 */
	@Test
	public void testAIOOB() {
		MultiscaleSeriesGroup group = new MultiscaleSeriesGroup(1);
		MultiscaleSeries series = group.get(0);
		series.addData(0, 1, dummyData.getStream(0, 64*1024));
		
		float[] buffer = new float[500];
		
		double x1 = 0.12147439778840458;
		double zoom = 237.37631379976963;
		series.getData(x1, x1 + 1.0/zoom, buffer);
		
		x1 = 0.6124545795216209;
		zoom = 4479449.738824559;
		series.getData(x1, x1 + 1.0/zoom, buffer);
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
