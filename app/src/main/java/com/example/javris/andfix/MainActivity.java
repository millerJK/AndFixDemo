package com.example.javris.andfix;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.euler.andfix.patch.PatchManager;
import com.example.javris.andfix.entity.VersionEntity;
import com.example.javris.andfix.util.PatchManagerUtil;
import com.example.javris.andfix.util.VersionUpdateManager;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

import static com.example.javris.andfix.util.VersionUpdateManager.MESSAGE_PATCH_OVER;

/**
 * Patch is placed in the root directory
 */
public class MainActivity extends AppCompatActivity {

    public static final String BASE_URL = "http://172.27.35.1:8080";//change you ip here
    private TextView mTextView;
    private Button mBtn;
    private VersionUpdateManager mVersionUpdateManager;
    private VersionEntity mEntity;
    private PatchManager mPatchManager;
    private PatchManagerUtil mPatchManagerUtil;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case VersionUpdateManager.MESSAGE_APP_OVER:
                    mVersionUpdateManager.startInstall();
                    break;
                case MESSAGE_PATCH_OVER:
                    mPatchManagerUtil.addPatch(msg.obj.toString());
                    break;
                case VersionUpdateManager.MESSAGE_UPDATE:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.main_tv);
        mBtn = (Button) findViewById(R.id.main_bt);
        mPatchManagerUtil = PatchManagerUtil.getInstance(getApplicationContext());
        mPatchManager = mPatchManagerUtil.getPatchManager();
        checkUpdate();
        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                test();
            }
        });
    }

    private void checkUpdate() {

        String url = BASE_URL + "/MySpringWeb/mvc/getVersion";
        Log.e("check update url", url);

        OkHttpUtils
                .get()
                .url(url)
                .build()
                .execute(new StringCallback() {
                    @Override
                    public void onError(Call call, Exception e, int id) {
                    }

                    @Override
                    public void onResponse(String response, int id) {
                        Log.e("json from server", response);
                        dealData(response);
                    }
                });
    }

    private void dealData(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            String version_code = jsonObject.getString("version_code");
            String version_patch = jsonObject.getString("version_patch");
            String remark = jsonObject.getString("remark");
            String version_type = jsonObject.getString("version_type");
            String app_url = BASE_URL + jsonObject.getString("app_url");
            String patch_url = BASE_URL + jsonObject.getString("patch_url");

            mEntity = new VersionEntity(app_url, patch_url, version_code, version_patch, remark, version_type);
            Log.e("append url with ip", mEntity.toString());
            mVersionUpdateManager = VersionUpdateManager.getInstance(MainActivity.this, mEntity, mHandler);
            mVersionUpdateManager.startTask();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void test() {
        Toast.makeText(this, "this is origin bug apk", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

}
