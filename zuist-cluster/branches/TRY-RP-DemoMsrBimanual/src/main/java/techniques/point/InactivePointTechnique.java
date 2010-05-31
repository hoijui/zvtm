/**
 * 
 */
package techniques.point;


/**
 * Default pointing technique. Does nothing.
 * 
 * @author mathieunancel
 *
 */
public class InactivePointTechnique extends AbstractPointTechnique {

	/**
	 * @param id
	 * @param o
	 */
	public InactivePointTechnique(String id, ORDER o) {
		super(id, o);
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
