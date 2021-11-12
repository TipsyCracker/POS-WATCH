package com.nowfloats.smartretailer.Services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import com.google.firebase.iid.FirebaseInstanceId;
import com.nowfloats.smartretailer.HelperClasses.BluetoothSPP;
import com.nowfloats.smartretailer.HelperClasses.NotificationHelper;
import com.nowfloats.smartretailer.Models.MainBillingModel;
import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.Models.Product_Table;
import com.nowfloats.smartretailer.Models.TransactionData;
import com.nowfloats.smartretailer.Models.TransactionData_Table;
import com.nowfloats.smartretailer.Models.TransactionHistory;
import com.nowfloats.smartretailer.Models.TransactionHistory_Table;
import com.nowfloats.smartretailer.NetworkService.AdapterFactory;
import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.BluetoothState;
import com.nowfloats.smartretailer.Utils.Constants;
import com.nowfloats.smartretailer.Utils.DataEvents;
import com.nowfloats.smartretailer.Utils.KeyMap;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import timber.log.Timber;

/**
 * Created by NowFloats on 12-01-2017.
 */

public class TransactionService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, BluetoothSPP.OnDataReceivedListener,
        BluetoothSPP.BluetoothConnectionListener, DataApi.DataListener{

    String mTransactionId;
    private GoogleApiClient mGoogleApiClient;
    private BluetoothSPP mBtSPP;
    private SharedPreferences mSharedPref;
    private boolean mIsConnected = false;

    @Override
    public void onCreate() {
        super.onCreate();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mSharedPref = getSharedPreferences(Constants.SHARED_PREF_NAME, MODE_PRIVATE);
        mBtSPP = new BluetoothSPP(getApplicationContext());
        mBtSPP.setupService();
        mBtSPP.startService(BluetoothState.DEVICE_OTHER);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        mGoogleApiClient.connect();
        String scannerMacId = mSharedPref.getString(KeyMap.KEY_SCANNER_ID, null);

        if(scannerMacId!=null && mBtSPP.getServiceState()==BluetoothState.STATE_CONNECTED)
        {
            mTransactionId = UUID.randomUUID().toString().replaceAll("-", "");

            TransactionHistory transactionHistory = new TransactionHistory();
            transactionHistory.setTransactionId(mTransactionId);
            transactionHistory.setDateTimeStamp(new Date());
            transactionHistory.setStatus(0);
            transactionHistory.setTotalAmount(0);
            transactionHistory.save();

            final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataEvents.DATA_EVENT_INIT_TRANSACTION);
            putDataMapRequest.getDataMap().putBoolean(KeyMap.KEY_INIT_TRANSACT, true);
            putDataMapRequest.getDataMap().putString(KeyMap.KEY_TRANSACTION_ID, mTransactionId);
            putDataMapRequest.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());

            final PutDataRequest dataRequest = putDataMapRequest.asPutDataRequest();
            dataRequest.setUrgent();
            putDataMapRequest.setUrgent();
            PendingResult<DataApi.DataItemResult> pendingResult =
                    Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);
            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                    //Wearable.DataApi.deleteDataItems(mGoogleApiClient, putDataMapRequest.getUri());
                }
            });

        } else if(scannerMacId!=null && mBtSPP.getServiceState()!=BluetoothState.STATE_CONNECTED) {
            mBtSPP.connect(scannerMacId);
        } else {
            Intent notifyIntent =
                    getPackageManager().getLaunchIntentForPackage(getPackageName());
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifyIntent,
                    PendingIntent.FLAG_ONE_SHOT);
            NotificationHelper.showNotification(this, getString(R.string.scanner_not_added_title),
                    getString(R.string.scanner_not_added_content), true, pendingIntent, 23);
        }

        mBtSPP.setOnDataReceivedListener(this);
        mBtSPP.setBluetoothConnectionListener(this);

        Timber.d("Calling of all Bluetooth Intents");

        return START_STICKY;
    }



    public void sendProductDataToWearable(final String productId){
        SQLite.select()
                .from(Product.class)
                .where(Product_Table.productId.is(productId))
                .async()
                .querySingleResultCallback(new QueryTransaction.QueryResultSingleCallback<Product>() {
                    @Override
                    public void onSingleQueryResult(QueryTransaction transaction, @Nullable final Product product) {
                        Timber.d("Product Query Callback");
                        if (product != null) {
                            final PutDataRequest dataRequest =  product.getDataMapRequestObject().asPutDataRequest();
                            dataRequest.setUrgent();
                            PendingResult<DataApi.DataItemResult> pendingResult =
                                    Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);
                            pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                @Override
                                public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {

                                    //Wearable.DataApi.deleteDataItems(mGoogleApiClient, dataRequest.getUri());

                                    Timber.d("Status: %s", dataItemResult.getStatus());
                                    TransactionData transactionData = new TransactionData();
                                    transactionData.setTransactionId(mTransactionId);
                                    transactionData.setNetPrice(product.getProductPrice() - product.getProductDiscount());
                                    transactionData.setProductId(product.getProductId());
                                    transactionData.setQuantity(1);
                                    transactionData.setDateTimeStamp(new Date());
                                    transactionData.save();
                                }
                            });
                        }else {

                            final PutDataMapRequest dataRequest =  PutDataMapRequest.create(DataEvents.DATA_EVENT_PRODUCT);
                            dataRequest.getDataMap().putBoolean(KeyMap.KEY_IS_PRODUCT_AVAILABLE, false);
                            dataRequest.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());
                            dataRequest.setUrgent();
                            PutDataRequest sendDataRequest = dataRequest.asPutDataRequest();
                            sendDataRequest.setUrgent();
                            PendingResult<DataApi.DataItemResult> pendingResult =
                                    Wearable.DataApi.putDataItem(mGoogleApiClient, sendDataRequest);
                            Timber.d("Product not available");
                        }
                    }
                }).execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataReceived(byte[] data, String message) {
        Timber.d("Received Data %s", message);
        sendProductDataToWearable(message.trim());
    }

    @Override
    public void onDeviceConnected(String name, String address) {
        mIsConnected = true;
        Timber.d("Connection Success");
        mTransactionId = UUID.randomUUID().toString().replaceAll("-", "");

        TransactionHistory transactionHistory = new TransactionHistory();
        transactionHistory.setTransactionId(mTransactionId);
        transactionHistory.setDateTimeStamp(new Date());
        transactionHistory.setStatus(0);
        transactionHistory.setTotalAmount(0);
        transactionHistory.save();

        final PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataEvents.DATA_EVENT_INIT_TRANSACTION);
        putDataMapRequest.getDataMap().putBoolean(KeyMap.KEY_INIT_TRANSACT, true);
        putDataMapRequest.getDataMap().putString(KeyMap.KEY_TRANSACTION_ID, mTransactionId);
        putDataMapRequest.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());

        final PutDataRequest dataRequest = putDataMapRequest.asPutDataRequest();
        dataRequest.setUrgent();
        putDataMapRequest.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                //Wearable.DataApi.deleteDataItems(mGoogleApiClient, putDataMapRequest.getUri());
            }
        });

    }

    @Override
    public void onDeviceDisconnected() {
        mIsConnected = false;
        Timber.d("Device Disconnected");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!mIsConnected) {
                    stopSelf();
                }
                Timber.d("Stopping Process");
            }
        }, 60000);
    }

    @Override
    public void onDeviceNotPaired() {

    }

    @Override
    public void onDeviceConnectionFailed() {
        Timber.d("Connection Failed");
        final PutDataMapRequest putDataMapRequest = PutDataMapRequest
                .create(DataEvents.DATA_EVENT_INIT_TRANSACTION).setUrgent();
        putDataMapRequest.getDataMap().putBoolean(KeyMap.KEY_INIT_TRANSACT, false);
        putDataMapRequest.getDataMap().putString(KeyMap.KEY_REASON,
                getString(R.string.connection_failed));
        putDataMapRequest.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());

        putDataMapRequest.setUrgent();

        final PutDataRequest dataRequest = putDataMapRequest.asPutDataRequest();
        dataRequest.setUrgent();
        PendingResult<DataApi.DataItemResult> pendingResult =
                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);
        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                //Wearable.DataApi.deleteDataItems(mGoogleApiClient, putDataMapRequest.getUri());
            }
        });
    }

    @Override
    public void onBluetoothConnected() {

    }

    @Override
    public void onBluetoothDisConnected() {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d("MainActivity", "DataEvent");
        for(final DataEvent dataEvent : dataEventBuffer){
            if(dataEvent.getType() == DataEvent.TYPE_CHANGED &&
                    dataEvent.getDataItem().getUri().getPath().equals(DataEvents.DATA_EVENT_DELETE_PRODUCT)){
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                SQLite.select(TransactionData_Table.id)
                        .from(TransactionData.class)
                        .where(TransactionData_Table.transactionId.is(dataMapItem.getDataMap().getString(KeyMap.KEY_TRANSACTION_ID))
                                , TransactionData_Table.mainProductId.is(dataMapItem.getDataMap().getString(KeyMap.KEY_PRODUCT_ID)))
                        .async()
                        .querySingleResultCallback(new QueryTransaction.QueryResultSingleCallback<TransactionData>() {
                            @Override
                            public void onSingleQueryResult(QueryTransaction transaction, @Nullable TransactionData transactionData) {
                                SQLite.delete(TransactionData.class)
                                        .where(TransactionData_Table.id.is(transactionData.getId()))
                                        .async()
                                        .execute();
                            }
                        }).execute();

                Timber.d("Item Deleted");
            }else if(dataEvent.getType() == DataEvent.TYPE_CHANGED &&
                    dataEvent.getDataItem().getUri().getPath().equals(DataEvents.DATA_EVENT_BILLING)){
                final DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                SQLite.update(TransactionHistory.class)
                        .set(TransactionHistory_Table.totalAmount.eq(dataMapItem.getDataMap().getDouble(KeyMap.KEY_BILLING_AMOUNT)))
                        .where(TransactionHistory_Table.transactionId.is(dataMapItem.getDataMap().getString(KeyMap.KEY_TRANSACTION_ID)))
                        .async()
                        .execute();

                SQLite.select(Product_Table.productId, Product_Table.productName, Product_Table.productPrice,
                        Product_Table.productCurrency, Product_Table.productDiscount)
                        .from(Product.class)
                        .innerJoin(TransactionData.class)
                        .on(Product_Table.productId.withTable().eq(TransactionData_Table.mainProductId))
                        .where(TransactionData_Table.transactionId.is(dataMapItem.getDataMap().getString(KeyMap.KEY_TRANSACTION_ID)))
                        .async()
                        .queryListResultCallback(new QueryTransaction.QueryResultListCallback<Product>() {
                            @Override
                            public void onListQueryResult(QueryTransaction transaction, @NonNull List<Product> tResult) {
                                MainBillingModel mainBillingModel = getBillingModel(tResult,
                                        dataMapItem.getDataMap().getString(KeyMap.KEY_TRANSACTION_ID),
                                        dataMapItem.getDataMap().getDouble(KeyMap.KEY_BILLING_AMOUNT));
                                AdapterFactory.getStoreApiAdapter().postPurchaseDetails(mainBillingModel, new Callback<MainBillingModel.BillingResult>() {
                                    @Override
                                    public void success(MainBillingModel.BillingResult billingResult, Response response) {
                                        Timber.d(billingResult.getProcessingId());
                                        Timber.d("Hello World");
                                        Timber.d(billingResult.toString());
                                        MainBillingModel.BillingResult result = billingResult;
                                        PutDataMapRequest dataMap = PutDataMapRequest.create(DataEvents.DATA_EVENT_BILLING_ID);
                                        dataMap.getDataMap().putBoolean(KeyMap.KEY_IS_BILLING_SUCCESS, true);
                                        dataMap.getDataMap().putString(KeyMap.KEY_BILLING_ID, result.getProcessingId());
                                        dataMap.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());
                                        dataMap.setUrgent();
                                        PutDataRequest request = dataMap.asPutDataRequest();
                                        request.setUrgent();
                                        PendingResult<DataApi.DataItemResult> pendingResult =
                                                Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                                        pendingResult.setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                                            @Override
                                            public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                                                //Wearable.DataApi.deleteDataItems(mGoogleApiClient, putDataMapRequest.getUri());
                                                Timber.d("ProcessingId sent.");
                                            }
                                        });
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        Timber.d("failed");
                                        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataEvents.DATA_EVENT_BILLING_ID);
                                        putDataMapRequest.getDataMap().putBoolean(KeyMap.KEY_IS_BILLING_SUCCESS, false);
                                        putDataMapRequest.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());
                                        putDataMapRequest.setUrgent();
                                        PutDataRequest dataRequest = putDataMapRequest.asPutDataRequest();
                                        dataRequest.setUrgent();
                                        PendingResult<DataApi.DataItemResult> pendingResult =
                                                Wearable.DataApi.putDataItem(mGoogleApiClient, dataRequest);

                                    }
                                });

                            }
                        })
                        .execute();
            }else if(dataEvent.getType() == DataEvent.TYPE_CHANGED &&
                    dataEvent.getDataItem().getUri().getPath().equals(DataEvents.DATA_EVENT_CHANGE_QUANTITY)){
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                SQLite.update(TransactionData.class)
                        .set(TransactionData_Table.quantity.eq(dataMapItem.getDataMap().getLong(KeyMap.KEY_PRODUCT_QUANTITY)))
                        .where(TransactionData_Table.transactionId.eq(dataMapItem.getDataMap().getString(KeyMap.KEY_TRANSACTION_ID)))
                        .and(TransactionData_Table.mainProductId.eq(dataMapItem.getDataMap().getString(KeyMap.KEY_PRODUCT_ID)))
                        .async()
                        .execute();

            }
        }
        dataEventBuffer.release();
    }

    private MainBillingModel getBillingModel(List<Product> tResult, String transactionId, double amount) {
        MainBillingModel mainBillingModel = new MainBillingModel();
        mainBillingModel.setTransactionId(transactionId);
        mainBillingModel.setProcessingId(null);
        mainBillingModel.setDateAndTime(new Date().toString());
        mainBillingModel.setTotalAmount(amount);
        mainBillingModel.setFbToken(FirebaseInstanceId.getInstance().getToken());
        List<MainBillingModel.ItemsList> itemList = new ArrayList<>();
        for(Product product: tResult){
            MainBillingModel.ItemsList item = new MainBillingModel().new ItemsList();
            item.setProductId(product.getProductId());
            item.setProductName(product.getProductName());
            item.setPrice(product.getProductPrice());
            item.setDiscount(product.getProductDiscount());
            itemList.add(item);
        }
        mainBillingModel.setItemsList(itemList);
        return mainBillingModel;
    }

    //TODO: Make a shared pref variable to store the transactionId, delete the transaction id when the transaction complete. if the transaction id is not null us that transaction Id on Start Command

}
