package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

/**
 * Created by Martin on 31/10/2016.
 */

public class BeaconEddystoneUid extends BeaconEddystone {

    byte [] uid_namespace = new byte[10];
    byte [] uid_instance = new byte[6];

    // service_data includes the service UUID FEAA
    public BeaconEddystoneUid(byte[] service_data) throws MalformedDataException {
        super(service_data);
        if (service_data.length < 20) {
            throw new MalformedDataException();
        }
        System.arraycopy(service_data,4,uid_namespace,0,10);
        System.arraycopy(service_data,14,uid_instance,0,6);
    }

    public String toString() {
        return super.toString()+"\n"+
                " uid_namespace:0x"+ Utility.bytesToHex(uid_namespace)+"\n"+
                " uid_instance:0x"+ Utility.bytesToHex(uid_instance);
    }
    public static void main(String args[]) throws MalformedDataException {
        byte [] service_data = {(byte)0xFE, (byte)0xAA, // service UUID
                (byte)0x00, // frame type
                (byte)0xF6, // TX power
                (byte)0x00, (byte)0x62, (byte)0x69, (byte)0x74, (byte)0x74,(byte)0x00, (byte)0x62, (byte)0x69, (byte)0x74, (byte)0x74,
                (byte)0x79, (byte)0x73, (byte)0x6F, (byte)0x66, (byte)0x74, (byte)0x77};
        BeaconEddystoneUid beacon = new BeaconEddystoneUid(service_data);
        System.out.println(beacon.toString());
    }
}
