/*   Copyright (c) INRIA, 2011-2012. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.widgets;

/**
 * A menu item.
 *@author R.Primet, INRIA
 */

public class MenuItem<T> {
    private final String text;
    private final T userObject;

    public MenuItem(String text, T userObject){
        this.text = text;
        this.userObject = userObject;
    }

    public String getText(){
        return text;
    }

    public T getUserObject(){
        return userObject;
    }
}

