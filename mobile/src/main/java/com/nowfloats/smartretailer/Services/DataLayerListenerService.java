package com.nowfloats.smartretailer.Services;

import android.content.Intent;

import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by NowFloats on 12-01-2017.
 */

public class DataLayerListenerService extends WearableListenerService {

    private static final String START_TRANSACTION_PATH = "/start_transaction";

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        if(messageEvent.getPath().equals(START_TRANSACTION_PATH)){
            if(new String(messageEvent.getData()).equals("true")){
                Intent intent = new Intent(this, TransactionService.class);
                startService(intent);
            }
        }
    }

}
