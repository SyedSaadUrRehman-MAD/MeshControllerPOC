package com.bluetooth.androidmeshcontroller.bluetooth.advertising;

import java.util.Hashtable;

/**
 * Created by mwoolley on 31/07/2015.
 */
public class Appearances {

    private static Appearances instance;
    private Hashtable<Short, String> appearances;

    private Appearances() {
        appearances = new Hashtable<Short, String>();
        appearances.put(new Short((short) 0), "Unknown");
        appearances.put(new Short((short) 64), "Generic Phone");
        appearances.put(new Short((short) 128), "Generic Computer");
        appearances.put(new Short((short) 192), "Generic Watch");
        appearances.put(new Short((short) 193), "Watch: Sports Watch");
        appearances.put(new Short((short) 256), "Generic Clock");
        appearances.put(new Short((short) 320), "Generic Display");
        appearances.put(new Short((short) 384), "Generic Remote Control");
        appearances.put(new Short((short) 448), "Generic Eye-glasses");
        appearances.put(new Short((short) 512), "Generic Tag");
        appearances.put(new Short((short) 576), "Generic Keyring");
        appearances.put(new Short((short) 640), "Generic Media Player");
        appearances.put(new Short((short) 704), "Generic Barcode Scanner");
        appearances.put(new Short((short) 768), "Generic Thermometer");
        appearances.put(new Short((short) 769), "Thermometer: Ear");
        appearances.put(new Short((short) 832), "Generic Heart rate Sensor");
        appearances.put(new Short((short) 833), "Heart Rate Sensor: Heart Rate Belt");
        appearances.put(new Short((short) 896), "Generic Blood Pressure");
        appearances.put(new Short((short) 897), "Blood Pressure: Arm");
        appearances.put(new Short((short) 898), "Blood Pressure: Wrist");
        appearances.put(new Short((short) 960), "Human Interface Device (HID)");
        appearances.put(new Short((short) 961), "Keyboard");
        appearances.put(new Short((short) 962), "Mouse");
        appearances.put(new Short((short) 963), "Joystick");
        appearances.put(new Short((short) 964), "Gamepad");
        appearances.put(new Short((short) 965), "Digitizer Tablet");
        appearances.put(new Short((short) 966), "Card Reader");
        appearances.put(new Short((short) 967), "Digital Pen");
        appearances.put(new Short((short) 968), "Barcode Scanner");
        appearances.put(new Short((short) 1024), "Generic Glucose Meter");
        appearances.put(new Short((short) 1088), "Generic: Running Walking Sensor");
        appearances.put(new Short((short) 1089), "Running Walking Sensor: In-Shoe");
        appearances.put(new Short((short) 1090), "Running Walking Sensor: On-Shoe");
        appearances.put(new Short((short) 1091), "Running Walking Sensor: On-Hip");
        appearances.put(new Short((short) 1152), "Generic: Cycling");
        appearances.put(new Short((short) 1153), "Cycling: Cycling Computer");
        appearances.put(new Short((short) 1154), "Cycling: Speed Sensor");
        appearances.put(new Short((short) 1155), "Cycling: Cadence Sensor");
        appearances.put(new Short((short) 1156), "Cycling: Power Sensor");
        appearances.put(new Short((short) 1157), "Cycling: Speed and Cadence Sensor");
        appearances.put(new Short((short) 3136), "Generic: Pulse Oximeter");
        appearances.put(new Short((short) 3137), "Fingertip");
        appearances.put(new Short((short) 3138), "Wrist Worn");
        appearances.put(new Short((short) 3200), "Generic: Weight Scale");
        appearances.put(new Short((short) 5184), "Generic: Outdoor Sports Activity");
        appearances.put(new Short((short) 5185), "Location Display Device");
        appearances.put(new Short((short) 5186), "Location and Navigation Display Device");
        appearances.put(new Short((short) 5187), "Location Pod");
        appearances.put(new Short((short) 5188), "Location and Navigation Pod");
        appearances.put(new Short((short) 5696), "Generic: Environmental Sensor");
    }

    public static synchronized  Appearances getInstance() {
        if (instance == null) {
            instance = new Appearances();
        }
        return instance;
    }

    public String getValue(short appearance) {
        Short app = new Short(appearance);
        String value = appearances.get(app);
        if (value == null) {
            value = "Undefined";
        }
        return value;
    }
}
