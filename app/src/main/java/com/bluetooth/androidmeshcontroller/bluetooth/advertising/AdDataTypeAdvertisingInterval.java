package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.ValueNotDefinedException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeAdvertisingInterval extends AdDataType {

    private byte [] adv_interval;

    public AdDataTypeAdvertisingInterval(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);
        adv_interval = new byte[2];
        adv_interval[0] = value_bytes[0];
        adv_interval[1] = value_bytes[1];
    }

    public byte [] getAdv_interval_raw() {
        return adv_interval;
    }

    public short getAdv_interval_ms() {
        return (short) ((adv_interval[1] << 8 + adv_interval[0]) * 1.25);
    }

    public String toString() {
        String s = super.toString();
        short adv_int = getAdv_interval_ms();
        s = s + " "+adv_int+"ms (0x" + Utility.bytesToHex(getAdv_interval_raw())+")";
        s = s + "\n";
        return s;
    }
}
