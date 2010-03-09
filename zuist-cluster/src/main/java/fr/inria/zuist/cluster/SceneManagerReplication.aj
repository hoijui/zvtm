package fr.inria.zuist.cluster;

import java.awt.Color;
import java.net.URL;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.cluster.Delta;
import fr.inria.zvtm.cluster.Identifiables;
import fr.inria.zvtm.cluster.ObjId;
import fr.inria.zvtm.cluster.SlaveUpdater;
import fr.inria.zuist.engine.ImageDescription;
import fr.inria.zuist.engine.ObjectDescription;
import fr.inria.zuist.engine.TextDescription;
import fr.inria.zuist.engine.Level;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.SceneManager;

aspect SceneManagerReplication {
    //instrument *createLevel, *createRegion, *destroyRegion,
    //*createImageDescription, *createTextDescription, createClosedShapeDescription

    
    pointcut sceneManagerCreation(SceneManager sceneManager, 
            VirtualSpace[] spaces, Camera[] cameras) : 
        execution(public SceneManager.new(VirtualSpace[], Camera[])) && 
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        this(sceneManager) &&
        args(spaces, cameras);

    after(SceneManager sceneManager, 
            VirtualSpace[] spaces, 
            Camera cameras[]) 
        returning() : 
        sceneManagerCreation(sceneManager, spaces, cameras) &&
        !cflowbelow(sceneManagerCreation(SceneManager, VirtualSpace[], Camera[])){
            for(VirtualSpace vs: spaces){
                vs.setMirrored(false);
            }
            
            SceneManagerCreateDelta delta = 
                new SceneManagerCreateDelta(sceneManager.getObjId(),
                        Arrays.asList(spaces), Arrays.asList(cameras));
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class SceneManagerCreateDelta implements Delta {
        private final ObjId<SceneManager> smId;
        private final ArrayList<ObjId<VirtualSpace>> spaceRefs;
        private final ArrayList<ObjId<Camera>> cameraRefs;

        SceneManagerCreateDelta(ObjId<SceneManager> smId, List<VirtualSpace> spaces, List<Camera> cameras){
            this.smId = smId;
            this.spaceRefs = Identifiables.getRefList(spaces);
            this.cameraRefs = Identifiables.getRefList(cameras);
        }

        public void apply(SlaveUpdater su){
            SceneManager sm = 
                new SceneManager(su.getSlaveObjectArrayList(spaceRefs).toArray(new VirtualSpace[0]),
                        su.getSlaveObjectArrayList(cameraRefs).toArray(new Camera[0]));
            su.putSlaveObject(smId, sm);
        }
    }

    pointcut createLevel(SceneManager sceneManager,
            int depth, float ceilingAlt, float floorAlt) :
        execution(public Level SceneManager.createLevel(int, float, float)) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        this(sceneManager) &&
        args(depth, ceilingAlt, floorAlt);

    after(SceneManager sceneManager, int depth,
            float ceilingAlt, float floorAlt) 
        returning(Level level) : 
        createLevel(sceneManager, depth, ceilingAlt, floorAlt) &&
        !cflowbelow(createLevel(SceneManager, int, float, float)){
            LevelCreateDelta delta = new LevelCreateDelta(
                    sceneManager.getObjId(),
                    level.getObjId(),
                    depth, ceilingAlt, floorAlt);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class LevelCreateDelta implements Delta {
        private final ObjId<SceneManager> smId;
        private final ObjId<Level> levelId;
        private final int depth;
        private final float ceilingAlt;
        private final float floorAlt;

        LevelCreateDelta(ObjId<SceneManager> smId, ObjId<Level> levelId,
                int depth, float ceilingAlt, float floorAlt){
            this.smId = smId;
            this.levelId = levelId;
            this.depth = depth;
            this.ceilingAlt = ceilingAlt;
            this.floorAlt = floorAlt;
        }

        public void apply(SlaveUpdater su){
            SceneManager sm = su.getSlaveObject(smId);
            Level level = sm.createLevel(depth, ceilingAlt, floorAlt);
            su.putSlaveObject(levelId, level);
        }
    }

    pointcut createRegion(SceneManager sceneManager, 
            long x, long y, long w, long h,
            int highestLevel, int lowestLevel, 
            String id, String title, int li, short[] transitions, 
            short requestOrdering, boolean sensitivity, Color fill, 
            Color stroke) : 
        execution(public Region SceneManager.createRegion(long, long, long, long,
                    int, int, String, String, int, short[], short, boolean, 
                    Color, Color)) &&
        this(sceneManager) && 
        args(x, y, w, h, 
            highestLevel, lowestLevel, 
            id, title, li, transitions, 
            requestOrdering, sensitivity, fill, 
            stroke);

    after(SceneManager sceneManager, long x, long y, long w, long h, 
            int highestLevel, int lowestLevel, 
            String id, String title, int li, short[] transitions, 
            short requestOrdering, boolean sensitivity, Color fill, 
            Color stroke) returning(Region region): 
        createRegion(sceneManager, x, y, w, h, highestLevel, lowestLevel, 
                id, title, li, transitions, 
                requestOrdering, sensitivity, fill, 
                stroke) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        !cflowbelow(createRegion(SceneManager, long, long, long, long, int, int, 
                String, String, int, short[], short, boolean, Color, Color)){
            RegionCreateDelta delta = new RegionCreateDelta(
                    sceneManager.getObjId(),
                    region.getObjId(),
                    x, y, w, h, 
                    highestLevel, lowestLevel, 
                    id, title, li, transitions, 
                    requestOrdering, sensitivity, fill, 
                    stroke);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class RegionCreateDelta implements Delta {
        private final ObjId<SceneManager> smId;
        private final ObjId<Region> regionId;
        private final long x;
        private final long y;
        private final long w;
        private final long h;
        private final int highestLevel;
        private final int lowestLevel;
        private final String id;
        private final String title;
        private final int li;
        private final short[] transitions;
        private final short requestOrdering;
        private final boolean sensitivity;
        private final Color fill;
        private final Color stroke;

        RegionCreateDelta(ObjId<SceneManager> smId, ObjId<Region> regionId,
                long x, long y, long w, long h, 
                int highestLevel, int lowestLevel, 
                String id, String title, int li, short[] transitions, 
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
            this.li = li;
            this.transitions = transitions;
            this.requestOrdering = requestOrdering;
            this.sensitivity = sensitivity;
            this.fill = fill;
            this.stroke = stroke;
        }

        public void apply(SlaveUpdater su){
            SceneManager sm = su.getSlaveObject(smId);
            Region region = sm.createRegion(x, y, w, h, 
                    highestLevel, lowestLevel, 
                    id, title, li, transitions, 
                    requestOrdering, sensitivity, fill, 
                    stroke);
            su.putSlaveObject(regionId, region);
        }
    }

    pointcut destroyRegion(SceneManager sceneManager, Region region) : 
        execution(public void SceneManager.destroyRegion(Region)) &&
        this(sceneManager) &&
        args(region);

    after(SceneManager sceneManager, Region region) returning() : 
        destroyRegion(sceneManager, region) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        !cflowbelow(destroyRegion(SceneManager, Region)) {
            RegionDestroyDelta delta = new RegionDestroyDelta(
                    sceneManager.getObjId(),
                    region.getObjId());
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class RegionDestroyDelta implements Delta {
        private final ObjId<SceneManager> smId;
        private final ObjId<Region> regionId;

        RegionDestroyDelta(ObjId<SceneManager> smId, ObjId<Region> regionId){
            this.smId = smId;
            this.regionId = regionId;
        }

        public void apply(SlaveUpdater su){
            SceneManager sm = su.getSlaveObject(smId);
            Region region = su.getSlaveObject(regionId);
            sm.destroyRegion(region);
            su.removeSlaveObject(regionId);
        }
    }

    pointcut createImageDescription(
            SceneManager sceneManager,
            long x, long y, long w, long h, 
            String id, int zindex, Region region,
            URL imageURL, boolean sensitivity, 
            Color stroke, String params) : 
        execution(public ImageDescription SceneManager.createImageDescription(
                    long, long, long, long, 
                    String, int, Region,
                    URL, boolean, 
                    Color, String)) &&
        this(sceneManager) && 
        args(x, y, w, h, 
            id, zindex, region,
            imageURL, sensitivity, 
            stroke, params);
    
    after(SceneManager sceneManager,
            long x, long y, long w, long h, 
            String id, int zindex, Region region,
            URL imageURL, boolean sensitivity, 
            Color stroke, String params
            ) returning(ObjectDescription imageDesc):
        createImageDescription(sceneManager,
                x, y, w, h, 
                id, zindex, region,
                imageURL, sensitivity, 
                stroke, params) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        !cflowbelow(createImageDescription(SceneManager,
                long, long, long, long, 
                String, int, Region,
                URL, boolean, 
                Color, String)) {
            Delta delta = new ImageCreateDelta(sceneManager.getObjId(),
                    imageDesc.getObjId(),
                    x, y, w, h,
                    id, zindex, region.getObjId(),
                    imageURL, sensitivity,
                    stroke, params);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class ImageCreateDelta implements Delta {
       private final ObjId<SceneManager> smId;
       private final ObjId<ObjectDescription> descId;
       private final long x;
       private final long y;
       private final long w;
       private final long h;
       private final String id;
       private final int zindex;
       private final ObjId<Region> regionId;
       private final URL imageURL;
       private final boolean sensitivity;
       private final Color stroke;
       private final String params;

       ImageCreateDelta(ObjId<SceneManager> smId,
               ObjId<ObjectDescription> descId,
               long x, long y, long w, long h, 
               String id, int zindex, ObjId<Region> regionId,
               URL imageURL, boolean sensitivity, 
               Color stroke, String params){
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
           this.params = params;
       }

       public void apply(SlaveUpdater su){
           SceneManager sm = su.getSlaveObject(smId);
           Region region = su.getSlaveObject(regionId);
           ImageDescription desc = sm.createImageDescription(
                   x, y, w, h,
                   id, zindex, region,
                   imageURL, sensitivity,
                   stroke, params);
           su.putSlaveObject(descId, desc);
       }
    }

    pointcut createTextDescription(SceneManager sceneManager, 
            long x, long y, String id, int zindex, Region region, float scale, 
            String text, short anchor, Color fill, String family, 
            int style, int size, boolean sensitivity) :
        execution(public TextDescription SceneManager.createTextDescription(
                    long, long, String, int, Region, float, String,
                    short, Color, String, int, int, boolean)) &&
        this(sceneManager) && 
        args(x, y, id, zindex, region, scale, 
            text, anchor, fill, family, 
            style, size, sensitivity);

    after(SceneManager sceneManager, 
            long x, long y, String id, int zindex, Region region, float scale, 
            String text, short anchor, Color fill, String family, 
            int style, int size, boolean sensitivity) 
        returning(ObjectDescription textDesc) :
            createTextDescription(sceneManager, x, y, id, 
                    zindex, region, scale, 
                    text, anchor, fill, family, 
                    style, size, sensitivity) &&
            if(VirtualSpaceManager.INSTANCE.isMaster()) &&
            !cflowbelow(createTextDescription(SceneManager, long, long, 
                        String, int, Region, float, String,
                        short, Color, String, int, int, boolean)) {
                Delta delta = new TextCreateDelta(sceneManager.getObjId(),
                        textDesc.getObjId(),
                        x, y, id, zindex, region.getObjId(), scale, 
                        text, anchor, fill, family, 
                        style, size, sensitivity );
                VirtualSpaceManager.INSTANCE.sendDelta(delta);
            }

    private static class TextCreateDelta implements Delta {
        private final ObjId<SceneManager> smId;
        private final ObjId<ObjectDescription> descId;
        private final long x; 
        private final long y;
        private final String id;
        private final int zindex;
        private final ObjId<Region> regionId;
        private final float scale;
        private final String text;
        private final short anchor;
        private final Color fill;
        private final String family;
        private final int style;
        private final int size;
        private final boolean sensitivity;

        TextCreateDelta(ObjId<SceneManager> smId, 
                ObjId<ObjectDescription> descId,
                long x, long y, String id, int zindex, ObjId<Region> regionId, 
                float scale, String text, short anchor, 
                Color fill, String family, 
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
            this.family = family;
            this.style = style;
            this.size = size;
            this.sensitivity = sensitivity;
        }

       public void apply(SlaveUpdater su){
           SceneManager sm = su.getSlaveObject(smId);
           Region region = su.getSlaveObject(regionId);
           TextDescription desc = sm.createTextDescription(
                   x, y, id, zindex, region, scale, 
                   text, anchor, fill, family, 
                   style, size, sensitivity);
           su.putSlaveObject(descId, desc);
       }
    }
}

