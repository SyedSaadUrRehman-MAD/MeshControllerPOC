package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

/**
 * Created by mwoolley on 29/07/2015.
 */
public class AdDataTypeTargetAddress extends AdDataType {

    private TargetAddress[] ptas;

    public AdDataTypeTargetAddress(byte type_id, String type_name, byte[] value_bytes) throws MalformedDataException {
        super(type_id, type_name, value_bytes);

        if (value_bytes.length % 6 != 0) {
            throw new MalformedDataException();
        }
        ptas = new TargetAddress[value_bytes.length / 6];

        int j=0;
        for (int i=0;i<value_bytes.length;i=i+6) {
            byte [] pta_bytes = new byte[6];
            System.arraycopy(value_bytes,i,pta_bytes,0,6);
            TargetAddress pta = new TargetAddress(pta_bytes);
            ptas[j] = pta;
            j++;
        }
    }

    public String toString() {
        String s = super.toString();
        for (int i=0;i<ptas.length;i++) {
            s = s + " TA("+i+"):0x" + Utility.bytesToHex(ptas[i].getPta());
        }
        s = s + "\n";
       return s;
    }
}
