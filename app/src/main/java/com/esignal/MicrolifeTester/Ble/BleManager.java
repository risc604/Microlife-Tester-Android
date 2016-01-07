package com.esignal.MicrolifeTester.Ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.esignal.MicrolifeTester.CCallBack;
import com.esignal.MicrolifeTester.CallBack;
import com.esignal.MicrolifeTester.Utils;

import java.util.List;
import java.util.UUID;

public class BleManager implements BleExecutorListener
{
    private final static String TAG = BleManager.class.getSimpleName();

    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    private static UUID CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BleGattExecutor executor = BleUtils.createExecutor(this);
    private BluetoothAdapter adapter;
    private BluetoothGatt gatt;

    private String deviceAddress;
    public static int connectionState = STATE_DISCONNECTED;
    private BleServiceListener serviceListener;

    private CallBack rCallBack;
    private CallBack wCallBack;
    private CCallBack cCallBack;

    public int getState()
    {
        return connectionState;
    }
    public String getConnectedDeviceAddress()
    {
        return deviceAddress;
    }
    public void setServiceListener(BleServiceListener listener)
    {
        serviceListener = listener;
    }

    public boolean initialize(Context context)
    {
        if (adapter == null)
        {
            adapter = BleUtils.getBluetoothAdapter(context);
        }

        if (adapter == null || !adapter.isEnabled())
        {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    public boolean connect(Context context, String address)
    {
        if (adapter == null || address == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (deviceAddress != null && address.equals(deviceAddress) && gatt != null)
        {
            Log.d(TAG, "Trying to use an existing BluetoothGatt for connection.");
            if (gatt.connect())
            {
                connectionState = STATE_CONNECTING;
                return true;
            }
            else
            {
                return false;
            }
        }

        final BluetoothDevice device = adapter.getRemoteDevice(address);
        if (device == null)
        {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        gatt = device.connectGatt(context, true, executor);
        Log.d(TAG, "Trying to create a new connection.");
        deviceAddress = address;
        connectionState = STATE_CONNECTING;

        return true;
    }

    public void disconnect()
    {
        if (adapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        gatt.disconnect();
    }

    public void close()
    {
        if (gatt == null)
        {
            return;
        }
        gatt.close();
        gatt = null;
    }

    public List<BluetoothGattService> getSupportedGattServices()
    {
        if (gatt == null)
            return null;

        return gatt.getServices();
    }

    public BluetoothGattService getSupportedGattService(UUID uuid)
    {
        if (gatt == null)
            return null;

        return gatt.getService(uuid);
    }

    public boolean readDescriptor(BluetoothGattDescriptor descriptor, CallBack callBack)
    {
        if (adapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        rCallBack = callBack;
        return gatt.readDescriptor(descriptor);
    }

    public boolean writeDescriptor(BluetoothGattDescriptor descriptor)
    {
        if (adapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        return gatt.writeDescriptor(descriptor);
    }

    public boolean readCharacteristic(BluetoothGattCharacteristic characteristic, CCallBack callBack)
    {
        if (adapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        cCallBack = callBack;
        return gatt.readCharacteristic(characteristic);
    }

    public boolean writeCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (adapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }
        //gatt.beginReliableWrite();
        return gatt.writeCharacteristic(characteristic);
    }

    public boolean notifyCharacteristic(BluetoothGattCharacteristic characteristic, boolean start, CallBack callBack)
    {
        if (adapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        // enable/disable locally
        gatt.setCharacteristicNotification(characteristic, start);

        wCallBack = callBack;
        final BluetoothGattDescriptor config = characteristic.getDescriptor(CHARACTERISTIC_CONFIG);
        if (config == null)
            return false;

        // enable/disable remotely
        config.setValue(start ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        return gatt.writeDescriptor(config);
    }

    public boolean indicateCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        if (adapter == null || gatt == null)
        {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return false;
        }

        // enable/disable locally
        gatt.setCharacteristicNotification(characteristic, true);

        final BluetoothGattDescriptor config = characteristic.getDescriptor(CHARACTERISTIC_CONFIG);
        if (config == null)
            return false;

        // enable/disable remotely
        config.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        return gatt.writeDescriptor(config);
    }

    public boolean beginReliableWrite()
    {
        return gatt.beginReliableWrite();
    }

    public boolean executeReliableWrite()
    {
        return gatt.executeReliableWrite();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public void abortReliableWrite()
    {
        gatt.abortReliableWrite();
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
    {
        if (newState == BluetoothProfile.STATE_CONNECTED)
        {

            if (serviceListener != null)
                serviceListener.onConnected();

            connectionState = STATE_CONNECTED;
            Log.i(TAG, "Connected to GATT server.");
            // Attempts to discover services after successful connection.
            Log.i(TAG, "Attempting to start service discovery: " + gatt.discoverServices());
        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED)
        {
            connectionState = STATE_DISCONNECTED;
            Log.i(TAG, "Disconnected from GATT server.");

            if (serviceListener != null)
                serviceListener.onDisconnected();
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status)
    {
        if (status == BluetoothGatt.GATT_SUCCESS)
        {
            if (serviceListener != null)
                serviceListener.onServiceDiscovered();
        }
        else
        {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
        Log.i(TAG, "onCharacteristicRead");
        Log.i(TAG, characteristic.getStringValue(0));

        if (status != BluetoothGatt.GATT_SUCCESS)
            return;
        broadcastUpdate(characteristic);  ///
        if (cCallBack != null)
        {
            cCallBack.callBack(characteristic, status);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status)
    {
//        broadcastUpdate(characteristic);
        final byte[] data = characteristic.getValue();
        String text = Utils.byteArray2String(data);

        Log.i(TAG, "onCharacteristicWrite status " + status + text);
        if (status != BluetoothGatt.GATT_SUCCESS)
            return;
        // Stop updating data in broadcast when response is onCharacteristicWrite
       // broadcastUpdate(characteristic);		///
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic)
    {
        broadcastUpdate(characteristic);
        //Log.i(TAG, "onCharacteristicChanged");
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
    {
        Log.i(TAG, "onDescriptorRead " + status);
//        Log.i(TAG, Utils.byteArray2String(descriptor.getValue()));
        if (rCallBack != null)
        {
            rCallBack.callBack(descriptor, status);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status)
    {
        Log.i(TAG, "onDescriptorWrite " + status);
        if (wCallBack != null)
        {
            wCallBack.callBack(descriptor, status);
        }
    }

    private void broadcastUpdate(BluetoothGattCharacteristic characteristic)
    {
        final String serviceUuid = characteristic.getService().getUuid().toString();
        final String characteristicUuid = characteristic.getUuid().toString();
        final byte[] data = characteristic.getValue();
        String text = Utils.byteArray2String(data);

        Log.i(TAG, "DataUpdate ");
        if (serviceListener != null)
            serviceListener.onDataAvailable(serviceUuid, characteristicUuid, text, data);
    }
}
