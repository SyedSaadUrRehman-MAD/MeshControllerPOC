package com.bluetooth.androidmeshcontroller.bluetooth.mesh;
import android.util.Log;

import com.bluetooth.androidmeshcontroller.Constants;
import com.bluetooth.androidmeshcontroller.Utility;

import org.spongycastle.crypto.BlockCipher;
import org.spongycastle.crypto.CipherParameters;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.engines.AESEngine;
import org.spongycastle.crypto.engines.AESLightEngine;
import org.spongycastle.crypto.macs.CMac;
import org.spongycastle.crypto.modes.CCMBlockCipher;
import org.spongycastle.crypto.params.AEADParameters;
import org.spongycastle.crypto.params.KeyParameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;

public class Crypto {
    private static Crypto crypto;

    private final byte [] ZERO = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    private final byte [] ZERO_5 = {0,0,0,0,0};
    private byte [] k2_salt;
    private byte [] k3_salt;
    private byte [] k4_salt;
    private final byte [] id64 = {0x69, 0x64, 0x36, 0x34};
    private final byte [] id64_01 = {0x69, 0x64, 0x36, 0x34, 0x01};
    private final byte [] id6  = {0x69, 0x64, 0x36};
    private final byte [] id6_01  = {0x69, 0x64, 0x36, 0x01};
    private BigInteger TWO_POW_64 = new BigInteger("10000000000000000", 16);
    private BigInteger BIGINT_64 = new BigInteger(new byte[]{0x40});

    private Crypto() {
        byte [] k2_plain = {0x73, 0x6d, 0x6b, 0x32};
        k2_salt = s1(k2_plain); // "smk2"
        byte [] k3_plain = {(byte) (0xFF & 0x73), 0x6d, 0x6b, 0x33};
        k3_salt = s1(k3_plain); // "smk3"
        byte [] k4_plain = {0x73, 0x6d, 0x6b, 0x34};
        k4_salt = s1(k4_plain); // "smk4"
    }

    public static synchronized Crypto getInstance() {
        if (crypto == null) {
            crypto = new Crypto();
        }
        return crypto;
    }

    public byte [] getAesCmac(byte [] key, byte [] message) {
        final byte[] cmac_bytes = new byte[16];
        CipherParameters cipherParameters = new KeyParameter(key);
        BlockCipher blockCipher = new AESEngine();
        CMac mac = new CMac(blockCipher);
        mac.init(cipherParameters);
        mac.update(message, 0, message.length);
        mac.doFinal(cmac_bytes, 0);
        return cmac_bytes;
    }

    public byte [] s1(byte [] M) {
        byte [] cmac = getAesCmac(ZERO, M);
        return cmac;
    }

    public K2KeyMaterial k2(byte [] N, byte [] P) throws IOException {
        K2KeyMaterial k2_material = new K2KeyMaterial();

        // T = AES-CMACsalt (N)
        byte []  T = getAesCmac(k2_salt, N);

        byte [] T0 = {};
        //  T1 = AES-CMACt (T0 || P || 0x01)
        byte [] M1 = new byte[P.length + 1];
        System.arraycopy(P,0,M1,0,P.length);
        M1[P.length] = 0x01;

        byte [] T1 = getAesCmac(T, M1);

        // T2 = AES-CMACt (T1 || P || 0x02)
        byte [] M2 = new byte[T1.length+P.length+1];
        System.arraycopy(T1,0,M2,0,T1.length);
        System.arraycopy(P,0,M2,T1.length,P.length);
        M2[M2.length-1] = 0x02;
        byte [] T2 = getAesCmac(T, M2);

        // T3 = AES-CMACt (T2 || P || 0x03)
        byte [] M3 = new byte[T2.length+P.length+1];
        System.arraycopy(T2,0,M3,0,T2.length);
        System.arraycopy(P,0,M3,T2.length,P.length);
        M3[M3.length-1] = 0x03;
        byte [] T3 = getAesCmac(T, M3);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(T1);
        bos.write(T2);
        bos.write(T3);
        byte [] T123 = bos.toByteArray();

        BigInteger TWO_POW_263 = new BigInteger("800000000000000000000000000000000000000000000000000000000000000000",16);
        BigInteger  T123_BIGINT = new BigInteger (T123);
        BigInteger modval = T123_BIGINT.mod(TWO_POW_263);
        String modval_hex = modval.toString(16);

        k2_material.NID = Utility.hexToBytes(modval_hex.substring(0, 2));
        k2_material.encryption_key = Utility.hexToBytes(modval_hex.substring(2, 34));
        k2_material.privacy_key = Utility.hexToBytes(modval_hex.substring(34, 66));

        return k2_material;
    };

    public byte [] k3(byte [] N ) {

        // T = AES-CMACsalt (N)
        byte [] T = getAesCmac(k3_salt, N);

        // k3(N) = AES-CMACt ( “id64” || 0x01 ) mod 2^64
        byte [] k3_cmac = getAesCmac(T, id64_01);

        BigInteger k3_cmac_bigint = new BigInteger(Utility.bytesToHex(k3_cmac), 16);

        BigInteger k3_modval = k3_cmac_bigint.mod(TWO_POW_64);

        return Utility.hexToBytes(k3_modval.toString(16));
    };

    public byte k4(byte [] N) {

        System.out.println("XXXX k4 "+Utility.bytesToHex(N));

        // K4(N) = AES-CMACt ( “id6” || 0x01 ) mod 2^6

        // T = AES-CMACsalt (N)
        byte [] T = getAesCmac(k4_salt, N);
        System.out.println("XXXX k4 T="+Utility.bytesToHex(T));

        //  k4_cmac = crypto.getAesCmac(T.toString(), id6_hex + "01");
        byte [] k4_cmac = getAesCmac(T, id6_01);
        System.out.println("XXXX k4_cmac="+Utility.bytesToHex(k4_cmac));

        BigInteger k4_cmac_bigint = new BigInteger(Utility.bytesToHex(k4_cmac),16);

        BigInteger k4_modval = k4_cmac_bigint.mod(BIGINT_64);

        System.out.println("XXXX k4 k4_modval.toString(16))="+k4_modval.toString(16));

        return Utility.hexToBytes(k4_modval.toString(16))[0];
    };

    public byte [] aesccm(byte [] key, byte [] nonce, byte [] plaintext) {
        final byte[] encrypted_bytes = new byte[plaintext.length + 4];
        final CCMBlockCipher ccmBlockCipher = new CCMBlockCipher(new AESEngine());
        final AEADParameters aeadParameters = new AEADParameters(new KeyParameter(key), 32, nonce);
        ccmBlockCipher.init(true, aeadParameters);
        ccmBlockCipher.processBytes(plaintext, 0, plaintext.length, encrypted_bytes, plaintext.length);
        try {
            ccmBlockCipher.doFinal(encrypted_bytes, 0);
            return encrypted_bytes;
        } catch (InvalidCipherTextException e) {
            Log.e(Constants.TAG, "Error performing AESCCM encryption: " + e.getMessage());
            return null;
        }
    }

    public byte [] e(byte [] key , byte [] plaintext ) {
        final byte[] encrypted = new byte[plaintext.length];
        final CipherParameters cipher_params = new KeyParameter(key);
        final AESLightEngine engine = new AESLightEngine();
        engine.init(true, cipher_params);
        engine.processBlock(plaintext, 0, encrypted, 0);
        return encrypted;
    }

    public byte [] privacyRandom(byte [] enc_dst, byte [] enc_transport_pdu, byte [] netmic) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(enc_dst);
        bos.write(enc_transport_pdu);
        bos.write(netmic);
        byte [] pr = bos.toByteArray();
        byte [] pr07 = new byte[7];
        System.arraycopy(pr,0,pr07,0,7);
        return pr07;
    }

    public byte [] obfuscate(byte [] enc_dst, byte [] enc_transport_pdu, byte [] netmic, byte ctl, byte ttl, byte [] seq, byte [] src, byte [] iv_index, byte [] privacy_key) throws IOException {
        // 1. Create Privacy Random
        byte [] privacy_random = privacyRandom(enc_dst, enc_transport_pdu, netmic);
        Log.d(Constants.TAG,"CCCC privacy_random="+Utility.bytesToHex(privacy_random).toLowerCase());
        Log.d(Constants.TAG,"CCCC ZERO_5="+Utility.bytesToHex(ZERO_5).toLowerCase());
        Log.d(Constants.TAG,"CCCC iv_index="+Utility.bytesToHex(iv_index).toLowerCase());
        Log.d(Constants.TAG,"CCCC privacy_key="+Utility.bytesToHex(privacy_key).toLowerCase());

        // 2. Calculate PECB
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bos.write(ZERO_5);
        bos.write(iv_index);
        bos.write(privacy_random);
        bos.flush();
        byte [] pecb_input = bos.toByteArray();
        Log.d(Constants.TAG,"CCCC pecb_input="+Utility.bytesToHex(pecb_input).toLowerCase());
        byte [] pecb = e(privacy_key, pecb_input);
        byte [] pecb05 = new byte[6];
        System.arraycopy(pecb,0,pecb05,0,6);
        Log.d(Constants.TAG,"CCCC PECB="+Utility.bytesToHex(pecb05).toLowerCase());

        // 3. Obfuscate
        byte ctl_ttl = (byte) (ctl | ttl);
        bos = new ByteArrayOutputStream();
        bos.write(ctl_ttl);
        bos.write(seq);
        bos.write(src);
        bos.flush();
        byte [] ctl_ttl_seq_src = bos.toByteArray();
        Log.d(Constants.TAG,"CCCC ctl_ttl_seq_src="+Utility.bytesToHex(ctl_ttl_seq_src).toLowerCase());
        byte [] obf = Utility.xorU8Arrays(ctl_ttl_seq_src, pecb05);
        return obf;
    }



}
