package com.nowfloats.smartretailer.Activities;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.BoolRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableRecyclerView;
import android.util.FloatMath;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.nowfloats.smartretailer.Adapters.ShoppingListRvAdapter;
import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.Models.ShoppingItemModel;
import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.DataEvents;
import com.nowfloats.smartretailer.Utils.KeyMap;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends WearableActivity implements ShoppingListRvAdapter.OnItemNumberUpdate,
        DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, SensorEventListener,
        ShoppingListRvAdapter.ItemRemoveCallback, ShoppingListRvAdapter.OnItemClickListener{

    private static final float SHAKE_THRESHOLD = 2.0f;
    private static final int SHAKE_WAIT_TIME_MS = 250;
    private static final int VIBRATION_TIME = 150;

    private WearableRecyclerView rvShoppingList;
    private TextView tvCurrentItemNumber;
    private ProgressDialog pd;

    private ShoppingListRvAdapter mShoppingListAdapter;
    private List<Product> mShoppingItemList = new ArrayList<>();
    private GoogleApiClient mGoogleApiClient;

    private long mShakeTime = 0;
    private boolean mShouldDetect = true;
    private String mTransactionId;

    int totalXscroleed=0;

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Vibrator mVibrator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mTransactionId = getIntent().getStringExtra(KeyMap.KEY_TRANSACTION_ID);

        setAmbientEnabled();

        pd = new ProgressDialog(this);
        pd.setMessage("Please Wait...");

        //TODO: show Empty view if product List is Empty

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                tvCurrentItemNumber = (TextView) findViewById(R.id.tvCurrentItemNumber);
                rvShoppingList = (WearableRecyclerView) findViewById(R.id.rvShoppingList);
                final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.HORIZONTAL, false);
                rvShoppingList.setLayoutManager(linearLayoutManager);
                mShoppingListAdapter = new ShoppingListRvAdapter(mShoppingItemList, MainActivity.this);
                mShoppingListAdapter.setmItemRemoveCallback(MainActivity.this);
                mShoppingListAdapter.setOnItemClickListener(new ShoppingListRvAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        showQuantityConfiramrionDialog(position);
                    }
                });
                rvShoppingList.setAdapter(mShoppingListAdapter);

            }
        });

    }

    @Override
    public void onBackPressed() {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Do you want to cancel this Transaction?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    private void showQuantityConfiramrionDialog(final int position) {

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.quantity_set_dialogue);
        final Product product = mShoppingItemList.get(position);
        final TextView tvTotalAmount = (TextView) dialog.findViewById(R.id.tvTotalNetPrice);
        tvTotalAmount.setText(getString(R.string.rs)+(product.getProductPrice()-product.getProductDiscount()));
        final NumberPicker quantityPicker = (NumberPicker) dialog.findViewById(R.id.npTotalQuantity);
        String[] nums = new String[20];
        for(int i=0; i<nums.length; i++)
            nums[i] = Integer.toString(i+1);

        quantityPicker.setMinValue(1);
        quantityPicker.setMaxValue(20);
        quantityPicker.setWrapSelectorWheel(false);
        quantityPicker.setDisplayedValues(nums);
        quantityPicker.setValue(1);
        ImageView ivConfirmQuantity = (ImageView) dialog.findViewById(R.id.ivQuantitySubmit);
        quantityPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                tvTotalAmount.setText(getString(R.string.rs)+(product.getProductPrice()-product.getProductDiscount())*newVal);
            }
        });
        ivConfirmQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShoppingItemList.get(position).setQuantity(quantityPicker.getValue());
                mShoppingListAdapter.notifyItemChanged(position);
                PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataEvents.DATA_EVENT_CHANGE_QUANTITY);
                putDataMapRequest.getDataMap().putLong(KeyMap.KEY_PRODUCT_QUANTITY, quantityPicker.getValue());
                putDataMapRequest.getDataMap().putString(KeyMap.KEY_TRANSACTION_ID, mTransactionId);
                putDataMapRequest.getDataMap().putString(KeyMap.KEY_PRODUCT_ID, product.getProductId());
                putDataMapRequest.setUrgent();

                PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult =
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
                pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        //Wearable.DataApi.deleteDataItems(mGoogleApiClient, putDataRequest.getUri());
                        dialog.dismiss();
                    }
                });

            }
        });
        dialog.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {

            mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    @Override
    public void onUpdate(int currentItem, int totalItem) {
        tvCurrentItemNumber.setText((currentItem+1) + " / " + mShoppingItemList.size());
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        Log.d("MainActivity", "Google API client Connected");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d("MainActivity", "Google API client Connection Failed");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d("MainActivity", "DataEvent");
        for(DataEvent dataEvent : dataEventBuffer){
            if(dataEvent.getType() == DataEvent.TYPE_CHANGED &&
                    dataEvent.getDataItem().getUri().getPath().equals(DataEvents.DATA_EVENT_PRODUCT)){
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                if(dataMapItem.getDataMap().getBoolean(KeyMap.KEY_IS_PRODUCT_AVAILABLE)) {
                    Product product = new Product();
                    product.setProductName(dataMapItem.getDataMap().getString("productName"));
                    product.setProductId(dataMapItem.getDataMap().getString("productId"));
                    product.setProductCurrency(dataMapItem.getDataMap().getString("productCurrency"));
                    product.setProductPrice(dataMapItem.getDataMap().getDouble("productPrice"));
                    product.setProductDiscount(dataMapItem.getDataMap().getDouble("productDiscount"));
                    mShoppingItemList.add(product);
                    mShoppingListAdapter.notifyItemInserted(mShoppingItemList.size());
                    rvShoppingList.scrollToPosition(mShoppingListAdapter.getItemCount() - 1);
                    mVibrator.vibrate(VIBRATION_TIME);
                }else {
                    Toast.makeText(this, getString(R.string.product_not_available), Toast.LENGTH_SHORT).show();
                }

            }else if(dataEvent.getType() == DataEvent.TYPE_CHANGED &&
                    dataEvent.getDataItem().getUri().getPath().equals(DataEvents.DATA_EVENT_BILLING_ID)){
                Log.d("MainActivity", "Triggered data event billing ID");
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                if(pd.isShowing()){
                    pd.dismiss();
                }
                if(dataMapItem.getDataMap().getBoolean(KeyMap.KEY_IS_BILLING_SUCCESS)) {
                    android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this)
                            .setCancelable(false)
                            .setTitle("Billing Id")
                            .setMessage("Billing Id: " + dataMapItem.getDataMap().getString(KeyMap.KEY_BILLING_ID))
                            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(final DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    finish();
                                }
                            }).create();
                    mVibrator.vibrate(VIBRATION_TIME);
                    alertDialog.show();
                }else {
                    Toast.makeText(this, getString(R.string.unable_to_generate_id), Toast.LENGTH_SHORT).show();
                }

            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        //Log.d("Shake", "Sensor changed");
        if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
        {
            return;
        }

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if(mShouldDetect) {
                detectShake(event);
            }
        }
    }

    private void detectShake(SensorEvent event) {
        long now = System.currentTimeMillis();

        if((now - mShakeTime) > SHAKE_WAIT_TIME_MS) {
            mShakeTime = now;

            float gX = event.values[0] / SensorManager.GRAVITY_EARTH;
            float gY = event.values[1] / SensorManager.GRAVITY_EARTH;
            float gZ = event.values[2] / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement
            float gForce = (float)Math.sqrt(gX*gX + gY*gY + gZ*gZ);
            //Log.d("Shake", gForce+"");

            // Change background color if gForce exceeds threshold;
            // otherwise, reset the color
            if(gForce > SHAKE_THRESHOLD) {
                mShouldDetect = false;
                double totalAmount = 0;
                for(Product product:mShoppingItemList){
                    totalAmount+=((product.getProductPrice()-product.getProductDiscount())*product.getQuantity());
                }
                showConfirmationDialog(totalAmount, mShoppingItemList.size());
            }
        }
    }

    private void showConfirmationDialog(final double totalAmount, int size) {
        android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Confirmation")
                .setMessage("Total cost is: " + getString(R.string.rs) + totalAmount + "\nNumber of Items: " + size)
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, int which) {
                        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataEvents.DATA_EVENT_BILLING);
                        putDataMapRequest.getDataMap().putDouble(KeyMap.KEY_BILLING_AMOUNT, totalAmount);
                        putDataMapRequest.getDataMap().putString(KeyMap.KEY_TRANSACTION_ID, mTransactionId);
                        putDataMapRequest.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());
                        putDataMapRequest.setUrgent();
                        PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
                        putDataRequest.setUrgent();

                        PendingResult<DataApi.DataItemResult> pendingResult = Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
                        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                            @Override
                            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                                dialog.dismiss();
                                pd.show();
                            }
                        });
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mShouldDetect = true;
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onItemRemoved(final int position, String productId) {
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataEvents.DATA_EVENT_DELETE_PRODUCT);
        putDataMapRequest.getDataMap().putString(KeyMap.KEY_PRODUCT_ID, productId);
        putDataMapRequest.getDataMap().putString(KeyMap.KEY_TRANSACTION_ID, mTransactionId);
        putDataMapRequest.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());
        putDataMapRequest.setUrgent();

        final PutDataRequest putDataRequest = putDataMapRequest.asPutDataRequest();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, putDataRequest);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                //Wearable.DataApi.deleteDataItems(mGoogleApiClient, putDataRequest.getUri());
                mShoppingItemList.remove(position);
                mShoppingListAdapter.notifyItemRemoved(position);
            }
        });

    }

    @Override
    public void onItemClick(int position) {

    }


}
