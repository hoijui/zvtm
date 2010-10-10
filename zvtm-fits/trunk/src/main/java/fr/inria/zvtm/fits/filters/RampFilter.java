/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.gradients;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class RampFilter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public RampFilter(){
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00780f, .00780f, .00780f);
        map[2] = new Color(.01570f, .01570f, .01570f);
        map[3] = new Color(.02350f, .02350f, .02350f);
        map[4] = new Color(.03140f, .03140f, .03140f);
        map[5] = new Color(.03920f, .03920f, .03920f);
        map[6] = new Color(.04710f, .04710f, .04710f);
        map[7] = new Color(.05490f, .05490f, .05490f);
        map[8] = new Color(.06270f, .06270f, .06270f);
        map[9] = new Color(.07060f, .07060f, .07060f);
        map[10] = new Color(.07840f, .07840f, .07840f);
        map[11] = new Color(.08630f, .08630f, .08630f);
        map[12] = new Color(.09410f, .09410f, .09410f);
        map[13] = new Color(.10200f, .10200f, .10200f);
        map[14] = new Color(.10980f, .10980f, .10980f);
        map[15] = new Color(.11760f, .11760f, .11760f);
        map[16] = new Color(.12550f, .12550f, .12550f);
        map[17] = new Color(.13330f, .13330f, .13330f);
        map[18] = new Color(.14120f, .14120f, .14120f);
        map[19] = new Color(.14900f, .14900f, .14900f);
        map[20] = new Color(.15690f, .15690f, .15690f);
        map[21] = new Color(.16470f, .16470f, .16470f);
        map[22] = new Color(.17250f, .17250f, .17250f);
        map[23] = new Color(.18040f, .18040f, .18040f);
        map[24] = new Color(.18820f, .18820f, .18820f);
        map[25] = new Color(.19610f, .19610f, .19610f);
        map[26] = new Color(.20390f, .20390f, .20390f);
        map[27] = new Color(.21180f, .21180f, .21180f);
        map[28] = new Color(.21960f, .21960f, .21960f);
        map[29] = new Color(.22750f, .22750f, .22750f);
        map[30] = new Color(.23530f, .23530f, .23530f);
        map[31] = new Color(.24310f, .24310f, .24310f);
        map[32] = new Color(.25100f, .25100f, .25100f);
        map[33] = new Color(.25880f, .25880f, .25880f);
        map[34] = new Color(.26670f, .26670f, .26670f);
        map[35] = new Color(.27450f, .27450f, .27450f);
        map[36] = new Color(.28240f, .28240f, .28240f);
        map[37] = new Color(.29020f, .29020f, .29020f);
        map[38] = new Color(.29800f, .29800f, .29800f);
        map[39] = new Color(.30590f, .30590f, .30590f);
        map[40] = new Color(.31370f, .31370f, .31370f);
        map[41] = new Color(.32160f, .32160f, .32160f);
        map[42] = new Color(.32940f, .32940f, .32940f);
        map[43] = new Color(.33730f, .33730f, .33730f);
        map[44] = new Color(.34510f, .34510f, .34510f);
        map[45] = new Color(.35290f, .35290f, .35290f);
        map[46] = new Color(.36080f, .36080f, .36080f);
        map[47] = new Color(.36860f, .36860f, .36860f);
        map[48] = new Color(.37650f, .37650f, .37650f);
        map[49] = new Color(.38430f, .38430f, .38430f);
        map[50] = new Color(.39220f, .39220f, .39220f);
        map[51] = new Color(.40000f, .40000f, .40000f);
        map[52] = new Color(.40780f, .40780f, .40780f);
        map[53] = new Color(.41570f, .41570f, .41570f);
        map[54] = new Color(.42350f, .42350f, .42350f);
        map[55] = new Color(.43140f, .43140f, .43140f);
        map[56] = new Color(.43920f, .43920f, .43920f);
        map[57] = new Color(.44710f, .44710f, .44710f);
        map[58] = new Color(.45490f, .45490f, .45490f);
        map[59] = new Color(.46270f, .46270f, .46270f);
        map[60] = new Color(.47060f, .47060f, .47060f);
        map[61] = new Color(.47840f, .47840f, .47840f);
        map[62] = new Color(.48630f, .48630f, .48630f);
        map[63] = new Color(.49410f, .49410f, .49410f);
        map[64] = new Color(.50200f, .50200f, .50200f);
        map[65] = new Color(.50980f, .50980f, .50980f);
        map[66] = new Color(.51760f, .51760f, .51760f);
        map[67] = new Color(.52550f, .52550f, .52550f);
        map[68] = new Color(.53330f, .53330f, .53330f);
        map[69] = new Color(.54120f, .54120f, .54120f);
        map[70] = new Color(.54900f, .54900f, .54900f);
        map[71] = new Color(.55690f, .55690f, .55690f);
        map[72] = new Color(.56470f, .56470f, .56470f);
        map[73] = new Color(.57250f, .57250f, .57250f);
        map[74] = new Color(.58040f, .58040f, .58040f);
        map[75] = new Color(.58820f, .58820f, .58820f);
        map[76] = new Color(.60000f, .60000f, .60000f);
        map[77] = new Color(.60390f, .60390f, .60390f);
        map[78] = new Color(.61180f, .61180f, .61180f);
        map[79] = new Color(.61960f, .61960f, .61960f);
        map[80] = new Color(.62750f, .62750f, .62750f);
        map[81] = new Color(.63530f, .63530f, .63530f);
        map[82] = new Color(.64310f, .64310f, .64310f);
        map[83] = new Color(.65100f, .65100f, .65100f);
        map[84] = new Color(.65880f, .65880f, .65880f);
        map[85] = new Color(.66670f, .66670f, .66670f);
        map[86] = new Color(.67450f, .67450f, .67450f);
        map[87] = new Color(.68240f, .68240f, .68240f);
        map[88] = new Color(.69020f, .69020f, .69020f);
        map[89] = new Color(.69800f, .69800f, .69800f);
        map[90] = new Color(.70590f, .70590f, .70590f);
        map[91] = new Color(.71370f, .71370f, .71370f);
        map[92] = new Color(.72160f, .72160f, .72160f);
        map[93] = new Color(.72940f, .72940f, .72940f);
        map[94] = new Color(.73730f, .73730f, .73730f);
        map[95] = new Color(.74510f, .74510f, .74510f);
        map[96] = new Color(.75290f, .75290f, .75290f);
        map[97] = new Color(.76080f, .76080f, .76080f);
        map[98] = new Color(.76860f, .76860f, .76860f);
        map[99] = new Color(.77650f, .77650f, .77650f);
        map[100] = new Color(.78430f, .78430f, .78430f);
        map[101] = new Color(.79220f, .79220f, .79220f);
        map[102] = new Color(.80000f, .80000f, .80000f);
        map[103] = new Color(.80780f, .80780f, .80780f);
        map[104] = new Color(.81570f, .81570f, .81570f);
        map[105] = new Color(.82350f, .82350f, .82350f);
        map[106] = new Color(.83140f, .83140f, .83140f);
        map[107] = new Color(.83920f, .83920f, .83920f);
        map[108] = new Color(.84710f, .84710f, .84710f);
        map[109] = new Color(.85490f, .85490f, .85490f);
        map[110] = new Color(.86270f, .86270f, .86270f);
        map[111] = new Color(.87060f, .87060f, .87060f);
        map[112] = new Color(.87840f, .87840f, .87840f);
        map[113] = new Color(.88630f, .88630f, .88630f);
        map[114] = new Color(.89410f, .89410f, .89410f);
        map[115] = new Color(.90200f, .90200f, .90200f);
        map[116] = new Color(.90980f, .90980f, .90980f);
        map[117] = new Color(.91760f, .91760f, .91760f);
        map[118] = new Color(.92550f, .92550f, .92550f);
        map[119] = new Color(.93330f, .93330f, .93330f);
        map[120] = new Color(.94120f, .94120f, .94120f);
        map[121] = new Color(.94900f, .94900f, .94900f);
        map[122] = new Color(.95690f, .95690f, .95690f);
        map[123] = new Color(.96470f, .96470f, .96470f);
        map[124] = new Color(.97250f, .97250f, .97250f);
        map[125] = new Color(.98040f, .98040f, .98040f);
        map[126] = new Color(.98820f, .98820f, .98820f);
        map[127] = new Color(.99610f, .99610f, .99610f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
