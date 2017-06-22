package fr.inria.zvtm.cluster;

import java.util.Vector;
import javax.swing.JPanel;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.AlphaComposite;
import javax.swing.Timer;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.Set;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.StdViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.cluster.SlaveUpdater;

import org.jgroups.Channel;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.Address;
import org.jgroups.util.MessageBatch;


public class ClusteredViewPanel extends StdViewPanel implements SyncView {

    private boolean lockPaint = true; //Prevent asynchroned call to paint
    private long lastOffScreenPaintTime = 0;
    private SlaveUpdater slaveUpdater;

    ClusteredViewPanel(Vector<Camera> cameras,View v, boolean arfome) {
        super(cameras, v, arfome);
    }

    public void setSlaveUpdater(SlaveUpdater slaveUpdater) {
        this.slaveUpdater = slaveUpdater;
    }

    @Override
    protected void initPanel () {
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
                            Color currentColor = g.getColor();
                            g.setColor(Color.RED);
                            g.drawString(String.format("FPS=%.2f",fps), 50, 50 );
                            g.setColor(currentColor);
                        }
                        else {              
                            g.drawImage(backBuffer, 0, 0, panel);
                        }
                        // draw the offscreen portal here !
                        for (int i=0;i<parent.portals.length;i++){
                            Portal p = parent.portals[i];
                            BufferedImage bi = p.getBufferImage();
                            if (bi != null){
                                AlphaComposite ac = p.getAlphaComposite();
                                if (ac != null){
                                    ((Graphics2D)g).setComposite(ac);
                                }
                                g.drawImage(bi, p.getBufferX(), p.getBufferY(), panel);
                                if (ac != null){
                                    ((Graphics2D)g).setComposite(Translucent.acO);
                                }
                            }     
                        }
                        if (overlayBuffer != null){
                            g.drawImage(overlayBuffer, 0, 0, panel);
                        }
                        lockPaint=true;
                    }
                }
                try{
                       slaveUpdater.sendAckSync();
                } catch (Exception ce){
                    System.out.println("Could not send sync Ack message: " + ce);
                    ce.printStackTrace(System.out);
                }                        
            }
        };
    }

    public void drawAndAck() {
        super.drawOffscreen();
        try{
            slaveUpdater.sendAckSync();
        } catch (Exception ce){
            System.out.println("Could not send sync Ack message: " + ce);
            ce.printStackTrace(System.out);
        }  
    }

    public void paintAndAck() {
        lockPaint=false;
        panel.paintImmediately(0,0,panel.getWidth(),panel.getHeight());
    }

   

    @Override
    public void setRefreshRate(int rr){
        System.out.println("setRefreshRate disabled using -s option");
    }

    @Override
    public int getRefreshRate(){
        System.out.println("getRefreshRate disabled using -s option");
        return 0;
    }


}


