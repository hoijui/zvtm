/*
 *  (c) COPYRIGHT CNRS 2016.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import java.io.Serializable;

public interface ToMasterMsg extends Serializable {
    public void apply();
}