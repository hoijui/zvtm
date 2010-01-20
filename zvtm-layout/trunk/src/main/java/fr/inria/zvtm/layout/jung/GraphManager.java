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
import java.util.Timer;
import java.util.TimerTask;

import fr.inria.zvtm.engine.VirtualSpaceManager;
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
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.AbstractLayout;

import org.apache.commons.collections15.Factory;

class GraphManager {
    
    static final int LAYOUT_UPDATE_FREQUENCY = 50;
    
    static final short LAYOUT_SPRING = 0;
	static final short LAYOUT_CIRCLE = 1;
	static final short LAYOUT_KK = 2;
	static final short LAYOUT_ISOM = 3;
	static final short LAYOUT_FR = 4;
    short cla = LAYOUT_SPRING;
    LayoutUpdater lu;
    
    Viewer application;
    SparseMultigraph<Glyph,DPath> graph;
    AbstractLayout<Glyph,DPath> layout;
    
    GraphManager(Viewer app){
        this.application = app;
        Timer timer = new Timer();
		lu = new LayoutUpdater(this);
		timer.scheduleAtFixedRate(lu, LAYOUT_UPDATE_FREQUENCY, LAYOUT_UPDATE_FREQUENCY);
    }
    
    void loadGraphML(File f){
        if (lu.isEnabled()){
            lu.setEnabled(false);
        }
        Factory<Glyph> vertexFactory = new Factory<Glyph>() {
    		public Glyph create() {return (Glyph)new VCircle(0, 0, 0, ConfigManager.DEFAULT_NODE_SIZE, ConfigManager.DEFAULT_NODE_COLOR);}
    	};
        Factory<DPath> edgeFactory = new Factory<DPath>() {
        	public DPath create() {return new DPath(0, 0, 0, ConfigManager.DEFAULT_EDGE_COLOR);}
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
    
    /* --------------------- Layout ------------------------*/
    
    void chooseLayout(short l){
        cla = l;
		switch(cla){
			case LAYOUT_SPRING:{changeLayout(new SpringLayout<Glyph,DPath>(graph));break;}
			case LAYOUT_CIRCLE:{changeLayout(new CircleLayout<Glyph,DPath>(graph));break;}
			case LAYOUT_KK:{changeLayout(new KKLayout<Glyph,DPath>(graph));break;}
			case LAYOUT_ISOM:{changeLayout(new ISOMLayout<Glyph,DPath>(graph));break;}
			case LAYOUT_FR:{changeLayout(new FRLayout<Glyph,DPath>(graph));break;}
		}
	}
	
	void changeLayout(AbstractLayout<Glyph,DPath> al){
		layout = al;
		layout.setSize(new Dimension(ConfigManager.GRAPH_SIZE_FACTOR*graph.getVertexCount(), ConfigManager.GRAPH_SIZE_FACTOR*graph.getVertexCount()));
		layout.initialize();
		//Iterator i = layout.getVisibleEdges().iterator();
		//while (i.hasNext()){
		//	Edge e = (Edge)i.next();
		//	DPath p = (DPath)e.getUserDatum(_glyph);
		//	EdgeTransformer.updateLine(e, layout, p, GraphicsManager.ANIM_MOVE_DURATION, grMngr.vsm.animator);
		//}
		//i = layout.getVisibleVertices().iterator();
		//while (true){
		//	Vertex v = (Vertex)i.next();
		//	Coordinates c = layout.getCoordinates(v);
        //
		//	Glyph cl = (Glyph)v.getUserDatum(_glyph);			
		//	LongPoint translation = new LongPoint((long)c.getX()-cl.vx, (long)c.getY()-cl.vy);
		//	VTextST t = (VTextST)v.getUserDatum(_glyphLabel);
		//	// label
		//	if (t != null){
		//		grMngr.vsm.animator.createGlyphAnimation(GraphicsManager.ANIM_MOVE_DURATION, AnimManager.GL_TRANS_SIG,
		//		                                         translation, t.getID());
		//	}
		//	// node shape
		//	if (i.hasNext()){
		//		grMngr.vsm.animator.createGlyphAnimation(GraphicsManager.ANIM_MOVE_DURATION, AnimManager.GL_TRANS_SIG,
		//		                                         translation, cl.getID());
		//	}
		//	else {
		//		// one call to update overview (triggered after last animation has ended)
		//		grMngr.vsm.animator.createGlyphAnimation(GraphicsManager.ANIM_MOVE_DURATION, AnimManager.GL_TRANS_SIG,
		//		                                         translation, cl.getID(),
		//		                                         new PostAnimationAdapter(){
		//			public void animationEnded(Object target, short type, String dimension){grMngr.updateOverview();}
		//		});
		//		break;
		//	}
		//}		
	}
	
    void createInitialLayout(){
        layout = new SpringLayout<Glyph,DPath>(graph);
        layout.setSize(new Dimension(ConfigManager.GRAPH_SIZE_FACTOR*graph.getVertexCount(), ConfigManager.GRAPH_SIZE_FACTOR*graph.getVertexCount()));
        layout.initialize();
        initializeGraphics();
    }
    
    void initializeGraphics(){
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
            switch(cla){
    			case LAYOUT_SPRING:{((SpringLayout<Glyph,DPath>)layout).step();break;}
    			case LAYOUT_KK:{((KKLayout<Glyph,DPath>)layout).step();break;}
    			case LAYOUT_ISOM:{((ISOMLayout<Glyph,DPath>)layout).step();break;}
    			case LAYOUT_FR:{((FRLayout<Glyph,DPath>)layout).step();break;}
    		}
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
    
    void toggleLayoutUpdate(){
        lu.setEnabled(!lu.isEnabled());
        VirtualSpaceManager.INSTANCE.repaintNow();
    }
    
}

class LayoutUpdater extends TimerTask {

    private boolean enabled = false;
    GraphManager gm;

	LayoutUpdater(GraphManager gm){
		super();
		this.gm = gm;
	}

	public void setEnabled(boolean b){
		enabled = b;
	}
    
	public boolean isEnabled(){
		return enabled;
	}
    
	public void run(){		
		if (enabled){
		    for (short i=0;i<ConfigManager.NUMBER_OF_UPDATES_PER_CYCLE;i++){
    			gm.incLayout();		        
		    }
		}
	}

}
