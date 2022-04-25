package com.aliyun.imp.flutter.sdk.alicloud_impinteraction_sdk_example;

import io.flutter.embedding.android.FlutterActivity;

import androidx.annotation.NonNull;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import com.aliyun.imp.flutter.sdk.alicloud_impinteraction_sdk.NativeViewFactory;

public class MainActivity extends FlutterActivity {

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
      super.configureFlutterEngine(flutterEngine);
      flutterEngine
              .getPlatformViewsController()
              .getRegistry()
              .registerViewFactory("<platform-view-type>", new NativeViewFactory());
    }
}
