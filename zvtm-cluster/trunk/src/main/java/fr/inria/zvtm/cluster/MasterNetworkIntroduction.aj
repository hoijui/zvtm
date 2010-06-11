/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009-2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpaceManager;

import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.Message;

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
		} catch (ChannelException ce){
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
		} catch (ChannelException ce){
			logger.error("Could not join network channel: " + ce);
		}
	}

	private static class MasterNetworkDelegate {
		private JChannel channel;

		void startOperation(String appName) throws ChannelException {
			channel = ChannelFactory.makeChannel();
			//disable local echo
			channel.setOpt(Channel.LOCAL, Boolean.FALSE); 
			channel.connect(appName);
		}

		void stop(){
            //Flush the channel and do not restart it
            if(channel.flushSupported()){
                channel.startFlush(false);
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

		void sendDelta(Delta delta) throws ChannelException {
			Message msg = new Message(null, null, delta);
            channel.send(msg);
		}

	}
}

