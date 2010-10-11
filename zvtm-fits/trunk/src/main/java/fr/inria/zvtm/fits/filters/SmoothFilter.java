/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class SmoothFilter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.00000f, .00000f, 1.00000f);
        map[1] = new Color(.03530f, .00000f, .96470f);
        map[2] = new Color(.06670f, .00000f, .93330f);
        map[3] = new Color(.10200f, .00000f, .89800f);
        map[4] = new Color(.13730f, .00000f, .86270f);
        map[5] = new Color(.16860f, .00000f, .83140f);
        map[6] = new Color(.20390f, .00000f, .79610f);
        map[7] = new Color(.23920f, .00000f, .76080f);
        map[8] = new Color(.27060f, .00000f, .72940f);
        map[9] = new Color(.30590f, .00000f, .69410f);
        map[10] = new Color(.33730f, .00000f, .66270f);
        map[11] = new Color(.37250f, .00000f, .62750f);
        map[12] = new Color(.40780f, .00000f, .59220f);
        map[13] = new Color(.43920f, .00000f, .56080f);
        map[14] = new Color(.47450f, .00000f, .52550f);
        map[15] = new Color(.50980f, .00000f, .49020f);
        map[16] = new Color(.54120f, .00000f, .45880f);
        map[17] = new Color(.57650f, .00000f, .42350f);
        map[18] = new Color(.61180f, .00000f, .38820f);
        map[19] = new Color(.64310f, .00000f, .35690f);
        map[20] = new Color(.67840f, .00000f, .32160f);
        map[21] = new Color(.71370f, .00000f, .28630f);
        map[22] = new Color(.74510f, .00000f, .25490f);
        map[23] = new Color(.78040f, .00000f, .21960f);
        map[24] = new Color(.81180f, .00000f, .18820f);
        map[25] = new Color(.84710f, .00000f, .15290f);
        map[26] = new Color(.88240f, .00000f, .11760f);
        map[27] = new Color(.91370f, .00000f, .08630f);
        map[28] = new Color(.94900f, .00000f, .05100f);
        map[29] = new Color(.98430f, .00000f, .01570f);
        map[30] = new Color(1.00000f, .00000f, .00000f);
        map[31] = new Color(1.00000f, .01960f, .00000f);
        map[32] = new Color(1.00000f, .03920f, .00000f);
        map[33] = new Color(1.00000f, .05880f, .00000f);
        map[34] = new Color(1.00000f, .08240f, .00000f);
        map[35] = new Color(1.00000f, .10200f, .00000f);
        map[36] = new Color(1.00000f, .12160f, .00000f);
        map[37] = new Color(1.00000f, .14120f, .00000f);
        map[38] = new Color(.99610f, .16080f, .00000f);
        map[39] = new Color(.99610f, .18040f, .00000f);
        map[40] = new Color(.99610f, .20390f, .00000f);
        map[41] = new Color(.99610f, .22350f, .00000f);
        map[42] = new Color(.99610f, .24310f, .00000f);
        map[43] = new Color(.99610f, .26270f, .00000f);
        map[44] = new Color(.99610f, .28240f, .00000f);
        map[45] = new Color(.99610f, .30200f, .00000f);
        map[46] = new Color(.99610f, .32160f, .00000f);
        map[47] = new Color(.99610f, .34510f, .00000f);
        map[48] = new Color(.99610f, .36470f, .00000f);
        map[49] = new Color(.99610f, .38430f, .00000f);
        map[50] = new Color(.99610f, .40390f, .00000f);
        map[51] = new Color(.99610f, .42350f, .00000f);
        map[52] = new Color(.99610f, .44310f, .00000f);
        map[53] = new Color(.99220f, .46670f, .00000f);
        map[54] = new Color(.99220f, .48630f, .00000f);
        map[55] = new Color(.99220f, .50590f, .00000f);
        map[56] = new Color(.99220f, .52550f, .00000f);
        map[57] = new Color(.99220f, .54510f, .00000f);
        map[58] = new Color(.99220f, .56470f, .00000f);
        map[59] = new Color(.99220f, .58430f, .00000f);
        map[60] = new Color(.99220f, .60000f, .00000f);
        map[61] = new Color(.99220f, .61180f, .00000f);
        map[62] = new Color(.99220f, .61960f, .00000f);
        map[63] = new Color(.99220f, .63140f, .00000f);
        map[64] = new Color(.99220f, .64310f, .00000f);
        map[65] = new Color(.98820f, .65100f, .00000f);
        map[66] = new Color(.98820f, .66270f, .00000f);
        map[67] = new Color(.98820f, .67450f, .00000f);
        map[68] = new Color(.98820f, .68240f, .00000f);
        map[69] = new Color(.98820f, .69410f, .00000f);
        map[70] = new Color(.98820f, .70200f, .00000f);
        map[71] = new Color(.98820f, .71370f, .00000f);
        map[72] = new Color(.98820f, .72550f, .00000f);
        map[73] = new Color(.98820f, .73330f, .00000f);
        map[74] = new Color(.98820f, .74510f, .00000f);
        map[75] = new Color(.98430f, .75290f, .00000f);
        map[76] = new Color(.98430f, .76470f, .00000f);
        map[77] = new Color(.98430f, .77250f, .00000f);
        map[78] = new Color(.98430f, .78430f, .00000f);
        map[79] = new Color(.98430f, .79610f, .00000f);
        map[80] = new Color(.98430f, .80390f, .00000f);
        map[81] = new Color(.98430f, .81570f, .00000f);
        map[82] = new Color(.98430f, .82750f, .00000f);
        map[83] = new Color(.98430f, .83530f, .00000f);
        map[84] = new Color(.98430f, .84710f, .00000f);
        map[85] = new Color(.98040f, .85490f, .00000f);
        map[86] = new Color(.98040f, .86670f, .00000f);
        map[87] = new Color(.98040f, .87840f, .00000f);
        map[88] = new Color(.98040f, .88630f, .00000f);
        map[89] = new Color(.98040f, .89800f, .00000f);
        map[90] = new Color(.98040f, .90200f, .00000f);
        map[91] = new Color(.94900f, .87060f, .00000f);
        map[92] = new Color(.91370f, .83920f, .00000f);
        map[93] = new Color(.88240f, .81180f, .00000f);
        map[94] = new Color(.84710f, .78040f, .00000f);
        map[95] = new Color(.81570f, .74900f, .00000f);
        map[96] = new Color(.78040f, .71760f, .00000f);
        map[97] = new Color(.74900f, .68630f, .00000f);
        map[98] = new Color(.71370f, .65880f, .00000f);
        map[99] = new Color(.68240f, .62750f, .00000f);
        map[100] = new Color(.64710f, .59610f, .00000f);
        map[101] = new Color(.61570f, .56470f, .00000f);
        map[102] = new Color(.58040f, .53330f, .00000f);
        map[103] = new Color(.54900f, .50590f, .00000f);
        map[104] = new Color(.51370f, .47450f, .00000f);
        map[105] = new Color(.48240f, .44310f, .00000f);
        map[106] = new Color(.44710f, .41180f, .00000f);
        map[107] = new Color(.41570f, .38040f, .00000f);
        map[108] = new Color(.38040f, .35290f, .00000f);
        map[109] = new Color(.34900f, .32160f, .00000f);
        map[110] = new Color(.31760f, .29020f, .00000f);
        map[111] = new Color(.28240f, .25880f, .00000f);
        map[112] = new Color(.25100f, .22750f, .00000f);
        map[113] = new Color(.21570f, .20000f, .00000f);
        map[114] = new Color(.18430f, .16860f, .00000f);
        map[115] = new Color(.14900f, .13730f, .00000f);
        map[116] = new Color(.11760f, .10590f, .00000f);
        map[117] = new Color(.08240f, .07450f, .00000f);
        map[118] = new Color(.05100f, .04710f, .00000f);
        map[119] = new Color(.01570f, .01570f, .00000f);
        map[120] = new Color(.00000f, .00000f, .00000f);
        map[121] = new Color(.00000f, .00000f, .00000f);
        map[122] = new Color(.00000f, .00000f, .00000f);
        map[123] = new Color(.00000f, .00000f, .00000f);
        map[124] = new Color(.00000f, .00000f, .00000f);
        map[125] = new Color(.00000f, .00000f, .00000f);
        map[126] = new Color(.00000f, .00000f, .00000f);
        map[127] = new Color(.00000f, .00000f, .00000f);
    }

    public SmoothFilter(){}

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
