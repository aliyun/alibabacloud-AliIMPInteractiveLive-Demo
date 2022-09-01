package com.aliyun.roompaas.base.util;

public class HexUtil {

    public static byte[] hex2ByteArray(String inHex) {
        int hexlen = inHex.length();
        byte[] result;
        if (hexlen % 2 == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hex2Byte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static byte hex2Byte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }
}
