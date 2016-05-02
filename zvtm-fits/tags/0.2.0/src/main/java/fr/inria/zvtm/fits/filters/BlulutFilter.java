/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class BlulutFilter extends RGBImageFilter implements ColorGradient {

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
        map[27] = new Color(.00390f, .00390f, .21180f);
        map[28] = new Color(.00390f, .00390f, .21960f);
        map[29] = new Color(.00390f, .00390f, .22750f);
        map[30] = new Color(.00390f, .00390f, .23530f);
        map[31] = new Color(.00390f, .00390f, .24310f);
        map[32] = new Color(.00390f, .00390f, .25100f);
        map[33] = new Color(.00390f, .00390f, .25880f);
        map[34] = new Color(.00390f, .00390f, .26670f);
        map[35] = new Color(.00390f, .00390f, .27450f);
        map[36] = new Color(.00780f, .00780f, .28240f);
        map[37] = new Color(.00780f, .00780f, .29020f);
        map[38] = new Color(.00780f, .00780f, .29800f);
        map[39] = new Color(.00780f, .00780f, .30590f);
        map[40] = new Color(.00780f, .00780f, .31370f);
        map[41] = new Color(.01180f, .01180f, .32160f);
        map[42] = new Color(.01180f, .01180f, .32940f);
        map[43] = new Color(.01180f, .01180f, .33730f);
        map[44] = new Color(.01570f, .01570f, .34510f);
        map[45] = new Color(.01570f, .01570f, .35290f);
        map[46] = new Color(.01570f, .01570f, .36080f);
        map[47] = new Color(.01960f, .01960f, .36860f);
        map[48] = new Color(.01960f, .01960f, .37650f);
        map[49] = new Color(.02350f, .02350f, .38430f);
        map[50] = new Color(.02350f, .02350f, .39220f);
        map[51] = new Color(.02750f, .02750f, .40000f);
        map[52] = new Color(.02750f, .02750f, .40780f);
        map[53] = new Color(.03140f, .03140f, .41570f);
        map[54] = new Color(.03140f, .03140f, .42350f);
        map[55] = new Color(.03530f, .03530f, .43140f);
        map[56] = new Color(.03530f, .03530f, .43920f);
        map[57] = new Color(.03920f, .03920f, .44710f);
        map[58] = new Color(.04310f, .04310f, .45490f);
        map[59] = new Color(.04710f, .04710f, .46270f);
        map[60] = new Color(.05100f, .05100f, .47060f);
        map[61] = new Color(.05100f, .05100f, .47840f);
        map[62] = new Color(.05490f, .05490f, .48630f);
        map[63] = new Color(.05880f, .05880f, .49410f);
        map[64] = new Color(.06270f, .06270f, .50200f);
        map[65] = new Color(.06670f, .06670f, .50980f);
        map[66] = new Color(.07060f, .07060f, .51760f);
        map[67] = new Color(.07450f, .07450f, .52550f);
        map[68] = new Color(.08240f, .08240f, .53330f);
        map[69] = new Color(.08630f, .08630f, .54120f);
        map[70] = new Color(.09020f, .09020f, .54900f);
        map[71] = new Color(.09800f, .09800f, .55690f);
        map[72] = new Color(.10200f, .10200f, .56470f);
        map[73] = new Color(.10590f, .10590f, .57250f);
        map[74] = new Color(.11370f, .11370f, .58040f);
        map[75] = new Color(.12160f, .12160f, .58820f);
        map[76] = new Color(.12550f, .12550f, .59610f);
        map[77] = new Color(.13330f, .13330f, .60390f);
        map[78] = new Color(.14120f, .14120f, .61180f);
        map[79] = new Color(.14900f, .14900f, .61960f);
        map[80] = new Color(.15690f, .15690f, .62750f);
        map[81] = new Color(.16470f, .16470f, .63530f);
        map[82] = new Color(.17250f, .17250f, .64310f);
        map[83] = new Color(.18040f, .18040f, .65100f);
        map[84] = new Color(.18820f, .18820f, .65880f);
        map[85] = new Color(.19610f, .19610f, .66670f);
        map[86] = new Color(.20780f, .20780f, .67450f);
        map[87] = new Color(.21570f, .21570f, .68240f);
        map[88] = new Color(.22750f, .22750f, .69020f);
        map[89] = new Color(.23920f, .23920f, .69800f);
        map[90] = new Color(.24710f, .24710f, .70590f);
        map[91] = new Color(.25880f, .25880f, .71370f);
        map[92] = new Color(.27060f, .27060f, .72160f);
        map[93] = new Color(.28240f, .28240f, .72940f);
        map[94] = new Color(.29410f, .29410f, .73730f);
        map[95] = new Color(.30980f, .30980f, .74510f);
        map[96] = new Color(.32160f, .32160f, .75290f);
        map[97] = new Color(.33330f, .33330f, .76080f);
        map[98] = new Color(.34900f, .34900f, .76860f);
        map[99] = new Color(.36470f, .36470f, .77650f);
        map[100] = new Color(.37650f, .37650f, .78430f);
        map[101] = new Color(.39220f, .39220f, .79220f);
        map[102] = new Color(.40780f, .40780f, .80000f);
        map[103] = new Color(.42750f, .42750f, .80780f);
        map[104] = new Color(.44310f, .44310f, .81570f);
        map[105] = new Color(.45880f, .45880f, .82350f);
        map[106] = new Color(.47840f, .47840f, .83140f);
        map[107] = new Color(.49410f, .49410f, .83920f);
        map[108] = new Color(.51370f, .51370f, .84710f);
        map[109] = new Color(.53330f, .53330f, .85490f);
        map[110] = new Color(.55290f, .55290f, .86270f);
        map[111] = new Color(.57250f, .57250f, .87060f);
        map[112] = new Color(.59610f, .59610f, .87840f);
        map[113] = new Color(.61570f, .61570f, .88630f);
        map[114] = new Color(.63920f, .63920f, .89410f);
        map[115] = new Color(.66270f, .66270f, .90200f);
        map[116] = new Color(.68630f, .68630f, .90980f);
        map[117] = new Color(.70980f, .70980f, .91760f);
        map[118] = new Color(.73330f, .73330f, .92550f);
        map[119] = new Color(.76080f, .76080f, .93330f);
        map[120] = new Color(.78430f, .78430f, .94120f);
        map[121] = new Color(.81180f, .81180f, .94900f);
        map[122] = new Color(.83920f, .83920f, .95690f);
        map[123] = new Color(.86670f, .86670f, .96470f);
        map[124] = new Color(.89410f, .89410f, .97250f);
        map[125] = new Color(.92550f, .92550f, .98040f);
        map[126] = new Color(.95290f, .95290f, .98820f);
        map[127] = new Color(.98430f, .98430f, .99610f);
    }

    public BlulutFilter(){}

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
