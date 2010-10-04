/*   AUTHOR :           Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2009-2010. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zuist.engine;

import java.awt.Color;
import java.awt.RenderingHints;

import java.util.HashMap;

import java.net.URL;
import java.io.IOException;
import org.icepdf.core.pobjects.Document;

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Cache;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

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
    /** Detail factor */
    public static final String _df = "df=";
    
    /** Memory Cache*/
    static SelfPopulatingCache documentCache = new SelfPopulatingCache((new CacheManager()).getCache("zuistPDFCache"), new CachedDocumentFactory());
    
    /** Reset memory cache*/
    public static void resetCache(){
        documentCache.removeAll();
    }
    
    /** Get a PDF Document (IcePDF Document instance) given its URL.
     * The document gets stored in a cache the first time it is fetched, for faster access later.
     */
    public static Document getDocument(URL pdfURL){
        return (Document)documentCache.get(pdfURL).getObjectValue();
    }
    
    /* PDF Resource Handler */

    public PDFResourceHandler(){}
    
    public PDFPageDescription createResourceDescription(double x, double y, String id, int zindex, Region region, 
                                                        URL resourceURL, boolean sensitivity, Color stroke, String params){
        Object im = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
        int page = 1;
        float scale = 1f;
        float detail = 1f;
        if (params != null){
            String[] paramTokens = params.split(SceneManager.PARAM_SEPARATOR);
            for (int i=0;i<paramTokens.length;i++) {
                if (paramTokens[i].startsWith(PDFResourceHandler._pg)){
                    page = Integer.parseInt(paramTokens[i].substring(3));
                }
                else if (paramTokens[i].startsWith(PDFResourceHandler._sc)){
                    scale = Float.parseFloat(paramTokens[i].substring(3));
                }
                else if (paramTokens[i].startsWith(PDFResourceHandler._df)){
                    detail = Float.parseFloat(paramTokens[i].substring(3));
                }
                else if (paramTokens[i].startsWith(SceneManager._im)){
                    im = SceneManager.parseInterpolation(params.substring(3));
                }
                else {
                    System.err.println("Uknown type of resource parameter: "+paramTokens[i]);
                }
            }            
        }
        PDFPageDescription pdfd = new PDFPageDescription(id, x, y, zindex, detail, scale, resourceURL, page, stroke, im, region);
        pdfd.setSensitive(sensitivity);
        region.addObject(pdfd);
        return pdfd;
    }
    
}

class CachedDocumentFactory implements CacheEntryFactory {
    
    public Object createEntry(Object key){
        URL pdfURL = (URL)key;
        Document document = new Document();
        try {
            document.setInputStream(pdfURL.openStream(), pdfURL.toString());
        } catch (PDFException ex) {
            System.out.println("Error parsing PDF document " + ex);
        } catch (PDFSecurityException ex) {
            System.out.println("Error encryption not supported " + ex);
        } catch (IOException ex) {
            System.out.println("Error handling PDF document " + ex);
        }
        return document;
    }
    
}
