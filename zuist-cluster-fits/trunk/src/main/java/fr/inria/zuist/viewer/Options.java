/*   Copyright (c) INRIA, 2010-2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.viewer;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class Options {

    @Option(name = "-bw", aliases = {"--block-width"}, usage = "clustered view block width")
    public int blockWidth = 640;

    @Option(name = "-bh", aliases = {"--block-height"}, usage = "clustered view block height")
    public int blockHeight = 480;

    @Option(name = "-r", aliases = {"--num-rows"}, usage = "number of rows in the clustered view")
    public int numRows = 1;

    @Option(name = "-c", aliases = {"--num-cols"}, usage = "number of columns in the clustered view")
    public int numCols = 1;

    @Option(name = "-mw", aliases = {"--mullion-width"}, usage = "mullions width")
    public int mullionWidth = 0;

    @Option(name = "-mh", aliases = {"--mullion-height"}, usage = "mullions height")
    public int mullionHeight = 0;

    @Option(name = "-f", aliases = {"--fullscreen"}, usage = "fullscreen mode")
    public boolean fullscreen = false;

    @Option(name = "-g", aliases = {"--opengl"}, usage = "use Java2D OpenGL rendering pipeline (Java 6+Linux/Windows), requires that -Dsun.java2d.opengl=true be set on cmd line")
    public boolean opengl = false;

    @Option(name = "-noaa", usage = "disable anti-aliasing")
    public boolean noaa = false;

    @Option(name = "-fits", aliases = {"--zuist-fits"}, usage = "background ZUIST Fits")
    public String xmlSceneFile = null;

    @Option(name = "-smooth", usage = "default to smooth transitions between levels when none specified")
    public boolean smooth = false;

    @Option(name = "-ref", usage = "reference to fits image")
    public String reference = null;

    @Option(name = "-user", usage = "HTTPS user name")
    public String httpUser = null;

    @Option(name = "-password", usage = "HTTPS password")
    public String httpPassword = null;

    @Option(name = "-hostmq", usage = "Host of the RabbitMQ")
    public static String HOST_NAME_MQ = "127.0.0.1";

    public static String PRODUCER_ROUTINGKEY_MQ = "java";
    public static String CONSUMER_ROUTINGKEY_MQ = "python";

    
    public static String VIRTUAL_HOST_MQ = "/";
    public static String USER_ID_MQ = "guest";
    public static String PASSWORD_MQ = "guest";
    public static String CONSUMER_TAG_MQ = "consumer";


    @Argument
    List<String> arguments = new ArrayList<String>();


}