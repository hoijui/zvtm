require 'java'

java_import 'java.awt.Color'

module ImageDistance
    # Returns an RGB color given a string representing the hex value of the
    # color. For instance c("ff0000") would return a red Color.
    def ImageDistance.c(string)
        Color.new(string[0..1].to_i(16), string[2..3].to_i(16), string[4..5].to_i(16))
    end

    # A set of primitive images
    def ImageDistance.images
        {
            c("bb1907"), "red.jpg",
            c("3626af"), "blue.jpg",
            c("f2d435"), "yellow.jpg",
            c("e4e4e4"), "light_gray.jpg",
            c("b5d267"), "light_green.jpg",
            c("363636"), "dark_gray.jpg",
            c("c73e87"), "purple.jpg", 
            c("b0cdfc"), "light_blue.jpg",
            c("e5734c"), "terracotta.jpg",
            c("667932"), "dark_green.jpg",
            c("201974"), "marine.jpg",
            c("565215"), "khaki.jpg",
            c("909090"), "gray_50.jpg",
            c("f1bbab"), "pink.jpg",
            c("000435"), "blue_black.jpg",
            c("6e4010"), "brown.jpg",
            c("ebe54c"), "yellow_green.jpg"
        }
    end

    # Given a color, returns the closest image in the image set
    def ImageDistance.closest_image(color)
        min_distance = Float::MAX
        ret = nil
        images.each_pair do |key, value|
            if distance2(color, key) < min_distance
                ret = value
                min_distance = distance2(color, key)
            end
        end
        ret
    end

    # Returns the distance between two colors
    # --
    # TODO: this could be improved (e.g. use Lab distance?)
    # ++
    def ImageDistance.distance(color1, color2)
        return 0.3*(color1.getRed-color2.getRed)*(color1.getRed-color2.getRed) + 0.6*(color1.getGreen-color2.getGreen)*(color1.getGreen-color2.getGreen) + 0.1*(color1.getBlue-color2.getBlue)*(color1.getBlue-color2.getBlue)
    end

    # Returns the distance between two colors
    # adapted from http://www.compuphase.com/cmetric.htm
    def ImageDistance.distance2(color1, color2)
        rmean = (color1.getRed + color2.getRed) / 2
        r = color1.getRed - color2.getRed
        g = color1.getGreen - color2.getGreen
        b = color1.getBlue - color2.getBlue 
        return ((512+rmean)*r*r)/256 + 4*g*g + (767-rmean)*b*b/256
    end
end
