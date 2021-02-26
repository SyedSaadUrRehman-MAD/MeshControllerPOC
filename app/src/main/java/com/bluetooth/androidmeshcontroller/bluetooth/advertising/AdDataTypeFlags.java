package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.bluetooth.advertising.AdDataType;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeFlags extends AdDataType {

    public AdDataTypeFlags(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);
    }

    private boolean isLeLimitedDiscoverableMode() {
        return ((byte) (value_bytes[0] & 0x01) == 0x01);
    }

    private boolean isLeGeneralDiscoverableMode() {
        return ((byte) (value_bytes[0] & 0x02) == 0x02);
    }

    private boolean isBrEdrNotSupported() {
        return ((byte) (value_bytes[0] & 0x04) == 0x04);
    }

    private boolean isSimultaneousLeBrEdrController() {
        return ((byte) (value_bytes[0] & 0x08) == 0x08);
    }

    private boolean isSimultaneousLeBrEdrHost() {
        return ((byte) (value_bytes[0] & 0x10) == 0x10);
    }

    public String toString() {
        String s = super.toString();
        if (isLeLimitedDiscoverableMode()) {
            s = s + " LE Limited Discoverable Mode";
        }
        if (isLeGeneralDiscoverableMode()) {
            s = s + " LE General Discoverable Mode";
        }
        if (isBrEdrNotSupported()) {
            s = s + " BR/EDR Not Supported";
        }
        if (isSimultaneousLeBrEdrController()) {
            s = s + " Simultaneous LE and BR/EDR (Controller)";
        }
        if (isSimultaneousLeBrEdrHost()) {
            s = s + " Simultaneous LE and BR/EDR (Host)";
        }
        s = s + "\n";
        return s;
    }
}
