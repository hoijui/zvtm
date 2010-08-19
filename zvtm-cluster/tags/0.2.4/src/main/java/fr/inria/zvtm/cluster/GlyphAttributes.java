package fr.inria.zvtm.cluster;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.Serializable;

import fr.inria.zvtm.glyphs.Glyph;

//a serializable vessel to move the main Glyph attributes
class GlyphAttributes implements Serializable {
	private final double x;
	private final double y;
	//private final int z; ??
	private final double orientation;
	private final Color borderColor;
	private final Color mainColor;
	private final boolean sensitive;
	private final boolean visible;
	private final float strokeWidth;
	private final float translucency;

	private GlyphAttributes(double x, double y, 
			double orientation,
			Color borderColor, Color mainColor,
			boolean sensitive,
			boolean visible,
			float strokeWidth,
			float translucency){
		this.x = x;
		this.y = y;
		this.orientation = orientation;
		this.borderColor = borderColor;
		this.mainColor = mainColor;
		this.sensitive = sensitive;
		this.visible = visible;
		this.strokeWidth = strokeWidth;
		this.translucency = translucency;
	}

	static GlyphAttributes fromGlyph(Glyph glyph){
		Point2D.Double loc = glyph.getLocation();
		return new GlyphAttributes(loc.x, loc.y,
				glyph.getOrient(),
				glyph.getBorderColor(), glyph.getDefaultColor(),
				glyph.isSensitive(),
				glyph.isVisible(),
				glyph.getStrokeWidth(),
				glyph.getTranslucencyValue());
	}

	void moveAttributesToGlyph(Glyph target){
		target.moveTo(x,y);
		target.orientTo(orientation);
		target.setBorderColor(borderColor);
		target.setColor(mainColor);
		target.setSensitivity(sensitive);
		target.setVisible(visible);
		target.setStrokeWidth(strokeWidth);
		target.setTranslucencyValue(translucency);
	}
}

