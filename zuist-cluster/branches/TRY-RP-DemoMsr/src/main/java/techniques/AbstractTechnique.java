package techniques;

import java.util.HashMap;


public abstract class AbstractTechnique {
	
	public enum ORDER {ZERO, FIRST}
	
	public static final boolean SHOW_STATS = false;
	
	protected ORDER order;
	protected String name;
	
	protected boolean cyclic = false;
	
	protected HashMap<String, Object> stats = new HashMap<String, Object>();
	
	/**
	 * Used to fill the stats arrays
	 */
	protected int current_zoom_stat_index = 0;
	protected int current_alt_stat_index = 0;
	
	/**
	 * Used for statistics (panning or zooming speed)
	 */
	protected long[] altitudeTimestamps;
	protected long[] zoomTimestamps;
	
	public AbstractTechnique(String id, ORDER o, boolean c) {
		
		this.name = id;
		this.order = o;
		this.cyclic = c;
		
	}
	
	public abstract void initListeners();
	public abstract void startListening();
	public abstract void stopListening();
	public abstract void close();
	
	public abstract void deleteStatLabels();
	
	
	public ORDER getOrder() {
		return order;
	}
	
	public Object getStat(String statName) {
		return stats.get(statName);
	}

}
