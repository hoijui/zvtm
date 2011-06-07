package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.compositor.MetisseWindow;

public aspect MetisseWindowReplication {

    pointcut metisseFbUpdate(MetisseWindow metWin, byte[] img, int x, int y, int w, int h):
       (execution(public void MetisseWindow.fbUpdate(byte[], int, int, int, int)))
		&& this(metWin)
		&& args(img, x, y, w, h)
		&& if(VirtualSpaceManager.INSTANCE.isMaster());

    after(MetisseWindow metWin, byte[] img, int x, int y, int w, int h) returning:
        metisseFbUpdate(metWin, img, x, y, w, h) && 
        !cflowbelow(metisseFbUpdate(MetisseWindow, byte[], int, int, int, int)) &&
        if(metWin.isReplicated()){
            Delta delta = new MetisseFbUpdateDelta(metWin.getObjId(),
                    img, x, y, w, h);
            VirtualSpaceManager.INSTANCE.sendDelta(delta);
	   }

    private static class MetisseFbUpdateDelta implements Delta {
        private final ObjId<MetisseWindow> targetId;
        private final byte[] img;
        private final int x;
	private final int y;
	private final int w;
	private final int h;

        MetisseFbUpdateDelta(ObjId<MetisseWindow> targetId, byte[] img, int x, int y, int w, int h){
            this.targetId = targetId;
            this.img = img;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public void apply(SlaveUpdater updater){
            MetisseWindow target = updater.getSlaveObject(targetId);
            target.fbUpdate(img, x, y, w, h);
        }
    }

}

