package com.nowfloats.smartretailer.Activities;

import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

import com.nowfloats.smartretailer.Adapters.AllProductsRvAdapter;
import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.R;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ViewAllProductsActivity extends BaseActivity {

    @BindView(R.id.etSearchProduct)
    EditText etSearchProduct;
    @BindView(R.id.rvProductList)
    RecyclerView rvProductList;

    private List<Product> mProductList = new ArrayList<>();
    private AllProductsRvAdapter mAllProductsRvAdapter;

    @Override
    public void initialize() {
        llContent.addView(mLayoutInflater.inflate(R.layout.activity_view_all_products, null));
        ButterKnife.bind(this);
        SQLite.select()
                .from(Product.class)
                .async()
                .queryListResultCallback(new QueryTransaction.QueryResultListCallback<Product>() {
                    @Override
                    public void onListQueryResult(QueryTransaction transaction, @NonNull List<Product> tResult) {
                        if (tResult != null) {
                            for (Product product : tResult) {
                                mProductList.add(product);
                            }
                            mAllProductsRvAdapter.notifyDataSetChanged();
                        }
                    }
                })
                .execute();
        mAllProductsRvAdapter = new AllProductsRvAdapter(mProductList);
        rvProductList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rvProductList.setAdapter(mAllProductsRvAdapter);
    }

    @OnClick(R.id.ivSearchProduct)
    public void searchProduct(View v) {

    }
}
