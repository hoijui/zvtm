/*   AUTHOR :            Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *   Copyright (c) INRIA, 2008-2011. All Rights Reserved
 *   Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id$
 */

package fr.inria.zvtm.demo;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.GradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.JButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Vector;
import java.util.Scanner;

import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.event.RepaintListener;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.IcePDFPageImg;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
import fr.inria.zvtm.glyphs.MultilineText;

import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.annotations.Annotation;
import org.icepdf.core.pobjects.graphics.text.*;

public class PDFViewer {

	static final int NAV_ANIM_DURATION = 300;

	static final short DIRECT_G2D_RENDERING = 0;
	static final short OFFSCREEN_IMAGE_RENDERING = 1;
	short rendering_technique = OFFSCREEN_IMAGE_RENDERING;

	VirtualSpace vs ;
	static final String spaceName = "pdfSpace";
	ViewListener eh;
	Camera mCamera;

	class RectFloat{
		public double x , y , width , height;
		RectFloat(double x, double y , double height , double width){
			this.x = x; this.y= y; this.height = height; this.width = width;
		}
	}

	View pdfView;
	VWGlassPane gp;
	private Document document;
    int panelWidth, panelHeight;
    private Vector<Glyph> lastSearchAdded = new Vector<Glyph>();
    private Vector<Glyph> annotationAdded = new Vector<Glyph>();
    private Vector<WordText> allWords = new Vector<WordText>();
    String lastSearch = new String();
    private int glphyIndex;
    private int pagesInDocument;
    boolean showAnnotation = false;
    float scaleFactor;

	PDFViewer(String pdfFilePath, float df, float sf){
		VirtualSpaceManager.INSTANCE.setDebug(true);
		initGUI();
		load(new File(pdfFilePath), df, sf);
		scaleFactor = sf;
	}

	public void initGUI(){
		vs = VirtualSpaceManager.INSTANCE.addVirtualSpace(spaceName);
		mCamera = vs.addCamera();
		Vector<Camera> cameras= new Vector<Camera>();
		cameras.add(mCamera);
		pdfView = VirtualSpaceManager.INSTANCE.addFrameView(cameras, "ZVTM PDF Viewer", View.STD_VIEW, 1024, 768, false, true, true, null);
		pdfView.setBackgroundColor(Color.WHITE);
		pdfView.setNotifyCursorMoved(false);
		updatePanelSize();
    	ComponentAdapter ca0 = new ComponentAdapter(){
    		public void componentResized(ComponentEvent e){
    			updatePanelSize();
    		}
    	};
    	pdfView.getFrame().addComponentListener(ca0);
		gp = new VWGlassPane(this);
		((JFrame)pdfView.getFrame()).setGlassPane(gp);
		eh = new PDFViewerEventHandler(this);
		pdfView.setListener(eh);
		pdfView.setAntialiasing(true);
		mCamera.setAltitude(0);
		VirtualSpaceManager.INSTANCE.repaint();
    }

    void updatePanelSize(){
        Dimension d = pdfView.getPanel().getComponent().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
    }

    RectFloat convertRect(RectFloat r, PDimension pageSize , int pageNumber ){
    	r.x*=scaleFactor;r.y*=scaleFactor;r.width*=scaleFactor;r.height*=scaleFactor;
    	r.x -=  scaleFactor*(pageSize.getWidth()/2) - r.width/2;
    	r.x +=  pageNumber*(pageSize.getWidth() + 60)*scaleFactor; // TO:DO add the Difference between the pages
    	r.y -= scaleFactor*(pageSize.getHeight()/2) - r.height/2;
    	return r;
    }

    void highlightPage(ArrayList<RectFloat> rects){
    	Color c = new Color(255,255,0,100);
    	for( RectFloat r : rects){
    		VRectangle v = new VRectangle(r.x , r.y ,0 , r.width , r.height ,c );
    		v.setBorderColor(new Color(0,0,0,0));
    		vs.addGlyph(v);
    		lastSearchAdded.add(v);
    	}
    	pdfView.centerOnGlyph(lastSearchAdded.elementAt(0), mCamera, 400 , true , 0.9f);
    }

   void removeLastSearch(){
   	 for(Glyph lastgly:lastSearchAdded){
   		 vs.removeGlyph(lastgly);
   	 }
   	 lastSearchAdded.clear();
   }

   void highlightAnnotation(){
	   	 for(Glyph lastgly:annotationAdded){
	   		 vs.addGlyph(lastgly);
	   	 }
	}

    void removeAnnotation(){
   	 for(Glyph lastgly:annotationAdded){
   		 vs.removeGlyph(lastgly);
   	 }
   }

    void previousWord(){
    	if(lastSearchAdded.size() ==0 )return ;
    	glphyIndex--;
    	if(glphyIndex == -1)
    		glphyIndex = lastSearchAdded.size() -1;
       	pdfView.centerOnGlyph(lastSearchAdded.elementAt(glphyIndex), mCamera, 400 , true , 0.9f);

    }

    void nextWord(){
    	if(lastSearchAdded.size() ==0 )return ;
    	glphyIndex++;
    	if(glphyIndex == lastSearchAdded.size())
    		glphyIndex = 0 ;
    	pdfView.centerOnGlyph(lastSearchAdded.elementAt(glphyIndex), mCamera, 400 , true , 0.9f);
    }

	void findWord(String search){
		if(search.equals(lastSearch)) return;
		lastSearch = search;
		glphyIndex =0;
		removeLastSearch();
		int wordsFound =0;
    	ArrayList<RectFloat> r = new ArrayList<RectFloat>();
    	PDimension pp = document.getPageDimension(0, 0);
        for(int i = 0 ; i< pagesInDocument ; i++){
	    	PageText pageText = document.getPageText(i);
	    	if(pageText==null) return ;
	         // start iteration over words.
	        ArrayList<LineText> pageLines = pageText.getPageLines();
	        for (LineText pageLine : pageLines) {
	            ArrayList<WordText> lineWords = pageLine.getWords();
	            // compare words against search terms.
	            for (WordText word : lineWords) {
	            	if(word.toString().toUpperCase().compareTo(search.toUpperCase())==0){
	            		wordsFound++;
	            		java.awt.geom.Rectangle2D.Float f=  word.getBounds();

	            		// convert it into the page dimension and then add.
	            		r.add(convertRect(new RectFloat(f.x,f.y,f.height,f.width) , pp , i));
	            	}
	             }
	         }
	     }

         if(wordsFound>0){
        	 highlightPage(r);
         }
    }

	void showAnnotations(){
		if(annotationAdded.size()!=0)
		{
			highlightAnnotation();
			return;
		}
		final ArrayList<MultilineText> mi = new ArrayList<MultilineText>();
		final ArrayList<VRectangle> vi = new ArrayList<VRectangle>();
		PDimension pp = document.getPageDimension(0, 0);
		for(int i = 0 ; i < pagesInDocument; i++){
			Page page = document.getPageTree().getPage(i, pagesInDocument);
			ArrayList<Annotation>	annotations = page.getAnnotations();
			if(annotations != null){
				for( int j =0 ;j<annotations.size() ; j+=2){
					Annotation annotationP = annotations.get(j);
					if(annotationP != null && annotationP.getObject("Contents") != null){
						java.awt.geom.Rectangle2D.Float r = annotationP.getUserSpaceRectangle();
						RectFloat f = convertRect(new RectFloat(r.x,r.y,r.height,r.width),pp,i);
						MultilineText m  = new MultilineText(annotationP.getObject("Contents").toString());
						m.setWidthConstraint(100);
						m.setScale(2);
						VRectangle vr = new VRectangle(f.x , f.y,0, 100, 100 ,annotationP.getColor()   );
						vi.add(vr);
						mi.add(m);

						pdfView.repaint(new RepaintListener(){
							public void viewRepainted(View v){
								for(int i =0 ;i<vi.size() ;i++){
									vi.get(i).setVisible(true);
									double width =mi.get(i).getBounds(mCamera.getIndex()).getX(), height=mi.get(i).getBounds(mCamera.getIndex()).getY();
									vi.get(i).setWidth(width);
									vi.get(i).setHeight(height);
									mi.get(i).moveTo(vi.get(i).vx -width/2, vi.get(i).vy +height/2);
									vi.get(i).setDrawBorder(false);
									mi.get(i).setType(ANNOTATION);
									mi.get(i).setColor(Color.BLACK);
									mi.get(i).stick(vi.get(i));
								}
								pdfView.removeRepaintListener();
							}
						});

						vr.setVisible(false);
						Color c = new Color(255 , 255 , 255 , 0);
						m.setColor(c);
						annotationAdded.add(vr);
						annotationAdded.add(m);
					}
				}
			}
		}
		highlightAnnotation();
	}
    /*------------------ PDF loading ------------------ */

    static final String ANNOTATION = "a";
    static final String PAGE = "p";

    void load(File f, float detailFactor, float scaleFactor){
        gp.setLabel("Loading "+f.getName());
        gp.setValue(10);
        gp.setVisible(true);
        document = new Document();
        try {
            document.setFile(f.getAbsolutePath());
        } catch (PDFException ex) {
            System.out.println("Error parsing PDF document " + ex);
        } catch (PDFSecurityException ex) {
            System.out.println("Error encryption not supported " + ex);
        } catch (FileNotFoundException ex) {
            System.out.println("Error file not found " + ex);
        } catch (IOException ex) {
            System.out.println("Error handling PDF document " + ex);
        }
        int page_width = (int)document.getPageDimension(0, 0).getWidth();
        pagesInDocument=document.getNumberOfPages();
        gp.setLabel("Loading "+f.getName()+ " (" + pagesInDocument + " pages)");
        Glyph[] pages = new Glyph[pagesInDocument];
        for (int i = 0; i < pagesInDocument; i++) {
            pages[i] = new IcePDFPageImg(i*Math.round(page_width*1.1f*detailFactor*scaleFactor), 0, 0, document, i, detailFactor, scaleFactor);
            gp.setValue(10+Math.round(i*90/(float)pagesInDocument));
            pages[i].setType(PAGE);
        }
        vs.addGlyphs(pages, true);
        // center on first page
        pdfView.centerOnGlyph(pages[0], mCamera, PDFViewer.NAV_ANIM_DURATION);
        // clean up resources
       // document.dispose();
        gp.setVisible(false);
    }

    static String getVersion(){
        Scanner sc = new Scanner(PDFViewer.class.getResourceAsStream("/properties")).useDelimiter("\\s*=\\s*");
        while (sc.hasNext()){
            String token = sc.next();
            if (token.equals("version")){
                return "PDFViewer v" + sc.next();
            }
        }
        return "PDFViewer";
    }

	public static void main(String[] args){
	    System.getProperties().put("org.icepdf.core.screen.alphaInterpolation", RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        System.getProperties().put("org.icepdf.core.screen.antiAliasing", RenderingHints.VALUE_ANTIALIAS_ON);
        System.getProperties().put("org.icepdf.core.screen.textAntiAliasing", RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        System.getProperties().put("org.icepdf.core.screen.colorRender", RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        System.getProperties().put("org.icepdf.core.screen.dither", RenderingHints.VALUE_DITHER_ENABLE);
        System.getProperties().put("org.icepdf.core.screen.fractionalmetrics", RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        System.getProperties().put("org.icepdf.core.screen.interpolation", RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        System.getProperties().put("org.icepdf.core.screen.render", RenderingHints.VALUE_RENDER_QUALITY);
        System.getProperties().put("org.icepdf.core.screen.stroke", RenderingHints.VALUE_STROKE_PURE);
		System.out.println("-----------------");
	    System.out.print(getVersion());
		System.out.println("-----------------");
		System.out.println("General information");
		System.out.println("JVM version: "+System.getProperty("java.vm.vendor")+" "+System.getProperty("java.vm.name")+" "+System.getProperty("java.vm.version"));
		System.out.println("OS type: "+System.getProperty("os.name")+" "+System.getProperty("os.version")+"/"+System.getProperty("os.arch")+" "+System.getProperty("sun.cpu.isalist"));
		System.out.println("-----------------");
		System.out.println("Directory information");
		System.out.println("Java Classpath: "+System.getProperty("java.class.path"));
		System.out.println("Java directory: "+System.getProperty("java.home"));
		System.out.println("Launching from: "+System.getProperty("user.dir"));
		System.out.println("-----------------");
		System.out.println("User informations");
		System.out.println("User name: "+System.getProperty("user.name"));
		System.out.println("User home directory: "+System.getProperty("user.home"));
		System.out.println("-----------------");
		new PDFViewer((args.length > 0) ? args[0] : null, (args.length > 1) ? Float.parseFloat(args[1]) : 1, (args.length > 2) ? Float.parseFloat(args[2]) : 1);
	}

}

class VWGlassPane extends JComponent {

    static final int BAR_WIDTH = 200;
    static final int BAR_HEIGHT = 10;

    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);
    static final AlphaComposite GLASS_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);
    static final Color MSG_COLOR = Color.DARK_GRAY;
    GradientPaint PROGRESS_GRADIENT = new GradientPaint(0, 0, Color.ORANGE, 0, BAR_HEIGHT, Color.BLUE);

    String msg = null;
    int msgX = 0;
    int msgY = 0;

    int completion = 0;
    int prX = 0;
    int prY = 0;
    int prW = 0;

    PDFViewer application;

    VWGlassPane(PDFViewer app){
        super();
        this.application = app;
        addMouseListener(new MouseAdapter(){});
        addMouseMotionListener(new MouseMotionAdapter(){});
        addKeyListener(new KeyAdapter(){});
    }

    public void setValue(int c){
        completion = c;
        prX = application.panelWidth/2-BAR_WIDTH/2;
        prY = application.panelHeight/2-BAR_HEIGHT/2;
        prW = (int)(BAR_WIDTH * ((float)completion) / 100.0f);
        PROGRESS_GRADIENT = new GradientPaint(0, prY, Color.LIGHT_GRAY, 0, prY+BAR_HEIGHT, Color.DARK_GRAY);
        repaint(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }

    public void setLabel(String m){
        msg = m;
        msgX = application.panelWidth/2-BAR_WIDTH/2;
        msgY = application.panelHeight/2-BAR_HEIGHT/2 - 10;
        repaint(msgX, msgY-50, 400, 70);
    }

    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D)g;
        Rectangle clip = g.getClipBounds();
        g2.setComposite(GLASS_ALPHA);
        g2.setColor(Color.WHITE);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setComposite(AlphaComposite.Src);
        if (msg != null && msg.length() > 0){
            g2.setColor(MSG_COLOR);
            g2.setFont(GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }

}

class PDFViewerEventHandler implements ViewListener {

	PDFViewer application;

	long lastJPX,lastJPY;

	PDFViewerEventHandler(PDFViewer appli){
		application = appli;
	}

	boolean panning = false;
	boolean rectDrag = false;
	int initialX , initialY;
	int finalY , finalX;
	public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
		lastJPX=jpx;
		lastJPY=jpy;
		Glyph g = v.lastGlyphEntered();
		if (g != null && g.getType()!=null &&g.getType().equals(PDFViewer.ANNOTATION)){
		    v.getVCursor().stickGlyph(g);
		}
		else {
		    panning = true;
    		//v.setDrawDrag(true);
			v.showFirstOrderPanWidget(jpx, jpy);
    		VirtualSpaceManager.INSTANCE.getActiveView().mouse.setSensitivity(false);
		}
	}

	public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
	    panning = false;
	    v.getVCursor().unstickLastGlyph();
		Camera c = VirtualSpaceManager.INSTANCE.getActiveView().getActiveCamera();
		c.setXspeed(0);
        c.setYspeed(0);
        c.setZspeed(0);
    	v.hideFirstOrderPanWidget();
		//v.setDrawDrag(false);
		VirtualSpaceManager.INSTANCE.getActiveView().mouse.setSensitivity(true);
	}

	public void click1(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){
		Glyph g = v.lastGlyphEntered();
		if (g != null){
			application.pdfView.centerOnGlyph(g, application.mCamera, PDFViewer.NAV_ANIM_DURATION);
		}
		else {
			application.pdfView.getGlobalView(application.mCamera, PDFViewer.NAV_ANIM_DURATION);
		}
	}

	public void press2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release2(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click2(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void press3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void release3(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){}

	public void click3(ViewPanel v,int mod,int jpx,int jpy,int clickNumber, MouseEvent e){}

	public void mouseMoved(ViewPanel v,int jpx,int jpy, MouseEvent e){}

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
		if (panning == true){
			Camera c = VirtualSpaceManager.INSTANCE.getActiveCamera();
			double a = (c.focal+Math.abs(c.altitude))/c.focal;
			if (mod == SHIFT_MOD) {
			    c.setXspeed(0);
                c.setYspeed(0);
                c.setZspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
				//50 is just a speed factor (too fast otherwise)
			}
			else {
			    c.setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/50.0f)) : (long)((jpx-lastJPX)/(a*50)));
                c.setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/50.0f)) : (long)((lastJPY-jpy)/(a*50)));
                c.setZspeed(0);
			}
		}
	}

	public void mouseWheelMoved(ViewPanel v,short wheelDirection,int jpx,int jpy, MouseWheelEvent e){
		Camera c = VirtualSpaceManager.INSTANCE.getActiveCamera();
		double a = (c.focal+Math.abs(c.altitude))/c.focal;
		if (wheelDirection == WHEEL_UP){
			c.altitudeOffset(-a*5);
			VirtualSpaceManager.INSTANCE.repaint();
		}
		else {
			//wheelDirection == WHEEL_DOWN
			c.altitudeOffset(a*5);
			VirtualSpaceManager.INSTANCE.repaint();
		}
	}

	public void enterGlyph(Glyph g){
	}

	public void exitGlyph(Glyph g){
	}
	public void Ktype(ViewPanel v,char c,int code,int mod, KeyEvent e){	}

	public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
		if (code == KeyEvent.VK_F && (mod==CTRL_MOD || mod==META_MOD)){
			JFrame searchBox = new SearchBox();
			searchBox.setVisible(true);
		}
		else if(code == KeyEvent.VK_A){
			if(application.showAnnotation == false){
				application.showAnnotations();
				application.showAnnotation =true;
			}
			else{
				application.removeAnnotation();
				application.showAnnotation = false;
			}
		}
	}

	public void Krelease(ViewPanel v,char c,int code,int mod, KeyEvent e){}

	public void viewActivated(View v){}

	public void viewDeactivated(View v){}

	public void viewIconified(View v){}

	public void viewDeiconified(View v){}

	public void viewClosing(View v){System.exit(0);}

	class SearchBox extends JFrame {

		private JTextField textField;

			public SearchBox(){
				setTitle("Search ");
		        textField = new JTextField(10);
		        add(textField,BorderLayout.NORTH);
		        setSize(180,80);
		        JButton previous = new JButton("Previous");
		        add(previous,BorderLayout.WEST);
		        JButton next = new JButton("Next");
		        add(next,BorderLayout.EAST);
		        previous.addActionListener(new ButtonListener());
		        next.addActionListener(new ButtonListener());
		        textField.addKeyListener(new TextFieldListner());
		        this.addWindowListener(new Closer());
			}

			class TextFieldListner implements KeyListener{
				public void keyTyped(KeyEvent e) {}

				    public void keyPressed(KeyEvent e) {
				    	if(e.getKeyCode()==10){
						String text = textField.getText();
						application.findWord(text);
				    	}
				    }

				    public void keyReleased(KeyEvent e) {}

			}
			class ButtonListener implements ActionListener{
				public void actionPerformed(ActionEvent e) {
					if(e.getActionCommand().equals("Next")){
						application.nextWord();
					}
					else if (e.getActionCommand().equals("Previous")){
						application.previousWord();
					}

				}
			}
			class Closer extends WindowAdapter {
			    public void windowClosing (WindowEvent event) {
			    	application.removeLastSearch();
			    	application.lastSearch ="";
			    }
			}
	}

}