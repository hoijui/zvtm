/*   FILE: DOTManager.java
 *   DATE OF CREATION:   Thu Jan 09 14:14:35 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004-2005. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: DOTManager.java,v 1.10 2005/10/12 13:26:31 epietrig Exp $
 */ 

package net.claribole.zgrviewer;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.StringTokenizer;
import javax.swing.JOptionPane;

import net.claribole.zgrviewer.dot.DOTLexer;
import net.claribole.zgrviewer.dot.DOTParser;
import net.claribole.zgrviewer.dot.DOTTreeParser;
import net.claribole.zgrviewer.dot.Graph;
import net.claribole.zgrviewer.dot.ZgrReader;

import org.w3c.dom.Document;

import antlr.CommonAST;

import com.xerox.VTM.svg.SVGReader;

class DOTManager {

    ZGRViewer application;

    File dotF;
    File svgF;
    
    Graph graph;

    DOTManager(ZGRViewer app){
	application=app;
    }

    void load(File f,String prg, boolean parser){//prg="dot" or "neato"
	ProgPanel pp=new ProgPanel("Resetting...","Loading DOT File");
	try {
	    svgF=Utils.createTempFile(ConfigManager.m_TmpDir.toString(),"zgrv",(parser?".dot":".svg"));
	    dotF=f;
	    callGraphViz(pp,prg, parser);
	    pp.setLabel("Deleting Temp File...");
	    pp.setPBValue(100);
	    pp.destroy();
	}
	catch (Exception ex){
	    pp.destroy();
	    javax.swing.JOptionPane.showMessageDialog(ZGRViewer.mainView.getFrame(),Messages.loadError+f.toString());
	}
    }
    
    private void callGraphViz(ProgPanel pp, String prg, boolean parser)
            throws Exception {
        try {
            pp.setLabel("Preparing " + (parser ? "Augmented DOT" : "SVG")
                    + " Temp File");
            pp.setPBValue(10);
            if (parser) {
                if (!generateDOTFile(dotF.getAbsolutePath(), svgF
                        .getAbsolutePath(), pp, prg)) {
                    deleteTempFiles();
                    return;
                }
                displayDOT(pp);
                if (ConfigManager.DELETE_TEMP_FILES) {
                    deleteTempFiles();
                }
            } else {
                if (!generateSVGFile(dotF.getAbsolutePath(), svgF
                        .getAbsolutePath(), pp, prg)) {
                    deleteTempFiles();
                    return;
                }
                displaySVG(pp);
                if (ConfigManager.DELETE_TEMP_FILES) {
                    deleteTempFiles();
                }
            }
        } catch (Exception e) {
            System.err.println("Exception generating graph: " + e.getMessage()
                    + "\n");
            e.printStackTrace();
            throw new Exception();
        }
    }

    void deleteTempFiles(){
	if (svgF!=null){svgF.delete();}	
    }
    
    private boolean generateDOTFile(String dotFilePath,String tmpFilePath,ProgPanel pp,String prg){
        String[] cmdArray = new String[(application.cfgMngr.FORCE_SILENT) ? 7 : 6];
    cmdArray[0] = (prg.equals("dot")) ? ConfigManager.m_DotPath.toString() : ConfigManager.m_NeatoPath.toString();
    cmdArray[1] = "-Tdot";

    if (application.cfgMngr.FORCE_SILENT){
        cmdArray[2] = "-q";
        cmdArray[3] = checkOptions(ConfigManager.CMD_LINE_OPTS);
        cmdArray[4] = "-o";
        cmdArray[5] = tmpFilePath;
        cmdArray[6] = dotFilePath;
    }
    else {
        cmdArray[2] = checkOptions(ConfigManager.CMD_LINE_OPTS);
        cmdArray[3] = "-o";
        cmdArray[4] = tmpFilePath;
        cmdArray[5] = dotFilePath;
    }
        Runtime rt=Runtime.getRuntime();
    pp.setLabel("Computing Graph Layout (GraphViz)...");
    pp.setPBValue(40);
        try {
            //Process p = rt.exec(cmdArray, environment);
            Process p = rt.exec(cmdArray);
            p.waitFor();
        }
    catch (Exception e) {System.err.println("Error: generating OutputFile.\n");return false;}
        return true;
    }

    /**
     * Invokes the GraphViz program to create a graph image from the
     * the given DOT data file
     *@param dotFilePath the name of the DOT data file
     *@param svgFilePath the name of the output data file
     *@param prg program to use (dot or neato)
     
     *@return true if success; false if any failure occurs
     */
    private boolean generateSVGFile(String dotFilePath,String svgFilePath,ProgPanel pp,String prg){
        String[] cmdArray = new String[(application.cfgMngr.FORCE_SILENT) ? 7 : 6];
	cmdArray[0] = (prg.equals("dot")) ? ConfigManager.m_DotPath.toString() : ConfigManager.m_NeatoPath.toString();
	cmdArray[1] = "-Tsvg";

	if (application.cfgMngr.FORCE_SILENT){
	    cmdArray[2] = "-q";
	    cmdArray[3] = checkOptions(ConfigManager.CMD_LINE_OPTS);
	    cmdArray[4] = "-o";
	    cmdArray[5] = svgFilePath;
	    cmdArray[6] = dotFilePath;
	}
	else {
	    cmdArray[2] = checkOptions(ConfigManager.CMD_LINE_OPTS);
	    cmdArray[3] = "-o";
	    cmdArray[4] = svgFilePath;
	    cmdArray[5] = dotFilePath;
	}
        Runtime rt=Runtime.getRuntime();
	pp.setLabel("Computing Graph Layout (GraphViz)...");
	pp.setPBValue(40);
        try {
            //Process p = rt.exec(cmdArray, environment);
            Process p = rt.exec(cmdArray);
            p.waitFor();
        }
	catch (Exception e) {System.err.println("Error: generating OutputFile.\n");return false;}
        return true;
    }

    /*load a file using a program other than dot/neato for computing the layout (e.g. twopi)*/
    void loadCustom(String srcFile, String cmdLineExpr){
	ProgPanel pp = new ProgPanel("Resetting...","Loading File");
	try {
	    svgF = Utils.createTempFile(ConfigManager.m_TmpDir.toString(), "zgrv", ".svg");
	    if (!generateSVGFileFOP(srcFile, svgF.getAbsolutePath(), pp, cmdLineExpr)){
		deleteTempFiles();
		return;
	    }
	    displaySVG(pp);
	    if (ConfigManager.DELETE_TEMP_FILES) {
		deleteTempFiles();
	    }
	    pp.setLabel("Deleting Temp File...");
	    pp.setPBValue(100);
	    pp.destroy();
	}
	catch (Exception ex){
	    pp.destroy();
	    javax.swing.JOptionPane.showMessageDialog(ZGRViewer.mainView.getFrame(),Messages.loadError+srcFile);
	}
    }
    

    /**
     * Invokes a program to create an SVG image from a source file using a program other than dot/neato for computing the layout (e.g. twopi)
     *@return true if success; false if any failure occurs
     */
    private boolean generateSVGFileFOP(String srcFilePath, String svgFilePath, ProgPanel pp, String commandLine){
	StringTokenizer st = new StringTokenizer(commandLine, " ");
	int nbTokens = st.countTokens();
	String[] cmdArray = new String[nbTokens];
	for (int i=0;i<nbTokens;i++){
	    cmdArray[i] = st.nextToken();
	    if (cmdArray[i].equals("%s")){cmdArray[i] = srcFilePath;}
	    else if (cmdArray[i].equals("%t")){cmdArray[i] = svgFilePath;}
	}
	Runtime rt=Runtime.getRuntime();
 	pp.setLabel("Computing layout...");
 	pp.setPBValue(40);
	try {
	    Process p = rt.exec(cmdArray);
	    p.waitFor();
	}
 	catch (Exception e){
	    JOptionPane.showMessageDialog(application.mainView.getFrame(), Messages.customCallExprError2 + Utils.join(cmdArray, " "),
					  "Command line call error", JOptionPane.ERROR_MESSAGE);
	    System.err.println("Error generating output SVG file.\n");
	    return false;
	}
        return true;
    }

    void displaySVG(ProgPanel pp){
	pp.setLabel("Parsing SVG...");
	pp.setPBValue(60);
	Document svgDoc=Utils.parse(svgF,false);
	pp.setLabel("Displaying...");
	pp.setPBValue(80);
	SVGReader.load(svgDoc,ZGRViewer.vsm,ZGRViewer.mainSpace,true);
    }

    void displayDOT(ProgPanel pp) throws Exception {
        pp.setLabel("Parsing Augmented DOT...");
        pp.setPBValue(60);
        DataInputStream graphInput = new DataInputStream(new FileInputStream(
                svgF));
        DOTLexer graphLexer = new DOTLexer(graphInput);
        DOTParser graphParser = new DOTParser(graphLexer);

        graphParser.graph();

        CommonAST ast = (CommonAST) graphParser.getAST();

        DOTTreeParser graphWalker = new DOTTreeParser();
        graph = graphWalker.graph(ast);

        pp.setLabel("Displaying...");
        pp.setPBValue(80);
        ZgrReader.load(graph, ZGRViewer.vsm, ZGRViewer.mainSpace, true);
    }


    /*checks that the command line options do not contain a -Txxx */
    static String checkOptions(String options){
	int i = options.indexOf("-T");
	if (i!=-1){
	    String res=options.substring(0,i);
	    while (i<options.length() && options.charAt(i)!=' '){i++;}
	    res+=options.substring(i);
	    return res;
	}
	else return options;
    }

}
