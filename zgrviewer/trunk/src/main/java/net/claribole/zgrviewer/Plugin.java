/*   FILE: Plugin.java
 *   DATE OF CREATION:  Thu May 26 16:20:27 2005
 *   Copyright (c) INRIA, 2004-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package net.claribole.zgrviewer;

import java.util.Hashtable;
import javax.swing.JFrame;

/**
 * An interface for ZGRViewer plugins (such as the one based on ZVTM-MPD). See <a href="http://zvtm.sourceforge.net/doc/tutorials/zgrvplugins/index.html">http://zvtm.sourceforge.net/doc/tutorials/zgrvplugins/index.html</a> for more details.
 **/
public interface Plugin {

    /**
     * Set the ZGRViewer instance this plug-in is associated with
     */
    public void setApplication(ZGRViewer app);

    /**
     * Called by ZGRViewer prior to exit (put clean up actions (if any) in here)
     */
    public void terminate();

    /**
     * Called by ZGRViewer at init time when loading preferences from zgrviewer.cfg
     * @param settings contains preferences relevant to this plug-in, as they were exported by method savePreferences
     */
    public void loadPreferences(Hashtable settings);

    /**
     * Called by ZGRViewer when saving preferences to zgrviewer.cfg
     * @return preferences relevant to this plug-in as a hashtable (can be null)
     */
    public Hashtable savePreferences();

    /**
     * Display a window for configuring plugin settings
     **/
    public void showSettings();

    /**
     * Gets author information about this plug-in - return an empty String if none
     **/
    public String getAuthor();
    
    /**
     * Gets information about this plug-in - return an empty String if none
     **/
    public String getName();

    /**
     * Gets version information about this plug-in - return an empty String if none
     **/
    public String getVersion();

    /**
     * Gets a URL pointing to more information about this plug-in (e.g. Web site) - can be null
     **/
    public java.net.URL getURL();

	/** Event triggered when the graph's logical structure has changed. */
	public static final short NOTIFY_PLUGIN_LOGICAL_STRUCTURE_CHANGED = 0;
	/** Event triggered when ZGRViewer's UI has been instantiated and has finished initializing. */
	public static final short NOTIFY_PLUGIN_GUI_INITIALIZED = 1;

	/** Event notification
	 *@param event one of NOTIFY_* events
	 */
	public void eventOccured(short event);

}

