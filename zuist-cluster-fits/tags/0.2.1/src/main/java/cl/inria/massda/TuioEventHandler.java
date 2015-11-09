

package cl.inria.massda;


import TUIO.TuioListener;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioObject;
import TUIO.TuioTime;
import TUIO.TuioPoint;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.Camera;

import java.util.Vector;

import fr.inria.zuist.viewer.FitsViewer;


public class TuioEventHandler implements TuioListener{

	FitsViewer application;

    WallTuioCursor[] cursors;
    int[] countCorrect;
    private boolean pan = false;
    private boolean zoom = false;
    private static final int minTrust = 4;
    static final float WHEEL_ZOOMIN_FACTOR = 15.0f;
    static final float WHEEL_ZOOMOUT_FACTOR = 16.0f;

    double lastDistance = 0.0;


	public TuioEventHandler(FitsViewer app){
        this(app, 3333);
    }

	public TuioEventHandler(FitsViewer app, int port){
		application = app;
		initTUIO(port);
        cursors = new WallTuioCursor[40];
        countCorrect = new int[40];
	}

	void initTUIO(int port){
        TuioClient client = new TuioClient(port);
        client.addTuioListener(this);
        client.connect();
        System.out.println("Listening to TUIO events on port "+port);
    }   

    public void refresh(TuioTime btime){
        
        int countTouch = 0;
        Vector<Integer> touchs = new Vector();
        for(int i = 0; i < cursors.length; i++){
            if(cursors[i] != null){
                if(( btime.getTotalMilliseconds() - cursors[i].getTotalMilliseconds() ) == 0 ){
                    countCorrect[i]++;
                } else {
                    countCorrect[i] = 0;
                }
                if( countCorrect[i] >= minTrust){
                    cursors[i].setVisible(true);
                    touchs.add(i);
                    countTouch++;
                } else {
                    cursors[i].setVisible(false);
                }
                //if(countCorrect[i] != 0) System.out.println("countCorrect: " + countCorrect[i]);
            }
        }

        //if(countTouch != 0) System.out.println("countTouch: "+countTouch);

        Camera c = application.mCamera;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        double distance = 0.0;

        switch(countTouch){
            case 3:

                break;
            case 2:
                if(zoom){
                    distance = ( cursors[touchs.get(countTouch-1)].getDistance(cursors[touchs.get(countTouch-2)]));
                    double x =  (cursors[touchs.get(countTouch-1)].getX() + cursors[touchs.get(countTouch-2)].getX() )/2;
                    double y =  (cursors[touchs.get(countTouch-1)].getY() + cursors[touchs.get(countTouch-2)].getY() )/2;
                    application.centeredZoom( (lastDistance / distance), x+application.SCENE_W/2, -y+application.SCENE_H/2);
                }
                zoom = true;
                pan = false;
                distance = ( cursors[touchs.get(countTouch-1)].getDistance(cursors[touchs.get(countTouch-2)]));
                lastDistance = distance;
                break;
            case 1:
                if(pan){
                    c.move( a*cursors[touchs.get(countTouch-1)].getDistanceX(), a*cursors[touchs.get(countTouch-1)].getDistanceY());
                }
                pan = true;
                zoom = false;
                break;
            
            default:
                pan = false;
                zoom = false;
                break;
        }
    }

    public void addTuioCursor(TuioCursor tcur){
        int cursorID = tcur.getCursorID();
        double x = tcur.getX();
        double y = tcur.getY();
        long time = tcur.getTuioTime().getTotalMilliseconds();

        double xx = x*application.SCENE_W-application.SCENE_W/2;
        double yy = (1-y)*application.SCENE_H-application.SCENE_H/2;

        if(cursorID >= 0 && cursorID < cursors.length && x >= 0 && x <=1 && y >= 0 && y <= 1){
            if(cursors[cursorID] == null){
                WallTuioCursor wtc = new WallTuioCursor(application.cursorSpace, cursorID, xx, yy, time);
                cursors[cursorID] = wtc;
            } else {
                cursors[cursorID].setTuioCursor(time, xx, yy);
            } 
        }
    }
    
    public void updateTuioCursor(TuioCursor tcur){
        int cursorID = tcur.getCursorID();
        double x = tcur.getX();
        double y = tcur.getY();
        long time = tcur.getTuioTime().getTotalMilliseconds();

        double xx = x*application.SCENE_W-application.SCENE_W/2;
        double yy = (1-y)*application.SCENE_H-application.SCENE_H/2;
        if(cursorID >= 0 && cursorID < cursors.length && x >= 0 && x <=1 && y >= 0 && y <= 1){
            if(cursors[cursorID] == null){
                WallTuioCursor wtc = new WallTuioCursor(application.cursorSpace, cursorID, xx, yy, time);
                cursors[cursorID] = wtc;
            } else {
                cursors[cursorID].setTuioCursor(time, xx, yy);
            } 
        }

    }

    public void removeTuioCursor(TuioCursor tcur){
    }
    
    public void addTuioObject(TuioObject tobj){}
    public void updateTuioObject(TuioObject tobj){}
    public void removeTuioObject(TuioObject tobj){}

}