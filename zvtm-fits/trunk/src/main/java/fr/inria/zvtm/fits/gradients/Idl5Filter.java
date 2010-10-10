/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.gradients;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class Idl5Filter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public Idl5Filter(){
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00000f, .00000f, .03920f);
        map[2] = new Color(.00000f, .00000f, .07840f);
        map[3] = new Color(.00000f, .00000f, .12160f);
        map[4] = new Color(.00000f, .00000f, .16080f);
        map[5] = new Color(.00000f, .00000f, .20390f);
        map[6] = new Color(.00000f, .00000f, .24310f);
        map[7] = new Color(.00000f, .00000f, .28240f);
        map[8] = new Color(.00000f, .00000f, .32550f);
        map[9] = new Color(.00000f, .00000f, .36470f);
        map[10] = new Color(.00000f, .00000f, .40780f);
        map[11] = new Color(.00000f, .00000f, .44710f);
        map[12] = new Color(.00000f, .00000f, .48630f);
        map[13] = new Color(.00000f, .00000f, .52940f);
        map[14] = new Color(.00000f, .00000f, .56860f);
        map[15] = new Color(.00000f, .00000f, .61180f);
        map[16] = new Color(.00000f, .00000f, .65100f);
        map[17] = new Color(.00000f, .00000f, .69020f);
        map[18] = new Color(.00000f, .00000f, .73330f);
        map[19] = new Color(.00000f, .00000f, .77250f);
        map[20] = new Color(.00000f, .00000f, .81570f);
        map[21] = new Color(.00000f, .00000f, .85490f);
        map[22] = new Color(.00000f, .00000f, .89410f);
        map[23] = new Color(.00000f, .00000f, .93730f);
        map[24] = new Color(.01570f, .00000f, .97650f);
        map[25] = new Color(.05490f, .00000f, .98040f);
        map[26] = new Color(.09020f, .00000f, .93730f);
        map[27] = new Color(.12940f, .00000f, .89410f);
        map[28] = new Color(.16470f, .00000f, .85490f);
        map[29] = new Color(.20390f, .00000f, .81180f);
        map[30] = new Color(.23920f, .00000f, .76860f);
        map[31] = new Color(.27840f, .00000f, .72550f);
        map[32] = new Color(.31760f, .00000f, .68240f);
        map[33] = new Color(.31760f, .00000f, .63920f);
        map[34] = new Color(.31760f, .00000f, .59610f);
        map[35] = new Color(.31760f, .00000f, .55690f);
        map[36] = new Color(.31370f, .00000f, .51370f);
        map[37] = new Color(.31370f, .00000f, .47060f);
        map[38] = new Color(.31370f, .00000f, .42750f);
        map[39] = new Color(.31370f, .00000f, .38430f);
        map[40] = new Color(.32940f, .00000f, .34120f);
        map[41] = new Color(.36860f, .00000f, .29800f);
        map[42] = new Color(.40780f, .00000f, .25880f);
        map[43] = new Color(.44710f, .00000f, .21570f);
        map[44] = new Color(.48630f, .00000f, .17250f);
        map[45] = new Color(.52550f, .00000f, .12940f);
        map[46] = new Color(.56470f, .00000f, .08630f);
        map[47] = new Color(.60390f, .00000f, .04310f);
        map[48] = new Color(.64310f, .00000f, .00000f);
        map[49] = new Color(.68240f, .00000f, .00000f);
        map[50] = new Color(.72550f, .00000f, .00000f);
        map[51] = new Color(.76860f, .00000f, .00000f);
        map[52] = new Color(.80780f, .00000f, .00000f);
        map[53] = new Color(.85100f, .00000f, .00000f);
        map[54] = new Color(.89410f, .00000f, .00000f);
        map[55] = new Color(1.00000f, .00000f, .00000f);
        map[56] = new Color(1.00000f, .00000f, .00000f);
        map[57] = new Color(1.00000f, .01960f, .00000f);
        map[58] = new Color(1.00000f, .06270f, .00000f);
        map[59] = new Color(1.00000f, .10590f, .00000f);
        map[60] = new Color(1.00000f, .14510f, .00000f);
        map[61] = new Color(1.00000f, .18820f, .00000f);
        map[62] = new Color(1.00000f, .23140f, .00000f);
        map[63] = new Color(1.00000f, .27450f, .00000f);
        map[64] = new Color(1.00000f, .31760f, .00000f);
        map[65] = new Color(1.00000f, .35290f, .03530f);
        map[66] = new Color(1.00000f, .39220f, .07450f);
        map[67] = new Color(1.00000f, .42750f, .10980f);
        map[68] = new Color(1.00000f, .46670f, .14900f);
        map[69] = new Color(1.00000f, .50590f, .18820f);
        map[70] = new Color(1.00000f, .54120f, .22350f);
        map[71] = new Color(1.00000f, .58040f, .26270f);
        map[72] = new Color(1.00000f, .61960f, .30200f);
        map[73] = new Color(1.00000f, .63920f, .30200f);
        map[74] = new Color(1.00000f, .63920f, .25490f);
        map[75] = new Color(1.00000f, .63920f, .20780f);
        map[76] = new Color(1.00000f, .63920f, .16080f);
        map[77] = new Color(1.00000f, .63920f, .11760f);
        map[78] = new Color(1.00000f, .63920f, .07060f);
        map[79] = new Color(1.00000f, .63920f, .02350f);
        map[80] = new Color(1.00000f, .63920f, .00000f);
        map[81] = new Color(1.00000f, .63920f, .00000f);
        map[82] = new Color(.94120f, .63920f, .00000f);
        map[83] = new Color(.88240f, .63920f, .00000f);
        map[84] = new Color(.81960f, .63920f, .00000f);
        map[85] = new Color(.76080f, .63920f, .00000f);
        map[86] = new Color(.70200f, .63920f, .00000f);
        map[87] = new Color(.63920f, .63920f, .00000f);
        map[88] = new Color(.67840f, .63920f, .00000f);
        map[89] = new Color(.71760f, .68630f, .02350f);
        map[90] = new Color(.75690f, .73330f, .04710f);
        map[91] = new Color(.79610f, .78040f, .07450f);
        map[92] = new Color(.83920f, .83140f, .09800f);
        map[93] = new Color(.87840f, .87840f, .12550f);
        map[94] = new Color(.91760f, .92550f, .14900f);
        map[95] = new Color(.95690f, .97250f, .17650f);
        map[96] = new Color(1.00000f, 1.00000f, .20000f);
        map[97] = new Color(1.00000f, 1.00000f, .22750f);
        map[98] = new Color(1.00000f, 1.00000f, .25100f);
        map[99] = new Color(1.00000f, 1.00000f, .27840f);
        map[100] = new Color(1.00000f, 1.00000f, .30200f);
        map[101] = new Color(1.00000f, 1.00000f, .32550f);
        map[102] = new Color(1.00000f, 1.00000f, .35290f);
        map[103] = new Color(1.00000f, 1.00000f, .37650f);
        map[104] = new Color(1.00000f, 1.00000f, .40390f);
        map[105] = new Color(1.00000f, 1.00000f, .42750f);
        map[106] = new Color(1.00000f, 1.00000f, .45490f);
        map[107] = new Color(1.00000f, 1.00000f, .47840f);
        map[108] = new Color(1.00000f, 1.00000f, .50590f);
        map[109] = new Color(1.00000f, 1.00000f, .52940f);
        map[110] = new Color(1.00000f, 1.00000f, .55690f);
        map[111] = new Color(1.00000f, 1.00000f, .58040f);
        map[112] = new Color(1.00000f, 1.00000f, .60390f);
        map[113] = new Color(1.00000f, 1.00000f, .63140f);
        map[114] = new Color(1.00000f, 1.00000f, .65490f);
        map[115] = new Color(1.00000f, 1.00000f, .68240f);
        map[116] = new Color(1.00000f, 1.00000f, .70590f);
        map[117] = new Color(1.00000f, 1.00000f, .73330f);
        map[118] = new Color(1.00000f, 1.00000f, .75690f);
        map[119] = new Color(1.00000f, 1.00000f, .78430f);
        map[120] = new Color(1.00000f, 1.00000f, .80780f);
        map[121] = new Color(1.00000f, 1.00000f, .83530f);
        map[122] = new Color(1.00000f, 1.00000f, .85880f);
        map[123] = new Color(1.00000f, 1.00000f, .88240f);
        map[124] = new Color(1.00000f, 1.00000f, .90980f);
        map[125] = new Color(1.00000f, 1.00000f, .93330f);
        map[126] = new Color(1.00000f, 1.00000f, .96080f);
        map[127] = new Color(1.00000f, 1.00000f, .98430f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
