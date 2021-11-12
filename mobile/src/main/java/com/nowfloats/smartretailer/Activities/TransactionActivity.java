package com.nowfloats.smartretailer.Activities;

import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.nowfloats.smartretailer.Adapters.ProductPagerAdapter;
import com.nowfloats.smartretailer.HelperClasses.BluetoothSPP;
import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.Models.Product_Table;
import com.nowfloats.smartretailer.R;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class TransactionActivity extends BaseActivity implements BluetoothSPP.OnDataReceivedListener {

    @BindView(R.id.rl_connecting)
    RelativeLayout rlConnecting;

    @BindView(R.id.vp_items)
    ViewPager vpTransactions;

    @BindView(R.id.llConfirm)
    LinearLayout llConfirm;

    private ProductPagerAdapter mProductPagerAdapter;

    private List<Product> mProductList = new ArrayList<>();

    @Override
    public void initialize() {
        llContent.addView(mLayoutInflater.inflate(R.layout.activity_transaction, null));
        ButterKnife.bind(this);
        mProductPagerAdapter = new ProductPagerAdapter(mLayoutInflater, mProductList);
        connectScanner(this);
    }

    @Override
    public void onDeviceConnected(String name, String address) {
        super.onDeviceConnected(name, address);
        rlConnecting.setVisibility(View.GONE);
        vpTransactions.setVisibility(View.VISIBLE);
        llConfirm.setVisibility(View.VISIBLE);
    }


    @Override
    public void onDataReceived(byte[] data, String message) {
        SQLite.select()
                .from(Product.class)
                .where(Product_Table.productId.is(message.trim()))
                .async()
                .querySingleResultCallback(new QueryTransaction.QueryResultSingleCallback<Product>() {
                    @Override
                    public void onSingleQueryResult(QueryTransaction transaction, @Nullable final Product product) {
                        Timber.d("Product Query Callback");
                        mProductList.add(product);
                        mProductPagerAdapter.refreshList(product);
//                        mProductPagerAdapter.notifyDataSetChanged();
                    }
                }).execute();
    }
}
