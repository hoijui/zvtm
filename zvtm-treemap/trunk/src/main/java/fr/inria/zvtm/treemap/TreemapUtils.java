/**
 * @author Romain Primet
 */
package fr.inria.zvtm.treemap;

import java.awt.Toolkit;

import fr.inria.zvtm.glyphs.VText;

public class TreemapUtils {
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
     * Returns the <b>approximate</b> height of a VText
     */ 
    public static double getVTextHeight(VText text){
        return text.getScale()*Toolkit.getDefaultToolkit().getFontMetrics(text.getMainFont()).getHeight();
    }

    /**
     * Returns the <b>approximate</b> width of a VText
     */ 
    public static double getVTextWidth(VText text){
        return text.getScale()*Toolkit.getDefaultToolkit().getFontMetrics(text.getMainFont()).charWidth('a')*text.getText().length();
    }
}

