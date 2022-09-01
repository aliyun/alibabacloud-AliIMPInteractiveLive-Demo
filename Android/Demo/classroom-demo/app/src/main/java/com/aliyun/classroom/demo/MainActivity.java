package com.aliyun.classroom.demo;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.classroom.lib.core.ClassPrototype;
import com.aliyun.roompaas.uibase.listener.SimpleTextWatcher;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private String classId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((EditText) findViewById(R.id.userId)).addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                userId = s.toString().trim();
            }
        });
        ((EditText) findViewById(R.id.classId)).addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                classId = s.toString().trim();
            }
        });

        findViewById(R.id.enterClass).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                init(userId);

                if (TextUtils.isEmpty(classId)) {
                    toast("课堂ID不能为空");
                } else {
                    setUp(classId);
                }
            }
        });
    }

    private void init(String uid) {
        String userId = !TextUtils.isEmpty(uid) ? uid : ClassConfig.acquireUserId(this);
        ClassPrototype.INSTANCE.init(this, ClassConfig.asClassInitParam(userId), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                toast("INSTANCE.init onSuccess: " + data);
            }

            @Override
            public void onError(String errorMsg) {
                toast("INSTANCE.init onError: " + errorMsg);
            }
        });
    }

    private void setUp(@NonNull String classId) {
        Context context = this;
        ClassPrototype.OpenClassParam classParam = new ClassPrototype.OpenClassParam();

        classParam.classId = classId;
        ClassPrototype.INSTANCE.setup(context, classParam, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                toast("setUp onSuccess: " + data);
            }

            @Override
            public void onError(String errorMsg) {
                toast("setUp onError: " + errorMsg);
            }
        });
    }

    private void toast(String str) {
        Log.i(TAG, str);
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}