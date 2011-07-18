package fr.inria.zvtm.common.gui.menu;

public interface ItemActivationListener {

	/**
	 * Callback for when the {@link Item} is activated
	 */
	public void activated();
	
	/**
	 * Callback for when the {@link Item} is deactivated
	 */
	public void deactivated();
}
