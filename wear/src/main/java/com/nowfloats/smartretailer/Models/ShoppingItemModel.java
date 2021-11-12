package com.nowfloats.smartretailer.Models;

/**
 * Created by NowFloats on 11-01-2017.
 */

public class ShoppingItemModel {
    private String itemName;
    private String itemPrice;
    private String currencyCode;

    public ShoppingItemModel(String itemName, String itemPrice, String currencyCode) {
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.currencyCode = currencyCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
}
