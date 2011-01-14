/**
 * @author Romain Primet
 */
package fr.inria.zvtm.treemap;

public interface Walker<T> {
  public void visitNode(T node);
}

