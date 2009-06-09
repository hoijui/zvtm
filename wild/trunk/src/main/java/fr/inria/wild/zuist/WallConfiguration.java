/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.wild.zuist;

import java.io.File;
import java.io.IOException;

import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

public class WallConfiguration {
    
    static final String _node = "node";
    static final String _viewport = "viewport";
    static final String _wall = "wall";
    static final String _name = "name";
    static final String _device = "device";
    static final String _dx = "dx";
    static final String _dy = "dy";
    static final String _port = "port";
    
    ClusterNode[] nodes;
    
    public WallConfiguration(File configFile){
        parseConfig(configFile);
    }
    
    public ClusterNode[] getNodes(){
        return nodes;
    }
    
    void parseConfig(File f){
        Element root = parseXML(f).getDocumentElement();
        NodeList nl = root.getChildNodes();
        Element e;
        Node n;
        Vector cnv = new Vector();
        for (int i=0;i<nl.getLength();i++){
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE){
                e = (Element)n;
                if (e.getTagName().equals(_node)){
                    cnv.add(parseClusterNode(e));
                }
            }
        }
        nodes = (ClusterNode[])cnv.toArray(new ClusterNode[cnv.size()]);
    }
    
    ClusterNode parseClusterNode(Element e){
        ClusterNode cn = new ClusterNode(e.getAttribute(_name));
        NodeList nl = e.getChildNodes();
        Element e2;
        Node n;
        for (int i=0;i<nl.getLength();i++){
            n = nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE){
                e2 = (Element)n;
                if (e2.getTagName().equals(_viewport)){
                    parseViewPort(e2, cn);
                }
            }
        }
        return cn;
    }
    
    void parseViewPort(Element e, ClusterNode cn){
        cn.addViewPort(Integer.parseInt(e.getAttribute(_dx)),
            Integer.parseInt(e.getAttribute(_dy)),
            Short.parseShort(e.getAttribute(_device)),
            Integer.parseInt(e.getAttribute(_port)));
    }
    
    public static Document parseXML(File f){
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));
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
