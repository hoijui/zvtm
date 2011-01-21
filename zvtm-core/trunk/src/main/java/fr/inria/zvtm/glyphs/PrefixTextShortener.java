package fr.inria.zvtm.glyphs;

public class PrefixTextShortener implements TextShortener {
    /**
     * Singleton PrefixTextShortener.
     * This class is thread-safe.
     */
    public static final PrefixTextShortener INSTANCE = 
        new PrefixTextShortener();

    private PrefixTextShortener(){}

    /**
     * @inheritDoc
     */
    public String shorten(String source, int len){
        if(len < 0){
            throw new IllegalArgumentException("len should be positive");
        }

        if(len >= source.length()){
            return source;
        }

        return source.substring(0, len);
    }
} 

