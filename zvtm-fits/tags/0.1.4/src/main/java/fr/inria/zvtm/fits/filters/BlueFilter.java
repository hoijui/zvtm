/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class BlueFilter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00000f, .00000f, .00780f);
        map[2] = new Color(.00000f, .00000f, .01570f);
        map[3] = new Color(.00000f, .00000f, .02350f);
        map[4] = new Color(.00000f, .00000f, .03140f);
        map[5] = new Color(.00000f, .00000f, .03920f);
        map[6] = new Color(.00000f, .00000f, .04710f);
        map[7] = new Color(.00000f, .00000f, .05490f);
        map[8] = new Color(.00000f, .00000f, .06270f);
        map[9] = new Color(.00000f, .00000f, .07060f);
        map[10] = new Color(.00000f, .00000f, .07840f);
        map[11] = new Color(.00000f, .00000f, .08630f);
        map[12] = new Color(.00000f, .00000f, .09410f);
        map[13] = new Color(.00000f, .00000f, .10200f);
        map[14] = new Color(.00000f, .00000f, .10980f);
        map[15] = new Color(.00000f, .00000f, .11760f);
        map[16] = new Color(.00000f, .00000f, .12550f);
        map[17] = new Color(.00000f, .00000f, .13330f);
        map[18] = new Color(.00000f, .00000f, .14120f);
        map[19] = new Color(.00000f, .00000f, .14900f);
        map[20] = new Color(.00000f, .00000f, .15690f);
        map[21] = new Color(.00000f, .00000f, .16470f);
        map[22] = new Color(.00000f, .00000f, .17250f);
        map[23] = new Color(.00000f, .00000f, .18040f);
        map[24] = new Color(.00000f, .00000f, .18820f);
        map[25] = new Color(.00000f, .00000f, .19610f);
        map[26] = new Color(.00000f, .00000f, .20390f);
        map[27] = new Color(.00000f, .00000f, .21180f);
        map[28] = new Color(.00000f, .00000f, .21960f);
        map[29] = new Color(.00000f, .00000f, .22750f);
        map[30] = new Color(.00000f, .00000f, .23530f);
        map[31] = new Color(.00000f, .00000f, .24310f);
        map[32] = new Color(.00000f, .00000f, .25100f);
        map[33] = new Color(.00000f, .00000f, .25880f);
        map[34] = new Color(.00000f, .00000f, .26670f);
        map[35] = new Color(.00000f, .00000f, .27450f);
        map[36] = new Color(.00000f, .00000f, .28240f);
        map[37] = new Color(.00000f, .00000f, .29020f);
        map[38] = new Color(.00000f, .00000f, .29800f);
        map[39] = new Color(.00000f, .00000f, .30590f);
        map[40] = new Color(.00000f, .00000f, .31370f);
        map[41] = new Color(.00000f, .00000f, .32160f);
        map[42] = new Color(.00000f, .00000f, .32940f);
        map[43] = new Color(.00000f, .00000f, .33730f);
        map[44] = new Color(.00000f, .00000f, .34510f);
        map[45] = new Color(.00000f, .00000f, .35290f);
        map[46] = new Color(.00000f, .00000f, .36080f);
        map[47] = new Color(.00000f, .00000f, .36860f);
        map[48] = new Color(.00000f, .00000f, .37650f);
        map[49] = new Color(.00000f, .00000f, .38430f);
        map[50] = new Color(.00000f, .00000f, .39220f);
        map[51] = new Color(.00000f, .00000f, .40000f);
        map[52] = new Color(.00000f, .00000f, .40780f);
        map[53] = new Color(.00000f, .00000f, .41570f);
        map[54] = new Color(.00000f, .00000f, .42350f);
        map[55] = new Color(.00000f, .00000f, .43140f);
        map[56] = new Color(.00000f, .00000f, .43920f);
        map[57] = new Color(.00000f, .00000f, .44710f);
        map[58] = new Color(.00000f, .00000f, .45490f);
        map[59] = new Color(.00000f, .00000f, .46270f);
        map[60] = new Color(.00000f, .00000f, .47060f);
        map[61] = new Color(.00000f, .00000f, .47840f);
        map[62] = new Color(.00000f, .00000f, .48630f);
        map[63] = new Color(.00000f, .00000f, .49410f);
        map[64] = new Color(.00000f, .00000f, .50200f);
        map[65] = new Color(.00000f, .00000f, .50980f);
        map[66] = new Color(.00000f, .00000f, .51760f);
        map[67] = new Color(.00000f, .00000f, .52550f);
        map[68] = new Color(.00000f, .00000f, .53330f);
        map[69] = new Color(.00000f, .00000f, .54120f);
        map[70] = new Color(.00000f, .00000f, .54900f);
        map[71] = new Color(.00000f, .00000f, .55690f);
        map[72] = new Color(.00000f, .00000f, .56470f);
        map[73] = new Color(.00000f, .00000f, .57250f);
        map[74] = new Color(.00000f, .00000f, .58040f);
        map[75] = new Color(.00000f, .00000f, .58820f);
        map[76] = new Color(.00000f, .00000f, .59610f);
        map[77] = new Color(.00000f, .00000f, .60390f);
        map[78] = new Color(.00000f, .00000f, .61180f);
        map[79] = new Color(.00000f, .00000f, .61960f);
        map[80] = new Color(.00000f, .00000f, .62750f);
        map[81] = new Color(.00000f, .00000f, .63530f);
        map[82] = new Color(.00000f, .00000f, .64310f);
        map[83] = new Color(.00000f, .00000f, .65100f);
        map[84] = new Color(.00000f, .00000f, .65880f);
        map[85] = new Color(.00000f, .00000f, .66670f);
        map[86] = new Color(.00000f, .00000f, .67450f);
        map[87] = new Color(.00000f, .00000f, .68240f);
        map[88] = new Color(.00000f, .00000f, .69020f);
        map[89] = new Color(.00000f, .00000f, .69800f);
        map[90] = new Color(.00000f, .00000f, .70590f);
        map[91] = new Color(.00000f, .00000f, .71370f);
        map[92] = new Color(.00000f, .00000f, .72160f);
        map[93] = new Color(.00000f, .00000f, .72940f);
        map[94] = new Color(.00000f, .00000f, .73730f);
        map[95] = new Color(.00000f, .00000f, .74510f);
        map[96] = new Color(.00000f, .00000f, .75290f);
        map[97] = new Color(.00000f, .00000f, .76080f);
        map[98] = new Color(.00000f, .00000f, .76860f);
        map[99] = new Color(.00000f, .00000f, .77650f);
        map[100] = new Color(.00000f, .00000f, .78430f);
        map[101] = new Color(.00000f, .00000f, .79220f);
        map[102] = new Color(.00000f, .00000f, .80000f);
        map[103] = new Color(.00000f, .00000f, .80780f);
        map[104] = new Color(.00000f, .00000f, .81570f);
        map[105] = new Color(.00000f, .00000f, .82350f);
        map[106] = new Color(.00000f, .00000f, .83140f);
        map[107] = new Color(.00000f, .00000f, .83920f);
        map[108] = new Color(.00000f, .00000f, .84710f);
        map[109] = new Color(.00000f, .00000f, .85490f);
        map[110] = new Color(.00000f, .00000f, .86270f);
        map[111] = new Color(.00000f, .00000f, .87060f);
        map[112] = new Color(.00000f, .00000f, .87840f);
        map[113] = new Color(.00000f, .00000f, .88630f);
        map[114] = new Color(.00000f, .00000f, .89410f);
        map[115] = new Color(.00000f, .00000f, .90200f);
        map[116] = new Color(.00000f, .00000f, .90980f);
        map[117] = new Color(.00000f, .00000f, .91760f);
        map[118] = new Color(.00000f, .00000f, .92550f);
        map[119] = new Color(.00000f, .00000f, .93330f);
        map[120] = new Color(.00000f, .00000f, .94120f);
        map[121] = new Color(.00000f, .00000f, .94900f);
        map[122] = new Color(.00000f, .00000f, .95690f);
        map[123] = new Color(.00000f, .00000f, .96470f);
        map[124] = new Color(.00000f, .00000f, .97250f);
        map[125] = new Color(.00000f, .00000f, .98040f);
        map[126] = new Color(.00000f, .00780f, .98820f);
        map[127] = new Color(.00000f, .01570f, .99610f);
    }

    public BlueFilter(){}

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
