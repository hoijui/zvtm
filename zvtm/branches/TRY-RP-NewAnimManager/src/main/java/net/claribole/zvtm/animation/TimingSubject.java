package net.claribole.zvtm.animation;

import org.jdesktop.animation.timing.TimingTarget;

public abstract class TimingSubject implements TimingTarget {
    public abstract void begin();
    public abstract void end();
    public abstract void repeat();
    public abstract void timingEvent(float fraction);
    
    public static enum Dimension {POSITION, ALTITUDE, SIZE, 
	    BORDERCOLOR, FILLCOLOR, TRANSLUCENCY, PATH};
    public abstract Object subject();
    public abstract Dimension dimension();
    
    public boolean orthogonalWith(TimingSubject other){
	return ( !((subject() == other.subject()) &&
		   (dimension() == other.dimension())) );
    }
}