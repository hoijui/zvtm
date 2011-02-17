/*   FILE: VCursor.java
 *   DATE OF CREATION:   Jul 11 2000
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 *   Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2011. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.AlphaComposite;
import java.awt.geom.GeneralPath;

import java.util.Vector;
import java.util.HashMap;
import java.util.Set;
import java.util.Arrays;

import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.Translucent;

/* For DynaSpot */
import java.util.Timer;
import java.util.TimerTask;
import java.awt.geom.Point2D;
import java.awt.geom.Ellipse2D;
import fr.inria.zvtm.event.DynaSpotListener;
import fr.inria.zvtm.event.SelectionListener;
import fr.inria.zvtm.glyphs.Translucency;

/**
 * Glyph representing pointing device cursor. One instance per view.
 * @author Emmanuel Pietriga
 *
 * <h4>Using DynaSpot</h4>
 * <p>The DynaSpot behavior must be activated in VCursor, calling</p>
 * <ul><li>VCursor.activateDynaSpot(boolean b)</li></ul>
 * 
 * <p>In your ViewListener, simply call VCursor.dynaPick(Camera c) wherever this makes sense. Usually this will be mouseMoved(...):</p>
 * <ul>
 *  <li>v.getMouse().dynaPick(c); // where c is the active camera</li>
 * </ul>
 * <p>This updates the list of glyphs intersected by the DynaSpot disc, and
 *    identifies the one glyph actually selected (which is returned). The method
 *    also takes care of highlighting/unhighlighting the selected glyph.</p>
 * <p><strong>Note:</strong> dynaPick() also gets called internally when DynaSpot's size changes.</p>
 */

public class VCursor {

    /**cursor color*/
    Color color;

    /**color of geometrical hints associated with cursor (drag segment, selection rectangle, etc.)*/
    Color hcolor;

    /**tells whether a cross should be drawn at cursor pos or not*/
    boolean isVisible=true;

    /**tells whether we should detect entry/exit in glyphs*/
    boolean sensit=true;

    /**sync VTM cursor and system cursor if true*/
    boolean sync;

    /**coord in camera space (same as jpanel coords, but conventional coord sys at center of view panel, upward)*/
    protected float cx,cy;
    /**coord in virtual space*/
    protected double vx,vy;
    /**previous coords in virtual space*/
    protected double pvx,pvy;
    /**coords in JPanel*/
    protected int jpx,jpy;
    /**gain for cursor unprojection w.r.t lens (if any lens is set)*/
    float[] gain = new float[2];

    Picker picker;

    /**glyphs sticked to the mouse cursor*/
    Glyph[] stickedGlyphs;

    /**view to which this cursor belongs*/
    View owningView;

    /* crosshair size */
    int size = 10;

    VCursor(View v){
        this.owningView=v;
        picker = new Picker();
        vx=0;pvx=0;
        vy=0;pvy=0;
        cx=0;
        cy=0;
        jpx=0;
        jpy=0;
        color=Color.black;
        hcolor = Color.black;
        stickedGlyphs = new Glyph[0];
        sync=true;
        computeDynaSpotParams();
        setSelectionListener(new DefaultSelectionAction());
    }

    /** Set cursor size (crosshair length). */
    public void setSize(int s){
	    this.size = s;
    }

    /** Get cursor size (crosshair length).*/
    public int getSize(){
	    return size;
    }

    /** Get the cursor's location in virtual space (for active layer/camera). */
    public Point2D.Double getLocation(){return new Point2D.Double(vx,vy);}

    /** Get view to which this cursor belongs. */
    public View getOwningView(){return owningView;}

    /** Set whether this ZVTM cursor is synchronized with the system cursor or not. */
    public void setSync(boolean b){
	    sync = b;
    }
    
    /** Tells whether this ZVTM cursor is synchronized with the system cursor or not. */
    public boolean getSync(){
        return sync;
    }

    /** Set cursor color. */
    public void setColor(Color c){
	    this.color = c;
    }

    /** Set color of elements associated with cursor (drag segment, selection rectangle, etc.). */
    public void setHintColor(Color c){
	    this.hcolor = c;
    }
    
    /** Move mouse cursor (JPanel coordinates).
     *@param x x-coordinate, in JPanel coordinates system
     *@param y y-coordinate, in JPanel coordinates system
     */
    public void setJPanelCoordinates(int x, int y){
        if (sync){
            jpx = x;
            jpy = y;
            picker.setJPanelCoordinates(jpx, jpy);
        }
    }

    /** Propagate cursor movements to sticked glyphs. */
    public void propagateMove(){
        for (int i=0;i<stickedGlyphs.length;i++){
            stickedGlyphs[i].move(vx-pvx, vy-pvy);
        }
    }

    /** Attach glyph g to cursor. */
	public void stickGlyph(Glyph g){
		if (g==null){return;}
		//make it unsensitive (was automatically disabled when glyph was sticked to mouse)
		//because false enter/exit events can be generated when moving the mouse too fast
		//in small glyphs   (I did not find a way to correct this bug yet)
		g.setSensitivity(false);
		Glyph[] newStickList = new Glyph[stickedGlyphs.length + 1];
		System.arraycopy(stickedGlyphs, 0, newStickList, 0, stickedGlyphs.length);
		newStickList[stickedGlyphs.length] = g;
		stickedGlyphs = newStickList;
		g.stickedTo = this;
	}

    /** Unstick glyph that was last sticked to mouse.
     * The glyph is automatically made sensitive to mouse events.
     * The number of glyphs sticked to the mouse can be obtained by calling VCursor.getStickedGlyphsNumber().
     */
	public Glyph unstickLastGlyph(){
		if (stickedGlyphs.length>0){
			Glyph g = stickedGlyphs[stickedGlyphs.length - 1];
			g.setSensitivity(true);  //make it sensitive again (was automatically disabled when glyph was sticked to mouse)
			g.stickedTo = null;
			Glyph[] newStickList = new Glyph[stickedGlyphs.length - 1];
			System.arraycopy(stickedGlyphs, 0, newStickList, 0, stickedGlyphs.length - 1);
			stickedGlyphs = newStickList;
			return g;
		}
		return null;
	}

    /** Get the number of glyphs sticked to the cursor. */
    public int getStickedGlyphsNumber(){return stickedGlyphs.length;}

    /** Detach glyph from cursor. */
    void unstickSpecificGlyph(Glyph g){
        for (int i=0;i<stickedGlyphs.length;i++){
            if (stickedGlyphs[i] == g){
                g.stickedTo = null;
                Glyph[] newStickList = new Glyph[stickedGlyphs.length - 1];
                System.arraycopy(stickedGlyphs, 0, newStickList, 0, i);
                System.arraycopy(stickedGlyphs, i+1, newStickList, i, stickedGlyphs.length-i-1);
                stickedGlyphs = newStickList;
                break;
            }
        }
    }

    /** Get list of glyphs sticked to cursor.
     *@return the actual list, not a copy.
     */
    public Glyph[] getStickedGlyphArray(){
	    return stickedGlyphs;
    }

    /** Should the cursor glyph be drawn or not. */
    public void setVisibility(boolean b){
	    isVisible = b;
    }

    /** Enable/disable entry/exit of cursor into/from glyphs. */
    public void setSensitivity(boolean b){
	    sensit = b;
    }

    /** Tells whether entry/exit of cursor into/from glyphs is enabled or disabled. */
    public boolean isSensitive(){return sensit;}

    /** Unproject the cursor from JPanel coordinates to VirtualSpace coordinates. */
    public void unProject(Camera c, ViewPanel v){
        if (sync && v.size != null){
            //translate from JPanel coords
            if (v.lens != null){
                //take lens into account (if set)
                v.lens.gf(jpx, jpy, gain);
                // take lens focus offset into account only when above threshold as the offset is not taken into account during rendering when below threshold
                // jpx - v.size.width/2 = cx when no lens
                cx = (gain[0] >= v.lens.getBufferThreshold()) ? v.lens.lx + (jpx+v.lens.getXfocusOffset() - v.size.width/2 - v.lens.lx) / gain[0] : v.lens.lx + (jpx - v.size.width/2 - v.lens.lx) / gain[0];
                // v.size.height/2 - jpy = cy when no lens
                cy = (gain[1] >= v.lens.getBufferThreshold()) ? (v.lens.ly + v.size.height/2 - jpy-v.lens.getYfocusOffset()) / gain[1] - v.lens.ly : (v.lens.ly + v.size.height/2 - jpy) / gain[1] - v.lens.ly;
            }
            else {
                cx = jpx - v.size.width/2;
                cy = v.size.height/2 - jpy;
            }
            double ucoef = (c.focal+c.altitude) / c.focal;
            //find coordinates of object's geom center wrt to camera center and project IN VIRTUAL SPACE
            pvx = vx;
            pvy = vy;
            vx = (cx*ucoef) + c.vx;
            vy = (cy*ucoef) + c.vy;
            picker.setVSCoordinates(vx, vy);
        }
    }
    
    public Picker getPicker(){
        return picker;
    }

    /** Get the virtual space coordinates of the cursor. Unprojected w.r.t Camera c. */
    public Point2D.Double getVSCoordinates(Camera c){
        ViewPanel v = owningView.getPanel();
        //translate from JPanel coords
        double tcx,tcy;
        if (v.lens != null){
            //take lens into account (if set)
            v.lens.gf(jpx, jpy, gain);
            // take lens focus offset into account only when above threshold as the offset is not taken into account during rendering when below threshold
            // jpx - v.size.width/2 = cx when no lens
            tcx = (gain[0] >= v.lens.getBufferThreshold()) ? v.lens.lx + (jpx+v.lens.getXfocusOffset() - v.size.width/2 - v.lens.lx) / gain[0] : v.lens.lx + (jpx - v.size.width/2 - v.lens.lx) / gain[0];
            // v.size.height/2 - jpy = cy when no lens
            tcy = (gain[1] >= v.lens.getBufferThreshold()) ? (v.lens.ly + v.size.height/2 - jpy-v.lens.getYfocusOffset()) / gain[1] - v.lens.ly : (v.lens.ly + v.size.height/2 - jpy) / gain[1] - v.lens.ly;
        }
        else {
            tcx = jpx - v.size.width/2;
            tcy = v.size.height/2 - jpy;
        }
        double ucoef = (c.focal+c.altitude) / c.focal;
        // find coordinates of object's geom center wrt to camera center and project IN VIRTUAL SPACE
        return new Point2D.Double((tcx*ucoef) + c.vx, (tcy*ucoef) + c.vy);
    }

    /** Get the cursor's x-coordinate in JPanel coordinates system. */
    public int getPanelXCoordinate(){
	    return jpx;
    }

    /** Get the cursor's y-coordinate in JPanel coordinates system. */
    public int getPanelYCoordinate(){
	    return jpy;
    }
    
    /** Get the cursor's x-coordinate in virtual space coordinates system. */
    public double getVSXCoordinate(){
	    return vx;
    }

    /** Get the cursor's y-coordinate in virtual space coordinates system. */
    public double getVSYCoordinate(){
	    return vy;
    }

    /** Draw cursor crosshair. */
    void draw(Graphics2D g){
        if (isVisible){
            g.setColor(this.color);
            g.drawLine(jpx-size,jpy,jpx+size,jpy);
            g.drawLine(jpx,jpy-size,jpx,jpy+size);
        }
		if (dynaSpotActivated && showDynarea){
			g.setColor(DYNASPOT_COLOR);
			switch(dynaSpotVisibility){
				case DYNASPOT_VISIBILITY_VISIBLE:{g.setComposite(dsST);break;}
				case DYNASPOT_VISIBILITY_FADEIN:{g.setComposite(Translucency.acs[(int)Math.round((1-opacity) * DYNASPOT_MAX_TRANSLUCENCY * Translucency.ACS_ACCURACY)]);break;}
				case DYNASPOT_VISIBILITY_FADEOUT:{g.setComposite(Translucency.acs[(int)Math.round(opacity * DYNASPOT_MAX_TRANSLUCENCY * Translucency.ACS_ACCURACY)]);break;}
			}
			g.fillOval(jpx-dynaSpotRadius, jpy-dynaSpotRadius, 2*dynaSpotRadius, 2*dynaSpotRadius);
			g.setComposite(Translucent.acO);
		}
	}

	/* ---- DynaSpot implementation ---- */
	
	/* Glyphs in DynaSpot area */
	HashMap gida = new HashMap(20);
	
	Color DYNASPOT_COLOR = Color.LIGHT_GRAY;
	float DYNASPOT_MAX_TRANSLUCENCY = 0.3f;
	AlphaComposite dsST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)DYNASPOT_MAX_TRANSLUCENCY);
	
	/** The DynaSpot area is never displayed. */
	public static final short DYNASPOT_VISIBILITY_INVISIBLE = 0;
	/** The DynaSpot area is always displayed. */
	public static final short DYNASPOT_VISIBILITY_VISIBLE = 1;
	/** The DynaSpot area is invisible when the cursor is still, and gradually fades in when the cursor moves. */
	public static final short DYNASPOT_VISIBILITY_FADEIN = 2;
	/** The DynaSpot area is visible when the cursor is still, and gradually fades out when the cursor moves. */
	public static final short DYNASPOT_VISIBILITY_FADEOUT = 3;
	
	short dynaSpotVisibility = DYNASPOT_VISIBILITY_VISIBLE;
	
	/** Set the visibility and visual behaviour of the DynaSpot.
	 *@param v one of DYNASPOT_VISIBILITY_*
	 */
	public void setDynaSpotVisibility(short v){
		dynaSpotVisibility = v;
		showDynarea = dynaSpotVisibility != DYNASPOT_VISIBILITY_INVISIBLE;
	}

	/** Set the color of the dynaspot area.
        *@param c color of dynaspot area
        */	
	public void setDynaSpotColor(Color c){
	    DYNASPOT_COLOR = c;
	}
	
	/** Get the color of the dynaspot area.
        */	
	public Color getDynaSpotColor(){
	    return DYNASPOT_COLOR;
	}

	/** Set the translucence level of the dynaspot area.
        *@param a alpha value in [0.0-1.0]
        */	
	public void setDynaSpotTranslucence(float a){
	    DYNASPOT_MAX_TRANSLUCENCY = a;
	    dsST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)DYNASPOT_MAX_TRANSLUCENCY);
	}
	
	/** Get the translucence level of the dynaspot area.
        *@return an alpha value in [0.0-1.0]
        */	
	public float getDynaSpotTranslucence(){
	    return DYNASPOT_MAX_TRANSLUCENCY;
	}
	
	int DYNASPOT_MAX_RADIUS = 16;

	int LAG_TIME = 120;
	int REDUC_TIME = 180;
	
	/** Set DynaSpot lag parameter. See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. */
	public void setDynaSpotLagTime(int t){
	    LAG_TIME = t;
	}

	/** Get DynaSpot lag parameter. See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. */
	public int getDynaSpotLagTime(){
	    return LAG_TIME;
	}

	/** Set DynaSpot reduction time parameter. See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. */
	public void setDynaSpotReducTime(int t){
	    REDUC_TIME = t;
	    computeDynaSpotParams();
	}

	/** Get DynaSpot reduction time parameter. See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. */
	public int getDynaSpotReducTime(){
	    return REDUC_TIME;
	}
	
	int MIN_SPEED = 100;
	int MAX_SPEED = 300;
	
	/* dynaspot parameters */
	float ds_aa;
	float ds_ab;
	float ds_ra;
	float ds_rb;
	
	void computeDynaSpotParams(){
	    ds_aa = DYNASPOT_MAX_RADIUS / (float)(MAX_SPEED-MIN_SPEED);
    	ds_ab = -DYNASPOT_MAX_RADIUS * MIN_SPEED / (float)(MAX_SPEED-MIN_SPEED);
    	// linear drop-off
//    	ds_ra = -DYNASPOT_MAX_RADIUS / (float)REDUC_TIME;
        // co-exponential drop-off
    	ds_ra = -DYNASPOT_MAX_RADIUS / (float)Math.pow(REDUC_TIME,2);
    	ds_rb = DYNASPOT_MAX_RADIUS;
	}
	
	int dynaSpotRadius = 0;
		
	boolean dynaSpotActivated = false;
	
	boolean showDynarea = true;
	
	Timer dstimer;
	DynaSpotTimer dynaspotTimer;

	double opacity = 1.0f;

	double[] dynawnes = new double[4];
	
	Ellipse2D dynaspotVSshape = new Ellipse2D.Double(0, 0, 1, 1);
	
	void initDynaSpotTimer(){
		dstimer = new Timer();
		dynaspotTimer = new DynaSpotTimer(this);
		dstimer.scheduleAtFixedRate(dynaspotTimer, 40, 20);
	}
	
	static final int NB_SPEED_POINTS = 4;
	
	long[] cursor_time = new long[NB_SPEED_POINTS];
	int[] cursor_x = new int[NB_SPEED_POINTS];
	int[] cursor_y = new int[NB_SPEED_POINTS];

	float[] speeds = new float[NB_SPEED_POINTS-1];
	
	float mean_speed = 0;
	
	boolean dynaspot_triggered = false;
	
	long lastTimeAboveMinSpeed = -1;
	
	boolean reducing = false;
	long reducStartTime = 0;

    /**
     * Update DynaSpot's parameters.
     *@param currentTime current absolute time, obtained from System.currentTimeMillis()
     */
	public void updateDynaSpot(long currentTime){
		// compute mean speed over last 3 points
		for (int i=1;i<NB_SPEED_POINTS;i++){
			cursor_time[i-1] = cursor_time[i];
			cursor_x[i-1] = cursor_x[i];
			cursor_y[i-1] = cursor_y[i];
		}
		cursor_time[NB_SPEED_POINTS-1] = currentTime;
		cursor_x[NB_SPEED_POINTS-1] = this.jpx;
		cursor_y[NB_SPEED_POINTS-1] = this.jpy;
		for (int i=0;i<speeds.length;i++){
			speeds[i] = (float)Math.sqrt(Math.pow(cursor_x[i+1]-cursor_x[i],2)+Math.pow(cursor_y[i+1]-cursor_y[i],2)) / (float)(cursor_time[i+1]-cursor_time[i]);
		}
		mean_speed = 0;
		for (int i=0;i<speeds.length;i++){
			mean_speed += speeds[i];
		}
		mean_speed = mean_speed / (float)speeds.length * 1000;
		// adapt dynaspot area accordingly
		if (dynaspot_triggered){
		 	if (mean_speed > MIN_SPEED){
				lastTimeAboveMinSpeed = System.currentTimeMillis();
				if (mean_speed > MAX_SPEED){
					if (dynaSpotRadius < DYNASPOT_MAX_RADIUS){
						updateDynaSpotArea(DYNASPOT_MAX_RADIUS);
					}				
				}
			}
			else {
				if (lastTimeAboveMinSpeed > 0 && currentTime - lastTimeAboveMinSpeed >= LAG_TIME){
					lastTimeAboveMinSpeed = -1;
					reducing = true;
					reducStartTime = currentTime;
					dynaspot_triggered = false;
				}
			}		
		}
		else {
		 	if (mean_speed > MIN_SPEED){
				lastTimeAboveMinSpeed = System.currentTimeMillis();
				dynaspot_triggered = true;
				if (mean_speed > MAX_SPEED){
					if (dynaSpotRadius < DYNASPOT_MAX_RADIUS){
						updateDynaSpotArea(DYNASPOT_MAX_RADIUS);
					}				
				}
				else {
					updateDynaSpotArea(Math.round(ds_aa*mean_speed+ds_ab));
				}
			}
			else if (reducing){
				if (currentTime-reducStartTime >= REDUC_TIME){
					updateDynaSpotArea(0);
					reducing = false;
				}
				else {
				    // linear drop-off
//					updateDynaSpotArea(Math.round(ds_ra*(currentTime-reducStartTime)+ds_rb));
                    // co-exponential drop-off
					updateDynaSpotArea(Math.round(ds_ra*(float)Math.pow(currentTime-reducStartTime,2)+ds_rb));
				}
			}
		}
		owningView.repaint();
    }
    
	void updateDynaSpotArea(int r){
		dynaSpotRadius = r;
		dynaPick();
		if (dsl != null){
			dsl.spotSizeChanged(this, dynaSpotRadius);
		}
	}
	
	/** Get DynaSpot's current radius. See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. */
	public int getDynaSpotRadius(){
	    return dynaSpotRadius;
	}
	
	DynaSpotListener dsl;

	/** Listen for DynaSpot events. */
	public void setDynaSpotListener(DynaSpotListener dsl){
		this.dsl = dsl;
	}

	/** Find out who is listening for DynaSpot events. */	
	public DynaSpotListener getDynaSpotListener(){
		return dsl;
	}
	
	/** Enable/disable DynaSpot cursor behavior. */
	public void activateDynaSpot(boolean b){
		dynaSpotActivated = b;
		if (dynaSpotActivated){
			if (dstimer != null){
				dstimer.cancel();
			}
			initDynaSpotTimer();
		}
		else {
			try {
				dstimer.cancel();
				dstimer = null;
			}
			catch (NullPointerException ex){}
		}
	}
	
	/** Tells whether DynaSpot cursor behavior is enabled or not. */
	public boolean isDynaSpotActivated(){
	    return dynaSpotActivated;
	}

	/** Set maximum size of DynaSpot selection region. See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. */
	public void setDynaSpotMaxRadius(int r){
		DYNASPOT_MAX_RADIUS = (r < 0) ? 0 : r;
		computeDynaSpotParams();
	}

	/** Get maximum size of DynaSpot selection region. See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. */
	public int getDynaSpotMaxRadius(){
		return DYNASPOT_MAX_RADIUS;
	}

    Camera refToCam4DynaPick = null;

	/** Compute the list of glyphs picked by the dynaspot cursor.
	 * The best picked glyph is returned.
	 *@see #dynaPick(Camera c)
	 */
	void dynaPick(){
        dynaPick(refToCam4DynaPick);
    }
    
    Glyph lastDynaPicked = null;
    
    SelectionListener sl;
    
    /** Set a Selection Listener callback triggered when a glyph gets selected/unselected by DynaSpot.
        *@param sl set to null to remove
        */
    public void setSelectionListener(SelectionListener sl){
        this.sl = sl;
    }
    
    /** Get the Selection Listener callback triggered when a glyph gets selected/unselected by DynaSpot.
        *@return null if none set.
        */
    public SelectionListener getSelectionListener(){
        return this.sl;
    }
    
	/** Compute the list of glyphs picked by the DynaSpot cursor.
	 * The best picked glyph is returned.
	 * See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. 
	 *@return null if the dynaspot cursor does not pick anything.
     *@see #dynaPick()
	 */
	public Glyph dynaPick(Camera c){
	    if (c == null){
	        return null;
	    }
	    refToCam4DynaPick = c;
		Vector drawnGlyphs = c.getOwningSpace().getDrawnGlyphs(c.getIndex());
		Glyph selectedGlyph = null;
	    // initialized at -1 because we don't know have any easy way to compute some sort of "initial" distance for comparison
	    // when == 0, means that the cursor's hotspot is actually inside the glyph
	    // if > 0 at the end of the loop, dynaspot intersects at least one glyph (but cursor hotspot is not inside any glyph)
	    // if == -1, nothing is intersected by the dynaspot area
		double distanceToSelectedGlyph = -1;
		Glyph g;
		int gumIndex = -1;
		int cgumIndex = -1;
	    double unprojectedDSRadius = ((c.focal+c.altitude) / c.focal) * dynaSpotRadius;
		dynawnes[0] = vx - unprojectedDSRadius; // west bound
		dynawnes[1] = vy + unprojectedDSRadius; // north bound
		dynawnes[2] = vx + unprojectedDSRadius; // east bound
		dynawnes[3] = vy - unprojectedDSRadius; // south bound
		dynaspotVSshape.setFrame(dynawnes[0], dynawnes[3], 2*unprojectedDSRadius, 2*unprojectedDSRadius);
		synchronized(drawnGlyphs){
    		for (int i=0;i<drawnGlyphs.size();i++){
    			g = (Glyph)drawnGlyphs.elementAt(i);
    			if (!g.isSensitive()){
    			    continue;
    			}
    			// check if cursor hotspot is inside glyph
    			// if hotspot in several glyphs, selected glyph will be the last glyph entered (according to glyphsUnderMouse)
    			cgumIndex = Utils.indexOfGlyph(picker.pickedGlyphs, g, picker.maxIndex+1);
    			if (cgumIndex > -1){
    				if (cgumIndex > gumIndex){
    					gumIndex = cgumIndex;
    					selectedGlyph = g;
    					distanceToSelectedGlyph = 0;
    				}
    				gida.put(g, null);
    			}
    			// if cursor hotspot is not inside the glyph, check bounding boxes (Glyph's and DynaSpot's),
    			// if they do intersect, peform a finer-grain chec with Areas
    			else if (g.visibleInRegion(dynawnes[0], dynawnes[1], dynawnes[2], dynawnes[3], c.getIndex()) &&
    			 	g.visibleInDisc(vx, vy, unprojectedDSRadius, dynaspotVSshape, c.getIndex(), jpx, jpy, dynaSpotRadius)){
                    // glyph intersects dynaspot area    
                    gida.put(g, null);
                    double d = Math.sqrt(Math.pow(g.vx-vx,2)+Math.pow(g.vy-vy,2));
                    if (distanceToSelectedGlyph == -1 || d < distanceToSelectedGlyph){
                        selectedGlyph = g;
                        distanceToSelectedGlyph = d;
                    }
    			}
    			else {
    			    // glyph does not intersect dynaspot area
    			    if (gida.containsKey(g)){
        		        gida.remove(g);
        		        if (sl != null){
        		            sl.glyphSelected(g, false);
        		        }
    			    }
    			}
    		}		    
		}
        if (selectedGlyph != null && sl != null){
            sl.glyphSelected(selectedGlyph, true);
        }
        if (lastDynaPicked != null && selectedGlyph != lastDynaPicked && sl != null){
            sl.glyphSelected(lastDynaPicked, false);
        }
        lastDynaPicked = selectedGlyph;
        return selectedGlyph;
	}

	/** Get the set of glyphs intersected by the cursor's dynaspot region.
	 * See <a href="http://zvtm.sourceforge.net/doc/dynaspot.html">http://zvtm.sourceforge.net/doc/dynaspot.html</a> for more detail. 
	 *@return a set of Glyph IDs
	 *@see #dynaPick(Camera c)
	 */
	public Set getGlyphsInDynaSpotRegion(Camera c){
		return gida.keySet();
	}

}

class DynaSpotTimer extends TimerTask{

	VCursor c;
	
	DynaSpotTimer(VCursor c){
		super();
		this.c = c;
	}
	
	public void run(){
		c.updateDynaSpot(System.currentTimeMillis());
	}
	
}

class DefaultSelectionAction implements SelectionListener {
    
    public void glyphSelected(Glyph g, boolean b){
        g.highlight(b, null);        
    }
    
}
