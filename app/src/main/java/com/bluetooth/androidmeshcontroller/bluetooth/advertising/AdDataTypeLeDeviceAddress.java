package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeLeDeviceAddress extends AdDataType {

    private DeviceAddress device_address;

    public AdDataTypeLeDeviceAddress(byte type_id, String type_name, byte[] value_bytes) throws MalformedDataException {
        super(type_id, type_name, value_bytes);
        if (value_bytes.length % 7 != 0) {
            throw new MalformedDataException();
        }
        byte [] dev_addr = new byte[7];
        System.arraycopy(value_bytes,0,dev_addr,0,7);
        device_address = new DeviceAddress(dev_addr);
    }

    public String toString() {
        String s = super.toString();
        s = s + " " + device_address.humanReadable();
        s = s + "\n";
       return s;
    }
}
