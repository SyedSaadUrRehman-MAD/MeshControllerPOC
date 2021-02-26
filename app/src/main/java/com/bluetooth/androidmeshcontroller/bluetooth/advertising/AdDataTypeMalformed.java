package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeMalformed extends AdDataType {

    public AdDataTypeMalformed(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);
    }

    public String toString() {
        String s = super.toString();
        s = s + "!! 0x" + Utility.bytesToHex(value_bytes);
        s = s + "\n";
        return s;
    }
}
