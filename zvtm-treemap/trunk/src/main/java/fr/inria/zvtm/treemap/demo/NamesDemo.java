/**
 * @author Romain Primet
 */
package fr.inria.zvtm.treemap.demo;

import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JTree;

import java.io.File;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.PRectangle;
import fr.inria.zvtm.glyphs.AdaptiveText;
import fr.inria.zvtm.treemap.Mappable;
import fr.inria.zvtm.treemap.Rect;
import fr.inria.zvtm.treemap.Squarified;
import fr.inria.zvtm.treemap.Tree;
import fr.inria.zvtm.treemap.TreemapUtils;
import fr.inria.zvtm.treemap.Walker;
import fr.inria.zvtm.treemap.ZMapItem;

/**
 * Builds a treemap representation from the default model of a JTree.
 */
class NamesDemo {
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    private final VirtualSpace demoSpace;
    private final Camera cam;
    private final View view;
    private static final Color DEFAULT_FILL = new Color(220, 220, 220);
    private static final Color HIGHLIGHT_FILL = new Color(217, 225, 7);

    NamesDemo(){
        demoSpace = vsm.addVirtualSpace("demoSpace");
        cam = demoSpace.addCamera();
        view = vsm.addFrameView(new Vector(Arrays.asList(new Camera[]{cam})),
                "Treemap demo", View.STD_VIEW, 800, 600, true);
        view.getCursor().setColor(Color.GREEN);

        Tree<ZMapItem> tree = buildTree();
        tree.sum(); //XXX
        Squarified.INSTANCE.computeShapesResize(new Rect(-200, 300, 1024, 768), tree, 20, 12);
        makeRepr(tree, demoSpace);
        view.getGlobalView(cam, 500); 
        ((JFrame)view.getFrame()).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private void setListener(ViewListener listener){
        view.setListener(listener);
    }

    private static void makeRepr(final Tree tree, final VirtualSpace vs){
        tree.traversePre(new Walker<Tree<ZMapItem>>(){
            public void visitNode(Tree<ZMapItem> t){
                ZMapItem item = t.getMapItem();
                Rect bounds = item.getBounds();
                PRectangle rect = new PRectangle(bounds.x + bounds.w*0.5,
                    bounds.y + bounds.h * 0.5,
                    0,
                    bounds.w, bounds.h,
                    TreemapUtils.makeDiagGradient((float)bounds.w, (float)bounds.h, DEFAULT_FILL));
                rect.setBorderColor(Color.BLACK);
                rect.setCursorInsidePaint(TreemapUtils.makeDiagGradient((float)rect.getWidth(), (float)rect.getHeight(), HIGHLIGHT_FILL));
                String txt = item.getUserObject().toString();
                AdaptiveText text = new AdaptiveText(rect.vx, rect.vy+rect.getHeight()*0.5, 0, 
                    Color.BLACK, txt, 
                    rect.getWidth(), 11);
                text.move(0, -TreemapUtils.getVTextHeight(text));
                if(t.isLeaf()){
                    text.moveTo(rect.vx, rect.vy);
                } 
                item.putGraphicalObject("RECT", rect);
                item.putGraphicalObject("TEXT", text);
                vs.addGlyph(rect);
                vs.addGlyph(text);
            }
        });
    }

    private static Tree<ZMapItem> buildTree(){
        ZMapItem rootItem = new ZMapItem("Prénoms");
        Tree<ZMapItem> retval = new Tree<ZMapItem>(rootItem);
        for(Map.Entry<String, Integer> entry : Names.NAMES.entrySet()){
            ZMapItem item = new ZMapItem(entry.getKey());
            item.setSize(entry.getValue());
            Tree<ZMapItem> node = new Tree<ZMapItem>(item);
            retval.addChild(node);
        }
        return retval;
    }

    public static void main(String[] args){
        NamesDemo demo = new NamesDemo(); 
        demo.setListener(new ViewAdapter(){
               double lastJPX = 0;
               double lastJPY = 0;
               final float ZOOM_SPEED_COEF = 1.0f/50.0f;
               final double PAN_SPEED_COEF = 50.0;

            @Override public void enterGlyph(Glyph g){
                g.highlight(true, null);
            }

            @Override public void exitGlyph(Glyph g){
                g.highlight(false, null);
            }

            @Override public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
                if (buttonNumber == 3 || ((mod == META_MOD || mod == META_SHIFT_MOD) && buttonNumber == 1)){
                    Camera c=VirtualSpaceManager.INSTANCE.getActiveCamera();
                    double a=(c.focal+Math.abs(c.altitude))/c.focal;
                    if (mod == META_SHIFT_MOD) {
                        c.setXspeed(0);
                        c.setYspeed(0);
                        c.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
                        //50 is just a speed factor (too fast otherwise)
                    }
                    else {
                        c.setXspeed((c.altitude>0) ? ((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : ((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                        c.setYspeed((c.altitude>0) ? ((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : ((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
                        c.setZspeed(0);
                    }
                }
            }

           @Override public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
                Camera c=VirtualSpaceManager.INSTANCE.getActiveCamera();
                double a=(c.focal+Math.abs(c.altitude))/c.focal;
                if (wheelDirection == WHEEL_DOWN){
                    c.altitudeOffset(-a*5);
                    VirtualSpaceManager.INSTANCE.repaint();
                }
                else {
                    //wheelDirection == WHEEL_UP
                    c.altitudeOffset(a*5);
                    VirtualSpaceManager.INSTANCE.repaint();
                }
            }

            @Override public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
                lastJPX=jpx;
                lastJPY=jpy;
                v.setDrawDrag(true);
                VirtualSpaceManager.INSTANCE.getActiveView().mouse.setSensitivity(false);
            }

            @Override public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
                Camera c=VirtualSpaceManager.INSTANCE.getActiveCamera();
                c.setXspeed(0);
                c.setYspeed(0);
                c.setZspeed(0);
                v.setDrawDrag(false);
                VirtualSpaceManager.INSTANCE.getActiveView().mouse.setSensitivity(true);
            }
        });
    }
}

