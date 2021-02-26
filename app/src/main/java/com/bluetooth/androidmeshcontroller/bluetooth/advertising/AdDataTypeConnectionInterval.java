package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.ValueNotDefinedException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeConnectionInterval extends AdDataType {

    private byte [] conn_interval_min;

    private byte [] conn_interval_max;

    public AdDataTypeConnectionInterval(byte type_id, String type_name, byte[] value_bytes) {
        super(type_id, type_name, value_bytes);
        conn_interval_min = new byte[2];
        conn_interval_max = new byte[2];
        conn_interval_min[0] = value_bytes[0];
        conn_interval_min[1] = value_bytes[1];
        conn_interval_max[0] = value_bytes[2];
        conn_interval_max[1] = value_bytes[3];
    }

    public byte [] getConn_interval_min_raw() {
        return conn_interval_min;
    }

    public byte [] getConn_interval_max_raw() {
        return conn_interval_max;
    }

    public short getConn_interval_min_ms() throws ValueNotDefinedException {
        if (conn_interval_min[1] != (byte) 0xFF || conn_interval_min[0] != (byte) 0xFF) {
            return (short) ((conn_interval_min[0] << 8 + conn_interval_min[1]) * 1.25);
        } else {
            throw new ValueNotDefinedException();
        }
    }

    public short getConn_interval_max_ms() throws ValueNotDefinedException {
        if (conn_interval_max[0] != (byte) 0xFF || conn_interval_max[1] != (byte) 0xFF) {
            return (short) ((conn_interval_max[1] << 8 + conn_interval_max[0]) * 1.25);
        } else {
            throw new ValueNotDefinedException();
        }
    }

    public String toString() {
        String s = super.toString();
        try {
            short min = getConn_interval_min_ms();
            s = s + " Min Slave Conn Interval:"+min+"ms (" + Utility.bytesToHex(getConn_interval_min_raw())+")";
        } catch (ValueNotDefinedException e) {
            s = s + " Min Slave Conn Interval: no minimum";
        }
        try {
            short max = getConn_interval_max_ms();
            s = s + " Max Slave Conn Interval:"+max+"ms (" + Utility.bytesToHex(getConn_interval_max_raw())+")";
        } catch (ValueNotDefinedException e) {
            s = s + " Max Slave Conn Interval: no minimum";
        }
        s = s + "\n";
        return s;
    }
}
