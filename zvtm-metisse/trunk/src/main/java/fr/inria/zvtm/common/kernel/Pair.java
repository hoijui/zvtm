package fr.inria.zvtm.common.kernel;

import java.net.Socket;

/**
 * A simple wrapping structure used for Metisse windows id's "NAT"
 * @author Julien Altieri
 *
 */
public class Pair {
	public int detranslatedWindow;
	public Socket dest;
	public Pair(int translatedWindow, Socket dest) {
		this.detranslatedWindow = translatedWindow;
		this.dest = dest;
	}	
}