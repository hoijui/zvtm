/*   AUTHOR : Olivier Gladin (olivier.gladin@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
// Interface for network synchronized views

package fr.inria.zvtm.cluster;


public interface SyncView {

    public void drawAndAck();

    public void paintAndAck();

}