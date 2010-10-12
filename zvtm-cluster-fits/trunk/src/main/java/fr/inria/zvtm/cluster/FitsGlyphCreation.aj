package fr.inria.zvtm.cluster;

import java.awt.geom.Point2D;
import java.awt.Color;
import java.awt.LinearGradientPaint;

import java.net.URL;

import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.FitsImage;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.LGRectangle;
import fr.inria.zvtm.fits.RangeSelection;
import fr.inria.zvtm.fits.Slider;

public aspect FitsGlyphCreation {
    @Override GlyphReplicator FitsImage.getReplicator(){
        return new FitsImageReplicator(this);
    }

    @Override GlyphReplicator RangeSelection.getReplicator(){
        return new RangeSelectionReplicator(this);
    }

    @Override GlyphReplicator Slider.getReplicator(){
        return new SliderReplicator(this);
    }
    
    @Override GlyphReplicator LGRectangle.getReplicator(){
        return new LGRectangleReplicator(this);
    }

    private static class FitsImageReplicator extends GlyphCreation.ClosedShapeReplicator {
        private final URL imageLocation;
        private final double scaleFactor;

        FitsImageReplicator(FitsImage source){
            super(source);
            this.scaleFactor = source.scaleFactor;
            this.imageLocation = source.getImageLocation();
        }

        Glyph doCreateGlyph(){
            try{
                return new FitsImage(0.,0.,0,imageLocation, scaleFactor);
            } catch(Exception e){
                //XXX error handling
                throw new Error(e);
            }
        }
    }

    private static class RangeSelectionReplicator extends GlyphCreation.AbstractGlyphReplicator {
        private final double leftTickPos;
        private final double rightTickPos;

        RangeSelectionReplicator(RangeSelection source){
            super(source);
            this.leftTickPos = source.getLeftValue();
            this.rightTickPos = source.getRightValue();
        }

        Glyph doCreateGlyph(){
            RangeSelection retval = new RangeSelection();
            retval.setTicksVal(leftTickPos, rightTickPos);
            return retval;
        }
    }

    private static class SliderReplicator extends GlyphCreation.AbstractGlyphReplicator {
        private final double tickVal;

        SliderReplicator(Slider source){
            super(source);
            this.tickVal = source.getValue();
        }

        Glyph doCreateGlyph(){
            Slider retval = new Slider();
            retval.setTickVal(tickVal);
            return retval;
        }
    }
    
    private static class LGRectangleReplicator extends GlyphCreation.VRectangleReplicator {

        private final Point2D startPoint;
        private final Point2D endPoint;
        private final float[] fractions;
        private final Color[] colors;

        LGRectangleReplicator(LGRectangle source){
            super(source);
            startPoint = source.getGradient().getStartPoint();
            endPoint = source.getGradient().getEndPoint();
            fractions = source.getGradient().getFractions();
            colors = source.getGradient().getColors();
        }

        public Glyph doCreateGlyph(){
            LGRectangle retval = new LGRectangle(0, 0, 0, width, height, new LinearGradientPaint(startPoint, endPoint, fractions, colors));
            return retval;
        }
    }
    
}
