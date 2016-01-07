package com.esignal.MicrolifeTester;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class UuidManager
{
    private static final String KEY_UPDATE_CODE_SERVICE = "update_code_service";
    private static final String KEY_RESP_INDICATION_CHARACTERISTIC = "resp_indication_characteristic";
    private static final String KEY_WRITE_CODE_REQUEST_CHARACTERISTIC = "write_code_request_characteristic";

    private Activity mContext;

    public UuidManager(Context context)
    {
        mContext = (Activity) context;
    }

    public Uuid get()
    {
        SharedPreferences sharedPref = mContext.getPreferences(Context.MODE_PRIVATE);
        Uuid uuid = new Uuid();
        uuid.setUUID_UPDATE_CODE_SERVICE(UUID.fromString(sharedPref.getString(KEY_UPDATE_CODE_SERVICE, "0000fff0-0000-1000-8000-00805f9b34fb")));
        uuid.setUUID_RESP_INDICATION_CHARACTERISTIC(UUID.fromString(sharedPref.getString(KEY_RESP_INDICATION_CHARACTERISTIC, "0000fff1-0000-1000-8000-00805f9b34fb")));
        uuid.setUUID_WRITE_CODE_REQUEST_CHARACTERISTIC(UUID.fromString(sharedPref.getString(KEY_WRITE_CODE_REQUEST_CHARACTERISTIC, "0000fff2-0000-1000-8000-00805f9b34fb")));
        return uuid;
    }

    public void save(Uuid uuid)
    {
        mContext.getPreferences(Context.MODE_PRIVATE).edit()
                .putString(KEY_UPDATE_CODE_SERVICE, uuid.getUUID_UPDATE_CODE_SERVICE().toString())
                .putString(KEY_RESP_INDICATION_CHARACTERISTIC, uuid.getUUID_RESP_INDICATION_CHARACTERISTIC().toString())
                .putString(KEY_WRITE_CODE_REQUEST_CHARACTERISTIC, uuid.getUUID_WRITE_CODE_REQUEST_CHARACTERISTIC().toString())
                .apply();
    }

    public void clear()
    {
        mContext.getPreferences(Context.MODE_PRIVATE).edit()
                .remove(KEY_UPDATE_CODE_SERVICE)
                .remove(KEY_RESP_INDICATION_CHARACTERISTIC)
                .remove(KEY_WRITE_CODE_REQUEST_CHARACTERISTIC)
                .apply();
    }
}
