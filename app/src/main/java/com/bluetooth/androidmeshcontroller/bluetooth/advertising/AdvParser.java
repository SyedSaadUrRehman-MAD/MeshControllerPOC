package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import android.util.Log;

import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

import java.util.ArrayList;
import java.util.Hashtable;

public class AdvParser {

    private static AdvParser instance;

    private Hashtable<Byte, String> adv_data_types;

    String TAG = "AdvScanner";

    public static final byte AD_COMPLETE_LIST_16_BIT_SERVICE_UUID = 0x03;
    public static final byte AD_SERVICE_DATA_16_BIT               = 0x16;

    private AdvParser() {
        adv_data_types = new Hashtable<Byte, String>();
        adv_data_types.put(new Byte((byte) 0x01), "Flags");
        adv_data_types.put(new Byte((byte) 0x02), "Incomplete list 16 bit service UUIDs");
        adv_data_types.put(new Byte((byte) 0x03), "Complete list 16 bit service UUIDs");
        adv_data_types.put(new Byte((byte) 0x04), "Incomplete list 32 bit service UUIDs");
        adv_data_types.put(new Byte((byte) 0x05), "Complete list 32 bit service UUIDs");
        adv_data_types.put(new Byte((byte) 0x06), "Incomplete list 128 bit service UUIDs");
        adv_data_types.put(new Byte((byte) 0x07), "Complete list 128 bit service UUIDs");
        adv_data_types.put(new Byte((byte) 0x08), "Shortened local name");
        adv_data_types.put(new Byte((byte) 0x09), "Complete local name");
        adv_data_types.put(new Byte((byte) 0x0A), "Tx power level");
        adv_data_types.put(new Byte((byte) 0x12), "Slave Connection Interval Range");
        adv_data_types.put(new Byte((byte) 0x14), "List of 16-bit Service Solicitation UUIDs");
        adv_data_types.put(new Byte((byte) 0x1F), "List of 32-bit Service Solicitation UUIDs");
        adv_data_types.put(new Byte((byte) 0x15), "List of 128-bit Service Solicitation UUIDs");
        adv_data_types.put(new Byte((byte) 0x16), "Service Data (16-bit UUID)");
        adv_data_types.put(new Byte((byte) 0x20), "Service Data (32-bit UUID)");
        adv_data_types.put(new Byte((byte) 0x21), "Service Data (128-bit UUID)");
        adv_data_types.put(new Byte((byte) 0x17), "Public Target Address");
        adv_data_types.put(new Byte((byte) 0x18), "Random Target Address");
        adv_data_types.put(new Byte((byte) 0x19), "Appearance");
        adv_data_types.put(new Byte((byte) 0x1A), "Advertising Interval");
        adv_data_types.put(new Byte((byte) 0x1B), "LE Bluetooth Device Address");
        adv_data_types.put(new Byte((byte) 0x1C), "LE Role");
        adv_data_types.put(new Byte((byte) 0x24), "URI");
        adv_data_types.put(new Byte((byte) 0xFF), "Manufacturer Specific Data");

    }

    public static synchronized AdvParser getInstance() {
        if (instance == null) {
            instance = new AdvParser();
        }
        return instance;
    }

    public ArrayList<AdDataType> parse(AdvertisingPacket packet) {
        byte[] bytes = packet.getPacketBytes();

        Log.d(TAG, "parse: " + Utility.bytesToHex(bytes));

        ArrayList<AdDataType> ad_types = new ArrayList<AdDataType>();
        int i = 0;
        int len = bytes[i];
        byte tag = bytes[i + 1];
        byte[] value = new byte[len - 1];

        boolean finished = false;
        while (!finished) {
            System.arraycopy(bytes, i + 2, value, 0, len - 1);
            String type_name = adv_data_types.get(new Byte((tag)));
            if (type_name == null) {
                type_name = "Unknown";
            }

            AdDataType ad_type;

            switch (tag) {
                case 0x01:
                    ad_type = new AdDataTypeFlags(tag, type_name, value);
                    break;
                case 0x02:
                    // incomplete list of...
                    ad_type = new AdDataType16BitUuids(tag, type_name, value);
                    break;
                case 0x03:
                    // complete list of...
                    ad_type = new AdDataType16BitUuids(tag, type_name, value);
                    break;
                case 0x04:
                    // incomplete list of...
                    ad_type = new AdDataType32BitUuids(tag, type_name, value);
                    break;
                case 0x05:
                    // complete list of...
                    ad_type = new AdDataType32BitUuids(tag, type_name, value);
                    break;
                case 0x06:
                    // incomplete list of...
                    ad_type = new AdDataType128BitUuids(tag, type_name, value);
                    break;
                case 0x07:
                    // complete list of...
                    ad_type = new AdDataType128BitUuids(tag, type_name, value);
                    break;
                case 0x08:
                    // shortened local name
                    ad_type = new AdDataTypeUtf8(tag, type_name, value);
                    break;
                case 0x09:
                    // complete local name
                    ad_type = new AdDataTypeUtf8(tag, type_name, value);
                    break;
                case 0x0A:
                    ad_type = new AdDataTypeSbyte(tag, type_name, value);
                    break;
                case 0x12:
                    ad_type = new AdDataTypeConnectionInterval(tag, type_name, value);
                    break;
                case 0x14:
                    ad_type = new AdDataType16BitUuids(tag, type_name, value);
                    break;
                case 0x1F:
                    ad_type = new AdDataType32BitUuids(tag, type_name, value);
                    break;
                case 0x15:
                    ad_type = new AdDataType128BitUuids(tag, type_name, value);
                    break;
                case 0x16:
                    ad_type = new AdDataTypeServiceData16(tag, type_name, value);
                    break;
                case 0x17:
                    // Public Target Address
                    try {
                        ad_type = new AdDataTypeTargetAddress(tag, type_name, value);
                    } catch (MalformedDataException e) {
                        ad_type = new AdDataTypeMalformed(tag, type_name, value);
                    }
                    break;
                case 0x18:
                    // Random Target Address
                    try {
                        ad_type = new AdDataTypeTargetAddress(tag, type_name, value);
                    } catch (MalformedDataException e) {
                        ad_type = new AdDataTypeMalformed(tag, type_name, value);
                    }
                    break;
                case 0x19:
                    try {
                    ad_type = new AdDataTypeShort(tag, type_name, value);
                    } catch (MalformedDataException e) {
                        ad_type = new AdDataTypeMalformed(tag, type_name, value);
                    }
                case 0x1A:
                    ad_type = new AdDataTypeAdvertisingInterval(tag, type_name, value);
                    break;
                case 0x1B:
                    try {
                        ad_type = new AdDataTypeLeDeviceAddress(tag, type_name, value);
                    } catch (MalformedDataException e) {
                        ad_type = new AdDataTypeMalformed(tag, type_name, value);
                    }
                    break;
                case 0x1C:
                    ad_type = new AdDataTypeLeRole(tag, type_name, value);
                    break;
                case 0x20:
                    ad_type = new AdDataTypeServiceData32(tag, type_name, value);
                    break;
                case 0x21:
                    ad_type = new AdDataTypeServiceData128(tag, type_name, value);
                    break;
                case 0x24:
                    ad_type = new AdDataTypeUri(tag, type_name, value);
                    break;
                case (byte) 0xFF:
                    try {
                        ad_type = new AdDataTypeManufacturerData(tag, type_name, value);
                    } catch (MalformedDataException e) {
                        ad_type = new AdDataTypeMalformed(tag, type_name, value);
                    }
                    break;
                default:ad_type = new AdDataType(tag, type_name, value);
            }

            ad_types.add(ad_type);
            i = i + len + 1;
            if (i > bytes.length) {
                finished = true;
            } else {
                len = bytes[i];
                if (len > 0) {
                    tag = bytes[i + 1];
                    value = new byte[len - 1];
                } else {
                    finished = true;
                }
            }
        }
        return ad_types;
    }

}
