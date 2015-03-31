/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class Pseudo1Filter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, 1.00000f);
        map[1] = new Color(.00000f, .00000f, .98820f);
        map[2] = new Color(.00390f, .00000f, .98040f);
        map[3] = new Color(.00780f, .00000f, .96860f);
        map[4] = new Color(.01570f, .00000f, .96080f);
        map[5] = new Color(.01960f, .00000f, .95290f);
        map[6] = new Color(.02350f, .00780f, .94120f);
        map[7] = new Color(.02750f, .02750f, .93330f);
        map[8] = new Color(.03530f, .05100f, .92160f);
        map[9] = new Color(.03920f, .08240f, .91370f);
        map[10] = new Color(.04710f, .11760f, .90590f);
        map[11] = new Color(.05100f, .08240f, .89410f);
        map[12] = new Color(.05490f, .05100f, .88630f);
        map[13] = new Color(.06270f, .02750f, .87840f);
        map[14] = new Color(.07060f, .00780f, .86670f);
        map[15] = new Color(.07450f, .00000f, .85880f);
        map[16] = new Color(.08240f, .01180f, .85100f);
        map[17] = new Color(.08630f, .03920f, .83920f);
        map[18] = new Color(.09410f, .07060f, .83140f);
        map[19] = new Color(.09800f, .10980f, .82350f);
        map[20] = new Color(.10590f, .15690f, .81180f);
        map[21] = new Color(.11370f, .10980f, .80390f);
        map[22] = new Color(.11760f, .07060f, .79610f);
        map[23] = new Color(.12550f, .03920f, .78430f);
        map[24] = new Color(.13330f, .01180f, .77650f);
        map[25] = new Color(.14120f, .00000f, .76860f);
        map[26] = new Color(.14510f, .01570f, .75690f);
        map[27] = new Color(.15290f, .04710f, .74900f);
        map[28] = new Color(.16080f, .09020f, .74120f);
        map[29] = new Color(.16860f, .13730f, .73330f);
        map[30] = new Color(.17250f, .19610f, .72160f);
        map[31] = new Color(.18040f, .13730f, .71370f);
        map[32] = new Color(.18820f, .09020f, .70590f);
        map[33] = new Color(.19610f, .04710f, .69800f);
        map[34] = new Color(.20390f, .01570f, .68630f);
        map[35] = new Color(.21180f, .00000f, .67840f);
        map[36] = new Color(.21570f, .02350f, .67060f);
        map[37] = new Color(.22350f, .06670f, .66270f);
        map[38] = new Color(.23140f, .12550f, .65100f);
        map[39] = new Color(.23920f, .19610f, .64310f);
        map[40] = new Color(.24710f, .27450f, .63530f);
        map[41] = new Color(.25490f, .19610f, .62750f);
        map[42] = new Color(.26270f, .12550f, .61570f);
        map[43] = new Color(.27060f, .06670f, .60780f);
        map[44] = new Color(.27840f, .02350f, .60000f);
        map[45] = new Color(.28630f, .00000f, .59220f);
        map[46] = new Color(.29410f, .03140f, .58430f);
        map[47] = new Color(.29800f, .08630f, .57250f);
        map[48] = new Color(.30590f, .16080f, .56470f);
        map[49] = new Color(.31370f, .25100f, .55690f);
        map[50] = new Color(.32160f, .35290f, .54900f);
        map[51] = new Color(.32940f, .25100f, .54120f);
        map[52] = new Color(.33730f, .16080f, .52940f);
        map[53] = new Color(.34510f, .08630f, .52160f);
        map[54] = new Color(.35290f, .03140f, .51370f);
        map[55] = new Color(.36080f, .00000f, .50590f);
        map[56] = new Color(.37250f, .03530f, .49800f);
        map[57] = new Color(.38040f, .10590f, .49020f);
        map[58] = new Color(.38820f, .20000f, .48240f);
        map[59] = new Color(.39610f, .30590f, .47060f);
        map[60] = new Color(.40390f, .43140f, .46270f);
        map[61] = new Color(.41180f, .30590f, .45490f);
        map[62] = new Color(.41960f, .20000f, .44710f);
        map[63] = new Color(.42750f, .10590f, .43920f);
        map[64] = new Color(.43530f, .03530f, .43140f);
        map[65] = new Color(.44310f, .00000f, .42350f);
        map[66] = new Color(.45100f, .04310f, .41570f);
        map[67] = new Color(.45880f, .12550f, .40780f);
        map[68] = new Color(.46670f, .23530f, .40000f);
        map[69] = new Color(.47840f, .36470f, .39220f);
        map[70] = new Color(.48630f, .50980f, .38430f);
        map[71] = new Color(.49410f, .36470f, .37650f);
        map[72] = new Color(.50200f, .23530f, .36470f);
        map[73] = new Color(.50980f, .12550f, .35690f);
        map[74] = new Color(.51760f, .04310f, .34900f);
        map[75] = new Color(.52550f, .00000f, .34120f);
        map[76] = new Color(.53730f, .05100f, .33330f);
        map[77] = new Color(.54510f, .14510f, .32550f);
        map[78] = new Color(.55290f, .27060f, .31760f);
        map[79] = new Color(.56080f, .41960f, .30980f);
        map[80] = new Color(.56860f, .58820f, .30200f);
        map[81] = new Color(.57650f, .41960f, .29800f);
        map[82] = new Color(.58820f, .27060f, .29020f);
        map[83] = new Color(.59610f, .14510f, .28240f);
        map[84] = new Color(.60390f, .05100f, .27450f);
        map[85] = new Color(.61180f, .00000f, .26670f);
        map[86] = new Color(.61960f, .05880f, .25880f);
        map[87] = new Color(.63140f, .16860f, .25100f);
        map[88] = new Color(.63920f, .30980f, .24310f);
        map[89] = new Color(.64710f, .47450f, .23530f);
        map[90] = new Color(.65490f, .66670f, .22750f);
        map[91] = new Color(.66670f, .47450f, .21960f);
        map[92] = new Color(.67450f, .30980f, .21180f);
        map[93] = new Color(.68240f, .16860f, .20780f);
        map[94] = new Color(.69020f, .05880f, .20000f);
        map[95] = new Color(.70200f, .00000f, .19220f);
        map[96] = new Color(.70980f, .06270f, .18430f);
        map[97] = new Color(.71760f, .18820f, .17650f);
        map[98] = new Color(.72550f, .34510f, .17250f);
        map[99] = new Color(.73730f, .52940f, .16470f);
        map[100] = new Color(.74510f, .74510f, .15690f);
        map[101] = new Color(.75290f, .52940f, .14900f);
        map[102] = new Color(.76470f, .34510f, .14120f);
        map[103] = new Color(.77250f, .18820f, .13730f);
        map[104] = new Color(.78040f, .06270f, .12940f);
        map[105] = new Color(.79220f, .00000f, .12160f);
        map[106] = new Color(.80000f, .07060f, .11760f);
        map[107] = new Color(.80780f, .20780f, .10980f);
        map[108] = new Color(.81570f, .38040f, .10200f);
        map[109] = new Color(.82750f, .58820f, .09800f);
        map[110] = new Color(.83530f, .82350f, .09020f);
        map[111] = new Color(.84310f, .58820f, .08240f);
        map[112] = new Color(.85490f, .38040f, .07840f);
        map[113] = new Color(.86270f, .20780f, .07060f);
        map[114] = new Color(.87060f, .07060f, .06670f);
        map[115] = new Color(.88240f, .00000f, .05880f);
        map[116] = new Color(.89020f, .07840f, .05490f);
        map[117] = new Color(.90200f, .22750f, .04710f);
        map[118] = new Color(.90980f, .41570f, .04310f);
        map[119] = new Color(.91760f, .64310f, .03530f);
        map[120] = new Color(.92940f, .90200f, .03140f);
        map[121] = new Color(.93730f, .64310f, .02750f);
        map[122] = new Color(.94510f, .41570f, .01960f);
        map[123] = new Color(.95690f, .25880f, .01570f);
        map[124] = new Color(.96470f, .23920f, .01180f);
        map[125] = new Color(.97650f, .35290f, .00780f);
        map[126] = new Color(.98430f, .58430f, .00390f);
        map[127] = new Color(.99220f, .85100f, .00000f);
    }

    public Pseudo1Filter(){}

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
