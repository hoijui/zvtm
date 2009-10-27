/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;

import java.util.HashMap;

import java.net.URL;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

/** Interface implemented by handlers of the various resource types.
 *@author Emmanuel Pietriga
 */

public class PDFResourceHandler implements ResourceHandler {
    
    /* PDFFile cache management */
    
    static HashMap URL_2_PDF_FILE = new HashMap();
    
    public static void emptyCache(){
        URL_2_PDF_FILE.clear();
        System.gc();
    }
    
    public static int getCacheEntryCount(){
        return URL_2_PDF_FILE.size();
    }
    
    public static PDFPage getPage(URL pdfURL, int page){
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
            return (pf != null && page <= pf.getNumPages()) ? pf.getPage(page) : null;            
        }
    }
    
    /* PDF Resource Handler */

    public PDFResourceHandler(){}
        
    public PDFPageDescription createResourceDescription(long x, long y, long w, long h, String id, int zindex, Region region, 
                                                               String imagePath, boolean sensitivity, Color stroke, Object im){
        PDFPageDescription pdfd = new PDFPageDescription(id, x, y, zindex, w, h, imagePath, stroke, im, region);
        pdfd.setSensitive(sensitivity);
        region.addObject(pdfd);
        return pdfd;
    }
    
}
