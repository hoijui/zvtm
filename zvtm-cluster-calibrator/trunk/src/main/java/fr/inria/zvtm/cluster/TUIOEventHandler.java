



package fr.inria.zvtm.cluster;

import fr.inria.zvtm.cluster.tuio.WallTuioCursor;

import TUIO.TuioListener;
import TUIO.TuioClient;
import TUIO.TuioCursor;
import TUIO.TuioObject;
import TUIO.TuioTime;
import TUIO.TuioPoint;

import fr.inria.zvtm.engine.VirtualSpace;

import java.util.Vector;


class TUIOEventHandler implements TuioListener{

	Calibrator application;
	Vector<WallTuioCursor> cursors;


	TUIOEventHandler(Calibrator app){
		application = app;
		initTUIO(3333);
        cursors = new Vector<WallTuioCursor>();
	}

	TUIOEventHandler(Calibrator app, int port){
		application = app;
		initTUIO(port);
        cursors = new Vector<WallTuioCursor>();
	}

	void initTUIO(int port){
        TuioClient client = new TuioClient(port);
        client.addTuioListener(this);
        client.connect();
        System.out.println("Listening to TUIO events on port "+port);
    }


    public void addTuioCursor(TuioCursor tcur){
        //System.out.println("A C "+tcur.getPosition().getX()+" "+tcur.getPosition().getY());
        //application.addObject(tcur.getPosition());
        //System.out.println("addTuioCursor");
        //showCursor(tcur);

        
        int cursorID = tcur.getCursorID();
        if(cursorID >= cursors.size()){
            WallTuioCursor wtc = new WallTuioCursor(application.mSpace, tcur);
            wtc.setVisible(true);
            double x = tcur.getPosition().getX()*application.SCENE_W - application.SCENE_W/2;
            double y = application.SCENE_H/2 - tcur.getPosition().getY()*application.SCENE_H;
            wtc.moveTo(x, y);
            cursors.add(wtc);
        } else {
            WallTuioCursor wtc = cursors.get(tcur.getCursorID());
            wtc.setVisible(true);
            double x = tcur.getPosition().getX()*application.SCENE_W - application.SCENE_W/2;
            double y = application.SCENE_H/2 - tcur.getPosition().getY()*application.SCENE_H;
            wtc.moveTo(x, y);
        }
        
    }

    public void addTuioObject(TuioObject tobj){
        //System.out.println("A O "+tobj.getPosition());
        //application.addObject(tobj.getPosition());
    }

    public void refresh(TuioTime btime){
        // System.out.println("R at "+btime);
    }

    public void removeTuioCursor(TuioCursor tcur){
        // System.out.println("R C "+tcur);
        //System.out.println("removeTuioCursor");
        //showCursor(tcur);

        WallTuioCursor wtc = cursors.get(tcur.getCursorID());
        wtc.setVisible(false);
    }

    public void removeTuioObject(TuioObject tobj){
        // System.out.println("R O "+tobj);
    }

    public void updateTuioCursor(TuioCursor tcur){
        // System.out.println("U C "+tcur.getPosition().getX()+" "+tcur.getPosition().getY());
        //System.out.println("updateTuioCursor");
        //showCursor(tcur);

        WallTuioCursor wtc = cursors.get(tcur.getCursorID());
        double x = tcur.getPosition().getX()*Calibrator.SCENE_W - Calibrator.SCENE_W/2;
        double y = Calibrator.SCENE_H/2 - tcur.getPosition().getY()*Calibrator.SCENE_H;
        wtc.moveTo(x, y);

    }

    public void updateTuioObject(TuioObject tobj){
        // System.out.println("U O "+tobj.getPosition());
    }

    private void showCursor(TuioCursor tcur){
    	System.out.println("    ID        : "+tcur.getCursorID());
    	//System.out.println("    Accel     : "+tcur.getMotionAccel());
		//System.out.println("    Speed     : "+tcur.getMotionSpeed());
		System.out.println("    Point     : ("+tcur.getPosition().getX() + ", " + tcur.getPosition().getY() + ")");
		//System.out.println("    SessionID : " + tcur.getSessionID());
		System.out.println("    TuioState : " + tcur.getTuioState());
        //System.out.println("    XSpeed    : " + tcur.getXSpeed());
        //System.out.println("    YSpeed    : " + tcur.getYSpeed());
        System.out.println("    isMoving  : " + tcur.isMoving());
    }

}