package com.nowfloats.smartretailer.NetworkService;

import com.nowfloats.smartretailer.Models.BillingModel;
import com.nowfloats.smartretailer.Models.MainBillingModel;
import com.nowfloats.smartretailer.Models.Product;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.POST;


/**
 * Created by NowFloats on 03-01-2017.
 */

public interface IRetailingCall {

    @POST("/postProducts")
    void postPurchaseDetails(@Body MainBillingModel mainBillingModel, Callback<MainBillingModel.BillingResult> result);
}
