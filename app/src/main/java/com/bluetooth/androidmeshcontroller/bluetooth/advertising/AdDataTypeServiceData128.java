package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeServiceData128 extends AdDataType {

    private byte [] uuid;
    private byte [] service_data;

    public AdDataTypeServiceData128(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);

        uuid = new byte[16];
        service_data = new byte[value_bytes.length - 16];
        for (int i=16;i<value_bytes.length;i++) {
            service_data[i] = value_bytes[i+16];
        }
    }

    public String toString() {
        String s = super.toString();
        s = s + "UUID:0x" + Utility.bytesToHex(uuid);
        s = s + " Service Data:0x" + Utility.bytesToHex(service_data);
        s = s + "\n";
       return s;
    }
}
