package fr.inria.zvtm.nodetrix;

import java.awt.Color;
import java.util.Vector;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;

/**Infobox that shows additional information about a matix node.
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
	static int INFO_BOX_WIDTH = 200;
	
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
		
		//title
		long currentRowIntend = -INFO_BOX_PADDING*2;
		gTitle = new VText(INFO_BOX_PADDING, currentRowIntend+3, 0, Color.black.darker(), node.getName());
		gTitle.setSensitivity(false);
		vs.addGlyph(gTitle);
		currentRowIntend -= INFO_BOX_LINE_HEIGHT*2; 
		
		VText gText;
		for(String[] sEntry : sEntries){
			VText gEntry[] = new VText[fields];
			int i = 0;
			for(String s : sEntry){
				gText = new VText(INFO_BOX_PADDING, currentRowIntend,0 ,NodeTrixViz.COLOR_INFO_BOX_TEXT, s);
				gText.setSensitivity(false);
				vs.addGlyph(gText);
				gEntry[i] = gText;
				i++;
				currentRowIntend -= INFO_BOX_LINE_HEIGHT; //height of text 
			}
			currentRowIntend -= INFO_BOX_LINE_HEIGHT; //height of text 
			gEntries.add(gEntry);
		}
		
		long height = (currentRowIntend - INFO_BOX_PADDING) / -2; 
		long width = INFO_BOX_WIDTH;
		gBox = new VRectangle(width, -height,0,width, height, NodeTrixViz.COLOR_INFO_BOX);
		gBox.stick(gTitle);
		gBox.setOwner(this);
		vs.addGlyph(gBox);
		
		gTitleBox = new VRectangle(width, -INFO_BOX_LINE_HEIGHT,0, width, INFO_BOX_LINE_HEIGHT, NodeTrixViz.COLOR_INFO_BOX);
		gTitleBox.setSensitivity(false);
		vs.addGlyph(gTitleBox);
		gBox.stick(gTitleBox);
		
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
		gBox.moveTo(x - gBox.getWidth(), y - gBox.getHeight());
	}
	public void alignToNorthernLabel(long x, long y){
		gBox.moveTo(x + gBox.getWidth(), y - gBox.getHeight());
	}
	public void move(long x, long y){
		gBox.move(x, y);
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

