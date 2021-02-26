package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeLeRole extends AdDataType {

    public AdDataTypeLeRole(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);
    }

    private boolean isOnlyPeripheralMode() {
        return (value_bytes[0]  == 0x00);
    }

    private boolean isOnlyCentralMode() {
        return (value_bytes[0]  == 0x01);
    }

    private boolean isPeripheralModePreferred() {
        return (value_bytes[0]  == 0x02);
    }

    private boolean isCentralModePreferred() {
        return (value_bytes[0]  == 0x03);
    }

    public String toString() {
        String s = super.toString();
        if (isOnlyPeripheralMode()) {
            s = s + " Only Peripheral Mode";
        }
        if (isOnlyCentralMode()) {
            s = s + " Only Central Mode";
        }
        if (isPeripheralModePreferred()) {
            s = s + " Peripheral Mode Preferred";
        }
        if (isCentralModePreferred()) {
            s = s + " Central Mode Preferred";
        }
        s = s + "\n";
        return s;
    }
}
