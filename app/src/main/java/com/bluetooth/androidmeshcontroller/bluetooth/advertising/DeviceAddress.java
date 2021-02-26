package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

/**
 * Created by mwoolley on 30/07/2015.
 */
public class DeviceAddress {
    private byte[] device_address;
    private boolean random_addr;
    private boolean public_addr;

    public DeviceAddress(byte[] device_addr) throws MalformedDataException {
        if (device_addr.length == 7) {
            this.device_address = device_addr;
        } else {
            throw new MalformedDataException();
        }
        random_addr = false;
        public_addr = false;

        if ((device_addr[6] & 0x01) == 0x01) {
            random_addr = true;
        } else {
            public_addr = true;
        }
    }

    public byte[] getDevice_address() {
        return device_address;
    }

    public boolean isRandom_addr() {
        return random_addr;
    }

    public boolean isPublic_addr() {
        return public_addr;
    }

    public String humanReadable() {
        String hr = Utility.colonSeperatedHexString(device_address);
        if (isPublic_addr()) {
            hr = hr + " (P)";
        }
        if (isRandom_addr()) {
            hr = hr + " (R)";
        }
        return hr;
    }
}
