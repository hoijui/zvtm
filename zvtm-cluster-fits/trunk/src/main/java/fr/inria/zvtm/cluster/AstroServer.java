package fr.inria.zvtm.cluster;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

class AstroServer extends NanoHTTPD {
    private final AstroRad delegate;

    public AstroServer(AstroRad delegate, int port) throws IOException{
        super(port);
        this.delegate = delegate;
    }

    public Response serve(String uri, String method, Properties header, Properties parms)
    {
        //crude query handling. we only serve one type
        //of request, so no need for sophistication here
        System.out.println("uri: " + uri);
        System.out.println("method: " + method);
        System.out.println("header: " + header);
        System.out.println("parms: " + parms);

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

        URL imgUrl;
        try{
            imgUrl = new URL(image);
        } catch (MalformedURLException ex){
            return new NanoHTTPD.Response(HTTP_BADREQUEST, MIME_PLAINTEXT, "image URL error (required image: " + image + ")\n");
        } 

        delegate.addImage(imgUrl);

        return new NanoHTTPD.Response(HTTP_OK, MIME_PLAINTEXT, "addpage successful\n");
    }
}

