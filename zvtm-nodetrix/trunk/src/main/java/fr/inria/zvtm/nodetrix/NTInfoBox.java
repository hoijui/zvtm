package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

/**Infobox that shows additional information about a matrix node.
 * The information is stored simply in a list containing a configurable 
 * amount of fields for each entry. The contained data is shown as a table.
 * 
 * The box appears when it is clicked on a node. It can be dragged and by clicking
 * directly on the box, in disappears. A mouse-over causes the corresponding node
 * to be highlighted.
 **/
public class NTInfoBox {

	//STATIC VARS
	static int INFO_BOX_PADDING = 10;
	static int INFO_BOX_LINE_HEIGHT = 13;
	static int INFO_BOX_WIDTH = 400;
	
	//MODEL
	Object owner;
	Vector<String[]> sEntries = new Vector<String[]>();
	int fields = 0;
	NTNode node;
	
	//GRAPHICS
	VirtualSpace vs;
	VRectangle gBox,gTitleBox;
	VText gTitle;
	Vector<VText[]> gEntries = new Vector<VText[]>();
	boolean visible = false;
	
	public NTInfoBox(NTNode node, int fields)
	{
		this.node = node;
		this.fields = fields;
	}
	
	//----GRAPHICS----
	public void createGraphics(VirtualSpace vs){
		this.vs = vs;
		if(gBox != null) return;
		
		long boxWidth = INFO_BOX_WIDTH;
		
		gTitleBox = new VRectangle(0, 0 ,0, boxWidth, INFO_BOX_LINE_HEIGHT * 2, ProjectColors.NODE_BACKGROUND[ProjectColors.COLOR_SCHEME]);
		gTitleBox.setSensitivity(false);
		gTitleBox.setOwner(this);
		vs.addGlyph(gTitleBox);
		

		gTitle = new VText((-gTitleBox.getWidth() /2) + INFO_BOX_PADDING, 0 , 0, ProjectColors.NODE_TEXT[ProjectColors.COLOR_SCHEME], node.getName());
		gTitle.setSensitivity(false);
		vs.addGlyph(gTitle);
		gTitleBox.stick(gTitle);
		gTitleBox.setOwner(this);

		
		long currentRowIntend = -INFO_BOX_LINE_HEIGHT *2;
		VText gText;
		for(String[] sEntry : sEntries){
			VText gEntry[] = new VText[fields];
			int i = 0;
			for(String s : sEntry){
				gText = new VText((-gTitleBox.getWidth() /2) + INFO_BOX_PADDING, currentRowIntend,0 ,ProjectColors.NODE_TEXT[ProjectColors.COLOR_SCHEME], s);
				vs.addGlyph(gText);
				gText.setSensitivity(false);
				gEntry[i] = gText;
				i++;
				currentRowIntend -= INFO_BOX_LINE_HEIGHT; //height of text 
			}
			currentRowIntend -= INFO_BOX_LINE_HEIGHT; //height of text 
			gEntries.add(gEntry);
		}
		
		long boxHeight = - (currentRowIntend - INFO_BOX_PADDING); 
		gBox = new VRectangle(0,-((INFO_BOX_LINE_HEIGHT ) + boxHeight /2),0 ,boxWidth, boxHeight, ProjectColors.NODE_BACKGROUND[ProjectColors.COLOR_SCHEME]);
		vs.addGlyph(gBox);
		gBox.setOwner(this);

		
		gTitleBox.stick(gBox);
		
	
		
		for(VText[] gEntry : gEntries){
			for(VText g : gEntry){
				gBox.stick(g);
				vs.hide(g);
			}
		}
		
		onTop();
		hide();
	}

	public void onTop(){
		vs.onTop(gBox);
		vs.onTop(gTitleBox);
		vs.onTop(gTitle);
		for(VText[] textEntry : gEntries){
			for(VText gText : textEntry){
				vs.onTop(gText);
			}
		}
	}
	
	/**Must be called before showing the box.
	 * */
	public void alignToWesternLabel(long x, long y){
	    if (gTitleBox == null){return;} //XXX: temporary fix, I don't know why it is null in case of single node matrices
		gTitleBox.moveTo(x - gTitleBox.getWidth() /2, y - gTitleBox.getHeight());
	}
	public void alignToNorthernLabel(long x, long y){
	    if (gTitleBox == null){return;} //XXX: temporary fix, I don't know why it is null in case of single node matrices
		gTitleBox.moveTo(x + gTitleBox.getWidth(), y - gTitleBox.getHeight() /2);
	}
	public void move(long x, long y){
	    if (gTitleBox == null){return;} //XXX: temporary fix, I don't know why it is null in case of single node matrices
		gTitleBox.move(x, y);
	}
	
	public void hide(){
		vs.hide(gBox);
		vs.hide(gTitleBox);
		vs.hide(gTitle);
		for(VText[] textEntry : gEntries){
			for(VText gText : textEntry){
				vs.hide(gText);
			}
		}
		visible = false;
	}
	public void show(){
		vs.show(gBox);
		vs.show(gTitleBox);
		vs.show(gTitle);
		for(VText[] textEntry : gEntries){
			for(VText gText : textEntry){
				vs.show(gText);
			}
		}		
		visible = true;
	}
	
	//----BUILDING MODEL----
	
	public void addEntry(String[] entry){
		if(entry.length != fields) return;
		sEntries.add(entry);
	}
	
	public void setOwner(Object o){
		owner = o;
	}
	
	
	//----GETTER/SETTER---
	public boolean hasEntries(){
		return sEntries.size() > 0;
	}
	public boolean isVisible(){
		return visible;
	}
	public NTNode getNode(){
		return node;
	}
	

	
}

