package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import android.text.Html;

import com.bluetooth.androidmeshcontroller.Utility;

import java.io.UnsupportedEncodingException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataType {

    byte type_id;
    String type_name = "";
    byte[] value_bytes;

    public AdDataType(byte type_id, String type_name, byte[] value_bytes) {
        this.type_id = type_id;
        this.type_name = type_name;
        this.value_bytes = value_bytes;
    }

    public byte[] getValue_bytes() {
        return value_bytes;
    }

    public void setValue_bytes(byte[] value_bytes) {
        this.value_bytes = value_bytes;
    }

    public byte getType_id() {
        return type_id;
    }

    public void setType_id(byte type_id) {
        this.type_id = type_id;
    }

    public String getValueAsString() {
        return "";
    }

    public String getType_name() {
        return type_name;
    }

    public void setType_name(String type_name) {
        this.type_name = type_name;
    }

    public String toString() {
        String s = type_name;
        return s;
    }
}
