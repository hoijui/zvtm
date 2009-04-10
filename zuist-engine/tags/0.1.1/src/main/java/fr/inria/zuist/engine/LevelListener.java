/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: LevelListener.java,v 1.1 2007/10/02 09:11:28 pietriga Exp $
 */

package fr.inria.zuist.engine;

public interface LevelListener {
	
	public void enteredLevel(int depth);
	
	public void exitedLevel(int depth);
	
}
