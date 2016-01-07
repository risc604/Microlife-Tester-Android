package com.esignal.MicrolifeTester.Dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.esignal.MicrolifeTester.R;

public class EnableBluetoothDialog extends DialogFragment
{
    public static final String TAG = EnableBluetoothDialog.class.getSimpleName();
    private onClickListener listener;

    public EnableBluetoothDialog()
    {
        setCancelable(false);
    }

    public static EnableBluetoothDialog newInstance(onClickListener listener)
    {
        EnableBluetoothDialog dialog = new EnableBluetoothDialog();
        dialog.setOnClickListener(listener);
        return dialog;
    }

    private void setOnClickListener(onClickListener listener)
    {
        this.listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(null)
                .setMessage(R.string.dialog_enable_bluetooth)
                .setPositiveButton(R.string.dialog_turn_on, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        listener.onEnableBluetooth();
                    }
                })
                .setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        listener.onCancel();
                    }
                });
        return builder.create();
    }

    public interface onClickListener
    {
        void onEnableBluetooth();

        void onCancel();
    }
}