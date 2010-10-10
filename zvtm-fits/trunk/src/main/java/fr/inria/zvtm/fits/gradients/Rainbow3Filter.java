/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.gradients;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class Rainbow3Filter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public Rainbow3Filter(){
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00000f, .00000f, .00000f);
        map[2] = new Color(.00000f, .00000f, .00000f);
        map[3] = new Color(.00000f, .00000f, .07840f);
        map[4] = new Color(.00000f, .00000f, .15690f);
        map[5] = new Color(.00000f, .00000f, .23920f);
        map[6] = new Color(.00000f, .00000f, .31760f);
        map[7] = new Color(.00000f, .00000f, .40000f);
        map[8] = new Color(.00000f, .00000f, .47840f);
        map[9] = new Color(.00000f, .00000f, .55690f);
        map[10] = new Color(.00000f, .00000f, .63920f);
        map[11] = new Color(.00000f, .00000f, .71760f);
        map[12] = new Color(.00000f, .00000f, .80000f);
        map[13] = new Color(.00000f, .00000f, .87840f);
        map[14] = new Color(.00000f, .00000f, .95690f);
        map[15] = new Color(.00000f, .03140f, 1.00000f);
        map[16] = new Color(.00000f, .09410f, 1.00000f);
        map[17] = new Color(.00000f, .15690f, 1.00000f);
        map[18] = new Color(.00000f, .21960f, 1.00000f);
        map[19] = new Color(.00000f, .28630f, 1.00000f);
        map[20] = new Color(.00000f, .34900f, 1.00000f);
        map[21] = new Color(.00000f, .41180f, 1.00000f);
        map[22] = new Color(.00000f, .47840f, 1.00000f);
        map[23] = new Color(.00000f, .51760f, 1.00000f);
        map[24] = new Color(.00000f, .55690f, 1.00000f);
        map[25] = new Color(.00000f, .60000f, 1.00000f);
        map[26] = new Color(.00000f, .63920f, 1.00000f);
        map[27] = new Color(.00000f, .68240f, 1.00000f);
        map[28] = new Color(.00000f, .72160f, 1.00000f);
        map[29] = new Color(.00000f, .76080f, 1.00000f);
        map[30] = new Color(.00000f, .79610f, 1.00000f);
        map[31] = new Color(.00000f, .82750f, 1.00000f);
        map[32] = new Color(.00000f, .85490f, 1.00000f);
        map[33] = new Color(.00000f, .88240f, 1.00000f);
        map[34] = new Color(.00000f, .91370f, 1.00000f);
        map[35] = new Color(.00000f, .94120f, 1.00000f);
        map[36] = new Color(.00000f, .96860f, 1.00000f);
        map[37] = new Color(.00000f, 1.00000f, 1.00000f);
        map[38] = new Color(.00000f, 1.00000f, .96080f);
        map[39] = new Color(.00000f, 1.00000f, .92160f);
        map[40] = new Color(.00000f, 1.00000f, .88240f);
        map[41] = new Color(.00000f, 1.00000f, .84310f);
        map[42] = new Color(.00000f, 1.00000f, .80390f);
        map[43] = new Color(.00000f, 1.00000f, .76470f);
        map[44] = new Color(.00000f, 1.00000f, .72550f);
        map[45] = new Color(.00000f, 1.00000f, .65880f);
        map[46] = new Color(.00000f, 1.00000f, .56470f);
        map[47] = new Color(.00000f, 1.00000f, .47060f);
        map[48] = new Color(.00000f, 1.00000f, .37650f);
        map[49] = new Color(.00000f, 1.00000f, .27840f);
        map[50] = new Color(.00000f, 1.00000f, .18430f);
        map[51] = new Color(.00000f, 1.00000f, .09020f);
        map[52] = new Color(.00000f, 1.00000f, .00000f);
        map[53] = new Color(.09410f, 1.00000f, .00000f);
        map[54] = new Color(.18820f, 1.00000f, .00000f);
        map[55] = new Color(.28240f, 1.00000f, .00000f);
        map[56] = new Color(.37650f, 1.00000f, .00000f);
        map[57] = new Color(.47060f, 1.00000f, .00000f);
        map[58] = new Color(.56470f, 1.00000f, .00000f);
        map[59] = new Color(.65880f, 1.00000f, .00000f);
        map[60] = new Color(.72550f, 1.00000f, .00000f);
        map[61] = new Color(.76470f, 1.00000f, .00000f);
        map[62] = new Color(.80390f, 1.00000f, .00000f);
        map[63] = new Color(.84310f, 1.00000f, .00000f);
        map[64] = new Color(.88240f, 1.00000f, .00000f);
        map[65] = new Color(.92160f, 1.00000f, .00000f);
        map[66] = new Color(.96080f, 1.00000f, .00000f);
        map[67] = new Color(1.00000f, 1.00000f, .00000f);
        map[68] = new Color(.99610f, .96080f, .00000f);
        map[69] = new Color(.99610f, .92550f, .00000f);
        map[70] = new Color(.99220f, .89020f, .00000f);
        map[71] = new Color(.99220f, .85100f, .00000f);
        map[72] = new Color(.99220f, .81570f, .00000f);
        map[73] = new Color(.98820f, .78040f, .00000f);
        map[74] = new Color(.98820f, .74120f, .00000f);
        map[75] = new Color(.98820f, .70590f, .00000f);
        map[76] = new Color(.98820f, .67450f, .00000f);
        map[77] = new Color(.99220f, .63920f, .00000f);
        map[78] = new Color(.99220f, .60390f, .00000f);
        map[79] = new Color(.99220f, .57250f, .00000f);
        map[80] = new Color(.99610f, .53730f, .00000f);
        map[81] = new Color(.99610f, .50200f, .00000f);
        map[82] = new Color(1.00000f, .47060f, .00000f);
        map[83] = new Color(1.00000f, .40780f, .00000f);
        map[84] = new Color(1.00000f, .34510f, .00000f);
        map[85] = new Color(1.00000f, .28240f, .00000f);
        map[86] = new Color(1.00000f, .21570f, .00000f);
        map[87] = new Color(1.00000f, .15290f, .00000f);
        map[88] = new Color(1.00000f, .09020f, .00000f);
        map[89] = new Color(1.00000f, .02750f, .00000f);
        map[90] = new Color(1.00000f, .00000f, .00000f);
        map[91] = new Color(1.00000f, .00000f, .00000f);
        map[92] = new Color(1.00000f, .00000f, .00000f);
        map[93] = new Color(1.00000f, .00000f, .00000f);
        map[94] = new Color(1.00000f, .00000f, .00000f);
        map[95] = new Color(1.00000f, .00000f, .00000f);
        map[96] = new Color(1.00000f, .00000f, .00000f);
        map[97] = new Color(1.00000f, .00000f, .00000f);
        map[98] = new Color(1.00000f, .00000f, .09410f);
        map[99] = new Color(1.00000f, .00000f, .18820f);
        map[100] = new Color(1.00000f, .00000f, .28240f);
        map[101] = new Color(1.00000f, .00000f, .37650f);
        map[102] = new Color(1.00000f, .00000f, .47060f);
        map[103] = new Color(1.00000f, .00000f, .56470f);
        map[104] = new Color(1.00000f, .00000f, .65880f);
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
