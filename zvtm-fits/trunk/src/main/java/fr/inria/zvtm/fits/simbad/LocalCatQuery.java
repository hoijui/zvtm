package fr.inria.zvtm.fits.simbad;


import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

import java.net.URL;
import java.io.Reader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import org.apache.commons.lang3.StringEscapeUtils;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

public class LocalCatQuery {

	//static String prefix = "http://fits-catalog.inria.cl:9000/catalog/query";
    static String prefix = "http://localhost:9000/catalog/query";

	public static List<AstroObject> makeCoordQuery(double ra, double dec, double arcmin) throws IOException, JSONException{
		List<AstroObject> retval = new ArrayList<AstroObject>();

		JSONArray arr = readArrayJson(prefix + "?ra="+ra+"&dec="+dec+"&arcmin="+arcmin);
		for (int i = 0; i < arr.length(); i++){
			JSONObject obj = arr.getJSONObject(i);
			//System.out.println(obj);
			AstroObject candidate = AstroObject.fromJSON(obj);
			if(candidate != null)
				retval.add(candidate);
		}

		return retval;
	}

	public static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return StringEscapeUtils.unescapeHtml4(sb.toString());
    }
	
	static final Charset CHARSET = Charset.forName("ISO-8859-1"); 

    public static JSONArray readArrayJson(String url) throws IOException, JSONException {
        System.out.println("Request server - readArrayJson");
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, CHARSET));
            String jsonText = readAll(rd);
            JSONArray arr = new JSONArray(jsonText);
            System.out.println("JSONArray size:" + arr.length());
            return arr;
        } finally {
            is.close();
        }
    }


}