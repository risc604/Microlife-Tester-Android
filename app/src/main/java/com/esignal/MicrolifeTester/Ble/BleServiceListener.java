package com.esignal.MicrolifeTester.Ble;

public interface BleServiceListener
{

    /**
     * <b>This method is called on separate from Main thread.</b>
     */
    public void onConnected();

    /**
     * <b>This method is called on separate from Main thread.</b>
     */
    public void onDisconnected();

    /**
     * <b>This method is called on separate from Main thread.</b>
     */
    public void onServiceDiscovered();

    /**
     * <b>This method is called on separate from Main thread.</b>
     */
    public void onDataAvailable(String serviceUUid, String characteristicUUid, String text, byte[] data);
}
