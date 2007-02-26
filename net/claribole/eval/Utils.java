/*   FILE: Utils.java
 *   DATE OF CREATION:  Mon Feb 26 13:55:06 2007
 *   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   MODIF:             Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2007. All Rights Reserved
 *
 * $Id:  $
 */

package net.claribole.eval;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Utils {

    public static Document parseXML(File f, boolean validation){ 
	try {
	    DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	    factory.setValidating(validation);
	    if (!validation){factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));}
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document res = builder.parse(f);
	    return res;
	}
	catch (FactoryConfigurationError e){e.printStackTrace();return null;} 
	catch (ParserConfigurationException e){e.printStackTrace();return null;}
	catch (SAXException e){e.printStackTrace();return null;}
	catch (IOException e){e.printStackTrace();return null;}
    }
}
