/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;
import java.awt.RenderingHints;

import java.util.HashMap;

import java.net.URL;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/** ResourceHandler implementation for PDF documents.
 *@author Emmanuel Pietriga
 */

public class PDFResourceHandler implements ResourceHandler {
    
	/** Resource of type PDF document. */
    public static final String RESOURCE_TYPE_PDF = "pdf";
    /** Custom parameter names for this type of resource */
    /** Page number */
    public static final String _pg = "pg=";
    /** Scale factor */
    public static final String _sc = "sc=";
    
    /* PDFFile cache management */
    static HashMap URL_2_PDF_FILE = new HashMap();
    
    public static void emptyCache(){
        synchronized(URL_2_PDF_FILE){
            URL_2_PDF_FILE.clear();
        }
        System.gc();
    }
    
    public static int getCacheEntryCount(){
        return URL_2_PDF_FILE.size();
    }
    
    public static PDFFile getPDF(URL pdfURL){
        synchronized(URL_2_PDF_FILE){
            PDFFile pf = null;
            if (URL_2_PDF_FILE.containsKey(pdfURL)){
                pf = (PDFFile)URL_2_PDF_FILE.get(pdfURL);
            }
            else {
                try {
                    RandomAccessFile raf = new RandomAccessFile(new File(pdfURL.toURI()), "r");
                    FileChannel channel = raf.getChannel();
                    ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
                    pf = new PDFFile(buf);
                    URL_2_PDF_FILE.put(pdfURL, pf);
                }
                catch (Exception ex){System.err.println("Error reading PDF file at "+pdfURL.toString());}
            }
            return pf;
        }
    }
    
    public static PDFPage getPage(URL pdfURL, int page) {
        synchronized(URL_2_PDF_FILE){
            PDFFile pf = null;
			int n;
            if (URL_2_PDF_FILE.containsKey(pdfURL)){
                pf = (PDFFile)URL_2_PDF_FILE.get(pdfURL);
            }
            else {
                try {    
        			InputStream is = pdfURL.openStream();					
					byte[] buffer = new byte[4096];
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					while ((n = is.read(buffer)) != -1) {
						baos.write(buffer, 0, n);
					}
					is.close();
					
        			ByteBuffer buf = ByteBuffer.wrap(baos.toByteArray());
        			pf = new PDFFile(buf);
                    URL_2_PDF_FILE.put(pdfURL, pf);
                }
                catch (Exception ex){System.err.println("Error reading PDF file at "+pdfURL.toString());}
            }
            return (pf != null && page <= pf.getNumPages()) ? pf.getPage(page) : null;
        }
    }
    
    /* PDF Resource Handler */

    public PDFResourceHandler(){}
        
    public PDFPageDescription createResourceDescription(long x, long y, String id, int zindex, Region region, 
                                                        URL resourceURL, boolean sensitivity, Color stroke, String params){
        Object im = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        int page = 1;
        double scale = 1.0;
        if (params != null){
            String[] paramTokens = params.split(SceneManager.PARAM_SEPARATOR);
            for (int i=0;i<paramTokens.length;i++) {
                if (paramTokens[i].startsWith(PDFResourceHandler._pg)){
                    page = Integer.parseInt(paramTokens[i].substring(3));
					System.out.println(page);
                }
                else if (paramTokens[i].startsWith(PDFResourceHandler._sc)){
                    scale = Double.parseDouble(paramTokens[i].substring(3));
					System.out.println(scale);
                }
                else if (paramTokens[i].startsWith(SceneManager._im)){
                    im = SceneManager.parseInterpolation(params.substring(3));
					System.out.println(im);
                }
                else {
                    System.err.println("Uknown type of resource parameter: "+paramTokens[i]);
                }
            }            
        }
        PDFPageDescription pdfd = new PDFPageDescription(id, x, y, zindex, scale, resourceURL, page, stroke, im, region);
        pdfd.setSensitive(sensitivity);
        region.addObject(pdfd);
        return pdfd;
    }
    
}
