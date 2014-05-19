

package fr.inria.zvtm.cluster.andes;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;


public class FitsOptions {

    @Option(name = "-bw", aliases = {"--block-width"}, usage = "clustered view block width")
    public int blockWidth = 400;

    @Option(name = "-bh", aliases = {"--block-height"}, usage = "clustered view block height")
    public int blockHeight = 300;

    @Option(name = "-r", aliases = {"--num-rows"}, usage = "number of rows in the clustered view")
    public int numRows = 2;

    @Option(name = "-c", aliases = {"--num-cols"}, usage = "number of columns in the clustered view")
    public int numCols = 2;

    @Option(name = "-f", aliases = {"--fullscreen"}, usage = "full-screen")
    public boolean fullscreen = false;

    @Option(name = "-g", aliases = {"--opengl"}, usage = "enable java2d OpenGL pipeline")
    public boolean opengl = false;

    @Option(name = "-noaa", usage = "disable anti-aliasing")
    public boolean noaa = false;

    @Option(name = "-file", usage = "file of FITS")
    public String file = null;

    @Option(name = "-url", usage= "url of FITS")
    public String url = null;


    @Argument
    List<String> arguments = new ArrayList<String>();

}