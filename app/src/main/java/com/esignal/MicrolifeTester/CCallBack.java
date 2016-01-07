package com.esignal.MicrolifeTester;

import android.bluetooth.BluetoothGattCharacteristic;


public interface CCallBack
{
    void callBack(BluetoothGattCharacteristic characteristic, int status);

}
