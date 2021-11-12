package com.nowfloats.smartretailer.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.nowfloats.smartretailer.Models.Product;
import com.nowfloats.smartretailer.Models.ShoppingItemModel;
import com.nowfloats.smartretailer.R;

import java.util.List;

/**
 * Created by NowFloats on 11-01-2017.
 */

public class ShoppingListRvAdapter extends RecyclerView.Adapter<ShoppingListRvAdapter.ShoppingListViewHolder> {

    private final List<Product> mShoppingItemList;
    private OnItemNumberUpdate mItemUpdater;
    private ItemRemoveCallback mItemRemoveCallback;
    private OnItemClickListener mOnItemClickListener;

    public ShoppingListRvAdapter(List<Product> mShoppingItemList, OnItemNumberUpdate onItemNumberUpdate) {
        this.mShoppingItemList = mShoppingItemList;
        this.mItemUpdater = onItemNumberUpdate;
    }

    public void setmItemRemoveCallback(ItemRemoveCallback callback){
        this.mItemRemoveCallback = callback;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener){
        mOnItemClickListener = itemClickListener;
    }

    @Override
    public ShoppingListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.shop_list_row_layout, parent, false);
        return new ShoppingListViewHolder(v);
    }

    //TODO: enable changing productQuantity and product Currency
    @Override
    public void onBindViewHolder(ShoppingListViewHolder holder, final int position) {
        mItemUpdater.onUpdate(holder.getAdapterPosition(), holder.getLayoutPosition());
        final Product product = mShoppingItemList.get(position);
        holder.tvItemName.setText(product.getProductName());
        holder.tvItemQuantity.setText(product.getQuantity()+"");
        holder.tvItemUnitPrice.setText(product.getProductPrice()+"");
        holder.tvItemDiscount.setText(product.getProductDiscount()+"");
        holder.tvItemNetPrice.setText((product.getProductPrice()-product.getProductDiscount())*product.getQuantity() + "");

        holder.ivItemRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ShoppingAdapter", "Item Remove Clicked " + position);
                mItemRemoveCallback.onItemRemoved(position, product.getProductId());

            }
        });

        holder.ivItemIncreaseCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ShoppingAdapter", "Item Increase Clicked " + position);
            }
        });

        holder.ivItemReduceCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("ShoppingAdapter", "Item Reduce Clicked " + position);
            }
        });

        holder.mainView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onItemClick(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mShoppingItemList.size();
    }

    public class ShoppingListViewHolder extends RecyclerView.ViewHolder{

        TextView tvItemName, tvItemUnitPrice, tvItemDiscount, tvItemNetPrice, tvItemQuantity;
        ImageView ivItemRemove, ivItemIncreaseCount, ivItemReduceCount;
        View mainView;

        public ShoppingListViewHolder(View itemView) {
            super(itemView);

            tvItemName = (TextView) itemView.findViewById(R.id.tvItemName);
            tvItemUnitPrice = (TextView) itemView.findViewById(R.id.tvItemUnitPrice);
            tvItemDiscount = (TextView) itemView.findViewById(R.id.tvItemDiscount);
            tvItemNetPrice = (TextView) itemView.findViewById(R.id.tvItemNetPrice);
            tvItemQuantity = (TextView) itemView.findViewById(R.id.tvItemQuantity);

            ivItemRemove = (ImageView) itemView.findViewById(R.id.ivItemRemove);
            ivItemIncreaseCount = (ImageView) itemView.findViewById(R.id.ivItemIncreaseCount);
            ivItemReduceCount = (ImageView) itemView.findViewById(R.id.ivItemReduceCount);

            mainView = itemView;

        }
    }


    public interface OnItemNumberUpdate{
        void onUpdate(int currentItem, int totalItem);
    }
    public interface ItemRemoveCallback{
        void onItemRemoved(int position, String productId);
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
    }


}
