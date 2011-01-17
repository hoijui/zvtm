/**
 * @author Romain Primet
 */
package fr.inria.zvtm.treemap;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Toolkit;

import fr.inria.zvtm.glyphs.VText;

public class TreemapUtils {
    //let's not instantiate this
    private TreemapUtils(){}

    /**
     * Returns a new UMD Tree instance that is equivalent to the 
     * given Swing TreeModel
     */
    public static Tree<ZMapItem> swingToUmd(javax.swing.tree.TreeModel srcModel){
        return swingToUmd(srcModel, srcModel.getRoot());
    }

    private static Tree<ZMapItem> swingToUmd(javax.swing.tree.TreeModel srcModel, Object node){
        if(node == null){
            throw new Error("Building an empty tree is unsupported");
        } 
        Tree retval = new Tree(new ZMapItem(node));
        if (srcModel.isLeaf(node)) {
            return retval; 
        } else {
            int childCount = srcModel.getChildCount(node);
            for(int i=0; i<childCount; ++i){
                Tree childTree = swingToUmd(srcModel, srcModel.getChild(node, i));
                retval.addChild(childTree);
            }
            return retval;
        }
    }

    /**
     * Fits a VText instance to the given bounds, obeying a maximum height 
     * constraint
     */
    public static void fitText(VText text, Rect bounds, double maxHeight){
        double textLen = getVTextWidth(text);
        text.setScale(text.getScale() * (float)(bounds.w/textLen));
        double textHeight = getVTextHeight(text);
        if(textHeight >= maxHeight){
            text.setScale((float)(text.getScale() * maxHeight/textHeight));
        }
    }

    /**
     * Returns the <b>approximate</b> height of a VText, in 
     * virtual space units.
     */ 
    public static double getVTextHeight(VText text){
        return text.getScale()*Toolkit.getDefaultToolkit().getFontMetrics(text.getMainFont()).getHeight();
    }

    /**
     * Returns the <b>approximate</b> width of a VText, in 
     * virtual space units.
     */ 
    public static double getVTextWidth(VText text){
        return text.getScale()*Toolkit.getDefaultToolkit().getFontMetrics(text.getMainFont()).charWidth('a')*text.getText().length();
    }

    /**
     * Returns a diagonal gradient centered around a color (upper left 
     * corner is lighter, lower right corner is darker)
     */
    public static GradientPaint makeDiagGradient(float width, float height, Color centerColor){
        float[] hsb = new float[3];
        Color.RGBtoHSB(centerColor.getRed(),
                centerColor.getGreen(),
                centerColor.getBlue(),
                hsb);
        float lightB = (float)Math.min(1, hsb[2]*1.2);
        float darkB = (float)Math.max(0, hsb[2]*0.8);
        Color lighter = Color.getHSBColor(hsb[0], hsb[1], lightB);
        Color darker = Color.getHSBColor(hsb[0], hsb[1], darkB);
        return new GradientPaint(0f, 0f, lighter, width, height, darker);
    }
}

