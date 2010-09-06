/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.awt.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Vector;
import java.util.List;

import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.DPath;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

class AirTrafficManager {
    
	static int MAX_WEIGHT = 400000;
	
	static final int AIRPORT_NODE_SIZE = 25;
	
	static final float DEFAULT_ARC_ALPHA = 0.7f;
	static final double QUAD_ANGLE = Math.PI / 6.0;
	
	static final Color AIRPORT_FILL_COLOR = Color.YELLOW;
	static final Color AIRPORT_STROKE_COLOR = Color.BLACK;
	static final Color AIRPORT_LABEL_STROKE_COLOR = Color.BLACK;
	    
    static final String GML_FILE_PATH = "data/airports/airtraffic_2004.gml";
	static final String GEO_FILE_PATH = "data/airports/airports.csv";
	static final String INPUT_CSV_SEP = ";";
	
    LNode[] allNodes;
	LEdge[] allArcs;
	
    WorldExplorer application;
    
    AirTrafficManager(WorldExplorer app, boolean show){
        this.application = app;
        if (show){
            System.out.println("Loading air traffic information...");
            loadTraffic(loadAirports());
        }
    }
    
    HashMap<String,Airport> loadAirports(){
        HashMap<String,Airport> iata2airport = new HashMap();
		try {
			FileInputStream fis = new FileInputStream(new File(GEO_FILE_PATH));
			InputStreamReader isr = new InputStreamReader(fis, "UTF-8");
			BufferedReader br = new BufferedReader(isr);
			String line = br.readLine();
			while (line != null){
				if (line.length() > 0){
					String[] data = line.split(INPUT_CSV_SEP);
					if (iata2airport.containsKey(data[0])){
						System.err.println("Warning: airport "+data[0]+" defined multiple times: "+data[1]);
					}
					else {
						iata2airport.put(data[0], new Airport(data));
					}
				}
				line = br.readLine();
			}
			fis.close();
		}
		catch (IOException ex){
			ex.printStackTrace();
		}
		System.out.println("Loaded " + iata2airport.size() + " airport localizations");
		return iata2airport;
    }
    
    void loadTraffic(HashMap<String,Airport> iata2airport){
        try {
            GMLLexer lex = new GMLLexer(new ANTLRFileStream(GML_FILE_PATH));
           	CommonTokenStream tokens = new CommonTokenStream(lex);
            GMLParser parser = new GMLParser(tokens);
            GMLParser.gmlgr_return parserResult = parser.gmlgr();
            CommonTree ast = (CommonTree) parserResult.getTree();            
            List<CommonTree> nodes = ast.getChildren();
            HashMap<String,LNode> id2node = new HashMap();
            for (CommonTree node:nodes){
                if (node.getType() == GMLParser.NODE){
                    createAirportNode(node, id2node, iata2airport);
                }
            }
    		Vector<LNode> ans = new Vector(id2node.values());
    		allNodes = (LNode[])ans.toArray(new LNode[ans.size()]);
            Vector<LEdge> aes = new Vector();
            for (CommonTree node:nodes){
                if (node.getType() == GMLParser.EDGE){
                    LEdge e = createFlightEdge(node, id2node);
                    if (e != null){
                        aes.add(e);
                    }
                }
            }
            allArcs = (LEdge[])aes.toArray(new LEdge[aes.size()]);
    		System.out.println("Constructing " + allNodes.length + " airports");
    		System.out.println("Constructing " + allArcs.length + " connections");
            id2node.clear();    		
        } catch (RecognitionException e){
            e.printStackTrace();
        }
         catch (IOException e)  {
            e.printStackTrace();
        }
        iata2airport.clear();
    }
    
    static final String _id = "id";
    static final String _airport_code = "airport_code";
    static final String _weight = "weight";
    static final String _source = "source";
    static final String _target = "target";
    
    void createAirportNode(CommonTree node, HashMap<String,LNode> id2node, HashMap<String,Airport> iata2airport){
        String iataCode = null;
        String id = null;
        // get IATA code
        List<CommonTree> nodes = node.getChildren();
        for (CommonTree child:nodes){
            if (child.getText().equals(_id)){
                id = child.getChild(0).getText();
            }
            else if (child.getText().equals(_airport_code)){
                iataCode = child.getChild(0).getText();
                iataCode = iataCode.substring(1,iataCode.length()-1);
            }
        }
		Airport ap = iata2airport.get(iataCode);
        // not all airports from the traffic file are in our database of lat/lon coords
		if (ap == null){return;}
		double x = ap.lng * GeoToolsManager.CC;
		double y = ap.lat * GeoToolsManager.CC;
		VCircle shape = new VCircle(x, y, 10, AIRPORT_NODE_SIZE, AIRPORT_FILL_COLOR, AIRPORT_STROKE_COLOR, 1.0f);
		VText label = new VText(x, y-3, 10, AIRPORT_LABEL_STROKE_COLOR, ap.iataCode, VText.TEXT_ANCHOR_MIDDLE, 1.0f, 1);
		application.bSpace.addGlyph(shape);
		application.bSpace.addGlyph(label);
		id2node.put(id, new LNode(iataCode, ap.name, shape, label));
    }
    
    LEdge createFlightEdge(CommonTree node, HashMap<String,LNode> id2node){
		LEdge res = null;
		int weight = 0;
		String src = null;
		String tgt= null;
		List<CommonTree> nodes = node.getChildren();
        for (CommonTree child:nodes){
            if (child.getText().equals(_source)){
                src = child.getChild(0).getText();
            }
            else if (child.getText().equals(_target)){
                tgt = child.getChild(0).getText();
            }
            else if (child.getText().equals(_weight)){
                weight = Integer.parseInt(child.getChild(0).getText());
            }
        }
		//   2000 leaves 10412 edges
		// 500000 leaves 462 edges
		// 800000 leaves 120 edges
        if (weight > MAX_WEIGHT){
			LNode tail = id2node.get(src);
			LNode head = id2node.get(tgt);
			if (tail != null && head != null){
				double alpha = Math.atan2(head.getShape().vy-tail.getShape().vy,
				                          head.getShape().vx-tail.getShape().vx);					
				double ds = Math.sqrt(Math.pow((head.getShape().vx-tail.getShape().vx),2)+Math.pow((head.getShape().vy-tail.getShape().vy),2)) / 2.0;
				double rho = ds / Math.cos(QUAD_ANGLE);					
				double cx = tail.getShape().vx + rho*Math.cos(alpha+QUAD_ANGLE);
				double cy = tail.getShape().vy + rho*Math.sin(alpha+QUAD_ANGLE);
				DPath p = new DPath(tail.getShape().vx, tail.getShape().vy, 5, AIRPORT_FILL_COLOR, DEFAULT_ARC_ALPHA);
				p.addQdCurve(head.getShape().vx, head.getShape().vy, cx, cy, true);
				application.bSpace.addGlyph(p);
				res = new LEdge(weight, p);
				res.setDirected(true);
				res.setTail(tail);
				res.setHead(head);
			}
		}
		return res;
    }
    
}

class Airport {
	
	String iataCode;
	String name;
	double lat;
	double lng;
	
	Airport(String[] data){
		iataCode = data[0];
		name = data[1];
		lat = Double.parseDouble(data[2]);
		lng = Double.parseDouble(data[3]);
	}
	
}

abstract class LElem {

	abstract void setTranslucency(float a);
	
}

class LNode extends LElem {

    String code, name;

    LEdge[] edges;
    short[] edgeDirections;

	VCircle nodeShape;
	VText nodeLabel;

    LNode(String code, String name, VCircle nodeShape, VText nodeLabel){
        this.code = code;
        this.name = name;
		this.nodeShape = nodeShape;
		this.nodeLabel = nodeLabel;
		this.nodeShape.setOwner(this);
		this.nodeLabel.setOwner(this);
        edges = new LEdge[0];
        edgeDirections = new short[0];
    }
    
    String getCode(){
        return code;
    }

    String getName(){
        return name;
    }

	void addArc(LEdge e, short direction){
		LEdge[] nedges = new LEdge[edges.length+1];
		short[] nedgeDirections = new short[nedges.length];
		System.arraycopy(edges, 0, nedges, 0, edges.length);
		System.arraycopy(edgeDirections, 0, nedgeDirections, 0, edgeDirections.length);
		nedges[edges.length] = e;
		nedgeDirections[edgeDirections.length] = direction;
		edges = nedges;
		edgeDirections = nedgeDirections;
	}

	LEdge[] getAllArcs(){
		LEdge[] res = new LEdge[edges.length];
		System.arraycopy(edges, 0, res, 0, edges.length);
		return res;
	}
	
	/** Get all arcs incoming or outgoing from this node, except for the specified one. */
	LEdge[] getOtherArcs(LEdge arc){
		int count = 0;
		for (int i=0;i<edges.length;i++){
			if (arc != edges[i]){count++;}
		}
		LEdge[] res = new LEdge[count];
		int j = 0;
		for (int i=0;i<edges.length;i++){
			if (arc != edges[i]){res[j++] = edges[i];}
		}
		return res;
	}

	LEdge[] getOutgoingArcs(){
		int oaCount = 0;
		for (int i=0;i<edgeDirections.length;i++){
			if (edgeDirections[i] == LEdge.OUTGOING){oaCount++;}
		}
		LEdge[] res = new LEdge[oaCount];
		int j = 0;
		for (int i=0;i<edges.length;i++){
			if (edgeDirections[i] == LEdge.OUTGOING){
				res[j++] = edges[i];
			}
		}
		return res;
	}

	LEdge[] getIncomingArcs(){
		int oaCount = 0;
		for (int i=0;i<edgeDirections.length;i++){
			if (edgeDirections[i] == LEdge.INCOMING){oaCount++;}
		}
		LEdge[] res = new LEdge[oaCount];
		int j = 0;
		for (int i=0;i<edges.length;i++){
			if (edgeDirections[i] == LEdge.INCOMING){
				res[j++] = edges[i];
			}
		}
		return res;
	}

	LEdge[] getUndirectedArcs(){
		int oaCount = 0;
		for (int i=0;i<edgeDirections.length;i++){
			if (edgeDirections[i] == LEdge.UNDIRECTED){oaCount++;}
		}
		LEdge[] res = new LEdge[oaCount];
		int j = 0;
		for (int i=0;i<edges.length;i++){
			if (edgeDirections[i] == LEdge.UNDIRECTED){
				res[j++] = edges[i];
			}
		}
		return res;
	}
	
	LEdge getArcLeadingTo(LNode n){
		for (int i=0;i<edges.length;i++){
			if (edges[i].getOtherEnd(this) == n){
				return edges[i];
			}
		}
		return null;
	}

	VCircle getShape(){
		return nodeShape;
	}
	
	VText getLabel(){
		return nodeLabel;
	}
	
	void setTranslucency(float a){
		nodeShape.setTranslucencyValue(a);
		nodeLabel.setTranslucencyValue(a);
	}

	public String toString(){
		String res = code + " " + name + "[";
		for (int i=0;i<edges.length;i++){
			res += ((edges[i] != null) ? edges[i].weight + "@" + edges[i].hashCode() : "NULL") + "(" + edgeDirections[i] + ") ";
		}
		res += "]";
		return res;
	}

}

class LEdge extends LElem {

	static final short UNDIRECTED = 0;
	static final short INCOMING = 1;
	static final short OUTGOING = 2;

	static final String UNDIRECTED_STR = "--";
	static final String DIRECTED_STR = "->";

	boolean directed = false;

	int weight;

	LNode tail;
	LNode head;
	
	DPath edgeSpline;

	LEdge(int weight, DPath edgeSpline){
		this.weight = weight;
		this.edgeSpline = edgeSpline;
		this.edgeSpline.setOwner(this);
	}

	void setDirected(boolean b){
		directed = b;
	}

	boolean isDirected(){
		return directed;
	}

	boolean isLoop(){
		return tail == head;
	}

	void setTail(LNode n){
		tail = n;
		if (tail != null){
			tail.addArc(this, (directed) ? LEdge.OUTGOING : LEdge.UNDIRECTED);
		}
	}

	void setHead(LNode n){
		head = n;
		if (head != null){
			head.addArc(this, (directed) ? LEdge.INCOMING : LEdge.UNDIRECTED);
		}
	}
	
	boolean isConnectedTo(LNode n){
		return (n == head) || (n == tail);
	}

	LNode getTail(){
		return tail;
	}

	LNode getHead(){
		return head;
	}

	LNode getOtherEnd(LNode n){
		return (n == tail) ? head : tail;
	}

	DPath getSpline(){
		return edgeSpline;
	}

	void setTranslucency(float a){
		edgeSpline.setTranslucencyValue(a);
	}

	public String toString(){
		return weight + "@" + hashCode() + " [" + 
			((tail != null) ? tail.getCode() + "@" + tail.hashCode() : "NULL")+
			((directed) ? LEdge.DIRECTED_STR : LEdge.UNDIRECTED_STR) +
			((head != null) ? head.getCode() + "@" + head.hashCode() : "NULL") +
			"]";
	}

}
