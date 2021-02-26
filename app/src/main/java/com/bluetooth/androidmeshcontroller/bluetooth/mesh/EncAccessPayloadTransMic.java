package com.bluetooth.androidmeshcontroller.bluetooth.mesh;

import com.bluetooth.androidmeshcontroller.Utility;

public class EncAccessPayloadTransMic {
    public byte [] enc_access_payload;
    public byte [] transMIC = new byte [4];

    public byte [] getUpperTransportPdu() {
        byte [] upper_transport_pdu = new byte[enc_access_payload.length + 4];
        System.arraycopy(enc_access_payload,0,upper_transport_pdu,0,enc_access_payload.length);
        System.arraycopy(transMIC,0,upper_transport_pdu,enc_access_payload.length,4);
        return upper_transport_pdu;
    }

    public int getLength() {
        return enc_access_payload.length + 4;
    }

    public String toString() {
        return "enc_access_payload="+ Utility.bytesToHex(enc_access_payload)+" transMIC="+Utility.bytesToHex(transMIC);
    }
}
