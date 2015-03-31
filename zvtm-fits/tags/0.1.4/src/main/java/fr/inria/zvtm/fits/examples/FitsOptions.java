/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits.examples;

import java.util.ArrayList;
import java.util.List;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.Option;


public class FitsOptions {

    @Option(name = "-fs", aliases = {"--fullscreen"}, usage = "full-screen")
    public boolean fullscreen = false;

    @Option(name = "-noaa", usage = "disable anti-aliasing")
    public boolean noaa = false;

    @Option(name = "-f", usage = "path to FITS file")
    public String file = null;

    @Option(name = "-url", usage= "FITS file URL")
    public String url = null;


    @Argument
    List<String> arguments = new ArrayList<String>();

}