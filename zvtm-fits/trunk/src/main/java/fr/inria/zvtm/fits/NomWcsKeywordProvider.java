package fr.inria.zvtm.fits;

import jsky.coords.WCSKeywordProvider;
import nom.tam.fits.Header;

public class NomWcsKeywordProvider implements WCSKeywordProvider{
    private final Header header;

    public NomWcsKeywordProvider(Header hdr){
        this.header = hdr;
    }

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

