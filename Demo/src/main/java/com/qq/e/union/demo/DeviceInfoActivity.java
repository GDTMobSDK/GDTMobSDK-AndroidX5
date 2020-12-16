package com.qq.e.union.demo;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qq.e.comm.managers.status.DeviceStatus;

/**
 * 展示设备信息
 *
 * @author tysche
 * @version 2018.5.3
 */
public class DeviceInfoActivity extends Activity {

  private DeviceStatus deviceStatus;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    deviceStatus = new DeviceStatus(this.getApplicationContext());
    setContentView(createRootView());
  }

  private View createRootView() {
    LinearLayout layout = new LinearLayout(this);
    layout.setOrientation(LinearLayout.VERTICAL);

    layout.addView(createTextView("IMEI: " + deviceStatus.getDid()));
    layout.addView(createTextView("Android Id: " + deviceStatus.getUid()));
    return layout;
  }

  private TextView createTextView(String text) {
    TextView textView = new TextView(this);
    textView.setLayoutParams(new LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
      textView.setTextIsSelectable(true);
    }
    textView.setText(text);
    return textView;
  }
}
