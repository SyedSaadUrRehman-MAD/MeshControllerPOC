package com.bluetooth.androidmeshcontroller.bluetooth.mesh;

import com.bluetooth.androidmeshcontroller.Utility;

public class AuthEncNetwork {
    public byte [] enc_dst = new byte [2];
    public byte [] enc_transport_pdu;
    public byte [] netMIC = new byte [4];

    public String toString() {
        return "enc_dst="+Utility.bytesToHex(enc_dst)+" enc_transport_pdu="+Utility.bytesToHex(enc_transport_pdu)+" netMIC="+Utility.bytesToHex(netMIC);
    }
}
