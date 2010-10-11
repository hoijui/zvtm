/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class PastelFilter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.01960f, .00000f, .98040f);
        map[2] = new Color(.08630f, .00390f, .91370f);
        map[3] = new Color(.13730f, .00390f, .86270f);
        map[4] = new Color(.18040f, .00390f, .81960f);
        map[5] = new Color(.21570f, .00780f, .78430f);
        map[6] = new Color(.25100f, .00780f, .74900f);
        map[7] = new Color(.28240f, .01180f, .71760f);
        map[8] = new Color(.30590f, .01180f, .69410f);
        map[9] = new Color(.33330f, .01570f, .66670f);
        map[10] = new Color(.35290f, .01570f, .64710f);
        map[11] = new Color(.37250f, .01960f, .62750f);
        map[12] = new Color(.39220f, .01960f, .60780f);
        map[13] = new Color(.41180f, .02350f, .58820f);
        map[14] = new Color(.43140f, .02350f, .56860f);
        map[15] = new Color(.44310f, .02750f, .55690f);
        map[16] = new Color(.45880f, .02750f, .54120f);
        map[17] = new Color(.47060f, .03140f, .52940f);
        map[18] = new Color(.48630f, .03530f, .51370f);
        map[19] = new Color(.50200f, .03530f, .49800f);
        map[20] = new Color(.50980f, .04310f, .49020f);
        map[21] = new Color(.52160f, .04310f, .47840f);
        map[22] = new Color(.53730f, .04710f, .46270f);
        map[23] = new Color(.54900f, .05100f, .45100f);
        map[24] = new Color(.55690f, .05100f, .44310f);
        map[25] = new Color(.56470f, .05490f, .43530f);
        map[26] = new Color(.58040f, .05880f, .41960f);
        map[27] = new Color(.58820f, .05880f, .41180f);
        map[28] = new Color(.59610f, .06270f, .40390f);
        map[29] = new Color(.60390f, .06670f, .39610f);
        map[30] = new Color(.61180f, .06670f, .38820f);
        map[31] = new Color(.61960f, .07060f, .38040f);
        map[32] = new Color(.63140f, .07450f, .36860f);
        map[33] = new Color(.63920f, .07840f, .36080f);
        map[34] = new Color(.64710f, .08240f, .35290f);
        map[35] = new Color(.65490f, .08240f, .34510f);
        map[36] = new Color(.65880f, .08630f, .34120f);
        map[37] = new Color(.66670f, .09020f, .33330f);
        map[38] = new Color(.67840f, .09410f, .32160f);
        map[39] = new Color(.68240f, .09800f, .31760f);
        map[40] = new Color(.69020f, .10200f, .30980f);
        map[41] = new Color(.69410f, .10590f, .30590f);
        map[42] = new Color(.70200f, .10980f, .29800f);
        map[43] = new Color(.70590f, .10980f, .29410f);
        map[44] = new Color(.71370f, .11760f, .28630f);
        map[45] = new Color(.71760f, .12160f, .28240f);
        map[46] = new Color(.72160f, .12550f, .27840f);
        map[47] = new Color(.73330f, .12940f, .26670f);
        map[48] = new Color(.73730f, .13730f, .26270f);
        map[49] = new Color(.74120f, .14120f, .25880f);
        map[50] = new Color(.74510f, .14510f, .25490f);
        map[51] = new Color(.75290f, .14900f, .24710f);
        map[52] = new Color(.75690f, .15290f, .24310f);
        map[53] = new Color(.76080f, .15690f, .23920f);
        map[54] = new Color(.76470f, .16080f, .23530f);
        map[55] = new Color(.76860f, .16860f, .23140f);
        map[56] = new Color(.78040f, .17250f, .21960f);
        map[57] = new Color(.78430f, .17650f, .21570f);
        map[58] = new Color(.78820f, .18040f, .21180f);
        map[59] = new Color(.79220f, .18820f, .20780f);
        map[60] = new Color(.79610f, .19610f, .20390f);
        map[61] = new Color(.80000f, .20000f, .20000f);
        map[62] = new Color(.80390f, .20780f, .19610f);
        map[63] = new Color(.80780f, .21180f, .19220f);
        map[64] = new Color(.81180f, .21960f, .18820f);
        map[65] = new Color(.81570f, .22350f, .18430f);
        map[66] = new Color(.81960f, .23140f, .18040f);
        map[67] = new Color(.82750f, .23530f, .17250f);
        map[68] = new Color(.83140f, .24310f, .16860f);
        map[69] = new Color(.83530f, .24710f, .16470f);
        map[70] = new Color(.83920f, .25490f, .16080f);
        map[71] = new Color(.83920f, .26270f, .16080f);
        map[72] = new Color(.84310f, .27060f, .15690f);
        map[73] = new Color(.84710f, .27840f, .15290f);
        map[74] = new Color(.85100f, .28630f, .14900f);
        map[75] = new Color(.85490f, .29410f, .14510f);
        map[76] = new Color(.85880f, .29800f, .14120f);
        map[77] = new Color(.86270f, .30590f, .13730f);
        map[78] = new Color(.86270f, .31370f, .13730f);
        map[79] = new Color(.86670f, .32160f, .13330f);
        map[80] = new Color(.87060f, .33330f, .12940f);
        map[81] = new Color(.87840f, .34120f, .12160f);
        map[82] = new Color(.88240f, .34900f, .11760f);
        map[83] = new Color(.88240f, .35690f, .11760f);
        map[84] = new Color(.88630f, .36470f, .11370f);
        map[85] = new Color(.89020f, .37650f, .10980f);
        map[86] = new Color(.89410f, .38430f, .10590f);
        map[87] = new Color(.89410f, .39220f, .10590f);
        map[88] = new Color(.89800f, .40390f, .10200f);
        map[89] = new Color(.90200f, .41180f, .09800f);
        map[90] = new Color(.90590f, .42350f, .09410f);
        map[91] = new Color(.90590f, .43530f, .09410f);
        map[92] = new Color(.90980f, .44310f, .09020f);
        map[93] = new Color(.91370f, .45490f, .08630f);
        map[94] = new Color(.91370f, .46670f, .08630f);
        map[95] = new Color(.91760f, .47840f, .08240f);
        map[96] = new Color(.92160f, .49020f, .07840f);
        map[97] = new Color(.92160f, .50200f, .07840f);
        map[98] = new Color(.92940f, .51370f, .07060f);
        map[99] = new Color(.93330f, .52550f, .06670f);
        map[100] = new Color(.93330f, .53730f, .06670f);
        map[101] = new Color(.93730f, .54900f, .06270f);
        map[102] = new Color(.94120f, .56080f, .05880f);
        map[103] = new Color(.94120f, .57650f, .05880f);
        map[104] = new Color(.94510f, .58820f, .05490f);
        map[105] = new Color(.94510f, .60000f, .05490f);
        map[106] = new Color(.94900f, .61570f, .05100f);
        map[107] = new Color(.95290f, .63140f, .04710f);
        map[108] = new Color(.95290f, .64310f, .04710f);
        map[109] = new Color(.95690f, .65880f, .04310f);
        map[110] = new Color(.95690f, .67450f, .04310f);
        map[111] = new Color(.96080f, .69020f, .03920f);
        map[112] = new Color(.96470f, .70590f, .03530f);
        map[113] = new Color(.96470f, .72160f, .03530f);
        map[114] = new Color(.96860f, .73730f, .03140f);
        map[115] = new Color(.96860f, .75290f, .03140f);
        map[116] = new Color(.97250f, .77250f, .02750f);
        map[117] = new Color(.97250f, .78820f, .02750f);
        map[118] = new Color(.98040f, .80390f, .01960f);
        map[119] = new Color(.98040f, .82350f, .01960f);
        map[120] = new Color(.98430f, .84310f, .01570f);
        map[121] = new Color(.98820f, .86270f, .01180f);
        map[122] = new Color(.98820f, .87840f, .01180f);
        map[123] = new Color(.99220f, .89800f, .00780f);
        map[124] = new Color(.99220f, .91760f, .00780f);
        map[125] = new Color(.99610f, .94120f, .00390f);
        map[126] = new Color(.99610f, .96080f, .00390f);
        map[127] = new Color(1.00000f, .98040f, .00000f);
    }

    public PastelFilter(){}

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
