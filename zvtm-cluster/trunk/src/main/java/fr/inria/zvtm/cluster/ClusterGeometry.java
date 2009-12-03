package fr.inria.zvtm.cluster;

/**
 * A rectangular display wall.
 * Attributes are the size of a block (ie screen),
 * the number of rows and the number of columns of the wall.
 */
//@Immutable
public class ClusterGeometry {
    private final int blockWidth;
    private final int blockHeight;
    private final int cols;
    private final int rows;

    public ClusterGeometry(int blockWidth, int blockHeight,
            int cols, int rows){
        if((blockWidth <= 0) || (blockHeight <= 0) || (cols <= 0) || (rows <= 0)){
            throw new IllegalArgumentException("view parameters must be positive");
        }
        this.blockWidth = blockWidth;
        this.blockHeight = blockHeight;
        this.cols = cols;
        this.rows = rows;
    }

    public ClusterGeometry(ClusterGeometry cg){
        this.blockWidth = cg.blockWidth;
        this.blockHeight = cg.blockHeight;
        this.cols = cg.cols;
        this.rows = cg.rows;
    }

    /**
     * Returns a new ClusterGeometry that adds bezels to the dimensions
     * of the current one (e.g. to allow drawing under the bezels).
     * @param bezelWidth total bezel width (left+right)
     * @param bezelHeight total bezel height (top+bottom)
     */
    public ClusterGeometry addBezels(int bezelWidth, int bezelHeight){
        return new ClusterGeometry(this.blockWidth + bezelWidth,
                this.blockHeight + bezelHeight,
                cols, rows);
    }

    /**
     * Returns the total width of the display wall
     */
    public int getWidth(){
        return cols*blockWidth;
    }

    /**
     * Returns the total height of the display wall
     */
    public int getHeight(){
        return rows*blockHeight;
    }

    public int getBlockWidth(){
        return blockWidth;
    }

    public int getBlockHeight(){
        return blockHeight;
    }

    public int getColumns(){
        return cols;
    }

    public int getRows(){
        return rows;
    }

}

