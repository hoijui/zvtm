/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

import java.util.HashMap;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import org.jgroups.Address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.log4j.BasicConfigurator;

import javax.swing.SwingUtilities;

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
        BasicConfigurator.configure();
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
    
    public void VirtualSpaceManager.sendDeltaPI(Delta delta){
        if(!isMaster()){
            throw new IllegalStateException(
                "VirtualSpaceManager must be a master to send deltas, see setMaster()");
        }

        try{
            networkDelegate.sendDeltaPI(delta);
        } catch (Exception ce){
            logger.error("Could not send Delta message: " + ce);
        }
    }

    public void VirtualSpaceManager.sendDeltaPI(Address add, Delta delta){
        if(!isMaster()){
            throw new IllegalStateException(
                "VirtualSpaceManager must be a master to send deltas, see setMaster()");
        }

        try{
            networkDelegate.sendDeltaPI(add,delta);
        } catch  (Exception ce){ 
            logger.error("Could not send Delta message: " + ce);
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
        } catch  (Exception ce){
            logger.error("Could not join network channel: " + ce);
        }
    }

    private void  VirtualSpaceManager.handleSyncPaint(Message msg) {
        // FIXME: several clustered views
        if (allClusteredViews==null || !isMaster()) return;
        //System.out.println("VirtualSpaceManager.handleSyncPaint: " + allClusteredViews.length);
        for (int i=0;i<allClusteredViews.length;i++){
            allClusteredViews[i].handleSyncPaint(msg);
        }
    }

    private static class MasterNetworkDelegate {
        private JChannel channel;

        void startOperation(String appName)  throws Exception { 
            channel = ChannelFactory.makeChannel();
            //disable local echo
            channel.setDiscardOwnMessages(true);
            channel.connect(appName);

            channel.setReceiver(new ReceiverAdapter(){
                @Override public void viewAccepted(View newView){
                    VirtualSpaceManager.INSTANCE.logger.info("master new view: {}", newView);
                    //System.err.println("new view: {}"+ newView);
                }

                @Override public void receive(Message msg){
                    final Message mmsg = msg;
                    SwingUtilities.invokeLater(new Runnable(){
                        public void run(){
                            if(mmsg.getObject() instanceof ToMasterMsg){
                                ((ToMasterMsg)mmsg.getObject()).apply();
                            }
                            else {
                                // for now we receive only sync paint msg from the slaves
                                VirtualSpaceManager.INSTANCE.handleSyncPaint(mmsg);
                            }
                        }
                    });
                }
            });
        }

        void stop(){
            //Flush the channel and do not restart it
            if(channel.flushSupported()){
                try{
                    channel.startFlush(false);
                }
                catch (Exception e) {}
            } else {
                //we cannot flush, so try to allow the close message
                //to reach its recipients
                System.err.println("Flush not supported, slaves might not receive quit message");
                //try{
                    //Thread.sleep(1000);
                //} catch (InterruptedException swallow){}
            }
            channel.disconnect();
            channel.close();
        }

        // send rendering replication DONT_BUNDLE for speed... ???
        void sendDelta(Delta delta) throws Exception {
            Message msg = new Message(null, null, delta);
            //msg.setFlag(Message.DONT_BUNDLE);
            channel.send(msg);
        }

        // send paint immediatly, bundle for sync
        void sendDeltaPI(Delta delta) throws Exception {
            Message msg = new Message(null, null, delta);
            channel.send(msg);
        }

        // send paint immediatly, bundle for sync
        void sendDeltaPI(Address add, Delta delta) throws Exception {
            Message msg = new Message(add, null, delta);
            channel.send(msg);
        }

    }
 }

