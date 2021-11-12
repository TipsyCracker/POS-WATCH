package com.nowfloats.smartretailer.Models;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MainBillingModel {

    @SerializedName("transaction_id")
    @Expose
    private String transactionId;
    @SerializedName("total_amount")
    @Expose
    private Double totalAmount;
    @SerializedName("date_and_time")
    @Expose
    private String dateAndTime;
    @SerializedName("processing_id")
    @Expose
    private String processingId;
    @SerializedName("fb_token")
    @Expose
    private String fbToken;
    @SerializedName("items_list")
    @Expose
    private List<ItemsList> itemsList = null;

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDateAndTime() {
        return dateAndTime;
    }

    public void setDateAndTime(String dateAndTime) {
        this.dateAndTime = dateAndTime;
    }

    public String getProcessingId() {
        return processingId;
    }

    public void setProcessingId(String processingId) {
        this.processingId = processingId;
    }

    public List<ItemsList> getItemsList() {
        return itemsList;
    }

    public void setItemsList(List<ItemsList> itemsList) {
        this.itemsList = itemsList;
    }

    public String getFbToken() {
        return fbToken;
    }

    public void setFbToken(String fbToken) {
        this.fbToken = fbToken;
    }

    public class ItemsList {

        @SerializedName("product_name")
        @Expose
        private String productName;
        @SerializedName("product_id")
        @Expose
        private String productId;
        @SerializedName("price")
        @Expose
        private Double price;
        @SerializedName("discount")
        @Expose
        private Double discount;

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public Double getPrice() {
            return price;
        }

        public void setPrice(Double price) {
            this.price = price;
        }

        public Double getDiscount() {
            return discount;
        }

        public void setDiscount(Double discount) {
            this.discount = discount;
        }

    }


    public class BillingResult{
        @SerializedName("processing_id")
        @Expose
        private String processingId;

        public String getProcessingId() {
            return processingId;
        }

        public void setProcessingId(String processingId) {
            this.processingId = processingId;
        }
    }


}
