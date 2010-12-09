//author Romain Primet
package fr.inria.zvtm.treemap;

//Computes x and y insets given a Tree. You need to provide
//an implementation when laying out a tree.
//We mainly expect implementers to use the depth of the tree as
//their defining parameter
public interface InsetComputer {
  public double getXinsetLeft(Tree model);
  public double getXinsetRight(Tree model);
  public double getYinsetTop(Tree model);
  public double getYinsetBottom(Tree model);
}

