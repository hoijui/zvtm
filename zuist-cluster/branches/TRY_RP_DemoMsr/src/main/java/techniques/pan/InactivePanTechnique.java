/**
 * 
 */
package techniques.pan;


/**
 * Default pan technique. Does nothing.
 * 
 * @author mathieunancel
 *
 */
public class InactivePanTechnique extends AbstractPanTechnique {

	/**
	 * @param id
	 * @param o
	 */
	public InactivePanTechnique(String id, ORDER o) {
		super(id, o);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#close()
	 */
	@Override
	public void close() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#deleteStatLabels()
	 */
	@Override
	public void deleteStatLabels() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#initListeners()
	 */
	@Override
	public void initListeners() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#startListening()
	 */
	@Override
	public void startListening() {
		// Nothing
	}

	/* (non-Javadoc)
	 * @see package techniques.AbstractTechnique#stopListening()
	 */
	@Override
	public void stopListening() {
		// Nothing
	}

}
