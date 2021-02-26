package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import android.util.Log;

import com.bluetooth.androidmeshcontroller.Utility;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataType128BitUuids extends AdDataType {

    String TAG = "AdvScanner";

    private byte [][] uuids;

    public AdDataType128BitUuids(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);

        Log.d(TAG, "AdDataType128BitUuids: "+Utility.bytesToHex(value_bytes));
        Log.d(TAG, "AdDataType128BitUuids: array dimensions "+value_bytes.length / 16 + " , 16" );
        uuids = new byte[value_bytes.length / 16][16];
        int j=0;
        int k=15;
        for (int i=0;i<value_bytes.length;i++) {
            uuids[j][k] = value_bytes[i];
            k--;
            if (k == -1) {
                k=15;
                j++;
            }
        }
    }

    public String toString() {
        String s = super.toString();
        for (int j=0;j<uuids.length;j++) {
            byte [] uuid = new byte[16];
            for (int k=0;k<16;k++) {
                uuid[k] = uuids[j][k];
            }
            s = s + " 0x" + Utility.bytesToHex(uuid);
        }
        s = s + "\n";
       return s;
    }
}
