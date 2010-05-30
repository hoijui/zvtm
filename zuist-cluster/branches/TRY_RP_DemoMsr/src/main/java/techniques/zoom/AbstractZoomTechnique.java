package techniques.zoom;

import java.awt.Color;
import java.awt.geom.Point2D;

import fr.inria.zuist.cluster.viewer.Viewer;
import techniques.AbstractTechnique;
import fr.inria.zvtm.glyphs.VText;

public abstract class AbstractZoomTechnique extends AbstractTechnique {
	
	public enum ViewerTechnique {Push4_2H, Push4, Push3, Push2, Knob, Wheel,Wheel2Handed, Wheel2, Fist, Push, Mouse, TurningIPod, LinearIPod}
	
	/* ========================  STATS VARIABLES  =========================== */
	
	public static final int NB_SAMPLES_STATS = 20;
	
	public static final int[] STATS_AVG_WINDOWS = {1, 5, 10};
	public static final int[] STATS_MIN_MAX_WINDOWS = {1, 3, 5};
	
	public static final String AAVG = "altAvg", AMIN = "altMin", AMAX = "altMax";
	public static final String ZAVG = "zoomAvg", ZMIN = "zoomMin", ZMAX = "zoomMax";
	
	/**
	 * Used in combination with timestamps[] to measure zooming speed
	 */
	protected float[] altitudes;
	protected float[] zooms;
	
	protected float[] avg_altitudes = new float[STATS_AVG_WINDOWS.length];
	protected float[] min_altitudes = new float[STATS_MIN_MAX_WINDOWS.length];
	protected float[] max_altitudes = new float[STATS_MIN_MAX_WINDOWS.length];
	
	protected float[] avg_zooms = new float[STATS_AVG_WINDOWS.length];
	protected float[] min_zooms = new float[STATS_MIN_MAX_WINDOWS.length];
	protected float[] max_zooms = new float[STATS_MIN_MAX_WINDOWS.length];
	
	protected static VText[] averageAltLabels;
	protected static VText[] minAltLabels;
	protected static VText[] maxAltLabels;
	
	protected static VText[] averageViewerLabels;
	protected static VText[] minViewerLabels;
	protected static VText[] maxViewerLabels;
	
	protected static VText altLabel;
	protected static VText zoomLabel;
	
	/* ========================  END STATS VARIABLES  ======================= */
	
	
	
	
	/* 1-order zoom with absolute values from the VICON*/
	protected Point2D.Float refPoint = new Point2D.Float(0,0);
	
	public AbstractZoomTechnique(String id, ORDER o, boolean c) {
		super(id, o, c);
		
	}
	
	public static AbstractZoomTechnique createTechnique(String xmlName) {
		
		// TODO the names need to be cleaned
		
		ORDER order = null;
		
		if (xmlName.contains("0")) {
			order = ORDER.ZERO;
		} else if (xmlName.contains("1")) {
			order = ORDER.FIRST;
		} else {
			System.err.println("WARNING : unknown order. Couldn't create technique.");
		}
		
		if (order == null) {
			order = ORDER.ZERO;
		}

		//System.err.println("XML NAME: " + xmlName); 
		
		if (xmlName.contains(ViewerTechnique.Push4_2H.toString())) {
			
			return new Push4_2H(ViewerTechnique.Push4_2H.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT, true);
			
		} else if (xmlName.contains(ViewerTechnique.Push4.toString())) {
			
			return new Push4(ViewerTechnique.Push4.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT, true);
			
		} else if (xmlName.contains(ViewerTechnique.Push3.toString())) {
			
			return new Push3(ViewerTechnique.Push3.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT, true);
			
		} else if (xmlName.contains(ViewerTechnique.Push2.toString())) {
			
			return new Push2(ViewerTechnique.Push2.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT, true);
			
		} else if (xmlName.contains(ViewerTechnique.Knob.toString())) {
			
			return new Knob(ViewerTechnique.Knob.toString(), order);
			
		}else if (xmlName.contains(ViewerTechnique.Wheel2Handed.toString())) {
			
			return new TurningWheel2Handed(ViewerTechnique.Wheel2Handed.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT);
			
		}else if (xmlName.contains(ViewerTechnique.Wheel2.toString())) {
			
			return new TurningWheel2(ViewerTechnique.Wheel2.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT);
			
		} else if (xmlName.contains(ViewerTechnique.Wheel.toString()) && 
			   !xmlName.contains(ViewerTechnique.Mouse.toString())) {
			
			System.out.println("Creating TurningWheel with port " + Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT);
			return new TurningWheel(ViewerTechnique.Wheel.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT);
			
		} else if (xmlName.contains(ViewerTechnique.Fist.toString())) {
			
			return new Tilt(ViewerTechnique.Fist.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT);
			
		} else if (xmlName.contains(ViewerTechnique.Push.toString())) {
			
			return new Push(ViewerTechnique.Push.toString(), order, Viewer.DEFAULT_ZOOM_OSC_LISTENING_PORT);
			
		} else if (xmlName.contains(ViewerTechnique.Mouse.toString())) {
			
		    //System.err.println("Mouse: " + xmlName +"/"+ ViewerTechnique.Mouse.toString());
			return new Mouse(ViewerTechnique.Mouse.toString(), order);
			
		} else if (xmlName.contains(ViewerTechnique.TurningIPod.toString())) {
			
			// System.out.println("TurningIPod !!");
			return new TurningIPod(ViewerTechnique.TurningIPod.toString(), order);
			
		} else if (xmlName.contains(ViewerTechnique.LinearIPod.toString())) {
			
			return new LinearIPod(ViewerTechnique.LinearIPod.toString(), order, true);
			
		}
		
		System.err.println("WARNING : unknown technique name. Couldn't create technique.");
		return null;
		
	}
	
	protected void addAltitudeSample() {
		
		float alt = Viewer.getInstance().getMCamera().altitude;
		
		if (SHOW_STATS) {
			
			altLabel.setText("ALT : " + alt);
			
			current_alt_stat_index++;
			altitudeTimestamps[ current_alt_stat_index % NB_SAMPLES_STATS ] = System.currentTimeMillis();
			altitudes[ current_alt_stat_index % NB_SAMPLES_STATS ] = alt;
			
			computeAltitudeAverages();
			computeAltitudeMinMaxSpeeds();
			
			updateVisibleStats();
			
		}

	}
	
	protected void addViewerSample(float zoom) {
		
		if (SHOW_STATS) {
		
			// Platform.getInstance().setMeasureValue("zoom", new Float(zoom) );
			// Platform.getInstance().getCinematicLogger().log();
			altLabel.setText("ZOOM : " + zoom);
			
			current_zoom_stat_index++;
			zoomTimestamps[ current_zoom_stat_index % NB_SAMPLES_STATS ] = System.currentTimeMillis();
			zooms[ current_zoom_stat_index % NB_SAMPLES_STATS ] = zoom;
			
			computeViewerAverages();
			computeViewerMinMaxSpeeds();
			
			updateVisibleStats();
			
		}
		
	}
	
	protected void computeAltitudeAverages() {
		
		if (SHOW_STATS) {
		
			/*
			 * The speeds are computed between two consecutive values.
			 * We then compute averages from different amounts of these speeds (starting from the last one). 
			 */
			
			for (int i = 0 ; i < STATS_AVG_WINDOWS.length ; i++) {
				
				float average = 0;
				int windowSize = Math.min(STATS_AVG_WINDOWS[i], current_alt_stat_index);
				
				for (int j = current_alt_stat_index ; j > current_alt_stat_index - windowSize ; j--) {
					
					int index = (j + altitudes.length) % altitudes.length; // So that it doesn't go below 0.
					int previousIndex = (j - 1 + altitudes.length) % altitudes.length; 
					
					average += (altitudes[index] - altitudes[previousIndex]) / (altitudeTimestamps[index] - altitudeTimestamps[previousIndex]);
					
				}
				
				average /= windowSize;
				
				if (
						average != Float.NaN
						&& average != 0f
						&& average != Float.POSITIVE_INFINITY
						&& average != Float.NEGATIVE_INFINITY
						
						&& windowSize == STATS_AVG_WINDOWS[i]
				) {
				
					avg_altitudes[i] = average;
					
					stats.put(AAVG + STATS_AVG_WINDOWS[i], new Float(average));
					
				}
				
			}
			
		}
		
	}
	
	protected void computeAltitudeMinMaxSpeeds() {
		
		if (SHOW_STATS) {
			
			/*
			 * The speeds are computed between two consecutive values.
			 * We then compute speed averages from different amounts of altitudes (starting from the last one).
			 * Then we update min and max values.
			 */
			
			for (int i = 0 ; i < STATS_MIN_MAX_WINDOWS.length ; i++) {
				
				float average = 0;
				int windowSize = Math.min(STATS_MIN_MAX_WINDOWS[i], current_alt_stat_index);
				
				String data = "";
				
				for (int j = current_alt_stat_index ; j > current_alt_stat_index - windowSize ; j--) {
					
					int index = (j + altitudes.length) % altitudes.length; // So that it doesn't go below 0.
					int previousIndex = (j - 1 + altitudes.length) % altitudes.length; 
					
					average += (altitudes[index] - altitudes[previousIndex]) / (altitudeTimestamps[index] - altitudeTimestamps[previousIndex]);
					
					data += "" + ((altitudes[index] - altitudes[previousIndex]) / (altitudeTimestamps[index] - altitudeTimestamps[previousIndex])) + " - ";
					
				}
				
				average /= windowSize;
				
				average = Math.abs(average);
				
				if (
						average != Float.NaN
						&& average != 0f
						&& average != Float.POSITIVE_INFINITY
						
						&& windowSize == STATS_MIN_MAX_WINDOWS[i]
				) {
				
					if (average > max_altitudes[i]) {
						max_altitudes[i] = average;
						stats.put(AMAX + STATS_MIN_MAX_WINDOWS[i], new Float(average));
						
						// System.out.println("max : " + data + " => " + average);
					}
					
					if (average < min_altitudes[i]) {
						min_altitudes[i] = average;
						stats.put(AMIN + STATS_MIN_MAX_WINDOWS[i], new Float(average));
						
						// System.out.println("min : " + data + " => " + average);
					}
				}
				
			}
		}
		
	}
	
	protected void computeViewerAverages() {
		
		if (SHOW_STATS) {
			
			/*
			 * The speeds are computed between two consecutive values.
			 * We then compute averages from different amounts of these speeds (starting from the last one). 
			 */
			
			for (int i = 0 ; i < STATS_AVG_WINDOWS.length ; i++) {
				
				float average = 0;
				int windowSize = Math.min(STATS_AVG_WINDOWS[i], current_zoom_stat_index);
				
				for (int j = current_zoom_stat_index ; j > current_zoom_stat_index - windowSize ; j--) {
					
					int index = (j + zooms.length) % zooms.length; // So that it doesn't go below 0.
					int previousIndex = (j - 1 + zooms.length) % zooms.length; 
					
					average += (zooms[index] - zooms[previousIndex]) / (zoomTimestamps[index] - zoomTimestamps[previousIndex]);
					
				}
				
				average /= windowSize;
				
				if (
						average != Float.NaN
						&& average != 0f
						&& average != Float.POSITIVE_INFINITY
						&& average != Float.NEGATIVE_INFINITY
						
						&& windowSize == STATS_AVG_WINDOWS[i]
				) {
				
					avg_zooms[i] = average;
					
					stats.put(ZAVG + STATS_AVG_WINDOWS[i], new Float(average));
					
				}
				
			}
		}
		
	}
	
	protected void computeViewerMinMaxSpeeds() {
		
		if (SHOW_STATS) {
			
			/*
			 * The speeds are computed between two consecutive values.
			 * We then compute speed averages from different amounts of altitudes (starting from the last one).
			 * Then we update min and max values.
			 */
			
			for (int i = 0 ; i < STATS_MIN_MAX_WINDOWS.length ; i++) {
				
				float average = 0;
				int windowSize = Math.min(STATS_MIN_MAX_WINDOWS[i], current_zoom_stat_index);
				
				String data = "";
				
				for (int j = current_zoom_stat_index ; j > current_zoom_stat_index - windowSize ; j--) {
					
					int index = (j + zooms.length) % zooms.length; // So that it doesn't go below 0.
					int previousIndex = (j - 1 + zooms.length) % zooms.length; 
					
					average += (zooms[index] - zooms[previousIndex]) / (zoomTimestamps[index] - zoomTimestamps[previousIndex]);
					
					data += "" + ((zooms[index] - zooms[previousIndex]) / (zoomTimestamps[index] - zoomTimestamps[previousIndex])) + " - ";
					
				}
				
				average /= windowSize;
				
				average = Math.abs(average);
				
				if (
						average != Float.NaN
						&& average != 0f
						&& average != Float.POSITIVE_INFINITY
						
						&& windowSize == STATS_MIN_MAX_WINDOWS[i]
				) {
				
					if (average > max_zooms[i]) {
						max_zooms[i] = average;
						stats.put(ZMAX + STATS_MIN_MAX_WINDOWS[i], new Float(average));
						
						// System.out.println("max : " + data + " => " + average);
					}
					
					if (average < min_zooms[i]) {
						min_zooms[i] = average;
						stats.put(ZMIN + STATS_MIN_MAX_WINDOWS[i], new Float(average));
						
						// System.out.println("min : " + data + " => " + average);
					}
				}
				
			}
		}
		
	}
	
	protected void updateVisibleStats() {
		
		if (SHOW_STATS) {
			
			for (int i = 0 ; i < STATS_AVG_WINDOWS.length ; i++) {
				
				averageAltLabels[i].setText( "AVG(" + STATS_AVG_WINDOWS[i] + ") : " + Float.toString(avg_altitudes[i]) );
				averageViewerLabels[i].setText( "AVG(" + STATS_AVG_WINDOWS[i] + ") : " + Float.toString(avg_zooms[i]) );
				
			}
			
			for (int i = 0 ; i < STATS_MIN_MAX_WINDOWS.length ; i++) {
				
				minAltLabels[i].setText( "MIN(" + STATS_MIN_MAX_WINDOWS[i] + ") : " + Float.toString(min_altitudes[i]) );
				maxAltLabels[i].setText( "MAX(" + STATS_MIN_MAX_WINDOWS[i] + ") : " + Float.toString(max_altitudes[i]) );
				
				minViewerLabels[i].setText( "MIN(" + STATS_MIN_MAX_WINDOWS[i] + ") : " + Float.toString(min_zooms[i]) );
				maxViewerLabels[i].setText( "MAX(" + STATS_MIN_MAX_WINDOWS[i] + ") : " + Float.toString(max_zooms[i]) );
				
			}
		}
		
	}
	
	@Override
	public void deleteStatLabels() {
		
		
	}

}
