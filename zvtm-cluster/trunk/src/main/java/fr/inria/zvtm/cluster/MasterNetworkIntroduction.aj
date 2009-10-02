/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
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
	private String VirtualSpaceManager.appName;
	private final Logger VirtualSpaceManager.logger = 
		LoggerFactory.getLogger(VirtualSpaceManager.class);


	public void VirtualSpaceManager.setMaster(String app){
		appName = app;
		startOperation();
	}

	public void VirtualSpaceManager.sendDelta(Delta delta){
		//assert(vsm.isMaster());
		try{
		networkDelegate.sendDelta(delta);
		} catch (ChannelException ce){
			logger.error("Could not send Delta message: " + ce);
		}
	}

	void VirtualSpaceManager.stop(){
		networkDelegate.stop();
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
			channel = new JChannel();
			//disable local echo
			channel.setOpt(Channel.LOCAL, Boolean.FALSE); 
			channel.connect(appName);
		}

		void stop(){
			channel.close();
		}

		void sendDelta(Delta delta) throws ChannelException {
			Message msg = new Message(null, null, delta);
				channel.send(msg);
		}

	}
}

