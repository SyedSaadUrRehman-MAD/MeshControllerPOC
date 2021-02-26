package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import android.bluetooth.le.ScanFilter;
import android.os.ParcelUuid;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class BeaconEddystone extends Beacon {

    public static final byte [] EDDYSTONE_SERVICE_UUID = {(byte) 0xAA,(byte) 0xFE};
    public static final byte EDDYSTONE_UID = 0x00;
    public static final byte EDDYSTONE_URL = 0x10;
    public static final byte EDDYSTONE_TLM = 0x20;
    public static final byte EDDYSTONE_EID = 0x30;

    // The Eddystone Service UUID, 0xFEAA.
    public static final ParcelUuid EDDYSTONE_128_BIT_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

    byte frame_type;
    byte calibrated_tx_power;

    // service_data includes the service UUID FEAA
    public BeaconEddystone(byte[] service_data)  throws MalformedDataException {
        if (service_data.length < 4) {
            throw new MalformedDataException();
        }
        frame_type = service_data[2];
        calibrated_tx_power = service_data[3];
    }

    public static byte getFrameType(byte [] service_data) throws MalformedDataException {
        if (service_data.length > 2) {
            return service_data[2];
        } else {
            throw new MalformedDataException();
        }
    }

    public String toString() {
        String s = Utility.htmlColorYellow("Eddystone ("+getFrameTypeName()+")")+"\n"
                +" TX:"+calibrated_tx_power;
        return s;
    }

    public String getFrameTypeName() {
        switch (frame_type) {
            case EDDYSTONE_UID:
                return "UID";
            case EDDYSTONE_URL:
                return "URL";
            case EDDYSTONE_TLM:
                return "TLM";
            case EDDYSTONE_EID:
                return "EID";
            default:
                return "???";
        }
    }
}
