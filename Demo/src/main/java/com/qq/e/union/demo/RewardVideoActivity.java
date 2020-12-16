package com.qq.e.union.demo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.util.AdError;
import com.qq.e.comm.util.VideoAdValidity;
import com.qq.e.union.demo.adapter.PosIdArrayAdapter;

import java.util.Date;
import java.util.Locale;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * 激励视频广告基本接入示例，演示了基本的激励视频广告功能（1.初始化激励视频广告;2.加载激励视频广告;3.展示激励视频广告）。
 * <p>
 * Created by chaotao on 2018/10/8.
 */

public class RewardVideoActivity extends Activity implements RewardVideoADListener,
        AdapterView.OnItemSelectedListener {

  private static final String TAG = RewardVideoActivity.class.getSimpleName();
  private RewardVideoAD rewardVideoAD;
  private EditText posIdEdt;
  private boolean adLoaded;//广告加载成功标志
  private boolean videoCached;//视频素材文件下载完成标志

  private Spinner spinner;
  private PosIdArrayAdapter arrayAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_reward_video);
    posIdEdt = findViewById(R.id.position_id);

    spinner = findViewById(R.id.id_spinner);
    arrayAdapter = new PosIdArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.reward_video));
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(this);
  }

  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.change_orientation_button:
        int currentOrientation = getResources().getConfiguration().orientation;
        if (currentOrientation == ORIENTATION_PORTRAIT) {
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (currentOrientation == ORIENTATION_LANDSCAPE) {
          setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        break;
      case R.id.load_ad_button:
        // 1. 初始化激励视频广告
        boolean volumeOn = ((CheckBox) findViewById(R.id.volume_on_checkbox)).isChecked();
        rewardVideoAD = new RewardVideoAD(this, getPosID(), this, volumeOn);
        adLoaded = false;
        videoCached = false;
        // 2. 加载激励视频广告
        rewardVideoAD.loadAD();
        break;
      case R.id.show_ad_button:
      case R.id.show_ad_button_activity:
        // 3. 展示激励视频广告
        if (adLoaded && rewardVideoAD != null)
        {//广告展示检查1：广告成功加载，此处也可以使用videoCached来实现视频预加载完成后再展示激励视频广告的逻辑
          // 广告展示检查2：是否过期、是否展示过、是否缓存
          VideoAdValidity validity = rewardVideoAD.checkValidity();
          switch (validity) {
            case SHOWED:
              Toast.makeText(this, "此条广告已经展示过，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
              Log.i(TAG, "onClick: " + validity.getMessage());
              return;
            case OVERDUE:
              Toast.makeText(this, "激励视频广告已过期，请再次请求广告后进行广告展示！", Toast.LENGTH_LONG).show();
              Log.i(TAG, "onClick: " + validity.getMessage());
              return;
            // 在视频缓存成功后展示，以省去用户的等待时间，提升用户体验
            case NONE_CACHE:
              Toast.makeText(this, "广告素材未缓存成功", Toast.LENGTH_LONG).show();
//              return;
            case VALID:
              Log.i(TAG, "onClick: " + validity.getMessage());
              // 展示广告
              break;
          }
          if (view.getId() == R.id.show_ad_button) {
            rewardVideoAD.showAD();
          } else {
            rewardVideoAD.showAD(RewardVideoActivity.this);
          }
        } else {
          Toast.makeText(this, "成功加载广告后再进行广告展示！", Toast.LENGTH_LONG).show();
        }
        break;
    }
  }

  private String getPosID() {
    return posIdEdt.getText().toString();
  }

  /**
   * 广告加载成功，可在此回调后进行广告展示
   **/
  @Override
  public void onADLoad() {
    adLoaded = true;
    String msg = "load ad success ! expireTime = " + new Date(System.currentTimeMillis() +
        rewardVideoAD.getExpireTimestamp() - SystemClock.elapsedRealtime());
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();

    if (rewardVideoAD.getRewardAdType() == RewardVideoAD.REWARD_TYPE_VIDEO) {
      Log.d(TAG, "eCPMLevel = " + rewardVideoAD.getECPMLevel() + " ,video duration = " + rewardVideoAD.getVideoDuration());
    } else if (rewardVideoAD.getRewardAdType() == RewardVideoAD.REWARD_TYPE_PAGE) {
      Log.d(TAG, "eCPMLevel = " + rewardVideoAD.getECPMLevel());
    }

  }

  /**
   * 视频素材缓存成功，可在此回调后进行广告展示
   */
  @Override
  public void onVideoCached() {
    videoCached = true;
    Log.i(TAG, "onVideoCached");
  }

  /**
   * 激励视频广告页面展示
   */
  @Override
  public void onADShow() {
    Log.i(TAG, "onADShow");
  }

  /**
   * 激励视频广告曝光
   */
  @Override
  public void onADExpose() {
    Log.i(TAG, "onADExpose");
  }

  /**
   * 激励视频触发激励（观看视频大于一定时长或者视频播放完毕）
   */
  @Override
  public void onReward() {
    Log.i(TAG, "onReward");
  }

  /**
   * 激励视频广告被点击
   */
  @Override
  public void onADClick() {
    Log.i(TAG, "onADClick");
  }

  /**
   * 激励视频播放完毕
   */
  @Override
  public void onVideoComplete() {
    Log.i(TAG, "onVideoComplete");
  }

  /**
   * 激励视频广告被关闭
   */
  @Override
  public void onADClose() {
    Log.i(TAG, "onADClose");
  }

  /**
   * 广告流程出错
   */
  @Override
  public void onError(AdError adError) {
    String msg = String.format(Locale.getDefault(), "onError, error code: %d, error msg: %s",
        adError.getErrorCode(), adError.getErrorMsg());
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    Log.i(TAG, "onError, adError=" + msg);
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    arrayAdapter.setSelectedPos(position);
    posIdEdt.setText(getResources().getStringArray(R.array.reward_video_value)[position]);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }
}
