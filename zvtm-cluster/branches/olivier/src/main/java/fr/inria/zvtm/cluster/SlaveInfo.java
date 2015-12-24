package fr.inria.zvtm.cluster;

import org.jgroups.Address;

class SlaveInfo{
        long repaintCount;
        Address address = null;
        long atTime;
        long pTime;
        int skipped;
        boolean paintAsked;

        SlaveInfo(Address add) {
        	address = add;
        	repaintCount = 0;
        	atTime = System.currentTimeMillis();
        	skipped = 0;
        	paintAsked = false;
        }
}