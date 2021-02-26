package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import android.bluetooth.BluetoothDevice;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class AdvertisingPacket {

    private Date event_time;
    private String bd_addr;
    private int rssi;
    private byte [] bytes;
    private ArrayList<AdDataType> ad_types;
    private Beacon beacon;

    private SimpleDateFormat sf_mm_ss = new SimpleDateFormat("mm:ss");
    private SimpleDateFormat sf_mm_ss_SSS = new SimpleDateFormat("mm:ss:SSS");

    public AdvertisingPacket(String bd_addr, int rssi, byte [] bytes) throws MalformedDataException {
        event_time = new Date();
        this.bytes = bytes;
        this.rssi = rssi;
        this.bd_addr = bd_addr;
        ad_types = AdvParser.getInstance().parse(this);
        if (isEddystoneBeacon()) {
            System.out.println("XXXX EDDYSTONE");
            byte [] service_data = getEddystoneServiceData();
            BeaconEddystone eddystone_beacon = null;
            byte frame_type = BeaconEddystone.getFrameType(service_data);
            System.out.println("XXXX EDDYSTONE frame_type="+frame_type);
            switch (frame_type) {
                case BeaconEddystone.EDDYSTONE_UID:
                    eddystone_beacon = new BeaconEddystoneUid(service_data);
                    break;
                case BeaconEddystone.EDDYSTONE_URL:
                    eddystone_beacon = new BeaconEddystoneUrl(service_data);
                    break;
                case BeaconEddystone.EDDYSTONE_TLM:
                    eddystone_beacon = new BeaconEddystoneTlm(service_data);
                    break;
                case BeaconEddystone.EDDYSTONE_EID:
                    eddystone_beacon = new BeaconEddystoneEid(service_data);
                    break;
            }
            beacon = eddystone_beacon;
        }
    }

    public String toString() {
        String s="device: "+bd_addr+" rssi: "+rssi+" ";
        s = s + "\n";
        if (beacon != null && beacon instanceof BeaconEddystone) {
            s = s + ((BeaconEddystone)beacon).toString()+"\n";
        }
        for (AdDataType ad : ad_types) {
            s = s + ad.toString();
        }
        return s;
    }

    public String getMinsSecs() {
        return sf_mm_ss.format(event_time);
    }

    public String getMinsSecsMs() {
        return sf_mm_ss_SSS.format(event_time);
    }

    public Date getEvent_time() {
        return event_time;
    }

    public byte[] getPacketBytes() {
        return bytes;
    }

    public ArrayList<AdDataType> getAd_types() {
        return ad_types;
    }

    public String getBd_addr() {
        return bd_addr;
    }

    public int getRssi() {
        return rssi;
    }

    public boolean isEddystoneBeacon() {
        boolean has_eddystone_service_uuid=false;
        boolean has_eddystone_service_data=false;
        for (AdDataType ad : ad_types) {
            System.out.println("XXXX checking AD "+ad.getType_name());
            if (ad.getType_id() == AdvParser.AD_COMPLETE_LIST_16_BIT_SERVICE_UUID) {
                System.out.println("XXXX AD value: 0x"+Utility.bytesToHex(ad.getValue_bytes()));
            }
             if (ad.getType_id() == AdvParser.AD_COMPLETE_LIST_16_BIT_SERVICE_UUID
                     && ad.getValue_bytes()[0] == BeaconEddystone.EDDYSTONE_SERVICE_UUID[0]
                     && ad.getValue_bytes()[1] == BeaconEddystone.EDDYSTONE_SERVICE_UUID[1]) {
                has_eddystone_service_uuid = true;
             } else {
                 if (ad.getType_id() == AdvParser.AD_SERVICE_DATA_16_BIT
                         && ad.getValue_bytes()[0] == BeaconEddystone.EDDYSTONE_SERVICE_UUID[0]
                         && ad.getValue_bytes()[1] == BeaconEddystone.EDDYSTONE_SERVICE_UUID[1]) {
                     has_eddystone_service_data = true;
                 }
             }
        }
        return (has_eddystone_service_uuid && has_eddystone_service_data);
    }

    public byte [] getEddystoneServiceData() {
        for (AdDataType ad : ad_types) {
            if (ad.getType_id() == AdvParser.AD_SERVICE_DATA_16_BIT
                    && ad.getValue_bytes()[0] == BeaconEddystone.EDDYSTONE_SERVICE_UUID[0]
                    && ad.getValue_bytes()[1] == BeaconEddystone.EDDYSTONE_SERVICE_UUID[1]) {
                System.out.println("XXXX Eddystone service data length: "+ad.getValue_bytes().length);
                return ad.getValue_bytes();
            }
        }
        return null;
    }

    public byte [] getServiceData16() {
        for (AdDataType ad : ad_types) {
            if (ad.getType_id() == AdvParser.AD_SERVICE_DATA_16_BIT) {
                return ad.getValue_bytes();
            }
        }
        return null;
    }

    public Beacon getBeacon() {
        return beacon;
    }

}
