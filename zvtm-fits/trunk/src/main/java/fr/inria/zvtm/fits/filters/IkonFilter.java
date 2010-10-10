/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class IkonFilter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public IkonFilter(){
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00000f, .28240f, .00000f);
        map[2] = new Color(.00000f, .56470f, .00000f);
        map[3] = new Color(.00000f, .84710f, .00000f);
        map[4] = new Color(.00000f, .00000f, .14120f);
        map[5] = new Color(.00000f, .28240f, .14120f);
        map[6] = new Color(.00000f, .56470f, .14120f);
        map[7] = new Color(.00000f, .84710f, .14120f);
        map[8] = new Color(.00000f, .00000f, .28240f);
        map[9] = new Color(.00000f, .28240f, .28240f);
        map[10] = new Color(.00000f, .56470f, .28240f);
        map[11] = new Color(.00000f, .84710f, .28240f);
        map[12] = new Color(.00000f, .00000f, .42350f);
        map[13] = new Color(.00000f, .28240f, .42350f);
        map[14] = new Color(.00000f, .56470f, .42350f);
        map[15] = new Color(.00000f, .84710f, .42350f);
        map[16] = new Color(.00000f, .00000f, .56470f);
        map[17] = new Color(.00000f, .28240f, .56470f);
        map[18] = new Color(.00000f, .56470f, .56470f);
        map[19] = new Color(.00000f, .84710f, .56470f);
        map[20] = new Color(.00000f, .00000f, .70590f);
        map[21] = new Color(.00000f, .28240f, .70590f);
        map[22] = new Color(.00000f, .56470f, .70590f);
        map[23] = new Color(.00000f, .84710f, .70590f);
        map[24] = new Color(.00000f, .00000f, .84710f);
        map[25] = new Color(.00000f, .28240f, .84710f);
        map[26] = new Color(.00000f, .56470f, .84710f);
        map[27] = new Color(.00000f, .84710f, .84710f);
        map[28] = new Color(.00000f, .00000f, .98820f);
        map[29] = new Color(.00000f, .28240f, .98820f);
        map[30] = new Color(.00000f, .56470f, .98820f);
        map[31] = new Color(.00000f, .84710f, .98820f);
        map[32] = new Color(.00000f, .00000f, .00000f);
        map[33] = new Color(.32940f, .28240f, .00000f);
        map[34] = new Color(.32940f, .56470f, .00000f);
        map[35] = new Color(.32940f, .84710f, .00000f);
        map[36] = new Color(.32940f, .00000f, .14120f);
        map[37] = new Color(.32940f, .28240f, .14120f);
        map[38] = new Color(.32940f, .56470f, .14120f);
        map[39] = new Color(.32940f, .84710f, .14120f);
        map[40] = new Color(.32940f, .00000f, .28240f);
        map[41] = new Color(.32940f, .28240f, .28240f);
        map[42] = new Color(.32940f, .56470f, .28240f);
        map[43] = new Color(.32940f, .84710f, .28240f);
        map[44] = new Color(.32940f, .00000f, .42350f);
        map[45] = new Color(.32940f, .28240f, .42350f);
        map[46] = new Color(.32940f, .56470f, .42350f);
        map[47] = new Color(.32940f, .84710f, .42350f);
        map[48] = new Color(.32940f, .00000f, .56470f);
        map[49] = new Color(.32940f, .28240f, .56470f);
        map[50] = new Color(.32940f, .56470f, .56470f);
        map[51] = new Color(.32940f, .84710f, .56470f);
        map[52] = new Color(.32940f, .00000f, .70590f);
        map[53] = new Color(.32940f, .28240f, .70590f);
        map[54] = new Color(.32940f, .56470f, .70590f);
        map[55] = new Color(.32940f, .84710f, .70590f);
        map[56] = new Color(.32940f, .00000f, .84710f);
        map[57] = new Color(.32940f, .28240f, .84710f);
        map[58] = new Color(.32940f, .56470f, .84710f);
        map[59] = new Color(.32940f, .84710f, .84710f);
        map[60] = new Color(.32940f, .00000f, .98820f);
        map[61] = new Color(.32940f, .28240f, .98820f);
        map[62] = new Color(.32940f, .56470f, .98820f);
        map[63] = new Color(.32940f, .84710f, .98820f);
        map[64] = new Color(.32940f, .00000f, .00000f);
        map[65] = new Color(.65880f, .28240f, .00000f);
        map[66] = new Color(.65880f, .56470f, .00000f);
        map[67] = new Color(.65880f, .84710f, .00000f);
        map[68] = new Color(.65880f, .00000f, .14120f);
        map[69] = new Color(.65880f, .28240f, .14120f);
        map[70] = new Color(.65880f, .56470f, .14120f);
        map[71] = new Color(.65880f, .84710f, .14120f);
        map[72] = new Color(.65880f, .00000f, .28240f);
        map[73] = new Color(.65880f, .28240f, .28240f);
        map[74] = new Color(.65880f, .56470f, .28240f);
        map[75] = new Color(.65880f, .84710f, .28240f);
        map[76] = new Color(.65880f, .00000f, .42350f);
        map[77] = new Color(.65880f, .28240f, .42350f);
        map[78] = new Color(.65880f, .56470f, .42350f);
        map[79] = new Color(.65880f, .84710f, .42350f);
        map[80] = new Color(.65880f, .00000f, .56470f);
        map[81] = new Color(.65880f, .28240f, .56470f);
        map[82] = new Color(.65880f, .56470f, .56470f);
        map[83] = new Color(.65880f, .84710f, .56470f);
        map[84] = new Color(.65880f, .00000f, .70590f);
        map[85] = new Color(.65880f, .28240f, .70590f);
        map[86] = new Color(.65880f, .56470f, .70590f);
        map[87] = new Color(.65880f, .84710f, .70590f);
        map[88] = new Color(.65880f, .00000f, .84710f);
        map[89] = new Color(.65880f, .28240f, .84710f);
        map[90] = new Color(.65880f, .56470f, .84710f);
        map[91] = new Color(.65880f, .84710f, .84710f);
        map[92] = new Color(.65880f, .00000f, .98820f);
        map[93] = new Color(.65880f, .28240f, .98820f);
        map[94] = new Color(.65880f, .56470f, .98820f);
        map[95] = new Color(.65880f, .84710f, .98820f);
        map[96] = new Color(.65880f, .00000f, .00000f);
        map[97] = new Color(.98820f, .28240f, .00000f);
        map[98] = new Color(.98820f, .56470f, .00000f);
        map[99] = new Color(.98820f, .84710f, .00000f);
        map[100] = new Color(.98820f, .00000f, .14120f);
        map[101] = new Color(.98820f, .28240f, .14120f);
        map[102] = new Color(.98820f, .56470f, .14120f);
        map[103] = new Color(.98820f, .84710f, .14120f);
        map[104] = new Color(.98820f, .00000f, .28240f);
        map[105] = new Color(.98820f, .28240f, .28240f);
        map[106] = new Color(.98820f, .56470f, .28240f);
        map[107] = new Color(.98820f, .84710f, .28240f);
        map[108] = new Color(.98820f, .00000f, .42350f);
        map[109] = new Color(.98820f, .28240f, .42350f);
        map[110] = new Color(.98820f, .56470f, .42350f);
        map[111] = new Color(.98820f, .84710f, .42350f);
        map[112] = new Color(.98820f, .00000f, .56470f);
        map[113] = new Color(.98820f, .28240f, .56470f);
        map[114] = new Color(.98820f, .56470f, .56470f);
        map[115] = new Color(.98820f, .84710f, .56470f);
        map[116] = new Color(.98820f, .00000f, .70590f);
        map[117] = new Color(.98820f, .28240f, .70590f);
        map[118] = new Color(.98820f, .56470f, .70590f);
        map[119] = new Color(.98820f, .84710f, .70590f);
        map[120] = new Color(.98820f, .00000f, .84710f);
        map[121] = new Color(.98820f, .28240f, .84710f);
        map[122] = new Color(.98820f, .56470f, .84710f);
        map[123] = new Color(.98820f, .84710f, .84710f);
        map[124] = new Color(.98820f, .00000f, .98820f);
        map[125] = new Color(.98820f, .28240f, .98820f);
        map[126] = new Color(.98820f, .56470f, .98820f);
        map[127] = new Color(.98820f, .84710f, .98820f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
