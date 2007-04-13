
package net.claribole.eval.alphalens;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.xerox.VTM.svg.SVGReader;

public class EvalGraphPointing extends EvalPointing {

    static final String GRAPH_PATH = "images/graphs/jcl.svg";
    static final File GRAPH_FILE = new File(GRAPH_PATH);
    
    public EvalGraphPointing(short t){
	initGUI();
	this.technique = t;
	mViewName = TECHNIQUE_NAMES[this.technique];
	eh = new BaseEventHandlerPointing(this);
	mView.setEventHandler(eh);
	mView.setAntialiasing(false);
	initScene();
// 	mCamera.moveTo(0, 0);
// 	mCamera.setAltitude(100.0f);
	vsm.getGlobalView(mSpace.getCamera(0),100);
    }

    void initScene(){
	mView.setBackgroundColor(EvalPointing.BACKGROUND_COLOR);
	try {
	SVGReader.load(parse(GRAPH_FILE), vsm, mSpaceName, false, GRAPH_FILE.toURL().toString());
	}
	catch (MalformedURLException ex){ex.printStackTrace();}
    }

    static Document parse(File f){ 
	try {
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setValidating(false);
	    factory.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", new Boolean(false));
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    Document res = builder.parse(f);
	    return res;
	}
	catch (FactoryConfigurationError e){e.printStackTrace();return null;} 
	catch (ParserConfigurationException e){e.printStackTrace();return null;}
	catch (SAXException e){e.printStackTrace();return null;}
	catch (IOException e){e.printStackTrace();return null;}
    }
    
    public static void main(String[] args){
	try {
	    if (args.length >= 3){
		EvalGraphPointing.VIEW_MAX_W = Integer.parseInt(args[1]);
		EvalGraphPointing.VIEW_MAX_H = Integer.parseInt(args[2]);
	    }
	    new EvalGraphPointing(Short.parseShort(args[0]));
	}
	catch (Exception ex){
	    System.err.println("No cmd line parameter to indicate technique, defaulting to Fading Lens");
	    new EvalGraphPointing(EvalPointing.TECHNIQUE_FL);
	}
    }

}
