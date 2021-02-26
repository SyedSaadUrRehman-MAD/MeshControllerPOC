package com.bluetooth.androidmeshcontroller.ui;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bluetooth.androidmeshcontroller.Constants;
import com.bluetooth.androidmeshcontroller.R;
import com.bluetooth.androidmeshcontroller.Utility;
import com.bluetooth.androidmeshcontroller.bluetooth.BleScanner;
import com.bluetooth.androidmeshcontroller.bluetooth.MeshProxy;
import com.bluetooth.androidmeshcontroller.bluetooth.ScanResultsConsumer;
import com.bluetooth.androidmeshcontroller.bluetooth.advertising.AdvertisingPacket;
import com.bluetooth.androidmeshcontroller.bluetooth.mesh.AuthEncNetwork;
import com.bluetooth.androidmeshcontroller.bluetooth.mesh.BluetoothMesh;
import com.bluetooth.androidmeshcontroller.bluetooth.mesh.Crypto;
import com.bluetooth.androidmeshcontroller.bluetooth.mesh.EncAccessPayloadTransMic;
import com.bluetooth.androidmeshcontroller.bluetooth.mesh.K2KeyMaterial;
import com.bluetooth.androidmeshcontroller.exceptions.MalformedDataException;

import org.spongycastle.pqc.math.ntru.util.Util;

import java.io.IOException;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements ScanResultsConsumer {

    private boolean ble_scanning = false;
    private Handler handler = new Handler();
    private ListAdapter ble_device_list_adapter;
    private BleScanner ble_scanner;
    private static final long SCAN_TIMEOUT = 30000;
    private static final int REQUEST_LOCATION = 0;
    private static String[] PERMISSIONS_LOCATION = {Manifest.permission.ACCESS_FINE_LOCATION};
    private boolean permissions_granted=false;
    private int device_count=0;
    private Toast toast;

    static class ViewHolder {
        public TextView proxy_type;
        public TextView proxy_id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setButtonText();

        ble_device_list_adapter = new ListAdapter();

        ListView listView = (ListView) this.findViewById(R.id.deviceList);
        listView.setAdapter(ble_device_list_adapter);

        ble_scanner = new BleScanner(this.getApplicationContext());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                if (ble_scanning) {
                    ble_scanner.stopScanning();
                }

                MeshProxy device = ble_device_list_adapter.getDevice(position);
                if (toast != null) {
                    toast.cancel();
                }
                Intent intent = new Intent(MainActivity.this, ProxyControlActivity.class);
                intent.putExtra(ProxyControlActivity.EXTRA_ID, device.getBdaddr());
                startActivity(intent);

            }
        });
    }

    @Override
    public void candidateBleDevice(final BluetoothDevice device, byte[] scan_record, int rssi) {
        Log.d(Constants.TAG, "Candidate BLE Device: "+device);
        try {
            final AdvertisingPacket packet = new AdvertisingPacket(device.getAddress(),rssi, scan_record);
            Log.d(Constants.TAG, "ADV Packet: "+packet);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MeshProxy proxy = null;
                    Log.d(Constants.TAG, "Service Data: "+Utility.bytesToHex(packet.getServiceData16()));
                    if (packet.getServiceData16()[2] == 0x00) {
                        byte [] network_id = new byte[8];
                        System.arraycopy(packet.getServiceData16(),2,network_id,0,8);
                        proxy = new MeshProxy(device.getAddress(),network_id);
                    } else {
                        byte [] hash = new byte[8];
                        byte [] random = new byte[8];
                        System.arraycopy(packet.getServiceData16(),2,hash,0,8);
                        System.arraycopy(packet.getServiceData16(),11,random,0,8);
                        proxy = new MeshProxy(device.getAddress(),hash,random);
                    }
                    System.out.println("found candidate device");
                    ble_device_list_adapter.addDevice(proxy);
                    ble_device_list_adapter.notifyDataSetChanged();
                    device_count++;
                }
            });
        } catch (MalformedDataException e) {
            Log.d(Constants.TAG, "MalformedDataException parsing ADV packet");
            e.printStackTrace();
        }
    }

    @Override
    public void scanningStarted() {
        setScanState(true);
    }

    @Override
    public void scanningStopped() {
        if (toast != null) {
            toast.cancel();
        }
        setScanState(false);
    }

    private void setButtonText() {
        String text="";
        text = Constants.FIND;
        final String button_text = text;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) MainActivity.this.findViewById(R.id.scanButton)).setText(button_text);
            }
        });
    }

    private void setScanState(boolean value) {
        ble_scanning = value;
        Log.d(Constants.TAG,"Setting scan state to "+value);
        ((Button) this.findViewById(R.id.scanButton)).setText(value ? Constants.STOP_SCANNING : Constants.FIND);
    }

    private class ListAdapter extends BaseAdapter {
        private ArrayList<MeshProxy> ble_devices;

        public ListAdapter() {
            super();
            ble_devices = new ArrayList<MeshProxy>();
        }

        public void addDevice(MeshProxy device) {
            if (!ble_devices.contains(device)) {
                ble_devices.add(device);
            }
        }

        public boolean contains(MeshProxy device) {
            return ble_devices.contains(device);
        }

        public MeshProxy getDevice(int position) {
            return ble_devices.get(position);
        }

        public void clear() {
            ble_devices.clear();
        }

        @Override
        public int getCount() {
            return ble_devices.size();
        }

        @Override
        public Object getItem(int i) {
            return ble_devices.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder viewHolder;
            if (view == null) {
                view = MainActivity.this.getLayoutInflater().inflate(R.layout.list_row, null);
                viewHolder = new ViewHolder();
                viewHolder.proxy_type = (TextView) view.findViewById(R.id.txt_type);
                viewHolder.proxy_id = (TextView) view.findViewById(R.id.txt_id);
                view.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) view.getTag();
            }
            MeshProxy device = ble_devices.get(i);
            Log.d(Constants.TAG, "getView: device="+device);
            Log.d(Constants.TAG, "getView: viewHolder="+viewHolder);
            String ptype = device.getBdaddr();
            viewHolder.proxy_type.setText(ptype);
            if (device.getId_type() == 0x00) {
                viewHolder.proxy_id.setText("Type: "+Constants.PROXY_ID_TYPES[device.getId_type()]+" - Network ID: "+Utility.bytesToHex(device.getNetwork_id()));
            } else {
                viewHolder.proxy_id.setText("Type: "+Constants.PROXY_ID_TYPES[device.getId_type()]+" - Hash: "+Utility.bytesToHex(device.getProxyHash())+" Random: "+Utility.bytesToHex(device.getRandom()));
            }
            return view;
        }
    }

    public void tests() throws IOException {
        Crypto crypto = Crypto.getInstance();
        BluetoothMesh mesh = BluetoothMesh.getInstance();

        String test_name = "AES ECB mode encryption test";
        Log.d(Constants.TAG,"CCCC "+test_name);
        byte [] plain_text = Utility.hexToBytes("000102030405060708090A0B0C0D0E0F");
        byte [] key = Utility.hexToBytes("f7a2a44f8e8a8029064f173ddc1e2b00");
        byte [] encrypted = crypto.e(key,plain_text);
        Log.d(Constants.TAG,"CCCC "+test_name+" result: "+Utility.bytesToHex(encrypted).toLowerCase());
        if (!Utility.bytesToHex(encrypted).toLowerCase().equals("05d352b51c3a6c132ab3a766b7978fdd")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "K2 derivation test";
        Log.d(Constants.TAG,"CCCC "+test_name);
        byte [] N = Utility.hexToBytes("f7a2a44f8e8a8029064f173ddc1e2b00");
        byte [] P = Utility.hexToBytes("00");
        K2KeyMaterial k2 = crypto.k2(N,P);
        Log.d(Constants.TAG,"CCCC "+test_name+" result: NID="+Utility.bytesToHex(k2.NID).toLowerCase());
        Log.d(Constants.TAG,"CCCC "+test_name+" result: encryption_key="+Utility.bytesToHex(k2.encryption_key).toLowerCase());
        Log.d(Constants.TAG,"CCCC "+test_name+" result: privacy_key="+Utility.bytesToHex(k2.privacy_key).toLowerCase());
        if (!Utility.bytesToHex(k2.NID).toLowerCase().equals("7f")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        if (!Utility.bytesToHex(k2.encryption_key).toLowerCase().equals("9f589181a0f50de73c8070c7a6d27f46")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        if (!Utility.bytesToHex(k2.privacy_key).toLowerCase().equals("4c715bd4a64b938f99b453351653124f")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "K3 derivation test";
        Log.d(Constants.TAG,"CCCC "+test_name);
        N = Utility.hexToBytes("f7a2a44f8e8a8029064f173ddc1e2b00");
        byte [] k3 = crypto.k3(N);
        Log.d(Constants.TAG,"CCCC "+test_name+" result: k3="+Utility.bytesToHex(k3).toLowerCase());
        if (!Utility.bytesToHex(k3).toLowerCase().equals("ff046958233db014")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "K4 derivation test";
        Log.d(Constants.TAG,"CCCC "+test_name);
        byte [] appkey = Utility.hexToBytes("3216d1509884b533248541792b877f98");
        byte AID = crypto.k4(appkey);
        Log.d(Constants.TAG,"CCCC "+test_name+" result: AID="+Integer.toString(AID,16).toLowerCase());
        if (!Integer.toString(AID,16).toLowerCase().equals("38")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "Obfuscation";
        Log.d(Constants.TAG,"CCCC "+test_name);
        byte [] netkey = Utility.hexToBytes("7dd7364cd842ad18c17c2b820c84c3d6");
        byte [] nonce = Utility.hexToBytes("000307080a1234000012345677");
        byte [] iv_index = Utility.hexToBytes("12345677");
        byte ctl = 0x00;
        byte ttl = 0x03;
        byte [] seq = Utility.hexToBytes("07080a");
        byte [] src = Utility.hexToBytes("1234");
        byte [] dst = Utility.hexToBytes("8105");
        byte [] transport_pdu = Utility.hexToBytes("662fa730fd98f6e4bd120ea9d6");
        P = Utility.hexToBytes("00");
        k2 = crypto.k2(netkey,P);
        AuthEncNetwork auth_enc_network = mesh.meshAuthEncNetwork(k2.encryption_key, nonce, dst, transport_pdu);
        Log.d(Constants.TAG,"CCCC "+test_name+" privacy_key="+Utility.bytesToHex(k2.privacy_key).toLowerCase());
        byte [] obfuscated = crypto.obfuscate(auth_enc_network.enc_dst, auth_enc_network.enc_transport_pdu, auth_enc_network.netMIC, ctl, ttl, seq, src, iv_index, k2.privacy_key);
        Log.d(Constants.TAG,"CCCC "+test_name+" result: obfuscated="+Utility.bytesToHex(obfuscated).toLowerCase());
        if (!Utility.bytesToHex(obfuscated).toLowerCase().equals("b1051f5e945a")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "Network Layer";
        Log.d(Constants.TAG,"CCCC "+test_name);
        P = Utility.hexToBytes("00");
        k2 = crypto.k2(Utility.hexToBytes("7dd7364cd842ad18c17c2b820c84c3d6"),P);
        auth_enc_network = mesh.meshAuthEncNetwork(k2.encryption_key, Utility.hexToBytes("000307080a1234000012345677"), Utility.hexToBytes("8105"), Utility.hexToBytes("662fa730fd98f6e4bd120ea9d6"));
        Log.d(Constants.TAG,"CCCC "+test_name+" result: network layer="+auth_enc_network.toString());
        if (!Utility.bytesToHex(auth_enc_network.enc_dst).toLowerCase().equals("e4d6")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED (enc_dst)");
            throw new AssertionError(test_name+" failed");
        }
        if (!Utility.bytesToHex(auth_enc_network.enc_transport_pdu).toLowerCase().equals("11358eaf17796a6c98977f69e5")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED (enc_transport_pdu)");
            throw new AssertionError(test_name+" failed");
        }
        if (!Utility.bytesToHex(auth_enc_network.netMIC).toLowerCase().equals("872c4620")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED (netMIC)");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "Access Layer";
        Log.d(Constants.TAG,"CCCC "+test_name);
        appkey = Utility.hexToBytes("63964771734fbd76e3b40519d1d94a48");
        nonce = Utility.hexToBytes("010007080a1234810512345677");
        byte [] plain_access_payload = Utility.hexToBytes("d50a0048656c6c6f");
        EncAccessPayloadTransMic upper_trans_pdu = mesh.meshAuthEncAccessPayload(appkey, nonce, plain_access_payload);
        Log.d(Constants.TAG,"CCCC "+test_name+" result: access layer="+upper_trans_pdu.toString());
        if (!Utility.bytesToHex(upper_trans_pdu.enc_access_payload).toLowerCase().equals("2fa730fd98f6e4bd")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED (enc_access_payload)");
            throw new AssertionError(test_name+" failed");
        }
        if (!Utility.bytesToHex(upper_trans_pdu.transMIC).toLowerCase().equals("120ea9d6")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED (upper_trans_pdu)");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "16 bit LE array #1";
        Log.d(Constants.TAG,"CCCC "+test_name);
        int num = 1;
        byte [] result = Utility.uint16ToUint8ArrayLE(num);
        Log.d(Constants.TAG,"CCCC "+test_name+" result="+Utility.bytesToHex(result).toLowerCase());
        if (!Utility.bytesToHex(result).toLowerCase().equals("0100")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "16 bit LE array #2";
        Log.d(Constants.TAG,"CCCC "+test_name);
        num = 65520;
        result = Utility.uint16ToUint8ArrayLE(num);
        Log.d(Constants.TAG,"CCCC "+test_name+" result="+Utility.bytesToHex(result).toLowerCase());
        if (!Utility.bytesToHex(result).toLowerCase().equals("f0ff")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "16 bit LE array #3";
        Log.d(Constants.TAG,"CCCC "+test_name);
        num = 65534;
        result = Utility.uint16ToUint8ArrayLE(num);
        Log.d(Constants.TAG,"CCCC "+test_name+" result="+Utility.bytesToHex(result).toLowerCase());
        if (!Utility.bytesToHex(result).toLowerCase().equals("feff")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "16 bit BE array #1";
        Log.d(Constants.TAG,"CCCC "+test_name);
        num = 1;
        result = Utility.uint16ToUint8ArrayBE(num);
        Log.d(Constants.TAG,"CCCC "+test_name+" result="+Utility.bytesToHex(result).toLowerCase());
        if (!Utility.bytesToHex(result).toLowerCase().equals("0001")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "16 bit BE array #2";
        Log.d(Constants.TAG,"CCCC "+test_name);
        num = 65520;
        result = Utility.uint16ToUint8ArrayBE(num);
        Log.d(Constants.TAG,"CCCC "+test_name+" result="+Utility.bytesToHex(result).toLowerCase());
        if (!Utility.bytesToHex(result).toLowerCase().equals("fff0")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

        test_name = "16 bit BE array #3";
        Log.d(Constants.TAG,"CCCC "+test_name);
        num = 65534;
        result = Utility.uint16ToUint8ArrayBE(num);
        Log.d(Constants.TAG,"CCCC "+test_name+" result="+Utility.bytesToHex(result).toLowerCase());
        if (!Utility.bytesToHex(result).toLowerCase().equals("fffe")) {
            Log.e(Constants.TAG,"CCCC "+test_name+" - FAILED");
            throw new AssertionError(test_name+" failed");
        }
        Log.d(Constants.TAG,"CCCC "+test_name+" - PASSED");
        Log.d(Constants.TAG,"CCCC ");

    }

    public void onScan(View view) {

//        try {
//            tests();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        if (!ble_scanner.isScanning()) {
            Log.d(Constants.TAG, "Not currently scanning");
            device_count=0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissions_granted = false;
                    requestLocationPermission();
                } else {
                    Log.i(Constants.TAG, "Location permission has already been granted. Starting scanning.");
                    permissions_granted = true;
                }
            } else {
                // the ACCESS_FINE_LOCATION permission did not exist before M so....
                permissions_granted = true;
            }
            startScanning();
        } else {
            Log.d(Constants.TAG, "Already scanning");
            ble_scanner.stopScanning();
        }
    }

    private void startScanning() {
        if (permissions_granted) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ble_device_list_adapter.clear();
                    ble_device_list_adapter.notifyDataSetChanged();
                }
            });
            simpleToast(Constants.SCANNING,2000);
            ble_scanner.startScanning(this, SCAN_TIMEOUT);
        } else {
            Log.i(Constants.TAG, "Permission to perform Bluetooth scanning was not yet granted");
        }
    }

    private void requestLocationPermission() {
        Log.i(Constants.TAG, "Location permission has NOT yet been granted. Requesting permission.");
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)){
            Log.i(Constants.TAG, "Displaying location permission rationale to provide additional context.");
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Permission Required");
            builder.setMessage("Please grant Location access so this application can perform Bluetooth scanning");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    Log.d(Constants.TAG, "Requesting permissions after explanation");
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
                }
            });
            builder.show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            Log.i(Constants.TAG, "Received response for location permission request.");
            // Check if the only required permission has been granted
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Location permission has been granted
                Log.i(Constants.TAG, "Location permission has now been granted. Scanning.....");
                permissions_granted = true;
                if (ble_scanner.isScanning()) {
                    startScanning();
                }
            }else{
                Log.i(Constants.TAG, "Location permission was NOT granted.");
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void simpleToast(String message, int duration) {
        toast = Toast.makeText(this, message, duration);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
