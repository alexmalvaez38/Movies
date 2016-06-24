package com.alex.test.movies.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.alex.test.movies.fragments.MainFragment;
import com.alex.test.movies.utils.NetworkMagic;

public class NetworkReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkReceiverTAG_";
    MainFragment mMainFragment;

    public NetworkReceiver(MainFragment mainFragment) {
        mMainFragment = mainFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (mMainFragment != null && NetworkMagic.isNetworkAvailable(context.getApplicationContext())){
            if (mMainFragment.isActuallyEmpty()){
                mMainFragment.getActivityCallback().onEmptyResults();
            }
        }
    }
}
