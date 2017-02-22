package com.example.javris.andfix.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import com.example.javris.andfix.entity.VersionEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * 对话框仅仅用作app版本的更新提示，差异包更新是在后台自动下载的
 */
public class VersionUpdateManager {

    private static final String TAG = "VersionUpdateManager";

    private static final String APATCH_PATH = "out.apatch";

    private static final String APP_PATH = "new.apk";

    private static final String PATCH_VERSION_CODE = "patch_version";

    public static final int MESSAGE_UPDATE = 1;

    public static final int MESSAGE_APP_OVER = 2;

    public static final int MESSAGE_PATCH_OVER = 3;

    private Context mContext;

    private VersionEntity mVersionInfo;

    private String mRootDir;

    private String mSaveApkDirPath;

    private String mSavePatchDirPath;

    private static VersionUpdateManager mVersionUpdateManager;

    private SharedPreferences sp;

    private boolean isCancel = false;

    private boolean isAppUpdate = false;

    private AlertDialog.Builder mBuilder;

    private Dialog mVersionUpdateDialog;

    private ProgressDialog mProgressDialog;

    private Handler mHandler;


    private VersionUpdateManager(Context context, VersionEntity mVersionInfo, Handler handler) {
        this.mContext = context;
        this.mVersionInfo = mVersionInfo;
        this.mHandler = handler;
        sp = context.getSharedPreferences(PATCH_VERSION_CODE, Context.MODE_PRIVATE);
        createFileSavePath();
    }

    public static VersionUpdateManager getInstance(Context context, VersionEntity mVersionInfo, Handler handler) {
        if (mVersionUpdateManager == null) {
            mVersionUpdateManager = new VersionUpdateManager(context, mVersionInfo, handler);
        }
        return mVersionUpdateManager;
    }

    private void createFileSavePath() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            mRootDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            mRootDir = mRootDir + File.separator + "zhangfutong";
            mSaveApkDirPath = mRootDir + File.separator + "apk";
            FileUtil.createFolder(mSaveApkDirPath);
            mSavePatchDirPath = mRootDir + File.separator + "patch";
            FileUtil.createFolder(mSavePatchDirPath);
        } else
            Log.e(TAG, "sd is not found");

    }

    public void startTask() {

        Log.e(TAG, "start task");

        if (mVersionInfo != null) {
            return;
        }

        //只有在app不需要进行版本升级的时候才会检查补丁版本
        if (isAppUpdate = isAppUpdate(mVersionInfo)) {
            showVersionUpdateDialog(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    // TODO Auto-generated
                    showProgressDialog(new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                            isCancel = true;
                        }
                    });
                }
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.dismiss();
                }
            });
            //reset sharePreference patch version code
            setPatchVersionCode("0.0");
        } else if (isPatchUpdate(mVersionInfo)) {
            // TODO: 2017/2/15  patch download
            new Thread(downApkRunnable).start();
        }
    }


    /**
     * whether a app version upgrade is required
     *
     * @param version
     * @return
     */
    private boolean isAppUpdate(VersionEntity version) {

        if (version == null) {
            return false;
        }
        if (version.version_code == null || "".equals(version.version_code)) {
            return false;
        }
        PackageManager mPackManager = mContext.getPackageManager();
        try {
            PackageInfo info = mPackManager.getPackageInfo(mContext.getPackageName(), 0);
            if (compareVersion(version.version_code, info.versionName) >= 0) {
                Log.e(TAG, "******** No app version updates required ********");
                return false;
            } else {
                Log.e(TAG, "******** app version updates required ********");
                return true;
            }
        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 设置差异包版本号
     *
     * @param patchVersionCode
     */
    public void setPatchVersionCode(String patchVersionCode) {
        sp.edit().putString(PATCH_VERSION_CODE, patchVersionCode).commit();
    }

    /**
     * 获取差异包版本号
     *
     * @return
     */
    public String getPatchVersionCode() {
        String patchVersionCode = sp.getString(PATCH_VERSION_CODE, "0.0");
        return patchVersionCode;
    }

    /**
     * whether a patch version upgrade is required
     *
     * @param entity
     * @return
     */
    private boolean isPatchUpdate(VersionEntity entity) {

        String oldPatchVersion = getPatchVersionCode();
        Log.e(TAG, "oldPatchVersion :" + oldPatchVersion);
        if (entity == null
                || TextUtils.isEmpty(entity.version_patch)
                || TextUtils.isEmpty(oldPatchVersion))
            return false;

        if (compareVersion(oldPatchVersion, entity.version_patch) >= 0) {
            Log.e(TAG, "******** No patch updates required ********");
            return false;
        } else {
            Log.e(TAG, "******** patch updates required ********");
            return true;
        }

    }

    /**
     * whether Connect Wifi
     *
     * @return true success
     */
    private boolean isWifiNetWork() {

        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        String netWorkName = cm.getActiveNetworkInfo().getTypeName();
        if (netWorkName.equals("WIFI")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * patch and apk version update
     */
    private Runnable downApkRunnable = new Runnable() {
        @Override
        public void run() {

            String fileUrl;
            String savePath;

            if (isAppUpdate) {
                fileUrl = mVersionInfo.app_url;
                savePath = mSaveApkDirPath + File.separator + APP_PATH;
                Log.e(TAG, "start downloading APK  " + mVersionInfo.app_url);
            } else {
                fileUrl = mVersionInfo.patch_url;
                savePath = mSavePatchDirPath + File.separator + APATCH_PATH;
                Log.e(TAG, "start downloading Patch  " + mVersionInfo.patch_url);
            }

            Log.e(TAG, savePath);

            try {
                URL url = new URL(fileUrl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                int length = conn.getContentLength();
                InputStream is = conn.getInputStream();

                File ApkFile = new File(savePath);
                FileOutputStream fos = new FileOutputStream(ApkFile);
                int count = 0;
                byte[] buf = new byte[1024 * 5];

                do {
                    int numread = is.read(buf);
                    count += numread;
                    int progress = (int) (((float) count / length) * 100);
                    sendProgressMessage(progress);
                    Log.e(TAG, "downloading ..." + count + "/" + length + "   " + progress + "%");
                    if (numread <= 0) {
                        if (isAppUpdate) {
                            mProgressDialog.dismiss();
                            mHandler.sendEmptyMessage(MESSAGE_APP_OVER);
                            Log.e(TAG, "App download finished !!!!");
                        } else {
                            Message message = mHandler.obtainMessage();
                            message.what = MESSAGE_PATCH_OVER;
                            message.obj = savePath;
                            mHandler.sendMessage(message);
                            setPatchVersionCode(mVersionInfo.version_patch);
                            Log.e(TAG, "Patch download finished !!!!");
                        }
                        break;
                    }
                    fos.write(buf, 0, numread);
                    fos.flush();
                } while (!isCancel);
                fos.close();
                is.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    private void sendProgressMessage(int progress) {
        Message message = mHandler.obtainMessage();
        message.what = MESSAGE_UPDATE;
        message.obj = progress;
        mHandler.sendMessage(message);
    }


    /**
     * 显示更新对话框
     *
     * @param mPositionListener
     * @param mNegativeListener
     */
    private void showVersionUpdateDialog(

            DialogInterface.OnClickListener mPositionListener,
            DialogInterface.OnClickListener mNegativeListener) {
        if (mBuilder == null) {
            mBuilder = new AlertDialog.Builder(mContext);
            mBuilder.setTitle("版本更新");
            if (mVersionInfo.remark != null && !"".equals(mVersionInfo.remark)) {
                mBuilder.setMessage(mVersionInfo.remark);
            }
            if (mVersionInfo.version_type.equals("1")) {
                //提示升级
                mBuilder.setPositiveButton("立即更新", mPositionListener);
                mBuilder.setNegativeButton("稍后更新", mNegativeListener);
                mVersionUpdateDialog = mBuilder.show();
            } else if (mVersionInfo.version_type.equals("2")) {
                //强制升级
                mBuilder.setCancelable(false);
                mBuilder.setPositiveButton("立即更新", mPositionListener);
                mVersionUpdateDialog = mBuilder.show();
            } else if (mVersionInfo.version_type.equals("3")) {
                Log.e(TAG, "最新版本无需更新");
            }
        }
    }

    /**
     * 显示进度条对话框
     *
     * @param mNegativeListener
     */
    private void showProgressDialog(
            DialogInterface.OnClickListener mNegativeListener) {

        mVersionUpdateDialog.dismiss();
        //// TODO: 2017/2/15 app download
        new Thread(downApkRunnable).start();

        mProgressDialog = new ProgressDialog(mContext);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setTitle("版本正在更新中");
        if (mVersionInfo.version_type.equals("1")) {
            //提示升级
            mProgressDialog.setButton("取消", mNegativeListener);
        } else if (mVersionInfo.version_type.equals("2")) {
            //强制升级
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.show();
    }

    public void setProgress(int progress) {
        mProgressDialog.setProgress(progress);
    }

    public void startInstall() {
        installApk(mSaveApkDirPath + "/new.apk");
    }

    /**
     * intall apk
     */
    public void installApk(String saveFileName) {
        File apkfile = new File(saveFileName);
        if (!apkfile.exists()) {
            return;
        }
        try {
            unInstall();
            install(apkfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * uninstall the original application first
     */
    private void unInstall() {
        Uri uri = Uri.parse("package:" + mContext.getPackageName());
        Intent deleteIntent = new Intent();
        deleteIntent.setType(Intent.ACTION_DELETE);
        deleteIntent.setData(uri);
        mContext.startActivity(deleteIntent);
    }

    /**
     * install the new application second
     */
    private void install(File apkfile) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
                "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(i);

    }

    /**
     * 比较版本号的大小,前者大则返回一个正数,后者大返回一个负数,相等则返回0
     *
     * @param version1
     * @param version2
     * @return
     */
    public int compareVersion(String version1, String version2) {

        if (TextUtils.isEmpty(version1) || TextUtils.isEmpty(version2))
            return 0;

        String[] versionArray1 = version1.split("\\.");
        String[] versionArray2 = version2.split("\\.");
        int idx = 0;
        int minLength = Math.min(versionArray1.length, versionArray2.length);//取最小长度值
        int diff = 0;
        while (idx < minLength
                && (diff = versionArray1[idx].length() - versionArray2[idx].length()) == 0//先比较长度
                && (diff = versionArray1[idx].compareTo(versionArray2[idx])) == 0) {//再比较字符
            ++idx;
        }
        //如果已经分出大小，则直接返回，如果未分出大小，则再比较位数，有子版本的为大；
        diff = (diff != 0) ? diff : versionArray1.length - versionArray2.length;
        return diff;
    }
}
