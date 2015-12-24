package fr.inria.zuist.cluster;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zuist.engine.PseudoView;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.cluster.ObjId;
import fr.inria.zvtm.cluster.SlaveUpdater;
import fr.inria.zvtm.cluster.Delta;

aspect PseudoViewReplication {

	pointcut pseudoViewCreation(
		PseudoView pseudoView, VirtualSpace vs, Camera c, int w, int h, int layerIndex) :
        execution(public PseudoView.new(VirtualSpace, Camera, int, int, int)) &&
        if(VirtualSpaceManager.INSTANCE.isMaster()) &&
        this(pseudoView) &&
        args(vs, c, w, h, layerIndex);

    after(PseudoView pseudoView, VirtualSpace vs, Camera c, int w, int h, int layerIndex)
        returning() :
        pseudoViewCreation(pseudoView, vs, c, w, h, layerIndex) &&
        !cflowbelow(pseudoViewCreation(PseudoView, VirtualSpace, Camera, int, int, int))
        {
            
            // vs.setZuistOwned(true);

           	pseudoView.setReplicated(true);

            PseudoViewCreateDelta delta =
                new PseudoViewCreateDelta(pseudoView.getObjId(),
                        vs.getObjId(), c.getObjId(), w, h, layerIndex);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
        }

    private static class PseudoViewCreateDelta implements Delta {
        private final ObjId<PseudoView> pvId;
        private final ObjId<VirtualSpace> vsId;
        private final ObjId<Camera> cId;
        private final int w;
        private final int h;
        private final int layerIndex;

       	PseudoViewCreateDelta(
       		ObjId<PseudoView> pvId, ObjId<VirtualSpace> vsId, ObjId<Camera> cId,
            int w, int h, int layerIndex)
       	{
            this.pvId = pvId;
            this.vsId = vsId;
            this.cId = cId;
            this.w = w;
            this.h = h;
            this.layerIndex = layerIndex;
        }

        public void apply(SlaveUpdater su){
            PseudoView pv =
                new PseudoView(su.getSlaveObject(vsId),su.getSlaveObject(cId),w,h,layerIndex);
            su.putSlaveObject(pvId, pv);
            //System.out.println("New slave Pweudo View !!!"+ pv.getWidth() +" "+ pv.getHeight());
        }
    }

}
