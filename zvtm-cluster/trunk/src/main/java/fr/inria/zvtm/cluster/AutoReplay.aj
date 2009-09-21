package fr.inria.zvtm.cluster;

import fr.inria.zvtm.glyphs.Glyph;

import java.awt.Color;

/**
 * Define methods that will be replayed automatically
 * on remote virtual spaces.
 * Autoreplay is a quick way of propagating changes to
 * remote objects without writing Delta classes.
 * Use only for "atomic" operations (change one attribute at a time).
 * Likely candidates are Glyph.setStrokeWidth(float),
 * Glyph.setMouseInsideHighlightColor(Color).
 * Bad candidates includes Glyph.setLocation(Location)
 */
public aspect AutoReplay {
	pointcut glyphAutoReplayMethods(Glyph glyph) : 
		this(glyph) && (
		execution(public * Glyph.setStrokeWidth(float))	||
		execution(public * Glyph.setMouseInsideHighlightColor(Color)) ||
		execution(public * Glyph.setVisible(boolean))
		)
		;

	after(Glyph glyph) : glyphAutoReplayMethods(glyph) {
		//create a delta message, supplying the Glyph's object
		//id and method argument.
	}
}

