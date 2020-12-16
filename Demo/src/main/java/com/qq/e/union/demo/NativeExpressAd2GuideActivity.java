package com.qq.e.union.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.qq.e.ads.nativ.express2.VideoOption2;
import com.qq.e.union.demo.adapter.PosIdArrayAdapter;

import static com.qq.e.union.demo.Constants.VIDEO_DURATION_SETTING_MAX;
import static com.qq.e.union.demo.Constants.VIDEO_DURATION_SETTING_MIN;


public class NativeExpressAd2GuideActivity extends Activity implements
    CompoundButton.OnCheckedChangeListener, AdapterView.OnItemSelectedListener {

  private CheckBox btnNoOption;
  private CheckBox btnMute;
  private CheckBox btnDetailMute;
  private Spinner networkSpinner;
  private EditText posIdEdt;

  private Spinner spinner;
  private PosIdArrayAdapter arrayAdapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_express_2_ad);

    posIdEdt = findViewById(R.id.posId);

    spinner = findViewById(R.id.id_spinner);
    arrayAdapter = new PosIdArrayAdapter(this, android.R.layout.simple_spinner_item,
        getResources().getStringArray(R.array.native_express_2_ad));
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(this);

    btnNoOption = findViewById(R.id.cb_none_video_option);
    btnNoOption.setOnCheckedChangeListener(this);
    btnMute = findViewById(R.id.btn_mute);
    btnDetailMute = findViewById(R.id.btn_detail_mute);
    networkSpinner = findViewById(R.id.spinner_network);
  }


  private String getPosID() throws IllegalArgumentException {
    String posId = ((EditText) findViewById(R.id.posId)).getText().toString();
    if (TextUtils.isEmpty(posId)) {
      throw new IllegalArgumentException("Express2.0 posId should not be null");
    }
    return posId;
  }

  private int getMinVideoDuration() {
    if (((CheckBox) findViewById(R.id.cbMinVideoDuration)).isChecked()) {
      try {
        int rst = Integer
            .parseInt(((EditText) findViewById(R.id.etMinVideoDuration)).getText().toString());
        if (rst > 0) {
          return rst;
        } else {
          Toast.makeText(getApplicationContext(), "最小视频时长输入须大于0!", Toast.LENGTH_LONG).show();
        }
      } catch (NumberFormatException e) {
        Toast.makeText(getApplicationContext(), "最小视频时长输入不是整数!", Toast.LENGTH_LONG).show();
      }
    }
    return 0;
  }

  private int getMaxVideoDuration() {
    if (((CheckBox) findViewById(R.id.cbMaxVideoDuration)).isChecked()) {
      try {
        int rst = Integer
            .parseInt(((EditText) findViewById(R.id.etMaxVideoDuration)).getText().toString());
        if (rst >= VIDEO_DURATION_SETTING_MIN && rst <= VIDEO_DURATION_SETTING_MAX) {
          return rst;
        } else {
          Toast.makeText(getApplicationContext(), "最大视频时长输入不在有效区间内!", Toast.LENGTH_LONG).show();
        }
      } catch (NumberFormatException e) {
        Toast.makeText(getApplicationContext(), "最大视频时长输入不是整数!", Toast.LENGTH_LONG).show();
      }
    }
    return 0;
  }

  public void onNormalViewClicked(View view) {
    try {
      Intent intent = new Intent();
      intent.setClass(this, NativeExpressAd2SimpleDemoActivity.class);
      putExtraToIntent(intent);
      startActivity(intent);
    } catch (Exception e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  public void onRecyclerViewClicked(View view) {
    try {
      Intent intent = new Intent();
          intent.setClass(this, NativeExpressAd2RecyclerViewActivity.class);
      putExtraToIntent(intent);
      startActivity(intent);
    } catch (Exception e) {
      Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
    }
  }

  private void putExtraToIntent(Intent intent) {
    intent.putExtra(Constants.POS_ID, getPosID());
    intent.putExtra(Constants.MIN_VIDEO_DURATION, getMinVideoDuration());
    intent.putExtra(Constants.MAX_VIDEO_DURATION, getMaxVideoDuration());
    int loadAdCount = getLoadAdCount();
    if (loadAdCount > 0) {
      intent.putExtra(Constants.LOAD_AD_COUNT, loadAdCount);
    }
    if (btnNoOption.isChecked()) {
      intent.putExtra(Constants.NONE_OPTION, true);
    } else {
      intent.putExtra(Constants.PLAY_MUTE, btnMute.isChecked());
      intent.putExtra(Constants.PLAY_NETWORK, networkSpinner.getSelectedItemPosition());
      intent.putExtra(Constants.DETAIL_PAGE_MUTED, btnDetailMute.isChecked());
    }
  }

  private int getLoadAdCount() {
    EditText editTextAdCount = findViewById(R.id.et_ad_count);
    String text = editTextAdCount.getText().toString().trim();
    if (TextUtils.isEmpty(text)) {
      return 0;
    } else {
      return Integer.parseInt(text);
    }
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (buttonView == btnNoOption) {
      networkSpinner.setEnabled(!isChecked);
      btnMute.setEnabled(!isChecked);
      btnDetailMute.setEnabled(!isChecked);
    }
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    arrayAdapter.setSelectedPos(position);
    posIdEdt
        .setText(getResources().getStringArray(R.array.native_express_2_ad_value)[position]);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }

  public static int getLoadAdCount(Intent intent, int defaultValue) {
    if (intent == null) {
      return defaultValue;
    } else {
      return intent.getIntExtra(Constants.LOAD_AD_COUNT, defaultValue);
    }
  }

  /**
   * 根据引导页的配置项对视频广告进行配置如下参数
   *   AutoPlayPolicy:（1）WiFi下自动播放--默认条件（2）总是自动播放（3）从不自动播放
   *   PlayMuted: 静音播放
   *   DetailPageMuted：视频详情页静音播放
   *   MaxVideoDuration：可接受最长视频长度
   *   MinVideoDuration：可接受最短视频长度
   */
  public static VideoOption2 getVideoOption(Intent intent) {
    if (intent == null) {
      return null;
    }
    VideoOption2 videoOption = null;
    boolean noneOption = intent.getBooleanExtra(Constants.NONE_OPTION, false);
    if (!noneOption) {
      VideoOption2.Builder builder = new VideoOption2.Builder();
      builder.setAutoPlayPolicy(getValueFromInt(
          intent.getIntExtra(Constants.PLAY_NETWORK, VideoOption2.AutoPlayPolicy.WIFI.getPolicy()))) // WIFI 环境下可以自动播放视频
          .setAutoPlayMuted(intent.getBooleanExtra(Constants.PLAY_MUTE, true))  // 自动播放时是否为静音
          .setDetailPageMuted(intent.getBooleanExtra(Constants.DETAIL_PAGE_MUTED, false)) // 视频详情页是否为静音
          //  设置返回视频广告的最大视频时长（闭区间，可单独设置），单位:秒，默认为 0 代表无限制，合法输入为：5<=maxVideoDuration<=60. 此设置会影响广告填充，请谨慎设置
          .setMaxVideoDuration(intent.getIntExtra(Constants.MAX_VIDEO_DURATION, 0))
          // 设置返回视频广告的最小视频时长（闭区间，可单独设置），单位:秒，默认为 0 代表无限制， 此设置会影响广告填充，请谨慎设置
          .setMinVideoDuration(intent.getIntExtra(Constants.MIN_VIDEO_DURATION, 0));
      videoOption = builder.build();
    }
    return videoOption;
  }

  public static VideoOption2.AutoPlayPolicy getValueFromInt(int value) {
    VideoOption2.AutoPlayPolicy[] enums = VideoOption2.AutoPlayPolicy.values();
    for (VideoOption2.AutoPlayPolicy policy : enums) {
      if (value == policy.getPolicy()) {
        return policy;
      }
    }
    return VideoOption2.AutoPlayPolicy.WIFI;
  }

}
