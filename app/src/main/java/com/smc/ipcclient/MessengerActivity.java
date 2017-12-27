package com.smc.ipcclient;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

/**
 * @author shenmengchao
 * @version 1.0.0
 * @date 2017/12/25
 * @description
 */

public class MessengerActivity extends Activity {

    private final static int WHAT = 1;

    private Button mBtnBind, mBtnMessenger;
    private boolean mIsBindSuccess;
    private Messenger mServiceMessenger;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(MessengerActivity.this, "bindService成功", Toast.LENGTH_SHORT).show();
            mServiceMessenger = new Messenger(iBinder);
            mIsBindSuccess = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mServiceMessenger = null;
            mIsBindSuccess = false;
        }
    };

    private Handler mClientHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case WHAT:
                    Bundle serviceBundle = (Bundle) msg.obj;
                    String serviceStr = (String) serviceBundle.get("str");
                    Toast.makeText(MessengerActivity.this, "service 响应的结果是 ： " + serviceStr, Toast.LENGTH_SHORT).show();
                    Log.d("serviceStr", "service 响应的结果是 ： " + serviceStr);
                    break;
                default:
                    break;
            }
        }
    };

    private Messenger mClientMessenger = new Messenger(mClientHandler);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messenger);

        mBtnBind = (Button) findViewById(R.id.btn_bind);
        mBtnMessenger = (Button) findViewById(R.id.btn_messenger);

        mBtnBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindService();
            }
        });
        mBtnMessenger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsBindSuccess) {
                    Message msgClient = Message.obtain();
                    Bundle clientBundle = new Bundle();
                    clientBundle.putString("str", "我是client");
                    msgClient.what = WHAT;
                    msgClient.obj = clientBundle;
                    msgClient.replyTo = mClientMessenger;
                    try {
                        mServiceMessenger.send(msgClient);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(MessengerActivity.this, "请先bind serivce！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void bindService() {
        Intent intent = new Intent();
//        intent.setAction("com.arcvideo.aidlservice.MessengerService");
        intent.setComponent(new ComponentName("com.smc.ipcservice",
                "com.smc.ipcservice.MessengerService"));
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

//        final Intent intent = new Intent();
//        intent.setAction("com.arcvideo.aidlservice.MessengerService");
//        final Intent eintent = new Intent(createExplicitFromImplicitIntent(this, intent));
//        bindService(eintent, mConnection, Service.BIND_AUTO_CREATE);
    }

    public static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        // Retrieve all services that can match the given intent
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> resolveInfo = pm.queryIntentServices(implicitIntent, 0);

        // Make sure only one match was found
        if (resolveInfo == null || resolveInfo.size() != 1) {
            return null;
        }

        // Get component info and create ComponentName
        ResolveInfo serviceInfo = resolveInfo.get(0);
        String packageName = serviceInfo.serviceInfo.packageName;
        String className = serviceInfo.serviceInfo.name;
        ComponentName component = new ComponentName(packageName, className);

        // Create a new intent. Use the old one for extras and such reuse
        Intent explicitIntent = new Intent(implicitIntent);

        // Set the component to be explicit
        explicitIntent.setComponent(component);

        return explicitIntent;
    }
}
