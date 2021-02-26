package com.bluetooth.androidmeshcontroller;

public class Constants {
    public static final String TAG = "amc";
    public static final String FIND = "Find Mesh Proxies";
    public static final String STOP_SCANNING = "Stop Scanning";
    public static final String SCANNING = "Scanning";
    public static final String [] PROXY_ID_TYPES = {"Network","Node"};

    public static final String SEQ_SETTINGS = "com.bluetooth.androidmeshcontroller.seqsettings";
    public static final String SEQ_KEY = "com.bluetooth.androidmeshcontroller.seqno";

    public static int WHITE = 0;
    public static int  RED = 1;
    public static int  GREEN = 2;
    public static int  BLUE = 3;
    public static int  YELLOW = 4;
    public static int  CYAN = 5;
    public static int  MAGENTA = 6;
    public static int  BLACK = 7;

    public static int [] H = {     0,      0,  21845,  43690,  10922,  32768,  54613,  0};
    public static int [] S = {     0,  65535,  65535,  65535,  65535,  65535,  65535,  0};
    public static int [] L = { 65535,  32767,  32767,  32767,  32767,  32767,  32767,  0};

}
