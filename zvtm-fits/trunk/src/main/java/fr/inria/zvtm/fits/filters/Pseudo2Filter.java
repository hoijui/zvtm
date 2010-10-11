/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class Pseudo2Filter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = new Color(.30980f, .29020f, .22350f);
        map[1] = new Color(.33330f, .31760f, .25490f);
        map[2] = new Color(.35690f, .34510f, .29020f);
        map[3] = new Color(.38040f, .37650f, .32550f);
        map[4] = new Color(.40390f, .40390f, .36470f);
        map[5] = new Color(.42750f, .43530f, .40390f);
        map[6] = new Color(.45100f, .46670f, .44310f);
        map[7] = new Color(.47450f, .49800f, .48630f);
        map[8] = new Color(.50200f, .53330f, .53330f);
        map[9] = new Color(.52550f, .56860f, .58040f);
        map[10] = new Color(.55290f, .60000f, .62750f);
        map[11] = new Color(.58040f, .63530f, .67840f);
        map[12] = new Color(.60780f, .67060f, .72940f);
        map[13] = new Color(.63530f, .70980f, .78430f);
        map[14] = new Color(.66270f, .74510f, .72940f);
        map[15] = new Color(.69020f, .78430f, .68240f);
        map[16] = new Color(.71760f, .82350f, .64310f);
        map[17] = new Color(.74900f, .78820f, .60390f);
        map[18] = new Color(.77650f, .75690f, .57250f);
        map[19] = new Color(.80780f, .73330f, .54120f);
        map[20] = new Color(.83920f, .70590f, .51370f);
        map[21] = new Color(.87060f, .68240f, .49410f);
        map[22] = new Color(.83920f, .66670f, .47840f);
        map[23] = new Color(.81570f, .65100f, .46270f);
        map[24] = new Color(.79220f, .63530f, .45490f);
        map[25] = new Color(.77250f, .61960f, .44710f);
        map[26] = new Color(.74900f, .60780f, .44710f);
        map[27] = new Color(.73330f, .60000f, .44710f);
        map[28] = new Color(.71760f, .59220f, .45490f);
        map[29] = new Color(.70590f, .58820f, .46270f);
        map[30] = new Color(.69020f, .58040f, .47840f);
        map[31] = new Color(.67840f, .58040f, .49410f);
        map[32] = new Color(.66670f, .58040f, .51370f);
        map[33] = new Color(.65490f, .58040f, .54120f);
        map[34] = new Color(.64710f, .58040f, .57250f);
        map[35] = new Color(.63530f, .58820f, .60390f);
        map[36] = new Color(.63140f, .59220f, .64310f);
        map[37] = new Color(.62750f, .60000f, .68240f);
        map[38] = new Color(.62350f, .60780f, .72940f);
        map[39] = new Color(.61960f, .61960f, .78430f);
        map[40] = new Color(.61570f, .63530f, .72940f);
        map[41] = new Color(.61180f, .65100f, .68240f);
        map[42] = new Color(.61180f, .66670f, .64310f);
        map[43] = new Color(.61180f, .68240f, .60390f);
        map[44] = new Color(.61180f, .70590f, .57250f);
        map[45] = new Color(.61570f, .73330f, .54120f);
        map[46] = new Color(.61960f, .75690f, .51370f);
        map[47] = new Color(.62350f, .78820f, .49410f);
        map[48] = new Color(.62750f, .82350f, .47840f);
        map[49] = new Color(.63530f, .78820f, .46270f);
        map[50] = new Color(.64310f, .75690f, .45490f);
        map[51] = new Color(.65100f, .73330f, .44710f);
        map[52] = new Color(.66270f, .70590f, .44710f);
        map[53] = new Color(.67450f, .68240f, .44710f);
        map[54] = new Color(.68240f, .66670f, .45490f);
        map[55] = new Color(.69410f, .65100f, .46270f);
        map[56] = new Color(.70980f, .63530f, .47840f);
        map[57] = new Color(.72940f, .61960f, .49410f);
        map[58] = new Color(.74510f, .60780f, .51370f);
        map[59] = new Color(.76080f, .60000f, .54120f);
        map[60] = new Color(.79220f, .59220f, .57250f);
        map[61] = new Color(.81960f, .58820f, .60390f);
        map[62] = new Color(.85490f, .58040f, .64310f);
        map[63] = new Color(.89800f, .58040f, .68240f);
        map[64] = new Color(.91370f, .58040f, .72940f);
        map[65] = new Color(.89800f, .58040f, .78430f);
        map[66] = new Color(.88630f, .58040f, .72940f);
        map[67] = new Color(.87840f, .58820f, .68240f);
        map[68] = new Color(.87060f, .59220f, .64310f);
        map[69] = new Color(.86270f, .60000f, .60390f);
        map[70] = new Color(.85880f, .60780f, .57250f);
        map[71] = new Color(.85100f, .61960f, .54120f);
        map[72] = new Color(.85100f, .63530f, .51370f);
        map[73] = new Color(.85100f, .65490f, .49410f);
        map[74] = new Color(.85490f, .68240f, .47840f);
        map[75] = new Color(.85490f, .71370f, .46270f);
        map[76] = new Color(.85490f, .75690f, .45490f);
        map[77] = new Color(.86270f, .80390f, .44710f);
        map[78] = new Color(.86670f, .85100f, .44710f);
        map[79] = new Color(.87450f, .90980f, .44710f);
        map[80] = new Color(.88240f, .97250f, .45490f);
        map[81] = new Color(.89020f, .96080f, .46270f);
        map[82] = new Color(.90200f, .95290f, .47840f);
        map[83] = new Color(.91370f, .94900f, .49410f);
        map[84] = new Color(.92550f, .94120f, .51370f);
        map[85] = new Color(.94120f, .94120f, .54120f);
        map[86] = new Color(.95290f, .94510f, .57250f);
        map[87] = new Color(.97250f, .94900f, .60390f);
        map[88] = new Color(.98820f, .95290f, .65100f);
        map[89] = new Color(1.00000f, .95690f, .70590f);
        map[90] = new Color(1.00000f, .96860f, .76860f);
        map[91] = new Color(1.00000f, .98040f, .84310f);
        map[92] = new Color(.98820f, .99220f, .80780f);
        map[93] = new Color(.93330f, 1.00000f, .78430f);
        map[94] = new Color(.88240f, 1.00000f, .76470f);
        map[95] = new Color(.83140f, 1.00000f, .74900f);
        map[96] = new Color(.78040f, 1.00000f, .73730f);
        map[97] = new Color(.73330f, 1.00000f, .72940f);
        map[98] = new Color(.69020f, 1.00000f, .72940f);
        map[99] = new Color(.64310f, .93730f, .73330f);
        map[100] = new Color(.60390f, .86670f, .74120f);
        map[101] = new Color(.56080f, .79610f, .75290f);
        map[102] = new Color(.52940f, .73330f, .76860f);
        map[103] = new Color(.50200f, .67060f, .78820f);
        map[104] = new Color(.47060f, .61180f, .81570f);
        map[105] = new Color(.44310f, .55290f, .84710f);
        map[106] = new Color(.42350f, .50590f, .88240f);
        map[107] = new Color(.41180f, .45490f, .92550f);
        map[108] = new Color(.40000f, .41570f, .83920f);
        map[109] = new Color(.38820f, .37650f, .75690f);
        map[110] = new Color(.38040f, .34120f, .67840f);
        map[111] = new Color(.37250f, .31760f, .61570f);
        map[112] = new Color(.37250f, .29800f, .55690f);
        map[113] = new Color(.37650f, .28630f, .50200f);
        map[114] = new Color(.38040f, .28240f, .45880f);
        map[115] = new Color(.38820f, .27840f, .41960f);
        map[116] = new Color(.40000f, .28240f, .40000f);
        map[117] = new Color(.41570f, .29020f, .38430f);
        map[118] = new Color(.43530f, .30590f, .37650f);
        map[119] = new Color(.45880f, .32550f, .38040f);
        map[120] = new Color(.48240f, .35290f, .39220f);
        map[121] = new Color(.51760f, .39220f, .40780f);
        map[122] = new Color(.55690f, .43920f, .43530f);
        map[123] = new Color(.60000f, .49800f, .47450f);
        map[124] = new Color(.65490f, .56080f, .52550f);
        map[125] = new Color(.70980f, .62750f, .58430f);
        map[126] = new Color(.77250f, .70200f, .65100f);
        map[127] = new Color(.83530f, .78040f, .73330f);
    }

    public Pseudo2Filter(){}

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
