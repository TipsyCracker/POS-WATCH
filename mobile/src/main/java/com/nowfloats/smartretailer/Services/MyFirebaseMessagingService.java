package com.nowfloats.smartretailer.Services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.nowfloats.smartretailer.HelperClasses.NotificationHelper;
import com.nowfloats.smartretailer.Models.TransactionHistory;
import com.nowfloats.smartretailer.Models.TransactionHistory_Table;
import com.raizlabs.android.dbflow.sql.language.SQLite;

/**
 * Created by NowFloats on 18-01-2017.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        NotificationHelper.showNotification(this, "Payment Successful",
                "Payment Successfull for transaction Id: " +
                        remoteMessage.getData().get("transaction_id"),
                true, null, 15 );

        SQLite.update(TransactionHistory.class)
                .set(TransactionHistory_Table.status.eq(1))
                .where(TransactionHistory_Table.transactionId.is(remoteMessage.getData().get("transaction_id")))
                .async()
                .execute();
    }
}
