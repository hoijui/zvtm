/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class Idl2Filter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00000f, .28240f, .00000f);
        map[2] = new Color(.00000f, .30980f, .00000f);
        map[3] = new Color(.00000f, .33730f, .00000f);
        map[4] = new Color(.00000f, .36470f, .00000f);
        map[5] = new Color(.00000f, .39220f, .00000f);
        map[6] = new Color(.00000f, .42350f, .00000f);
        map[7] = new Color(.00000f, .49410f, .00000f);
        map[8] = new Color(.00000f, .56470f, .00000f);
        map[9] = new Color(.00000f, .63530f, .00000f);
        map[10] = new Color(.00000f, .70590f, .00000f);
        map[11] = new Color(.00000f, .77650f, .00000f);
        map[12] = new Color(.00000f, .84710f, .00000f);
        map[13] = new Color(.00000f, .91760f, .00000f);
        map[14] = new Color(.00000f, .98820f, .00000f);
        map[15] = new Color(.04710f, .96470f, .00000f);
        map[16] = new Color(.09410f, .94120f, .00000f);
        map[17] = new Color(.14120f, .89410f, .00000f);
        map[18] = new Color(.18820f, .84710f, .00000f);
        map[19] = new Color(.23530f, .80000f, .00000f);
        map[20] = new Color(.28240f, .75290f, .00000f);
        map[21] = new Color(.32940f, .70590f, .00000f);
        map[22] = new Color(.37650f, .65880f, .00000f);
        map[23] = new Color(.42350f, .61180f, .00000f);
        map[24] = new Color(.47060f, .56470f, .00000f);
        map[25] = new Color(.51760f, .51760f, .00000f);
        map[26] = new Color(.56470f, .47060f, .00000f);
        map[27] = new Color(.61180f, .42350f, .00000f);
        map[28] = new Color(.65880f, .37650f, .00000f);
        map[29] = new Color(.70590f, .32940f, .00000f);
        map[30] = new Color(.75290f, .28240f, .00000f);
        map[31] = new Color(.80000f, .23530f, .00000f);
        map[32] = new Color(.84710f, .18820f, .00000f);
        map[33] = new Color(.89410f, .14120f, .00000f);
        map[34] = new Color(.94120f, .09410f, .00000f);
        map[35] = new Color(.96470f, .04710f, .00000f);
        map[36] = new Color(.98820f, .00000f, .00000f);
        map[37] = new Color(.98820f, .00000f, .00000f);
        map[38] = new Color(.98820f, .00000f, .00390f);
        map[39] = new Color(.98040f, .00000f, .01960f);
        map[40] = new Color(.97250f, .00000f, .03530f);
        map[41] = new Color(.97250f, .00000f, .04710f);
        map[42] = new Color(.97250f, .00000f, .06270f);
        map[43] = new Color(.96470f, .00000f, .07840f);
        map[44] = new Color(.95690f, .00000f, .09800f);
        map[45] = new Color(.94900f, .00000f, .11370f);
        map[46] = new Color(.94120f, .00000f, .13330f);
        map[47] = new Color(.93730f, .00000f, .14510f);
        map[48] = new Color(.92940f, .00000f, .16080f);
        map[49] = new Color(.92550f, .00000f, .17650f);
        map[50] = new Color(.92550f, .00000f, .19610f);
        map[51] = new Color(.91760f, .00000f, .21180f);
        map[52] = new Color(.90980f, .00000f, .23140f);
        map[53] = new Color(.90200f, .00000f, .24710f);
        map[54] = new Color(.89410f, .00000f, .26270f);
        map[55] = new Color(.89410f, .00000f, .27450f);
        map[56] = new Color(.89410f, .00000f, .29020f);
        map[57] = new Color(.88630f, .00000f, .30590f);
        map[58] = new Color(.87840f, .00000f, .32550f);
        map[59] = new Color(.87060f, .00000f, .34120f);
        map[60] = new Color(.86270f, .00390f, .36080f);
        map[61] = new Color(.85490f, .00390f, .37650f);
        map[62] = new Color(.84710f, .00000f, .39610f);
        map[63] = new Color(.84710f, .00000f, .40780f);
        map[64] = new Color(.84710f, .00000f, .42350f);
        map[65] = new Color(.83920f, .00000f, .43920f);
        map[66] = new Color(.83140f, .00000f, .45490f);
        map[67] = new Color(.82350f, .00000f, .47060f);
        map[68] = new Color(.81570f, .00000f, .49020f);
        map[69] = new Color(.80780f, .00000f, .50590f);
        map[70] = new Color(.80000f, .00000f, .52550f);
        map[71] = new Color(.80000f, .00000f, .53730f);
        map[72] = new Color(.80000f, .00000f, .55290f);
        map[73] = new Color(.79220f, .00000f, .56860f);
        map[74] = new Color(.78430f, .00000f, .58820f);
        map[75] = new Color(.77650f, .00000f, .60390f);
        map[76] = new Color(.76860f, .00000f, .62350f);
        map[77] = new Color(.76860f, .00000f, .63530f);
        map[78] = new Color(.76860f, .00000f, .64710f);
        map[79] = new Color(.76080f, .00000f, .66270f);
        map[80] = new Color(.75290f, .00000f, .68240f);
        map[81] = new Color(.74510f, .00000f, .69800f);
        map[82] = new Color(.73730f, .00000f, .71760f);
        map[83] = new Color(.72940f, .00000f, .73330f);
        map[84] = new Color(.72160f, .00000f, .75290f);
        map[85] = new Color(.72160f, .00000f, .76470f);
        map[86] = new Color(.72160f, .00000f, .78040f);
        map[87] = new Color(.71370f, .00000f, .79610f);
        map[88] = new Color(.70590f, .00000f, .81570f);
        map[89] = new Color(.69800f, .00000f, .83140f);
        map[90] = new Color(.69020f, .00000f, .84710f);
        map[91] = new Color(.69020f, .00000f, .85880f);
        map[92] = new Color(.69020f, .00000f, .87450f);
        map[93] = new Color(.68240f, .00000f, .89020f);
        map[94] = new Color(.67450f, .00000f, .90980f);
        map[95] = new Color(.66670f, .00000f, .92550f);
        map[96] = new Color(.65880f, .00000f, .94510f);
        map[97] = new Color(.65100f, .00000f, .96080f);
        map[98] = new Color(.64310f, .00000f, .98040f);
        map[99] = new Color(.64310f, .00000f, .98820f);
        map[100] = new Color(.64310f, .00000f, 1.00000f);
        map[101] = new Color(.63530f, .00000f, 1.00000f);
        map[102] = new Color(.62750f, .00000f, 1.00000f);
        map[103] = new Color(.61960f, .00000f, 1.00000f);
        map[104] = new Color(.61180f, .00000f, 1.00000f);
        map[105] = new Color(.60390f, .00000f, 1.00000f);
        map[106] = new Color(.59610f, .00000f, 1.00000f);
        map[107] = new Color(.59610f, .00000f, 1.00000f);
        map[108] = new Color(.59610f, .00000f, 1.00000f);
        map[109] = new Color(.58820f, .00000f, 1.00000f);
        map[110] = new Color(.58040f, .00000f, 1.00000f);
        map[111] = new Color(.60390f, .06270f, 1.00000f);
        map[112] = new Color(.62750f, .12550f, 1.00000f);
        map[113] = new Color(.65100f, .18820f, 1.00000f);
        map[114] = new Color(.67450f, .25100f, 1.00000f);
        map[115] = new Color(.70590f, .31370f, 1.00000f);
        map[116] = new Color(.73730f, .37650f, 1.00000f);
        map[117] = new Color(.76080f, .43920f, 1.00000f);
        map[118] = new Color(.78430f, .50200f, 1.00000f);
        map[119] = new Color(.80780f, .55690f, 1.00000f);
        map[120] = new Color(.83140f, .61180f, 1.00000f);
        map[121] = new Color(.85490f, .67450f, 1.00000f);
        map[122] = new Color(.87840f, .73730f, 1.00000f);
        map[123] = new Color(.90980f, .80000f, 1.00000f);
        map[124] = new Color(.94120f, .86270f, 1.00000f);
        map[125] = new Color(.96470f, .92550f, 1.00000f);
        map[126] = new Color(.98820f, .98820f, 1.00000f);
        map[127] = new Color(.99610f, .99610f, 1.00000f);
    }

    public Idl2Filter(){}

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
