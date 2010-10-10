/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class BackgrFilter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public BackgrFilter(){
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.03140f, .03140f, .03140f);
        map[2] = new Color(.06270f, .06270f, .06270f);
        map[3] = new Color(.09410f, .09410f, .09410f);
        map[4] = new Color(.12550f, .12550f, .12550f);
        map[5] = new Color(.15690f, .15690f, .15690f);
        map[6] = new Color(.19220f, .19220f, .19220f);
        map[7] = new Color(.22350f, .22350f, .22350f);
        map[8] = new Color(.25490f, .25490f, .25490f);
        map[9] = new Color(.28630f, .28630f, .28630f);
        map[10] = new Color(.31760f, .31760f, .31760f);
        map[11] = new Color(.34900f, .34900f, .34900f);
        map[12] = new Color(.38040f, .38040f, .38040f);
        map[13] = new Color(.41180f, .41180f, .41180f);
        map[14] = new Color(.44310f, .44310f, .44310f);
        map[15] = new Color(.47450f, .47450f, .47450f);
        map[16] = new Color(.50590f, .50590f, .50590f);
        map[17] = new Color(.54120f, .54120f, .54120f);
        map[18] = new Color(.57250f, .57250f, .57250f);
        map[19] = new Color(.60390f, .60390f, .60390f);
        map[20] = new Color(.63530f, .63530f, .63530f);
        map[21] = new Color(.66670f, .66670f, .66670f);
        map[22] = new Color(.69800f, .69800f, .69800f);
        map[23] = new Color(.72940f, .72940f, .72940f);
        map[24] = new Color(.76080f, .76080f, .76080f);
        map[25] = new Color(.79220f, .79220f, .79220f);
        map[26] = new Color(.82350f, .82350f, .82350f);
        map[27] = new Color(.85880f, .85880f, .85880f);
        map[28] = new Color(.89020f, .89020f, .89020f);
        map[29] = new Color(.92160f, .92160f, .92160f);
        map[30] = new Color(.95290f, .95290f, .95290f);
        map[31] = new Color(.98430f, .98430f, .98430f);
        map[32] = new Color(.00000f, .00000f, 1.00000f);
        map[33] = new Color(.00000f, .03140f, .96860f);
        map[34] = new Color(.00000f, .06270f, .93730f);
        map[35] = new Color(.00000f, .09410f, .90590f);
        map[36] = new Color(.00000f, .12550f, .87450f);
        map[37] = new Color(.00000f, .15690f, .83920f);
        map[38] = new Color(.00000f, .19220f, .80780f);
        map[39] = new Color(.00000f, .22350f, .77650f);
        map[40] = new Color(.00000f, .25490f, .74510f);
        map[41] = new Color(.00000f, .28630f, .71370f);
        map[42] = new Color(.00000f, .31760f, .68240f);
        map[43] = new Color(.00000f, .34900f, .65100f);
        map[44] = new Color(.00000f, .38040f, .61960f);
        map[45] = new Color(.00000f, .41180f, .58820f);
        map[46] = new Color(.00000f, .44310f, .55690f);
        map[47] = new Color(.00000f, .47450f, .52550f);
        map[48] = new Color(.00000f, .50590f, .49020f);
        map[49] = new Color(.00000f, .54120f, .45880f);
        map[50] = new Color(.00000f, .57250f, .42750f);
        map[51] = new Color(.00000f, .60390f, .39610f);
        map[52] = new Color(.00000f, .63530f, .36470f);
        map[53] = new Color(.00000f, .66670f, .33330f);
        map[54] = new Color(.00000f, .69800f, .30200f);
        map[55] = new Color(.00000f, .72940f, .27060f);
        map[56] = new Color(.00000f, .76080f, .23920f);
        map[57] = new Color(.00000f, .79220f, .20780f);
        map[58] = new Color(.00000f, .82350f, .17650f);
        map[59] = new Color(.00000f, .85880f, .14120f);
        map[60] = new Color(.00000f, .89020f, .10980f);
        map[61] = new Color(.00000f, .92160f, .07840f);
        map[62] = new Color(.00000f, .95290f, .04710f);
        map[63] = new Color(.00000f, .98430f, .01570f);
        map[64] = new Color(.00000f, 1.00000f, .00000f);
        map[65] = new Color(.03140f, 1.00000f, .00000f);
        map[66] = new Color(.06270f, 1.00000f, .00000f);
        map[67] = new Color(.09410f, 1.00000f, .00000f);
        map[68] = new Color(.12550f, 1.00000f, .00000f);
        map[69] = new Color(.15690f, 1.00000f, .00000f);
        map[70] = new Color(.19220f, 1.00000f, .00000f);
        map[71] = new Color(.22350f, 1.00000f, .00000f);
        map[72] = new Color(.25490f, 1.00000f, .00000f);
        map[73] = new Color(.28630f, 1.00000f, .00000f);
        map[74] = new Color(.31760f, 1.00000f, .00000f);
        map[75] = new Color(.34900f, 1.00000f, .00000f);
        map[76] = new Color(.38040f, 1.00000f, .00000f);
        map[77] = new Color(.41180f, 1.00000f, .00000f);
        map[78] = new Color(.44310f, 1.00000f, .00000f);
        map[79] = new Color(.47450f, 1.00000f, .00000f);
        map[80] = new Color(.50590f, 1.00000f, .00000f);
        map[81] = new Color(.54120f, 1.00000f, .00000f);
        map[82] = new Color(.57250f, 1.00000f, .00000f);
        map[83] = new Color(.60390f, 1.00000f, .00000f);
        map[84] = new Color(.63530f, 1.00000f, .00000f);
        map[85] = new Color(.66670f, 1.00000f, .00000f);
        map[86] = new Color(.69800f, 1.00000f, .00000f);
        map[87] = new Color(.72940f, 1.00000f, .00000f);
        map[88] = new Color(.76080f, 1.00000f, .00000f);
        map[89] = new Color(.79220f, 1.00000f, .00000f);
        map[90] = new Color(.82350f, 1.00000f, .00000f);
        map[91] = new Color(.85880f, 1.00000f, .00000f);
        map[92] = new Color(.89020f, 1.00000f, .00000f);
        map[93] = new Color(.92160f, 1.00000f, .00000f);
        map[94] = new Color(.95290f, 1.00000f, .00000f);
        map[95] = new Color(.98430f, 1.00000f, .00000f);
        map[96] = new Color(1.00000f, 1.00000f, .00000f);
        map[97] = new Color(1.00000f, .96860f, .00000f);
        map[98] = new Color(1.00000f, .93730f, .00000f);
        map[99] = new Color(1.00000f, .90590f, .00000f);
        map[100] = new Color(1.00000f, .87450f, .00000f);
        map[101] = new Color(1.00000f, .83920f, .00000f);
        map[102] = new Color(1.00000f, .80780f, .00000f);
        map[103] = new Color(1.00000f, .77650f, .00000f);
        map[104] = new Color(1.00000f, .74510f, .00000f);
        map[105] = new Color(1.00000f, .71370f, .00000f);
        map[106] = new Color(1.00000f, .68240f, .00000f);
        map[107] = new Color(1.00000f, .65100f, .00000f);
        map[108] = new Color(1.00000f, .61960f, .00000f);
        map[109] = new Color(1.00000f, .58820f, .00000f);
        map[110] = new Color(1.00000f, .55690f, .00000f);
        map[111] = new Color(1.00000f, .52550f, .00000f);
        map[112] = new Color(1.00000f, .49020f, .00000f);
        map[113] = new Color(1.00000f, .45880f, .00000f);
        map[114] = new Color(1.00000f, .42750f, .00000f);
        map[115] = new Color(1.00000f, .39610f, .00000f);
        map[116] = new Color(1.00000f, .36470f, .00000f);
        map[117] = new Color(1.00000f, .33330f, .00000f);
        map[118] = new Color(1.00000f, .30200f, .00000f);
        map[119] = new Color(1.00000f, .27060f, .00000f);
        map[120] = new Color(1.00000f, .23920f, .00000f);
        map[121] = new Color(1.00000f, .20780f, .00000f);
        map[122] = new Color(1.00000f, .17650f, .00000f);
        map[123] = new Color(1.00000f, .14120f, .00000f);
        map[124] = new Color(1.00000f, .10980f, .00000f);
        map[125] = new Color(1.00000f, .07840f, .00000f);
        map[126] = new Color(1.00000f, .04710f, .00000f);
        map[127] = new Color(1.00000f, .01570f, .00000f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
