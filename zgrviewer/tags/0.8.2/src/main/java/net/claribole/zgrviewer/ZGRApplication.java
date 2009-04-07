/*   FILE: StatusBar.java
 *   DATE OF CREATION:   Thu Nov 30 11:29:31 2006
 *   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2006. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 *   $Id$
 */ 

package net.claribole.zgrviewer;

public interface ZGRApplication {

    public void about();

    public void setStatusBarText(String s);
    
}