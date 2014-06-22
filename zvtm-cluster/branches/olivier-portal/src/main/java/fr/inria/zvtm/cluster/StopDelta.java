package fr.inria.zvtm.cluster;

class StopDelta implements Delta{
    public void apply(SlaveUpdater updater){
        updater.stop();
    }
}

