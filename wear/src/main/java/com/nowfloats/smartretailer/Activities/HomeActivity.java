package com.nowfloats.smartretailer.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.CapabilityApi;
import com.google.android.gms.wearable.CapabilityInfo;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.DataEvents;
import com.nowfloats.smartretailer.Utils.KeyMap;

import java.util.Set;

public class HomeActivity extends Activity implements DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private static final String START_TRANSACTION = "start_transaction";
    private static final String START_TRANSACTION_PATH = "/start_transaction";

    ImageView ivInitiateTransaction;
    ProgressDialog pd;

    private GoogleApiClient mGoogleApiClient;
    private String mMainNode;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApiIfAvailable(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        pd = new ProgressDialog(this);
        pd.setMessage(getString(R.string.connecting_to_scanner));

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                ivInitiateTransaction = (ImageView) findViewById(R.id.ivInitiateTransaction);
                ivInitiateTransaction.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sendInitiateTransactionMessage();
                    }
                });
            }
        });

    }

    private void sendInitiateTransactionMessage() {
        if(mMainNode!=null){
            Wearable.MessageApi.sendMessage(mGoogleApiClient, mMainNode,
                    START_TRANSACTION_PATH, "true".getBytes()).setResultCallback(
                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if(!sendMessageResult.getStatus().isSuccess()){
                                Toast.makeText(HomeActivity.this, "Error while initiating transaction", Toast.LENGTH_SHORT).show();
                            }else {
                                if(pd!=null && !pd.isShowing()){
                                    pd.show();
                                }
                            }
                        }
                    }
            );
        }else {
            Toast.makeText(this, "Device is not connected to phone", Toast.LENGTH_SHORT).show();
        }
        /*Intent i = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(i);*/
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(mGoogleApiClient, this);
        findNodeId();
    }

    private void findNodeId() {

        Wearable.CapabilityApi.getCapability(
                mGoogleApiClient, START_TRANSACTION,
                CapabilityApi.FILTER_REACHABLE).setResultCallback(new ResultCallback<CapabilityApi.GetCapabilityResult>() {
            @Override
            public void onResult(@NonNull CapabilityApi.GetCapabilityResult getCapabilityResult) {
                updateTranscriptionCapability(getCapabilityResult.getCapability());
            }
        });




        CapabilityApi.CapabilityListener capabilityListener =
                new CapabilityApi.CapabilityListener() {
                    @Override
                    public void onCapabilityChanged(CapabilityInfo capabilityInfo) {
                        updateTranscriptionCapability(capabilityInfo);
                    }
                };

        Wearable.CapabilityApi.addCapabilityListener(
                mGoogleApiClient,
                capabilityListener,
                START_TRANSACTION);
    }

    private void updateTranscriptionCapability(CapabilityInfo capabilityInfo) {
        Set<Node> connectedNodes = capabilityInfo.getNodes();

        mMainNode = pickBestNodeId(connectedNodes);
    }

    private String pickBestNodeId(Set<Node> nodes) {
        String bestNodeId = null;
        for (Node node : nodes) {
            if (node.isNearby()) {
                return node.getId();
            }
            bestNodeId = node.getId();
        }
        return bestNodeId;
    }


    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, R.string.connection_with_phone_suspended, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, R.string.connection_with_phone_failed, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        for(DataEvent dataEvent : dataEventBuffer){
            if(dataEvent.getType() == DataEvent.TYPE_CHANGED &&
                    dataEvent.getDataItem().getUri().getPath()
                            .equals(DataEvents.DATA_EVENT_INIT_TRANSACTION)){
                if(pd!=null && pd.isShowing()){
                    pd.dismiss();
                }
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                Long time = dataMapItem.getDataMap().getLong(KeyMap.KEY_TIME);
                if(dataMapItem.getDataMap().getBoolean(KeyMap.KEY_INIT_TRANSACT)){
                    Intent i = new Intent(this, MainActivity.class);
                    i.putExtra(KeyMap.KEY_TRANSACTION_ID, dataMapItem.getDataMap().getString(KeyMap.KEY_TRANSACTION_ID));
                    startActivity(i);
                }else {
                    Toast.makeText(this, dataMapItem.getDataMap().getString(KeyMap.KEY_REASON), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
