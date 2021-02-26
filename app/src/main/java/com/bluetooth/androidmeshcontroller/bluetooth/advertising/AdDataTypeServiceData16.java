package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeServiceData16 extends AdDataType {

    private byte [] uuid;
    private byte [] service_data;

    public AdDataTypeServiceData16(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);
        uuid = new byte[2];
        uuid[0] = value_bytes[0];
        uuid[1] = value_bytes[1];
        service_data = new byte[value_bytes.length - 2];
        int j=0;
        for (int i=2;i<value_bytes.length;i++) {
            service_data[j] = value_bytes[i];
            j++;
        }
    }

    public String toString() {
        String s = super.toString();
        byte [] uuid_le = { uuid[1] , uuid[0] };
        s = s + " UUID:0x" + Utility.bytesToHex(uuid_le);
        s = s + " Service Data:0x" + Utility.bytesToHex(service_data);
        s = s + "\n";
       return s;
    }
}
