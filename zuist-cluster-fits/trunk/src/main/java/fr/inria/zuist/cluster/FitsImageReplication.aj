package fr.inria.zuist.cluster;

import java.awt.Color;
import java.net.URL;
import fr.inria.zvtm.cluster.Delta;
import fr.inria.zvtm.cluster.ObjId;
import fr.inria.zvtm.cluster.SlaveUpdater;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zuist.engine.FitsImageDescription;
import fr.inria.zuist.engine.FitsResourceHandler;
import fr.inria.zuist.engine.Region;
import fr.inria.zuist.engine.ResourceDescription;
import fr.inria.zuist.engine.SceneManager;

aspect FitsImageDescReplication {
    //capture creation of a FitsImage object
    //and send the appropriate Delta
    pointcut createFitsImageDescription(SceneManager sceneManager, long x,
            long y, String id, int zindex, Region region, URL resourceURL, 
            String type, boolean sensitivity, Color stroke, String params) : 
        execution(public ResourceDescription 
                SceneManager.createResourceDescription(long, 
                    long, String, int, Region,
                    URL, String, boolean, Color, String)) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        if(type.equals(FitsResourceHandler.RESOURCE_TYPE_FITS)) &&
        this(sceneManager) &&
        args(x, y, id, zindex, region, resourceURL, type, 
                sensitivity, stroke, params);

    after(SceneManager sceneManager,
            long x, long y, 
            String id, int zindex, Region region,
            URL imageURL, String type, boolean sensitivity, 
            Color stroke, String params)
        returning(ResourceDescription rdesc): 
            createFitsImageDescription(sceneManager, x, y, id,
                    zindex, region, imageURL, type, sensitivity,
                    stroke, params) &&
            !cflowbelow(createFitsImageDescription(SceneManager, long, long, 
                        String, int, Region, URL, String, 
                        boolean, Color, String)){
                rdesc.setReplicated(true);
                FitsImageCreateDelta delta = new FitsImageCreateDelta(
                        id, sceneManager.getObjId(), rdesc.getObjId(),
                        x,y,zindex,region.getObjId(), imageURL, params);
                VirtualSpaceManager.INSTANCE.sendDelta(delta);
            }

    private static class FitsImageCreateDelta implements Delta {
        private final String id;
        private final ObjId<SceneManager> smId;
        private final ObjId<Region> regionId;
        private final ObjId<ResourceDescription> descId;
        private final long x;
        private final long y;
        private final int z;
        private final URL location;
        private final String params;

        FitsImageCreateDelta(String id, ObjId<SceneManager> smId, 
                ObjId<ResourceDescription> descId, long x, long y,
                int z, ObjId<Region> regionId, URL location, String params){
            this.id = id;
            this.smId = smId;
            this.regionId = regionId;
            this.descId = descId;
            this.x = x;
            this.y = y;
            this.z = z;
            this.location = location;
            this.params = params;
        }

        public void apply(SlaveUpdater su){
            SceneManager sm = su.getSlaveObject(smId);
            Region region = su.getSlaveObject(regionId);
            ResourceDescription desc = sm.createResourceDescription(x,y,id,z,region,
                    location,FitsResourceHandler.RESOURCE_TYPE_FITS,true,Color.BLACK,params);
            su.putSlaveObject(descId, desc);
        }
    }
}

