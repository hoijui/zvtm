/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class Idl14Filter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00000f, .33330f, .00000f);
        map[2] = new Color(.00000f, .66670f, .00000f);
        map[3] = new Color(.00000f, 1.00000f, .00000f);
        map[4] = new Color(.00000f, .92550f, .00000f);
        map[5] = new Color(.00000f, .84710f, .00000f);
        map[6] = new Color(.00000f, .77250f, .00000f);
        map[7] = new Color(.00000f, .69410f, .00000f);
        map[8] = new Color(.00000f, .61570f, .00000f);
        map[9] = new Color(.00000f, .54120f, .00000f);
        map[10] = new Color(.00000f, .46270f, .00000f);
        map[11] = new Color(.00000f, .38820f, .00000f);
        map[12] = new Color(.00000f, .30980f, .00000f);
        map[13] = new Color(.00000f, .23140f, .00000f);
        map[14] = new Color(.00000f, .15690f, .00000f);
        map[15] = new Color(.00000f, .07840f, .00000f);
        map[16] = new Color(.00000f, .00000f, .00000f);
        map[17] = new Color(.00000f, .00000f, .06270f);
        map[18] = new Color(.00000f, .00000f, .12550f);
        map[19] = new Color(.00000f, .00000f, .19220f);
        map[20] = new Color(.00000f, .00000f, .25490f);
        map[21] = new Color(.00000f, .00000f, .32160f);
        map[22] = new Color(.00000f, .00000f, .38430f);
        map[23] = new Color(.00000f, .00000f, .45100f);
        map[24] = new Color(.00000f, .00000f, .51370f);
        map[25] = new Color(.00000f, .00000f, .58040f);
        map[26] = new Color(.00000f, .00000f, .64310f);
        map[27] = new Color(.00000f, .00000f, .70590f);
        map[28] = new Color(.00000f, .00000f, .77250f);
        map[29] = new Color(.00000f, .00000f, .83530f);
        map[30] = new Color(.00000f, .00000f, .90200f);
        map[31] = new Color(.00000f, .00000f, .96470f);
        map[32] = new Color(.00000f, .00000f, .00000f);
        map[33] = new Color(.05880f, .00000f, .03920f);
        map[34] = new Color(.12160f, .00000f, .08240f);
        map[35] = new Color(.18430f, .00000f, .12160f);
        map[36] = new Color(.24710f, .00000f, .16470f);
        map[37] = new Color(.30590f, .00000f, .20780f);
        map[38] = new Color(.36860f, .00000f, .24710f);
        map[39] = new Color(.43140f, .00000f, .29020f);
        map[40] = new Color(.49410f, .00000f, .33330f);
        map[41] = new Color(.55690f, .00000f, .36860f);
        map[42] = new Color(.62350f, .00000f, .40390f);
        map[43] = new Color(.69020f, .00000f, .43920f);
        map[44] = new Color(.75690f, .00000f, .47450f);
        map[45] = new Color(.82350f, .00000f, .51370f);
        map[46] = new Color(.88630f, .00000f, .54900f);
        map[47] = new Color(.95290f, .00000f, .58430f);
        map[48] = new Color(.00000f, .00000f, .00000f);
        map[49] = new Color(.00780f, .00000f, .00000f);
        map[50] = new Color(.01570f, .00000f, .00000f);
        map[51] = new Color(.02350f, .00000f, .00000f);
        map[52] = new Color(.03140f, .00000f, .00000f);
        map[53] = new Color(.03920f, .00000f, .00000f);
        map[54] = new Color(.04710f, .00000f, .00000f);
        map[55] = new Color(.06270f, .00000f, .00000f);
        map[56] = new Color(.07840f, .00000f, .00000f);
        map[57] = new Color(.09800f, .00000f, .00000f);
        map[58] = new Color(.11370f, .00000f, .00000f);
        map[59] = new Color(.13330f, .00000f, .00000f);
        map[60] = new Color(.14900f, .00000f, .00000f);
        map[61] = new Color(.17250f, .00000f, .00000f);
        map[62] = new Color(.19610f, .00000f, .00000f);
        map[63] = new Color(.21960f, .00000f, .00000f);
        map[64] = new Color(.24710f, .00000f, .00000f);
        map[65] = new Color(.27060f, .00000f, .00000f);
        map[66] = new Color(.29410f, .00000f, .00000f);
        map[67] = new Color(.32160f, .00000f, .00390f);
        map[68] = new Color(.34900f, .00000f, .00390f);
        map[69] = new Color(.38040f, .00000f, .00390f);
        map[70] = new Color(.41180f, .00000f, .00390f);
        map[71] = new Color(.43920f, .00000f, .00390f);
        map[72] = new Color(.47060f, .00000f, .00390f);
        map[73] = new Color(.50200f, .00390f, .00390f);
        map[74] = new Color(.52940f, .00390f, .00390f);
        map[75] = new Color(.56080f, .00390f, .00390f);
        map[76] = new Color(.59220f, .00390f, .00390f);
        map[77] = new Color(.62350f, .00390f, .00390f);
        map[78] = new Color(.65490f, .00390f, .00390f);
        map[79] = new Color(.68630f, .00390f, .00390f);
        map[80] = new Color(.70980f, .00390f, .00390f);
        map[81] = new Color(.73730f, .00390f, .00390f);
        map[82] = new Color(.76470f, .00390f, .00390f);
        map[83] = new Color(.79220f, .00390f, .00390f);
        map[84] = new Color(.81960f, .00390f, .00390f);
        map[85] = new Color(.84710f, .00390f, .00780f);
        map[86] = new Color(.86270f, .00390f, .00780f);
        map[87] = new Color(.88240f, .00390f, .00780f);
        map[88] = new Color(.90200f, .00390f, .00780f);
        map[89] = new Color(.91760f, .00390f, .00780f);
        map[90] = new Color(.93730f, .00390f, .00780f);
        map[91] = new Color(.95690f, .00780f, .00780f);
        map[92] = new Color(.96080f, .00780f, .00780f);
        map[93] = new Color(.96860f, .00780f, .00780f);
        map[94] = new Color(.97250f, .00780f, .00780f);
        map[95] = new Color(.98040f, .00780f, .00780f);
        map[96] = new Color(.98430f, .00780f, .00780f);
        map[97] = new Color(.99220f, .00780f, .00780f);
        map[98] = new Color(.99610f, .00390f, .00780f);
        map[99] = new Color(.99610f, .01960f, .00780f);
        map[100] = new Color(.99610f, .03920f, .00780f);
        map[101] = new Color(.99610f, .05880f, .00780f);
        map[102] = new Color(.99610f, .07450f, .00780f);
        map[103] = new Color(.99610f, .09410f, .00780f);
        map[104] = new Color(.99610f, .11370f, .00780f);
        map[105] = new Color(.99610f, .12940f, .00780f);
        map[106] = new Color(.99610f, .14120f, .00780f);
        map[107] = new Color(.99610f, .15690f, .01570f);
        map[108] = new Color(1.00000f, .17650f, .02750f);
        map[109] = new Color(1.00000f, .20000f, .04710f);
        map[110] = new Color(1.00000f, .22750f, .06670f);
        map[111] = new Color(1.00000f, .25100f, .09020f);
        map[112] = new Color(1.00000f, .27450f, .11760f);
        map[113] = new Color(1.00000f, .30200f, .15290f);
        map[114] = new Color(1.00000f, .34120f, .19220f);
        map[115] = new Color(1.00000f, .37650f, .23530f);
        map[116] = new Color(1.00000f, .40780f, .27840f);
        map[117] = new Color(1.00000f, .44310f, .32160f);
        map[118] = new Color(1.00000f, .49020f, .38040f);
        map[119] = new Color(1.00000f, .54120f, .43920f);
        map[120] = new Color(1.00000f, .59220f, .50200f);
        map[121] = new Color(1.00000f, .64310f, .56860f);
        map[122] = new Color(1.00000f, .69800f, .63530f);
        map[123] = new Color(1.00000f, .75690f, .70590f);
        map[124] = new Color(1.00000f, .81570f, .77650f);
        map[125] = new Color(1.00000f, .87450f, .85100f);
        map[126] = new Color(1.00000f, .92160f, .90980f);
        map[127] = new Color(1.00000f, .97250f, .96860f);
    }

    public Idl14Filter(){}

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
