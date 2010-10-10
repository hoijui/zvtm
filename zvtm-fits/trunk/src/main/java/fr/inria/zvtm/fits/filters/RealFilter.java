/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class RealFilter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public RealFilter(){
        map[0] = new Color(.00780f, .00390f, .00000f);
        map[1] = new Color(.02350f, .01180f, .00000f);
        map[2] = new Color(.03920f, .01960f, .00000f);
        map[3] = new Color(.05490f, .02750f, .00000f);
        map[4] = new Color(.07060f, .03530f, .00000f);
        map[5] = new Color(.08630f, .04310f, .00000f);
        map[6] = new Color(.10200f, .05100f, .00000f);
        map[7] = new Color(.11760f, .05880f, .00000f);
        map[8] = new Color(.13330f, .06670f, .00000f);
        map[9] = new Color(.14900f, .07450f, .00000f);
        map[10] = new Color(.16470f, .08240f, .00000f);
        map[11] = new Color(.18040f, .09020f, .00000f);
        map[12] = new Color(.19610f, .09800f, .00000f);
        map[13] = new Color(.21180f, .10590f, .00000f);
        map[14] = new Color(.22750f, .11370f, .00000f);
        map[15] = new Color(.24310f, .12160f, .00000f);
        map[16] = new Color(.25880f, .12940f, .00000f);
        map[17] = new Color(.27450f, .13730f, .00000f);
        map[18] = new Color(.29020f, .14510f, .00000f);
        map[19] = new Color(.30590f, .15290f, .00000f);
        map[20] = new Color(.32160f, .16080f, .00000f);
        map[21] = new Color(.33730f, .16860f, .00000f);
        map[22] = new Color(.35290f, .17650f, .00000f);
        map[23] = new Color(.36860f, .18430f, .00000f);
        map[24] = new Color(.38430f, .19220f, .00000f);
        map[25] = new Color(.40000f, .20000f, .00000f);
        map[26] = new Color(.41570f, .20780f, .00000f);
        map[27] = new Color(.43140f, .21570f, .00000f);
        map[28] = new Color(.44710f, .22350f, .00000f);
        map[29] = new Color(.46270f, .23140f, .00000f);
        map[30] = new Color(.47840f, .23920f, .00000f);
        map[31] = new Color(.49410f, .24710f, .00000f);
        map[32] = new Color(.50980f, .25490f, .00000f);
        map[33] = new Color(.52550f, .26270f, .00000f);
        map[34] = new Color(.54120f, .27060f, .00000f);
        map[35] = new Color(.55690f, .27840f, .00000f);
        map[36] = new Color(.57250f, .28630f, .00000f);
        map[37] = new Color(.58820f, .29410f, .00000f);
        map[38] = new Color(.60390f, .30200f, .00000f);
        map[39] = new Color(.61960f, .30980f, .00000f);
        map[40] = new Color(.63530f, .31760f, .00000f);
        map[41] = new Color(.65100f, .32550f, .00000f);
        map[42] = new Color(.66670f, .33330f, .00000f);
        map[43] = new Color(.68240f, .34120f, .00000f);
        map[44] = new Color(.69800f, .34900f, .00000f);
        map[45] = new Color(.71370f, .35690f, .00000f);
        map[46] = new Color(.72940f, .36470f, .00000f);
        map[47] = new Color(.74510f, .37250f, .00000f);
        map[48] = new Color(.76080f, .38040f, .00000f);
        map[49] = new Color(.77650f, .38820f, .00000f);
        map[50] = new Color(.79220f, .39610f, .00000f);
        map[51] = new Color(.80780f, .40390f, .00000f);
        map[52] = new Color(.82350f, .41180f, .00000f);
        map[53] = new Color(.83920f, .41960f, .00000f);
        map[54] = new Color(.85490f, .42750f, .00000f);
        map[55] = new Color(.87060f, .43530f, .00000f);
        map[56] = new Color(.88630f, .44310f, .00000f);
        map[57] = new Color(.90200f, .45100f, .00000f);
        map[58] = new Color(.91760f, .45880f, .00000f);
        map[59] = new Color(.93330f, .46670f, .00000f);
        map[60] = new Color(.94900f, .47450f, .00000f);
        map[61] = new Color(.96470f, .48240f, .00000f);
        map[62] = new Color(.98040f, .49020f, .00000f);
        map[63] = new Color(.99610f, .49800f, .00000f);
        map[64] = new Color(1.00000f, .50590f, .00780f);
        map[65] = new Color(1.00000f, .51370f, .02350f);
        map[66] = new Color(1.00000f, .52160f, .03920f);
        map[67] = new Color(1.00000f, .52940f, .05490f);
        map[68] = new Color(1.00000f, .53730f, .07060f);
        map[69] = new Color(1.00000f, .54510f, .08630f);
        map[70] = new Color(1.00000f, .55290f, .10200f);
        map[71] = new Color(1.00000f, .56080f, .11760f);
        map[72] = new Color(1.00000f, .56860f, .13330f);
        map[73] = new Color(1.00000f, .57650f, .14900f);
        map[74] = new Color(1.00000f, .58430f, .16470f);
        map[75] = new Color(1.00000f, .59220f, .18040f);
        map[76] = new Color(1.00000f, .60000f, .19610f);
        map[77] = new Color(1.00000f, .60780f, .21180f);
        map[78] = new Color(1.00000f, .61570f, .22750f);
        map[79] = new Color(1.00000f, .62350f, .24310f);
        map[80] = new Color(1.00000f, .63140f, .25880f);
        map[81] = new Color(1.00000f, .63920f, .27450f);
        map[82] = new Color(1.00000f, .64710f, .29020f);
        map[83] = new Color(1.00000f, .65490f, .30590f);
        map[84] = new Color(1.00000f, .66270f, .32160f);
        map[85] = new Color(1.00000f, .67060f, .33730f);
        map[86] = new Color(1.00000f, .67840f, .35290f);
        map[87] = new Color(1.00000f, .68630f, .36860f);
        map[88] = new Color(1.00000f, .69410f, .38430f);
        map[89] = new Color(1.00000f, .70200f, .40000f);
        map[90] = new Color(1.00000f, .70980f, .41570f);
        map[91] = new Color(1.00000f, .71760f, .43140f);
        map[92] = new Color(1.00000f, .72550f, .44710f);
        map[93] = new Color(1.00000f, .73330f, .46270f);
        map[94] = new Color(1.00000f, .74120f, .47840f);
        map[95] = new Color(1.00000f, .74900f, .49410f);
        map[96] = new Color(1.00000f, .75690f, .50980f);
        map[97] = new Color(1.00000f, .76470f, .52550f);
        map[98] = new Color(1.00000f, .77250f, .54120f);
        map[99] = new Color(1.00000f, .78040f, .55690f);
        map[100] = new Color(1.00000f, .78820f, .57250f);
        map[101] = new Color(1.00000f, .79610f, .58820f);
        map[102] = new Color(1.00000f, .80390f, .60390f);
        map[103] = new Color(1.00000f, .81180f, .61960f);
        map[104] = new Color(1.00000f, .81960f, .63530f);
        map[105] = new Color(1.00000f, .82750f, .65100f);
        map[106] = new Color(1.00000f, .83530f, .66670f);
        map[107] = new Color(1.00000f, .84310f, .68240f);
        map[108] = new Color(1.00000f, .85100f, .69800f);
        map[109] = new Color(1.00000f, .85880f, .71370f);
        map[110] = new Color(1.00000f, .86670f, .72940f);
        map[111] = new Color(1.00000f, .87450f, .74510f);
        map[112] = new Color(1.00000f, .88240f, .76080f);
        map[113] = new Color(1.00000f, .89020f, .77650f);
        map[114] = new Color(1.00000f, .89800f, .79220f);
        map[115] = new Color(1.00000f, .90590f, .80780f);
        map[116] = new Color(1.00000f, .91370f, .82350f);
        map[117] = new Color(1.00000f, .92160f, .83920f);
        map[118] = new Color(1.00000f, .92940f, .85490f);
        map[119] = new Color(1.00000f, .93730f, .87060f);
        map[120] = new Color(1.00000f, .94510f, .88630f);
        map[121] = new Color(1.00000f, .95290f, .90200f);
        map[122] = new Color(1.00000f, .96080f, .91760f);
        map[123] = new Color(1.00000f, .96860f, .93330f);
        map[124] = new Color(1.00000f, .97650f, .94900f);
        map[125] = new Color(1.00000f, .98430f, .96470f);
        map[126] = new Color(1.00000f, .99220f, .98040f);
        map[127] = new Color(1.00000f, 1.00000f, .99610f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
