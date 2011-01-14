/**
 * @author Romain Primet
 */
package fr.inria.zvtm.treemap.demo;

import java.awt.Color;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JTree;

import java.io.File;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.treemap.Mappable;
import fr.inria.zvtm.treemap.Rect;
import fr.inria.zvtm.treemap.Squarified;
import fr.inria.zvtm.treemap.Tree;
import fr.inria.zvtm.treemap.TreemapUtils;
import fr.inria.zvtm.treemap.Walker;
import fr.inria.zvtm.treemap.ZMapItem;

class Demo {
    private static final Rect TREEMAP_RECT = new Rect(0,0,800000,400000);
    private static final double LABEL_HEIGHT = 24000;
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

        Tree<ZMapItem> tree = TreemapUtils.swingToUmd(new JTree().getModel());
        //Tree tree = TreemapUtils.swingToUmd(new FileTreeModel(new File("/home/rprimet/temp/foo")));
        //tree.layout(new SquarifiedLayout(), TREEMAP_RECT, LABEL_HEIGHT, INSETS);
        tree.sum();
        Squarified.INSTANCE.computeShapes(new Rect(0, 0, 1024, 768), tree);
        makeRepr(tree, demoSpace);
        view.getGlobalView(cam, 500);
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
                    bounds.w, bounds.h, new Color(200, 200, 200));
                rect.setBorderColor(Color.WHITE);
                String txt = item.getUserObject() instanceof File? 
                    ((File)item.getUserObject()).getName() : item.getUserObject().toString();
               // VText text = new VText(rect.vx-rect.getWidth()*0.5, rect.vy+rect.getHeight()*0.5, 0, 
               //     Color.WHITE, txt, 
               //     VText.TEXT_ANCHOR_START, 1f);
               // text.setScale((float)(text.getScale()*LABEL_HEIGHT/TreemapUtils.getVTextHeight(text)));
               // text.move(0, -TreemapUtils.getVTextHeight(text));
                item.putGraphicalObject("RECT", rect);
               // item.putGraphicalObject("TEXT", text);
                vs.addGlyph(rect);
               // vs.addGlyph(text);
            }
        });
    }

    public static void main(String[] args){
        new Demo(); 
    }
}

