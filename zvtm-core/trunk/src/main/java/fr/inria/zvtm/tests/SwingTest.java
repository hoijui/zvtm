package fr.inria.zvtm.tests;

import java.awt.Color;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
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
    
    public SwingTest(){
        TableModel dataModel = new AbstractTableModel() {
            public int getColumnCount() { return 10; }
            public int getRowCount() { return 10;}
            public Object getValueAt(int row, int col) { return new Integer(row*col); }
        };
        JTable table = new JTable(dataModel);
        table.setVisible(true);
        table.setSize(200, 100);
        drawnPanel = new JPanel();
        drawnPanel.add(table);
        VSwingComponent vsc = new VSwingComponent(table);

        testSpace = vsm.addVirtualSpace("testSpace");
        cam = testSpace.addCamera();
        testSpace.addGlyph(vsc);
        view = vsm.addFrameView(new Vector(Arrays.asList(new Camera[]{cam})),
                "VSwingTest", View.STD_VIEW, 800, 600, false, true);
        view.getCursor().setColor(Color.GREEN);
        //view.getGlobalView(cam, 500);
    }

    public static void main(String[] args){
        new SwingTest();    
    }
}

