/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class Idl4Filter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public Idl4Filter(){
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00000f, .00000f, .01570f);
        map[2] = new Color(.00000f, .00000f, .03140f);
        map[3] = new Color(.00000f, .00000f, .04710f);
        map[4] = new Color(.00000f, .00000f, .06270f);
        map[5] = new Color(.00000f, .00000f, .07840f);
        map[6] = new Color(.00000f, .00000f, .09800f);
        map[7] = new Color(.00000f, .00000f, .11370f);
        map[8] = new Color(.00000f, .00000f, .12940f);
        map[9] = new Color(.00000f, .00000f, .14510f);
        map[10] = new Color(.00000f, .00000f, .16080f);
        map[11] = new Color(.00000f, .00000f, .17650f);
        map[12] = new Color(.00000f, .00000f, .19610f);
        map[13] = new Color(.00000f, .00000f, .21180f);
        map[14] = new Color(.00000f, .00000f, .22750f);
        map[15] = new Color(.00000f, .00000f, .24310f);
        map[16] = new Color(.00000f, .00000f, .25880f);
        map[17] = new Color(.00000f, .02350f, .27450f);
        map[18] = new Color(.00000f, .04710f, .29410f);
        map[19] = new Color(.00000f, .07060f, .30980f);
        map[20] = new Color(.00000f, .09800f, .32550f);
        map[21] = new Color(.00000f, .12160f, .34120f);
        map[22] = new Color(.00000f, .14510f, .35690f);
        map[23] = new Color(.00000f, .16860f, .37250f);
        map[24] = new Color(.00000f, .19610f, .39220f);
        map[25] = new Color(.00000f, .21960f, .39220f);
        map[26] = new Color(.00000f, .24310f, .39220f);
        map[27] = new Color(.00000f, .26670f, .39220f);
        map[28] = new Color(.00000f, .29410f, .39220f);
        map[29] = new Color(.00000f, .31760f, .39220f);
        map[30] = new Color(.00000f, .34120f, .39220f);
        map[31] = new Color(.00000f, .36470f, .39220f);
        map[32] = new Color(.00000f, .39220f, .39220f);
        map[33] = new Color(.00000f, .41570f, .39220f);
        map[34] = new Color(.00000f, .43920f, .39220f);
        map[35] = new Color(.00000f, .46270f, .39220f);
        map[36] = new Color(.00000f, .49020f, .39220f);
        map[37] = new Color(.00000f, .51370f, .39220f);
        map[38] = new Color(.00000f, .53730f, .39220f);
        map[39] = new Color(.00000f, .56080f, .39220f);
        map[40] = new Color(.00000f, .58820f, .39220f);
        map[41] = new Color(.00000f, .58820f, .36470f);
        map[42] = new Color(.00000f, .58820f, .34120f);
        map[43] = new Color(.00000f, .58820f, .31760f);
        map[44] = new Color(.00000f, .58820f, .29410f);
        map[45] = new Color(.00000f, .58820f, .26670f);
        map[46] = new Color(.00000f, .58820f, .24310f);
        map[47] = new Color(.00000f, .58820f, .21960f);
        map[48] = new Color(.00000f, .58820f, .19610f);
        map[49] = new Color(.00000f, .58040f, .16860f);
        map[50] = new Color(.00000f, .57650f, .14510f);
        map[51] = new Color(.00000f, .57250f, .12160f);
        map[52] = new Color(.00000f, .56860f, .09800f);
        map[53] = new Color(.00000f, .56080f, .07060f);
        map[54] = new Color(.00000f, .55690f, .04710f);
        map[55] = new Color(.00000f, .55290f, .02350f);
        map[56] = new Color(.00000f, .54900f, .00000f);
        map[57] = new Color(.05880f, .52940f, .00000f);
        map[58] = new Color(.11760f, .50980f, .00000f);
        map[59] = new Color(.17650f, .49020f, .00000f);
        map[60] = new Color(.23530f, .47060f, .00000f);
        map[61] = new Color(.29410f, .45100f, .00000f);
        map[62] = new Color(.35290f, .43140f, .00000f);
        map[63] = new Color(.41180f, .41180f, .00000f);
        map[64] = new Color(.47060f, .39220f, .00000f);
        map[65] = new Color(.50980f, .34120f, .00000f);
        map[66] = new Color(.54900f, .29410f, .00000f);
        map[67] = new Color(.58820f, .24310f, .00000f);
        map[68] = new Color(.62750f, .19610f, .00000f);
        map[69] = new Color(.66670f, .14510f, .00000f);
        map[70] = new Color(.70590f, .09800f, .00000f);
        map[71] = new Color(.74510f, .04710f, .00000f);
        map[72] = new Color(.78430f, .00000f, .00000f);
        map[73] = new Color(.78820f, .01570f, .00000f);
        map[74] = new Color(.79220f, .03530f, .00000f);
        map[75] = new Color(.79610f, .05100f, .00000f);
        map[76] = new Color(.80000f, .07060f, .00000f);
        map[77] = new Color(.80390f, .09020f, .00000f);
        map[78] = new Color(.80780f, .10590f, .00000f);
        map[79] = new Color(.81180f, .12550f, .00000f);
        map[80] = new Color(.81570f, .14120f, .00000f);
        map[81] = new Color(.81960f, .16080f, .00000f);
        map[82] = new Color(.82350f, .18040f, .00000f);
        map[83] = new Color(.82750f, .19610f, .00000f);
        map[84] = new Color(.83140f, .21570f, .00000f);
        map[85] = new Color(.83530f, .23140f, .00000f);
        map[86] = new Color(.83920f, .25100f, .00000f);
        map[87] = new Color(.84310f, .27060f, .00000f);
        map[88] = new Color(.84710f, .28630f, .00000f);
        map[89] = new Color(.85100f, .30590f, .00000f);
        map[90] = new Color(.85490f, .32550f, .00000f);
        map[91] = new Color(.85880f, .34120f, .00000f);
        map[92] = new Color(.86270f, .36080f, .00000f);
        map[93] = new Color(.86670f, .37650f, .00000f);
        map[94] = new Color(.87060f, .39610f, .00000f);
        map[95] = new Color(.87450f, .41570f, .00000f);
        map[96] = new Color(.87840f, .43140f, .00000f);
        map[97] = new Color(.88240f, .45100f, .00000f);
        map[98] = new Color(.88630f, .46670f, .00000f);
        map[99] = new Color(.89020f, .48630f, .00000f);
        map[100] = new Color(.89410f, .50590f, .00000f);
        map[101] = new Color(.89800f, .52160f, .00000f);
        map[102] = new Color(.90200f, .54120f, .00000f);
        map[103] = new Color(.90590f, .55690f, .00000f);
        map[104] = new Color(.90980f, .57650f, .00000f);
        map[105] = new Color(.91370f, .59610f, .00000f);
        map[106] = new Color(.91760f, .61180f, .00000f);
        map[107] = new Color(.92160f, .63140f, .00000f);
        map[108] = new Color(.92550f, .65100f, .00000f);
        map[109] = new Color(.92940f, .66670f, .00000f);
        map[110] = new Color(.93330f, .68630f, .00000f);
        map[111] = new Color(.93730f, .70200f, .00000f);
        map[112] = new Color(.94120f, .72160f, .00000f);
        map[113] = new Color(.94510f, .74120f, .00000f);
        map[114] = new Color(.94900f, .75690f, .00000f);
        map[115] = new Color(.95290f, .77650f, .00000f);
        map[116] = new Color(.95690f, .79220f, .00000f);
        map[117] = new Color(.96080f, .81180f, .00000f);
        map[118] = new Color(.96470f, .83140f, .00000f);
        map[119] = new Color(.96860f, .84710f, .00000f);
        map[120] = new Color(.97250f, .86670f, .00000f);
        map[121] = new Color(.97650f, .88630f, .00000f);
        map[122] = new Color(.98040f, .90200f, .00000f);
        map[123] = new Color(.98430f, .92160f, .00000f);
        map[124] = new Color(.98820f, .93730f, .00000f);
        map[125] = new Color(.99220f, .95690f, .00000f);
        map[126] = new Color(.99610f, .97650f, .00000f);
        map[127] = new Color(1.00000f, .99220f, .00000f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
