/*
 *   Copyright (c) INRIA, 2010-2013. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Precedence.aj 5175 2014-06-27 13:46:58Z epietrig $
 */

package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.GlyphCreation;

aspect Precedence {
    //This precedence declaration is made so that VirtualSpace.addGlyph
    //can set the target glyph to a replicated state if the VirtualSpace
    //is not owned by ZUIST, and to an unreplicated state otherwise.
    //See the glyphAdd after advice in VirtualSpaceIntroduction.
    declare precedence: VirtualSpaceIntroduction, GlyphCreation;
}

