/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package net.claribole.zvtm.animation;

public interface TimingHandler {
    public void begin(Object subject, Animation.Dimension dim);
    public void end(Object subject, Animation.Dimension dim);
    public void repeat(Object subject, Animation.Dimension dim);
    public void timingEvent(float fraction, 
			    Object subject, Animation.Dimension dim);
}