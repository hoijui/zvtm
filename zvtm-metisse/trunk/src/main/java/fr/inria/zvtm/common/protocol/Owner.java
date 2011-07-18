package fr.inria.zvtm.common.protocol;

/**
 * The interface {@link Owner} is aimed at clean-close purposes. 
 * When a field of the {@link Owner} detects the end of a connection for instance, it can call the end() method to ask it's parent to close the connection.
 * @author Julien Altieri
 *
 */
public interface Owner {
	
	/**
	 * The method which should be called to close properly this owner object (mostly used by slaves).
	 */
	public void end();
}
