package com.bluetooth.androidmeshcontroller.bluetooth.mesh;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bluetooth.androidmeshcontroller.Constants;
import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.bluetooth.BleAdapterService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class BluetoothMesh {

    private static BluetoothMesh mesh;
    private static Crypto crypto;

    private int seq_number;
    private Context context;

    private byte [] netkey = { 0x56, (byte) (0xFF & 0xc7), 0x69, (byte) (0xFF & 0xf8), (byte) (0xFF & 0x4f), (byte) (0xFF & 0x41), (byte) (0xFF & 0x84), (byte) (0xFF & 0x56), (byte) (0xFF & 0x74), 0x3b, (byte) (0xFF & 0xd1), 0x6f,  (byte) (0xFF & 0xdc),  (byte) (0xFF & 0x9b), (byte) (0xFF & 0xe9), (byte) (0xFF & 0x31)};
//    56C769F84F418456743BD16FDC9BE931: NETWORK KEY h22 firmware 2

//    private byte [] netkey = {(byte) (0xFF & 0xbe), (byte) (0xFF & 0x6f), 0x04, 0x28, (byte) (0xFF & 0xee), (byte) (0xFF & 0x6c), (byte) (0xFF & 0xa7), (byte) (0xFF & 0x6f), 0x39, (byte) (0xFF & 0xe7), 0x05, 0x7b, (byte) (0xFF & 0xaf), (byte) (0xFF & 0xd0), 0x72, (byte) (0xFF & 0xd0)};
    //BE6F0428EE6CA76F39E7057BAFD072D0 mesh controller net key

//    private byte [] netkey = {(byte) (0xFF & 0xaf), (byte) (0xFF & 0xc3), 0x27, 0x0e, (byte) (0xFF & 0xda), (byte) (0xFF & 0x88), 0x02, (byte) (0xFF & 0xf7), 0x2c, 0x1e, 0x53, 0x24, 0x38, (byte) (0xFF & 0xa9), 0x79, (byte) (0xFF & 0xeb)};
//    //AFC3270EDA8802F72C1E532438A979EB mesh controller net key


    private byte [] appkey = {0x50,0x01, (byte) (0xFF & 0xae), 0x4a, (byte) (0xFF &  0xc6), (byte) (0xFF & 0xcb), (byte) (0xFF & 0x23), (byte) (0xFF & 0x0b), (byte) (0xFF & 0xb8), (byte) (0xFF & 0xff), (byte) (0xFF & 0x03),(byte) (0xFF & 0xd5), (byte) (0xFF & 0xec), (byte) (0xFF & 0x8c),(byte) (0xFF & 0xa9), (byte) (0xFF & 0xd4)};
    //    private String appkey = "5001AE4AC6CB230BB8FF03D5EC8CA9D4"; h22 mesh rom 2

//    private byte [] appkey = {0x44, (byte) (0xFF & 0xb5), 0x2b,  0x77, (byte) (0xFF & 0xaf), (byte) (0xFF & 0x8d), (byte) (0xFF & 0xc2), (byte) (0xFF & 0x2a),  0x04, (byte) (0xFF & 0x4b),0x35, 0x68, (byte) (0xFF & 0xce),0x60, (byte) (0xFF & 0xe3),0x10};
//        private String appkey = "44B52B77AF8DC22A044B3568CE60E310"; nrf

//    private byte [] appkey = {0x42, 0x2b, (byte) (0xFF & 0xf4), 0x56, (byte) (0xFF & 0xf5), (byte) (0xFF & 0xf3), (byte) (0xFF & 0xe6), (byte) (0xFF & 0xb7), (byte) (0xFF & 0xc5), (byte) (0xFF & 0xe9), 0x00, 0x6a, 0x02, 0x2b, 0x6d, (byte) (0xFF & 0x8e)};
//    private String appkey = "422BF456F5F3E6B7C5E9006A022B6D8E"; mesh controller

    private byte [] iv_index = {0x00, 0x00, 0x00, 0x00};
    private byte [] encryption_key = {};
    private byte [] privacy_key = {};
    private byte [] network_id = {};
    private byte sar = 0;
    private byte msg_type = 0;

    // network PDU fields
    private byte ivi = 0;
    private byte nid = 0;
    private byte ctl = 0;
    private byte ttl = 0x03;
    private byte [] seq = {0x00, 0x00, 0x01};
    private byte [] src = {0x12, 0x35};
    private byte seg = 0;
    private byte akf = 1; // means application key is in use
    private byte aid = 0;
    private byte [] opcode = {0x00, 0x00};
    private byte [] opparams = {};

    private BluetoothMesh() throws IOException {
        crypto = Crypto.getInstance();
        K2KeyMaterial k2_material = crypto.k2(netkey, new byte[]{0x00});
        encryption_key = k2_material.encryption_key;
        System.out.println("encryption_key: "+Utility.bytesToHex(encryption_key));
        privacy_key = k2_material.privacy_key;
        System.out.println("privacy_key: "+Utility.bytesToHex(privacy_key));
        nid = k2_material.NID[0];
        System.out.println("nid: "+Utility.bytesToHex(new byte[]{nid}));
        network_id = crypto.k3(netkey);
        System.out.println("network_id: "+Utility.bytesToHex(network_id));
        aid = crypto.k4(appkey);
        System.out.println("aid="+Utility.bytesToHex(new byte[]{aid}));
        ivi = Utility.leastSignificantBit(iv_index[3]);
    }

    public static synchronized BluetoothMesh getInstance() throws IOException {
        if (mesh == null) {
            mesh = new BluetoothMesh();
        }
        return mesh;
    }

    public void setContext(Context ctx) {
        this.context = ctx;
        // restore last used sequence number
        SharedPreferences sharedpreferences = context.getSharedPreferences(Constants.SEQ_SETTINGS, Context.MODE_PRIVATE);
        seq_number = (int) sharedpreferences.getInt(Constants.SEQ_KEY,0);
        seq = Utility.intSeqToByteArraySeq(seq_number);
    }

    private void incrementSequenceNumber() {
        seq_number++;
        if (seq_number > 0xFFFFFF) {
            seq_number = 0;
        }
        seq = Utility.intSeqToByteArraySeq(seq_number);
        // persist
        SharedPreferences sharedPref = context.getSharedPreferences(Constants.SEQ_SETTINGS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(Constants.SEQ_KEY, seq_number);
        editor.commit();
    }

    public EncAccessPayloadTransMic meshAuthEncAccessPayload(byte[] key, byte[] nonce, byte[] payload) {
        // EncAccessPayload, TransMIC = AES-CCM (AppKey, Application Nonce, AccessPayload)
        Log.d(Constants.TAG, "TTTT meshAuthEncAccessPayload: key="+Utility.bytesToHex(key)+" nonce="+Utility.bytesToHex(nonce)+" payload="+Utility.bytesToHex(payload));
        EncAccessPayloadTransMic result = new EncAccessPayloadTransMic();
        byte [] ciphertext = crypto.aesccm(key, nonce, payload);
        Log.d(Constants.TAG, "TTTT meshAuthEncAccessPayload: ciphertext="+Utility.bytesToHex(ciphertext));
        byte [] eap = new byte[ciphertext.length - 4];
        System.arraycopy(ciphertext,0,eap,0,ciphertext.length - 4);
        byte [] tmic = new byte[4];
        System.arraycopy(ciphertext,ciphertext.length-4,tmic,0,4);
        result.enc_access_payload = eap;
        Log.d(Constants.TAG, "TTTT meshAuthEncAccessPayload: enc_access_payload="+Utility.bytesToHex(eap));
        result.transMIC = tmic;
        return result;
    };

    public AuthEncNetwork meshAuthEncNetwork(byte[] encryption_key, byte[] nonce, byte[] dst, byte[] transport_pdu) throws IOException {
        AuthEncNetwork result = new AuthEncNetwork();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(dst);
        bos.write(transport_pdu);
        byte [] dst_plus_transport_pdu = bos.toByteArray();
        byte [] ciphertext = crypto.aesccm(encryption_key, nonce , dst_plus_transport_pdu);
        int len = ciphertext.length;
        result.enc_dst[0] = ciphertext[0];
        result.enc_dst[1] = ciphertext[1];
        byte [] etp = new byte[len - 6];
        System.arraycopy(ciphertext,2,etp,0,len - 6);
        result.enc_transport_pdu = etp;
        result.netMIC[0] = ciphertext[len - 4];
        result.netMIC[1] = ciphertext[len - 3];
        result.netMIC[2] = ciphertext[len - 2];
        result.netMIC[3] = ciphertext[len - 1];
        return result;
    }

    private byte [] finaliseProxyPdu(byte [] network_pdu) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte sm = (byte) ((sar << 6) | msg_type);
        bos.write(sm);
        bos.write(network_pdu);
        return bos.toByteArray();
    }

    private EncAccessPayloadTransMic deriveSecureUpperTransportPdu(byte [] access_payload, byte [] dst) throws IOException {
        EncAccessPayloadTransMic upper_trans_pdu;
        // derive Application Nonce (ref 3.8.5.2)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(new byte[]{0x01, 0x00});
        bos.write(seq);
        bos.write(src);
        bos.write(dst);
        bos.write(iv_index);
        byte [] app_nonce = bos.toByteArray();
        Log.d(Constants.TAG, "TTTT deriveSecureUpperTransportPdu: app_nonce="+ Utility.bytesToHex(app_nonce));
        upper_trans_pdu = meshAuthEncAccessPayload(appkey, app_nonce, access_payload);
        return upper_trans_pdu;
    }

    private byte [] deriveLowerTransportPdu(EncAccessPayloadTransMic upper_transport_pdu) {
        byte [] lower_transport_pdu = new byte[upper_transport_pdu.getLength()+1];
        // seg=0 (1 bit), akf=1 (1 bit), aid (6 bits) already derived from k4
        byte ltpdu1 = (byte) ((seg << 7) | (akf << 6) | aid);
        lower_transport_pdu[0] = ltpdu1;
        System.arraycopy(upper_transport_pdu.getUpperTransportPdu(),0,lower_transport_pdu,1,upper_transport_pdu.getLength());
        return lower_transport_pdu;
    }

    private AuthEncNetwork deriveSecureNetworkLayer(byte [] lower_transport_pdu, byte [] dst) throws IOException {
        byte ctl_ttl = (byte) (ctl | ttl);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(0);
        bos.write(ctl_ttl);
        bos.write(seq);
        bos.write(src);
        bos.write(0);
        bos.write(0);
        bos.write(iv_index);
        byte [] net_nonce = bos.toByteArray();
        AuthEncNetwork authencnet = meshAuthEncNetwork(encryption_key, net_nonce, dst, lower_transport_pdu);
        return authencnet;
    }

    private byte [] finaliseNetworkPdu(byte ivi, byte nid, byte [] obfuscated_ctl_ttl_seq_src, byte [] enc_dst, byte [] enc_transport_pdu, byte [] netmic) throws IOException {

        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        byte npdu1 = (byte) ((ivi << 7) | nid);
        bos.write(npdu1);
        bos.write(obfuscated_ctl_ttl_seq_src);
        bos.write(enc_dst);
        bos.write(enc_transport_pdu);
        bos.write(netmic);
        bos.flush();
        return bos.toByteArray();
    }

    private byte [] deriveLowerLayers(byte [] access_payload, byte [] dst) throws IOException {
        // upper transport PDU
        EncAccessPayloadTransMic upper_transport_pdu_obj = deriveSecureUpperTransportPdu(access_payload, dst );
        Log.d(Constants.TAG,"TTTT transMIC: "+Utility.bytesToHex(upper_transport_pdu_obj.transMIC));

        byte [] upper_transport_pdu = upper_transport_pdu_obj.getUpperTransportPdu();
        Log.d(Constants.TAG,"TTTT upper_transport_pdu: "+Utility.bytesToHex(upper_transport_pdu));

        // derive lower transport PDU
        byte [] lower_transport_pdu = deriveLowerTransportPdu(upper_transport_pdu_obj);
        Log.d(Constants.TAG,"TTTT lower_transport_pdu: "+Utility.bytesToHex(lower_transport_pdu));

        // encrypt network PDU
        AuthEncNetwork auth_enc_network = deriveSecureNetworkLayer(lower_transport_pdu, dst);
        Log.d(Constants.TAG,"TTTT enc_net: "+Utility.bytesToHex(auth_enc_network.enc_transport_pdu));

        // obfuscate
        byte [] obfuscated_ctl_ttl_seq_src = crypto.obfuscate(auth_enc_network.enc_dst, auth_enc_network.enc_transport_pdu, auth_enc_network.netMIC, ctl, ttl, seq, src, iv_index, privacy_key);
        Log.d(Constants.TAG,"TTTT obfuscated_ctl_ttl_seq_src: "+Utility.bytesToHex(obfuscated_ctl_ttl_seq_src));

        // finalise
        Log.d(Constants.TAG,"TTTT netMIC: "+Utility.bytesToHex(auth_enc_network.netMIC));
        byte [] network_pdu = finaliseNetworkPdu(ivi, nid, obfuscated_ctl_ttl_seq_src, auth_enc_network.enc_dst, auth_enc_network.enc_transport_pdu, auth_enc_network.netMIC);
        return network_pdu;
    }

    public boolean sendGenericOnOffSetUnack(BleAdapterService adapter, byte [] dst , byte onoff){

        if (!adapter.isConnected()) {
            return false;
        }

        // PDU creation buffer
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // access layer
        byte [] access_payload;
        byte [] opcode = { (byte) (0xFF & 0x82) , 0x03 };
        try {
            bos.write(opcode);
            bos.write(onoff);
            // TID
            bos.write(new byte[]{01});
            access_payload = bos.toByteArray();
            // create obfuscated network PDU fields
            byte [] obfuscated_network_pdu = deriveLowerLayers(access_payload, dst);
            byte [] pdu = finaliseProxyPdu(obfuscated_network_pdu);

            Log.d(Constants.TAG,"TTTT netkey: "+Utility.bytesToHex(netkey));
            Log.d(Constants.TAG,"TTTT appkey: "+Utility.bytesToHex(appkey));
//            Log.d(Constants.TAG,"TTTT appkeybytesnrf: "+new String (Utility.hexToBytes(nrfAppKey)));
            Log.d(Constants.TAG,"TTTT iv_index: "+Utility.bytesToHex(iv_index));
            Log.d(Constants.TAG,"TTTT encryption_key: "+Utility.bytesToHex(encryption_key));
            Log.d(Constants.TAG,"TTTT privacy key: "+Utility.bytesToHex(privacy_key));
            Log.d(Constants.TAG,"TTTT seq: "+Utility.bytesToHex(seq));

            boolean result = adapter.writeCharacteristic(BleAdapterService.MESH_PROXY_SERVICE_UUID,
                    BleAdapterService.MESH_PROXY_DATA_IN, pdu);
            incrementSequenceNumber();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            incrementSequenceNumber();
            return false;
        }

    }
    public boolean sendVendorModelSetUnack(BleAdapterService adapter, byte [] dst , byte onoff){

        if (!adapter.isConnected()) {
            return false;
        }

        // PDU creation buffer
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // access layer
        byte [] access_payload;
        byte [] opcode = { (byte) (0xFF & 0x82) , 0x03 };
        try {
            bos.write(opcode);
            bos.write(onoff);
            // TID
            bos.write(new byte[]{01});
            access_payload = bos.toByteArray();
            // create obfuscated network PDU fields
            byte [] obfuscated_network_pdu = deriveLowerLayers(access_payload, dst);
            byte [] pdu = finaliseProxyPdu(obfuscated_network_pdu);

            Log.d(Constants.TAG,"TTTT netkey: "+Utility.bytesToHex(netkey));
            Log.d(Constants.TAG,"TTTT appkey: "+Utility.bytesToHex(appkey));
//            Log.d(Constants.TAG,"TTTT appkeybytesnrf: "+new String (Utility.hexToBytes(nrfAppKey)));
            Log.d(Constants.TAG,"TTTT iv_index: "+Utility.bytesToHex(iv_index));
            Log.d(Constants.TAG,"TTTT encryption_key: "+Utility.bytesToHex(encryption_key));
            Log.d(Constants.TAG,"TTTT privacy key: "+Utility.bytesToHex(privacy_key));
            Log.d(Constants.TAG,"TTTT seq: "+Utility.bytesToHex(seq));

            boolean result = adapter.writeCharacteristic(BleAdapterService.MESH_PROXY_SERVICE_UUID,
                    BleAdapterService.MESH_PROXY_DATA_IN, pdu);
            incrementSequenceNumber();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            incrementSequenceNumber();
            return false;
        }

    }

    public boolean sendLightHslSetUnack(BleAdapterService adapter, byte [] dst, int h, int s, int l) {

        if (!adapter.isConnected()) {
            return false;
        }

        // PDU creation buffer
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        // access layer
        byte [] access_payload;
        byte [] opcode = { (byte) (0xFF & 0x82) , 0x77 };
        try {
            bos.write(opcode);
            bos.write(Utility.uint16ToUint8ArrayLE(l));
            bos.write(Utility.uint16ToUint8ArrayLE(h));
            bos.write(Utility.uint16ToUint8ArrayLE(s));

            // TID
            bos.write(new byte[]{01});
            access_payload = bos.toByteArray();
            // create obfuscated network PDU fields
            byte [] obfuscated_network_pdu = deriveLowerLayers(access_payload, dst);
            byte [] pdu = finaliseProxyPdu(obfuscated_network_pdu);

            Log.d(Constants.TAG,"TTTT netkey: "+Utility.bytesToHex(netkey));
            Log.d(Constants.TAG,"TTTT appkey: "+Utility.bytesToHex(appkey));
            Log.d(Constants.TAG,"TTTT iv_index: "+Utility.bytesToHex(iv_index));
            Log.d(Constants.TAG,"TTTT encryption_key: "+Utility.bytesToHex(encryption_key));
            Log.d(Constants.TAG,"TTTT privacy key: "+Utility.bytesToHex(privacy_key));
            Log.d(Constants.TAG,"TTTT seq: "+Utility.bytesToHex(seq));
            Log.d(Constants.TAG,"TTTT access payload: "+Utility.bytesToHex(access_payload));
            Log.d(Constants.TAG,"TTTT obfuscated_network_pdu: "+Utility.bytesToHex(obfuscated_network_pdu));

            boolean result = adapter.writeCharacteristic(BleAdapterService.MESH_PROXY_SERVICE_UUID,
                    BleAdapterService.MESH_PROXY_DATA_IN, pdu);
            incrementSequenceNumber();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            incrementSequenceNumber();
            return false;
        }

    }


}
