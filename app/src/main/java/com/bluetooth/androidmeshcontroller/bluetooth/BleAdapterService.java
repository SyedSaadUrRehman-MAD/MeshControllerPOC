package com.bluetooth.androidmeshcontroller.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.bluetooth.androidmeshcontroller.Constants;
import com.bluetooth.androidmeshcontroller.Utility;

import java.util.List;
import java.util.UUID;

import static com.bluetooth.androidmeshcontroller.Constants.TAG;

public class BleAdapterService extends Service {

    private BluetoothAdapter bluetooth_adapter;
    private BluetoothGatt bluetooth_gatt;
    private BluetoothManager bluetooth_manager;
    private Handler activity_handler = null;
    private BluetoothDevice device;
    private BluetoothGattDescriptor descriptor;
    private final IBinder binder = new LocalBinder();

    public boolean isConnected() {
        return connected;
    }

    private boolean connected = false;

    // messages sent back to activity
    public static final int GATT_CONNECTED = 1;
    public static final int GATT_DISCONNECT = 2;
    public static final int GATT_SERVICES_DISCOVERED = 3;
    public static final int GATT_CHARACTERISTIC_READ = 4;
    public static final int GATT_CHARACTERISTIC_WRITTEN = 5;
    public static final int GATT_REMOTE_RSSI = 6;
    public static final int MESSAGE = 7;
    public static final int NOTIFICATION_OR_INDICATION_RECEIVED = 8;
    public static final int MTU_CHANGED = 9;

    // message parms
    public static final String PARCEL_DESCRIPTOR_UUID = "DESCRIPTOR_UUID";
    public static final String PARCEL_CHARACTERISTIC_UUID = "CHARACTERISTIC_UUID";
    public static final String PARCEL_SERVICE_UUID = "SERVICE_UUID";
    public static final String PARCEL_VALUE = "VALUE";
    public static final String PARCEL_RSSI = "RSSI";
    public static final String PARCEL_TEXT = "TEXT";

    // service uuids
    public static String MESH_PROXY_SERVICE_UUID = "00001828-0000-1000-8000-00805F9B34FB";

    // service characteristics
    public static String MESH_PROXY_DATA_IN = "00002ADD-0000-1000-8000-00805F9B34FB";
    public static String MESH_PROXY_DATA_OUT = "00002ADE-0000-1000-8000-00805F9B34FB";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805F9B34FB";

    public void enableDataOutNotificationAndIndication() {
        if(bluetooth_gatt != null)
        {
            final BluetoothGattService meshProxyService = bluetooth_gatt.getService(UUID.fromString(MESH_PROXY_SERVICE_UUID));
            if (meshProxyService != null) {
                BluetoothGattCharacteristic mMeshProxyDataOutCharacteristic = meshProxyService.getCharacteristic(UUID.fromString(MESH_PROXY_DATA_OUT));
                internalEnableIndications(bluetooth_gatt, mMeshProxyDataOutCharacteristic);
                setNotificationState(MESH_PROXY_SERVICE_UUID,MESH_PROXY_DATA_OUT,true);
            }else
                Log.d(TAG,"meshProxyService is null");
        }else
            Log.d(TAG,"Bluetooth_gatt is null");
    }

    private boolean internalEnableIndications(final BluetoothGatt mBluetoothGatt, final BluetoothGattCharacteristic characteristic) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || characteristic == null)
            return false;

        final BluetoothGattDescriptor descriptor = getCccd(characteristic, BluetoothGattCharacteristic.PROPERTY_INDICATE);
        if (descriptor != null) {
            Log.d(TAG, "gatt.setCharacteristicNotification(" + characteristic.getUuid() + ", true)");
            gatt.setCharacteristicNotification(characteristic, true);

            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            Log.d(TAG, "Enabling indications for " + characteristic.getUuid());
            Log.d(TAG, "gatt.writeDescriptor(" + UUID.fromString(BleAdapterService.CLIENT_CHARACTERISTIC_CONFIG) + ", value=0x02-00)");
            return internalWriteDescriptorWorkaround(gatt,descriptor);
        }
        return false;
    }

    public boolean setNotificationState(String serviceUuid, String characteristicUuid, boolean enabled) {

        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("setIndicationsState: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt.getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("setIndicationsState: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService.getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("setIndicationsState: gattChar null");
            return false;
        }

        bluetooth_gatt.setCharacteristicNotification(gattChar, enabled);
        // Enable remote notifications
        descriptor = gattChar.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTIC_CONFIG));
        if(descriptor != null) {
            if (enabled) {
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            } else {
                descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
            }
            boolean ok = bluetooth_gatt.writeDescriptor(descriptor);

            return ok;
        }
        else return false;
    }

    private boolean internalWriteDescriptorWorkaround(final BluetoothGatt mBluetoothGatt,final BluetoothGattDescriptor descriptor) {
        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null || descriptor == null)
            return false;

        final BluetoothGattCharacteristic parentCharacteristic = descriptor.getCharacteristic();
        final int originalWriteType = parentCharacteristic.getWriteType();
        parentCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
        final boolean result = gatt.writeDescriptor(descriptor);
        parentCharacteristic.setWriteType(originalWriteType);
        return result;
    }
    private static BluetoothGattDescriptor getCccd(@Nullable final BluetoothGattCharacteristic characteristic,
                                                   final int requiredProperty) {
        if (characteristic == null)
            return null;

        // Check characteristic property
        final int properties = characteristic.getProperties();
        if ((properties & requiredProperty) == 0)
            return null;

        return characteristic.getDescriptor(UUID.fromString(BleAdapterService.CLIENT_CHARACTERISTIC_CONFIG));
    }

    public class LocalBinder extends Binder {
        public BleAdapterService getService() {
            return BleAdapterService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    // set activity the will receive the messages
    public void setActivityHandler(Handler handler) {
        activity_handler = handler;
    }

    private void sendConsoleMessage(String text) {
        Message msg = Message.obtain(activity_handler, MESSAGE);
        Bundle data = new Bundle();
        data.putString(PARCEL_TEXT, text);
        msg.setData(data);
        msg.sendToTarget();
    }

    @Override
    public void onCreate() {
        if (bluetooth_manager == null) {
            bluetooth_manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (bluetooth_manager == null) {
                return;
            }
        }
        bluetooth_adapter = bluetooth_manager.getAdapter();
        if (bluetooth_adapter == null) {
            return;
        }
    }

    // connect to the device
    public boolean connect(final String address) {

        if (bluetooth_adapter == null || address == null) {
            sendConsoleMessage("connect: bluetooth_adapter=null");
            return false;
        }

        device = bluetooth_adapter.getRemoteDevice(address);
        if (device == null) {
            sendConsoleMessage("connect: device=null");
            return false;
        }

        bluetooth_gatt = device.connectGatt(this, false, gatt_callback);
        return true;
    }

    // disconnect from device
    public void disconnect() {
        sendConsoleMessage("disconnecting");
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("disconnect: bluetooth_adapter|bluetooth_gatt null");
            return;
        }
        if (bluetooth_gatt != null) {
            bluetooth_gatt.disconnect();
        }
    }

    public void readRemoteRssi() {
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            return;
        }
        bluetooth_gatt.readRemoteRssi();
    }

    public void discoverServices() {
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            return;
        }
        Log.d(TAG, "Discovering GATT services");
        bluetooth_gatt.discoverServices();
    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (bluetooth_gatt == null)
            return null;
        return bluetooth_gatt.getServices();
    }

    public boolean readCharacteristic(String serviceUuid,
                                      String characteristicUuid) {
        Log.d(TAG, "readCharacteristic:" + characteristicUuid + " of " + serviceUuid);
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("readCharacteristic: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt
                .getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("readCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService
                .getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("readCharacteristic: gattChar null");
            return false;
        }
        return bluetooth_gatt.readCharacteristic(gattChar);
    }

    public boolean writeCharacteristic(String serviceUuid,
                                       String characteristicUuid, byte[] value) {

        Log.d(TAG, "TTTT writeCharacteristic:" + characteristicUuid + " of " + serviceUuid + " bytes: " + Utility.bytesToHex(value));
        if (bluetooth_adapter == null || bluetooth_gatt == null) {
            sendConsoleMessage("writeCharacteristic: bluetooth_adapter|bluetooth_gatt null");
            return false;
        }

        BluetoothGattService gattService = bluetooth_gatt
                .getService(java.util.UUID.fromString(serviceUuid));
        if (gattService == null) {
            sendConsoleMessage("writeCharacteristic: gattService null");
            return false;
        }
        BluetoothGattCharacteristic gattChar = gattService
                .getCharacteristic(java.util.UUID.fromString(characteristicUuid));
        if (gattChar == null) {
            sendConsoleMessage("writeCharacteristic: gattChar null");
            return false;
        }
        gattChar.setValue(value);

        return bluetooth_gatt.writeCharacteristic(gattChar);

    }

    public boolean requestMtu(int mtu) {
        return bluetooth_gatt.requestMtu(mtu);
    }

    private final BluetoothGattCallback gatt_callback = new BluetoothGattCallback() {

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                            int newState) {
            Log.d(TAG, "onConnectionStateChange: status=" + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "onConnectionStateChange: CONNECTED");
                connected = true;
                Message msg = Message.obtain(activity_handler, GATT_CONNECTED);
                msg.sendToTarget();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "onConnectionStateChange: DISCONNECTED");
                Message msg = Message.obtain(activity_handler, GATT_DISCONNECT);
                msg.sendToTarget();
                if (bluetooth_gatt != null) {
                    Log.d(TAG, "Closing and destroying BluetoothGatt object");
                    connected = false;
                    bluetooth_gatt.close();
                    bluetooth_gatt = null;
                }
            }
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendConsoleMessage("RSSI read OK");
                Bundle bundle = new Bundle();
                bundle.putInt(PARCEL_RSSI, rssi);
                Message msg = Message
                        .obtain(activity_handler, GATT_REMOTE_RSSI);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("RSSI read err:" + status);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            sendConsoleMessage("Services Discovered");
            Message msg = Message.obtain(activity_handler,
                    GATT_SERVICES_DISCOVERED);
            msg.sendToTarget();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicRead " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid()
                        .toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler,
                        GATT_CHARACTERISTIC_READ);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                Log.d(TAG, "failed to read characteristic:" + characteristic.getUuid().toString() + " of service " + characteristic.getService().getUuid().toString() + " : status=" + status);
                sendConsoleMessage("characteristic read err:" + status);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            Log.d("acm","onCharacteristicChanged");
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(),"On CHarr changed",Toast.LENGTH_LONG).show();
                }
            });
            Bundle bundle = new Bundle();
            bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
            bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
            bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
            // notifications and indications are both communicated from here in this way
            Message msg = Message.obtain(activity_handler, NOTIFICATION_OR_INDICATION_RECEIVED);
            msg.setData(bundle);
            msg.sendToTarget();
        }

        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "onCharacteristicWrite");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putString(PARCEL_CHARACTERISTIC_UUID, characteristic.getUuid().toString());
                bundle.putString(PARCEL_SERVICE_UUID, characteristic.getService().getUuid().toString());
                bundle.putByteArray(PARCEL_VALUE, characteristic.getValue());
                Message msg = Message.obtain(activity_handler, GATT_CHARACTERISTIC_WRITTEN);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("characteristic write err:" + status);
            }
        }

        public void onMtuChanged(BluetoothGatt gatt,
                                 int mtu,
                                 int status) {
            Log.d(TAG, "onCharacteristicWrite");
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Bundle bundle = new Bundle();
                bundle.putInt(PARCEL_VALUE, mtu);
                Message msg = Message.obtain(activity_handler, MTU_CHANGED);
                msg.setData(bundle);
                msg.sendToTarget();
            } else {
                sendConsoleMessage("MTU change err:" + status);
            }
        }
    };
}
