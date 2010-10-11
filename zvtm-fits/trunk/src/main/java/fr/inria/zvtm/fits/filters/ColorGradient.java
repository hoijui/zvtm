/*   AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 *
 */

package fr.inria.zvtm.fits.filters;

import java.awt.LinearGradientPaint;

public interface ColorGradient {
    
    public LinearGradientPaint getGradient(float w);
    
}
