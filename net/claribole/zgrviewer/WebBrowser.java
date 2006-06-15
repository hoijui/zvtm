/*   FILE: WebBrowser.java
 *   DATE OF CREATION:   Wed Dec 03 09:11:41 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@claribole.net)
 *   MODIF:              Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Copyright (c) INRIA, 2004. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *   $Id: WebBrowser.java,v 1.2 2004/12/17 15:48:12 epietrig Exp $
 */ 

package net.claribole.zgrviewer;

import java.io.IOException;

class WebBrowser {

    WebBrowser(){}

    public void show(String url){
	if (url!=null && url.length()>0){   //perhaps we should try to convert it to a URL, or make the param a URL
	    String command=null;            //instead of a string
	    if (ConfigManager.autoDetectBrowser){  //try to autodetect browser
		try {
		    if (Utils.osIsWindows()){//running under Win32
			command="rundll32 url.dll,FileProtocolHandler "+url;
			Process proc=Runtime.getRuntime().exec(command);
		    }
		    else if (Utils.osIsMacOS()){
			command = "open "+url;
			Process proc=Runtime.getRuntime().exec(command);
		    }
		    else {//UNIX and perhaps Linux - not tested yet  (no support for Mac right now)
			command="mozilla -remote openURL("+url+")";
			Process proc=Runtime.getRuntime().exec(command);
			int exitCode;
			try {
			    if ((exitCode=proc.waitFor())!=0){
				command="mozilla "+url;
				proc=Runtime.getRuntime().exec(command);
			    }
			}
			catch (InterruptedException ex1){javax.swing.JOptionPane.showMessageDialog(ZGRViewer.vsm.getActiveView().getFrame(),"Browser invokation failed "+command+"\n"+ex1);}
		    }
		    
		}
		catch (IOException ex2){javax.swing.JOptionPane.showMessageDialog(ZGRViewer.vsm.getActiveView().getFrame(),"Browser invokation failed "+command+"\n"+ex2);}
	    }
	    else {
		try {
		    command=ConfigManager.browserPath+" "+ConfigManager.browserOptions+" "+url;
		    Process proc=Runtime.getRuntime().exec(command);
		}
		catch (Exception ex3){javax.swing.JOptionPane.showMessageDialog(ZGRViewer.vsm.getActiveView().getFrame(),"Browser invokation failed "+command+"\n"+ex3);}
	    }
	}
    }

}
