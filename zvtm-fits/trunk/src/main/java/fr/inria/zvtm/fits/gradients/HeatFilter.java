package fr.inria.zvtm.fits.gradients;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

//data set taken from jsky (http://jsky.sf.net)
public class HeatFilter extends RGBImageFilter {
    private Color[] map = new Color[256];

    public HeatFilter(){
        map[0] = new Color(0.00000f, 0.00000f, 0.00000f);
        map[1] = new Color(0.01176f, 0.00392f, 0.00000f);
        map[2] = new Color(0.02353f, 0.00784f, 0.00000f);
        map[3] = new Color(0.03529f, 0.01176f, 0.00000f);
        map[4] = new Color(0.04706f, 0.01569f, 0.00000f);
        map[5] = new Color(0.05882f, 0.01961f, 0.00000f);
        map[6] = new Color(0.07059f, 0.02353f, 0.00000f);
        map[7] = new Color(0.08235f, 0.02745f, 0.00000f);
        map[8] = new Color(0.09412f, 0.03137f, 0.00000f);
        map[9] = new Color(0.10588f, 0.03529f, 0.00000f);
        map[10] = new Color(0.11765f, 0.03922f, 0.00000f);
        map[11] = new Color(0.12941f, 0.04314f, 0.00000f);
        map[12] = new Color(0.14118f, 0.04706f, 0.00000f);
        map[13] = new Color(0.15294f, 0.05098f, 0.00000f);
        map[14] = new Color(0.16471f, 0.05490f, 0.00000f);
        map[15] = new Color(0.17647f, 0.05882f, 0.00000f);
        map[16] = new Color(0.18824f, 0.06275f, 0.00000f);
        map[17] = new Color(0.20000f, 0.06667f, 0.00000f);
        map[18] = new Color(0.21176f, 0.07059f, 0.00000f);
        map[19] = new Color(0.22353f, 0.07451f, 0.00000f);
        map[20] = new Color(0.23529f, 0.07843f, 0.00000f);
        map[21] = new Color(0.24706f, 0.08235f, 0.00000f);
        map[22] = new Color(0.25882f, 0.08627f, 0.00000f);
        map[23] = new Color(0.27059f, 0.09020f, 0.00000f);
        map[24] = new Color(0.28235f, 0.09412f, 0.00000f);
        map[25] = new Color(0.29412f, 0.09804f, 0.00000f);
        map[26] = new Color(0.30588f, 0.10196f, 0.00000f);
        map[27] = new Color(0.31765f, 0.10588f, 0.00000f);
        map[28] = new Color(0.32941f, 0.10980f, 0.00000f);
        map[29] = new Color(0.34118f, 0.11373f, 0.00000f);
        map[30] = new Color(0.35294f, 0.11765f, 0.00000f);
        map[31] = new Color(0.36471f, 0.12157f, 0.00000f);
        map[32] = new Color(0.37647f, 0.12549f, 0.00000f);
        map[33] = new Color(0.38824f, 0.12941f, 0.00000f);
        map[34] = new Color(0.40000f, 0.13333f, 0.00000f);
        map[35] = new Color(0.41176f, 0.13725f, 0.00000f);
        map[36] = new Color(0.42353f, 0.14118f, 0.00000f);
        map[37] = new Color(0.43529f, 0.14510f, 0.00000f);
        map[38] = new Color(0.44706f, 0.14902f, 0.00000f);
        map[39] = new Color(0.45882f, 0.15294f, 0.00000f);
        map[40] = new Color(0.47059f, 0.15686f, 0.00000f);
        map[41] = new Color(0.48235f, 0.16078f, 0.00000f);
        map[42] = new Color(0.49412f, 0.16471f, 0.00000f);
        map[43] = new Color(0.50588f, 0.16863f, 0.00000f);
        map[44] = new Color(0.51765f, 0.17255f, 0.00000f);
        map[45] = new Color(0.52941f, 0.17647f, 0.00000f);
        map[46] = new Color(0.54118f, 0.18039f, 0.00000f);
        map[47] = new Color(0.55294f, 0.18431f, 0.00000f);
        map[48] = new Color(0.56471f, 0.18824f, 0.00000f);
        map[49] = new Color(0.57647f, 0.19216f, 0.00000f);
        map[50] = new Color(0.58824f, 0.19608f, 0.00000f);
        map[51] = new Color(0.60000f, 0.20000f, 0.00000f);
        map[52] = new Color(0.61176f, 0.20392f, 0.00000f);
        map[53] = new Color(0.62353f, 0.20784f, 0.00000f);
        map[54] = new Color(0.63529f, 0.21176f, 0.00000f);
        map[55] = new Color(0.64706f, 0.21569f, 0.00000f);
        map[56] = new Color(0.65882f, 0.21961f, 0.00000f);
        map[57] = new Color(0.67059f, 0.22353f, 0.00000f);
        map[58] = new Color(0.68235f, 0.22745f, 0.00000f);
        map[59] = new Color(0.69412f, 0.23137f, 0.00000f);
        map[60] = new Color(0.70588f, 0.23529f, 0.00000f);
        map[61] = new Color(0.71765f, 0.23922f, 0.00000f);
        map[62] = new Color(0.72941f, 0.24314f, 0.00000f);
        map[63] = new Color(0.74118f, 0.24706f, 0.00000f);
        map[64] = new Color(0.75294f, 0.25098f, 0.00000f);
        map[65] = new Color(0.76471f, 0.25490f, 0.00000f);
        map[66] = new Color(0.77647f, 0.25882f, 0.00000f);
        map[67] = new Color(0.78824f, 0.26275f, 0.00000f);
        map[68] = new Color(0.80000f, 0.26667f, 0.00000f);
        map[69] = new Color(0.81176f, 0.27059f, 0.00000f);
        map[70] = new Color(0.82353f, 0.27451f, 0.00000f);
        map[71] = new Color(0.83529f, 0.27843f, 0.00000f);
        map[72] = new Color(0.84706f, 0.28235f, 0.00000f);
        map[73] = new Color(0.85882f, 0.28627f, 0.00000f);
        map[74] = new Color(0.87059f, 0.29020f, 0.00000f);
        map[75] = new Color(0.88235f, 0.29412f, 0.00000f);
        map[76] = new Color(0.89412f, 0.29804f, 0.00000f);
        map[77] = new Color(0.90588f, 0.30196f, 0.00000f);
        map[78] = new Color(0.91765f, 0.30588f, 0.00000f);
        map[79] = new Color(0.92941f, 0.30980f, 0.00000f);
        map[80] = new Color(0.94118f, 0.31373f, 0.00000f);
        map[81] = new Color(0.95294f, 0.31765f, 0.00000f);
        map[82] = new Color(0.96471f, 0.32157f, 0.00000f);
        map[83] = new Color(0.97647f, 0.32549f, 0.00000f);
        map[84] = new Color(0.98824f, 0.32941f, 0.00000f);
        map[85] = new Color(1.00000f, 0.33333f, 0.00000f);
        map[86] = new Color(1.00000f, 0.33725f, 0.00000f);
        map[87] = new Color(1.00000f, 0.34118f, 0.00000f);
        map[88] = new Color(1.00000f, 0.34510f, 0.00000f);
        map[89] = new Color(1.00000f, 0.34902f, 0.00000f);
        map[90] = new Color(1.00000f, 0.35294f, 0.00000f);
        map[91] = new Color(1.00000f, 0.35686f, 0.00000f);
        map[92] = new Color(1.00000f, 0.36078f, 0.00000f);
        map[93] = new Color(1.00000f, 0.36471f, 0.00000f);
        map[94] = new Color(1.00000f, 0.36863f, 0.00000f);
        map[95] = new Color(1.00000f, 0.37255f, 0.00000f);
        map[96] = new Color(1.00000f, 0.37647f, 0.00000f);
        map[97] = new Color(1.00000f, 0.38039f, 0.00000f);
        map[98] = new Color(1.00000f, 0.38431f, 0.00000f);
        map[99] = new Color(1.00000f, 0.38824f, 0.00000f);
        map[100] = new Color(1.00000f, 0.39216f, 0.00000f);
        map[101] = new Color(1.00000f, 0.39608f, 0.00000f);
        map[102] = new Color(1.00000f, 0.40000f, 0.00000f);
        map[103] = new Color(1.00000f, 0.40392f, 0.00000f);
        map[104] = new Color(1.00000f, 0.40784f, 0.00000f);
        map[105] = new Color(1.00000f, 0.41176f, 0.00000f);
        map[106] = new Color(1.00000f, 0.41569f, 0.00000f);
        map[107] = new Color(1.00000f, 0.41961f, 0.00000f);
        map[108] = new Color(1.00000f, 0.42353f, 0.00000f);
        map[109] = new Color(1.00000f, 0.42745f, 0.00000f);
        map[110] = new Color(1.00000f, 0.43137f, 0.00000f);
        map[111] = new Color(1.00000f, 0.43529f, 0.00000f);
        map[112] = new Color(1.00000f, 0.43922f, 0.00000f);
        map[113] = new Color(1.00000f, 0.44314f, 0.00000f);
        map[114] = new Color(1.00000f, 0.44706f, 0.00000f);
        map[115] = new Color(1.00000f, 0.45098f, 0.00000f);
        map[116] = new Color(1.00000f, 0.45490f, 0.00000f);
        map[117] = new Color(1.00000f, 0.45882f, 0.00000f);
        map[118] = new Color(1.00000f, 0.46275f, 0.00000f);
        map[119] = new Color(1.00000f, 0.46667f, 0.00000f);
        map[120] = new Color(1.00000f, 0.47059f, 0.00000f);
        map[121] = new Color(1.00000f, 0.47451f, 0.00000f);
        map[122] = new Color(1.00000f, 0.47843f, 0.00000f);
        map[123] = new Color(1.00000f, 0.48235f, 0.00000f);
        map[124] = new Color(1.00000f, 0.48627f, 0.00000f);
        map[125] = new Color(1.00000f, 0.49020f, 0.00000f);
        map[126] = new Color(1.00000f, 0.49412f, 0.00000f);
        map[127] = new Color(1.00000f, 0.49804f, 0.00000f);
        map[128] = new Color(1.00000f, 0.50196f, 0.00000f);
        map[129] = new Color(1.00000f, 0.50588f, 0.00000f);
        map[130] = new Color(1.00000f, 0.50980f, 0.00000f);
        map[131] = new Color(1.00000f, 0.51373f, 0.00000f);
        map[132] = new Color(1.00000f, 0.51765f, 0.00000f);
        map[133] = new Color(1.00000f, 0.52157f, 0.00000f);
        map[134] = new Color(1.00000f, 0.52549f, 0.00000f);
        map[135] = new Color(1.00000f, 0.52941f, 0.00000f);
        map[136] = new Color(1.00000f, 0.53333f, 0.00000f);
        map[137] = new Color(1.00000f, 0.53725f, 0.00000f);
        map[138] = new Color(1.00000f, 0.54118f, 0.00000f);
        map[139] = new Color(1.00000f, 0.54510f, 0.00000f);
        map[140] = new Color(1.00000f, 0.54902f, 0.00000f);
        map[141] = new Color(1.00000f, 0.55294f, 0.00000f);
        map[142] = new Color(1.00000f, 0.55686f, 0.00000f);
        map[143] = new Color(1.00000f, 0.56078f, 0.00000f);
        map[144] = new Color(1.00000f, 0.56471f, 0.00000f);
        map[145] = new Color(1.00000f, 0.56863f, 0.00000f);
        map[146] = new Color(1.00000f, 0.57255f, 0.00000f);
        map[147] = new Color(1.00000f, 0.57647f, 0.00000f);
        map[148] = new Color(1.00000f, 0.58039f, 0.00000f);
        map[149] = new Color(1.00000f, 0.58431f, 0.00000f);
        map[150] = new Color(1.00000f, 0.58824f, 0.00000f);
        map[151] = new Color(1.00000f, 0.59216f, 0.00000f);
        map[152] = new Color(1.00000f, 0.59608f, 0.00000f);
        map[153] = new Color(1.00000f, 0.60000f, 0.00000f);
        map[154] = new Color(1.00000f, 0.60392f, 0.00000f);
        map[155] = new Color(1.00000f, 0.60784f, 0.00000f);
        map[156] = new Color(1.00000f, 0.61176f, 0.00000f);
        map[157] = new Color(1.00000f, 0.61569f, 0.00000f);
        map[158] = new Color(1.00000f, 0.61961f, 0.00000f);
        map[159] = new Color(1.00000f, 0.62353f, 0.00000f);
        map[160] = new Color(1.00000f, 0.62745f, 0.00000f);
        map[161] = new Color(1.00000f, 0.63137f, 0.00000f);
        map[162] = new Color(1.00000f, 0.63529f, 0.00000f);
        map[163] = new Color(1.00000f, 0.63922f, 0.00000f);
        map[164] = new Color(1.00000f, 0.64314f, 0.00000f);
        map[165] = new Color(1.00000f, 0.64706f, 0.00000f);
        map[166] = new Color(1.00000f, 0.65098f, 0.01176f);
        map[167] = new Color(1.00000f, 0.65490f, 0.02353f);
        map[168] = new Color(1.00000f, 0.65882f, 0.03529f);
        map[169] = new Color(1.00000f, 0.66275f, 0.04706f);
        map[170] = new Color(1.00000f, 0.66667f, 0.05882f);
        map[171] = new Color(1.00000f, 0.67059f, 0.07059f);
        map[172] = new Color(1.00000f, 0.67451f, 0.08235f);
        map[173] = new Color(1.00000f, 0.67843f, 0.09412f);
        map[174] = new Color(1.00000f, 0.68235f, 0.10588f);
        map[175] = new Color(1.00000f, 0.68627f, 0.11765f);
        map[176] = new Color(1.00000f, 0.69020f, 0.12941f);
        map[177] = new Color(1.00000f, 0.69412f, 0.14118f);
        map[178] = new Color(1.00000f, 0.69804f, 0.15294f);
        map[179] = new Color(1.00000f, 0.70196f, 0.16471f);
        map[180] = new Color(1.00000f, 0.70588f, 0.17647f);
        map[181] = new Color(1.00000f, 0.70980f, 0.18824f);
        map[182] = new Color(1.00000f, 0.71373f, 0.20000f);
        map[183] = new Color(1.00000f, 0.71765f, 0.21176f);
        map[184] = new Color(1.00000f, 0.72157f, 0.22353f);
        map[185] = new Color(1.00000f, 0.72549f, 0.23529f);
        map[186] = new Color(1.00000f, 0.72941f, 0.24706f);
        map[187] = new Color(1.00000f, 0.73333f, 0.25882f);
        map[188] = new Color(1.00000f, 0.73725f, 0.27059f);
        map[189] = new Color(1.00000f, 0.74118f, 0.28235f);
        map[190] = new Color(1.00000f, 0.74510f, 0.29412f);
        map[191] = new Color(1.00000f, 0.74902f, 0.30588f);
        map[192] = new Color(1.00000f, 0.75294f, 0.31765f);
        map[193] = new Color(1.00000f, 0.75686f, 0.32941f);
        map[194] = new Color(1.00000f, 0.76078f, 0.34118f);
        map[195] = new Color(1.00000f, 0.76471f, 0.35294f);
        map[196] = new Color(1.00000f, 0.76863f, 0.36471f);
        map[197] = new Color(1.00000f, 0.77255f, 0.37647f);
        map[198] = new Color(1.00000f, 0.77647f, 0.38824f);
        map[199] = new Color(1.00000f, 0.78039f, 0.40000f);
        map[200] = new Color(1.00000f, 0.78431f, 0.41176f);
        map[201] = new Color(1.00000f, 0.78824f, 0.42353f);
        map[202] = new Color(1.00000f, 0.79216f, 0.43529f);
        map[203] = new Color(1.00000f, 0.79608f, 0.44706f);
        map[204] = new Color(1.00000f, 0.80000f, 0.45882f);
        map[205] = new Color(1.00000f, 0.80392f, 0.47059f);
        map[206] = new Color(1.00000f, 0.80784f, 0.48235f);
        map[207] = new Color(1.00000f, 0.81176f, 0.49412f);
        map[208] = new Color(1.00000f, 0.81569f, 0.50588f);
        map[209] = new Color(1.00000f, 0.81961f, 0.51765f);
        map[210] = new Color(1.00000f, 0.82353f, 0.52941f);
        map[211] = new Color(1.00000f, 0.82745f, 0.54118f);
        map[212] = new Color(1.00000f, 0.83137f, 0.55294f);
        map[213] = new Color(1.00000f, 0.83529f, 0.56471f);
        map[214] = new Color(1.00000f, 0.83922f, 0.57647f);
        map[215] = new Color(1.00000f, 0.84314f, 0.58824f);
        map[216] = new Color(1.00000f, 0.84706f, 0.60000f);
        map[217] = new Color(1.00000f, 0.85098f, 0.61176f);
        map[218] = new Color(1.00000f, 0.85490f, 0.62353f);
        map[219] = new Color(1.00000f, 0.85882f, 0.63529f);
        map[220] = new Color(1.00000f, 0.86275f, 0.64706f);
        map[221] = new Color(1.00000f, 0.86667f, 0.65882f);
        map[222] = new Color(1.00000f, 0.87059f, 0.67059f);
        map[223] = new Color(1.00000f, 0.87451f, 0.68235f);
        map[224] = new Color(1.00000f, 0.87843f, 0.69412f);
        map[225] = new Color(1.00000f, 0.88235f, 0.70588f);
        map[226] = new Color(1.00000f, 0.88627f, 0.71765f);
        map[227] = new Color(1.00000f, 0.89020f, 0.72941f);
        map[228] = new Color(1.00000f, 0.89412f, 0.74118f);
        map[229] = new Color(1.00000f, 0.89804f, 0.75294f);
        map[230] = new Color(1.00000f, 0.90196f, 0.76471f);
        map[231] = new Color(1.00000f, 0.90588f, 0.77647f);
        map[232] = new Color(1.00000f, 0.90980f, 0.78824f);
        map[233] = new Color(1.00000f, 0.91373f, 0.80000f);
        map[234] = new Color(1.00000f, 0.91765f, 0.81176f);
        map[235] = new Color(1.00000f, 0.92157f, 0.82353f);
        map[236] = new Color(1.00000f, 0.92549f, 0.83529f);
        map[237] = new Color(1.00000f, 0.92941f, 0.84706f);
        map[238] = new Color(1.00000f, 0.93333f, 0.85882f);
        map[239] = new Color(1.00000f, 0.93725f, 0.87059f);
        map[240] = new Color(1.00000f, 0.94118f, 0.88235f);
        map[241] = new Color(1.00000f, 0.94510f, 0.89412f);
        map[242] = new Color(1.00000f, 0.94902f, 0.90588f);
        map[243] = new Color(1.00000f, 0.95294f, 0.91765f);
        map[244] = new Color(1.00000f, 0.95686f, 0.92941f);
        map[245] = new Color(1.00000f, 0.96078f, 0.94118f);
        map[246] = new Color(1.00000f, 0.96471f, 0.95294f);
        map[247] = new Color(1.00000f, 0.96863f, 0.96471f);
        map[248] = new Color(1.00000f, 0.97255f, 0.97647f);
        map[249] = new Color(1.00000f, 0.97647f, 0.98824f);
        map[250] = new Color(1.00000f, 0.98039f, 1.00000f);
        map[251] = new Color(1.00000f, 0.98431f, 1.00000f);
        map[252] = new Color(1.00000f, 0.98824f, 1.00000f);
        map[253] = new Color(1.00000f, 0.99216f, 1.00000f);
        map[254] = new Color(1.00000f, 0.99608f, 1.00000f);
        map[255] = new Color(1.00000f, 1.00000f, 1.00000f);
    }

    public int filterRGB(int x, int y, int rgb) {
        return map[rgb & 0xff].getRGB();
    }

}

