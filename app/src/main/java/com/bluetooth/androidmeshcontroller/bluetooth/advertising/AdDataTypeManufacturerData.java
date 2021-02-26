package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import android.util.Log;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeManufacturerData extends AdDataType {

    private byte [] company_id;
    private byte [] remainder;
    private boolean apple_id=false;
    private boolean radius_networks_id=false;

    String TAG = "AdvScanner";

    public AdDataTypeManufacturerData(byte type_id, String type_name, byte[] value_bytes) throws MalformedDataException {
        super(type_id, type_name, value_bytes);
        if (value_bytes.length < 4) {
            throw new MalformedDataException();
        }
        company_id = new byte[2];
        remainder = new byte[value_bytes.length - 2];
        System.arraycopy(value_bytes,0,company_id,0,2);
        System.arraycopy(value_bytes,2,remainder,0,value_bytes.length - 2);
        if (company_id[0] == 0x4C && company_id[1] == 0x00) {
            apple_id = true;
            Log.d(TAG,"Apple");
        } else {
            if (company_id[0] == 0x18 && company_id[1] == 0x01) {
                radius_networks_id = true;
                Log.d(TAG,"Radius Networks");
            }
        }
    }

    public String toString() {
        String s = super.toString();
        s = s + "\n";
        s = s + "  "+"Company ID: " + Utility.bytesToHex(company_id);
        s = s + "\n";
        s = s + "  "+"Mfr Data Remainder: " + Utility.bytesToHex(remainder);
        s = s + "\n";
        if (isAltBeacon()) {
            s = s + "  "+"Beacon Type: "+ "AltBeacon";
            s = s + "\n";
        }
        if (isIBeacon()) {
            s = s + "  "+"Beacon Type: "+"iBeacon";
            s = s + "\n";
        }
       return s;
    }

    public boolean isIBeacon() {
        if (apple_id && (remainder[0] == 0x02)) {
            return true;
        }
        return false;
    }

    public boolean isAltBeacon() {
        Log.d(TAG,Utility.bytesToHex(remainder));
        if (radius_networks_id && ((remainder[0] & 0xFF) == 0xBE) && ((remainder[1] & 0xFF) == 0xAC)) {
            Log.d(TAG,"AltBeacon");
            return true;
        }
        Log.d(TAG,"NOT AltBeacon");
        return false;
    }

    public byte[] getCompany_id() {
        return company_id;
    }
}
