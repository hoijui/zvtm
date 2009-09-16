
/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2009.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 */ 
package fr.inria.zvtm.clustering;

import java.net.URL;

import fr.inria.zvtm.engine.LongPoint;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.ZPDFPageImg;

import java.io.IOException;

/* Delta for ClusteredImage creation */
class PdfPageCreateDelta implements CreateDelta {
	private final ObjId id; //should be moved to Glyph attr??
	private final LongPoint center; //should be moved to Glyph attr??
	private final String filePath; 
	private final int pageNumber;

	PdfPageCreateDelta(ObjId id, LongPoint center,
			String filePath, int pageNumber){
		this.id = id;
		this.center = center;
		this.filePath = filePath;
		this.pageNumber = pageNumber;
	}

	public void apply(SlaveUpdater slaveUpdater){
		slaveUpdater.addGlyph(id, create());
	}

	public Glyph create(){
		try{
			ZPDFPageImg glyph = new ZPDFPageImg(center.x, center.y,
				0, filePath, pageNumber);
			return glyph;
		} catch (IOException ex){
			//Could do better with the error handling, but 'tis a prototype
			throw new Error("Could not create PDF page");
		}
	}

	@Override public String toString(){
		return "PdfPageCreateDelta, Glyph id " + id
			+ ", file path " + filePath;
	}

}

