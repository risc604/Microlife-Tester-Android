package com.esignal.MicrolifeTester.Ble;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/**
 * Implements callback methods for GATT events that the app cares about.
 * For example, connection change and services discovered.
 */
public interface BleExecutorListener
{
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState);

    public void onServicesDiscovered(BluetoothGatt gatt, int status);

    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status);

    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic characteristic,
                                      int status);

    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic);

    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);

    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status);
}
