package com.esignal.MicrolifeTester.Adapter;


import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import com.esignal.MicrolifeTester.R;

import java.util.ArrayList;
import java.util.HashMap;

public class BleDevicesAdapter extends BaseAdapter
{
    private final LayoutInflater inflater;

    private final ArrayList<BluetoothDevice> leDevices;
    private final HashMap<BluetoothDevice, Integer> rssiMap = new HashMap<BluetoothDevice, Integer>();

    public BleDevicesAdapter(Context context)
    {
        leDevices = new ArrayList<BluetoothDevice>();
        inflater = LayoutInflater.from(context);
    }

    public void addDevice(BluetoothDevice device, int rssi)
    {
        if (!leDevices.contains(device))
        {
            leDevices.add(device);
        }
        rssiMap.put(device, rssi);
    }

    public BluetoothDevice getDevice(int position)
    {
        return leDevices.get(position);
    }

    public int getRSSI(BluetoothDevice device)
    {

        return rssiMap.get(device);
    }

    public void clear()
    {
        leDevices.clear();
    }

    @Override
    public int getCount()
    {
        return leDevices.size();
    }

    @Override
    public Object getItem(int position)
    {
        return leDevices.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        ViewHolder viewHolder;
        // General ListView optimization code.
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.ble_device_adapter, null);
            viewHolder = new ViewHolder();
            viewHolder.deviceAddress = (TextView) convertView.findViewById(R.id.device_address);
            viewHolder.deviceName = (TextView) convertView.findViewById(R.id.device_name);
            viewHolder.deviceRssi = (TextView) convertView.findViewById(R.id.device_rssi);
            convertView.setTag(viewHolder);
        } else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        BluetoothDevice device = leDevices.get(position);
        final String deviceName = device.getName();
        if (deviceName != null && deviceName.length() > 0)
            viewHolder.deviceName.setText(deviceName);
        else
            viewHolder.deviceName.setText(R.string.unknown_device);
        viewHolder.deviceAddress.setText(device.getAddress());
        viewHolder.deviceRssi.setText("" + rssiMap.get(device) + " dBm");

        return convertView;
    }

    private static class ViewHolder
    {
        TextView deviceName;
        TextView deviceAddress;
        TextView deviceRssi;
    }
}