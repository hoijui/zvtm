/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

public class RedFilter extends RGBImageFilter {

    private Color[] map = new Color[128];

    public RedFilter(){
        map[0] = new Color(.00000f, .00000f, .00000f);
        map[1] = new Color(.00780f, .00000f, .00000f);
        map[2] = new Color(.01570f, .00000f, .00000f);
        map[3] = new Color(.02350f, .00000f, .00000f);
        map[4] = new Color(.03140f, .00000f, .00000f);
        map[5] = new Color(.03920f, .00000f, .00000f);
        map[6] = new Color(.04710f, .00000f, .00000f);
        map[7] = new Color(.05490f, .00000f, .00000f);
        map[8] = new Color(.06270f, .00000f, .00000f);
        map[9] = new Color(.07060f, .00000f, .00000f);
        map[10] = new Color(.07840f, .00000f, .00000f);
        map[11] = new Color(.08630f, .00000f, .00000f);
        map[12] = new Color(.09410f, .00000f, .00000f);
        map[13] = new Color(.10200f, .00000f, .00000f);
        map[14] = new Color(.10980f, .00000f, .00000f);
        map[15] = new Color(.11760f, .00000f, .00000f);
        map[16] = new Color(.12550f, .00000f, .00000f);
        map[17] = new Color(.13330f, .00000f, .00000f);
        map[18] = new Color(.14120f, .00000f, .00000f);
        map[19] = new Color(.14900f, .00000f, .00000f);
        map[20] = new Color(.15690f, .00000f, .00000f);
        map[21] = new Color(.16470f, .00000f, .00000f);
        map[22] = new Color(.17250f, .00000f, .00000f);
        map[23] = new Color(.18040f, .00000f, .00000f);
        map[24] = new Color(.18820f, .00000f, .00000f);
        map[25] = new Color(.19610f, .00000f, .00000f);
        map[26] = new Color(.20390f, .00000f, .00000f);
        map[27] = new Color(.21180f, .00000f, .00000f);
        map[28] = new Color(.21960f, .00000f, .00000f);
        map[29] = new Color(.22750f, .00000f, .00000f);
        map[30] = new Color(.23530f, .00000f, .00000f);
        map[31] = new Color(.24310f, .00000f, .00000f);
        map[32] = new Color(.25100f, .00000f, .00000f);
        map[33] = new Color(.25880f, .00000f, .00000f);
        map[34] = new Color(.26670f, .00000f, .00000f);
        map[35] = new Color(.27450f, .00000f, .00000f);
        map[36] = new Color(.28240f, .00000f, .00000f);
        map[37] = new Color(.29020f, .00000f, .00000f);
        map[38] = new Color(.29800f, .00000f, .00000f);
        map[39] = new Color(.30590f, .00000f, .00000f);
        map[40] = new Color(.31370f, .00000f, .00000f);
        map[41] = new Color(.32160f, .00000f, .00000f);
        map[42] = new Color(.32940f, .00000f, .00000f);
        map[43] = new Color(.33730f, .00000f, .00000f);
        map[44] = new Color(.34510f, .00000f, .00000f);
        map[45] = new Color(.35290f, .00000f, .00000f);
        map[46] = new Color(.36080f, .00000f, .00000f);
        map[47] = new Color(.36860f, .00000f, .00000f);
        map[48] = new Color(.37650f, .00000f, .00000f);
        map[49] = new Color(.38430f, .00000f, .00000f);
        map[50] = new Color(.39220f, .00000f, .00000f);
        map[51] = new Color(.40000f, .00000f, .00000f);
        map[52] = new Color(.40780f, .00000f, .00000f);
        map[53] = new Color(.41570f, .00000f, .00000f);
        map[54] = new Color(.42350f, .00000f, .00000f);
        map[55] = new Color(.43140f, .00000f, .00000f);
        map[56] = new Color(.43920f, .00000f, .00000f);
        map[57] = new Color(.44710f, .00000f, .00000f);
        map[58] = new Color(.45490f, .00000f, .00000f);
        map[59] = new Color(.46270f, .00000f, .00000f);
        map[60] = new Color(.47060f, .00000f, .00000f);
        map[61] = new Color(.47840f, .00000f, .00000f);
        map[62] = new Color(.48630f, .00000f, .00000f);
        map[63] = new Color(.49410f, .00000f, .00000f);
        map[64] = new Color(.50200f, .00000f, .00000f);
        map[65] = new Color(.50980f, .00000f, .00000f);
        map[66] = new Color(.51760f, .00000f, .00000f);
        map[67] = new Color(.52550f, .00000f, .00000f);
        map[68] = new Color(.53330f, .00000f, .00000f);
        map[69] = new Color(.54120f, .00000f, .00000f);
        map[70] = new Color(.54900f, .00000f, .00000f);
        map[71] = new Color(.55690f, .00000f, .00000f);
        map[72] = new Color(.56470f, .00000f, .00000f);
        map[73] = new Color(.57250f, .00000f, .00000f);
        map[74] = new Color(.58040f, .00000f, .00000f);
        map[75] = new Color(.58820f, .00000f, .00000f);
        map[76] = new Color(.59610f, .00000f, .00000f);
        map[77] = new Color(.60390f, .00000f, .00000f);
        map[78] = new Color(.61180f, .00000f, .00000f);
        map[79] = new Color(.61960f, .00000f, .00000f);
        map[80] = new Color(.62750f, .00000f, .00000f);
        map[81] = new Color(.63530f, .00000f, .00000f);
        map[82] = new Color(.64310f, .00000f, .00000f);
        map[83] = new Color(.65100f, .00000f, .00000f);
        map[84] = new Color(.65880f, .00000f, .00000f);
        map[85] = new Color(.66670f, .00000f, .00000f);
        map[86] = new Color(.67450f, .00000f, .00000f);
        map[87] = new Color(.68240f, .00000f, .00000f);
        map[88] = new Color(.69020f, .00000f, .00000f);
        map[89] = new Color(.69800f, .00000f, .00000f);
        map[90] = new Color(.70590f, .00000f, .00000f);
        map[91] = new Color(.71370f, .00000f, .00000f);
        map[92] = new Color(.72160f, .00000f, .00000f);
        map[93] = new Color(.72940f, .00000f, .00000f);
        map[94] = new Color(.73730f, .00000f, .00000f);
        map[95] = new Color(.74510f, .00000f, .00000f);
        map[96] = new Color(.75290f, .00000f, .00000f);
        map[97] = new Color(.76080f, .00000f, .00000f);
        map[98] = new Color(.76860f, .00000f, .00000f);
        map[99] = new Color(.77650f, .00000f, .00000f);
        map[100] = new Color(.78430f, .00000f, .00000f);
        map[101] = new Color(.79220f, .00000f, .00000f);
        map[102] = new Color(.80000f, .00000f, .00000f);
        map[103] = new Color(.80780f, .00000f, .00000f);
        map[104] = new Color(.81570f, .00000f, .00000f);
        map[105] = new Color(.82350f, .00000f, .00000f);
        map[106] = new Color(.83140f, .00000f, .00000f);
        map[107] = new Color(.83920f, .00000f, .00000f);
        map[108] = new Color(.84710f, .00000f, .00000f);
        map[109] = new Color(.85490f, .00000f, .00000f);
        map[110] = new Color(.86270f, .00000f, .00000f);
        map[111] = new Color(.87060f, .00000f, .00000f);
        map[112] = new Color(.87840f, .00000f, .00000f);
        map[113] = new Color(.88630f, .00000f, .00000f);
        map[114] = new Color(.89410f, .00000f, .00000f);
        map[115] = new Color(.90200f, .00000f, .00000f);
        map[116] = new Color(.90980f, .00000f, .00000f);
        map[117] = new Color(.91760f, .00000f, .00000f);
        map[118] = new Color(.92550f, .00000f, .00000f);
        map[119] = new Color(.93330f, .00000f, .00000f);
        map[120] = new Color(.94120f, .00000f, .00000f);
        map[121] = new Color(.94900f, .00000f, .00000f);
        map[122] = new Color(.95690f, .00000f, .00000f);
        map[123] = new Color(.96470f, .00000f, .00000f);
        map[124] = new Color(.97250f, .00000f, .00000f);
        map[125] = new Color(.98040f, .00000f, .00000f);
        map[126] = new Color(.98820f, .00000f, .00000f);
        map[127] = new Color(.99610f, .00000f, .00390f);
    }

    public int filterRGB(int x, int y, int rgb){
        return map[rgb & 0x7f].getRGB();
    }

}
