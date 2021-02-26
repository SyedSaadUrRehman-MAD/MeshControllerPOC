package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;

/**
 * Created by mwoolley on 29/07/2015.
 *
 * Signed byte
 */
public class AdDataTypeSbyte extends AdDataType {

    private byte sbyte;

    public AdDataTypeSbyte(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);

        sbyte = value_bytes[0];
    }

    public String toString() {
        String s = super.toString();
        s = s + " " + sbyte;
        s = s + "\n";
       return s;
    }
}
