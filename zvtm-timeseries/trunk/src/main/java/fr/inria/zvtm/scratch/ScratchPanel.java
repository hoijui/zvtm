package fr.inria.zvtm.scratch;

import java.awt.Graphics;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class ScratchPanel extends JPanel {
	private static final int CHUNK_SIZE = 1024;
	private static final int DATA_SIZE = 256*1024;
	private static final int COUNT = 500;
	private static final int VERTICAL_ZOOM = 1;
	private MultiscaleSeries[] series = new MultiscaleSeries[COUNT];
	private float[][] buffers = new float[COUNT][];
	
	private double zoom = 1;
	private double x1 = 0;
	
	private float minValue = Float.MAX_VALUE;
	private float maxValue = -Float.MAX_VALUE;
	
	private BufferedImage image;
	private MultiscaleSeriesGroup group;
	
	private static final int THREADS = 16;
	private ExecutorService executor = Executors.newFixedThreadPool(THREADS);
	
	private AtomicInteger created = new AtomicInteger();
	
	public ScratchPanel() {
		group = new MultiscaleSeriesGroup();
		for(int i=0;i<COUNT;i++) series[i] = new MySeries(group.getCache());
		group.setSeries(series);
		
		try {
			List<CreateDataTask> tasks = new ArrayList<CreateDataTask>(THREADS);
			for(int i=0;i<THREADS;i++) tasks.add(new CreateDataTask(i));
			executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		assert created.intValue() == COUNT;

		addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if ((e.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) != 0) {
					x1 += 1.0*e.getWheelRotation() / (10.0*zoom);
				} else {
					int i = e.getX();
					int w = getWidth();
					double z0 = zoom;
					zoom = z0 * Math.pow(1.2, -e.getWheelRotation());
					if (zoom < 1) zoom = 1;
					if (zoom > 1000000) zoom = 1000000;
					
					x1 = x1 + i/(z0*w) - i/(zoom*w); 
				}
				
				repaint();
			}
		});
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		updateImage(getWidth());
		g.drawImage(image, 
				0, 0, getWidth(), getHeight(),
				0, 0, getWidth(), getHeight()/VERTICAL_ZOOM,
				null);
	}
		
	private void updateImage(int w) {
		if (image == null || w != image.getWidth()) {
			byte[] red = new byte[256];
			byte[] green = new byte[256];
			byte[] blue = new byte[256];
			
			for(int i=0;i<255;i++) blue[i] = (byte) i;
			red[255] = (byte) 255;
			
			IndexColorModel cm = new IndexColorModel(8, 256, red, green, blue);
			image = new BufferedImage(w, COUNT, BufferedImage.TYPE_BYTE_INDEXED, cm);
			
			for(int i=0;i<COUNT;i++) {
				buffers[i] = new float[w];
			}			
		}
		
		double x2 = x1 + 1.0/zoom;
		System.out.println("x1: "+x1+", x2: "+x2+", dx: "+(x2-x1)+", zoom: "+zoom);

		long t0 = System.currentTimeMillis();
		
		try {
			List<GetDataTask> tasks = new ArrayList<GetDataTask>(THREADS);
			for(int i=0;i<THREADS;i++) tasks.add(new GetDataTask(group, i, buffers, x1, x2));
			executor.invokeAll(tasks);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		long t1 = System.currentTimeMillis();

		WritableRaster raster = image.getRaster();
		for(int i=0;i<COUNT;i++) {
			for(int j=0;j<w;j++) {
				float v = buffers[i][j];
				int c;
				if (Float.isNaN(v)) c = 255;
				else c = (int) (254f*(v-minValue)/(maxValue-minValue));
				raster.setSample(j, i, 0, c);
			}
		}
		
		long t2 = System.currentTimeMillis();
		System.out.println("getData: "+(t1-t0)+"ms, update image: "+(t2-t1)+"ms");
	}
	
	/**
	 * This task creates the initial data for a subset of the series group.
	 * @author gpothier
	 */
	private class CreateDataTask implements Runnable, Callable<Void> {
		private int rank;
		
		public CreateDataTask(int rank) {
			this.rank = rank;
		}

		@Override
		public void run() {
			System.out.println("Thread "+rank+" started");
			int createdHere = 0;
			float[] data = new float[DATA_SIZE];
			Random r = new Random();

			int groups = (COUNT + (THREADS-1))/THREADS;
			for(int i=0;i<groups;i++) {
				int id = rank*groups+i;
				if (id >= COUNT) {
					System.out.println("Skipping: "+id);
					continue;
				}
				
				data[0] = r.nextFloat();
				data[data.length-1] = r.nextFloat();
				plasma(r, data, 0, data.length-1);
				
				series[id].addData(0, 1, data);
				
				for(int j=0;j<data.length;j++) {
					float v = data[j];
					if (v < minValue) minValue = v;
					if (v > maxValue) maxValue = v;
				}
				
				System.out.println(created.incrementAndGet() + " - "+rank+" => "+id);
				createdHere++;
			}
			System.out.println("Thread "+rank+" created "+createdHere+" series");
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
	
	/**
	 * Recursively fills the buffer with random data from positions
	 * i1 to i2, both inclusive.
	 */
	private static void plasma(Random r, float[] data, int i1, int i2) {
		assert i1 < i2;
		if (i2-i1 <= 1) return;
		
		int mid = (i1+i2)/2;
		float avg = (data[i1] + data[i2])/2f;
		data[mid] = avg + (r.nextFloat()-0.5f);
		plasma(r, data, i1, mid);
		plasma(r, data, mid, i2);
	}
	
	/**
	 * This task obtains the scaled data from a subset of the series group.
	 * @author gpothier
	 */
	private static class GetDataTask implements Runnable, Callable<Void> {
		private MultiscaleSeriesGroup group;
		private int rank;
		private float[][] buffers;
		private double x1;
		private double x2;
		
		public GetDataTask(MultiscaleSeriesGroup group, int rank, float[][] buffers, double x1, double x2) {
			this.group = group;
			this.rank = rank;
			this.buffers = buffers;
			this.x1 = x1;
			this.x2 = x2;
		}
		
		@Override
		public void run() {
			int groups = (COUNT + (THREADS-1))/THREADS;
			for(int i=0;i<groups;i++) {
				int id = rank*groups+i;
				if (id >= COUNT) continue;
				group.get(id).getData(x1, x2, buffers[id]);
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
	
	/**
	 * An implementation of {@link DynamicMultiscaleSeries} that 
	 * generates missing data using the plasma method, based on data
	 * at higher scales.
	 * @author gpothier
	 */
	private class MySeries extends DynamicMultiscaleSeries {
		public MySeries(IChunkCache cache) {
			super(cache, CHUNK_SIZE);
		}

		@Override
		protected IDataStream fetch(int scale, double x1, double x2) {
			double ss = Math.pow(2, scale);
			
			float[] data = new float[(int)((x2-x1)/ss)];
			System.out.println("Fetching ["+getId()+"]: "+scale+", l: "+data.length+" - "+x1+", "+x2);
			
			Random r = new Random();
			
			double x = x1;
			while(x<x2) {
				int o = (int) (x/(ss*chunkSize));
				System.out.println("  gen: offset "+o+", x: "+x);
				DataChunk lowestChunk = getLowestChunk(scale, o);
				// Copy higher chunk data into new buffer
				assert lowestChunk.scale > scale;
				int n = 1 << (lowestChunk.scale - scale);
				int d = (int) (1.0*chunkSize*(1.0*o/n - lowestChunk.offset));
				
				int lastJ = -1;
				for(int i=0;i<chunkSize;i++) {
					int j = (i-d)*n;
					if (j >= 0 && j < data.length) {
						data[j] = lowestChunk.get(i);
						if (lastJ >= 0) plasma(r, data, lastJ, j);
						lastJ = j;
					}
				}
				x = Math.pow(2, lowestChunk.scale)*(lowestChunk.offset+1)*chunkSize;
			}
			System.out.println("Done fetching");

			return new ArrayDataStream(data);
		}	
		
		@Override
		protected void rangeUpdated(double x1, double x2) {
			repaint();
		}
	}
	
	public static void main(String[] args) {
		JFrame frame = new JFrame("Timescale");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setContentPane(new ScratchPanel());
		frame.setSize(500, 500);
		frame.setVisible(true);
	}
}
