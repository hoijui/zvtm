/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.wild.zuist;

import java.awt.Dimension;

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
    static final String _w = "w";
    static final String _h = "h";
    static final String _bw = "bw";
    static final String _bh = "bh";
    static final String _port = "port";
    static final String _cols = "cols";
    static final String _rows = "rows";
    static final String _col = "col";
    static final String _row = "row";
    
    int nbCols = 0;
    int nbRows = 0;
    
    ClusterNode[] nodes;
    
    Dimension size;
    
    public WallConfiguration(File configFile, boolean bezels){
        parseConfig(configFile, bezels);
    }
    
    public ClusterNode[] getNodes(){
        return nodes;
    }
    
    void parseConfig(File f, boolean bezels){
        Element root = parseXML(f).getDocumentElement();
        nbCols = Integer.parseInt(root.getAttribute(_cols));
        nbRows = Integer.parseInt(root.getAttribute(_rows));
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
        computeDimensions(bezels);
    }
    
    void computeDimensions(boolean bezels){
        int[] wnes = {0, 0, 0, 0};
        ViewPort v;
        int k;
        for (int i=0;i<nodes.length;i++){
            for (int j=0;j<nodes[i].viewports.length;j++){
                v = nodes[i].viewports[j];
                // west
                k = v.getX()-v.getW()/2-v.getBW();
                if (k < wnes[0]){wnes[0] = k;}
                // north
                k = v.getY()+v.getH()/2+v.getBH();
                if (k > wnes[1]){wnes[1] = k;}
                // east
                k = v.getX()+v.getW()/2+v.getBW();
                if (k > wnes[2]){wnes[2] = k;}
                // south
                k = v.getY()-v.getH()/2-v.getBH();
                if (k < wnes[3]){wnes[3] = k;}
            }
        }
        size = new Dimension(wnes[2]-wnes[0], wnes[1]-wnes[3]);
        if (bezels){
            // bezel horizontal thickness
            double bht = nodes[0].viewports[0].getBW() / ((double)size.width);
            // bezel vertical thickness
            double bvt = nodes[0].viewports[0].getBH() / ((double)size.height);
            for (int i=0;i<nodes.length;i++){
                for (int j=0;j<nodes[i].viewports.length;j++){
                    double[] dwnes = {(nodes[i].viewports[j].getColumn() == 0) ? 1/(double)nbCols * nodes[i].viewports[j].getColumn() : 1/(double)nbCols * nodes[i].viewports[j].getColumn() + bht,
                        (nodes[i].viewports[j].getRow() == 0) ? 1/(double)nbRows * (nodes[i].viewports[j].getRow()) : 1/(double)nbRows * (nodes[i].viewports[j].getRow()) + bvt,
                        (nodes[i].viewports[j].getColumn() == nbCols-1) ? 1/(double)nbCols * (nodes[i].viewports[j].getColumn()+1) : 1/(double)nbCols * (nodes[i].viewports[j].getColumn()+1) - bht,
                        (nodes[i].viewports[j].getRow() == nbRows-1) ? 1/(double)nbRows * (nodes[i].viewports[j].getRow()+1) : 1/(double)nbRows * (nodes[i].viewports[j].getRow()+1) - bvt
                    };
                    
                    System.out.println(nodes[i].viewports[j].getColumn()+" "+nodes[i].viewports[j].getRow());
                    System.out.println(dwnes[0]+" "+dwnes[1]+" "+dwnes[2]+" "+dwnes[3]);
                    nodes[i].viewports[j].setRelativeBounds(dwnes);
                }
            }
        }
        else {
            for (int i=0;i<nodes.length;i++){
                for (int j=0;j<nodes[i].viewports.length;j++){
                    double[] dwnes = {1/(double)nbCols * nodes[i].viewports[j].getColumn(),
                        1/(double)nbRows * (nodes[i].viewports[j].getRow()+1),
                        1/(double)nbCols * (nodes[i].viewports[j].getColumn()+1),
                        1/(double)nbRows * nodes[i].viewports[j].getRow()
                    };
                    nodes[i].viewports[j].setRelativeBounds(dwnes);
                }
            }
        }
    }
    
    public Dimension getSize(){
        return (Dimension)size.clone();
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
            Integer.parseInt(e.getAttribute(_w)),
            Integer.parseInt(e.getAttribute(_h)),
            Integer.parseInt(e.getAttribute(_bw)),
            Integer.parseInt(e.getAttribute(_bh)),
            Integer.parseInt(e.getAttribute(_col)),
            Integer.parseInt(e.getAttribute(_row)),
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
