package utils.dispatchOSC;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;

import com.illposed.osc.OSCListener;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCPortIn;
import com.illposed.osc.OSCPortOut;

public class OSCDispatcher {
	
	public static final String HERE = "127.0.0.1";
	
	
	protected OSCPortIn portIn;
	
	protected ArrayList<OSCPortOut> targetPorts;
	
	
	public OSCDispatcher(int pIn, int[] tPorts) {
		
		try {
			
			portIn = new OSCPortIn(pIn);
			
			targetPorts = new ArrayList<OSCPortOut>(tPorts.length);
			
			try {
				
				for (int i = 0 ; i < tPorts.length ; i++) {
					targetPorts.add(new OSCPortOut(InetAddress.getByName(HERE), tPorts[i]));
				}
				
			} catch (UnknownHostException uhe) {
				uhe.printStackTrace();
			}
			
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		OSCListener listener = new OSCListener() {
			
			public void acceptMessage(Date date, OSCMessage msg) {
				
				// System.out.println("Receiving a message : " + msg.getAddress() + ", " + msg.getArguments().length);
				
				try {
					
					for (OSCPortOut po : targetPorts) {
						
						// System.out.println("Sending");
						po.send(new OSCMessage(msg.getAddress(), msg.getArguments()));
						
					}
				
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			
			}
			
		};
		
		portIn.addListener(".*", listener);
		
	}
	
	
	
	public void dispatch() {
		
		portIn.startListening();
		
	}
	
	public void stop() {
		
		portIn.stopListening();
		
	}
	
	public void close() {
		
		for (OSCPortOut po : targetPorts) {
			po.close();
		}
		
		portIn.close();
		
	}
	
}
