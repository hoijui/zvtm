# The program takes an image (make sure it is not too big -- 100x100
# is probably a maximum) and generates a zvtm-cluster scene where
# each pixel of the original image is replaced by a 'primitive' image.
#
# Example use: jruby -J-Djava.n.preferIPv4Stack=true -J-Djgroups.bind_addr="192.168.0.87" imager.rb test.jpg

require 'java'
require 'image_distance'

java_import 'fr.inria.zvtm.glyphs.VImage'
java_import 'java.io.File'
java_import 'javax.imageio.ImageIO'
java_import 'javax.swing.ImageIcon'
java_import 'javax.swing.SwingUtilities'

java_import 'fr.inria.zvtm.event.ViewAdapter'

['VirtualSpaceManager', 'VirtualSpace', 'View', 'Camera'].each do |name|
    java_import "fr.inria.zvtm.engine.#{name}"
end

['ClusteredView', 'ClusterGeometry'].each do |name|
    java_import "fr.inria.zvtm.cluster.#{name}"
end

# Returns a BufferedImage instace given a file path
def get_buf_img(image_file)
    ImageIO.read File.new(image_file)
end

def process_pixels(image_file, space)
   bimg = get_buf_img(image_file)
   for col in (0...bimg.getWidth)
       for row in (0...bimg.getHeight)
           posx = 75*col
           posy = -75*row
           cl_img = ImageDistance.closest_image Color.new(bimg.getRGB(col, row))
           vimg = VImage.new(posx, posy, 0, ImageIcon.new("images/" + cl_img).getImage)
           vimg.setDrawBorder false
           space.addGlyph vimg 
       end
   end
end

class MyViewListener < ViewAdapter
    def set(view, cam)
        @view = view
        @cam = cam
        @lastJPX = 0
        @lastJPY = 0
    end

    def press3(v, mod, jpx, jpy, e)
        @lastJPX=jpx;
        @lastJPY=jpy;
        @view.mouse.setSensitivity false
    end

    def release3(v, mod, jpx, jpy, e)
        VirtualSpaceManager::INSTANCE.getAnimationManager.setXspeed 0
        VirtualSpaceManager::INSTANCE.getAnimationManager.setYspeed 0
        VirtualSpaceManager::INSTANCE.getAnimationManager.setZspeed 0
        @view.mouse.setSensitivity true
    end

    def mouseDragged(v, mod, buttonNumber, jpx, jpy, e)
        a = (@cam.focal+@cam.altitude)/@cam.focal
        VirtualSpaceManager::INSTANCE.getAnimationManager.setXspeed((@cam.altitude>0) ? (jpx-@lastJPX)*(a/4.0) : (jpx-@lastJPX)/(a*4))
        VirtualSpaceManager::INSTANCE.getAnimationManager.setYspeed((@cam.altitude>0) ? (@lastJPY-jpy)*(a/4.0) : (@lastJPY-jpy)/(a*4))
    end

    def viewClosing(v)
        VirtualSpaceManager::INSTANCE.stop
        java.lang.System.exit 0
    end
end

def main
    vsm = VirtualSpaceManager::INSTANCE
    vsm.setMaster "Imager"
    space = vsm.addVirtualSpace "ImageSpace" 
    cam = space.addCamera
    view = vsm.addFrameView [cam], "imager", View::STD_VIEW, 800, 
        600, false, true
    view.getCursor.setColor Color::GREEN
    clg = ClusterGeometry.new 2560, 1600, 8, 4
    clv = ClusteredView.new clg, 3, 8, 4, [cam]
    clv.setBackgroundColor Color::BLACK
    vsm.addClusteredView clv
    vl = MyViewListener.new
    vl.set view, cam
    view.setListener(vl)

    puts 'done initializing the scene'
    
    puts 'now building the image'
    puts ARGV[0]
    process_pixels ARGV[0], space 
    view.getGlobalView cam, 500
end

main

