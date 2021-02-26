package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeShort extends AdDataType {

    private short value;

    public AdDataTypeShort(byte type_id, String type_name, byte[] value_bytes) throws MalformedDataException {
        super(type_id, type_name, value_bytes);
        if (value_bytes.length < 2) {
            throw new MalformedDataException();
        }
        value = (short) (value_bytes[0] + (value_bytes[1] << 8));
    }

    public String toString() {
        String s = super.toString();
        s = s + " " + value;
        s = s + "\n";
        return s;
    }
}
