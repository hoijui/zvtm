/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: PaperInfo.java,v 1.2 2007/10/07 02:19:28 pietriga Exp $
 */

package fr.inria.zuist.app.lri;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

class PaperInfo {
    
    String[] authorIDs;
    String[] keywordIDs; // null if empty
    
    PaperInfo(Element e){
        NodeList nl = ((Element)e.getElementsByTagName("authors").item(0)).getElementsByTagName("author");
        authorIDs = new String[nl.getLength()];
        for (int i=0;i<nl.getLength();i++){
            authorIDs[i] = ((Element)nl.item(i)).getAttribute("idref");
        }
        nl = e.getElementsByTagName("keywords");
        if (nl.getLength() > 0){
            nl = ((Element)nl.item(0)).getElementsByTagName("kw");
            keywordIDs = new String[nl.getLength()];
            for (int i=0;i<nl.getLength();i++){
                keywordIDs[i] = ((Element)nl.item(i)).getAttribute("idref");
            }
        }
    }

}
