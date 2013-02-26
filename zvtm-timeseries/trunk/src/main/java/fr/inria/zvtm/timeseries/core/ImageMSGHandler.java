package fr.inria.zvtm.timeseries.core;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A utility class that can generate an image from an {@link MultiscaleSeriesGroup}.
 * 
 * @author gpothier
 *
 */
public class ImageMSGHandler {
	private static final int THREADS = 4;
	
	private final MultiscaleSeriesGroup group;
	private final float[][] buffers;
	private BufferedImage image;
	private final ExecutorService executor = Executors.newFixedThreadPool(THREADS);

	public ImageMSGHandler(MultiscaleSeriesGroup group) {
		this.group = group;
		buffers = new float[THREADS][];
	}

	/**
	 * Generates an image of the series in a given range.
	 * The image will be reused as long as the width does not change. 
	 * @param width Desired width of the image 
	 * @param x1 Range start
	 * @param x2 Range end
	 * @param minValue Minimum value in the dataset
	 * @param maxValue Maximum value in the dataset
	 */
	public BufferedImage generateImage(int width, double x1, double x2, double minValue, double maxValue) {
		if (image == null || width != image.getWidth()) {
			byte[] red = new byte[256];
			byte[] green = new byte[256];
			byte[] blue = new byte[256];
			
			for(int i=0;i<255;i++) blue[i] = (byte) i;
			red[255] = (byte) 255;
			
			IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);
			image = new BufferedImage(width, group.getSize(), BufferedImage.TYPE_BYTE_INDEXED, cm);
			
			for(int i=0;i<THREADS;i++) {
				buffers[i] = new float[width];
			}			
		}
		
		long t0 = System.currentTimeMillis();
		
		try {
			WritableRaster raster = image.getRaster();
			List<GetDataTask> tasks = new ArrayList<GetDataTask>(THREADS);
			for(int i=0;i<THREADS;i++) tasks.add(new GetDataTask(group, i, buffers[i], raster, x1, x2, minValue, maxValue));
			executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		long t1 = System.currentTimeMillis();
		System.out.println("getData: "+(t1-t0)+"ms");
		
		return image;
	}
	
	/**
	 * This task obtains the scaled data from a subset of the series group.
	 * @author gpothier
	 */
	private class GetDataTask implements Runnable, Callable<Void> {
		private MultiscaleSeriesGroup group;
		private int rank;
		private float[] buffer;
		private WritableRaster raster;
		private double x1;
		private double x2;
		private double minValue;
		private double maxValue;
		
		public GetDataTask(
				MultiscaleSeriesGroup group, 
				int rank, 
				float[] buffer,
				WritableRaster raster,
				double x1, double x2,
				double minValue, double maxValue) {
			this.group = group;
			this.rank = rank;
			this.buffer = buffer;
			this.raster = raster;
			this.x1 = x1;
			this.x2 = x2;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		@Override
		public void run() {
			int groups = (group.getSize() + (THREADS-1))/THREADS;
			for(int i=0;i<groups;i++) {
				int id = rank*groups+i;
				if (id >= group.getSize()) continue;
				
				group.get(id).getData(x1, x2, buffer);
				
				for(int j=0;j<buffer.length;j++) {
					float v = buffer[j];
					int c;
					if (Float.isNaN(v)) c = 255;
					else c = (int) (254f*(v-minValue)/(maxValue-minValue));
					raster.setSample(j, id, 0, c);
				}
			}
		}

		@Override
		public Void call() throws Exception {
			try {
				run();
			} catch (Throwable e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	
}
