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
import java.util.Collection;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VSegment;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.io.GraphMLReader;
import edu.uci.ics.jung.graph.util.Pair;

import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;

import org.apache.commons.collections15.Factory;

class GraphManager {
    
    Viewer application;
    SparseMultigraph<Glyph,DPath> graph;
    //CircleLayout<Glyph,DPath> layout;
    SpringLayout<Glyph,DPath> layout;
    
    GraphManager(Viewer app){
        this.application = app;
    }
    
    void loadGraphML(File f){
        Factory<Glyph> vertexFactory = new Factory<Glyph>() {
    		public Glyph create() { return (Glyph)new VCircle(0, 0, 0, ConfigManager.DEFAULT_NODE_SIZE, ConfigManager.DEFAULT_NODE_COLOR);}
    	};
        Factory<DPath> edgeFactory = new Factory<DPath>() {
        	public DPath create() {return new DPath(0, 0, 0, java.awt.Color.BLACK);}
        };
            	
        try {
            GraphMLReader<SparseMultigraph<Glyph,DPath>, Glyph, DPath> gr =
                new GraphMLReader<SparseMultigraph<Glyph,DPath>, Glyph, DPath>(vertexFactory, edgeFactory);
            graph = new SparseMultigraph<Glyph,DPath>();
            gr.load(new FileReader(f), graph);
            // System.out.println("V:"+graph.getVertexCount()+" E:"+graph.getEdgeCount());
            Map o2id = gr.getVertexIDs();
            for (Glyph g:(Set<Glyph>)o2id.keySet()){
                g.setOwner(o2id.get(g));
            }
            o2id = gr.getEdgeIDs();
            for (DPath g:(Set<DPath>)o2id.keySet()){
                g.setOwner(o2id.get(g));
            }
        }
        catch (Exception ex){
            System.err.println("Error loading graph:" + f.toString());
            ex.printStackTrace();
        }
    }
    
    void createLayout(){
        layout = new SpringLayout<Glyph,DPath>(graph);
        layout.setSize(new Dimension(ConfigManager.GRAPH_SIZE_FACTOR*graph.getVertexCount(), ConfigManager.GRAPH_SIZE_FACTOR*graph.getVertexCount()));
        layout.initialize();
        for (DPath d:graph.getEdges()){
            Pair p = graph.getEndpoints(d);
            d.moveTo(Math.round(layout.getX((Glyph)p.getFirst())), Math.round(layout.getY((Glyph)p.getFirst())));
            d.addSegment(Math.round(layout.getX((Glyph)p.getSecond())), Math.round(layout.getY((Glyph)p.getSecond())), true);
            application.mSpace.addGlyph(d);
        }
        for (Glyph g:graph.getVertices()){
            g.moveTo(Math.round(layout.getX(g)), Math.round(layout.getY(g)));
            application.mSpace.addGlyph(g);
        }
    }
    
    void incLayout(){
        if (layout != null){
            layout.step();
            for (Glyph g:graph.getVertices()){
                g.moveTo(Math.round(layout.getX(g)), Math.round(layout.getY(g)));
            }
            LongPoint[] points = new LongPoint[2];
            for (DPath d:graph.getEdges()){
                Pair p = graph.getEndpoints(d);
                points[0] = new LongPoint(Math.round(layout.getX((Glyph)p.getFirst())), Math.round(layout.getY((Glyph)p.getFirst())));
                points[1] = new LongPoint(Math.round(layout.getX((Glyph)p.getSecond())), Math.round(layout.getY((Glyph)p.getSecond())));
                d.edit(points, true);
            }
        }
    }
    
}
