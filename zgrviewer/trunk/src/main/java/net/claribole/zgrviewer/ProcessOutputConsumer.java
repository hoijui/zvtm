/**
 * A simple thread that will consume the stdout and stderr streams of a process, to prevent deadlocks.
 * 
 * @author David J. Hamilton <hamilton37@llnl.gov>
 *
 *$Id:$
 */

package net.claribole.zgrviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class ProcessOutputConsumer extends Thread {

	// Wrapping the inpustreams in readers because on my system
	// FileInputStream#skip would not actually consume any of the available
	// input
	private BufferedReader pout, perr;
	
	private long waitTime = 200;
	

	/**
	 * @param p The process whose stdout and stderr streams are to be consumed.
	 * @throws IOException 
	 */
	public ProcessOutputConsumer(Process p) throws IOException {
		p.getOutputStream().close();
		
		pout = new BufferedReader( new InputStreamReader( p.getInputStream()));
		perr = new BufferedReader( new InputStreamReader( p.getErrorStream()));
		
		setDaemon( true);
	}
	
	/**
	 * @param p The process whose stdout and stderr streams are to be consumed.
	 * @param waitTime How long to wait (in ms) between checks for output to consume.
	 * @throws IOException 
	 */
	public ProcessOutputConsumer(Process p, long waitTime) throws IOException {
		this(p);
		this.waitTime = waitTime;
	}
	

	@Override
	public void run() {
		try {
			while (true) {		
				while( pout.ready())
					pout.readLine();
				while( perr.ready())
					perr.readLine();
				
				if( waitTime > 0)
					sleep( waitTime);
			}
		} 
		catch (IOException e) { /* do nothing */ }
		catch (InterruptedException e) { /* do nothing */}
	}
}
