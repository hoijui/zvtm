/*   Copyright (c) INRIA, 2014. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:  $
 */

package fr.inria.zvtm.cluster;


import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class WEOptions {

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

    @Option(name = "-tuio", usage = "TUIO listening port")
    public int tuioPort = 3333;

    @Option(name = "-sw", aliases = {"--scene-width"}, usage = "scene width in virtual space")
    public int sceneWidth = 12000;

    @Option(name = "-sh", aliases = {"--scene-height"}, usage = "scene height in virtual space")
    public int sceneHeight = 4500;

    @Option(name = "-ss", aliases = {"--scene-step"}, usage = "scene step in virtual space")
    public int sceneStep = 500;

    // @Option(name = "-left", usage = "leftmost value")
    // public float b_w = 0;

    // @Option(name = "-top", usage = "topmost value")
    // public float b_n = 1;

    // @Option(name = "-right", usage = "rightmost value")
    // public float b_e = 1;

    // @Option(name = "-bottom", usage = "bottommost value")
    // public float b_s = 0;

    @Argument
    List<String> arguments = new ArrayList<String>();

    public boolean standalone = true; //not a CLI option

}
