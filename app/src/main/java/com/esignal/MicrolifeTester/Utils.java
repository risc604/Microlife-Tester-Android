package com.esignal.MicrolifeTester;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.view.Display;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class Utils
{
    public static int getDeviceWidth(Context context)
    {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getDeviceHeight(Context context)
    {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize((size));
        return size.y;
    }

    public static String byteArray2String(byte[] data)
    {
        String text = "";
        if (data != null && data.length > 0)
        {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            text = data.toString() + "\n" + stringBuilder.toString();
        } else
        {
            text = null;
        }
        return text;
    }

    public static byte[] hexStringToByteArray(String s)
    {
        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2)
        {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }

        return data;
    }

    public static int byte2int(byte data)
    {
        return Integer.parseInt(String.format("%02x", data), 16);
    }

    //public static void writeFile(Context context, String fileName, String content)
    public static void writeFile(Context context, File txtFile, String content)
    {
        try
        {
            //Environment.getDataDirectory()
            //FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_APPEND);
            FileOutputStream fos = new FileOutputStream(txtFile, true);
            fos.write(content.getBytes());
            //fos.flush();
            fos.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}

