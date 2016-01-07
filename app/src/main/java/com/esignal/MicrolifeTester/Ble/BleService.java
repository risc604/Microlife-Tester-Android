package com.esignal.MicrolifeTester.Ble;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class BleService extends Service implements BleServiceListener
{
    private final static String TAG = BleService.class.getSimpleName();

    private final static String INTENT_PREFIX = BleService.class.getPackage().getName();
    public final static String ACTION_GATT_CONNECTED = INTENT_PREFIX + ".ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = INTENT_PREFIX + ".ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = INTENT_PREFIX + ".ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE = INTENT_PREFIX + ".ACTION_DATA_AVAILABLE";
    public final static String EXTRA_SERVICE_UUID = INTENT_PREFIX + ".EXTRA_SERVICE_UUID";
    public final static String EXTRA_CHARACTERISTIC_UUID = INTENT_PREFIX + ".EXTRA_CHARACTERISTIC_UUI";
    public final static String EXTRA_DATA = INTENT_PREFIX + ".EXTRA_DATA";
    public final static String EXTRA_TEXT = INTENT_PREFIX + ".EXTRA_TEXT";
    private final IBinder binder = new LocalBinder();
    private final Handler uiThreadHandler = new Handler(Looper.getMainLooper());
    private final BleManager bleManager = new BleManager();

    private BleServiceListener serviceListener;

    public BleService()
    { }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.i(TAG, "Service onCreate");
        bleManager.setServiceListener(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        bleManager.close();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        bleManager.disconnect();
        bleManager.close();
    }

    public BleManager getBleManager()
    {
        return bleManager;
    }

    public void setServiceListener(BleServiceListener listener)
    {
        serviceListener = listener;
    }

    @Override
    public void onConnected()
    {
        broadcastUpdate(ACTION_GATT_CONNECTED);
        uiThreadHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                //  if (serviceListener != null)
                //    serviceListener.onConnected();
            }
        });
    }

    @Override
    public void onDisconnected()
    {
        broadcastUpdate(ACTION_GATT_DISCONNECTED);
        uiThreadHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                //  if (serviceListener != null)
                //    serviceListener.onDisconnected();
            }
        });
    }

    @Override
    public void onServiceDiscovered()
    {
        broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

        uiThreadHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                //if (serviceListener != null)
                //  serviceListener.onServiceDiscovered();
            }
        });

    }

    @Override
    public void onDataAvailable(final String serviceUUid, final String characteristicUUid, final String text, final byte[] data)
    {
        final Intent intent = new Intent(ACTION_DATA_AVAILABLE);
        intent.putExtra(EXTRA_SERVICE_UUID, serviceUUid);
        intent.putExtra(EXTRA_CHARACTERISTIC_UUID, characteristicUUid);
        intent.putExtra(EXTRA_TEXT, text);
        intent.putExtra(EXTRA_DATA, data);

        // Stop sending date to broadcast
//        sendBroadcast(intent);
        uiThreadHandler.post(new Runnable()
        {
            @Override
            public void run()
            {
                if (serviceListener != null)
                    serviceListener.onDataAvailable(serviceUUid, characteristicUUid, text, data);
            }
        });
    }

    private void broadcastUpdate(final String action)
    {
//        if (!AppConfig.REMOTE_BLE_SERVICE)
//            return;

        final Intent intent = new Intent(action);
        // Stop to notify broadcast
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder
    {
        public BleService getService()
        {
            return BleService.this;
        }
    }
}
