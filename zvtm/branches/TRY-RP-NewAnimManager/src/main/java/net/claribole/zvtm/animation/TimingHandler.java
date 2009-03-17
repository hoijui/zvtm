package net.claribole.zvtm.animation;

public interface TimingHandler {
    public void begin(Object subject, Animation.Dimension dim);
    public void end(Object subject, Animation.Dimension dim);
    public void repeat(Object subject, Animation.Dimension dim);
    public void timingEvent(float fraction, 
			    Object subject, Animation.Dimension dim);
}