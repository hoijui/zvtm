package fr.inria.zvtm.cluster;

import java.util.Vector;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.StdViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.cluster.SlaveUpdater;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.Address;
import org.jgroups.util.MessageBatch;


public class ClusteredViewPanel extends StdViewPanel {

    private boolean lockPaint = true; //Prevent asynchroned call to paint
    private long lastOffScreenPaintTime = 0;
    private boolean displayFPS = false;
    private SlaveUpdater slaveUpdater;
    private long lastOffscreenPaintId;

    ClusteredViewPanel(Vector<Camera> cameras,View v, boolean arfome) {
        super(cameras, v, arfome);
    }

    public void setSlaveUpdater(SlaveUpdater slaveUpdater) {
        this.slaveUpdater = slaveUpdater;
    }

    @Override
    protected void initPanel () {
        edtTimer=new Timer(10000,null);
        edtTimer.setRepeats(false);
        panel = new JPanel(){
            @Override
            public void paint(Graphics g) {
                if (backBuffer != null){
                    if (!lockPaint) {
                        if (displayFPS) {
                            long currentTime = System.nanoTime(); 
                            double fps = (double)(currentTime - lastOffScreenPaintTime);
                            lastOffScreenPaintTime = currentTime;
                            fps /= 1000000000.0d;
                            fps = 1.0d/fps;
                            g.drawImage(backBuffer, 0, 0, panel);
                            g.drawString(String.format("FPS=%.2f",fps), 50, 50 );                        
                        }
                        else 
                            g.drawImage(backBuffer, 0, 0, panel);
                        lockPaint=true;
                    }
                }
                else System.out.println("Backbuffer NULL");
                try{
                       slaveUpdater.sendAckSync(lastOffscreenPaintId);
                } catch (Exception ce){
                    System.out.println("Could not send sync Ack message: " + ce);
                    ce.printStackTrace(System.out);
                }                        
            }
        };
    }

    public void setDisplayFPS(boolean displayFPS) {
        this.displayFPS = displayFPS;
    }

    public void drawAndAck(long id) {
        lastOffscreenPaintId = id;
        super.drawOffscreen();
        try{
            slaveUpdater.sendAckSync(lastOffscreenPaintId);
        } catch (Exception ce){
            System.out.println("Could not send sync Ack message: " + ce);
            ce.printStackTrace(System.out);
        }  
    }

    public void paintAndAck(long id) {
        lastOffscreenPaintId = id;
        lockPaint=false;
        panel.paintImmediately(0,0,panel.getWidth(),panel.getHeight());
    }

   
}


