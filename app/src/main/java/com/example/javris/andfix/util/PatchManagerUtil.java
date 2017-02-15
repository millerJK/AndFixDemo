package com.example.javris.andfix.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alipay.euler.andfix.patch.PatchManager;

import java.io.IOException;


public class PatchManagerUtil {

    private static final String TAG = "PatchManagerUtil";
    private static PatchManagerUtil mPatchManagerUtil;
    private PatchManager mPatchManager;

    private PatchManagerUtil(Context context) {
        mPatchManager = new PatchManager(context);
    }

    /**
     * Initialize PatchManager,
     *
     * @param context
     * @return
     */
    public static PatchManagerUtil getInstance(Context context) {

        if (mPatchManagerUtil == null)
            synchronized (PatchManagerUtil.class) {
                if (mPatchManagerUtil == null)
                    mPatchManagerUtil = new PatchManagerUtil(context);
            }
        return mPatchManagerUtil;
    }

    /**
     * @return object PatchManager
     */
    public PatchManager getPatchManager() {
        if (mPatchManager != null)
            return mPatchManager;
        return null;
    }

    /**
     * You should load patch as early as possible, generally,
     * in the initialization phase of your application(such as Application.onCreate()).
     *
     * @Param context
     */
    public void initPatch(Context context) {

        if (mPatchManager == null)
            throw new NullPointerException("please call getInstance() method first !!!");
        else {
            mPatchManager.init(getAppVersion(context));
            mPatchManager.loadPatch();
        }

    }

    /**
     * @param path The file path, which suffix is .patch
     */
    public void addPatch(String path) {

        if (mPatchManager == null)
            throw new NullPointerException("please call getInstance() method first !!!");
        else
            try {
                Log.e(TAG, "mPatchManager add patch " + path + " right now !");
                mPatchManager.removeAllPatch();
                mPatchManager.addPatch(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * get app version
     *
     * @param context
     * @return
     */
    public String getAppVersion(Context context) {

        try {
            PackageInfo appVersion = context
                    .getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Log.e(TAG, appVersion.versionName);
            return appVersion.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
