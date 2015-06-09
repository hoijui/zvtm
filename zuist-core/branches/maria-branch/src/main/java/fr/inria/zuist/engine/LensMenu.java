package fr.inria.zuist.engine;

import java.awt.geom.Point2D;
import java.awt.Color;
import java.util.Vector;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VRing;
import fr.inria.zvtm.glyphs.VTextOr;
import fr.inria.zvtm.glyphs.VText;

public class LensMenu {

	Vector <Glyph> outerItems = new Vector <Glyph> ();
	Vector <Glyph> outerLabels = new Vector <Glyph>();

	Vector <Glyph> innerItems = new Vector <Glyph> ();
	Vector <Glyph> innerLabels = new Vector <Glyph>();

	Vector <String> contextOptions = new Vector <String>();
	Vector <String> lenseOptions = new Vector <String>();;
	VirtualSpace vs;
	double radius;
	double inner_radius;
	double outer_radius;
	double totalAngle = Math.PI;
	String innerType = "lensMenuLens";
	String outerType = "lensMenuContext";
	Point2D.Double centerCoordinates;
	float translucencyValue= 0.8f;
	Color lineColor = Color.LIGHT_GRAY;
	Color fillColor = Color.BLACK;

	public LensMenu (Vector <String> lenseOptions, Vector <String>  contextOptions, VirtualSpace vs, double inner_radius, double outer_radius, Point2D.Double centerCoordinates)
	{
		this.lenseOptions = lenseOptions;
		this.contextOptions = contextOptions;
		this.vs = vs;
		this.radius = radius;
		this.inner_radius = inner_radius;
		this.outer_radius = outer_radius;
		this.centerCoordinates = centerCoordinates;
		//drawLensMenu(centerCoordinates);
	}

	public void drawLensMenu (Point2D.Double centerCoordinates)
	{
		double startAngle = totalAngle/(2*lenseOptions.size());
		//startAngle = Math.PI/3;
		double angleWidth = totalAngle/(lenseOptions.size());
		//System.out.println("Options"+lenseOptions.size());
		double angle;
		double angleLabel;
		double [] offsetsX={5,-5,0};
		double[] offsetsY = {-5,-5,0};
		double offset=0;

		int direction = 1;	
			//System.out.println("TYPE " + types[j]);
			for(int i=0; i<lenseOptions.size(); i++)
			{
				
				angle = direction*(startAngle+i*angleWidth);
				angleLabel = direction *(startAngle + i*angleWidth/2);
				double offsetX = (Double)Math.signum(Math.cos(angleLabel))*offset;
				double offsetY = (Double)Math.signum(Math.sin(angleLabel))*offset;
				VRing innerRing = new VRing(centerCoordinates.getX(), centerCoordinates.getY(), 0, inner_radius, angleWidth, 0.5f, -angle, fillColor, lineColor, translucencyValue);

				VRing outerRing = new VRing(centerCoordinates.getX(), centerCoordinates.getY(), 0, outer_radius, angleWidth, 0.67f, angle, fillColor, lineColor, translucencyValue);
				innerItems.add(innerRing);
				outerItems.add(outerRing);
				VTextOr labelOuter = new VTextOr(offsetX+centerCoordinates.getX()+Math.cos(angle)*outer_radius*4/5, offsetY+
	                        centerCoordinates.getY()+Math.sin(angle)*outer_radius*4/5,
	                        0, Color.LIGHT_GRAY, contextOptions.get(i),0, VText.TEXT_ANCHOR_MIDDLE);

				VTextOr labelInner = new VTextOr(centerCoordinates.getX()+Math.cos(angle)*inner_radius*3/4, (
	                        centerCoordinates.getY()-Math.sin(angle)*inner_radius*3/4),
	                        0, Color.LIGHT_GRAY, lenseOptions.get(i),0, VText.TEXT_ANCHOR_MIDDLE);
				innerLabels.add(labelInner);
				outerLabels.add(labelOuter);
				//ring = new VRing(centerCoordinates.getX(), centerCoordinates.getY(), 0, 100, Math.PI/2, 0.5f, Math.PI*3/4, Color.BLACK, Color.BLACK);
				vs.addGlyph(innerRing);
				vs.addGlyph(outerRing);
				vs.addGlyph(labelInner);
				vs.addGlyph(labelOuter); 

				for( Glyph g : innerItems) {
					if(g.getType()==null) {
						g.setType(innerType);
					}
				}
				for( Glyph g : innerLabels) {
					if(g.getType()==null) {
						g.setType(innerType);
						g.setSensitivity(false);
					}
				}
				for( Glyph g : outerItems) {
					if(g.getType()==null) {
						g.setType(outerType);
					}
				}
				for( Glyph g : outerLabels) {
					if(g.getType()==null) {
						g.setType(outerType);
						g.setSensitivity(false);
					}
				}

			}
	}


	public void hideLensMenu()
	{
		for (Glyph g: outerItems) {
			vs.removeGlyph(g);
		}
		for (Glyph g: innerItems) {
			vs.removeGlyph(g);
		}
		for (Glyph g: innerLabels) {
			vs.removeGlyph(g);
		}
		for (Glyph g: outerLabels) {
			vs.removeGlyph(g);
		}
		
	}

	/*public Vector<Glyph> getOuterItems()
	{
		return outerItems;
	}*/

	public boolean isInMenu(Point2D.Double coordinates)
	{
		double distance = coordinates.distance(centerCoordinates);
		if(inner_radius/2<=distance && distance<=outer_radius) {
			return true;
		}
		else { return false; }
	}

	public boolean isInInnerMenu (Point2D.Double coordinates)
	{
		double distance = coordinates.distance(centerCoordinates);
		if(inner_radius/2<=distance && distance<=inner_radius) {
			return true;
		}
		else { return false; }
	}
	public boolean isInOuterMenu (Point2D.Double coordinates)
	{
		double distance = coordinates.distance(centerCoordinates);
		if(inner_radius<=distance && distance<=outer_radius) {
			return true;
		}
		else { return false; }
	}

	public boolean isInCenterMenu(Point2D.Double coordinates)
	{
		double distance = coordinates.distance(centerCoordinates);
		if(inner_radius/2>=distance)
			return true;
		else { return false; }
	}

	public boolean isOutOfMenu (Point2D.Double coordinates)
	{
		double distance = coordinates.distance(centerCoordinates);
		if(distance>=outer_radius) {return true;}
		else {return false; }
	}

	public int positionInMenu(Point2D.Double coordinates)
	{
		int result = -1;
		if (isInCenterMenu(coordinates)) { result = 0;}
		if (isInInnerMenu(coordinates) && coordinates.getY() >= centerCoordinates.getY()) {result = 1;}
		if (isInOuterMenu(coordinates) && coordinates.getY() <= centerCoordinates.getY()) {result = 2;}
		if (isOutOfMenu(coordinates)) {result = 3;}
		

		return result;
	}

	public boolean isInShowedMenu(Point2D.Double coordinates) {
		if ((isInInnerMenu(coordinates) && coordinates.getY()<= centerCoordinates.getY()) || (isInOuterMenu(coordinates)&&coordinates.getY()>=centerCoordinates.getY())) {return true;}
		else {return false;}
	}
	/*public  String getLayerType(Glyph g)
	{
		String layer = null;
		if (innerItems.contains(g)) {
			layer= "lens";
		}
		if(outerItems.contains(g)) {
			layer = "context";
		}
		return layer;
	}*/
	/*public void exitHideEvent(Glyph item, Point2D.Double coordinates)
	{
		
		if(innerItems.contains(item) && !(isInInnerMenu(coordinates))) {
			removeHideItems(innerItems);
			removeHideItems(innerLabels);
		}
		else  if (outerItems.contains(item) && !(isInOuterMenu(coordinates))){
			removeHideItems(outerItems);
			removeHideItems(outerLabels);
		}
	}

	public void exitShowEvent (Glyph item, Point2D.Double coordinates)
	{
		if(innerItems.contains(item) && !(isInMenu(coordinates))){
			removeHideItems(outerItems);
			removeHideItems(outerLabels);
		}
		if(outerItems.contains(item) && !(isInMenu(coordinates))){
			removeHideItems(innerItems);
			removeHideItems(innerLabels);
		}
	}

	public void enterShowEvent(Glyph item)
	{

		if(innerItems.contains(item)) {
			addHideItems(outerItems);
			addHideItems(outerLabels);
		}
		if(outerItems.contains(item)) {
			addHideItems(innerItems);
			addHideItems(innerLabels);
		}
	}

	void removeHideItems (Vector <Glyph> list)
	{
		for (Glyph g : list)
		{
			if(g.getType() == "lensMenuHide") {
				vs.removeGlyph(g);
			}				
		}
	}

	void addHideItems (Vector <Glyph> list) {
		for (Glyph g : list)
		{
			if(g.getType() == "lensMenuHide") {
				//System.out.println("Adding glyph");
				vs.addGlyph(g);
			}				
		}
	}

	public void lensHideMenuEvent(Glyph g) {
		if(innerItems.contains(g)) {
			int index = innerItems.indexOf(g);
			//System.out.println(((VText)innerLabels.get(index)).getText());
		}
	}*/

	public String getLabel(Glyph g) {
		String label=null;
		if (innerItems.contains(g)) {
			int index = innerItems.indexOf(g);
			//System.out.println("Index Inner Item "+index);
			label = ((VText)innerLabels.get(index)).getText();

		}
		else if (outerItems.contains(g)) {
			int index = outerItems.indexOf(g);
			//System.out.println("Index Outer Item"+index);
			label = ((VText)outerLabels.get(index)).getText();
		}
		return label;
	}

	public Point2D.Double getCenterCoordinates(){
		return centerCoordinates;
	}

	/*public void changeLabelsInner(Vector <String> options)
	{
		for(int i=0; i<options.size(); i++)
		{
			((VText)innerLabels.get(i)).setText(options.get(i));
			//innerLabels.get(i).draw();
		}
	}

	public void changeLabelsOuter(Vector <String> options)
	{
		for(int i=0; i<options.size(); i++)
		{
			((VText)outerLabels.get(i)).setText(options.get(i));
			//outerLabels.get(i).draw();
		}
	}*/

	public void highlight (Glyph g)
	{
		g.setBorderColor(Color.ORANGE);
		g.setTranslucencyValue(1.0f);
	}

	public void unHighlight(Glyph g)
	{
		g.setBorderColor(lineColor);
		g.setTranslucencyValue(translucencyValue);
	}


}