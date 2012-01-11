/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.cluster;

import java.io.File;

import org.jgroups.ChannelException;
import org.jgroups.JChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Non public factory: eases creation of JChannel sharing a common protocol.
//If the property "zvtm.cluster.channel_conf" is defined, use that, otherwise
//use an embedded, default channel configuration.
class ChannelFactory {
    private static final String DEFAULT_PROPFILE_PATH = "/config/flush-udp.xml";
    private static final Logger logger = LoggerFactory.getLogger(ChannelFactory.class);

    private static class Dummy{}

    /*
     * Creates a new channel. 
     */
    static final JChannel makeChannel() throws ChannelException{
        final String propFile = System.getProperty("zvtm.cluster.channel_conf");
        if(propFile == null){
            //create channel from the embedded configuration,
            //should always be available
            logger.info("Creating channel from default configuration {}", DEFAULT_PROPFILE_PATH);
            //WTF?
            return new JChannel(new Dummy().getClass().getResource(DEFAULT_PROPFILE_PATH));
            //return new JChannel(ClassLoader.getSystemResource(DEFAULT_PROPFILE_PATH));
        } else {
            logger.info("Creating channel from configuration file {}", propFile);
            return new JChannel(new File(propFile));
        }
    }

    //disallow instanciation
    private ChannelFactory(){}
}

