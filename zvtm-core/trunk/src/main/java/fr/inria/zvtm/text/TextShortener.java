package fr.inria.zvtm.text;

/**
 * Shortens a String to (at most) a given length.
 * Generic shortners are for instance a prefix or postfix shortner.
 * A specific shortner might be used to convert US state names to their
 * 2-letter abbreviation.
 */
public interface TextShortener {
    /**
     * Returns a string which is at most <code>len</code> characters
     * in length (but may be less).
     * @throw IllegalArgumentException - if <code>len</code> is negative.
     */
    public String shorten(String original, int len);
}

