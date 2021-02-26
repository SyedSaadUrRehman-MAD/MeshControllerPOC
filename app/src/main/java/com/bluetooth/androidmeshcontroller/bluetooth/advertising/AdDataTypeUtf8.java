package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import java.io.UnsupportedEncodingException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeUtf8 extends AdDataType {

    public AdDataTypeUtf8(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);
    }

    public String toString() {
        String s = super.toString();
        try {
            s = s + new String(value_bytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        s = s + "\n";
        return s;
    }
}
