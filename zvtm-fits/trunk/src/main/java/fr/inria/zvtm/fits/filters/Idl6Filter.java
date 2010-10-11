/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class Idl6Filter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.02750f, .00000f, .00000f);
        map[2] = new Color(.05880f, .00000f, .00000f);
        map[3] = new Color(.08630f, .00000f, .00000f);
        map[4] = new Color(.11760f, .00000f, .00000f);
        map[5] = new Color(.14900f, .00000f, .00000f);
        map[6] = new Color(.17650f, .00000f, .00000f);
        map[7] = new Color(.20780f, .00000f, .00000f);
        map[8] = new Color(.23530f, .00000f, .00000f);
        map[9] = new Color(.26670f, .00000f, .00000f);
        map[10] = new Color(.29800f, .00000f, .00000f);
        map[11] = new Color(.32550f, .00000f, .00000f);
        map[12] = new Color(.35690f, .00000f, .00000f);
        map[13] = new Color(.38430f, .00000f, .00000f);
        map[14] = new Color(.41570f, .00000f, .00000f);
        map[15] = new Color(.44710f, .00000f, .00000f);
        map[16] = new Color(.47450f, .00000f, .00000f);
        map[17] = new Color(.50590f, .00000f, .00000f);
        map[18] = new Color(.53730f, .00000f, .00000f);
        map[19] = new Color(.56470f, .00000f, .00000f);
        map[20] = new Color(.59610f, .00000f, .00000f);
        map[21] = new Color(.62350f, .00000f, .00000f);
        map[22] = new Color(.65490f, .00000f, .00000f);
        map[23] = new Color(.68630f, .00000f, .00000f);
        map[24] = new Color(.71370f, .00000f, .00000f);
        map[25] = new Color(.74510f, .00000f, .00000f);
        map[26] = new Color(.77250f, .00000f, .00000f);
        map[27] = new Color(.80390f, .00000f, .00000f);
        map[28] = new Color(.83530f, .00000f, .00000f);
        map[29] = new Color(.86270f, .00000f, .00000f);
        map[30] = new Color(.89410f, .00000f, .00000f);
        map[31] = new Color(.92160f, .00000f, .00000f);
        map[32] = new Color(.95290f, .00000f, .00000f);
        map[33] = new Color(.98430f, .02750f, .00000f);
        map[34] = new Color(.98430f, .05880f, .00000f);
        map[35] = new Color(.95290f, .09020f, .00000f);
        map[36] = new Color(.92160f, .12160f, .00000f);
        map[37] = new Color(.88630f, .15290f, .00000f);
        map[38] = new Color(.85490f, .18430f, .00000f);
        map[39] = new Color(.82350f, .21570f, .00000f);
        map[40] = new Color(.78820f, .24710f, .00000f);
        map[41] = new Color(.75690f, .27840f, .00000f);
        map[42] = new Color(.72160f, .30980f, .00000f);
        map[43] = new Color(.69020f, .34120f, .00000f);
        map[44] = new Color(.65880f, .37250f, .00000f);
        map[45] = new Color(.62350f, .40390f, .00000f);
        map[46] = new Color(.59220f, .43530f, .00000f);
        map[47] = new Color(.56080f, .46670f, .00000f);
        map[48] = new Color(.52550f, .49800f, .00000f);
        map[49] = new Color(.49410f, .52940f, .00000f);
        map[50] = new Color(.46270f, .56080f, .00000f);
        map[51] = new Color(.42750f, .59220f, .00000f);
        map[52] = new Color(.39610f, .62350f, .00000f);
        map[53] = new Color(.36080f, .65490f, .00000f);
        map[54] = new Color(.32940f, .68630f, .00000f);
        map[55] = new Color(.29800f, .71760f, .00000f);
        map[56] = new Color(.26270f, .74900f, .00000f);
        map[57] = new Color(.23140f, .78040f, .00000f);
        map[58] = new Color(.20000f, .81180f, .00000f);
        map[59] = new Color(.16470f, .84310f, .00000f);
        map[60] = new Color(.13330f, .87450f, .00000f);
        map[61] = new Color(.10200f, .90590f, .00000f);
        map[62] = new Color(.06670f, .93730f, .00000f);
        map[63] = new Color(.03530f, .96860f, .00000f);
        map[64] = new Color(.00000f, 1.00000f, .02750f);
        map[65] = new Color(.00000f, .96860f, .05880f);
        map[66] = new Color(.00000f, .93730f, .09020f);
        map[67] = new Color(.00000f, .90590f, .11760f);
        map[68] = new Color(.00000f, .87450f, .14900f);
        map[69] = new Color(.00000f, .84310f, .18040f);
        map[70] = new Color(.00000f, .81180f, .21180f);
        map[71] = new Color(.00000f, .78040f, .23920f);
        map[72] = new Color(.00000f, .74900f, .27060f);
        map[73] = new Color(.00000f, .71760f, .30200f);
        map[74] = new Color(.00000f, .68630f, .33330f);
        map[75] = new Color(.00000f, .65100f, .36080f);
        map[76] = new Color(.00000f, .61960f, .39220f);
        map[77] = new Color(.00000f, .58820f, .42350f);
        map[78] = new Color(.00000f, .55690f, .45100f);
        map[79] = new Color(.00000f, .52550f, .48240f);
        map[80] = new Color(.00000f, .49410f, .51370f);
        map[81] = new Color(.00000f, .46270f, .54510f);
        map[82] = new Color(.00000f, .43140f, .57250f);
        map[83] = new Color(.00000f, .40000f, .60390f);
        map[84] = new Color(.00000f, .36860f, .63530f);
        map[85] = new Color(.00000f, .33330f, .66670f);
        map[86] = new Color(.00000f, .30200f, .69410f);
        map[87] = new Color(.00000f, .27060f, .72550f);
        map[88] = new Color(.00000f, .23920f, .75690f);
        map[89] = new Color(.00000f, .20780f, .78430f);
        map[90] = new Color(.00000f, .17650f, .81570f);
        map[91] = new Color(.00000f, .14510f, .84710f);
        map[92] = new Color(.00000f, .11370f, .87840f);
        map[93] = new Color(.00000f, .08240f, .90590f);
        map[94] = new Color(.00000f, .05100f, .93730f);
        map[95] = new Color(.00000f, .01960f, .96860f);
        map[96] = new Color(.00000f, .00000f, 1.00000f);
        map[97] = new Color(.00000f, .00000f, .96860f);
        map[98] = new Color(.00000f, .00000f, .93730f);
        map[99] = new Color(.00000f, .00000f, .90590f);
        map[100] = new Color(.00000f, .00000f, .87450f);
        map[101] = new Color(.00000f, .00000f, .84310f);
        map[102] = new Color(.00000f, .00000f, .81180f);
        map[103] = new Color(.00000f, .00000f, .78040f);
        map[104] = new Color(.00000f, .00000f, .74900f);
        map[105] = new Color(.00000f, .00000f, .71760f);
        map[106] = new Color(.00000f, .00000f, .68630f);
        map[107] = new Color(.00000f, .00000f, .65100f);
        map[108] = new Color(.00000f, .00000f, .61960f);
        map[109] = new Color(.00000f, .00000f, .58820f);
        map[110] = new Color(.00000f, .00000f, .55690f);
        map[111] = new Color(.00000f, .00000f, .52550f);
        map[112] = new Color(.00000f, .00000f, .49410f);
        map[113] = new Color(.00000f, .00000f, .46270f);
        map[114] = new Color(.00000f, .00000f, .43140f);
        map[115] = new Color(.00000f, .00000f, .40000f);
        map[116] = new Color(.00000f, .00000f, .36860f);
        map[117] = new Color(.00000f, .00000f, .33330f);
        map[118] = new Color(.00000f, .00000f, .30200f);
        map[119] = new Color(.00000f, .00000f, .27060f);
        map[120] = new Color(.00000f, .00000f, .23920f);
        map[121] = new Color(.00000f, .00000f, .20780f);
        map[122] = new Color(.00000f, .00000f, .17650f);
        map[123] = new Color(.00000f, .00000f, .14510f);
        map[124] = new Color(.00000f, .00000f, .11370f);
        map[125] = new Color(.00000f, .00000f, .08240f);
        map[126] = new Color(.00000f, .00000f, .05100f);
        map[127] = new Color(.00000f, .00000f, .01960f);
    }

    public Idl6Filter(){}

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
