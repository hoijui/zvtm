//author Romain Primet
package fr.inria.zvtm.treemap;

//Computes x and y insets given a TreeModel. You need to provide
//an implementation when laying out a tree.
//We mainly expect implementers to use the depth of the tree as
//their defining parameter
public interface InsetComputer {
  public double getXinsetLeft(TreeModel model);
  public double getXinsetRight(TreeModel model);
  public double getYinsetTop(TreeModel model);
  public double getYinsetBottom(TreeModel model);
}

