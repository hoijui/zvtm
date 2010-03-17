package fr.inria.zuist.cluster;

import fr.inria.zvtm.cluster.GlyphCreation;

aspect Precedence {
    //This precedence declaration is made so that VirtualSpace.addGlyph
    //can set the target glyph to a replicated state if the VirtualSpace
    //is not owned by ZUIST, and to an unreplicated state otherwise.
    //See the glyphAdd after advice in VirtualSpaceIntroduction.
    declare precedence: VirtualSpaceIntroduction, GlyphCreation;
}

