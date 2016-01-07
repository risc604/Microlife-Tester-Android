package com.esignal.MicrolifeTester.Fragment;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.esignal.MicrolifeTester.Adapter.BleDevicesAdapter;
import com.esignal.MicrolifeTester.Ble.BleDevicesScanner;
import com.esignal.MicrolifeTester.Ble.BleUtils;
import com.esignal.MicrolifeTester.Dialog.EnableBluetoothDialog;
import com.esignal.MicrolifeTester.MainActivity;
import com.esignal.MicrolifeTester.R;

import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class BleDevicesList extends ListFragment implements EnableBluetoothDialog.onClickListener
{
    private final static String TAG = BleDevicesList.class.getSimpleName();
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final long SCAN_PERIOD = 500;
    public static BleDevicesScanner scanner;
    private BleDevicesAdapter leDeviceListAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private Button scanButton;
    private TextView devicesnumber;

    public BleDevicesList()
    {}

    public static BleDevicesList newInstance()
    {
        BleDevicesList fragment = new BleDevicesList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        // it's to check my phone BT enable.
        final int bleStatus = BleUtils.getBleStatus(getActivity().getBaseContext());
        switch (bleStatus)
        {
            case BleUtils.STATUS_BLE_NOT_AVAILABLE:
                return;

            case BleUtils.STATUS_BLUETOOTH_NOT_AVAILABLE:
                return;

            default:
                bluetoothAdapter = BleUtils.getBluetoothAdapter(getActivity().getBaseContext());
        }

        if (bluetoothAdapter == null)
            return;

        // initialize scanner neighborhood BT device
        scanner = new BleDevicesScanner(bluetoothAdapter, new BluetoothAdapter.LeScanCallback()
        {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord)
            {
                String Title = device.getName();

                //if (Title.equals("A6 BT"))
                {
                    // discovered device and add to list View item.
                    leDeviceListAdapter.addDevice(device, rssi);
                    leDeviceListAdapter.notifyDataSetChanged();
                    devicesnumber.setText(getListAdapter().getCount() + " devices");
                }
            }
        });

        scanner.setScanPeriod(SCAN_PERIOD);
        init();
    }

    public boolean isNumeric(String str)
    {
        Pattern pattern = Pattern.compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if (!isNum.matches())
        {
            return false;
        }
        return true;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ble_device_list, container, false);
        scanButton = (Button) view.findViewById(R.id.scan);
        devicesnumber = (TextView) view.findViewById(R.id.device_number);
        return view;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        if (bluetoothAdapter == null)
            return;

        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!bluetoothAdapter.isEnabled())
        {
            final Fragment f = getFragmentManager().findFragmentByTag(EnableBluetoothDialog.TAG);
            if (f == null)
                EnableBluetoothDialog.newInstance(this).show(getActivity().getFragmentManager(), EnableBluetoothDialog.TAG);
        }

        scanButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (scanner.isScanning())
                {
                    scanner.stop();
                    scanButton.setText("Scan");

                } else
                {
                    leDeviceListAdapter.clear();
                    leDeviceListAdapter.notifyDataSetChanged();
                    devicesnumber.setText(getListAdapter().getCount() + " devices");
                    scanner.start();
                    scanButton.setText("Stop");

                    new CountDownTimer(60000, 200)
                    {
                        int i = 0;

                        @Override
                        public void onTick(long millisUntilFinished)
                        {
                            i++;
                            if (i > 2)
                            {
                                i = 0;
                            }
                            if (getView() != null)
                            {

                            }
                        }

                        @Override
                        public void onFinish()
                        {
                            if (scanner != null)
                                scanner.stop();
                            scanButton.setText("Scan");
                        }
                    }.start();
                }
            }
        });
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (scanner != null)
            scanner.stop();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id)
        {
            case R.id.action_settings:
                return false;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init()
    {
        if (leDeviceListAdapter == null)
        {
            leDeviceListAdapter = new BleDevicesAdapter(getActivity().getBaseContext());
            setListAdapter(leDeviceListAdapter);
        }
//        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id)
    {
        final BluetoothDevice device = leDeviceListAdapter.getDevice(position);
        int RSSI = leDeviceListAdapter.getRSSI(device);
        String RSSIvalue = String.valueOf(RSSI);
        if (device == null)
            return;

        unpairDevice(device);
        scanner.stop();

        getActivity().getSupportFragmentManager().beginTransaction()
                .addToBackStack(TAG)
                .replace(MainActivity.ID_FRAGMENT_CONTAINER, ControlActivity.newInstance(device.getName(), device.getAddress(), RSSIvalue))
                .commit();
    }


    @Override
    public void onEnableBluetooth()
    {
        bluetoothAdapter.enable();
    }

    @Override
    public void onCancel()
    {
        getActivity().finish();
    }

    private void unpairDevice(BluetoothDevice device)
    {
        try
        {
            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
            method.invoke(device, (Class[]) null);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
