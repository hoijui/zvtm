package fr.inria.zvtm.gui;

import java.awt.Color;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.Picker;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;

public class PCursor {
	private Glyph cursorX;
	private Glyph cursorY;
	private double vx;
	private double vy;
	private Picker picker;
	private Camera mCamera;
	private Camera localCamera;
	private ViewListener vl;

	public PCursor(VirtualSpace mSpace,Color c,Camera cam,Camera locCam, ViewListener eh){
		cursorX = new VRectangle(0, 0, 1, 20, 1, c);
		cursorX.setBorderColor(c);
		cursorX.setVisible(true);
		mSpace.addGlyph(cursorX);
		cursorX.setSensitivity(false);
		cursorY = new VRectangle(0, 0, 1, 1, 20, c);
		cursorY.setBorderColor(c);
		cursorY.setVisible(true);
		mSpace.addGlyph(cursorY);
		cursorY.setSensitivity(false);
		picker = new Picker();
		this.mCamera = cam;
		this.localCamera = locCam;
		this.vl = eh;
		
	}

	public void moveCursorTo(double x, double y, int jpx, int jpy){
		this.vx = x;
		this.vy = y;
		cursorX.moveTo(x, y);
		cursorY.moveTo(x, y);
		picker.setVSCoordinates(x, y);
		picker.setJPanelCoordinates(jpx, jpy);
		picker.computePickedGlyphList(vl, localCamera);
		picker.computePickedGlyphList(vl, mCamera);
	}



	public Picker getPicker() {
		return picker;
	}

	public void setColor(Color clientId){
		cursorX.setBorderColor(clientId);
		cursorX.setColor(clientId);
		cursorY.setBorderColor(clientId);
		cursorY.setColor(clientId);
	}

	public double getVSXCoordinate(){
		return vx;
	}
	public double getVSYCoordinate(){
		return vy;
	}

}
