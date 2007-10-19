/*   FILE: MPDManager.java
 *   DATE OF CREATION:   May 9 2005
 *   AUTHOR :            Eric Mounhem (skbo@lri.fr)
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 * 
 * $Id$
 */

package net.claribole.zvtm.mpd;

import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.MutationEvent;
import org.w3c.dom.xpath.XPathResult;

import net.claribole.zvtm.engine.ViewEventHandler;
import com.xerox.VTM.engine.View;
import com.xerox.VTM.engine.ViewPanel;

import domino.Device;
import domino.Domino;
import domino.DominoImplementation;
import domino.Sensor;
import domino.Socket;
import domino.EventCodes;

/**
 * Manage Multiple Pointing Devices using Domino
 * 
 * @author skbo
 */
public class MPDManager {

    private class SocketEvents {

        private class Couple {
            EventListener event;
            Sensor        sensor;

            public Couple(EventListener event, Sensor sensor) {
                this.event = event;
                this.sensor = sensor;
            }
        }

        /**
         * Socket to which attach the EventListeners
         */
        Socket   socket;
        /**
         * EventListeners associated to a sensor
         */
        Couple[] events;

        /**
         * Add an EventListener to the class's list
         * 
         * @param evt
         *            EventListener to add
         */
        void addListener(EventListener event, Sensor sensor) {
            if (this.events == null) {
                this.events = new Couple[1];
                this.events[0] = new Couple(event, sensor);

            } else {
                Couple[] tmp = new Couple[this.events.length + 1];
                System.arraycopy(this.events, 0, tmp, 0, this.events.length);
                tmp[tmp.length - 1] = new Couple(event, sensor);
                this.events = tmp;
            }
        }
    }

    /**
     * Domino object that found event devices on the system
     */
    static Domino          domino;

    /**
     * Event Handler
     */
    MPDAppEventHandler     evh;

    /**
     * ViewPanel associated to an MPDAppEventHandler
     */
    ViewPanel              viewPanel;

    /**
     * Pointing devices discovered by Domino
     */
    private SocketEvents[] pointingDevices;

    /**
     * Every keyboard discovered by Domino
     */
    private Socket[]       keyboardDevices;
    
    /**
     * @return the list of pointing devices on the system (null if none)
     */
    public Socket[] getAvailablePointingDevices() {
        if (this.pointingDevices != null) {
            Socket[] res = new Socket[this.pointingDevices.length];
            for (int i = 0; i < this.pointingDevices.length; i++) {
                res[i] = this.pointingDevices[i].socket;
            }
            return res;
        }
        return null;
    }

    /**
     * Clean all domino instances and forks
     */
    public static void terminate() {
        domino.terminate();
    }

    /**
     * Add a mouse socket to the pool of sockets discovered
     * 
     * @param socket
     *            A socket identifying a pointing device
     */
    private void addPointingDevice(Socket socket) {
        if (this.pointingDevices == null) {
            this.pointingDevices = new SocketEvents[1];
            this.pointingDevices[0] = new SocketEvents();
            this.pointingDevices[0].socket = socket;
        } else {
            SocketEvents[] tmp = new SocketEvents[this.pointingDevices.length + 1];
            System.arraycopy(this.pointingDevices, 0, tmp, 0,
                    this.pointingDevices.length);
            tmp[tmp.length - 1] = new SocketEvents();
            tmp[tmp.length - 1].socket = socket;
            this.pointingDevices = tmp;
        }
    }

    /**
     * Add a keyboard socket to the pool of sockets discovered
     * 
     * @param socket
     *            A socket identifying a pointing device
     */
    private void addKeyboardDevice(Socket socket) {
        if (this.keyboardDevices == null) {
            this.keyboardDevices = new Socket[1];
            this.keyboardDevices[0] = socket;
        } else {
            Socket[] tmp = new Socket[this.keyboardDevices.length + 1];
            System.arraycopy(this.keyboardDevices, 0, tmp, 0,
                    this.keyboardDevices.length);
            tmp[tmp.length - 1] = socket;
            this.keyboardDevices = tmp;
        }
    }

    /**
     * Default constructor for an MPDManager object that found new pointing
     * devices on the system
     * 
     * @param evh
     *            MPDAppEventHandler implementing the methods you want to listen
     * @param view
     *            View associated with these devices
     */
    public MPDManager(MPDAppEventHandler evh, View view) {
        this.evh = evh;
        if (view != null)
            this.viewPanel = view.getPanel();
        domino = DominoImplementation.getInstance().createDomino(8, true, false);
        domino.initialize();

        XPathResult nodeSet;

        // TODO: complete the XPath expression to other modifiers
        nodeSet = (XPathResult) domino.evaluate(
                "/domino/socket[device/sensor/@name='key left shift']", domino,
                null, XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
        for (int n = 0; n < nodeSet.getSnapshotLength(); n++) {
            Socket socket = (Socket) nodeSet.snapshotItem(n);
            System.err.println("socket " + socket.getNumber() + ": "
                    + socket.getDeviceDescriptor().getName()
                    + " -> adding default keyboard listeners");
            addKeyboardDevice(socket);
        }

        // We reach devices through the Domino DOM tree that have x and y
        // sensors and at least one button (left, right or middle)
        nodeSet = (XPathResult) domino.evaluate(
                "/domino/socket[device/sensor[@name='y'] "
                        + "and device/sensor[@name='x'] "
                        + "and device/sensor[substring(@name,1,4)='btn ' "
                        + "and (substring(@name,5,4)='left'"
                        + "or substring(@name,5,5)='right'"
                        + "or substring(@name,5,6)='middle')]]", domino, null,
                XPathResult.ORDERED_NODE_SNAPSHOT_TYPE, null);
        for (int n = 0; n < nodeSet.getSnapshotLength(); n++) {
            Socket s = (Socket) nodeSet.snapshotItem(n);
            System.err.println("socket " + s.getNumber() + ": "
                    + s.getDeviceDescriptor().getName());
            addPointingDevice(s);
        }
    }

    /**
     * @return keyboard modifier
     */
    protected int getKeyboardModifiers() {
        int mod = ViewEventHandler.NO_MODIFIER;
        boolean control = false, shift = false, alt = false, meta = false;

        for (int i = 0; i < this.keyboardDevices.length; i++) {
            try {
                if (this.keyboardDevices[i].getDevice().getSensor(
                        EventCodes.EV_KEY, EventCodes.KEY_LEFTCTRL).getValue() > 0) {
                    control = true;
                }
            } catch (NumberFormatException e) {
                // Nothing to do when a "n/a" value is given
            }
            try {
                if (this.keyboardDevices[i].getDevice().getSensor(
                        EventCodes.EV_KEY, EventCodes.KEY_RIGHTCTRL).getValue() > 0) {
                    control = true;
                }
            } catch (NumberFormatException e) {
                // Nothing to do when a "n/a" value is given
            }
            try {
                if (this.keyboardDevices[i].getDevice().getSensor(
                        EventCodes.EV_KEY, EventCodes.KEY_LEFTSHIFT).getValue() > 0) {
                    shift = true;
                }
            } catch (NumberFormatException e) {
                // Nothing to do when a "n/a" value is given
            }
            try {
                if (this.keyboardDevices[i].getDevice().getSensor(
                        EventCodes.EV_KEY, EventCodes.KEY_RIGHTSHIFT)
                        .getValue() > 0) {
                    shift = true;
                }
            } catch (NumberFormatException e) {
                // Nothing to do when a "n/a" value is given
            }
            try {
                if (this.keyboardDevices[i].getDevice().getSensor(
                        EventCodes.EV_KEY, EventCodes.KEY_LEFTALT).getValue() > 0) {
                    alt = true;
                }
            } catch (NumberFormatException e) {
                // Nothing to do when a "n/a" value is given
            }
            try {
                if (this.keyboardDevices[i].getDevice().getSensor(
                        EventCodes.EV_KEY, EventCodes.KEY_RIGHTALT).getValue() > 0) {
                    alt = true;
                }
            } catch (NumberFormatException e) {
                // Nothing to do when a "n/a" value is given
            }
            try {
                if (this.keyboardDevices[i].getDevice().getSensor(
                        EventCodes.EV_KEY, EventCodes.KEY_LEFTMETA).getValue() > 0) {
                    meta = true;
                }
            } catch (NumberFormatException e) {
                // Nothing to do when a "n/a" value is given
            }
            try {
                if (this.keyboardDevices[i].getDevice().getSensor(
                        EventCodes.EV_KEY, EventCodes.KEY_RIGHTMETA).getValue() > 0) {
                    meta = true;
                }
            } catch (NumberFormatException e) {
                // Nothing to do when a "n/a" value is given
            }
        }
        if (!control && shift && !alt && !meta)
            mod = ViewEventHandler.SHIFT_MOD;
        else if (control && !shift && !alt && !meta)
            mod = ViewEventHandler.CTRL_MOD;
        else if (control && shift && !alt && !meta)
            mod = ViewEventHandler.CTRL_SHIFT_MOD;
        else if (!control && !shift && !alt && meta)
            mod = ViewEventHandler.META_MOD;
        else if (!control && shift && !alt && meta)
            mod = ViewEventHandler.META_SHIFT_MOD;
        else if (!control && !shift && alt && !meta)
            mod = ViewEventHandler.ALT_MOD;
        else if (!control && shift && alt && !meta)
            mod = ViewEventHandler.ALT_SHIFT_MOD;
        return mod;
    }

    /**
     * Detect a left mouse button pression
     */
    private class Press1Listener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            if (mutationEvent.getNewValue().equals("1")) {
                MPDManager.this.evh.press1(MPDManager.this.viewPanel,
                        getKeyboardModifiers(), 0, 0,
                        ((Sensor) evt.getTarget()).getDevice().getSocket());
            }
        }
    }

    /**
     * Detect a left mouse button release
     */
    private class Release1Listener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            if (mutationEvent.getNewValue().equals("0")) {
                MPDManager.this.evh.release1(MPDManager.this.viewPanel,
                        getKeyboardModifiers(), 0, 0,
                        ((Sensor) evt.getTarget()).getDevice().getSocket());
            }
        }
    }

    /**
     * Detect a right mouse button pression
     */
    private class Press2Listener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            if (mutationEvent.getNewValue().equals("1"))
                MPDManager.this.evh.press2(MPDManager.this.viewPanel,
                        getKeyboardModifiers(), 0, 0,
                        ((Sensor) evt.getTarget()).getDevice().getSocket());
        }
    }

    /**
     * Detect a right mouse button release
     */
    private class Release2Listener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            if (mutationEvent.getNewValue().equals("0"))
                MPDManager.this.evh.release2(MPDManager.this.viewPanel,
                        getKeyboardModifiers(), 0, 0,
                        ((Sensor) evt.getTarget()).getDevice().getSocket());
        }
    }

    /**
     * Detect a middle mouse button pression
     */
    private class Press3Listener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            if (mutationEvent.getNewValue().equals("1"))
                MPDManager.this.evh.press3(MPDManager.this.viewPanel,
                        getKeyboardModifiers(), 0, 0,
                        ((Sensor) evt.getTarget()).getDevice().getSocket());
        }
    }

    /**
     * Detect a middle mouse button release
     */
    private class Release3Listener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            if (mutationEvent.getNewValue().equals("0"))
                MPDManager.this.evh.release3(MPDManager.this.viewPanel,
                        getKeyboardModifiers(), 0, 0,
                        ((Sensor) evt.getTarget()).getDevice().getSocket());
        }
    }

    /**
     * Detect an horizontal mouse movement
     */
    private class MouseMovedHorListener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            int newValue;
            try {
                newValue = new Integer(mutationEvent.getNewValue());
            } catch (NumberFormatException e1) {
                newValue = 0;
            }
            MPDManager.this.evh.mouseMoved(MPDManager.this.viewPanel, newValue,
                    0, ((Sensor) evt.getTarget()).getDevice().getSocket());
        }
    }

    /**
     * Detect a vertical mouse movement
     */
    private class MouseMovedVerListener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            int newValue;
            try {
                newValue = new Integer(mutationEvent.getNewValue());
            } catch (NumberFormatException e1) {
                newValue = 0;
            }
            MPDManager.this.evh.mouseMoved(MPDManager.this.viewPanel, 0,
                    newValue, ((Sensor) evt.getTarget()).getDevice()
                            .getSocket());
        }
    }

    /**
     * Detect a mouse wheel change
     */
    private class MouseWheelMovedListener implements EventListener {
        /**
         * @see org.w3c.dom.events.EventListener#handleEvent(org.w3c.dom.events.Event)
         */
        public void handleEvent(Event evt) {
            MutationEvent mutationEvent = (MutationEvent) evt;
            Sensor sensor = (Sensor) mutationEvent.getTarget();
            int dir;
            try {
                dir = new Integer(mutationEvent.getNewValue());
            } catch (NumberFormatException e1) {
                dir = 0;
            }
            short direction;
            if (dir != 0) {
                if (dir > 0)
                    direction = ViewEventHandler.WHEEL_UP;
                else
                    direction = ViewEventHandler.WHEEL_DOWN;
                MPDManager.this.evh.mouseWheelMoved(MPDManager.this.viewPanel,
                        direction, 0, 0, ((Sensor) evt.getTarget()).getDevice()
                                .getSocket());
            }
        }
    }

    /**
     * Add MPDAppEventHandler listeners to a given Socket
     * 
     * @param socket
     */
    public void addListener(Socket socket) {
        Device device = socket.getDevice();
        Press1Listener press1 = new Press1Listener();
        Press2Listener press2 = new Press2Listener();
        Press3Listener press3 = new Press3Listener();
        Release1Listener release1 = new Release1Listener();
        Release2Listener release2 = new Release2Listener();
        Release3Listener release3 = new Release3Listener();
        MouseMovedHorListener horMoved = new MouseMovedHorListener();
        MouseMovedVerListener verMoved = new MouseMovedVerListener();
        MouseWheelMovedListener wheel = new MouseWheelMovedListener();
        SocketEvents socketList = null;
        for (int i = 0; i < this.pointingDevices.length; i++) {
            if (socket.equals(this.pointingDevices[i].socket)) {
                socketList = this.pointingDevices[i];
                break;
            }
        }
        if (socketList != null) {
            socketList.addListener(press1, device.getSensor(EventCodes.EV_KEY,
                    EventCodes.BTN_LEFT));
            socketList.addListener(press2, device.getSensor(EventCodes.EV_KEY,
                    EventCodes.BTN_RIGHT));
            socketList.addListener(press3, device.getSensor(EventCodes.EV_KEY,
                    EventCodes.BTN_MIDDLE));
            socketList.addListener(release1, device.getSensor(EventCodes.EV_KEY,
                    EventCodes.BTN_LEFT));
            socketList.addListener(release2, device.getSensor(EventCodes.EV_KEY,
                    EventCodes.BTN_RIGHT));
            socketList.addListener(release3, device.getSensor(EventCodes.EV_KEY,
                    EventCodes.BTN_MIDDLE));
            socketList.addListener(horMoved, device.getSensor(EventCodes.EV_REL,
                    EventCodes.REL_X));
            socketList.addListener(verMoved, device.getSensor(EventCodes.EV_REL,
                    EventCodes.REL_Y));
            socketList.addListener(wheel, device.getSensor(EventCodes.EV_REL,
                    EventCodes.REL_WHEEL));
        }
        device.getSensor(EventCodes.EV_KEY, EventCodes.BTN_LEFT).addEventListener(
                Domino.SENSOR_CHANGED, press1, false);
        device.getSensor(EventCodes.EV_KEY, EventCodes.BTN_RIGHT).addEventListener(
                Domino.SENSOR_CHANGED, press2, false);
        device.getSensor(EventCodes.EV_KEY, EventCodes.BTN_MIDDLE).addEventListener(
                Domino.SENSOR_CHANGED, press3, false);
        device.getSensor(EventCodes.EV_KEY, EventCodes.BTN_LEFT).addEventListener(
                Domino.SENSOR_CHANGED, release1, false);
        device.getSensor(EventCodes.EV_KEY, EventCodes.BTN_RIGHT).addEventListener(
                Domino.SENSOR_CHANGED, release2, false);
        device.getSensor(EventCodes.EV_KEY, EventCodes.BTN_MIDDLE).addEventListener(
                Domino.SENSOR_CHANGED, release3, false);
        // TODO: add ABS devices
        device.getSensor(EventCodes.EV_REL, EventCodes.REL_X).addEventListener(
                Domino.SENSOR_CHANGED, horMoved, false);
        device.getSensor(EventCodes.EV_REL, EventCodes.REL_Y).addEventListener(
                Domino.SENSOR_CHANGED, verMoved, false);
        device.getSensor(EventCodes.EV_REL, EventCodes.REL_WHEEL).addEventListener(
                Domino.SENSOR_CHANGED, wheel, false);
    }

    /**
     * Remove all MPDAppEventHandler listeners to all socket's listeners
     * 
     * @param socket
     */
    public void removeListener(Socket socket) {
        System.err.println("removing " + socket.getDeviceDescriptor().getName()
                + " listeners");
        SocketEvents s;
        for (int i = 0; i < this.pointingDevices.length; i++) {
            if (socket.equals(this.pointingDevices[i].socket)) {
                s = this.pointingDevices[i];
                for (int j = 0; j < s.events.length; j++) {
                    s.events[j].sensor.removeEventListener(
                            Domino.SENSOR_CHANGED, s.events[j].event, false);
                }
            }
        }
    }

    /**
     * Create a new MPDManager instance and use it
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {
        class MPD implements MPDAppEventHandler {

            // Show some text and the modifier value when a mouse button is
            // pressed
            public void press1(ViewPanel v, int mod, int jpx, int jpy, Socket pd) {
                System.err.println("clic + " + mod);
            }

            public void release1(ViewPanel v, int mod, int jpx, int jpy,
                    Socket pd) {}

            public void click1(ViewPanel v, int mod, int jpx, int jpy,
                    int clickNumber, Socket pd) {}

            public void press2(ViewPanel v, int mod, int jpx, int jpy, Socket pd) {}

            // Clean stopping of the application when the second button of the
            // device named "Kensington Kensington USB/PS2 Orbit" is released
            public void release2(ViewPanel v, int mod, int jpx, int jpy,
                    Socket pd) {
                if (pd.getDeviceDescriptor().getName().equals(
                        "Kensington Kensington USB/PS2 Orbit")) {
                    System.err.println("Terminating");
                    MPDManager.terminate();
                }
            }

            public void click2(ViewPanel v, int mod, int jpx, int jpy,
                    int clickNumber, Socket pd) {}

            public void press3(ViewPanel v, int mod, int jpx, int jpy, Socket pd) {}

            public void release3(ViewPanel v, int mod, int jpx, int jpy,
                    Socket pd) {}

            public void click3(ViewPanel v, int mod, int jpx, int jpy,
                    int clickNumber, Socket pd) {}

            // Print relative movement of listened devices
            public void mouseMoved(ViewPanel v, int jpx, int jpy, Socket pd) {
                System.out.println(jpx + "," + jpy);
            }

            public void mouseDragged(ViewPanel v, int mod, int buttonNumber,
                    int jpx, int jpy, Socket pd) {}

            public void mouseWheelMoved(ViewPanel v, short wheelDirection,
                    int jpx, int jpy, Socket pd) {}
        }

        try {
            MPDManager mpd = new MPDManager(new MPD(), null);
            if (mpd.getAvailablePointingDevices() != null)
                for (int i = 0; i < mpd.getAvailablePointingDevices().length; i++) {
                    Socket s = mpd.getAvailablePointingDevices()[i];
                    // Listen to every pointing devices except the one named
                    // "Logitech USB-PS/2 Optical Mouse"
                    if (!s.getDeviceDescriptor().getName().equals(
                            "Logitech USB-PS/2 Optical Mouse")) {
                        mpd.addListener(s);
                    }
                }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

}
