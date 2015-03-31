/*  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2010-2015.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.fits;

import jsky.coords.WCSKeywordProvider;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;

public class NomWcsKeywordProvider implements WCSKeywordProvider{
    private final Header header;

    public NomWcsKeywordProvider(Header hdr){
        this.header = hdr;
        /*
        HeaderCard it = hdr.nextCard();
        while(it != null){
            showHeaderCard(it);
            it = hdr.nextCard();
        }
        */
    }
/*
    public void showHeaderCard(HeaderCard it){
        if(it != null){
            System.out.println(it.getKey() + ": "+it.getValue() + " //"+it.getComment() );
        }
    }
*/
    public boolean findKey(java.lang.String key){
        return header.findKey(key) != null;
    }

    public java.lang.String getStringValue(java.lang.String key){
        return header.getStringValue(key);
    }

    public java.lang.String getStringValue(java.lang.String key,
                                java.lang.String defaultValue){
        if(header.getStringValue(key) == null){
            return defaultValue;
        } else {
            return header.getStringValue(key);
        }
    }

    public double getDoubleValue(java.lang.String key){
        return header.getDoubleValue(key);
    }

    public double getDoubleValue(java.lang.String key,
                      double defaultValue){
        return header.getDoubleValue(key, defaultValue);
    }

    public float getFloatValue(java.lang.String key){
        return header.getFloatValue(key);
    }

    public float getFloatValue(java.lang.String key,
                    float defaultValue){
        return header.getFloatValue(key, defaultValue);
    }

    public int getIntValue(java.lang.String key){
        return header.getIntValue(key);
    }

    public int getIntValue(java.lang.String key,
                int defaultValue){
        return header.getIntValue(key, defaultValue);
    }
}

