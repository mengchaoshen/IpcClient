package com.smc.ipcclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.smc.aidl.CalculateAidl;

public class AidlActivity extends AppCompatActivity {

    private Button mBtnBind, mBtnCalculate;

    private CalculateAidl mCalculateAidl;
    private boolean mIsBindSuccess;

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Toast.makeText(AidlActivity.this, "bind Service 成功", Toast.LENGTH_SHORT).show();
            mCalculateAidl = CalculateAidl.Stub.asInterface(iBinder);
            mIsBindSuccess = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mCalculateAidl = null;
            mIsBindSuccess = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aidl);

        mBtnBind = (Button) findViewById(R.id.btn_bind);
        mBtnCalculate = (Button) findViewById(R.id.btn_calculate);

        mBtnBind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bindService();
            }
        });
        mBtnCalculate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mIsBindSuccess) {
                    try {
                        int result = mCalculateAidl.add(1, 2);
                        Toast.makeText(AidlActivity.this, "result = " + result, Toast.LENGTH_SHORT).show();
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(AidlActivity.this, "请先bind serivce！", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void bindService() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.smc.ipcservice",
                "com.smc.ipcservice.CalculateService"));
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }
}
