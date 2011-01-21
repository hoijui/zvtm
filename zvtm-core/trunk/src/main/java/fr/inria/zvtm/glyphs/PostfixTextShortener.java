package fr.inria.zvtm.glyphs;

public class PostfixTextShortener implements TextShortener {
    public static final PostfixTextShortener INSTANCE = 
        new PostfixTextShortener();

    private PostfixTextShortener(){}

    /**
     * @inheritDoc
     */
    public String shorten(String source, int len){
        if(len < 0){
            throw new IllegalArgumentException("len should be positive");
        }
    
        int slen = source.length();
        if(len >= slen){
            return source;
        }

        return source.substring(slen-len, slen);
    }
}

