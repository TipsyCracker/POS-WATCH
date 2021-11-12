package com.nowfloats.smartretailer.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.nowfloats.smartretailer.HelperClasses.BluetoothSPP;
import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.BluetoothState;
import com.nowfloats.smartretailer.Utils.Constants;
import com.nowfloats.smartretailer.Utils.KeyMap;

/**
 * Created by admin on 5/27/2017.
 */

public abstract class BaseActivity extends AppCompatActivity implements BluetoothSPP.BluetoothConnectionListener {

    public LinearLayout llContent;
    public LayoutInflater mLayoutInflater;
    public BluetoothSPP mBt;
    public SharedPreferences mSharedPref;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        initalizeControls();
        initialize();
    }

    private void initalizeControls() {
        mLayoutInflater = getLayoutInflater();
        llContent = (LinearLayout) findViewById(R.id.llContent);
        mSharedPref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
    }

    public void showToast(String message) {
        Toast.makeText(BaseActivity.this, message, Toast.LENGTH_LONG).show();
    }

    public void showLoader(String message) {

        if (mProgressDialog == null)
            mProgressDialog = new ProgressDialog(BaseActivity.this);

        mProgressDialog.setMessage(message);
        mProgressDialog.setCancelable(false);

        if (!mProgressDialog.isShowing())
            mProgressDialog.show();
    }

    private void hideLoader() {

        if (mProgressDialog != null &&
                mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public abstract void initialize();

    public void connectScanner(BluetoothSPP.OnDataReceivedListener onDataReceivedListener) {

        if (mBt == null) {
            mBt = new BluetoothSPP(this);
        }
        /*
         * step 1 : Check if device supports bluetooth
         */
        if (!mBt.isBluetoothAvailable()) {
            showToast(getString(R.string.no_bluetooth_support));
            return;
        }

//        /*
//         * step 2: if bluetooth is not enabled enabling bluetooth
//         */
        if (!mBt.isBluetoothEnabled()) {
            showToast(getString(R.string.enabling_bluetooth));
            mBt.enable();
        }

        /*
         *  step 3: check if scanner configured connect scanner if not navigate to Device List
         */

        String scannerMacId = mSharedPref.getString(KeyMap.KEY_SCANNER_ID, null);
        if (scannerMacId != null) {
            mBt.setBluetoothConnectionListener(this);
            mBt.setOnDataReceivedListener(onDataReceivedListener);
            mBt.setupService();
            mBt.startService(BluetoothState.DEVICE_OTHER);
            showLoader(getString(R.string.trying_to_connect));
        } else {
            hideLoader();
            Intent intent = new Intent(getApplicationContext(), DeviceList.class);
            startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
            finish();
        }

    }

    @Override
    protected void onStop() {
        if (mBt != null) {
            mBt.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onDeviceConnected(String name, String address) {
        showToast(getString(R.string.connection_successful));
        hideLoader();
    }

    @Override
    public void onDeviceDisconnected() {
        showToast(getString(R.string.connection_disconnected));
        hideLoader();
    }

    @Override
    public void onDeviceConnectionFailed() {
        showToast(getString(R.string.connection_failed));
        hideLoader();
    }

    @Override
    public void onDeviceNotPaired() {
        hideLoader();
    }

    @Override
    public void onBluetoothConnected() {
        mBt.connect(mSharedPref.getString(KeyMap.KEY_SCANNER_ID, null));
    }

    @Override
    public void onBluetoothDisConnected() {

    }
}
