/**
 * 
 */
package techniques.zoom;


/**
 * Default zoom technique. Does nothing.
 * 
 * @author mathieunancel
 *
 */
public class InactiveViewerTechnique extends AbstractViewerTechnique {

	/**
	 * @param id
	 * @param o
	 */
	public InactiveViewerTechnique(String id, ORDER o) {
		super(id, o, false);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#close()
	 */
	@Override
	public void close() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#deleteStatLabels()
	 */
	@Override
	public void deleteStatLabels() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#initListeners()
	 */
	@Override
	public void initListeners() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#startListening()
	 */
	@Override
	public void startListening() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see techniques.AbstractTechnique#stopListening()
	 */
	@Override
	public void stopListening() {
		// Nothing
	}

}
