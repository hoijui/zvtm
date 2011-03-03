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
class Demo {
    private VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    private final VirtualSpace demoSpace;
    private final Camera cam;
    private final View view;
    private static final Color DEFAULT_FILL = new Color(220, 220, 220);
    private static final Color HIGHLIGHT_FILL = new Color(217, 225, 7);

    Demo(){
        demoSpace = vsm.addVirtualSpace("demoSpace");
        cam = demoSpace.addCamera();
        view = vsm.addFrameView(new Vector(Arrays.asList(new Camera[]{cam})),
                "Treemap demo", View.STD_VIEW, 800, 600, true);
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

