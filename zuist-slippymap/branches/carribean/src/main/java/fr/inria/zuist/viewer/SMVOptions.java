/*   Copyright (c) INRIA, 2015. All Rights Reserved
 * $Id: SMVOptions.java 5466 2015-03-31 21:19:25Z epietrig $
 */

package fr.inria.zuist.viewer;


import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class SMVOptions {

    @Option(name = "-bw", aliases = {"--block-width"}, usage = "clustered view block width")
    public int blockWidth = 400;

    @Option(name = "-bh", aliases = {"--block-height"}, usage = "clustered view block height")
    public int blockHeight = 300;

    @Option(name = "-r", aliases = {"--num-rows"}, usage = "number of rows in the clustered view")
    public int numRows = 2;

    @Option(name = "-c", aliases = {"--num-cols"}, usage = "number of columns in the clustered view")
    public int numCols = 2;

    @Option(name = "-mw", aliases = {"--mullion-width"}, usage = "mullions width")
    public int mullionWidth = 0;

    @Option(name = "-mh", aliases = {"--mullion-height"}, usage = "mullions height")
    public int mullionHeight = 0;

    @Option(name = "-f", aliases = {"--fullscreen"}, usage = "full-screen")
    public boolean fullscreen = false;

    @Option(name = "-g", aliases = {"--opengl"}, usage = "enable java2d OpenGL pipeline")
    public boolean opengl = false;

    @Option(name = "-noaa", usage = "disable anti-aliasing")
    public boolean noaa = false;

    @Option(name = "-map", aliases = {"--zuist-map"}, usage = "background ZUIST map")
    public String path_to_zuist_map = null;

    @Option(name = "-shp", aliases = {"--shapefile"}, usage = "ESRI shapefile")
    public String path_to_shapefile = null;

    @Option(name = "-user", usage = "HTTPS user name")
    public String httpUser = null;

    @Option(name = "-password", usage = "HTTPS password")
    public String httpPassword = null;

    @Option(name = "-ts", aliases = {"--tile-size"}, usage = "slippy map tile size")
    public int tileSize = 256;

    @Argument
    List<String> arguments = new ArrayList<String>();

    public boolean standalone = true; //not a CLI option

}
