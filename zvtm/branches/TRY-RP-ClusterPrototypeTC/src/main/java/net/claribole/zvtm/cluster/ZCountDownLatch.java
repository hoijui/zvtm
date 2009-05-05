package net.claribole.zvtm.cluster;

//Code adapted from Tushar Khairnar's website, see
//http://tusharkhairnar.blogspot.com
public class ZCountDownLatch {
	int count = -1;

	public ZCountDownLatch(int count)
	{
		this.count = count;
	}

	public synchronized void countDown()
	{
		count--;
		if (count == 0) { notifyAll();  }
	}

	public synchronized void reset(int count)
	{
		this.count = count;
	}

	public synchronized void await() throws InterruptedException 
	{
        if (count == 0) { notifyAll(); return; }
        else { 
        	while(count > 0)
        		{
        			wait(); 
        		}
        }
	}
}

