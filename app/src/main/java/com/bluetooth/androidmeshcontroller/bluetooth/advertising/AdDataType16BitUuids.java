package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataType16BitUuids extends AdDataType {

    private short [] uuids;

    public AdDataType16BitUuids(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);

        uuids = new short[value_bytes.length / 2];
        for (int i=0;i<uuids.length;i++) {
            short uuid = (short) ((value_bytes[i*2]) + value_bytes[(i*2) + 1] << 8);
        }
    }


    public String toString() {
        String s = super.toString();
        for (int i=0;i<value_bytes.length;i=i+2) {
            byte [] uuid = new byte[2];
            uuid[1] = value_bytes[i];
            uuid[0] = value_bytes[i+1];
            s = s + " 0x" + Utility.bytesToHex(uuid);
        }
        s = s + "\n";
       return s;
    }
}
