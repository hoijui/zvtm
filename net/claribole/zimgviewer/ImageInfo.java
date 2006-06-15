/*   FILE: ImageInfo.java
 *   DATE OF CREATION:   Thu May 29 16:06:36 2003
 *   AUTHOR :            Emmanuel Pietriga (emmanuel@w3.org)
 *   MODIF:              Thu May 29 17:19:02 2003 by Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net)
 *   Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 */ 

package net.claribole.zimgviewer;

import java.io.File;

import com.xerox.VTM.glyphs.VImage;

public class ImageInfo {

    VImage zvtmImage;
    File imageFile;

    ImageInfo(VImage im,File f){
	zvtmImage=im;
	imageFile=f;
    }

    VImage getImage(){
	return zvtmImage;
    }

    String getFileName(){
	return imageFile.getName();
    }

}
