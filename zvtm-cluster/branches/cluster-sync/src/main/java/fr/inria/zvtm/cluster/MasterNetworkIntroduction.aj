/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

import java.lang.Long;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.Address;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//adds state replication mechanism in VirtualSpaceManager
//(master half == send deltas).
aspect MasterNetworkIntroduction {
    private MasterNetworkDelegate VirtualSpaceManager.networkDelegate =
        new MasterNetworkDelegate();
    private boolean VirtualSpaceManager.isMaster = false;
    private String VirtualSpaceManager.appName;
    private final Logger VirtualSpaceManager.logger =
        LoggerFactory.getLogger(VirtualSpaceManager.class);

    public void VirtualSpaceManager.setMaster(String app){
        appName = app;
        startOperation();
        isMaster = true;
    }

    public boolean VirtualSpaceManager.isMaster(){ return isMaster; }

    public void VirtualSpaceManager.sendDelta(Delta delta){
        if(!isMaster()){
            throw new IllegalStateException("VirtualSpaceManager must be a master to send deltas, see setMaster()");
        }

        try{
            networkDelegate.sendDelta(delta);
        } catch (Exception ce){
            logger.error("Could not send Delta message: " + ce);
        }
    }

    public void VirtualSpaceManager.sendDeltaImmediatly(Delta delta){
        if(!isMaster()){
            throw new IllegalStateException("VirtualSpaceManager must be a master to send deltas, see setMaster()");
        }

        try{
            networkDelegate.sendDeltaImmediatly(delta);
        } catch (Exception ce){
            logger.error("Could not send sync Delta message: " + ce);
        }
    }



    public void VirtualSpaceManager.stop(){
        if(isMaster()){
            sendDelta(new StopDelta());
            networkDelegate.stop();
        }
    }

    private void VirtualSpaceManager.startOperation(){
        try{
            networkDelegate.startOperation(appName);
        } catch (Exception ce){
            logger.error("Could not join network channel: " + ce);
        }
    }

    private static class MasterNetworkDelegate implements Receiver {
        private JChannel channel;
        private org.jgroups.View jgroupsView; //Last received jgroup view
        private boolean computeJgroupsView = false; //Has the jgroups view changed
        private HashMap<Address, Boolean> hmap; 
        private boolean block = true;

        void startOperation(String appName) throws Exception {
            hmap = new HashMap<Address, Boolean>();
            channel = ChannelFactory.makeChannel();
            //disable local echo
            channel.setDiscardOwnMessages(true);
            channel.setReceiver(this);
            channel.connect(appName);
        }

        void stop(){
            //Flush the channel and do not restart it
            if(channel.flushSupported()){
    			try {
            		channel.startFlush(false);
            	} catch (Exception ce){
            		System.err.println("Flush error: " + ce);
        		}
            	
            } else {
                //we cannot flush, so try to allow the close message
                //to reach its recipients
                System.err.println("Flush not supported, slaves might not receive quit message");
                try{
                    Thread.sleep(1000);
                } catch (InterruptedException swallow){}
            }
            channel.disconnect();
            channel.close();
        }

        void sendDelta(Delta delta) throws Exception {
            Message msg = new Message(null, null, delta);
            channel.send(msg);
        }

        //Send the delta immediatly and wait for the answer
        void sendDeltaImmediatly(Delta delta) throws Exception {
            Message msg = new Message(null, null, delta);
            msg.setFlag(Message.DONT_BUNDLE);
            channel.send(msg);
        }


        @Override
        public void receive(Message msg) {
            //System.out.println("received msg from " + msg.getSrc() + ": " + msg.getObject()+" in thread"+Thread.currentThread().getId());

            if(!(msg.getObject() instanceof Long)) {
                //
                return;
            }

            Long receivedId = (Long)msg.getObject();

            if (computeJgroupsView)
            {
                hmap.clear();
                System.out.println("resetSlavesMap size:"+jgroupsView.getMembers().size() + " MASTER " +channel.getAddress());

                for (Address addr : jgroupsView.getMembers()) {
                    if (addr.compareTo(channel.getAddress())!=0) //Do not include the master
                        hmap.put(addr,Boolean.FALSE);
                }
                computeJgroupsView=false;
            } 

            boolean receivedAllMsg = true;

            Set set = hmap.entrySet();
            Iterator iterator = set.iterator();
            while(iterator.hasNext()) {
                Map.Entry mentry = (Map.Entry)iterator.next();
                Address addr = (Address)mentry.getKey();
                //System.out.println("Address "+addr+ " mentry.getValue() "+mentry.getValue() + " receivedId "+receivedId);

                if (addr.compareTo(msg.getSrc())==0) {
                    mentry.setValue(Boolean.TRUE);
                } else {
                    if (mentry.getValue()!=Boolean.TRUE) receivedAllMsg = false;
                }
            }


            if (receivedAllMsg) {  
                iterator = set.iterator();

                while(iterator.hasNext()) {
                    Map.Entry mentry = (Map.Entry)iterator.next();
                    mentry.setValue(Boolean.FALSE);
                }



                VirtualSpaceManager.INSTANCE.ackReceive(receivedId);
                //System.out.println("receivedAllMsg TRUE");

            }
//            else 
//                System.out.println("receivedAllMsg FALSE");
        }


        @Override
        public void getState(java.io.OutputStream output) {
            System.out.println("getState");
        };

        @Override
        public void setState(java.io.InputStream input) {
            System.out.println("setState");
        };

        @Override
        public void suspect(Address suspected_mbr) {
            System.out.println("suspect");
        };

        @Override
        public void block() {
            System.out.println("block");
            block = true;
        };

        @Override
        public void unblock() {
            System.out.println("unblock");
            block=false;
        };

        @Override
        public void viewAccepted(org.jgroups.View new_view) {
            System.out.println("viewAccepted " + channel.getAddress());
        //No long running actions, sending of messages or anything that could block should be done in this callback
        jgroupsView = new_view;
        computeJgroupsView = true;
        //Nothing more...
        };



    }
}

