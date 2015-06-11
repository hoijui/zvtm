/*   Copyright (c) INRIA, 2015. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: ViewerOptions.java 5388 2015-03-21 17:18:33Z epietrig $
 */

package fr.inria.zuist.viewer;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;

public class ViewerOptions {

    @Option(name = "-bd", aliases = {"--basic_debugger"}, usage = "launch basic debugger instead of the more elaborate scene viewer")
    public boolean basic_debugger = false;

    @Option(name = "-f", aliases = {"--scene"}, usage = "path to ZUIST scene")
    public String zuistScenePath = null;

    @Option(name = "-fs", aliases = {"--fullscreen"}, usage = "full-screen")
    public boolean fullscreen = false;

    @Option(name = "-ogl", aliases = {"--opengl"}, usage = "enable java2d OpenGL pipeline")
    public boolean opengl = false;

    @Option(name = "-noaa", usage = "disable anti-aliasing")
    public boolean noaa = false;

    @Option(name = "-user", usage = "HTTPS user name")
    public String httpUser = null;

    @Option(name = "-password", usage = "HTTPS password")
    public String httpPassword = null;

    @Option(name = "-smooth", usage = "smooth transitions regardless of settings in XML scene description")
    public boolean smooth = false;

    @Option(name = "-debug", usage = "print debug messages in console")
    public boolean debug = false;

    @Argument
    List<String> arguments = new ArrayList<String>();

    public boolean standalone = true; //not a CLI option

}
