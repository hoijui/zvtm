/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class Rainbow2Filter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.06270f, .00000f, .06270f);
        map[2] = new Color(.12550f, .00000f, .12550f);
        map[3] = new Color(.18820f, .00000f, .18820f);
        map[4] = new Color(.25100f, .00000f, .25100f);
        map[5] = new Color(.31370f, .00000f, .31370f);
        map[6] = new Color(.37650f, .00000f, .37650f);
        map[7] = new Color(.43920f, .00000f, .43920f);
        map[8] = new Color(.50200f, .00000f, .50200f);
        map[9] = new Color(.56470f, .00000f, .56470f);
        map[10] = new Color(.62750f, .00000f, .62750f);
        map[11] = new Color(.69020f, .00000f, .69020f);
        map[12] = new Color(.75290f, .00000f, .75290f);
        map[13] = new Color(.81570f, .00000f, .81570f);
        map[14] = new Color(.87840f, .00000f, .87840f);
        map[15] = new Color(.94120f, .00000f, .94120f);
        map[16] = new Color(1.00000f, .00000f, 1.00000f);
        map[17] = new Color(.93730f, .00000f, 1.00000f);
        map[18] = new Color(.87450f, .00000f, 1.00000f);
        map[19] = new Color(.81180f, .00000f, 1.00000f);
        map[20] = new Color(.74900f, .00000f, 1.00000f);
        map[21] = new Color(.68630f, .00000f, 1.00000f);
        map[22] = new Color(.62350f, .00000f, 1.00000f);
        map[23] = new Color(.56080f, .00000f, 1.00000f);
        map[24] = new Color(.49800f, .00000f, 1.00000f);
        map[25] = new Color(.43530f, .00000f, 1.00000f);
        map[26] = new Color(.37250f, .00000f, 1.00000f);
        map[27] = new Color(.30980f, .00000f, 1.00000f);
        map[28] = new Color(.24710f, .00000f, 1.00000f);
        map[29] = new Color(.18430f, .00000f, 1.00000f);
        map[30] = new Color(.12160f, .00000f, 1.00000f);
        map[31] = new Color(.05880f, .00000f, 1.00000f);
        map[32] = new Color(.00000f, .00000f, 1.00000f);
        map[33] = new Color(.00000f, .06270f, 1.00000f);
        map[34] = new Color(.00000f, .12550f, 1.00000f);
        map[35] = new Color(.00000f, .18820f, 1.00000f);
        map[36] = new Color(.00000f, .25100f, 1.00000f);
        map[37] = new Color(.00000f, .31370f, 1.00000f);
        map[38] = new Color(.00000f, .37650f, 1.00000f);
        map[39] = new Color(.00000f, .43920f, 1.00000f);
        map[40] = new Color(.00000f, .50200f, 1.00000f);
        map[41] = new Color(.00000f, .56470f, 1.00000f);
        map[42] = new Color(.00000f, .62750f, 1.00000f);
        map[43] = new Color(.00000f, .69020f, 1.00000f);
        map[44] = new Color(.00000f, .75290f, 1.00000f);
        map[45] = new Color(.00000f, .81570f, 1.00000f);
        map[46] = new Color(.00000f, .87840f, 1.00000f);
        map[47] = new Color(.00000f, .94120f, 1.00000f);
        map[48] = new Color(.00000f, 1.00000f, 1.00000f);
        map[49] = new Color(.00000f, 1.00000f, .93730f);
        map[50] = new Color(.00000f, 1.00000f, .87450f);
        map[51] = new Color(.00000f, 1.00000f, .81180f);
        map[52] = new Color(.00000f, 1.00000f, .74900f);
        map[53] = new Color(.00000f, 1.00000f, .68630f);
        map[54] = new Color(.00000f, 1.00000f, .62350f);
        map[55] = new Color(.00000f, 1.00000f, .56080f);
        map[56] = new Color(.00000f, 1.00000f, .49800f);
        map[57] = new Color(.00000f, 1.00000f, .43530f);
        map[58] = new Color(.00000f, 1.00000f, .37250f);
        map[59] = new Color(.00000f, 1.00000f, .30980f);
        map[60] = new Color(.00000f, 1.00000f, .24710f);
        map[61] = new Color(.00000f, 1.00000f, .18430f);
        map[62] = new Color(.00000f, 1.00000f, .12160f);
        map[63] = new Color(.00000f, 1.00000f, .05880f);
        map[64] = new Color(.00000f, 1.00000f, .00000f);
        map[65] = new Color(.06270f, 1.00000f, .00000f);
        map[66] = new Color(.12550f, 1.00000f, .00000f);
        map[67] = new Color(.18820f, 1.00000f, .00000f);
        map[68] = new Color(.25100f, 1.00000f, .00000f);
        map[69] = new Color(.31370f, 1.00000f, .00000f);
        map[70] = new Color(.37650f, 1.00000f, .00000f);
        map[71] = new Color(.43920f, 1.00000f, .00000f);
        map[72] = new Color(.50200f, 1.00000f, .00000f);
        map[73] = new Color(.56470f, 1.00000f, .00000f);
        map[74] = new Color(.62750f, 1.00000f, .00000f);
        map[75] = new Color(.69020f, 1.00000f, .00000f);
        map[76] = new Color(.75290f, 1.00000f, .00000f);
        map[77] = new Color(.81570f, 1.00000f, .00000f);
        map[78] = new Color(.87840f, 1.00000f, .00000f);
        map[79] = new Color(.94120f, 1.00000f, .00000f);
        map[80] = new Color(1.00000f, 1.00000f, .00000f);
        map[81] = new Color(1.00000f, .96860f, .00000f);
        map[82] = new Color(1.00000f, .93730f, .00000f);
        map[83] = new Color(1.00000f, .90590f, .00000f);
        map[84] = new Color(1.00000f, .87450f, .00000f);
        map[85] = new Color(1.00000f, .84310f, .00000f);
        map[86] = new Color(1.00000f, .81180f, .00000f);
        map[87] = new Color(1.00000f, .78040f, .00000f);
        map[88] = new Color(1.00000f, .74900f, .00000f);
        map[89] = new Color(1.00000f, .71760f, .00000f);
        map[90] = new Color(1.00000f, .68630f, .00000f);
        map[91] = new Color(1.00000f, .65490f, .00000f);
        map[92] = new Color(1.00000f, .62350f, .00000f);
        map[93] = new Color(1.00000f, .59220f, .00000f);
        map[94] = new Color(1.00000f, .56080f, .00000f);
        map[95] = new Color(1.00000f, .52940f, .00000f);
        map[96] = new Color(1.00000f, .49800f, .00000f);
        map[97] = new Color(1.00000f, .46670f, .00000f);
        map[98] = new Color(1.00000f, .43530f, .00000f);
        map[99] = new Color(1.00000f, .40390f, .00000f);
        map[100] = new Color(1.00000f, .37250f, .00000f);
        map[101] = new Color(1.00000f, .34120f, .00000f);
        map[102] = new Color(1.00000f, .30980f, .00000f);
        map[103] = new Color(1.00000f, .27840f, .00000f);
        map[104] = new Color(1.00000f, .24710f, .00000f);
        map[105] = new Color(1.00000f, .21570f, .00000f);
        map[106] = new Color(1.00000f, .18430f, .00000f);
        map[107] = new Color(1.00000f, .15290f, .00000f);
        map[108] = new Color(1.00000f, .12160f, .00000f);
        map[109] = new Color(1.00000f, .09020f, .00000f);
        map[110] = new Color(1.00000f, .05880f, .00000f);
        map[111] = new Color(1.00000f, .02750f, .00000f);
        map[112] = new Color(1.00000f, .00000f, .00000f);
        map[113] = new Color(1.00000f, .06270f, .06270f);
        map[114] = new Color(1.00000f, .12550f, .12550f);
        map[115] = new Color(1.00000f, .18820f, .18820f);
        map[116] = new Color(1.00000f, .25100f, .25100f);
        map[117] = new Color(1.00000f, .31370f, .31370f);
        map[118] = new Color(1.00000f, .37650f, .37650f);
        map[119] = new Color(1.00000f, .43920f, .43920f);
        map[120] = new Color(1.00000f, .50200f, .50200f);
        map[121] = new Color(1.00000f, .56470f, .56470f);
        map[122] = new Color(1.00000f, .62750f, .62750f);
        map[123] = new Color(1.00000f, .69020f, .69020f);
        map[124] = new Color(1.00000f, .75290f, .75290f);
        map[125] = new Color(1.00000f, .81570f, .81570f);
        map[126] = new Color(1.00000f, .87840f, .87840f);
        map[127] = new Color(1.00000f, .94120f, .94120f);
    }

    public Rainbow2Filter(){}

    public int filterRGB(int x, int y, int rgb){
        return map[(rgb & 0xff)/2].getRGB();
    }

    public LinearGradientPaint getGradient(float w){
        return getGradientS(w);
    }

    public static LinearGradientPaint getGradientS(float w){
        float[] fractions = new float[map.length];
        for (int i=0;i<fractions.length;i++){
            fractions[i] = i / (float)fractions.length;
        }
        return new LinearGradientPaint(0, 0, w, 0, fractions, map);
    }

}
