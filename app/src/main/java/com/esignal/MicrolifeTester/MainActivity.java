package com.esignal.MicrolifeTester;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v4.app.FragmentActivity;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.esignal.MicrolifeTester.Fragment.BleDevicesList;
import com.esignal.MicrolifeTester.R;


public class MainActivity extends FragmentActivity
{
    public static final int ID_FRAGMENT_CONTAINER = R.id.container;
    public TextView mTextView;

    @Override
    protected void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView) findViewById(R.id.textView);
        getActionBar().hide();
        new CountDownTimer(1500, 1000)
        {
            @Override
            public void onTick(long millisUntilFinished)
            {}

            @Override
            public void onFinish()
            {
                getActionBar().show();
                mTextView.setText("");
                getSupportFragmentManager().popBackStack();
                getSupportFragmentManager().beginTransaction()
                        .replace(ID_FRAGMENT_CONTAINER, BleDevicesList.newInstance())
                        .commitAllowingStateLoss();

            }
        }.start();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    /*
    private void setupTabs()
    {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(true);

        ActionBar.Tab bleDeviceListTab = actionBar
                .newTab()
                .setText(R.string.title_ble)
                .setTabListener(new ActionBar.TabListener()
                {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
                    {
                        getSupportFragmentManager().beginTransaction()
                                .replace(ID_FRAGMENT_CONTAINER, BleDevicesList.newInstance())
                                .commitAllowingStateLoss();
                    }

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
                    {}

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
                    {}
                });

        ActionBar.Tab settingTab = actionBar
                .newTab()
                .setText((R.string.title_setting))
                .setTabListener(new ActionBar.TabListener()
                {
                    @Override
                    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft)
                    {}

                    @Override
                    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft)
                    {}

                    @Override
                    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft)
                    {}
                });
        actionBar.addTab(bleDeviceListTab);
        actionBar.addTab(settingTab);
        actionBar.selectTab(bleDeviceListTab);
    }
    */
}
