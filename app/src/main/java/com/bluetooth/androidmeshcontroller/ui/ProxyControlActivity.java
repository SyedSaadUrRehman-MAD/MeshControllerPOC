package com.bluetooth.androidmeshcontroller.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.bluetooth.androidmeshcontroller.Constants;
import com.bluetooth.androidmeshcontroller.R;
import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.bluetooth.BleAdapterService;
import com.bluetooth.androidmeshcontroller.bluetooth.mesh.BluetoothMesh;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ProxyControlActivity extends Activity {
    public static final String EXTRA_ID = "id";

    private String device_address;
    private Timer mTimer;
    private boolean back_requested = false;
    private BleAdapterService bluetooth_le_adapter;
    private BluetoothMesh mesh;
    private String destination_address;
    private Timer timer;
    private boolean connecting = false;
    private Button last_address_button;
    private Handler readHandler = new Handler();
    private Runnable readRunnable = new Runnable() {
        @Override
        public void run() {
            readHandler.removeCallbacks(readRunnable);
            if (bluetooth_le_adapter != null) {
                boolean result = bluetooth_le_adapter.readCharacteristic(BleAdapterService.MESH_PROXY_SERVICE_UUID,
                        BleAdapterService.MESH_PROXY_DATA_OUT);
                Log.d(Constants.TAG, "Read Characteristics" + result);
            }
//            readHandler.postDelayed(readRunnable, 1000);
        }
    };

    public void subscribeNoti(View view) {
        if(bluetooth_le_adapter != null)
        {
            bluetooth_le_adapter.setIndicationsState(BleAdapterService.MESH_PROXY_SERVICE_UUID,
                    BleAdapterService.MESH_PROXY_DATA_IN,true);
            bluetooth_le_adapter.setIndicationsState(BleAdapterService.MESH_PROXY_SERVICE_UUID,
                    BleAdapterService.MESH_PROXY_DATA_OUT,true);
            readHandler.post(readRunnable);
        }
    }

    class ClearMsgTask extends TimerTask {
        public void run() {
            showMsg("");
        }
    }

    private final ServiceConnection service_connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(message_handler);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetooth_le_adapter = null;
        }
    };

    @SuppressLint("HandlerLeak")
    private Handler message_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            String service_uuid = "";
            String characteristic_uuid = "";
            byte[] b = null;

            bundle = msg.getData();
            Log.d(Constants.TAG,"mesh message handler => msg ="+ msg.toString());
            switch (msg.what) {
                case BleAdapterService.MESSAGE:
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(text);
                    break;
                case BleAdapterService.GATT_CONNECTED:
                    connecting = false;
                    setUiControlsState(true);
                    ((Button) ProxyControlActivity.this.findViewById(R.id.btn_connect)).setText("DISCONNECT");
                    bluetooth_le_adapter.discoverServices();
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    showMsg("Disconnected from the selected mesh proxy");
                    setUiControlsState(false);
                    ((Button) ProxyControlActivity.this.findViewById(R.id.btn_connect)).setText("CONNECT");
                    if (back_requested) {
                        ProxyControlActivity.this.finish();
                    }
                    break;

                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    ((Button) ProxyControlActivity.this.findViewById(R.id.btn_connect)).setEnabled(true);
                    // validate services and if ok....
                    List<BluetoothGattService> slist = bluetooth_le_adapter.getSupportedGattServices();
                    boolean mesh_proxy_service_present = false;
                    boolean mesh_proxy_data_in_present = false;
                    boolean mesh_proxy_data_out_present = false;

                    for (BluetoothGattService svc : slist) {
                        Log.d(Constants.TAG, "UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());
                        if (svc.getUuid().toString().equalsIgnoreCase(BleAdapterService.MESH_PROXY_SERVICE_UUID)) {
                            mesh_proxy_service_present = true;
                            for (BluetoothGattCharacteristic characteristic : svc.getCharacteristics()) {

                                if (characteristic.getUuid().toString().equalsIgnoreCase(BleAdapterService.MESH_PROXY_DATA_IN)) {
                                    mesh_proxy_data_in_present = true;
                                    continue;
                                }
                                if (characteristic.getUuid().toString().equalsIgnoreCase(BleAdapterService.MESH_PROXY_DATA_OUT)) {
                                    mesh_proxy_data_out_present = true;
                                    continue;
                                }
                            }
                        }
                    }

                    if (mesh_proxy_service_present && mesh_proxy_data_in_present && mesh_proxy_data_out_present) {
                        showMsg("Connected: Device is a mesh proxy");
                        bluetooth_le_adapter.requestMtu(32);
                    } else {
                        showMsg(Utility.htmlColorRed("Connected. ERROR: Device is not a mesh proxy"));
                        setUiControlsState(false);
                    }
                    break;

                case BleAdapterService.GATT_CHARACTERISTIC_WRITTEN:
                    bundle = msg.getData();
                    Log.d(Constants.TAG, "Service=" + bundle.get(BleAdapterService.PARCEL_SERVICE_UUID).toString().toUpperCase() + " Characteristic=" + bundle.get(BleAdapterService.PARCEL_CHARACTERISTIC_UUID).toString().toUpperCase());
                    break;

                case BleAdapterService.MTU_CHANGED:
                    int mtu = bundle.getInt(BleAdapterService.PARCEL_VALUE);
                    showMsg("MTU changed to " + mtu);
                    break;
            }
        }
    };

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        back_requested = true;
        if (bluetooth_le_adapter.isConnected()) {
            try {
                bluetooth_le_adapter.disconnect();
            } catch (Exception e) {
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proxy_control);

        timer = new Timer();

        // read intent data
        final Intent intent = getIntent();
        device_address = intent.getStringExtra(EXTRA_ID);

        setUiControlsState(false);

        // connect to the Bluetooth adapter service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, service_connection, BIND_AUTO_CREATE);

        try {
            mesh = BluetoothMesh.getInstance();
            mesh.setContext(this);
//            changeDst("C001");
            changeDst("0002");
//            changeDst("C000");
            ((Button) ProxyControlActivity.this.findViewById(R.id.btn_all)).setTextColor(Color.RED);
            last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_all));
            showMsg("READY");
        } catch (IOException e) {
            e.printStackTrace();
            showMsg(Utility.htmlColorRed("FATAL ERROR: No BluetoothMesh instance:" + e.getMessage()));
        }
    }

    private void setUiControlsState(boolean state) {
        // disable DST selection buttons
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_c1)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_c2)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_c3)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_c4)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_r1)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_r2)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_r3)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_r4)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_all)).setEnabled(state);

        // disable ON/OFF buttons
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_on)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_off)).setEnabled(state);

        // disable colour selection buttons
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_white)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_red)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_green)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_blue)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_yellow)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_cyan)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_magenta)).setEnabled(state);
        ((Button) ProxyControlActivity.this.findViewById(R.id.btn_black)).setEnabled(state);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(service_connection);
        bluetooth_le_adapter = null;
    }

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.txt_msg)).setText(msg);
            }
        });
    }

    private void showMsg(final String msg, int clear_after_s) {
        showMsg(msg);
        timer.schedule(new ClearMsgTask(), clear_after_s * 1000);
    }

    private void changeDst(final String dst) {
        destination_address = dst;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.txt_dts)).setText("DST: 0x" + dst);
            }
        });
    }

    public void onConnect(View view) {
        if (bluetooth_le_adapter != null) {
            if (!connecting && !bluetooth_le_adapter.isConnected()) {
                connecting = true;
                ((Button) ProxyControlActivity.this.findViewById(R.id.btn_connect)).setEnabled(false);
                showMsg("Connecting....");
                if (!bluetooth_le_adapter.connect(device_address)) {
                    showMsg("onConnect: failed to connect");
                    setUiControlsState(true);
                }
            } else {
                bluetooth_le_adapter.disconnect();
            }
        } else {
            showMsg("onConnect: bluetooth_le_adapter=null");
        }
    }

    public void onC1(View view) {
        changeDst("C021");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_c1));
        last_address_button.setTextColor(Color.RED);
    }

    public void onC2(View view) {
        changeDst("C022");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_c2));
        last_address_button.setTextColor(Color.RED);
    }

    public void onC3(View view) {
        changeDst("C023");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_c3));
        last_address_button.setTextColor(Color.RED);
    }

    public void onC4(View view) {
        changeDst("C024");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_c4));
        last_address_button.setTextColor(Color.RED);
    }

    public void onR1(View view) {
        changeDst("C011");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_r1));
        last_address_button.setTextColor(Color.RED);
    }

    public void onR2(View view) {
        changeDst("C012");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_r2));
        last_address_button.setTextColor(Color.RED);
    }

    public void onR3(View view) {
        changeDst("C013");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_r3));
        last_address_button.setTextColor(Color.RED);
    }

    public void onR4(View view) {
        changeDst("C014");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_r4));
        last_address_button.setTextColor(Color.RED);
    }

    public void onAll(View view) {
        changeDst("C001");
        last_address_button.setTextColor(Color.BLACK);
        last_address_button = ((Button) ProxyControlActivity.this.findViewById(R.id.btn_all));
        last_address_button.setTextColor(Color.RED);
    }

    public void onOn(View view) {
        showMsg("sending generic on off set unack (1)", 5);
        mesh.sendGenericOnOffSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), (byte) 1);
    }

    public void onVendorModel(View view) {
        showMsg("sending Vendor Model Trigger set unack (1)", 5);
        mesh.sendVendorModelSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), (byte) 1);
    }

    public void onOff(View view) {
        showMsg("sending generic on off set unack (0)", 5);
        mesh.sendGenericOnOffSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), (byte) 0);
    }

    public void onWhite(View view) {
        showMsg("sending HSL set set unack (white)", 5);
        mesh.sendLightHslSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), Constants.H[Constants.WHITE], Constants.S[Constants.WHITE], Constants.L[Constants.WHITE]);
    }

    public void onRed(View view) {
        showMsg("sending HSL set set unack (red)", 5);
        mesh.sendLightHslSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), Constants.H[Constants.RED], Constants.S[Constants.RED], Constants.L[Constants.RED]);
    }

    public void onGreen(View view) {
        showMsg("sending HSL set set unack (green)", 5);
        mesh.sendLightHslSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), Constants.H[Constants.GREEN], Constants.S[Constants.GREEN], Constants.L[Constants.GREEN]);
    }

    public void onBlue(View view) {
        showMsg("sending HSL set set unack (blue)", 5);
        mesh.sendLightHslSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), Constants.H[Constants.BLUE], Constants.S[Constants.BLUE], Constants.L[Constants.BLUE]);
    }

    public void onYellow(View view) {
        showMsg("sending HSL set set unack (yellow)", 5);
        mesh.sendLightHslSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), Constants.H[Constants.YELLOW], Constants.S[Constants.YELLOW], Constants.L[Constants.YELLOW]);
    }

    public void onCyan(View view) {
        showMsg("sending HSL set set unack (cyan)", 5);
        mesh.sendLightHslSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), Constants.H[Constants.CYAN], Constants.S[Constants.CYAN], Constants.L[Constants.CYAN]);
    }

    public void onMagenta(View view) {
        showMsg("sending HSL set set unack (magenta)", 5);
        mesh.sendLightHslSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), Constants.H[Constants.MAGENTA], Constants.S[Constants.MAGENTA], Constants.L[Constants.MAGENTA]);
    }

    public void onBlack(View view) {
        showMsg("sending HSL set set unack (black)", 5);
        mesh.sendLightHslSetUnack(bluetooth_le_adapter, Utility.hexToBytes(destination_address), Constants.H[Constants.BLACK], Constants.S[Constants.BLACK], Constants.L[Constants.BLACK]);
    }

    public String byteArrayAsHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        int l = bytes.length;
        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < l; i++) {
            if ((bytes[i] >= 0) & (bytes[i] < 16))
                hex.append("0");
            hex.append(Integer.toString(bytes[i] & 0xff, 16).toUpperCase());
        }
        return hex.toString();
    }
}
