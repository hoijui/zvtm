package utils.pointedscreen;

import java.awt.Dimension;

public class DisplayType {
	
	private String name;
	private Dimension resolution = new Dimension();
	private double width = 0, height = 0;
	private double hBorder = 0, vBorder = 0;
	
	public DisplayType(String name, double width, double height,
			double horizontalBorder, double verticalBorder, Dimension resolution) {
		this.name = name;
		this.width = width;
		this.height = height;
		this.hBorder = horizontalBorder;
		this.vBorder = verticalBorder;
		if (resolution != null)
			this.resolution = resolution;
	}
	
	public String getName() {
		return name;
	}
	
	public Dimension getResolution() {
		return resolution;
	}
	
	public double getWidth() {
		return width;
	}
	
	public double getHeight() {
		return height;
	}
	
	public double getHorizontalBorder() {
		return hBorder;
	}
	
	public double getVerticalBorder() {
		return vBorder;
	}

}
