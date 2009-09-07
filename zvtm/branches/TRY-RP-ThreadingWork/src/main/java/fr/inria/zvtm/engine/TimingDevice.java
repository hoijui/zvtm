package fr.inria.zvtm.engine;

/** 
 * Crude timing class that reports elapsed time 
 * after a number of 'events' have occured 
 */
public class TimingDevice {
	private final int eventCount;
	private int currentEvent = 0;
	private long lastTimestamp; //nanoseconds

	public TimingDevice(int eventCount){
		if( eventCount <= 0 ){
			throw new IllegalArgumentException("positive event count expected");
		}
		this.eventCount = eventCount;
		lastTimestamp = System.nanoTime(); //XXX
	}

	public void event(){
		currentEvent++;
		if( (currentEvent % eventCount) == 0 ){
			long timestamp = System.nanoTime();
			System.out.print("avg events per second: ");
			System.out.println(((float)eventCount / (timestamp - lastTimestamp) * 1000000000));
			lastTimestamp = timestamp;
		}
	}
}
