package com.xcv58.automatic.rest;

import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

/**
 * Created by xcv58 on 12/23/15.
 */
public class ServiceFactory {
    public static <T> T createRetrofitService(final Class<T> clazz, final String endPoint) {
        final Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(endPoint)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(clazz);
    }
}
