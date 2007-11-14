/*   FILE: TrajectoryViewer1D.java
 *   DATE OF CREATION:  Thu Jun 8 08:51:11 2006
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */ 

package net.claribole.zvtm.eval;

import javax.swing.table.*;

public class TrialTableModel extends DefaultTableModel {

    public TrialTableModel(int nbRow,int nbCol){
	super(nbRow,nbCol);
    }

    public boolean isCellEditable(int row,int column){
	return (column == 5);
    }

}
