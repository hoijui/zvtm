package fr.inria.zvtm.cluster;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;

public aspect VirtualSpaceIntroduction {
	//introduce parent space attribute to Glyph
	private VirtualSpace Glyph.parentSpace = null;

	VirtualSpace Glyph.getParentSpace(){ return parentSpace; }

	void Glyph.setParentSpace(VirtualSpace parentSpace){ 
		this.parentSpace = parentSpace; 
	}

    //Introduce isMirrired attribute in VirtualSpace.
    //By default, virtual spaces in ZVTM-cluster applications
    //are mirrored, but it is possible to selectively disable this
    //feature, for instance when the VirtualSpace should be controlled
    //by the ZUIST library
    private boolean VirtualSpace.mirrored = true;

    boolean VirtualSpace.isMirrored(){
        return mirrored;
    }

    public void VirtualSpace.setMirrored(boolean mirrored){
        this.mirrored = mirrored;
    }
}

