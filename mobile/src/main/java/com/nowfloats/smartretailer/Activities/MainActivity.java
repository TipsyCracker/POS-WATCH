package com.nowfloats.smartretailer.Activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;

import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.BluetoothState;
import com.nowfloats.smartretailer.Utils.Constants;
import com.nowfloats.smartretailer.Utils.KeyMap;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends BaseActivity {

    private SharedPreferences mSharedPref;

    @Override
    public void initialize() {
        llContent.addView(mLayoutInflater.inflate(R.layout.activity_main, null));
        ButterKnife.bind(this);
        mSharedPref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
    }

    @OnClick(R.id.btnAddProduct)
    public void setAddProduct(View view) {
        Intent i = new Intent(this, AddProductActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.btnViewAllProducts)
    public void viewAllProducts(View view) {
        Intent i = new Intent(this, ViewAllProductsActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.btnViewAllTransactions)
    public void biewAllTransactions(View view) {
        Intent i = new Intent(this, ViewAllTransactionActivity.class);
        startActivity(i);
    }

    @OnClick(R.id.btnAddBtScanner)
    public void addBtScanner(View v) {

        Intent i = new Intent(this, TransactionActivity.class);
        startActivity(i);

//        Intent intent = new Intent(getApplicationContext(), DeviceList.class);
//        startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == RESULT_OK) {
                SharedPreferences.Editor editor = mSharedPref.edit();
                editor.putString(KeyMap.KEY_SCANNER_ID, data.getExtras()
                        .getString(BluetoothState.EXTRA_DEVICE_ADDRESS));
                editor.commit();
            }
        }
        //TODO: <code>Write code for make sure that Bluetooth is on By opening Bluetooth Settings</code>
    }
}
