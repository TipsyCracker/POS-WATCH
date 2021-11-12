package com.nowfloats.smartretailer.Adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by NowFloats on 12-01-2017.
 */

public class AllProductsRvAdapter extends RecyclerView.Adapter<AllProductsRvAdapter.ProductViewHolder> {

    List<Product> mProductList;

    public AllProductsRvAdapter(List<Product> productList){
        mProductList = productList;
    }

    @Override
    public ProductViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_row_layout, parent, false);
        return new ProductViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ProductViewHolder holder, int position) {
        Product product = mProductList.get(position);
        holder.tvProductName.setText(product.getProductName());
        holder.tvProductId.setText(product.getProductId());
        holder.tvProductPrice.setText(product.getProductCurrency() + " " + product.getProductPrice());
        holder.tvProductDiscount.setText(product.getProductCurrency() + " " + product.getProductDiscount());
    }

    @Override
    public int getItemCount() {
        return mProductList.size();
    }

    public class ProductViewHolder extends RecyclerView.ViewHolder{

        @BindView(R.id.tvProductName)
        TextView tvProductName;
        @BindView(R.id.tvProductId)
        TextView tvProductId;
        @BindView(R.id.tvProductPrice)
        TextView tvProductPrice;
        @BindView(R.id.tvProductDiscount)
        TextView tvProductDiscount;

        public ProductViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
