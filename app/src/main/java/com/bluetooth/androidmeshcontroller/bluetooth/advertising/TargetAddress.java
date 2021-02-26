package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

/**
 * Created by mwoolley on 30/07/2015.
 */
public class TargetAddress {
    private byte[] pta;

    public TargetAddress(byte[] pta) throws MalformedDataException {
        if (pta.length == 6) {
            this.pta = pta;
        } else {
            throw new MalformedDataException();
        }
    }

    public byte[] getPta() {
        return pta;
    }
}
