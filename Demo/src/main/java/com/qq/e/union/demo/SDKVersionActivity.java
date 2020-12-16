package com.qq.e.union.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

import com.qq.e.comm.managers.status.SDKStatus;

/**
 * 版本号展示 Activity
 *
 * @author chonggao
 * Date 2019/11/15
 */
public class SDKVersionActivity extends Activity {
  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_sdk_version);
    ((TextView) findViewById(R.id.sdk_version_txt))
        .setText("SDk version is : " + SDKStatus.getIntegrationSDKVersion());
  }
}
