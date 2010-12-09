package fr.inria.zvtm.treemap;

public class TreemapUtils {
    /**
     * Returns a new UMD Tree instance that is equivalent to the 
     * given Swing TreeModel
     */
    public static Tree swingToUmd(javax.swing.tree.TreeModel srcModel){
        return swingToUmd(srcModel, srcModel.getRoot());
    }

    private static Tree swingToUmd(javax.swing.tree.TreeModel srcModel, Object node){
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

}

