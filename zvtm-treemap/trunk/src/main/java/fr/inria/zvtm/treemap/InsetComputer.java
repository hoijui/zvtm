/**
 * @author Romain Primet
 */
package fr.inria.zvtm.treemap;

/** 
 * Computes x and y insets that will be applied when laying out a Tree.
 */
public interface InsetComputer {
  /**
   * Returns the left horizontal inset.
   */
  public double getXleft(Tree model);

  /**
   * Returns the right horizontal inset.
   */
  public double getXright(Tree model);

  /**
   * Returns the top vertical inset.
   */
  public double getYtop(Tree model);

  /**
   * Returns the bottom vertical inset.
   */
  public double getYbottom(Tree model);
}

