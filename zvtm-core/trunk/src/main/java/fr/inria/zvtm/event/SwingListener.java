package fr.inria.zvtm.event;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import javax.swing.SwingUtilities;

import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VSwingComponent;

public class SwingListener extends ViewAdapter {
    @Override public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        pickAndForward(v, e);
    }

    @Override public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
      pickAndForward(v, e); 
    }

    @Override public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){
      pickAndForward(v, e); 
    }
    @Override public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){
      pickAndForward(v, e); 
    }
    @Override public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){
      pickAndForward(v, e); 
    }
    
	private void pickAndForward(ViewPanel v, MouseEvent e) {
		//pick glyph under cursor, redispatch if it is an instance
        //of VSwingComponent
        Glyph[] pickList = v.getVCursor().getGlyphsUnderMouseList();
        if(pickList.length == 0){
            return;
        }
        Glyph pickedGlyph = pickList[pickList.length - 1];
        if(pickedGlyph instanceof VSwingComponent){
            redispatchMouse(v, e, (VSwingComponent)pickedGlyph);
        }
	}

    void redispatchMouse(ViewPanel v, final MouseEvent evt, VSwingComponent c){
        //we have an event location in View coordinates
        //- transform into VS coords.
        //- transform VS coords into "global component" (VSC) coords
        //- find the deepest component within the VSC
        //- transform VSC coords into deepest component coords [TODO test]
        //- translate original event, reset its source to the 
        //deepest component and redispatch it
        Point2D vsCoords = v.viewToSpaceCoords(v.cams[v.activeLayer], evt.getX(), evt.getY()); //XXX replace v.cams... by v.getActiveCamera
        Point2D pt = spaceToComponent(c, vsCoords);
        final Component cmp = SwingUtilities.getDeepestComponentAt(c.getComponent(), (int)pt.getX(), (int)pt.getY());
        Point2D deepestCoords = new Point2D.Double(pt.getX() - cmp.getX(),
                pt.getY() - cmp.getY());
        evt.translatePoint((int)(deepestCoords.getX() - evt.getX()),
                (int)(deepestCoords.getY() - evt.getY()));
        evt.setSource(cmp);
        cmp.dispatchEvent(evt);
        VirtualSpaceManager.INSTANCE.repaint(); //XXX quick fix
    }

    Point2D spaceToComponent(VSwingComponent c, Point2D vsCoords){
        return new Point2D.Double(vsCoords.getX() - (c.vx - c.getWidth()/2),
                (c.vy + c.getHeight()/2) - vsCoords.getY());
    }
}

