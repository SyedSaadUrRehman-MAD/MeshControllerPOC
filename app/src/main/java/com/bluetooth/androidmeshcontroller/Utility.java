package com.bluetooth.androidmeshcontroller;

import android.util.Log;

import java.io.ByteArrayOutputStream;

public class Utility {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte [] hexToBytes(String str) {
        System.out.println("XXXX hexToBytes: "+str);
        String hex = str;
        // pad with leading zero if odd length
        if (hex.length() % 2 == 1) {
            hex = "0" + hex;
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while (hex.length() >= 2) {
            bos.write((byte) Short.parseShort(hex.substring(0, 2), 16));
            hex = hex.substring(2, hex.length());
        }

        byte [] result = bos.toByteArray();
        if (result.length == 0) {
            Log.e (Constants.TAG, "ERROR: hexToBytes: result is zero length array");
        }
        return result;
    }

    public static String newlineToHtmlBr(String s) {
        String html = "";
        char[] chars = s.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] != '\n') {
                html = html + chars[i];
            } else {
                if (i != (chars.length - 1)) {
                    html = html + "<br>";
                }
            }
        }
        return html;
    }

    public static String colonSeperatedHexString(byte[] bytes) {
        String s1 = bytesToHex(bytes);
        char[] chars = s1.toCharArray();
        String s2 = "";
        for (int i = 0; i < chars.length; i = i + 2) {
            s2 = s2 + chars[i];
            s2 = s2 + chars[i + 1];
            s2 = s2 + ":";
        }
        s2 = s2.substring(0, s2.length() - 2);
        return s2;
    }

    public static String htmlColorGreen(String s) {
        s = "<font color=\"#33CC33\">" + s + "</font>: ";
        return s;
    }

    public static String htmlColorYellow(String s) {
        s = "<font color=\"#FFFF00\">" + s + "</font>: ";
        return s;
    }

    public static String htmlColorRed(String s) {
        s = "<font color=\"#FF0000\">" + s + "</font>: ";
        return s;
    }

    public static byte[] makeByteArrayFromInt(int i, int numBytes) {
        byte[] result = new byte[numBytes];
        int shiftBits = (numBytes - 1) * 8;

        for (int j = 0; j < numBytes; j++) {
            result[j] = (byte) (i >>> shiftBits);
            shiftBits -= 8;
        }
        return result;
    }

    public static byte [] xorU8Arrays(byte [] arrayA , byte [] arrayB) {
        byte [] result = new byte[arrayA.length];
        for (int i=0; i<arrayA.length;i++) {
            result[i] = (byte) (arrayA[i] ^ arrayB[i]);
        }
        return result;
    }

    public static byte leastSignificantBit(byte number){
        return (byte) (number & 1);
    }

    public static byte [] intSeqToByteArraySeq(int int_seq){
        byte b1 = (byte) (int_seq  & 0x0000FF);
        byte b2 = (byte) ((int_seq & 0x00FF00) >> 8);
        byte b3 = (byte) ((int_seq & 0xFF0000) >> 16);
        return new byte[]{b3 , b2, b1};
    }

    public static byte [] uint16ToUint8ArrayLE(int sixteen_bits) {
        byte lsb = (byte) (sixteen_bits & 0xFF);
        byte msb = (byte) (sixteen_bits >> 8);
        return new byte []{lsb , msb};
    }

    public static byte [] uint16ToUint8ArrayBE(int sixteen_bits) {
        byte lsb = (byte) (sixteen_bits & 0xFF);
        byte msb = (byte) (sixteen_bits >> 8);
        return new byte[]{msb , lsb};
    }

}
