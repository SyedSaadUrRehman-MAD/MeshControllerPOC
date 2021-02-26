package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * Created by Martin on 31/10/2016.
 */

public class BeaconEddystoneUrl extends BeaconEddystone {

    private static final String [] URL_PREFIXES = {"http://www.", "https://www.","http://", "https://"};
    private static final String [] EXPANSION_CODES = {".com/", ".org/", ".edu/", ".net/", ".info/", ".biz/", ".gov/", ".com", ".org", ".edu", ".net", ".info", ".biz", ".gov"};

    byte url_scheme_prefix;
    byte [] encoded_url; // length 1-17

    // service_data includes the service UUID FEAA followed by frame type and calibrated power i.e. 4 bytes
    public BeaconEddystoneUrl(byte[] service_data) throws MalformedDataException {
        super(service_data);
        if (service_data.length < 6) {
            throw new MalformedDataException();
        }
        url_scheme_prefix = service_data[4];
        encoded_url = new byte[service_data.length-5];
        System.arraycopy(service_data,5,encoded_url,0,service_data.length-5);
    }

    public String toString() {
        return super.toString()+
                " scheme_prefix:"+url_scheme_prefix+"\n"+
                " URL:"+decodedUrl(encoded_url);
    }

    public String decodedUrl(byte [] encoded_url) {
        String decoded_url="";
        System.out.println("url_prefix="+url_scheme_prefix);
        if (url_scheme_prefix < 4 && url_scheme_prefix >= 0) {
            decoded_url = decoded_url + URL_PREFIXES[url_scheme_prefix];
        } else {
            decoded_url = decoded_url + "????://";
        }
        for (int i=0;i<encoded_url.length;i++) {
            if (encoded_url[i] < 0x0E ) {
                decoded_url = decoded_url + EXPANSION_CODES[encoded_url[i]];
            } else {
                byte [] url_char = new byte[1];
                url_char[0] = encoded_url[i];
                try {
                    decoded_url = decoded_url + new String(url_char,"UTF-8");
                } catch (UnsupportedEncodingException e) {
                    System.out.println(e.getClass().getName()+":"+e.getMessage());
                }
            }
        }
        return decoded_url;
    }

    public static void main(String args[]) throws MalformedDataException {
        byte [] service_data = {(byte)0xFE, (byte)0xAA, // service UUID
                (byte)0x10, // frame type
                (byte)0xF6, // TX power
                (byte)0x00, (byte)0x62, (byte)0x69, (byte)0x74, (byte)0x74,
                (byte)0x79, (byte)0x73, (byte)0x6F, (byte)0x66, (byte)0x74, (byte)0x77,
                (byte)0x61, (byte)0x72, (byte)0x65, (byte)0x07 };
        BeaconEddystoneUrl beacon = new BeaconEddystoneUrl(service_data);
        System.out.println(beacon.toString());
    }
}
