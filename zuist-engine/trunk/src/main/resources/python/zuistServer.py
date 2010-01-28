from mod_python import apache
#http://www.modpython.org/live/current/doc-html/

def getTile(req, test=""):    
    req.log_error('handler')
    req.content_type = 'image/png'
    req.send_http_header()
    req.sendfile("/tmp/image.png")
    return apache.OK
