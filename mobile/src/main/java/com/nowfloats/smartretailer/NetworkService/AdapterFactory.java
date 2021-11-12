package com.nowfloats.smartretailer.NetworkService;

import com.nowfloats.smartretailer.Utils.Constants;

import retrofit.RestAdapter;
import retrofit.android.AndroidLog;

/**
 * Created by NowFloats on 03-01-2017.
 */

/**
 * Factory for creating Retrofit network Adapters
 */
public class AdapterFactory {
    /**
     *
     * @param clazz
     * @param endPoint
     * @param <T>
     * @return
     * Takes class and the end point as input and return the Retrofit build adapter class
     */
    static <T> T createAdapter(final Class<T> clazz, final String endPoint){
        RestAdapter retrofit = new RestAdapter.Builder()
                .setEndpoint(endPoint)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setLog(new AndroidLog("Retrofit Response"))
                .build();
        T service = retrofit.create(clazz);
        return service;
    }

    /**
     *
     * @return IRetailingCall
     * Returns IRetailingCall(Which is a store Network Adapter)
     */
    public static IRetailingCall getStoreApiAdapter(){
        return createAdapter(IRetailingCall.class, Constants.API_URL);
    }
}
