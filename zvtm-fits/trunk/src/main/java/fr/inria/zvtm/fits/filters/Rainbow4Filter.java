/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class Rainbow4Filter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public Rainbow4Filter(){
        map[0] = new Color(.00000f, .00000f, .01180f);
        map[1] = new Color(.00000f, .00000f, .04310f);
        map[2] = new Color(.00000f, .00000f, .07450f);
        map[3] = new Color(.00000f, .00000f, .10590f);
        map[4] = new Color(.00000f, .00000f, .13730f);
        map[5] = new Color(.00000f, .00000f, .16860f);
        map[6] = new Color(.00000f, .00000f, .20000f);
        map[7] = new Color(.00000f, .00000f, .22750f);
        map[8] = new Color(.00000f, .00000f, .25880f);
        map[9] = new Color(.00000f, .00000f, .29020f);
        map[10] = new Color(.00000f, .00000f, .32160f);
        map[11] = new Color(.00000f, .00000f, .35290f);
        map[12] = new Color(.00000f, .00000f, .38430f);
        map[13] = new Color(.00000f, .00000f, .41180f);
        map[14] = new Color(.00000f, .00000f, .44310f);
        map[15] = new Color(.00000f, .00000f, .47450f);
        map[16] = new Color(.00000f, .00000f, .50590f);
        map[17] = new Color(.00000f, .00000f, .53730f);
        map[18] = new Color(.00000f, .00000f, .56860f);
        map[19] = new Color(.00000f, .00000f, .60000f);
        map[20] = new Color(.00000f, .00000f, .62750f);
        map[21] = new Color(.00000f, .00000f, .65880f);
        map[22] = new Color(.00000f, .00000f, .69020f);
        map[23] = new Color(.00000f, .00000f, .72160f);
        map[24] = new Color(.00000f, .00000f, .75290f);
        map[25] = new Color(.00000f, .00000f, .78430f);
        map[26] = new Color(.00000f, .00000f, .81180f);
        map[27] = new Color(.00000f, .00000f, .84310f);
        map[28] = new Color(.00000f, .00000f, .87450f);
        map[29] = new Color(.00000f, .00000f, .90590f);
        map[30] = new Color(.00000f, .00000f, .93730f);
        map[31] = new Color(.00000f, .00000f, .96860f);
        map[32] = new Color(.00000f, .00000f, 1.00000f);
        map[33] = new Color(.00000f, .07060f, 1.00000f);
        map[34] = new Color(.00000f, .14510f, 1.00000f);
        map[35] = new Color(.00000f, .21960f, 1.00000f);
        map[36] = new Color(.00000f, .29410f, 1.00000f);
        map[37] = new Color(.00000f, .36470f, 1.00000f);
        map[38] = new Color(.00000f, .43920f, 1.00000f);
        map[39] = new Color(.00000f, .50200f, 1.00000f);
        map[40] = new Color(.00000f, .54900f, 1.00000f);
        map[41] = new Color(.00000f, .59610f, 1.00000f);
        map[42] = new Color(.00000f, .64310f, 1.00000f);
        map[43] = new Color(.00000f, .69020f, 1.00000f);
        map[44] = new Color(.00000f, .73730f, 1.00000f);
        map[45] = new Color(.00000f, .78430f, 1.00000f);
        map[46] = new Color(.00000f, .81570f, 1.00000f);
        map[47] = new Color(.00000f, .84710f, 1.00000f);
        map[48] = new Color(.00000f, .88240f, 1.00000f);
        map[49] = new Color(.00000f, .91370f, 1.00000f);
        map[50] = new Color(.00000f, .94900f, 1.00000f);
        map[51] = new Color(.00000f, .98040f, 1.00000f);
        map[52] = new Color(.00000f, 1.00000f, .97650f);
        map[53] = new Color(.00000f, 1.00000f, .92940f);
        map[54] = new Color(.00000f, 1.00000f, .88630f);
        map[55] = new Color(.00000f, 1.00000f, .83920f);
        map[56] = new Color(.00000f, 1.00000f, .79610f);
        map[57] = new Color(.00000f, 1.00000f, .74900f);
        map[58] = new Color(.00000f, 1.00000f, .70590f);
        map[59] = new Color(.00000f, 1.00000f, .59610f);
        map[60] = new Color(.00000f, 1.00000f, .48630f);
        map[61] = new Color(.00000f, 1.00000f, .37650f);
        map[62] = new Color(.00000f, 1.00000f, .27060f);
        map[63] = new Color(.00000f, 1.00000f, .16080f);
        map[64] = new Color(.00000f, 1.00000f, .05100f);
        map[65] = new Color(.05100f, 1.00000f, .00000f);
        map[66] = new Color(.16080f, 1.00000f, .00000f);
        map[67] = new Color(.27060f, 1.00000f, .00000f);
        map[68] = new Color(.37650f, 1.00000f, .00000f);
        map[69] = new Color(.48630f, 1.00000f, .00000f);
        map[70] = new Color(.59610f, 1.00000f, .00000f);
        map[71] = new Color(.70590f, 1.00000f, .00000f);
        map[72] = new Color(.74900f, 1.00000f, .00000f);
        map[73] = new Color(.79610f, 1.00000f, .00000f);
        map[74] = new Color(.83920f, 1.00000f, .00000f);
        map[75] = new Color(.88630f, 1.00000f, .00000f);
        map[76] = new Color(.92940f, 1.00000f, .00000f);
        map[77] = new Color(.97650f, 1.00000f, .00000f);
        map[78] = new Color(.99610f, .97650f, .00000f);
        map[79] = new Color(.99610f, .93330f, .00000f);
        map[80] = new Color(.99220f, .89410f, .00000f);
        map[81] = new Color(.99220f, .85100f, .00000f);
        map[82] = new Color(.98820f, .80780f, .00000f);
        map[83] = new Color(.98820f, .76470f, .00000f);
        map[84] = new Color(.98820f, .72550f, .00000f);
        map[85] = new Color(.98820f, .68630f, .00000f);
        map[86] = new Color(.98820f, .64710f, .00000f);
        map[87] = new Color(.99220f, .60780f, .00000f);
        map[88] = new Color(.99220f, .56860f, .00000f);
        map[89] = new Color(.99610f, .52940f, .00000f);
        map[90] = new Color(.99610f, .49020f, .00000f);
        map[91] = new Color(1.00000f, .43140f, .00000f);
        map[92] = new Color(1.00000f, .36080f, .00000f);
        map[93] = new Color(1.00000f, .28630f, .00000f);
        map[94] = new Color(1.00000f, .21570f, .00000f);
        map[95] = new Color(1.00000f, .14120f, .00000f);
        map[96] = new Color(1.00000f, .07060f, .00000f);
        map[97] = new Color(1.00000f, .00000f, .00000f);
        map[98] = new Color(1.00000f, .00000f, .00000f);
        map[99] = new Color(1.00000f, .00000f, .10590f);
        map[100] = new Color(1.00000f, .00000f, .21570f);
        map[101] = new Color(1.00000f, .00000f, .32550f);
        map[102] = new Color(1.00000f, .00000f, .43140f);
        map[103] = new Color(1.00000f, .00000f, .54120f);
        map[104] = new Color(1.00000f, .00000f, .65100f);
        map[105] = new Color(1.00000f, .00000f, .72550f);
        map[106] = new Color(1.00000f, .00000f, .77250f);
        map[107] = new Color(1.00000f, .00000f, .81570f);
        map[108] = new Color(1.00000f, .00000f, .86270f);
        map[109] = new Color(1.00000f, .00000f, .90590f);
        map[110] = new Color(1.00000f, .00000f, .95290f);
        map[111] = new Color(1.00000f, .00000f, 1.00000f);
        map[112] = new Color(1.00000f, .07060f, 1.00000f);
        map[113] = new Color(1.00000f, .14120f, 1.00000f);
        map[114] = new Color(1.00000f, .21570f, 1.00000f);
        map[115] = new Color(1.00000f, .28630f, 1.00000f);
        map[116] = new Color(1.00000f, .36080f, 1.00000f);
        map[117] = new Color(1.00000f, .43140f, 1.00000f);
        map[118] = new Color(1.00000f, .48630f, 1.00000f);
        map[119] = new Color(1.00000f, .52160f, 1.00000f);
        map[120] = new Color(1.00000f, .56080f, 1.00000f);
        map[121] = new Color(1.00000f, .59610f, 1.00000f);
        map[122] = new Color(1.00000f, .63140f, 1.00000f);
        map[123] = new Color(1.00000f, .66670f, 1.00000f);
        map[124] = new Color(1.00000f, .70590f, 1.00000f);
        map[125] = new Color(1.00000f, .78820f, 1.00000f);
        map[126] = new Color(1.00000f, .87060f, 1.00000f);
        map[127] = new Color(1.00000f, .95690f, 1.00000f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
