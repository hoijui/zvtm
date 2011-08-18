package fr.inria.zvtm.cluster;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import jsky.science.Coordinates;

class CatQuery {

    public static void main(String[] args) throws Exception{
        //attempt to retrieve objects from the Simbad catalog
        List<AstroObject> objs = makeSimbadCoordQuery(1, 4, 12);
        for(AstroObject obj: objs){
            System.err.println(obj);
        }
    }

    static List<AstroObject> makeSimbadCoordQuery(double ra, double dec, int radmin) throws IOException{
        List<AstroObject> retval = new ArrayList<AstroObject>();
        URL queryUrl = makeSimbadCoordQueryUrl(ra, dec, radmin);
        return parseObjectList(readLines(queryUrl));
    }

    private static URL makeSimbadCoordQueryUrl(double ra, double dec, 
            int radMin){
        try{
            // Query script example:
            // format object "%IDLIST(1)|%COO(d;A)|%COO(d;D)"
            // query coo 12 30 +10 20 radius=6m

            Coordinates coords = new Coordinates(ra, dec);

            String prefix = "http://simbad.u-strasbg.fr/simbad/sim-script?script=";
            String argStr = String.format("output console=off script=off\nformat object \"%%IDLIST(1)|%%COO(d;A)|%%COO(d;D)\"\nquery coo %s %s radius=%dm", coords.raToString(), coords.decToString(), radMin);

            return new URL(prefix + URLEncoder.encode(argStr, "UTF-8"));
        } catch (MalformedURLException ex){
            //we are supposed to create well-formed URLs here...
            throw new Error(ex);
        } catch (UnsupportedEncodingException eex){
            throw new Error(eex);
        }
    }

    //A better version should deal with http errors
    private static List<String> readLines(URL url) throws IOException{
        URLConnection uc = url.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                    uc.getInputStream()));
        List<String> result = new ArrayList<String>();
        String toAppend;
        while((toAppend = in.readLine()) != null){
            result.add(toAppend);
        }
        in.close();
        return result;
    }

    private static List<AstroObject> parseObjectList(List<String> strList){
        ArrayList<AstroObject> retval = new ArrayList<AstroObject>();
        for(String objStr: strList){
            AstroObject candidate = AstroObject.fromSimbadRow(objStr);
            if(candidate != null){
                retval.add(candidate);
            }
        }
        return retval;
    }
}

