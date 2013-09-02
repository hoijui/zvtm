/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2010-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.app.wm;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.util.HashMap;
import java.util.Vector;
import java.util.List;
import java.util.Arrays;
import java.util.Iterator;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.animation.EndAction;
import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;

import org.antlr.runtime.ANTLRFileStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;

class AirTrafficManager {

    static int MIN_WEIGHT = 500000;

    static final int AIRPORT_NODE_SIZE = 25;

    static final float DEFAULT_ARC_ALPHA = 1f;
    static float EDGE_STROKE_WIDTH = 1f;
    static final double QUAD_ANGLE = Math.PI / 6.0;

    static final Color AIRPORT_FILL_COLOR = Color.YELLOW;
    static final Color AIRPORT_STROKE_COLOR = Color.BLACK;
    static final Color AIRPORT_LABEL_STROKE_COLOR = Color.BLACK;

    static final String GML_FILE_PATH = "data/airports/airtraffic_2004.gml";
    static final String GEO_FILE_PATH = "data/airports/airports.csv";
    static final String INPUT_CSV_SEP = ";";

    static final String AIRP = "AIR";

    LNode[] allNodes;
    LEdge[] allArcs;

    WorldExplorer application;

    boolean isShowing = false;

    AnimationManager AM;

    AirTrafficManager(WorldExplorer app, boolean show){
        this.application = app;
        AM = VirtualSpaceManager.INSTANCE.getAnimationManager();
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
            //for (LNode airport:allNodes){
            //    application.bSpace.onTop(airport.getShape());
            //    application.bSpace.onTop(airport.getLabel());
            //}
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
        isShowing = true;
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
        shape.setType(AIRP);
        VText label = new VText(x, y-3, 10, AIRPORT_LABEL_STROKE_COLOR, ap.iataCode, VText.TEXT_ANCHOR_MIDDLE, 1.0f, 1);
        label.setSensitivity(false);
        application.bSpace.addGlyph(shape);
        application.bSpace.addGlyph(label);
        Glyph.stickToGlyph(label, shape);
        id2node.put(id, new LNode(iataCode, ap.name, ap.lat, ap.lng, shape, label));
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
        if (weight > MIN_WEIGHT){
            LNode tail = id2node.get(src);
            LNode head = id2node.get(tgt);
            if (tail != null && head != null){
                double alpha = Math.atan2(head.getShape().vy-tail.getShape().vy,
                                          head.getShape().vx-tail.getShape().vx);
                double ds = Math.sqrt((head.getShape().vx-tail.getShape().vx)*(head.getShape().vx-tail.getShape().vx) + (head.getShape().vy-tail.getShape().vy)*(head.getShape().vy-tail.getShape().vy)) / 2.0;
                double rho = ds / Math.cos(QUAD_ANGLE);
                double cx = tail.getShape().vx + rho*Math.cos(alpha+QUAD_ANGLE);
                double cy = tail.getShape().vy + rho*Math.sin(alpha+QUAD_ANGLE);
                DPath p = new DPath(tail.getShape().vx, tail.getShape().vy, 5, AIRPORT_FILL_COLOR, DEFAULT_ARC_ALPHA);
                //p.setStroke(new BasicStroke(EDGE_STROKE_WIDTH));
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

    static float MIN_ALPHA = .2f;
    static float MAX_ALPHA = 1f;
    float alpha_a = 0;
    float alpha_b = 1f;

    void setTranslucencyByWeight(){
        int minW = Integer.MAX_VALUE;
        int maxW = 0;
        // find min and max weights for current network
        for (LEdge e:allArcs){
            if (e.weight > maxW){maxW = e.weight;}
            if (e.weight < minW){minW = e.weight;}
        }
        alpha_a = (MAX_ALPHA - MIN_ALPHA) / (float)(maxW - minW);
        alpha_b = (MIN_ALPHA * maxW - MAX_ALPHA * minW) / (float)(maxW - minW);
        // assign alpha value based on min weight, max weight and edge's weight
        for (LEdge e:allArcs){
            e.getSpline().setTranslucencyValue(alpha_a*e.weight + alpha_b);
        }
    }

    /* ------------------ network visibility ---------------------------- */

    void toggleTraffic(){
        showNetwork(!isShowing);
    }

    void showNetwork(boolean b){
        if (allArcs == null){return;}
        for (final LEdge e:allArcs){
            if (b){
                application.bSpace.show(e.getSpline());
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(NavigationManager.ANIM_MOVE_DURATION, e.getSpline(),
                    1f, false, IdentityInterpolator.getInstance(), null);
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
            else {
                Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createTranslucencyAnim(NavigationManager.ANIM_MOVE_DURATION, e.getSpline(),
                    0, false, IdentityInterpolator.getInstance(),
                    new EndAction(){
                        public void execute(Object subject, Animation.Dimension dimension){
                            application.bSpace.hide(e.getSpline());
                        }
                    });
                VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
            }
        }
        for (final LNode e:allNodes){
            if (b){
                application.bSpace.show(e.getShape());
                application.bSpace.show(e.getLabel());
            }
            else {
                application.bSpace.hide(e.getShape());
                application.bSpace.hide(e.getLabel());
            }
        }
        isShowing = b;
    }

    /* ------------------ highlighting ---------------------------- */

    static final Color HIGHLIGHT_COLOR = Color.RED;
    static final float DIMMED_ARC_ALPHA = 0.2f;

    Vector highlightedElements = new Vector();
    Vector dimmedElements = new Vector();

    Glyph[] airportInfo = new Glyph[5];

    boolean isHighlighting = false;

    void highlight(Glyph g){
        g.setColor(HIGHLIGHT_COLOR);
        LNode n = (LNode)g.getOwner();
        if (n != null){
            for (int i=0;i<allArcs.length;i++){
                dimmedElements.add(allArcs[i].getSpline());
            }
            LEdge[] arcs = n.getAllArcs();
            Glyph g2;
            for (int i=0;i<arcs.length;i++){
                dimmedElements.remove(arcs[i].getSpline());
                g2 = arcs[i].getSpline();
                g2.setColor(HIGHLIGHT_COLOR);
                //g2.setStroke(new BasicStroke(EDGE_STROKE_WIDTH*2));
                g2.setTranslucencyValue(1f);
                application.bSpace.onTop(g2, 5);
                highlightedElements.add(g2);
                g2 = arcs[i].getOtherEnd(n).getShape();
                g2.setColor(HIGHLIGHT_COLOR);
                highlightedElements.add(g2);
            }
        //  airportInfo[0] = new VRectangle(n.getShape().vx+100, n.getShape().vy+100, 20, 400, 100, Color.BLACK, Color.WHITE, .8f);
        //  airportInfo[1] = new VText(airportInfo[0].vx-190, airportInfo[0].vy+30, 20, Color.WHITE, "IATA: "+n.getCode());
        //  airportInfo[2] = new VText(airportInfo[0].vx-190, airportInfo[0].vy+10, 20, Color.WHITE, "Name: "+n.getName());
        //  airportInfo[3] = new VText(airportInfo[0].vx-190, airportInfo[0].vy-10, 20, Color.WHITE, "Lat: "+n.getLatitude());
        //  airportInfo[4] = new VText(airportInfo[0].vx-190, airportInfo[0].vy-30, 20, Color.WHITE, "Lon: "+n.getLongitude());
        //  for (Glyph g3:airportInfo){
        //      application.bSpace.addGlyph(g3);
        //  }
            isHighlighting = true;
        }
    }

    void unhighlight(Glyph g){
        if (g == null){return;}
        g.setColor(AIRPORT_FILL_COLOR);
    //  for (Glyph g3:airportInfo){
    //        if (g3 == null){continue;}
    //      application.bSpace.removeGlyph(g3);
    //  }
        Glyph g2;
        for (int i=0;i<highlightedElements.size();i++){
            g2 = (Glyph)highlightedElements.elementAt(i);
            g2.setColor(AIRPORT_FILL_COLOR);
            //g2.setStroke(new BasicStroke(EDGE_STROKE_WIDTH));
            if (g2 instanceof DPath){
                g2.setTranslucencyValue(alpha_a*((LEdge)g2.getOwner()).weight + alpha_b);
            }
            application.bSpace.atBottom(g2, 0);
        }
        highlightedElements.clear();
        dimmedElements.clear();
        isHighlighting = false;
    }

    /* ---------------------- Bring and go -----------------------------*/

    static final int BRING_ANIM_DURATION = 1000;
    static final int FOLLOW_ANIM_DURATION = 3000;
    static final double BRING_DISTANCE_FACTOR = 1.5;

    static final float SECOND_BNG_STEP_TRANSLUCENCY = 0.2f;
    static final float OUTSIDE_BNG_SCOPE_TRANSLUCENCY = 0.05f;
    static final float[] FADE_IN_ANIM = {0,0,0,0,0,0,1-OUTSIDE_BNG_SCOPE_TRANSLUCENCY};
    static final float[] FADE_OUT_ANIM = {0,0,0,0,0,0,OUTSIDE_BNG_SCOPE_TRANSLUCENCY-1};

    static final Color BNG_SHAPE_FILL_COLOR = Color.RED;

    boolean isBringingAndGoing = false;

    Vector<LNode> broughtStack = new Vector();

    HashMap<LElem,BroughtElement> brought2location = new HashMap();
    HashMap<LNode,LNode> broughtnode2broughtby = new HashMap();

    Vector<LNode> nodesOutsideScope;
    Vector<LEdge> arcsOutsideScope;

    void attemptToBring(Glyph g){
        LNode n = (LNode)g.getOwner();
        if (n == null){return;}
        int iis = broughtStack.indexOf(n);
        if (iis == -1){
            // entered a node that is not on the stack of nodes visited during this bring and go
            bringFor(n);
            // if there was a previous node in the stack, send back its brought nodes where they belong
            // (provided they are not brought by the new one we've just been working on)
            // do it only for previous node on stack; those before should have been taken care of when the
            // previous one was dealt with in here
            if (broughtStack.size() > 1){fadeStack(broughtStack.elementAt(broughtStack.size()-2), n);}
        }
        else {
            // entered a node previously visited during this bring and go
            // send back all nodes and arcs brought by subsequent steps of the bring and go
            for (int i=broughtStack.size()-1;i>=iis+1;i--){
                LNode n2 = broughtStack.elementAt(i);
                n2.getShape().setColor(AIRPORT_FILL_COLOR);
                broughtStack.remove(n2);
                sendBackFor(n2, n, n2.getArcLeadingTo(n) == null);
            }
        }
    }

    void bringFor(Glyph g){
        nodesOutsideScope = new Vector(Arrays.asList(allNodes));
        arcsOutsideScope = new Vector(Arrays.asList(allArcs));
        bringFor((LNode)g.getOwner());
    }

    void bringFor(LNode n){
        if (n == null){return;}
        isBringingAndGoing = true;
        broughtStack.add(n);
        ClosedShape thisEndShape = n.getShape();
        nodesOutsideScope.remove(n);
        thisEndShape.setColor(BNG_SHAPE_FILL_COLOR);
        double thisEndBoundingCircleRadius = thisEndShape.getSize();
        // distance between two rings
        double RING_STEP = 4 * thisEndBoundingCircleRadius;
        LEdge[] arcs = n.getAllArcs();
        // sort them according to distance from start node
        // (so as to try to keep the closest ones closer to the start node)
        Arrays.sort(arcs, new DistanceComparator(n));
        HashMap<LNode,Point2D.Double> node2bposition = new HashMap();
        RingManager rm = new RingManager();
        Vector<LEdge> arcsToBring = new Vector();
        // compute the position of nodes to be brought
        for (int i=0;i<arcs.length;i++){
            if (arcs[i].isLoop()){continue;}
            LNode otherEnd = arcs[i].getOtherEnd(n);
            // do not bring nodes that are on the bring and go stack (these nodes are frozen during the whole bring and go)
            // otherwise it would be a mess (they would move when entering one of the nodes brought by them)
            if (broughtStack.contains(otherEnd)){continue;}
            ClosedShape otherEndShape = otherEnd.getShape();
            double d = Math.sqrt((otherEndShape.vx-thisEndShape.vx)*(otherEndShape.vx-thisEndShape.vx) + (otherEndShape.vy-thisEndShape.vy)*(otherEndShape.vy-thisEndShape.vy));
            Ring ring = rm.getRing(Math.atan2(otherEndShape.vy-thisEndShape.vy, otherEndShape.vx-thisEndShape.vx), otherEndShape.getSize(), RING_STEP);
            double bd = ring.rank * RING_STEP;
            double ratio = bd / d;
            double bx = thisEndShape.vx + ratio * (otherEndShape.vx-thisEndShape.vx);
            double by = thisEndShape.vy + ratio * (otherEndShape.vy-thisEndShape.vy);
            arcsToBring.add(arcs[i]);
            node2bposition.put(otherEnd, new Point2D.Double(bx, by));
        }
        // actually bring the arcs and nodes
        for (int i=0;i< arcsToBring.size();i++){
            LEdge e = arcsToBring.elementAt(i);
            LNode otherEnd = e.getOtherEnd(n);
            ClosedShape otherEndShape = otherEnd.getShape();
            bring(e, otherEnd, n, thisEndShape.vx, thisEndShape.vy, otherEndShape.vx, otherEndShape.vy, node2bposition);
        }
    }

    // n1 is the node for which we attempt to send back connected nodes
    // n2 is the new center of the bring and go, so we do not send back nodes connected to n1 that are also connected to n2
    void sendBackFor(LNode n1, LNode n2, boolean nodeItself){
        if (nodeItself){
            BroughtElement be = brought2location.get(n1);
            be.restorePreviousState(BRING_ANIM_DURATION);
            // get the remembered location as the animation won't have finished before we need that
            // location to compute new edge end points
            updateEdges(n1, ((BroughtNode)be).previousLocation);
            Vector<LNode> nodesToSendBack = new Vector();
            synchronized(broughtnode2broughtby){
                Iterator<LNode> it = broughtnode2broughtby.keySet().iterator();
                while (it.hasNext()){
                    LNode n = it.next();
                    if (n != n2 && broughtnode2broughtby.get(n) == n1){
                        // do not send back n2, obviously
                        nodesToSendBack.add(n);
                    }
                }
                for (int i=0;i<nodesToSendBack.size();i++){
                    LNode n = nodesToSendBack.elementAt(i);
                    sendBack(n);
                    sendBack(n.getArcLeadingTo(n1));
                    broughtnode2broughtby.remove(n);
                }
            }
        }
        else {
            LEdge[] arcs = n2.getAllArcs();
            LNode oe;
            for (int i=0;i<arcs.length;i++){
                oe = arcs[i].getOtherEnd(n2);
                oe.getShape().setSensitivity(true);
                LEdge[] arcs2 = oe.getOtherArcs(arcs[i]);
            }
        }
    }

    void updateEdges(LNode n, Point2D.Double p){
        LEdge[] arcs = n.getAllArcs();
        for (int i=0;i<arcs.length;i++){
            DPath spline = arcs[i].edgeSpline;
            LNode oe = arcs[i].getOtherEnd(n);
            Point2D.Double asp = spline.getStartPoint();
            Point2D.Double aep = spline.getEndPoint();
            Point2D.Double sp, ep;
            if (Math.sqrt((p.x-aep.x)*(p.x-aep.x) + (p.y-aep.y)*(p.y-aep.y)) < Math.sqrt((p.x-asp.x)*(p.x-asp.x) + (p.y-asp.y)*(p.y-asp.y))){
                sp = oe.getShape().getLocation();
                ep = p;
            }
            else {
                sp = p;
                ep = oe.getShape().getLocation();
            }
            Point2D.Double[] splineCoords = DPath.getFlattenedCoordinates(spline, sp, ep, true);
            Animation a = AM.getAnimationFactory().createPathAnim(NavigationManager.ANIM_MOVE_DURATION, spline,
                splineCoords, false, SlowInSlowOutInterpolator.getInstance(), null);
            AM.startAnimation(a, true);
        }
    }

    void endBringAndGo(Glyph g){
        //XXX:TBW if g is null, or not the latest node in the bring and go stack, go back to initial state
        //        else send all nodes and edges to their initial position, but also move camera to g
        isBringingAndGoing = false;
        LNode followedNode = null;
        if (g != null){
            LNode n = (LNode)g.getOwner();
            BroughtNode bn = (BroughtNode)brought2location.get(n);
            if (bn != null && broughtStack.indexOf(n) != 0){
                // do not translate if node in which we release button is the node
                // in which the bring and go was inititated (unlikely the user wants to move)
                //XXX:TBW add more tests to do this translation only if its worth it (far away enough)
                // translate camera to node in which button was released
                followedNode = n;
                Point2D.Double lp = bn.previousLocation;
                Point2D.Double trans = new Point2D.Double((lp.x-application.mCamera.vx)/2d, (lp.y-application.mCamera.vy)/2d);

                //XXX:TBW compute altitude offset to see everything on the path at apex, but not higher
                double zoomout = 1000;
                //XXX:TBW compute altitude offset to see everything on the path at apex, but not higher
                double zoomin = -1000;

                application.sm.setUpdateLevel(false);
                if (application.isAAEnabled()){
                    // temporarily disable antialiasing for perf reasons
                    application.mView.setAntialiasing(false);
                }

                Animation a1 = AM.getAnimationFactory().createCameraAltAnim(FOLLOW_ANIM_DURATION/2, application.mCamera,
                    zoomout, true, IdentityInterpolator.getInstance(), null);
                Animation a2 = AM.getAnimationFactory().createCameraTranslation(FOLLOW_ANIM_DURATION/2, application.mCamera,
                    trans, true, IdentityInterpolator.getInstance(), null);
                AM.startAnimation(a1, false);
                AM.startAnimation(a2, false);
                a1 = AM.getAnimationFactory().createCameraAltAnim(FOLLOW_ANIM_DURATION/2, application.mCamera,
                    zoomin, true, IdentityInterpolator.getInstance(), null);
                a2 = AM.getAnimationFactory().createCameraTranslation(FOLLOW_ANIM_DURATION/2, application.mCamera,
                    trans, true, IdentityInterpolator.getInstance(),
                    new EndAction(){
                        public void execute(Object subject, Animation.Dimension dimension){
                            application.sm.setUpdateLevel(true);
                            if (application.isAAEnabled()){
                                application.mView.setAntialiasing(true);
                            }
                        }
                    });
                AM.startAnimation(a1, false);
                AM.startAnimation(a2, false);
            }
        }
        if (!brought2location.isEmpty()){
            Iterator i = brought2location.keySet().iterator();
            while (i.hasNext()){
                sendBackNTU(i.next(), followedNode);
            }
            brought2location.clear();
        }
        synchronized(broughtnode2broughtby){
            if (!broughtnode2broughtby.isEmpty()){
                broughtnode2broughtby.clear();
            }
        }
        if (!broughtStack.isEmpty()){
            for (int i=0;i<broughtStack.size();i++){
                broughtStack.elementAt(i).getShape().setColor(AIRPORT_FILL_COLOR);
            }
            broughtStack.clear();
        }
        nodesOutsideScope.clear();
        arcsOutsideScope.clear();
    }

    void bring(LEdge arc, LNode node, LNode broughtby, double sx, double sy, double ex, double ey, HashMap<LNode,Point2D.Double> node2bposition){
        synchronized(broughtnode2broughtby){
            if (brought2location.containsKey(node)){
                broughtnode2broughtby.put(node, broughtby);
            }
            else {
                brought2location.put(node, BroughtElement.rememberPreviousState(node));
                broughtnode2broughtby.put(node, broughtby);
            }
        }
        if (!brought2location.containsKey(arc)){
            brought2location.put(arc, BroughtElement.rememberPreviousState(arc));
        }
        ClosedShape nodeShape = node.getShape();
        application.bSpace.onTop(nodeShape);
        VText nodeLabel = node.getLabel();
        application.bSpace.onTop(nodeLabel);
        Point2D.Double bposition = node2bposition.get(node);
        Point2D.Double translation = new Point2D.Double(bposition.x, bposition.y);
        Animation a1 = AM.getAnimationFactory().createGlyphTranslation(BRING_ANIM_DURATION, nodeShape,
            translation, false, SlowInSlowOutInterpolator.getInstance(), null);
        //Animation a2 = AM.getAnimationFactory().createGlyphTranslation(BRING_ANIM_DURATION, nodeLabel,
        //    translation, false, SlowInSlowOutInterpolator.getInstance(), null);
        AM.startAnimation(a1, false);
        //AM.startAnimation(a2, false);
        DPath spline = arc.getSpline();
        Point2D.Double asp = spline.getStartPoint();
        Point2D.Double aep = spline.getEndPoint();
        Point2D.Double sp, ep;
        if (Math.sqrt((asp.x-ex)*(asp.x-ex) + (asp.y-ey)*(asp.y-ey)) < Math.sqrt((asp.x-sx)*(asp.x-sx) + (asp.y-sy)*(asp.y-sy))){
            sp = new Point2D.Double(bposition.x, bposition.y);
            ep = new Point2D.Double(sx, sy);
        }
        else {
            sp = new Point2D.Double(sx, sy);
            ep = new Point2D.Double(bposition.x, bposition.y);
        }
        Point2D.Double[] flatCoords = DPath.getFlattenedCoordinates(spline, sp, ep, true);
        Animation a = AM.getAnimationFactory().createPathAnim(BRING_ANIM_DURATION, spline,
            flatCoords, false, SlowInSlowOutInterpolator.getInstance(), null);
        AM.startAnimation(a, true);
        // brought elements should not be faded
        nodesOutsideScope.remove(node);
        arcsOutsideScope.remove(arc);
        LEdge[] otherArcs = node.getOtherArcs(arc);
        Glyph oe;
        for (int i=0;i<otherArcs.length;i++){
            if (!brought2location.containsKey(otherArcs[i])){
                brought2location.put(otherArcs[i], BroughtElement.rememberPreviousState(otherArcs[i]));
            }
            spline = otherArcs[i].getSpline();
            asp = spline.getStartPoint();
            aep = spline.getEndPoint();
            if (node2bposition.containsKey(otherArcs[i].getTail())
                && node2bposition.containsKey(otherArcs[i].getHead())){
                sp = node2bposition.get(otherArcs[i].getTail());
                ep = node2bposition.get(otherArcs[i].getHead());
            }
            else {
                oe = otherArcs[i].getOtherEnd(node).getShape();
                if (Math.sqrt((asp.x-ex)*(asp.x-ex) + (asp.y-ey)*(asp.y-ey)) <= Math.sqrt((aep.x-ex)*(aep.x-ex) + (aep.y-ey)*(aep.y-ey))){
                    sp = new Point2D.Double(bposition.x, bposition.y);
                    ep = oe.getLocation();
                }
                else {
                    sp = oe.getLocation();
                    ep = new Point2D.Double(bposition.x, bposition.y);
                }
            }
            flatCoords = DPath.getFlattenedCoordinates(spline, sp, ep, true);
            a = AM.getAnimationFactory().createPathAnim(BRING_ANIM_DURATION, spline,
                flatCoords, false, SlowInSlowOutInterpolator.getInstance(), null);
            AM.startAnimation(a, true);
            // 2nd step brought elements should not be faded
            arcsOutsideScope.remove(otherArcs[i]);
        }
    }

    void sendBackNTU(Object k, LNode followedNode){
        BroughtElement be = brought2location.get(k);
        //if (k == followedNode ||
        //    (k instanceof LEdge && ((LEdge)k).isConnectedTo(followedNode))){
        if (k == followedNode){
            be.restorePreviousState(FOLLOW_ANIM_DURATION);
        }
        else {
            be.restorePreviousState(BRING_ANIM_DURATION);
        }
    }

    void sendBack(LNode n){
        BroughtElement be = brought2location.get(n);
        be.restorePreviousState(BRING_ANIM_DURATION);
    }

    void sendBack(LEdge e){
        BroughtElement be = brought2location.get(e);
        be.restorePreviousState(BRING_ANIM_DURATION);
    }

    // n1 is the node for which we attempt to send back connected nodes
    // n2 is the new center of the bring and go, so we do not send back nodes connected to n1 that are also connected to n2
    void fadeStack(LNode n1, LNode n2){
        LEdge[] arcs = n1.getAllArcs();
        LNode oe;
        for (int i=0;i<arcs.length;i++){
            oe = arcs[i].getOtherEnd(n1);
            if (broughtnode2broughtby.get(oe) == n1 && !broughtStack.contains(oe)){
                // hide nodes that are brought by some node in the brought stack but not by the current one
                // do not send them back, just hide them in place (and don't do it for nodes in the brought stack)
                oe.getShape().setSensitivity(false);
                LEdge[] arcs2 = oe.getAllArcs();
            }
        }
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

}

class LNode extends LElem {

    String code, name, lat, lon;

    LEdge[] edges;
    short[] edgeDirections;

    VCircle nodeShape;
    VText nodeLabel;

    LNode(String code, String name, double lat, double lon, VCircle nodeShape, VText nodeLabel){
        this.code = code;
        this.name = name;
        this.lat = String.valueOf(lat);
        this.lon = String.valueOf(lon);
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

    String getLatitude(){
        return lat;
    }

    String getLongitude(){
        return lon;
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

    public String toString(){
        return weight + "@" + hashCode() + " [" +
            ((tail != null) ? tail.getCode() + "@" + tail.hashCode() : "NULL")+
            ((directed) ? LEdge.DIRECTED_STR : LEdge.UNDIRECTED_STR) +
            ((head != null) ? head.getCode() + "@" + head.hashCode() : "NULL") +
            "]";
    }

}

abstract class BroughtElement {

    Object owner;

    static BroughtElement rememberPreviousState(LNode el){
        return new BroughtNode(el);
    }

    static BroughtElement rememberPreviousState(LEdge el){
        return new BroughtEdge((LEdge)el);
    }

    abstract void restorePreviousState(int duration);

}

class BroughtNode extends BroughtElement {

    Glyph glyph;
    Point2D.Double previousLocation;

    BroughtNode(LNode n){
        owner = n;
        glyph = n.getShape();
        previousLocation = glyph.getLocation();
    }

    void restorePreviousState(int duration){
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createGlyphTranslation(duration, glyph,
            new Point2D.Double(previousLocation.x, previousLocation.y), false, IdentityInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, false);
    }

}

class BroughtEdge extends BroughtElement {

    DPath spline;
    float splineAlpha;
    Point2D.Double[] splineCoords;

    BroughtEdge(LEdge e){
        owner = e;
        spline = e.getSpline();
        if (spline != null){
            splineCoords = spline.getAllPointsCoordinates();
            splineAlpha = spline.getTranslucencyValue();
        }
    }

    void restorePreviousState(int duration){
        Animation a = VirtualSpaceManager.INSTANCE.getAnimationManager().getAnimationFactory().createPathAnim(duration, spline,
            splineCoords, false, SlowInSlowOutInterpolator.getInstance(), null);
        VirtualSpaceManager.INSTANCE.getAnimationManager().startAnimation(a, true);
    }

}

class RingManager {

    Ring[] rings = new Ring[0];

    Ring getRing(double direction, double size, double ringStep){
        // normalize direction in [0,2Pi[
        if (direction < 0){direction = 2 * Math.PI + direction;}
        // look for a ring where the new object could be placed, starting with the innermost one
        for (int i=0;i<rings.length;i++){
            double a = Math.abs(Math.atan2(size, rings[i].rank * ringStep));
            if (!rings[i].intersectsConeOfInfluence(direction-a, direction+a)){
                rings[i].addNode(direction-a, direction+a);
                return rings[i];
            }
        }
        // if couldn't find any room, create a new ring
        Ring r = createNewRing();
        double a = Math.abs(Math.atan2(size, ringStep));
        r.addNode(direction-a, direction+a);
        return r;
    }

    private Ring createNewRing(){
        Ring[] tr = new Ring[rings.length+1];
        System.arraycopy(rings, 0, tr, 0, rings.length);
        tr[rings.length] = new Ring(tr.length);
        rings = tr;
        return rings[rings.length-1];
    }

}

class Ring {

    /* rank of this ring (starts at 1) */
    int rank;
    double[][] cones = new double[0][2];

    Ring(int r){
        this.rank = r;
    }

    void addNode(double a1, double a2){
        // compute its cone of influence
        double[][] tc = new double[cones.length+1][2];
        System.arraycopy(cones, 0, tc, 0, cones.length);
        // normalize angles in [0,2Pi[
        if (a1 < 0){a1 = 2 * Math.PI + a1;}
        if (a2 < 0){a2 = 2 * Math.PI + a2;}
        tc[cones.length][0] = Math.min(a1, a2);
        tc[cones.length][1] = Math.max(a1, a2);
        cones = tc;
    }

    boolean intersectsConeOfInfluence(double a1, double a2){
        for (int i=0;i<cones.length;i++){
            if (a2 > cones[i][0] && a1 < cones[i][1]){return true;}
        }
        return false;
    }

}

class DistanceComparator implements java.util.Comparator {

    LNode centerNode;
    Glyph centerShape;

    DistanceComparator(LNode cn){
        this.centerNode = cn;
        this.centerShape = cn.getShape();
    }

    public int compare(Object o1, Object o2){
        Glyph n1 = ((LEdge)o1).getOtherEnd(centerNode).getShape();
        Glyph n2 = ((LEdge)o2).getOtherEnd(centerNode).getShape();
        double d1 = (centerShape.vx-n1.vx)*(centerShape.vx-n1.vx) + (centerShape.vy-n1.vy)*(centerShape.vy-n1.vy);
        double d2 = (centerShape.vx-n2.vx)*(centerShape.vx-n2.vx) + (centerShape.vy-n2.vy)*(centerShape.vy-n2.vy);
        if (d1 < d2){
            return -1;
        }
        else if (d1 > d2){
            return 1;
        }
        else {
            return 0;
        }
    }

}
