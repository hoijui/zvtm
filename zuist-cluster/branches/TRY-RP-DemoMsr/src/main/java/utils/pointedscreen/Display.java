package utils.pointedscreen;



import java.awt.Point;
import java.awt.Rectangle;



public class Display {
	
	private String name;
	
	private DisplayType type;
	
	private Rectangle localBounds;//bounds of this display in its local screen configuration
	private Rectangle globalBounds;//bounds of this display in the wall coordinates system
	
	private LocalSystem ls;
	
	/***
	 * Creates a display with a display type, a machine, its local and global locations and its calibration data
	 * 
	 * @param name
	 * @param type
	 * @param machine
	 * @param localX
	 * @param localY
	 * @param globalX
	 * @param globalY
	 * @param width
	 * @param height
	 * @param calibration
	 */
	public Display(String name, DisplayType type,
			int localX, int localY, int globalX, int globalY,
			LocalSystem calibration) {
		this.name = name;
		this.type = type;
		int width = type.getResolution().width;
		int height = type.getResolution().height;
		localBounds = new Rectangle(localX, localY, width, height);
		globalBounds = new Rectangle(globalX, globalY, width, height);
		this.ls = calibration;
	}

	public String getName() {
		return name;
	}
	
	public DisplayType getType() {
		return type;
	}

	public boolean contains(int x, int y) {
		if (globalBounds.contains(x, y))
			return true;
		return false;
	}
	
	public Point toLocal(Point p) {
		return toLocal(p.x, p.y);
	}
	
	public Point toLocal(int x, int y) {
		Point ret = new Point();
		int lx = x-globalBounds.x;
		int ly = y-globalBounds.y;
		ret.setLocation(lx, ly);
		return ret;
	}
	
	public int toLocalX(int x) {
		return x - globalBounds.x;
	}

	public int toLocalY(int y) {
		return y - globalBounds.y;
	}
	
	public Point toLocalConfig(Point p) {
		return toLocalConfig(p.x, p.y);
	}
	
	public Point toLocalConfig(int x, int y) {
		Point ret = new Point();
		int lx = x-globalBounds.x;
		int ly = y-globalBounds.y;
		ret.setLocation(lx + localBounds.x, ly + localBounds.y);
		return ret;
	}
	
	public int toLocalConfigX(int x) {
		return x - globalBounds.x + localBounds.x;
	}

	public int toLocalConfigY(int y) {
		return y - globalBounds.y + localBounds.y;
	}
	
	public Rectangle getGlobalBounds() {
		return globalBounds;
	}

	public Rectangle getLocalBounds() {
		return localBounds;
	}
	
	public LocalSystem getLocalSystem() {
		return ls;
	}

}
