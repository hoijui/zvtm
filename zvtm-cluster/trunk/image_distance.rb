require 'java'

java_import 'java.awt.Color'

module ImageDistance
    def ImageDistance.c(string)
        Color.new(string[0..1].to_i(16), string[2..3].to_i(16), string[4..5].to_i(16))
    end


    def ImageDistance.images
        {c("bb1907"), "red.jpg",
            c("3626af"), "blue.jpg",
            c("f2d435"), "yellow.jpg",
            c("e4e4e4"), "light_gray.jpg",
            c("b5d267"), "light_green.jpg",
            c("363636"), "dark_gray.jpg",
            c("c73e87"), "purple.jpg", 
            c("b0cdfc"), "light_blue.jpg",
            c("e5734c"), "terracotta.jpg",
            c("667932"), "dark_green.jpg"}
    end

    def ImageDistance.closest_image(color)
        min_distance = Float::MAX
        ret = nil
        images.each_pair do |key, value|
            if distance(color, key) < min_distance
                ret = value
                min_distance = distance(color, key)
            end
        end
        ret
    end

    def ImageDistance.distance(color1, color2)
        return 0.3*(color1.getRed-color2.getRed)*(color1.getRed-color2.getRed) + 0.6*(color1.getGreen-color2.getGreen)*(color1.getGreen-color2.getGreen) + 0.1*(color1.getBlue-color2.getBlue)*(color1.getBlue-color2.getBlue)
    end
end
