/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class FlagFilter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.98040f, .98040f, .98040f);
        map[2] = new Color(.98040f, .98040f, .98040f);
        map[3] = new Color(.98040f, .98040f, .98040f);
        map[4] = new Color(.98040f, .98040f, .98040f);
        map[5] = new Color(.98040f, .98040f, .98040f);
        map[6] = new Color(.98040f, .98040f, .98040f);
        map[7] = new Color(.98040f, .98040f, .98040f);
        map[8] = new Color(.98040f, .98040f, .98040f);
        map[9] = new Color(.98040f, .98040f, .98040f);
        map[10] = new Color(.98040f, .98040f, .98040f);
        map[11] = new Color(.98040f, .98040f, .98040f);
        map[12] = new Color(.98040f, .98040f, .98040f);
        map[13] = new Color(.98040f, .98040f, .98040f);
        map[14] = new Color(.98040f, .98040f, .98040f);
        map[15] = new Color(.98040f, .98040f, .98040f);
        map[16] = new Color(.98040f, .98040f, .98040f);
        map[17] = new Color(.98040f, .98040f, .98040f);
        map[18] = new Color(.98040f, .98040f, .98040f);
        map[19] = new Color(.98040f, .98040f, .98040f);
        map[20] = new Color(.98040f, .98040f, .98040f);
        map[21] = new Color(.98040f, .98040f, .98040f);
        map[22] = new Color(.98040f, .98040f, .98040f);
        map[23] = new Color(.98040f, .98040f, .98040f);
        map[24] = new Color(.98040f, .98040f, .98040f);
        map[25] = new Color(.98040f, .98040f, .98040f);
        map[26] = new Color(.98040f, .98040f, .98040f);
        map[27] = new Color(.98040f, .98040f, .98040f);
        map[28] = new Color(.98040f, .98040f, .98040f);
        map[29] = new Color(.98040f, .98040f, .98040f);
        map[30] = new Color(.98040f, .98040f, .98040f);
        map[31] = new Color(.98040f, .98040f, .98040f);
        map[32] = new Color(.98040f, .98040f, .98040f);
        map[33] = new Color(.98040f, .98040f, .98040f);
        map[34] = new Color(.98040f, .98040f, .98040f);
        map[35] = new Color(.98040f, .98040f, .98040f);
        map[36] = new Color(.98040f, .98040f, .98040f);
        map[37] = new Color(.98040f, .98040f, .98040f);
        map[38] = new Color(.98040f, .98040f, .98040f);
        map[39] = new Color(.98040f, .98040f, .98040f);
        map[40] = new Color(.98040f, .98040f, .98040f);
        map[41] = new Color(.98040f, .98040f, .98040f);
        map[42] = new Color(.98040f, .98040f, .98040f);
        map[43] = new Color(.98040f, .98040f, .98040f);
        map[44] = new Color(.98040f, .98040f, .98040f);
        map[45] = new Color(.98040f, .98040f, .98040f);
        map[46] = new Color(.98040f, .98040f, .98040f);
        map[47] = new Color(.98040f, .98040f, .98040f);
        map[48] = new Color(.98040f, .98040f, .98040f);
        map[49] = new Color(.98040f, .98040f, .98040f);
        map[50] = new Color(.98040f, .98040f, .98040f);
        map[51] = new Color(.98040f, .98040f, .98040f);
        map[52] = new Color(.98040f, .98040f, .98040f);
        map[53] = new Color(.98040f, .98040f, .98040f);
        map[54] = new Color(.98040f, .98040f, .98040f);
        map[55] = new Color(.98040f, .98040f, .98040f);
        map[56] = new Color(.98040f, .98040f, .98040f);
        map[57] = new Color(.98040f, .98040f, .98040f);
        map[58] = new Color(.98040f, .98040f, .98040f);
        map[59] = new Color(.98040f, .98040f, .98040f);
        map[60] = new Color(.98040f, .98040f, .98040f);
        map[61] = new Color(.98040f, .98040f, .98040f);
        map[62] = new Color(.98040f, .98040f, .98040f);
        map[63] = new Color(.98040f, .98040f, .98040f);
        map[64] = new Color(.98040f, .98040f, .98040f);
        map[65] = new Color(.98040f, .98040f, .98040f);
        map[66] = new Color(.98040f, .98040f, .98040f);
        map[67] = new Color(.98040f, .98040f, .98040f);
        map[68] = new Color(.98040f, .98040f, .98040f);
        map[69] = new Color(.98040f, .98040f, .98040f);
        map[70] = new Color(.98040f, .98040f, .98040f);
        map[71] = new Color(.98040f, .98040f, .98040f);
        map[72] = new Color(.98040f, .98040f, .98040f);
        map[73] = new Color(.98040f, .98040f, .98040f);
        map[74] = new Color(.98040f, .98040f, .98040f);
        map[75] = new Color(.98040f, .98040f, .98040f);
        map[76] = new Color(.98040f, .98040f, .98040f);
        map[77] = new Color(.98040f, .98040f, .98040f);
        map[78] = new Color(.98040f, .98040f, .98040f);
        map[79] = new Color(.98040f, .98040f, .98040f);
        map[80] = new Color(.98040f, .98040f, .98040f);
        map[81] = new Color(.98040f, .98040f, .98040f);
        map[82] = new Color(.98040f, .98040f, .98040f);
        map[83] = new Color(.98040f, .98040f, .98040f);
        map[84] = new Color(.98040f, .98040f, .98040f);
        map[85] = new Color(.98040f, .98040f, .98040f);
        map[86] = new Color(.98040f, .98040f, .98040f);
        map[87] = new Color(.98040f, .98040f, .98040f);
        map[88] = new Color(.98040f, .98040f, .98040f);
        map[89] = new Color(.98040f, .98040f, .98040f);
        map[90] = new Color(.98040f, .98040f, .98040f);
        map[91] = new Color(.98040f, .98040f, .98040f);
        map[92] = new Color(.98040f, .98040f, .98040f);
        map[93] = new Color(.98040f, .98040f, .98040f);
        map[94] = new Color(.98040f, .98040f, .98040f);
        map[95] = new Color(.98040f, .98040f, .98040f);
        map[96] = new Color(.98040f, .98040f, .98040f);
        map[97] = new Color(.98040f, .98040f, .98040f);
        map[98] = new Color(.98040f, .98040f, .98040f);
        map[99] = new Color(.98040f, .98040f, .98040f);
        map[100] = new Color(.98040f, .98040f, .98040f);
        map[101] = new Color(.98040f, .98040f, .98040f);
        map[102] = new Color(.98040f, .98040f, .98040f);
        map[103] = new Color(.98040f, .98040f, .98040f);
        map[104] = new Color(.98040f, .98040f, .98040f);
        map[105] = new Color(.98040f, .98040f, .98040f);
        map[106] = new Color(.98040f, .98040f, .98040f);
        map[107] = new Color(.98040f, .98040f, .98040f);
        map[108] = new Color(.98040f, .98040f, .98040f);
        map[109] = new Color(.98040f, .98040f, .98040f);
        map[110] = new Color(.98040f, .98040f, .98040f);
        map[111] = new Color(.98040f, .98040f, .98040f);
        map[112] = new Color(.98040f, .98040f, .98040f);
        map[113] = new Color(.98040f, .98040f, .98040f);
        map[114] = new Color(.98040f, .98040f, .98040f);
        map[115] = new Color(.98040f, .98040f, .98040f);
        map[116] = new Color(.98040f, .98040f, .98040f);
        map[117] = new Color(.98040f, .98040f, .98040f);
        map[118] = new Color(.98040f, .98040f, .98040f);
        map[119] = new Color(.98040f, .98040f, .98040f);
        map[120] = new Color(.98040f, .98040f, .98040f);
        map[121] = new Color(.98040f, .98040f, .98040f);
        map[122] = new Color(.98040f, .98040f, .98040f);
        map[123] = new Color(.98040f, .98040f, .98040f);
        map[124] = new Color(.98040f, .98040f, .98040f);
        map[125] = new Color(.98040f, .98040f, .98040f);
        map[126] = new Color(.98040f, .98040f, .98040f);
        map[127] = new Color(.98040f, .98040f, .98040f);
    }

    public FlagFilter(){}

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
