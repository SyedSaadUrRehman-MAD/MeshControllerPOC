package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataType32BitUuids extends AdDataType {

    private int [] uuids;

    public AdDataType32BitUuids(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);

        uuids = new int[value_bytes.length / 4];
        for (int i=0;i<uuids.length;i++) {
            int uuid = (int) ((value_bytes[i*2]) + (value_bytes[(i*2)+1] << 8) + (value_bytes[(i*2)+2] << 16) + value_bytes[(i*2) + 3]<<24);
        }
    }


    public String toString() {
        String s = super.toString();
        for (int i=0;i<value_bytes.length;i=i+4) {
            byte [] uuid = new byte[4];
            uuid[3] = value_bytes[i];
            uuid[2] = value_bytes[i+1];
            uuid[1] = value_bytes[i+2];
            uuid[0] = value_bytes[i+3];
            s = s + " 0x" + Utility.bytesToHex(uuid);
        }
        s = s + "\n";
       return s;
    }
}
