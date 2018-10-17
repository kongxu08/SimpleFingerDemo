package com.example.administrator.myapplication;

import android.hardware.fingerprint.FingerprintManager;
import android.os.Handler;
import android.os.Message;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.support.v4.os.CancellationSignal;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "tag";
    private FingerprintManagerCompat manager;
    private CancellationSignal cancel;
    private FingerprintManagerCompat.AuthenticationCallback callback;
    private MyHandler handler = new MyHandler(this);

    private TextView tv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.mytv);
        tv.setText("请将手指放在指纹传感器上");

        cancel= new CancellationSignal();

        manager = FingerprintManagerCompat.from(this);
        if (!manager.isHardwareDetected()) {
            //是否支持指纹识别
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("没有传感器");
            builder.setCancelable(true);
            builder.create().show();
        } else if (!manager.hasEnrolledFingerprints()) {
            //是否已注册指纹
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("没有注册指纹");
            builder.setCancelable(true);
            builder.create().show();
        } else {
            try {
                callback = new FingerprintManagerCompat.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errMsgId, CharSequence errString) {
                        super.onAuthenticationError(errMsgId, errString);
                        //验证错误时，回调该方法。当连续验证5次错误时，将会走onAuthenticationFailed()方法
                        handler.obtainMessage(1,errMsgId,0).sendToTarget();
                    }

                    @Override
                    public void onAuthenticationSucceeded(FingerprintManagerCompat.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        //验证成功时，回调该方法。fingerprint对象不能再验证
                        handler.obtainMessage(2).sendToTarget();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        //验证失败时，回调该方法。fingerprint对象不能再验证并且需要等待一段时间才能重新创建指纹管理对象进行验证
                        handler.obtainMessage(3).sendToTarget();
                    }
                };

                //这里去新建一个结果的回调，里面回调显示指纹验证的信息
                manager.authenticate(null, 0, cancel, callback, handler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }



    static class MyHandler extends Handler {
        WeakReference<MainActivity> mActivity;
        MyHandler(MainActivity activity){
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            if(activity!=null){
                //todo 逻辑处理
                switch (msg.what) {
                    case 1:   //验证错误
                        //todo 界面处理
                        activity.handleErrorCode(msg.arg1);
                        break;
                    case 2:   //验证成功
                        //todo 界面处理
                        activity.handleCode(200);
                        break;
                    case 3:    //验证失败
                        //todo 界面处理
                        activity.handleCode(500);
                        break;
                    default:
                        super.handleMessage(msg);
                }
            }

        };
    }

    //对应不同的错误，可以有不同的操作
    private void handleErrorCode(int code) {
        switch (code) {
            case FingerprintManager.FINGERPRINT_ERROR_CANCELED:
                //todo 指纹传感器不可用，该操作被取消
                Log.i(TAG,"指纹传感器不可用，该操作被取消");
                tv.setText("指纹传感器不可用，该操作被取消");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE:
                //todo 当前设备不可用，请稍后再试
                Log.i(TAG,"当前设备不可用，请稍后再试");
                tv.setText("当前设备不可用，请稍后再试");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_LOCKOUT:
                //todo 由于太多次尝试失败导致被锁，该操作被取消
                Log.i(TAG,"由于太多次尝试失败导致被锁，该操作被取消");
                tv.setText("由于太多次尝试失败导致被锁，该操作被取消");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_NO_SPACE:
                //todo 没有足够的存储空间保存这次操作，该操作不能完成
                Log.i(TAG,"没有足够的存储空间保存这次操作，该操作不能完成");
                tv.setText("没有足够的存储空间保存这次操作，该操作不能完成");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_TIMEOUT:
                //todo 操作时间太长，一般为30秒
                Log.i(TAG,"指纹传感器超时");
                tv.setText("指纹传感器超时");
                break;
            case FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                //todo 传感器不能处理当前指纹图片
                Log.i(TAG,"传感器不能处理当前指纹图片");
                tv.setText("传感器不能处理当前指纹图片");
                break;
        }
    }

    //对应不同的错误，可以有不同的操作
    private void handleCode(int code) {
        switch (code) {
            case 500:
                //todo 指纹传感器不可用，该操作被取消
                Log.i(TAG,"验证失败");
                tv.setText("验证失败");
                break;
            case 200:
                //todo 当前设备不可用，请稍后再试
                Log.i(TAG,"验证成功");
                tv.setText("验证成功");
                break;
        }
    }
}
