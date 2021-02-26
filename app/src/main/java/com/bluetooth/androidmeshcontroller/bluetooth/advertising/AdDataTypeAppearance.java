package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeAppearance extends AdDataType {

    private short appearance;

    public AdDataTypeAppearance(byte type_id, String type_name, byte[] value_bytes) throws MalformedDataException {
        super(type_id, type_name, value_bytes);
        if (value_bytes.length < 2) {
            throw new MalformedDataException();
        }
        appearance = (short) (value_bytes[0] + (value_bytes[1] << 8));
    }

    public String toString() {
        Appearances appearances = Appearances.getInstance();
        String s = super.toString();
        s = s + " " + appearance + ":" + appearances.getValue(appearance);
        s = s + "\n";
        return s;
    }
}
