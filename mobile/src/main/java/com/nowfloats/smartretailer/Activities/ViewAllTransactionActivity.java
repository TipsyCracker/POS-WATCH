package com.nowfloats.smartretailer.Activities;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.nowfloats.smartretailer.Adapters.AllProductsRvAdapter;
import com.nowfloats.smartretailer.Adapters.ViewAllTransactionsRvAdapter;
import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.Models.Product_Table;
import com.nowfloats.smartretailer.Models.TransactionData;
import com.nowfloats.smartretailer.Models.TransactionData_Table;
import com.nowfloats.smartretailer.Models.TransactionHistory;
import com.nowfloats.smartretailer.Models.TransactionHistory_Table;
import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.Utils;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class ViewAllTransactionActivity extends BaseActivity {

    private ViewAllTransactionsRvAdapter mTransactionRvAdapter;
    private List<TransactionHistory> mTransactionHistoryList = new ArrayList<>();


    @BindView(R.id.rvTransactionList)
    RecyclerView rvTransactionList;

    @Override
    public void initialize() {
        llContent.addView(mLayoutInflater.inflate(R.layout.activity_view_all_transaction, null));

        ButterKnife.bind(this);

        SQLite.select()
                .from(TransactionHistory.class)
                .orderBy(TransactionHistory_Table.id, false)
                .async()
                .queryListResultCallback(new QueryTransaction.QueryResultListCallback<TransactionHistory>() {
                    @Override
                    public void onListQueryResult(QueryTransaction transaction, @NonNull List<TransactionHistory> tResult) {
                        for (TransactionHistory trHistory : tResult) {
                            mTransactionHistoryList.add(trHistory);
                        }
                        mTransactionRvAdapter.notifyItemInserted(0);
                    }
                })
                .execute();

        mTransactionRvAdapter = new ViewAllTransactionsRvAdapter(this, mTransactionHistoryList);
        rvTransactionList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvTransactionList.setAdapter(mTransactionRvAdapter);
        mTransactionRvAdapter.setOnItemClickListener(new ViewAllTransactionsRvAdapter.OnItemClickListener() {
            @Override
            public void onClick(View v, int position) {
                showTransactionDetails(mTransactionHistoryList.get(position));
            }
        });

    }

    public void showTransactionDetails(TransactionHistory transactionHistory) {
        final List<Product> productList = new ArrayList<>();
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.transaction_history_by_id_layout);

        TextView tvTransactionId, tvDateAndTime, tvAmount, tvPaymentStatus;
        tvTransactionId = (TextView) dialog.findViewById(R.id.tvTransactionId);
        tvDateAndTime = (TextView) dialog.findViewById(R.id.tvDateAndTime);
        tvAmount = (TextView) dialog.findViewById(R.id.tvTotalAmount);
        tvPaymentStatus = (TextView) dialog.findViewById(R.id.tvPaymentStatus);

        tvTransactionId.setText(transactionHistory.getTransactionId());
        tvDateAndTime.setText(Utils.getFormattedDate(transactionHistory.getDateTimeStamp()));
        tvAmount.setText(transactionHistory.getTotalAmount() + "");
        tvPaymentStatus.setText(transactionHistory.getStatus() == 1 ? getString(R.string.paid) : getString(R.string.pending));

        RecyclerView rvItemList = (RecyclerView) dialog.findViewById(R.id.rvListOfItems);
        final AllProductsRvAdapter allProductsRvAdapter = new AllProductsRvAdapter(productList);
        rvItemList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvItemList.setAdapter(allProductsRvAdapter);

        SQLite.select(Product_Table.productId, Product_Table.productName, Product_Table.productPrice,
                Product_Table.productCurrency, Product_Table.productDiscount)
                .from(Product.class)
                .innerJoin(TransactionData.class)
                .on(Product_Table.productId.withTable().eq(TransactionData_Table.mainProductId))
                .where(TransactionData_Table.transactionId.is(transactionHistory.getTransactionId()))
                .async()
                .queryListResultCallback(new QueryTransaction.QueryResultListCallback<Product>() {
                    @Override
                    public void onListQueryResult(QueryTransaction transaction, @NonNull List<Product> tResult) {
                        Timber.d("Total number of products %d", tResult.size());
                        for (Product product : tResult) {
                            productList.add(product);
                        }
                        allProductsRvAdapter.notifyItemInserted(0);
                    }
                })
                .execute();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = dialog.getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        dialog.show();
        dialog.show();


    }


    //TODO: Later include 2 fragments and when a row is clicked

}
