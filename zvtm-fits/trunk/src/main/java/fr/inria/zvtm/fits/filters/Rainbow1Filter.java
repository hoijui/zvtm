/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class Rainbow1Filter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public Rainbow1Filter(){
        map[0] = new Color(.00000f, .00000f, .16470f);
        map[1] = new Color(.05880f, .00000f, .20000f);
        map[2] = new Color(.11370f, .00000f, .23920f);
        map[3] = new Color(.17650f, .00000f, .27840f);
        map[4] = new Color(.23140f, .00000f, .31760f);
        map[5] = new Color(.29410f, .00000f, .35690f);
        map[6] = new Color(.35290f, .00000f, .39610f);
        map[7] = new Color(.41180f, .00000f, .43530f);
        map[8] = new Color(.47060f, .00000f, .47450f);
        map[9] = new Color(.52940f, .00000f, .51370f);
        map[10] = new Color(.58820f, .00000f, .55690f);
        map[11] = new Color(.52940f, .00000f, .59610f);
        map[12] = new Color(.47060f, .00000f, .63920f);
        map[13] = new Color(.41180f, .00000f, .67840f);
        map[14] = new Color(.35290f, .00000f, .72160f);
        map[15] = new Color(.29410f, .00000f, .76470f);
        map[16] = new Color(.23140f, .00000f, .80390f);
        map[17] = new Color(.17650f, .00000f, .84710f);
        map[18] = new Color(.11370f, .00000f, .89020f);
        map[19] = new Color(.05880f, .00000f, .93330f);
        map[20] = new Color(.00000f, .00000f, .97650f);
        map[21] = new Color(.00000f, .00000f, .97650f);
        map[22] = new Color(.00000f, .01570f, .93330f);
        map[23] = new Color(.00000f, .03140f, .89020f);
        map[24] = new Color(.00000f, .05100f, .85100f);
        map[25] = new Color(.00000f, .07840f, .81180f);
        map[26] = new Color(.00000f, .11760f, .77250f);
        map[27] = new Color(.00000f, .15690f, .73330f);
        map[28] = new Color(.00000f, .19610f, .69410f);
        map[29] = new Color(.00000f, .23530f, .65880f);
        map[30] = new Color(.00000f, .27060f, .62750f);
        map[31] = new Color(.00000f, .30200f, .59610f);
        map[32] = new Color(.00000f, .33330f, .56470f);
        map[33] = new Color(.00000f, .35690f, .53330f);
        map[34] = new Color(.00000f, .38040f, .50200f);
        map[35] = new Color(.00000f, .40390f, .47060f);
        map[36] = new Color(.00000f, .42350f, .44710f);
        map[37] = new Color(.00000f, .44710f, .42350f);
        map[38] = new Color(.00000f, .46670f, .40000f);
        map[39] = new Color(.00000f, .49020f, .37650f);
        map[40] = new Color(.00000f, .50980f, .35290f);
        map[41] = new Color(.00000f, .52940f, .32940f);
        map[42] = new Color(.00000f, .55290f, .30590f);
        map[43] = new Color(.00000f, .57250f, .28240f);
        map[44] = new Color(.00000f, .59220f, .25880f);
        map[45] = new Color(.00000f, .61180f, .23530f);
        map[46] = new Color(.00000f, .63140f, .21180f);
        map[47] = new Color(.00000f, .65100f, .18820f);
        map[48] = new Color(.00000f, .67060f, .16470f);
        map[49] = new Color(.00000f, .69020f, .14120f);
        map[50] = new Color(.00000f, .70980f, .11760f);
        map[51] = new Color(.00000f, .72940f, .08630f);
        map[52] = new Color(.00000f, .74900f, .05490f);
        map[53] = new Color(.00000f, .76860f, .02350f);
        map[54] = new Color(.00000f, .78820f, .00000f);
        map[55] = new Color(.00000f, .80780f, .00000f);
        map[56] = new Color(.00000f, .82350f, .00000f);
        map[57] = new Color(.00000f, .84310f, .00000f);
        map[58] = new Color(.00000f, .86270f, .00000f);
        map[59] = new Color(.00000f, .88240f, .00000f);
        map[60] = new Color(.00000f, .89800f, .00000f);
        map[61] = new Color(.00000f, .91760f, .00000f);
        map[62] = new Color(.00000f, .93730f, .00000f);
        map[63] = new Color(.00000f, .95290f, .00000f);
        map[64] = new Color(.00000f, .97250f, .00000f);
        map[65] = new Color(.00000f, .98820f, .00000f);
        map[66] = new Color(.01570f, .98820f, .00000f);
        map[67] = new Color(.03140f, .97250f, .00000f);
        map[68] = new Color(.05490f, .95290f, .00000f);
        map[69] = new Color(.07840f, .93730f, .00000f);
        map[70] = new Color(.10200f, .91760f, .00000f);
        map[71] = new Color(.12550f, .89800f, .00000f);
        map[72] = new Color(.14900f, .88240f, .00000f);
        map[73] = new Color(.20000f, .86270f, .00000f);
        map[74] = new Color(.26670f, .84310f, .00000f);
        map[75] = new Color(.34120f, .82350f, .00000f);
        map[76] = new Color(.41180f, .80780f, .00000f);
        map[77] = new Color(.48630f, .78820f, .00000f);
        map[78] = new Color(.56080f, .76860f, .00000f);
        map[79] = new Color(.63530f, .78820f, .00000f);
        map[80] = new Color(.70980f, .81180f, .00000f);
        map[81] = new Color(.78430f, .84310f, .00000f);
        map[82] = new Color(.85880f, .87060f, .00000f);
        map[83] = new Color(.93330f, .90200f, .00000f);
        map[84] = new Color(1.00000f, .93330f, .00000f);
        map[85] = new Color(1.00000f, .96860f, .00000f);
        map[86] = new Color(1.00000f, 1.00000f, .00000f);
        map[87] = new Color(1.00000f, 1.00000f, .00000f);
        map[88] = new Color(1.00000f, 1.00000f, .00000f);
        map[89] = new Color(1.00000f, 1.00000f, .00000f);
        map[90] = new Color(1.00000f, 1.00000f, .00000f);
        map[91] = new Color(1.00000f, 1.00000f, .00000f);
        map[92] = new Color(1.00000f, 1.00000f, .00000f);
        map[93] = new Color(1.00000f, 1.00000f, .00000f);
        map[94] = new Color(1.00000f, 1.00000f, .00000f);
        map[95] = new Color(1.00000f, 1.00000f, .00000f);
        map[96] = new Color(1.00000f, 1.00000f, .00000f);
        map[97] = new Color(1.00000f, .93730f, .00000f);
        map[98] = new Color(1.00000f, .85880f, .00000f);
        map[99] = new Color(1.00000f, .78040f, .00000f);
        map[100] = new Color(1.00000f, .70200f, .00000f);
        map[101] = new Color(1.00000f, .62350f, .00000f);
        map[102] = new Color(1.00000f, .54510f, .00000f);
        map[103] = new Color(1.00000f, .46670f, .00000f);
        map[104] = new Color(1.00000f, .39220f, .00000f);
        map[105] = new Color(1.00000f, .31760f, .00000f);
        map[106] = new Color(1.00000f, .23920f, .00000f);
        map[107] = new Color(1.00000f, .16860f, .00000f);
        map[108] = new Color(1.00000f, .09800f, .00000f);
        map[109] = new Color(1.00000f, .06270f, .00000f);
        map[110] = new Color(1.00000f, .02350f, .00000f);
        map[111] = new Color(.99220f, .00000f, .00000f);
        map[112] = new Color(.97650f, .00000f, .00000f);
        map[113] = new Color(.96080f, .00000f, .00000f);
        map[114] = new Color(.94510f, .00000f, .00000f);
        map[115] = new Color(.92940f, .00000f, .00000f);
        map[116] = new Color(.91370f, .00000f, .00000f);
        map[117] = new Color(.89800f, .00000f, .00000f);
        map[118] = new Color(.88240f, .00000f, .00000f);
        map[119] = new Color(.86670f, .00000f, .00000f);
        map[120] = new Color(.85100f, .00000f, .00000f);
        map[121] = new Color(.83530f, .00000f, .00000f);
        map[122] = new Color(.81960f, .00000f, .00000f);
        map[123] = new Color(.80390f, .00000f, .00000f);
        map[124] = new Color(.78820f, .00000f, .00000f);
        map[125] = new Color(.77250f, .00000f, .00000f);
        map[126] = new Color(.75690f, .00000f, .00000f);
        map[127] = new Color(.74120f, .00000f, .00000f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
