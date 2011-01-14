/**
 * @author Romain Primet
 */
package fr.inria.zvtm.treemap.demo;

import java.awt.Color;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JTree;

import java.io.File;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
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
class Demo {
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    private final VirtualSpace demoSpace;
    private final Camera cam;
    private final View view;

    Demo(){
        demoSpace = vsm.addVirtualSpace("demoSpace");
        cam = demoSpace.addCamera();
        view = vsm.addFrameView(new Vector(Arrays.asList(new Camera[]{cam})),
                "Treemap demo", View.STD_VIEW, 800, 600, false, true);
        view.getCursor().setColor(Color.GREEN);

        Tree<ZMapItem> tree = TreemapUtils.swingToUmd(new JTree().getModel());
        tree.sum();
        Squarified.INSTANCE.computeShapes(new Rect(0, 0, 1024, 768), tree);
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
                System.out.println(item.getUserObject());
                Rect bounds = item.getBounds();
                VRectangle rect = new VRectangle(bounds.x + bounds.w*0.5, 
                    bounds.y + bounds.h * 0.5, 
                    0, 
                    bounds.w, bounds.h, new Color(220, 220, 220));
                rect.setBorderColor(Color.BLACK);
                String txt = item.getUserObject() instanceof File? 
                    ((File)item.getUserObject()).getName() : item.getUserObject().toString();
                VText text = new VText(rect.vx, rect.vy+rect.getHeight()*0.5, 0, 
                    Color.BLACK, txt, 
                    VText.TEXT_ANCHOR_MIDDLE, 1.2f);
               // text.setScale((float)(text.getScale()*LABEL_HEIGHT/TreemapUtils.getVTextHeight(text)));
                text.move(0, -TreemapUtils.getVTextHeight(text));
                if(t.isLeaf()){
                    text.moveTo(rect.vx, rect.vy);
                } else {
                    rect.setCursorInsideFillColor(new Color(237, 245, 7));
                }
                item.putGraphicalObject("RECT", rect);
                item.putGraphicalObject("TEXT", text);
                vs.addGlyph(rect);
                vs.addGlyph(text);
                // needs to happen after the glyph is added to the virtual
                // space for some reason
                rect.setCursorInsideHighlightColor(new Color(30, 30, 30));
            }
        });
    }

    public static void main(String[] args){
        Demo demo = new Demo(); 
        demo.setListener(new ViewAdapter(){
            public void enterGlyph(Glyph g){
                g.highlight(true, null);
            }

            public void exitGlyph(Glyph g){
                g.highlight(false, null);
            }
        });
    }
}

