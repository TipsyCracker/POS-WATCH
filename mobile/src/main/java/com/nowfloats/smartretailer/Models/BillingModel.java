package com.nowfloats.smartretailer.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.nowfloats.smartretailer.Adapters.AllProductsRvAdapter;

import java.util.ArrayList;

/**
 * Created by NowFloats on 15-01-2017.
 */

public class BillingModel {

        @SerializedName("ProductId")
        @Expose
        private String productId;
        @SerializedName("ProductName")
        @Expose
        private String productName;
        @SerializedName("DetailsLink")
        @Expose
        private String detailsLink;
        @SerializedName("ReviewsLink")
        @Expose
        private String reviewsLink;
        @SerializedName("Quantity")
        @Expose
        private Integer quantity;
        @SerializedName("NetPrice")
        @Expose
        private Double netPrice;

        public String getProductId() {
            return productId;
        }

        public void setProductId(String productId) {
            this.productId = productId;
        }

        public String getProductName() {
            return productName;
        }

        public void setProductName(String productName) {
            this.productName = productName;
        }

        public String getDetailsLink() {
            return detailsLink;
        }

        public void setDetailsLink(String detailsLink) {
            this.detailsLink = detailsLink;
        }

        public String getReviewsLink() {
            return reviewsLink;
        }

        public void setReviewsLink(String reviewsLink) {
            this.reviewsLink = reviewsLink;
        }

        public Integer getQuantity() {
            return quantity;
        }

        public void setQuantity(Integer quantity) {
            this.quantity = quantity;
        }

        public Double getNetPrice() {
            return netPrice;
        }

        public void setNetPrice(Double netPrice) {
            this.netPrice = netPrice;
        }


}
