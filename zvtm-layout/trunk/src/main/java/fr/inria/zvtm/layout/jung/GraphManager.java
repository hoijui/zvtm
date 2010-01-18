/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.layout.jung;

import java.awt.Dimension;

import java.io.File;
import java.io.FileReader;
import java.util.Set;
import java.util.Map;

import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VSegment;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.io.GraphMLReader;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;

import org.apache.commons.collections15.Factory;

class GraphManager {
    
    Viewer application;
    SparseMultigraph<Glyph,Glyph> graph;
    CircleLayout<Glyph,Glyph> layout;
    
    GraphManager(Viewer app){
        this.application = app;
    }
    
    void loadGraphML(File f){
        Factory<Glyph> vertexFactory = new Factory<Glyph>() {
    		public Glyph create() { return (Glyph)new VCircle(0, 0, 0, ConfigManager.DEFAULT_NODE_SIZE, ConfigManager.DEFAULT_NODE_COLOR);}
    	};
        Factory<Glyph> edgeFactory = new Factory<Glyph>() {
        	public Glyph create() {return (Glyph)new VSegment(0, 0, 0, java.awt.Color.BLACK, 0, 0);}
        };
            	
        try {
            GraphMLReader<SparseMultigraph<Glyph,Glyph>, Glyph, Glyph> gr =
                new GraphMLReader<SparseMultigraph<Glyph,Glyph>, Glyph,Glyph>(vertexFactory, edgeFactory);
            graph = new SparseMultigraph<Glyph,Glyph>();
            gr.load(new FileReader(f), graph);
            System.out.println("V:"+graph.getVertexCount()+" E:"+graph.getEdgeCount());
        }
        catch (Exception ex){
            System.err.println("Error loading graph:" + f.toString());
            ex.printStackTrace();
        }
    }
    
    void createLayout(){
        layout = new CircleLayout<Glyph,Glyph>(graph);
        layout.setSize(new Dimension(ConfigManager.DEFAULT_GRAPH_SIZE, ConfigManager.DEFAULT_GRAPH_SIZE));
        layout.initialize();
    }
    
    void incLayout(){
        if (layout != null){
            //layout.step();
        }
    }
    
}

// XXX: GET NODE EDGE LABEL FROM METADATA SSOCIATED WITH THEM
// XXX: DO THE LAYOUT