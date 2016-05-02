/* Generated with zvtm-fits/src/main/resources/scripts/lut.py */

package fr.inria.zvtm.fits.filters;

import java.awt.Color;
import java.awt.image.RGBImageFilter;

import java.awt.LinearGradientPaint;

public class HazeFilter extends RGBImageFilter implements ColorGradient {

    private static final Color[] map = new Color[128];

    static {
        map[0] = Color.decode("#FFE6FF");
        map[1] = Color.decode("#FAE2FF");
        map[2] = Color.decode("#F6DEFF");
        map[3] = Color.decode("#F2DBFF");
        map[4] = Color.decode("#EED7FF");
        map[5] = Color.decode("#EAD3FF");
        map[6] = Color.decode("#E6D0FF");
        map[7] = Color.decode("#E2CCFF");
        map[8] = Color.decode("#DEC8FF");
        map[9] = Color.decode("#DAC5FF");
        map[10] = Color.decode("#D6C1FF");
        map[11] = Color.decode("#D2BDFF");
        map[12] = Color.decode("#CEBAFF");
        map[13] = Color.decode("#CAB6FF");
        map[14] = Color.decode("#C6B2FF");
        map[15] = Color.decode("#C2AFFF");
        map[16] = Color.decode("#BEABFF");
        map[17] = Color.decode("#BAA7FF");
        map[18] = Color.decode("#B6A4FF");
        map[19] = Color.decode("#B2A0FF");
        map[20] = Color.decode("#AE9CFF");
        map[21] = Color.decode("#AA99FF");
        map[22] = Color.decode("#A595FF");
        map[23] = Color.decode("#A192FF");
        map[24] = Color.decode("#9D8EFF");
        map[25] = Color.decode("#998AFF");
        map[26] = Color.decode("#9587FF");
        map[27] = Color.decode("#9183FF");
        map[28] = Color.decode("#8D7FFF");
        map[29] = Color.decode("#897CFF");
        map[30] = Color.decode("#8578FF");
        map[31] = Color.decode("#8174FF");
        map[32] = Color.decode("#7D71FF");
        map[33] = Color.decode("#796DFF");
        map[34] = Color.decode("#7569FF");
        map[35] = Color.decode("#7166FF");
        map[36] = Color.decode("#6D62FF");
        map[37] = Color.decode("#695EFF");
        map[38] = Color.decode("#655BFF");
        map[39] = Color.decode("#6157FF");
        map[40] = Color.decode("#5D53FF");
        map[41] = Color.decode("#5950FF");
        map[42] = Color.decode("#554CFF");
        map[43] = Color.decode("#5049FF");
        map[44] = Color.decode("#4C45FF");
        map[45] = Color.decode("#4841FF");
        map[46] = Color.decode("#443EFF");
        map[47] = Color.decode("#403AFF");
        map[48] = Color.decode("#3C36FF");
        map[49] = Color.decode("#3833FF");
        map[50] = Color.decode("#342FFF");
        map[51] = Color.decode("#302BFF");
        map[52] = Color.decode("#2C28FF");
        map[53] = Color.decode("#2824FF");
        map[54] = Color.decode("#2420FF");
        map[55] = Color.decode("#201DFF");
        map[56] = Color.decode("#1C19FF");
        map[57] = Color.decode("#1815FF");
        map[58] = Color.decode("#1412FF");
        map[59] = Color.decode("#100EFF");
        map[60] = Color.decode("#0C0AFF");
        map[61] = Color.decode("#0807FF");
        map[62] = Color.decode("#0403FF");
        map[63] = Color.decode("#0000FF");
        map[64] = Color.decode("#0000FF");
        map[65] = Color.decode("#0404FA");
        map[66] = Color.decode("#0808F6");
        map[67] = Color.decode("#0C0CF2");
        map[68] = Color.decode("#1010EE");
        map[69] = Color.decode("#1414EA");
        map[70] = Color.decode("#1818E6");
        map[71] = Color.decode("#1C1CE2");
        map[72] = Color.decode("#2020DE");
        map[73] = Color.decode("#2424DA");
        map[74] = Color.decode("#2828D6");
        map[75] = Color.decode("#2C2CD2");
        map[76] = Color.decode("#3030CE");
        map[77] = Color.decode("#3434CA");
        map[78] = Color.decode("#3838C6");
        map[79] = Color.decode("#3C3CC2");
        map[80] = Color.decode("#4040BE");
        map[81] = Color.decode("#4444BA");
        map[82] = Color.decode("#4848B6");
        map[83] = Color.decode("#4C4CB2");
        map[84] = Color.decode("#5050AE");
        map[85] = Color.decode("#5555AA");
        map[86] = Color.decode("#5959A5");
        map[87] = Color.decode("#5D5DA1");
        map[88] = Color.decode("#61619D");
        map[89] = Color.decode("#656599");
        map[90] = Color.decode("#696995");
        map[91] = Color.decode("#6D6D91");
        map[92] = Color.decode("#71718D");
        map[93] = Color.decode("#757589");
        map[94] = Color.decode("#797985");
        map[95] = Color.decode("#7D7D81");
        map[96] = Color.decode("#81817D");
        map[97] = Color.decode("#858579");
        map[98] = Color.decode("#898975");
        map[99] = Color.decode("#8D8D71");
        map[100] = Color.decode("#91916D");
        map[101] = Color.decode("#959569");
        map[102] = Color.decode("#999965");
        map[103] = Color.decode("#9D9D61");
        map[104] = Color.decode("#A1A15D");
        map[105] = Color.decode("#A5A559");
        map[106] = Color.decode("#AAAA55");
        map[107] = Color.decode("#AEAE50");
        map[108] = Color.decode("#B2B24C");
        map[109] = Color.decode("#B6B648");
        map[110] = Color.decode("#BABA44");
        map[111] = Color.decode("#BEBE40");
        map[112] = Color.decode("#C2C23C");
        map[113] = Color.decode("#C6C638");
        map[114] = Color.decode("#CACA34");
        map[115] = Color.decode("#CECE30");
        map[116] = Color.decode("#D2D22C");
        map[117] = Color.decode("#D6D628");
        map[118] = Color.decode("#DADA24");
        map[119] = Color.decode("#DEDE20");
        map[120] = Color.decode("#E2E21C");
        map[121] = Color.decode("#E6E618");
        map[122] = Color.decode("#EAEA14");
        map[123] = Color.decode("#EEEE10");
        map[124] = Color.decode("#F2F20C");
        map[125] = Color.decode("#F6F608");
        map[126] = Color.decode("#FAFA04");
        map[127] = Color.decode("#FFFF00");
    }

    public HazeFilter(){}

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
