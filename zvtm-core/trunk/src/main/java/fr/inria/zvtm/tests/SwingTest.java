package fr.inria.zvtm.tests;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VSwingComponent;

/**
 * Testing VSwingComponent
 */
public class SwingTest {
    JPanel drawnPanel;

    final VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    Camera cam;
    View view;
    VirtualSpace testSpace;
    
    public SwingTest() throws Exception {
        TableModel dataModel = new AbstractTableModel() {
            public int getColumnCount() { return 10; }
            public int getRowCount() { return 10;}
            public Object getValueAt(int row, int col) { return new Integer(row*col); }
        };
        JTable table = new JTable(dataModel);
        table.setVisible(true);
        drawnPanel = new JPanel();
        drawnPanel.setBounds(0,0,300,300);
        drawnPanel.add(table);
        JButton btn = new JButton("42");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                System.out.println("OMG I got clicked!!");
            }
        });
        drawnPanel.add(btn);
        SwingUtilities.invokeAndWait(new Runnable(){
            public void run(){
                drawnPanel.doLayout();
            }
        });
        VSwingComponent vsc = new VSwingComponent(drawnPanel);
        //JButton tst = new JButton("foo");
        //tst.setSize(tst.getPreferredSize());
        //VSwingComponent vsc = new VSwingComponent(tst);

        testSpace = vsm.addVirtualSpace("testSpace");
        cam = testSpace.addCamera();
        testSpace.addGlyph(vsc);
        view = vsm.addFrameView(new Vector(Arrays.asList(new Camera[]{cam})),
                "VSwingTest", View.STD_VIEW, 800, 600, false, true);
        view.getCursor().setColor(Color.GREEN);
        view.setListener(new EvRedirector());
    }

    /**
     *
     */
    class EvRedirector extends ViewAdapter {
        @Override public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
            //pick glyph under cursor, redispatch if it is an instance
            //of VSwingComponent
            Glyph[] pickList = v.getVCursor().getGlyphsUnderMouseList();
            if(pickList.length == 0){
                return;
            }
            Glyph pickedGlyph = pickList[pickList.length - 1];
            if(pickedGlyph instanceof VSwingComponent){
                redispatch(v, e, (VSwingComponent)pickedGlyph);
            }
            System.err.println("press1");
        }

        @Override public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
           //pick glyph under cursor, redispatch if it is an instance
            //of VSwingComponent
            Glyph[] pickList = v.getVCursor().getGlyphsUnderMouseList();
            if(pickList.length == 0){
                return;
            }
            Glyph pickedGlyph = pickList[pickList.length - 1];
            if(pickedGlyph instanceof VSwingComponent){
                redispatch(v, e, (VSwingComponent)pickedGlyph);
            }
        }

        public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){
            //pick glyph under cursor, redispatch if it is an instance
            //of VSwingComponent
            Glyph[] pickList = v.getVCursor().getGlyphsUnderMouseList();
            if(pickList.length == 0){
                return;
            }
            Glyph pickedGlyph = pickList[pickList.length - 1];
            if(pickedGlyph instanceof VSwingComponent){
                redispatch(v, e, (VSwingComponent)pickedGlyph);
            }
        }
        public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){
            Glyph[] pickList = v.getVCursor().getGlyphsUnderMouseList();
            if(pickList.length == 0){
                return;
            }
            Glyph pickedGlyph = pickList[pickList.length - 1];
            if(pickedGlyph instanceof VSwingComponent){
                redispatch(v, e, (VSwingComponent)pickedGlyph);
            }
        }
        public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e){
            Glyph[] pickList = v.getVCursor().getGlyphsUnderMouseList();
            if(pickList.length == 0){
                return;
            }
            Glyph pickedGlyph = pickList[pickList.length - 1];
            if(pickedGlyph instanceof VSwingComponent){
                redispatch(v, e, (VSwingComponent)pickedGlyph);
            }
        }

        void redispatch(ViewPanel v, final MouseEvent evt, VSwingComponent c){
            //we have an event location in View coordinates
            //- transform into VS coords.
            //- transform VS coords into "global component" (VSC) coords
            //- find the deepest component within the VSC
            //- transform VSC coords into deepest component coords [TODO test]
            //- translate original event, reset its source to the 
            //deepest component and redispatch it
            Point2D vsCoords = v.viewToSpaceCoords(v.cams[v.activeLayer], evt.getX(), evt.getY()); //XXX replace v.cams... by v.getActiveCamera
            Point2D pt = spaceToComponent(c, vsCoords);
            final Component cmp = SwingUtilities.getDeepestComponentAt(c.getComponent(), (int)pt.getX(), (int)pt.getY());
            //System.out.println("Deepest component: " + cmp);
            Point2D deepestCoords = new Point2D.Double(pt.getX() - cmp.getX(),
                    pt.getY() - cmp.getY());
            System.err.println("Deepest coords: " + 
                    (int)(deepestCoords.getX()) + ", " +
                    (int)(deepestCoords.getY()));
            System.out.println("Event coords: " +
                    evt.getX() + ", " + evt.getY());
            evt.translatePoint((int)(deepestCoords.getX() - evt.getX()),
                    (int)(deepestCoords.getY() - evt.getY()));
            evt.setSource(cmp);
            cmp.dispatchEvent(evt);
        }

        Point2D spaceToComponent(VSwingComponent c, Point2D vsCoords){
            return new Point2D.Double(vsCoords.getX() - (c.vx - c.getWidth()/2),
                    (c.vy + c.getHeight()/2) - vsCoords.getY());
        }
    }

    public static void main(String[] args) throws Exception {
        new SwingTest();    
    }
}

