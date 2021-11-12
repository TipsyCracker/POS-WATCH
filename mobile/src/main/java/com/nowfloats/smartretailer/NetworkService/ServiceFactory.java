package com.nowfloats.smartretailer.NetworkService;

import android.content.Context;



import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by NowFloats on 03-01-2017.
 */

public class ServiceFactory {

    private Context mContext;
    private static ServiceFactory sServiceFactory;

    public static ServiceFactory getInstance(Context context){
        if(sServiceFactory==null){
            sServiceFactory = new ServiceFactory(context.getApplicationContext());
            return sServiceFactory;
        }else {
            return sServiceFactory;
        }
    }

    private ServiceFactory(Context context){
        this.mContext = context;
    }

    public void getPaymentInvoiceNumber(){

    }

    public interface IResultCallBack<T>{
        void onSuccess(T t);
        void onError(Throwable error);
    }
}
