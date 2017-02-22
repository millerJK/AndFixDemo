package com.example.javris.andfix.application;

import android.app.Application;

import com.example.javris.andfix.util.PatchManagerUtil;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * Created by jarvis on 2017/2/13.
 */

public class MyApplication extends Application {

    PatchManagerUtil mPatchManagerUtil;

    @Override
    public void onCreate() {
        super.onCreate();
        initAndFix();
        initOkhttp();
    }

    private void initOkhttp() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .build();
        OkHttpUtils.initClient(okHttpClient);
    }


    /**
     * init andFix
     */
    public void initAndFix() {
        mPatchManagerUtil = PatchManagerUtil.getInstance(this);
        mPatchManagerUtil.initPatch(this);
    }
}
