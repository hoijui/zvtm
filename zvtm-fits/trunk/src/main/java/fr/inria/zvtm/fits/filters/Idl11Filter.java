/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class Idl11Filter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public Idl11Filter(){
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00000f, .00780f, .00780f);
        map[2] = new Color(.00000f, .01570f, .01570f);
        map[3] = new Color(.00000f, .04710f, .04710f);
        map[4] = new Color(.00000f, .08240f, .08240f);
        map[5] = new Color(.00000f, .11370f, .11370f);
        map[6] = new Color(.00000f, .14900f, .14900f);
        map[7] = new Color(.00000f, .18040f, .18040f);
        map[8] = new Color(.00000f, .21570f, .21570f);
        map[9] = new Color(.00000f, .24710f, .24710f);
        map[10] = new Color(.00000f, .28240f, .28240f);
        map[11] = new Color(.00000f, .31370f, .31370f);
        map[12] = new Color(.00000f, .34900f, .34900f);
        map[13] = new Color(.00000f, .38040f, .38040f);
        map[14] = new Color(.00000f, .41570f, .41570f);
        map[15] = new Color(.00000f, .44710f, .44710f);
        map[16] = new Color(.00000f, .48240f, .48240f);
        map[17] = new Color(.00000f, .51370f, .51370f);
        map[18] = new Color(.00000f, .54900f, .54900f);
        map[19] = new Color(.00000f, .58040f, .58040f);
        map[20] = new Color(.00000f, .61570f, .61570f);
        map[21] = new Color(.00000f, .64710f, .64710f);
        map[22] = new Color(.00000f, .68240f, .68240f);
        map[23] = new Color(.00000f, .71370f, .71370f);
        map[24] = new Color(.00000f, .74900f, .74900f);
        map[25] = new Color(.00000f, .78040f, .78040f);
        map[26] = new Color(.00000f, .81570f, .81570f);
        map[27] = new Color(.00000f, .84710f, .84710f);
        map[28] = new Color(.00000f, .88240f, .88240f);
        map[29] = new Color(.00000f, .91370f, .91370f);
        map[30] = new Color(.00000f, .94900f, .94900f);
        map[31] = new Color(.00000f, .98040f, .98040f);
        map[32] = new Color(.00000f, 1.00000f, 1.00000f);
        map[33] = new Color(.00000f, .96860f, 1.00000f);
        map[34] = new Color(.00000f, .93730f, 1.00000f);
        map[35] = new Color(.00000f, .90590f, 1.00000f);
        map[36] = new Color(.00000f, .87450f, 1.00000f);
        map[37] = new Color(.00000f, .84310f, 1.00000f);
        map[38] = new Color(.00000f, .81180f, 1.00000f);
        map[39] = new Color(.00000f, .78040f, 1.00000f);
        map[40] = new Color(.00000f, .74900f, 1.00000f);
        map[41] = new Color(.00000f, .71760f, 1.00000f);
        map[42] = new Color(.00000f, .68630f, 1.00000f);
        map[43] = new Color(.00000f, .65100f, 1.00000f);
        map[44] = new Color(.00000f, .61960f, 1.00000f);
        map[45] = new Color(.00000f, .58820f, 1.00000f);
        map[46] = new Color(.00000f, .55690f, 1.00000f);
        map[47] = new Color(.00000f, .52550f, 1.00000f);
        map[48] = new Color(.00000f, .49410f, 1.00000f);
        map[49] = new Color(.00000f, .46270f, 1.00000f);
        map[50] = new Color(.00000f, .43140f, 1.00000f);
        map[51] = new Color(.00000f, .40000f, 1.00000f);
        map[52] = new Color(.00000f, .36860f, 1.00000f);
        map[53] = new Color(.00000f, .33330f, 1.00000f);
        map[54] = new Color(.00000f, .30200f, 1.00000f);
        map[55] = new Color(.00000f, .27060f, 1.00000f);
        map[56] = new Color(.00000f, .23920f, 1.00000f);
        map[57] = new Color(.00000f, .20780f, 1.00000f);
        map[58] = new Color(.00000f, .17650f, 1.00000f);
        map[59] = new Color(.00000f, .14510f, 1.00000f);
        map[60] = new Color(.00000f, .11370f, 1.00000f);
        map[61] = new Color(.00000f, .08240f, 1.00000f);
        map[62] = new Color(.00000f, .05100f, 1.00000f);
        map[63] = new Color(.00000f, .01960f, 1.00000f);
        map[64] = new Color(.00000f, .00000f, 1.00000f);
        map[65] = new Color(.03140f, .00000f, 1.00000f);
        map[66] = new Color(.06270f, .00000f, 1.00000f);
        map[67] = new Color(.09410f, .00000f, 1.00000f);
        map[68] = new Color(.12550f, .00000f, 1.00000f);
        map[69] = new Color(.15690f, .00000f, 1.00000f);
        map[70] = new Color(.18820f, .00000f, 1.00000f);
        map[71] = new Color(.21960f, .00000f, 1.00000f);
        map[72] = new Color(.25100f, .00000f, 1.00000f);
        map[73] = new Color(.28240f, .00000f, 1.00000f);
        map[74] = new Color(.31370f, .00000f, 1.00000f);
        map[75] = new Color(.34900f, .00000f, 1.00000f);
        map[76] = new Color(.38040f, .00000f, 1.00000f);
        map[77] = new Color(.41180f, .00000f, 1.00000f);
        map[78] = new Color(.44310f, .00000f, 1.00000f);
        map[79] = new Color(.47450f, .00000f, 1.00000f);
        map[80] = new Color(.50590f, .00000f, 1.00000f);
        map[81] = new Color(.53730f, .00000f, 1.00000f);
        map[82] = new Color(.56860f, .00000f, 1.00000f);
        map[83] = new Color(.60000f, .00000f, 1.00000f);
        map[84] = new Color(.63140f, .00000f, 1.00000f);
        map[85] = new Color(.66670f, .00000f, 1.00000f);
        map[86] = new Color(.69800f, .00000f, 1.00000f);
        map[87] = new Color(.72940f, .00000f, 1.00000f);
        map[88] = new Color(.76080f, .00000f, 1.00000f);
        map[89] = new Color(.79220f, .00000f, 1.00000f);
        map[90] = new Color(.82350f, .00000f, 1.00000f);
        map[91] = new Color(.85490f, .00000f, 1.00000f);
        map[92] = new Color(.88630f, .00000f, 1.00000f);
        map[93] = new Color(.91760f, .00000f, 1.00000f);
        map[94] = new Color(.94900f, .00000f, 1.00000f);
        map[95] = new Color(.98040f, .00000f, 1.00000f);
        map[96] = new Color(1.00000f, .00000f, 1.00000f);
        map[97] = new Color(1.00000f, .00000f, .96860f);
        map[98] = new Color(1.00000f, .00000f, .93730f);
        map[99] = new Color(1.00000f, .00000f, .90590f);
        map[100] = new Color(1.00000f, .00000f, .87450f);
        map[101] = new Color(1.00000f, .00000f, .83920f);
        map[102] = new Color(1.00000f, .00000f, .80780f);
        map[103] = new Color(1.00000f, .00000f, .77650f);
        map[104] = new Color(1.00000f, .00000f, .74510f);
        map[105] = new Color(1.00000f, .00000f, .70980f);
        map[106] = new Color(1.00000f, .00000f, .67840f);
        map[107] = new Color(1.00000f, .00000f, .64710f);
        map[108] = new Color(1.00000f, .00000f, .61570f);
        map[109] = new Color(1.00000f, .00000f, .58430f);
        map[110] = new Color(1.00000f, .00000f, .54900f);
        map[111] = new Color(1.00000f, .00000f, .51760f);
        map[112] = new Color(1.00000f, .00000f, .48630f);
        map[113] = new Color(1.00000f, .00000f, .45490f);
        map[114] = new Color(1.00000f, .00000f, .41960f);
        map[115] = new Color(1.00000f, .00000f, .38820f);
        map[116] = new Color(1.00000f, .00000f, .35690f);
        map[117] = new Color(1.00000f, .00000f, .32550f);
        map[118] = new Color(1.00000f, .00000f, .29410f);
        map[119] = new Color(1.00000f, .00000f, .25880f);
        map[120] = new Color(1.00000f, .00000f, .22750f);
        map[121] = new Color(1.00000f, .00000f, .19610f);
        map[122] = new Color(1.00000f, .00000f, .16470f);
        map[123] = new Color(1.00000f, .00000f, .12940f);
        map[124] = new Color(1.00000f, .00000f, .09800f);
        map[125] = new Color(1.00000f, .00000f, .06670f);
        map[126] = new Color(1.00000f, .00000f, .03530f);
        map[127] = new Color(1.00000f, .00000f, .00000f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
