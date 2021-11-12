package com.nowfloats.smartretailer.Adapters;

import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by NowFloats on 12-01-2017.
 */

public class ProductPagerAdapter extends PagerAdapter {

    private LayoutInflater mLayoutInflater;
    private List<Product> mList;

    public ProductPagerAdapter(LayoutInflater mLayoutInflater) {
        this.mLayoutInflater = mLayoutInflater;
    }

    public ProductPagerAdapter(LayoutInflater mLayoutInflater, List<Product> mProductList) {
        this.mLayoutInflater = mLayoutInflater;
        this.mList = mProductList;
    }

    @Override
    public int getCount() {

        if (mList != null && mList.size() > 0)
            return mList.size();
        return 0;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((LinearLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = mLayoutInflater.inflate(R.layout.activity_transaction_pager_item, container, false);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        view.setLayoutParams(layoutParams);

        Product product = mList.get(position);
        ((TextView) view.findViewById(R.id.tvName)).setText(product.getProductName());
        ((TextView) view.findViewById(R.id.tvQty)).setText(product.getProductId());
        ((TextView) view.findViewById(R.id.tvPrice)).setText(product.getProductPrice() + "");
        ((TextView) view.findViewById(R.id.tvDiscount)).setText(product.getProductDiscount() + "");

        container.addView(view);
        return view;
    }

    @Override
    public int getItemPosition(Object object){
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

    public void refreshList(Product product) {

        if (mList == null)
            mList = new ArrayList<>();

        mList.add(product);
        this.notifyDataSetChanged();
    }
}
