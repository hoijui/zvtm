# AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
# Copyright (c) INRIA, 2010. All Rights Reserved
# Licensed under the GNU LGPL. For full terms see the file COPYING.

# $Id$

# Usage example: GET http://data.wild.lri.fr/py/zuistServer/getTile?z=9&col=322&row=224

from mod_python import apache
#http://www.modpython.org/live/current/doc-html/

def getTile(req, z=-1, col=-1, row=-1):
    try:
        req.log_error('handler')
        req.content_type = 'image/png'
        req.send_http_header()
        req.sendfile("/var/www/tiles/%s/%s/%s.png" % (z,col,row))
        return apache.OK
    except:
        return apache.HTTP_NOT_FOUND
