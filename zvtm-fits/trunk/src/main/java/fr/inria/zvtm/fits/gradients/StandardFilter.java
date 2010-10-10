/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.gradients;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class StandardFilter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public StandardFilter(){
        map[0] = new Color(.00390f, .00390f, .33330f);
        map[1] = new Color(.01180f, .01180f, .34900f);
        map[2] = new Color(.01960f, .01960f, .36470f);
        map[3] = new Color(.02750f, .02750f, .38040f);
        map[4] = new Color(.03530f, .03530f, .39610f);
        map[5] = new Color(.04310f, .04310f, .41180f);
        map[6] = new Color(.05100f, .05100f, .42750f);
        map[7] = new Color(.05880f, .05880f, .44310f);
        map[8] = new Color(.06670f, .06670f, .45880f);
        map[9] = new Color(.07450f, .07450f, .47450f);
        map[10] = new Color(.08240f, .08240f, .49020f);
        map[11] = new Color(.09020f, .09020f, .50590f);
        map[12] = new Color(.09800f, .09800f, .52160f);
        map[13] = new Color(.10590f, .10590f, .53730f);
        map[14] = new Color(.11370f, .11370f, .55290f);
        map[15] = new Color(.12160f, .12160f, .56860f);
        map[16] = new Color(.12940f, .12940f, .58430f);
        map[17] = new Color(.13730f, .13730f, .60000f);
        map[18] = new Color(.14510f, .14510f, .61570f);
        map[19] = new Color(.15290f, .15290f, .63140f);
        map[20] = new Color(.16080f, .16080f, .64710f);
        map[21] = new Color(.16860f, .16860f, .66270f);
        map[22] = new Color(.17650f, .17650f, .67840f);
        map[23] = new Color(.18430f, .18430f, .69410f);
        map[24] = new Color(.19220f, .19220f, .70980f);
        map[25] = new Color(.20000f, .20000f, .72550f);
        map[26] = new Color(.20780f, .20780f, .74120f);
        map[27] = new Color(.21570f, .21570f, .75690f);
        map[28] = new Color(.22350f, .22350f, .77250f);
        map[29] = new Color(.23140f, .23140f, .78820f);
        map[30] = new Color(.23920f, .23920f, .80390f);
        map[31] = new Color(.24710f, .24710f, .81960f);
        map[32] = new Color(.25490f, .25490f, .83530f);
        map[33] = new Color(.26270f, .26270f, .85100f);
        map[34] = new Color(.27060f, .27060f, .86670f);
        map[35] = new Color(.27840f, .27840f, .88240f);
        map[36] = new Color(.28630f, .28630f, .89800f);
        map[37] = new Color(.29410f, .29410f, .91370f);
        map[38] = new Color(.30200f, .30200f, .92940f);
        map[39] = new Color(.30980f, .30980f, .94510f);
        map[40] = new Color(.31760f, .31760f, .96080f);
        map[41] = new Color(.32550f, .32550f, .97650f);
        map[42] = new Color(.33330f, .33330f, .99220f);
        map[43] = new Color(.00780f, .34120f, .00780f);
        map[44] = new Color(.01570f, .35690f, .01570f);
        map[45] = new Color(.02350f, .37250f, .02350f);
        map[46] = new Color(.03140f, .38820f, .03140f);
        map[47] = new Color(.03920f, .40390f, .03920f);
        map[48] = new Color(.04710f, .41960f, .04710f);
        map[49] = new Color(.05490f, .43530f, .05490f);
        map[50] = new Color(.06270f, .45100f, .06270f);
        map[51] = new Color(.07060f, .46670f, .07060f);
        map[52] = new Color(.07840f, .48240f, .07840f);
        map[53] = new Color(.08630f, .49800f, .08630f);
        map[54] = new Color(.09410f, .51370f, .09410f);
        map[55] = new Color(.10200f, .52940f, .10200f);
        map[56] = new Color(.10980f, .54510f, .10980f);
        map[57] = new Color(.11760f, .56080f, .11760f);
        map[58] = new Color(.12550f, .57650f, .12550f);
        map[59] = new Color(.13330f, .59220f, .13330f);
        map[60] = new Color(.14120f, .60780f, .14120f);
        map[61] = new Color(.14900f, .62350f, .14900f);
        map[62] = new Color(.15690f, .63920f, .15690f);
        map[63] = new Color(.16470f, .65490f, .16470f);
        map[64] = new Color(.17250f, .67060f, .17250f);
        map[65] = new Color(.18040f, .68630f, .18040f);
        map[66] = new Color(.18820f, .70200f, .18820f);
        map[67] = new Color(.19610f, .71760f, .19610f);
        map[68] = new Color(.20390f, .73330f, .20390f);
        map[69] = new Color(.21180f, .74900f, .21180f);
        map[70] = new Color(.21960f, .76470f, .21960f);
        map[71] = new Color(.22750f, .78040f, .22750f);
        map[72] = new Color(.23530f, .79610f, .23530f);
        map[73] = new Color(.24310f, .81180f, .24310f);
        map[74] = new Color(.25100f, .82750f, .25100f);
        map[75] = new Color(.25880f, .84310f, .25880f);
        map[76] = new Color(.26670f, .85880f, .26670f);
        map[77] = new Color(.27450f, .87450f, .27450f);
        map[78] = new Color(.28240f, .89020f, .28240f);
        map[79] = new Color(.29020f, .90590f, .29020f);
        map[80] = new Color(.29800f, .92160f, .29800f);
        map[81] = new Color(.30590f, .93730f, .30590f);
        map[82] = new Color(.31370f, .95290f, .31370f);
        map[83] = new Color(.32160f, .96860f, .32160f);
        map[84] = new Color(.32940f, .98430f, .32940f);
        map[85] = new Color(.33330f, .00390f, .00390f);
        map[86] = new Color(.34900f, .01180f, .01180f);
        map[87] = new Color(.36470f, .01960f, .01960f);
        map[88] = new Color(.38040f, .02750f, .02750f);
        map[89] = new Color(.39610f, .03530f, .03530f);
        map[90] = new Color(.41180f, .04310f, .04310f);
        map[91] = new Color(.42750f, .05100f, .05100f);
        map[92] = new Color(.44310f, .05880f, .05880f);
        map[93] = new Color(.45880f, .06670f, .06670f);
        map[94] = new Color(.47450f, .07450f, .07450f);
        map[95] = new Color(.49020f, .08240f, .08240f);
        map[96] = new Color(.50590f, .09020f, .09020f);
        map[97] = new Color(.52160f, .09800f, .09800f);
        map[98] = new Color(.53730f, .10590f, .10590f);
        map[99] = new Color(.55290f, .11370f, .11370f);
        map[100] = new Color(.56860f, .12160f, .12160f);
        map[101] = new Color(.58430f, .12940f, .12940f);
        map[102] = new Color(.60000f, .13730f, .13730f);
        map[103] = new Color(.61570f, .14510f, .14510f);
        map[104] = new Color(.63140f, .15290f, .15290f);
        map[105] = new Color(.64710f, .16080f, .16080f);
        map[106] = new Color(.66270f, .16860f, .16860f);
        map[107] = new Color(.67840f, .17650f, .17650f);
        map[108] = new Color(.69410f, .18430f, .18430f);
        map[109] = new Color(.70980f, .19220f, .19220f);
        map[110] = new Color(.72550f, .20000f, .20000f);
        map[111] = new Color(.74120f, .20780f, .20780f);
        map[112] = new Color(.75690f, .21570f, .21570f);
        map[113] = new Color(.77250f, .22350f, .22350f);
        map[114] = new Color(.78820f, .23140f, .23140f);
        map[115] = new Color(.80390f, .23920f, .23920f);
        map[116] = new Color(.81960f, .24710f, .24710f);
        map[117] = new Color(.83530f, .25490f, .25490f);
        map[118] = new Color(.85100f, .26270f, .26270f);
        map[119] = new Color(.86670f, .27060f, .27060f);
        map[120] = new Color(.88240f, .27840f, .27840f);
        map[121] = new Color(.89800f, .28630f, .28630f);
        map[122] = new Color(.91370f, .29410f, .29410f);
        map[123] = new Color(.92940f, .30200f, .30200f);
        map[124] = new Color(.94510f, .30980f, .30980f);
        map[125] = new Color(.96080f, .31760f, .31760f);
        map[126] = new Color(.97650f, .32550f, .32550f);
        map[127] = new Color(.99220f, .33330f, .33330f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
