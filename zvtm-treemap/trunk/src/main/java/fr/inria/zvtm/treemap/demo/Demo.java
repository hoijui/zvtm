/**
 * @author Romain Primet
 */
package fr.inria.zvtm.treemap.demo;

import java.awt.Color;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JTree;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.treemap.InsetComputer;
import fr.inria.zvtm.treemap.Mappable;
import fr.inria.zvtm.treemap.Rect;
import fr.inria.zvtm.treemap.SquarifiedLayout;
import fr.inria.zvtm.treemap.Tree;
import fr.inria.zvtm.treemap.TreemapUtils;
import fr.inria.zvtm.treemap.Walker;
import fr.inria.zvtm.treemap.ZMapItem;

class Demo {
    private static final Rect TREEMAP_RECT = new Rect(0,0,800000,400000);
    private static final InsetComputer INSET = new InsetComputer(){
        public double getXleft(Tree tree){
            return TREEMAP_RECT.h / 30.;
        }
        public double getXright(Tree tree){
            return TREEMAP_RECT.h / 30.;
        }
        public double getYtop(Tree tree){
            return TREEMAP_RECT.h / 10.;
        }
        public double getYbottom(Tree tree){
            return TREEMAP_RECT.h / 30.;
        }
    };
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    private VirtualSpace demoSpace;
    private Camera cam;
    private View view;

    Demo(){
        demoSpace = vsm.addVirtualSpace("demoSpace");
        cam = demoSpace.addCamera();
        view = vsm.addFrameView(new Vector(Arrays.asList(new Camera[]{cam})),
                "Treemap demo", View.STD_VIEW, 800, 600, false, true);
        view.getCursor().setColor(Color.GREEN);

        Tree tree = TreemapUtils.swingToUmd(new JTree().getModel());
        tree.layout(new SquarifiedLayout(), TREEMAP_RECT, INSET);
        makeRepr(tree, demoSpace);
        view.getGlobalView(cam, 500);
    }

    private static void makeRepr(final Tree tree, final VirtualSpace vs){
        tree.traversePre(new Walker<Tree>(){
            public void visitNode(Tree t){
                ZMapItem item = (ZMapItem)(t.getMapItem());
                System.out.println(item.getUserObject());
                Rect bounds = item.getBounds();
                VRectangle rect = new VRectangle(bounds.x + bounds.w*0.5, 
                    bounds.y + bounds.h * 0.5, 
                    0, 
                    bounds.w, bounds.h, Color.GRAY);
                rect.setBorderColor(Color.WHITE);
                //XXX yet another inset computation!
                if(!t.hasChildren() && (t.getParent() != null)){
                    rect.move(INSET.getXleft(t), -INSET.getYtop(t));
                    ZMapItem parentItem = (ZMapItem)(t.getParent().getMapItem());
                    Rect parentBounds = parentItem.getBounds();
                    double fx = (parentBounds.w - INSET.getXleft(t) - INSET.getXright(t))/parentBounds.w;
                    double fy = (parentBounds.h - INSET.getYtop(t) - INSET.getYbottom(t))/parentBounds.h;
                    rect.setHeight(fx*rect.getHeight());
                    rect.setWidth(fy*rect.getWidth());
                }
                VText text = new VText(rect.vx-rect.getWidth()*0.5, rect.vy+rect.getHeight()*0.5, 0, 
                    Color.WHITE, item.getUserObject().toString(), 
                    VText.TEXT_ANCHOR_START, 1f);
                text.setScale((float)(text.getScale()*INSET.getYtop(tree)/TreemapUtils.getVTextHeight(text)));
                text.move(0, -TreemapUtils.getVTextHeight(text));
                item.putGraphicalObject("RECT", rect);
                item.putGraphicalObject("TEXT", text);
                vs.addGlyph(rect);
                vs.addGlyph(text);
            }
        });
    }

    public static void main(String[] args){
        new Demo(); 
    }
}

