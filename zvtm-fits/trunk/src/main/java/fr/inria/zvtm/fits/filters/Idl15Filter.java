/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class Idl15Filter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.14120f, .00780f, .01180f);
        map[2] = new Color(.28240f, .01570f, .02750f);
        map[3] = new Color(.42350f, .02350f, .04310f);
        map[4] = new Color(.56860f, .03140f, .05880f);
        map[5] = new Color(.70980f, .03920f, .07450f);
        map[6] = new Color(.85100f, .04710f, .09020f);
        map[7] = new Color(.99610f, .05490f, .10590f);
        map[8] = new Color(.95690f, .06270f, .12160f);
        map[9] = new Color(.91760f, .07060f, .13730f);
        map[10] = new Color(.87450f, .07840f, .15290f);
        map[11] = new Color(.83530f, .08630f, .16860f);
        map[12] = new Color(.79610f, .09410f, .18430f);
        map[13] = new Color(.75290f, .10200f, .20000f);
        map[14] = new Color(.71370f, .10980f, .21570f);
        map[15] = new Color(.67450f, .11760f, .23140f);
        map[16] = new Color(.63140f, .12550f, .24710f);
        map[17] = new Color(.59220f, .13330f, .26270f);
        map[18] = new Color(.54900f, .14120f, .27840f);
        map[19] = new Color(.50980f, .14900f, .29410f);
        map[20] = new Color(.47060f, .15690f, .30980f);
        map[21] = new Color(.42750f, .16470f, .32550f);
        map[22] = new Color(.38820f, .17250f, .34120f);
        map[23] = new Color(.34900f, .18040f, .35690f);
        map[24] = new Color(.30590f, .18820f, .37250f);
        map[25] = new Color(.26670f, .19610f, .38820f);
        map[26] = new Color(.22750f, .20390f, .40390f);
        map[27] = new Color(.18430f, .21180f, .41960f);
        map[28] = new Color(.14510f, .21960f, .43530f);
        map[29] = new Color(.10200f, .22750f, .45100f);
        map[30] = new Color(.06270f, .23530f, .46670f);
        map[31] = new Color(.02350f, .24310f, .48240f);
        map[32] = new Color(.25100f, .25100f, .49800f);
        map[33] = new Color(.25880f, .25880f, .51370f);
        map[34] = new Color(.26670f, .26670f, .52940f);
        map[35] = new Color(.27450f, .27450f, .54510f);
        map[36] = new Color(.28240f, .28240f, .56080f);
        map[37] = new Color(.29020f, .29020f, .57650f);
        map[38] = new Color(.29800f, .29800f, .59220f);
        map[39] = new Color(.30590f, .30590f, .60780f);
        map[40] = new Color(.31370f, .31370f, .62350f);
        map[41] = new Color(.32160f, .32160f, .63920f);
        map[42] = new Color(.32940f, .32940f, .65490f);
        map[43] = new Color(.33730f, .33730f, .67060f);
        map[44] = new Color(.34510f, .34510f, .68630f);
        map[45] = new Color(.35290f, .35290f, .70200f);
        map[46] = new Color(.36080f, .36080f, .71760f);
        map[47] = new Color(.36860f, .36860f, .73330f);
        map[48] = new Color(.37650f, .37650f, .74900f);
        map[49] = new Color(.38430f, .38430f, .76470f);
        map[50] = new Color(.39220f, .39220f, .78040f);
        map[51] = new Color(.40000f, .40000f, .79610f);
        map[52] = new Color(.40780f, .40780f, .81180f);
        map[53] = new Color(.41570f, .41570f, .82750f);
        map[54] = new Color(.42350f, .42350f, .84310f);
        map[55] = new Color(.43140f, .43140f, .85880f);
        map[56] = new Color(.43920f, .43920f, .87450f);
        map[57] = new Color(.44710f, .44710f, .89020f);
        map[58] = new Color(.45490f, .45490f, .90590f);
        map[59] = new Color(.46270f, .46270f, .92160f);
        map[60] = new Color(.47060f, .47060f, .93730f);
        map[61] = new Color(.47840f, .47840f, .95290f);
        map[62] = new Color(.48630f, .48630f, .96860f);
        map[63] = new Color(.49410f, .49410f, .98430f);
        map[64] = new Color(.50200f, .50200f, 1.00000f);
        map[65] = new Color(.50980f, .50980f, .96860f);
        map[66] = new Color(.51760f, .51760f, .93330f);
        map[67] = new Color(.52550f, .52550f, .90200f);
        map[68] = new Color(.53330f, .53330f, .86670f);
        map[69] = new Color(.54120f, .54120f, .83530f);
        map[70] = new Color(.54900f, .54900f, .80000f);
        map[71] = new Color(.55690f, .55690f, .76860f);
        map[72] = new Color(.56470f, .56470f, .73330f);
        map[73] = new Color(.57250f, .57250f, .70200f);
        map[74] = new Color(.58040f, .58040f, .66670f);
        map[75] = new Color(.58820f, .58820f, .63530f);
        map[76] = new Color(.59610f, .59610f, .60000f);
        map[77] = new Color(.60390f, .60390f, .56860f);
        map[78] = new Color(.61180f, .61180f, .53330f);
        map[79] = new Color(.61960f, .61960f, .50200f);
        map[80] = new Color(.62750f, .62750f, .46670f);
        map[81] = new Color(.63530f, .63530f, .43530f);
        map[82] = new Color(.64310f, .64310f, .40000f);
        map[83] = new Color(.65100f, .65100f, .36860f);
        map[84] = new Color(.65880f, .65880f, .33330f);
        map[85] = new Color(.66670f, .66670f, .30200f);
        map[86] = new Color(.67450f, .67450f, .26670f);
        map[87] = new Color(.68240f, .68240f, .23530f);
        map[88] = new Color(.69020f, .69020f, .20000f);
        map[89] = new Color(.69800f, .69800f, .16860f);
        map[90] = new Color(.70590f, .70590f, .13330f);
        map[91] = new Color(.71370f, .71370f, .10200f);
        map[92] = new Color(.72160f, .72160f, .06670f);
        map[93] = new Color(.72940f, .72940f, .03530f);
        map[94] = new Color(.73730f, .73730f, .00000f);
        map[95] = new Color(.74510f, .74510f, .02750f);
        map[96] = new Color(.75290f, .75290f, .05880f);
        map[97] = new Color(.76080f, .76080f, .08630f);
        map[98] = new Color(.76860f, .76860f, .11760f);
        map[99] = new Color(.77650f, .77650f, .14900f);
        map[100] = new Color(.78430f, .78430f, .17650f);
        map[101] = new Color(.79220f, .79220f, .20780f);
        map[102] = new Color(.80000f, .80000f, .23530f);
        map[103] = new Color(.80780f, .80780f, .26670f);
        map[104] = new Color(.81570f, .81570f, .29800f);
        map[105] = new Color(.82350f, .82350f, .32550f);
        map[106] = new Color(.83140f, .83140f, .35690f);
        map[107] = new Color(.83920f, .83920f, .38430f);
        map[108] = new Color(.84710f, .84710f, .41570f);
        map[109] = new Color(.85490f, .85490f, .44710f);
        map[110] = new Color(.86270f, .86270f, .47450f);
        map[111] = new Color(.87060f, .87060f, .50590f);
        map[112] = new Color(.87840f, .87840f, .53730f);
        map[113] = new Color(.88630f, .88630f, .56470f);
        map[114] = new Color(.89410f, .89410f, .59610f);
        map[115] = new Color(.90200f, .90200f, .62350f);
        map[116] = new Color(.90980f, .90980f, .65490f);
        map[117] = new Color(.91760f, .91760f, .68630f);
        map[118] = new Color(.92550f, .92550f, .71370f);
        map[119] = new Color(.93330f, .93330f, .74510f);
        map[120] = new Color(.94120f, .94120f, .77250f);
        map[121] = new Color(.94900f, .94900f, .80390f);
        map[122] = new Color(.95690f, .95690f, .83530f);
        map[123] = new Color(.96470f, .96470f, .86270f);
        map[124] = new Color(.97250f, .97250f, .89410f);
        map[125] = new Color(.98040f, .98040f, .92160f);
        map[126] = new Color(.98820f, .98820f, .95290f);
        map[127] = new Color(.99610f, .99610f, .98430f);
    }

    public Idl15Filter(){}

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
