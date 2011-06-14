package fr.inria.zvtm.common.kernel;

import java.net.Socket;

public class Pair {
	public int detranslatedWindow;
	public Socket dest;
	public Pair(int translatedWindow, Socket dest) {
		this.detranslatedWindow = translatedWindow;
		this.dest = dest;
	}	
}