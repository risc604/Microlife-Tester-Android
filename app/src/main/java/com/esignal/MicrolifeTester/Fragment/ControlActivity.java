package com.esignal.MicrolifeTester.Fragment;

import android.app.Activity;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.esignal.MicrolifeTester.Ble.BleActionsReceiver;
import com.esignal.MicrolifeTester.Ble.BleService;
import com.esignal.MicrolifeTester.Ble.BleServiceListener;
import com.esignal.MicrolifeTester.CCallBack;
import com.esignal.MicrolifeTester.CallBack;
import com.esignal.MicrolifeTester.R;
import com.esignal.MicrolifeTester.Utils;

import java.io.File;
import java.util.Calendar;
import java.util.UUID;

import static java.lang.String.format;


public class ControlActivity extends Fragment implements BleServiceListener, ServiceConnection
{
    private final static String TAG = ControlActivity.class.getSimpleName();

    public final static String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public final static String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    public final static String EXTRAS_DEVICE_RSSI = "DEVICE_RSSI";

    private String deviceName;
    private String deviceAddress;
    private String deviceRSSI;
    private BleService bleService;

    /* private String password;*/
    private BroadcastReceiver bleActionsReceiver = new BleActionsReceiver(this);

    private PowerManager.WakeLock wl;

    private BluetoothGattCharacteristic mNotifyChar;
    private BluetoothGattCharacteristic mWriteChar;

    private boolean fReadSus = false;
    private boolean isServiceDiscover = false;
    private boolean readDataFlag = false;

    private String      mStringBuffer;
    private ImageView   mConnectionView;
    private TextView    mDataText;
    private TextView    mCheckText;
    private ScrollView  mScroller;
    private File        dirPath;

    private Button mTestButton;
    private final static UUID UUID_Notify = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    private final static UUID UUID_Write = UUID.fromString("0000fff2-0000-1000-8000-00805f9b34fb");

    public ControlActivity()
    {
        // Required empty public constructor
    }

    public static ControlActivity newInstance(String param1, String param2, String param3)
    {
        ControlActivity fragment = new ControlActivity();
        Bundle args = new Bundle();
        args.putString(EXTRAS_DEVICE_NAME, param1);
        args.putString(EXTRAS_DEVICE_ADDRESS, param2);
        args.putString(EXTRAS_DEVICE_RSSI, param3);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        if (getArguments() != null)
        {
            deviceName = getArguments().getString(EXTRAS_DEVICE_NAME);
            deviceAddress = getArguments().getString(EXTRAS_DEVICE_ADDRESS);
            deviceRSSI = getArguments().getString(EXTRAS_DEVICE_RSSI);
        }

        PowerManager pm = (PowerManager) getActivity().getSystemService(getActivity().POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, TAG);
        wl.acquire();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.activity_control, container, false);

        mConnectionView = (ImageView) view.findViewById(R.id.connectionView);
        mDataText = (TextView) view.findViewById(R.id.DataText);
        mDataText.setText("");
        mScroller = (ScrollView) view.findViewById(R.id.Scroller);
        mCheckText = (TextView) view.findViewById(R.id.textView3);
        mTestButton=(Button)view.findViewById(R.id.button);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        mDataText.setText("");
       // logFile =
        mTestButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //mDataText.setText("");
                if (mWriteChar != null) SendCMD(0x00);
                // if (mWriteChar != null) SendCMD(0x04);
                //SendCMD();

            }
        });
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        getActivity().registerReceiver(bleActionsReceiver, BleActionsReceiver.createIntentFilter());
        final Intent gattServiceIntent = new Intent(getActivity(), BleService.class);
        getActivity().bindService(gattServiceIntent, this, getActivity().BIND_AUTO_CREATE);
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (bleService != null)
            bleService.getBleManager().disconnect();
        getActivity().unregisterReceiver(bleActionsReceiver);
        getActivity().unbindService(this);
        wl.release();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        // inflater.inflate(R.menu.menu_update, menu);
        // super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu)
    {
        super.onPrepareOptionsMenu(menu);
    }

    public String getDeviceName()
    {
        return deviceName;
    }
    public String getDeviceAddress()
    {
        return deviceAddress;
    }
    public BleService getBleService()
    {
        return bleService;
    }

    @Override
    public void onConnected()
    {
        Log.i(TAG, "bleDevice connected");
        // InsertText("Deivce Connected ");

        mConnectionView.setImageResource(R.drawable.bleon);
    }

    @Override
    public void onDisconnected()
    {
        Log.i(TAG, "bleDevice disconnected");

        //  InsertText("Deivce DisConnected ");
        mConnectionView.setImageResource(R.drawable.bleon);
    }

    @Override
    public void onServiceDiscovered()
    {
        isServiceDiscover = true;
        mStringBuffer = "";

        for (int i = 0; i < bleService.getBleManager().getSupportedGattServices().size(); i++)
        {
            final BluetoothGattService service = bleService.getBleManager().getSupportedGattServices().get(i);
            int mCount = service.getCharacteristics().size();

            for (int x = 0; x < mCount; x++)
            {
                final BluetoothGattCharacteristic dataChar = service.getCharacteristics().get(x);
                final int charaProp = dataChar.getProperties();

                //InsertText("SD ID: " + service.getUuid());

                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0)
                {
                    if (dataChar.getUuid().equals(UUID_Notify))
                    {
                        mNotifyChar = dataChar;
                    }
                }

                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0)
                {
                    if (dataChar.getUuid().equals(UUID_Write))
                    {
                        mWriteChar = dataChar;
                    }
                }

                if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0)
                {
                        Log.i(TAG, "Read From: " + dataChar.getUuid().toString());
                    fReadSus = false;
                    bleService.getBleManager().readCharacteristic(dataChar, new CCallBack()
                    {
                        @Override
                        public void callBack(BluetoothGattCharacteristic characteristic, int status)
                        {
                            if (status == 0)
                            {
                                final byte[] data = characteristic.getValue();
                                final StringBuilder stringBuilder = new StringBuilder(data.length);
                                ///String ASCII = new String(data);
                                ///String text = Utils.byteArray2String(data);
                                ///for (byte byteChar : data)
                                ///{
                                ///    stringBuilder.append(format("%02X", byteChar));
                                ///}
                                ///
                                ///if ("00002a25-0000-1000-8000-00805f9b34fb".equals(dataChar.getUuid().toString()))
                                ///{
                                ///    mStringBuffer += "Serial Number: " + stringBuilder.toString() + "\r\n";
                                ///}
                                ///else if ("00002a29-0000-1000-8000-00805f9b34fb".equals(dataChar.getUuid().toString()))
                                ///{
                                ///    mStringBuffer += "Manafacture: " + ASCII + "\r\n";
                                ///}
                                ///else if ("00002a24-0000-1000-8000-00805f9b34fb".equals(dataChar.getUuid().toString()))
                                ///{
                                ///    mStringBuffer += "Modle Number: " + ASCII + "\r\n";
                                ///}
                                ///else if ("00002a27-0000-1000-8000-00805f9b34fb".equals(dataChar.getUuid().toString()))
                                ///{
                                ///    mStringBuffer += "Hardware Revision: " + ASCII + "\r\n";
                                ///}
                                ///else if ("00002a26-0000-1000-8000-00805f9b34fb".equals(dataChar.getUuid().toString()))
                                ///{
                                ///    mStringBuffer += "Firmware Revision: " + ASCII + "\r\n";
                                ///}
                                ///else if ("00002a28-0000-1000-8000-00805f9b34fb".equals(dataChar.getUuid().toString()))
                                ///{
                                ///    mStringBuffer += "Software Revision: " + stringBuilder.toString()+"\r\n";
                                ///}
                                //else if(UUID_Notify.equals(dataChar.getUuid().toString()))
                                //{
                                //    mStringBuffer += "MLC BLE Notify: " + stringBuilder.toString()+"\r\n";
                                //}
                                //else if(UUID_Write.equals(dataChar.getUuid().toString()))
                                //{
                                //   mStringBuffer += "MLC BLE write: " + stringBuilder.toString()+"\r\n";
                                //}

                                //mStringBuffer += "MLC BP data: \r\n" + ASCII + "\r\n" + stringBuilder.toString() + "\r\n";
                                fReadSus = true;
                            }
                        }
                    });

                    while (!fReadSus)
                    {}

                    fReadSus = false;
                }
            }
        }

        if (mNotifyChar != null)
        {
            fReadSus = false;
            bleService.getBleManager().notifyCharacteristic(mNotifyChar, true, new CallBack()
            {
                @Override
                public void callBack(BluetoothGattDescriptor descriptor, int status)
                {
                    fReadSus = true;
                }
            });

            while (!fReadSus)
            {}

            fReadSus = false;
        }

        //InsertText(mStringBuffer);

        //if (mWriteChar != null)
        //{
        //    SendCMD();
        //}
    }

    Calendar cal = Calendar.getInstance();
    String txtFileName = Integer.toString(cal.get(Calendar.YEAR)) +
                                    Integer.toString((cal.get(Calendar.MONTH))+1) +
                                    Integer.toString(cal.get(Calendar.DATE)) +
                                    Integer.toString(cal.get(Calendar.HOUR)) +
                                    Integer.toString(cal.get(Calendar.MINUTE)) + ".txt";
    @Override
    public void onDataAvailable(String serviceUUid, final String characteristicUUid, String text, byte[] data) {
        final StringBuilder stringBuilder = new StringBuilder(data.length);
        //String ASCII = new String(data);
        //if(characteristicUUid.equals(UUID_Notify))

        //Log.d("TAG", characteristicUUid);
        //if(characteristicUUid.equals(UUID_Notify) || characteristicUUid.equals(UUID_Write))
        if (readDataFlag)
        {
            for (byte byteChar : data)
            {
                stringBuilder.append(format("%02X", byteChar));
                InsertText(String.format("%02X", byteChar), 1);
                Utils.writeFile(this.getActivity(), "/sdcard/" + txtFileName,
                                String.format("%02X", byteChar));
            }

            Log.d("FileName", txtFileName);
        }
        //InsertText("FFF1 Receive: " + ASCII);
        //Byte.parseByte(ASCII, 16);

        //InsertText(": " + String.format("%00X", ASCII) );
        //String.format("%00X", ASCII);
        //}
            //if (ASCII.equals("0123456789"))
            //{
            //    mCheckText.setText("PASS");
            //}
            //else
            //{
            //    mCheckText.setText("Error");
            //}

    }

    //onServiceConnected and onServiceDisconnected method listen BleService event
    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        bleService = ((BleService.LocalBinder) service).getService();
        bleService.setServiceListener(this);
        if (!bleService.getBleManager().initialize(getActivity().getBaseContext()))
        {
            Log.i(TAG, "Unable to initialize Bluetooth");
            return;
        }
        // Connect to the Ble device display detail
        // InsertText("Trying to create a new connection with Device: " + deviceAddress);
        bleService.getBleManager().connect(getActivity().getBaseContext(), deviceAddress);

        InsertText("DviceName: " + deviceName, 9);
        InsertText("MacAddress: " + deviceAddress, 9);

    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {

        bleService = null;
    }

    //public void InsertText(String mString)
    //{
    //    mDataText.setText(mDataText.getText().toString() + mString + "\n\r");
    //    mScroller.fullScroll(ScrollView.FOCUS_DOWN);
    //}

    /*
     "type " for  display type
     0: Hex number
     1: dec number
     9: String
    */
    public void InsertText(String mString, int type)
    {
        switch(type)
        {
            case 0:
                //mDataText.append(String.format("%02X ",));
                mDataText.append(mString + ", ");
                break;

            case 1:
                mDataText.append(mString + ", ");
                break;

            case 9:
                mDataText.append(mString + " \n\r");
                break;

            default:
                break;
        }
    }

    private void SendCMD(final int fnCMD)
    {
        //if (mWriteChar != null)
        {
            readDataFlag = false;
            Calendar mCalendar = Calendar.getInstance();
            //final int cmdLength = 12; //for A6 BT
            final int cmdLength = 11;     //for CB2
            byte[] msgCMD = {   0x4d, (byte) 0xff, 0x08, (byte)fnCMD,
                                (byte)(mCalendar.get(Calendar.YEAR)-2000),
                                (byte)(mCalendar.get(Calendar.MONTH)+1),
                                (byte)mCalendar.get(Calendar.DATE),
                                (byte)mCalendar.get(Calendar.HOUR),
                                (byte)mCalendar.get(Calendar.MINUTE),
                                (byte)mCalendar.get(Calendar.SECOND),
                                0x00, 0x00 };

            for (int i=0; i<cmdLength-1; i++)
                msgCMD[cmdLength-1] += msgCMD[i];

            //InsertText("witre state: " + mNotifyChar.getProperties());
            mWriteChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            mWriteChar.setValue(msgCMD);
            bleService.getBleManager().writeCharacteristic(mWriteChar);
            InsertText("Send cmd Out.", 9);
            readDataFlag = true;
        }
    }
}
