package com.nowfloats.smartretailer.Adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.nowfloats.smartretailer.Models.TransactionHistory;
import com.nowfloats.smartretailer.R;
import com.nowfloats.smartretailer.Utils.Utils;

import org.w3c.dom.Text;

import java.util.List;

/**
 * Created by NowFloats on 15-01-2017.
 */

public class ViewAllTransactionsRvAdapter extends RecyclerView.Adapter<ViewAllTransactionsRvAdapter.AllTransactionViewHolder> {


    private List<TransactionHistory> mTransactionHistoryList;
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;

    public ViewAllTransactionsRvAdapter(Context context, List<TransactionHistory> transactionHistoryList){
        this.mTransactionHistoryList = transactionHistoryList;
        this.mContext = context;
    }

    public void setOnItemClickListener(OnItemClickListener itemClickListener){
        this.mOnItemClickListener = itemClickListener;
    }

    @Override
    public AllTransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.transaction_row_layout, parent, false);
        return new AllTransactionViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AllTransactionViewHolder holder, final int position) {
        TransactionHistory transactionData = mTransactionHistoryList.get(position);
        holder.tvTransactionId.setText(transactionData.getTransactionId());
        holder.tvdateAndtime.setText(Utils.getFormattedDate(transactionData.getDateTimeStamp()));
        holder.tvTotalAmount.setText(mContext.getString(R.string.rs) + transactionData.getTotalAmount());
        holder.tvPaymentStatus.setText(transactionData.getStatus()==1 ? mContext.getString(R.string.paid):mContext.getString(R.string.pending));
        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnItemClickListener.onClick(v, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mTransactionHistoryList.size();
    }

    public class AllTransactionViewHolder extends RecyclerView.ViewHolder{

        TextView tvTransactionId, tvdateAndtime, tvTotalAmount, tvPaymentStatus;
        View view;

        public AllTransactionViewHolder(View itemView) {
            super(itemView);

            tvTransactionId = (TextView) itemView.findViewById(R.id.tvTransactionId);
            tvdateAndtime = (TextView) itemView.findViewById(R.id.tvDateAndTime);
            tvTotalAmount = (TextView) itemView.findViewById(R.id.tvTotalAmount);
            tvPaymentStatus = (TextView) itemView.findViewById(R.id.tvIsPaid);
            view = itemView;
        }
    }

    public interface OnItemClickListener{
        public void onClick(View v, int position);
    }
}
