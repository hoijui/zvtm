/*
 *   Copyright (c) INRIA, 2010-2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zuist.cluster;

import java.awt.Color;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.cluster.Delta;
import fr.inria.zvtm.cluster.GlyphCreation;
import fr.inria.zvtm.cluster.GlyphReplicator;
import fr.inria.zvtm.cluster.Identifiables;
import fr.inria.zvtm.cluster.ObjId;
import fr.inria.zvtm.cluster.SlaveUpdater;
import fr.inria.zvtm.glyphs.ClosedShape;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zuist.engine.ClosedShapeDescription;
import fr.inria.zuist.engine.GlyphDescription;
import fr.inria.zuist.engine.ImageDescription;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.SceneFragmentDescription;
import fr.inria.zuist.engine.TextDescription;
import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;
import fr.inria.zuist.engine.SceneBuilder;
import fr.inria.zuist.engine.SceneObserver;

aspect SceneManagerReplication {
    //instrument *createLevel, *createRegion, *destroyRegion,
    //*createImageDescription, *createTextDescription,
    //*createClosedShapeDescription, createSceneFragmentDescription

    before(Glyph glyph, VirtualSpace virtualSpace):
        GlyphCreation.glyphAdd(glyph, virtualSpace) &&
        if(virtualSpace.isZuistOwned()) &&
        !cflowbelow(GlyphCreation.glyphAdd(Glyph, VirtualSpace)){
            glyph.setReplicated(false);
        }

    pointcut sceneManagerCreation(SceneManager sceneManager,
            SceneObserver[] observers, HashMap<String,String> properties) :
        execution(public SceneManager.new(SceneObserver[], HashMap<String,String>)) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        this(sceneManager) &&
        args(observers, properties);

    after(SceneManager sceneManager,
            SceneObserver[] observers,
            HashMap<String,String> properties)
        returning() :
        sceneManagerCreation(sceneManager, observers, properties) &&
        !cflowbelow(sceneManagerCreation(SceneManager, SceneObserver[], HashMap<String,String>)){
            VirtualSpace[] spaces = new VirtualSpace[observers.length];
            Camera[] cameras = new Camera[observers.length];
            for(int i=0;i<observers.length;i++){
                observers[i].getTargetVirtualSpace().setZuistOwned(true);
                spaces[i] = observers[i].getTargetVirtualSpace();
                cameras[i] = observers[i].getCamera();
            }

            sceneManager.setReplicated(true);

            SceneManagerCreateDelta delta =
                new SceneManagerCreateDelta(sceneManager.getObjId(), sceneManager.getSceneBuilder().getObjId(),
                        Arrays.asList(spaces), Arrays.asList(cameras), properties);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class SceneManagerCreateDelta implements Delta {
        private final ObjId<SceneManager> smId;
        private final ObjId<SceneBuilder> sbId;
        private final ArrayList<ObjId<VirtualSpace>> spaceRefs;
        private final ArrayList<ObjId<Camera>> cameraRefs;
        private final HashMap<String,String> properties;

        SceneManagerCreateDelta(ObjId<SceneManager> smId, ObjId<SceneBuilder> sbId,
                                List<VirtualSpace> spaces, List<Camera> cameras,
                                HashMap<String,String> props){
            this.smId = smId;
            this.sbId = sbId;
            this.spaceRefs = Identifiables.getRefList(spaces);
            this.cameraRefs = Identifiables.getRefList(cameras);
            this.properties = props;
        }

        public void apply(SlaveUpdater su){
            SceneManager sm =
                new SceneManager(su.getSlaveObjectArrayList(spaceRefs).toArray(new VirtualSpace[0]),
                        su.getSlaveObjectArrayList(cameraRefs).toArray(new Camera[0]), properties);
            su.putSlaveObject(smId, sm);
            su.putSlaveObject(sbId, sm.getSceneBuilder());
        }
    }

    pointcut createLevel(SceneBuilder sceneBuilder,
            int depth, double ceilingAlt, double floorAlt) :
        execution(public Level SceneBuilder.createLevel(int, double, double)) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        this(sceneBuilder) &&
        args(depth, ceilingAlt, floorAlt);

    after(SceneBuilder sceneBuilder, int depth,
            double ceilingAlt, double floorAlt)
        returning(Level level) :
        createLevel(sceneBuilder, depth, ceilingAlt, floorAlt) &&
        !cflowbelow(createLevel(SceneBuilder, int, double, double)){
            level.setReplicated(true);

            LevelCreateDelta delta = new LevelCreateDelta(
                    sceneBuilder.getObjId(),
                    level.getObjId(),
                    depth, ceilingAlt, floorAlt);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class LevelCreateDelta implements Delta {
        private final ObjId<SceneBuilder> smId;
        private final ObjId<Level> levelId;
        private final int depth;
        private final double ceilingAlt;
        private final double floorAlt;

        LevelCreateDelta(ObjId<SceneBuilder> smId, ObjId<Level> levelId,
                int depth, double ceilingAlt, double floorAlt){
            this.smId = smId;
            this.levelId = levelId;
            this.depth = depth;
            this.ceilingAlt = ceilingAlt;
            this.floorAlt = floorAlt;
        }

        public void apply(SlaveUpdater su){
            SceneBuilder sm = su.getSlaveObject(smId);
            Level level = sm.createLevel(depth, ceilingAlt, floorAlt);
            su.putSlaveObject(levelId, level);
        }
    }

    pointcut createRegion(SceneBuilder sceneBuilder,
            double x, double y, double w, double h,
            int highestLevel, int lowestLevel,
            String id, String title, String layer, short[] transitions,
            short requestOrdering, boolean sensitivity, Color fill,
            Color stroke) :
        execution(public Region SceneBuilder.createRegion(double, double, double, double,
                    int, int, String, String, String, short[], short, boolean,
                    Color, Color)) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        this(sceneBuilder) &&
        args(x, y, w, h,
            highestLevel, lowestLevel,
            id, title, layer, transitions,
            requestOrdering, sensitivity, fill,
            stroke);

    after(SceneBuilder sceneBuilder, double x, double y, double w, double h,
            int highestLevel, int lowestLevel,
            String id, String title, String layer, short[] transitions,
            short requestOrdering, boolean sensitivity, Color fill,
            Color stroke) returning(Region region):
        createRegion(sceneBuilder, x, y, w, h, highestLevel, lowestLevel,
                id, title, layer, transitions,
                requestOrdering, sensitivity, fill,
                stroke) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        !cflowbelow(createRegion(SceneBuilder, double, double, double, double, int, int,
                String, String, String, short[], short, boolean, Color, Color)){
            region.setReplicated(true);

            RegionCreateDelta delta = new RegionCreateDelta(
                    sceneBuilder.getObjId(),
                    region.getObjId(),
                    x, y, w, h,
                    highestLevel, lowestLevel,
                    id, title, layer, transitions,
                    requestOrdering, sensitivity, fill,
                    stroke);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class RegionCreateDelta implements Delta {
        private final ObjId<SceneBuilder> smId;
        private final ObjId<Region> regionId;
        private final double x;
        private final double y;
        private final double w;
        private final double h;
        private final int highestLevel;
        private final int lowestLevel;
        private final String id;
        private final String title;
        private final String layer;
        private final short[] transitions;
        private final short requestOrdering;
        private final boolean sensitivity;
        private final Color fill;
        private final Color stroke;

        RegionCreateDelta(ObjId<SceneBuilder> smId, ObjId<Region> regionId,
                double x, double y, double w, double h,
                int highestLevel, int lowestLevel,
                String id, String title, String layer, short[] transitions,
                short requestOrdering, boolean sensitivity, Color fill,
                Color stroke){
            this.smId = smId;
            this.regionId = regionId;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
            this.highestLevel = highestLevel;
            this.lowestLevel = lowestLevel;
            this.id = id;
            this.title = title;
            this.layer = layer;
            this.transitions = transitions;
            this.requestOrdering = requestOrdering;
            this.sensitivity = sensitivity;
            this.fill = fill;
            this.stroke = stroke;
        }

        public void apply(SlaveUpdater su){
            SceneBuilder sm = su.getSlaveObject(smId);
            Region region = sm.createRegion(x, y, w, h,
                    highestLevel, lowestLevel,
                    id, title, layer, transitions,
                    requestOrdering, sensitivity, fill,
                    stroke);
            su.putSlaveObject(regionId, region);
        }
    }

    pointcut destroyRegion(SceneBuilder sceneBuilder, Region region) :
        execution(public void SceneBuilder.destroyRegion(Region)) &&
        this(sceneBuilder) &&
        args(region);

    after(SceneBuilder sceneBuilder, Region region) returning() :
        destroyRegion(sceneBuilder, region) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        !cflowbelow(destroyRegion(SceneBuilder, Region)) {
            RegionDestroyDelta delta = new RegionDestroyDelta(
                    sceneBuilder.getObjId(),
                    region.getObjId());
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class RegionDestroyDelta implements Delta {
        private final ObjId<SceneBuilder> smId;
        private final ObjId<Region> regionId;

        RegionDestroyDelta(ObjId<SceneBuilder> smId, ObjId<Region> regionId){
            this.smId = smId;
            this.regionId = regionId;
        }

        public void apply(SlaveUpdater su){
            SceneBuilder sm = su.getSlaveObject(smId);
            Region region = su.getSlaveObject(regionId);
            sm.destroyRegion(region);
            su.removeSlaveObject(regionId);
        }
    }

    pointcut createImageDescription(
            SceneBuilder sceneBuilder,
            double x, double y, double w, double h,
            String id, int zindex, Region region,
            URL imageURL, boolean sensitivity,
            Color stroke, float alpha, String params) :
        execution(public ImageDescription SceneBuilder.createImageDescription(
                    double, double, double, double,
                    String, int, Region,
                    URL, boolean,
                    Color, float, String)) &&
        this(sceneBuilder) &&
        args(x, y, w, h,
            id, zindex, region,
            imageURL, sensitivity,
            stroke, alpha, params);

    after(SceneBuilder sceneBuilder,
            double x, double y, double w, double h,
            String id, int zindex, Region region,
            URL imageURL, boolean sensitivity,
            Color stroke, float alpha, String params
            ) returning(ObjectDescription imageDesc):
        createImageDescription(sceneBuilder,
                x, y, w, h,
                id, zindex, region,
                imageURL, sensitivity,
                stroke, alpha, params) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        !cflowbelow(createImageDescription(SceneBuilder,
                double, double, double, double,
                String, int, Region,
                URL, boolean,
                Color, float, String)) {
            imageDesc.setReplicated(true);

            Delta delta = new ImageCreateDelta(sceneBuilder.getObjId(),
                    imageDesc.getObjId(),
                    x, y, w, h,
                    id, zindex, region.getObjId(),
                    imageURL, sensitivity,
                    stroke, alpha, params);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class ImageCreateDelta implements Delta {
       private final ObjId<SceneBuilder> smId;
       private final ObjId<ObjectDescription> descId;
       private final double x;
       private final double y;
       private final double w;
       private final double h;
       private final String id;
       private final int zindex;
       private final ObjId<Region> regionId;
       private final URL imageURL;
       private final boolean sensitivity;
       private final Color stroke;
       private final float alpha;
       private final String params;

       ImageCreateDelta(ObjId<SceneBuilder> smId,
               ObjId<ObjectDescription> descId,
               double x, double y, double w, double h,
               String id, int zindex, ObjId<Region> regionId,
               URL imageURL, boolean sensitivity,
               Color stroke, float alpha, String params){
           this.smId = smId;
           this.descId = descId;
           this.x = x;
           this.y = y;
           this.w = w;
           this.h = h;
           this.id = id;
           this.zindex = zindex;
           this.regionId = regionId;
           this.imageURL = imageURL;
           this.sensitivity = sensitivity;
           this.stroke = stroke;
           this.alpha = alpha;
           this.params = params;
       }

       public void apply(SlaveUpdater su){
           SceneBuilder sm = su.getSlaveObject(smId);
           Region region = su.getSlaveObject(regionId);
           ImageDescription desc = sm.createImageDescription(
                   x, y, w, h,
                   id, zindex, region,
                   imageURL, sensitivity,
                   stroke, alpha, params);
           su.putSlaveObject(descId, desc);
       }
    }

    pointcut createTextDescription(SceneBuilder sceneBuilder,
            double x, double y, String id, int zindex, Region region, float scale,
            String text, short anchor, Color fill, float alpha, String family,
            int style, int size, boolean sensitivity) :
        execution(public TextDescription SceneBuilder.createTextDescription(
                    double, double, String, int, Region, float, String,
                    short, Color, float, String, int, int, boolean)) &&
        this(sceneBuilder) &&
        args(x, y, id, zindex, region, scale,
            text, anchor, fill, alpha, family,
            style, size, sensitivity);

    after(SceneBuilder sceneBuilder,
            double x, double y, String id, int zindex, Region region, float scale,
            String text, short anchor, Color fill, float alpha, String family,
            int style, int size, boolean sensitivity)
        returning(ObjectDescription textDesc) :
            createTextDescription(sceneBuilder, x, y, id,
                    zindex, region, scale,
                    text, anchor, fill, alpha, family,
                    style, size, sensitivity) &&
            if(VirtualSpaceManager.INSTANCE.isMaster()) &&
            !cflowbelow(createTextDescription(SceneBuilder, double, double,
                        String, int, Region, float, String,
                        short, Color, float, String, int, int, boolean)) {
                textDesc.setReplicated(true);

                Delta delta = new TextCreateDelta(sceneBuilder.getObjId(),
                        textDesc.getObjId(),
                        x, y, id, zindex, region.getObjId(), scale,
                        text, anchor, fill, alpha, family,
                        style, size, sensitivity );
                VirtualSpaceManager.INSTANCE.sendDelta(delta);
            }

    private static class TextCreateDelta implements Delta {
        private final ObjId<SceneBuilder> smId;
        private final ObjId<ObjectDescription> descId;
        private final double x;
        private final double y;
        private final String id;
        private final int zindex;
        private final ObjId<Region> regionId;
        private final float scale;
        private final String text;
        private final short anchor;
        private final Color fill;
        private final float alpha;
        private final String family;
        private final int style;
        private final int size;
        private final boolean sensitivity;

        TextCreateDelta(ObjId<SceneBuilder> smId,
                ObjId<ObjectDescription> descId,
                double x, double y, String id, int zindex, ObjId<Region> regionId,
                float scale, String text, short anchor,
                Color fill, float alpha, String family,
                int style, int size, boolean sensitivity){
            this.smId = smId;
            this.descId = descId;
            this.x = x;
            this.y = y;
            this.id = id;
            this.zindex = zindex;
            this.regionId = regionId;
            this.scale = scale;
            this.text = text;
            this.anchor = anchor;
            this.fill = fill;
            this.alpha = alpha;
            this.family = family;
            this.style = style;
            this.size = size;
            this.sensitivity = sensitivity;
        }

       public void apply(SlaveUpdater su){
           SceneBuilder sm = su.getSlaveObject(smId);
           Region region = su.getSlaveObject(regionId);
           TextDescription desc = sm.createTextDescription(
                   x, y, id, zindex, region, scale,
                   text, anchor, fill, alpha, family,
                   style, size, sensitivity);
           su.putSlaveObject(descId, desc);
       }
    }

    pointcut createClosedShapeDescription(SceneBuilder sceneBuilder,
            ClosedShape closedShape, String id, int zindex, Region region, boolean sensitivity) :
        execution(public ClosedShapeDescription SceneBuilder.createClosedShapeDescription(ClosedShape, String, int, Region, boolean)) &&
        this(sceneBuilder) &&
        args(closedShape, id, zindex, region, sensitivity);

    after(SceneBuilder sceneBuilder,
            ClosedShape closedShape, String id, int zindex, Region region, boolean sensitivity)
        returning(ClosedShapeDescription csDesc) :
            createClosedShapeDescription(sceneBuilder, closedShape, id, zindex, region, sensitivity) &&
            if(VirtualSpaceManager.INSTANCE.isMaster()) &&
            !cflowbelow(createClosedShapeDescription(SceneBuilder, ClosedShape, String, int, Region, boolean)) {
                csDesc.setReplicated(true);

                Delta delta = new ClosedShapeCreateDelta(sceneBuilder.getObjId(),
                        csDesc, id, zindex, region.getObjId(), sensitivity);
                VirtualSpaceManager.INSTANCE.sendDelta(delta);
            }

    private static class ClosedShapeCreateDelta implements Delta {
        private final ObjId<SceneBuilder> smId;
        private final ObjId<ObjectDescription> descId;
        private final GlyphReplicator csReplicator;
        private final String id;
        private final int zindex;
        private final ObjId<Region> regionId;
        private final boolean sensitivity;

        ClosedShapeCreateDelta(ObjId<SceneBuilder> smId,
                ClosedShapeDescription csDesc,
                String id, int zindex, ObjId<Region> regionId,
                boolean sensitivity){
            this.smId = smId;
            this.descId = csDesc.getObjId();
            this.csReplicator = csDesc.getGlyph().getReplicator();
            this.id = id;
            this.zindex = zindex;
            this.regionId = regionId;
            this.sensitivity = sensitivity;
        }

       public void apply(SlaveUpdater su){
           SceneBuilder sm = su.getSlaveObject(smId);
           Region region = su.getSlaveObject(regionId);
           ClosedShapeDescription desc = sm.createClosedShapeDescription(
                   (ClosedShape)(csReplicator.createGlyph()), id, zindex, region,
                   sensitivity);
           su.putSlaveObject(descId, desc);
       }
    }

    pointcut createSceneFragmentDescription(SceneBuilder sceneBuilder,
            double x, double y, String id, Region region, URL resourceURL) :
        execution(public SceneFragmentDescription
                SceneBuilder.createSceneFragmentDescription(double, double, String, Region, URL)) &&
        this(sceneBuilder) &&
        args(x, y, id, region, resourceURL);

    after(SceneBuilder sceneBuilder, double x, double y,
            String id, Region region, URL resourceURL)
        returning(SceneFragmentDescription fragDesc) :
            createSceneFragmentDescription(sceneBuilder, x, y, id,
                    region, resourceURL)  &&
            if(VirtualSpaceManager.INSTANCE.isMaster()) &&
            !cflowbelow(createSceneFragmentDescription(SceneBuilder, double, double, String, Region, URL)){
                fragDesc.setReplicated(true);

                Delta delta = new SceneFragmentCreateDelta(
                        sceneBuilder.getObjId(),
                        fragDesc.getObjId(),
                        x,y,id,region.getObjId(),
                        resourceURL);
                VirtualSpaceManager.INSTANCE.sendDelta(delta);
    }

    private static class SceneFragmentCreateDelta implements Delta {
        private final ObjId<SceneBuilder> smId;
        private final ObjId<SceneFragmentDescription> descId;
        private final double x;
        private final double y;
        private final String id;
        private final ObjId<Region> regionId;
        private final URL resourceURL;

        SceneFragmentCreateDelta(ObjId<SceneBuilder> smId,
                ObjId<SceneFragmentDescription> descId,
                double x, double y, String id,
                ObjId<Region> regionId, URL resourceURL){
            this.smId = smId;
            this.descId = descId;
            this.x = x;
            this.y = y;
            this.id = id;
            this.regionId = regionId;
            this.resourceURL = resourceURL;
        }

        public void apply(SlaveUpdater su){
            SceneBuilder sm = su.getSlaveObject(smId);
            Region region = su.getSlaveObject(regionId);
            SceneFragmentDescription desc = sm.createSceneFragmentDescription(
                    x,y,id,region,resourceURL);
            su.putSlaveObject(descId, desc);
        }
    }

    pointcut createGlyphDescription(SceneBuilder sceneBuilder,
            Glyph g, String id, int zindex, Region region, boolean sensitivity) :
        execution(public GlyphDescription SceneBuilder.createGlyphDescription(Glyph, String, int, Region, boolean)) &&
        this(sceneBuilder) &&
        args(g, id, zindex, region, sensitivity);

    after(SceneBuilder sceneBuilder,
            Glyph g, String id, int zindex, Region region, boolean sensitivity)
        returning(GlyphDescription gDesc) :
            createGlyphDescription(sceneBuilder, g, id, zindex, region, sensitivity) &&
            if(VirtualSpaceManager.INSTANCE.isMaster()) &&
            !cflowbelow(createGlyphDescription(SceneBuilder, Glyph, String, int, Region, boolean)) {
                gDesc.setReplicated(true);

                Delta delta = new GlyphCreateDelta(sceneBuilder.getObjId(),
                        gDesc, id, zindex, region.getObjId(), sensitivity);
                VirtualSpaceManager.INSTANCE.sendDelta(delta);
            }

    private static class GlyphCreateDelta implements Delta {
        private final ObjId<SceneBuilder> smId;
        private final ObjId<ObjectDescription> descId;
        private final GlyphReplicator gReplicator;
        private final String id;
        private final int zindex;
        private final ObjId<Region> regionId;
        private final boolean sensitivity;

        GlyphCreateDelta(ObjId<SceneBuilder> smId,
                GlyphDescription gDesc,
                String id, int zindex, ObjId<Region> regionId,
                boolean sensitivity){
            this.smId = smId;
            this.descId = gDesc.getObjId();
            this.gReplicator = gDesc.getGlyph().getReplicator();
            this.id = id;
            this.zindex = zindex;
            this.regionId = regionId;
            this.sensitivity = sensitivity;
        }

       public void apply(SlaveUpdater su){
           SceneBuilder sm = su.getSlaveObject(smId);
           Region region = su.getSlaveObject(regionId);
           GlyphDescription desc = sm.createGlyphDescription(
                   (Glyph)(gReplicator.createGlyph()), id, zindex, region,
                   sensitivity);
           su.putSlaveObject(descId, desc);
       }
    }

}
