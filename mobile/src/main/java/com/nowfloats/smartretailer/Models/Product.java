package com.nowfloats.smartretailer.Models;

import com.google.android.gms.wearable.PutDataMapRequest;
import com.nowfloats.smartretailer.Databases.AppDatabase;
import com.nowfloats.smartretailer.Utils.DataEvents;
import com.nowfloats.smartretailer.Utils.KeyMap;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ForeignKey;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.annotation.Unique;
import com.raizlabs.android.dbflow.structure.BaseModel;

/**
 * Created by NowFloats on 11-01-2017.
 */
@Table(database = AppDatabase.class)
public class Product extends BaseModel {
    @PrimaryKey(autoincrement = true)
    long id;

    @Column
    @Unique
    String productId;

    @Column
    String productName;

    @Column
    String productCurrency;

    @Column
    double productPrice;

    @Column
    double productDiscount;

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

    public String getProductCurrency() {
        return productCurrency;
    }

    public void setProductCurrency(String productCurrency) {
        this.productCurrency = productCurrency;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public double getProductDiscount() {
        return productDiscount;
    }

    public void setProductDiscount(double productDiscount) {
        this.productDiscount = productDiscount;
    }

    public PutDataMapRequest getDataMapRequestObject(){

        PutDataMapRequest putDataMapRequest = PutDataMapRequest.create(DataEvents.DATA_EVENT_PRODUCT).setUrgent();
        putDataMapRequest.getDataMap().putBoolean(KeyMap.KEY_IS_PRODUCT_AVAILABLE, true);
        putDataMapRequest.getDataMap().putString("productId", productId);
        putDataMapRequest.getDataMap().putString("productName", productName);
        putDataMapRequest.getDataMap().putString("productCurrency", productCurrency);
        putDataMapRequest.getDataMap().putDouble("productPrice", productPrice);
        putDataMapRequest.getDataMap().putDouble("productDiscount", productDiscount);
        putDataMapRequest.getDataMap().putLong(KeyMap.KEY_TIME, System.currentTimeMillis());

        return putDataMapRequest;
    }
}
