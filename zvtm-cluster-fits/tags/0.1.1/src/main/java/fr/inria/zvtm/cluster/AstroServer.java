package fr.inria.zvtm.cluster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

public class AstroServer extends NanoHTTPD {
    private final AstroRad delegate;

    public AstroServer(AstroRad delegate, int port) throws IOException{
        super(port);
        this.delegate = delegate;
    }

    public Response serve(String uri, String method, Properties header, Properties parms)
    {
        //crude query handling. we only serve one type
        //of request, so no need for sophistication here
        if(!uri.equals("/addImage")){
            return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "operation not supported\n");
        }

        if(!method.equals("POST")){
            return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "operation not supported\n");
        }

        String image = parms.getProperty("image");
        System.out.println("image: " + image);
        if(image == null){
            return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "missing parameter 'image'\n");
        }

        delegate.addImage(image);

        return new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, "addpage successful\n");
    }
}

