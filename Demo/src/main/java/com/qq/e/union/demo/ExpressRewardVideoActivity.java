package com.qq.e.union.demo;


import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.qq.e.ads.rewardvideo2.ExpressRewardVideoAD;
import com.qq.e.ads.rewardvideo2.ExpressRewardVideoAdListener;
import com.qq.e.comm.util.AdError;
import com.qq.e.comm.util.VideoAdValidity;
import com.qq.e.union.demo.adapter.PosIdArrayAdapter;

public class ExpressRewardVideoActivity extends Activity {

  private static final String TAG = ExpressRewardVideoActivity.class.getSimpleName();

  private ExpressRewardVideoAD mRewardVideoAD;
  private EditText mPosIdEt;
  private String mPosId;
  private boolean mIsLoaded;
  private boolean mIsCached;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_express_reward_video);
    mPosIdEt = findViewById(R.id.position_id);
    mPosId = getPosID();
    Spinner spinner = findViewById(R.id.id_spinner);
    PosIdArrayAdapter arrayAdapter = new PosIdArrayAdapter(this, android.R.layout.simple_spinner_item,
        getResources().getStringArray(R.array.express_reward_video));
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        arrayAdapter.setSelectedPos(position);
        mPosId = getResources().getStringArray(R.array.express_reward_video_value)[position];
        mPosIdEt.setText(mPosId);
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {/* do nothing*/}
    });
  }

  private void initAdManager(String posId) {
    if (mRewardVideoAD != null) {
      mRewardVideoAD.destroy();
    }
    mRewardVideoAD = new ExpressRewardVideoAD(this, posId, new ExpressRewardVideoAdListener() {
      @Override
      public void onAdLoaded() {
        mIsLoaded = true;
        Log.i(TAG,
            "onAdLoaded: VideoDuration " + mRewardVideoAD.getVideoDuration() + ", ECPMLevel " +
                mRewardVideoAD.getECPMLevel());
        showToast("广告拉取成功");
      }

      @Override
      public void onVideoCached() {
        // 在视频缓存完成之后再进行广告展示，以保证用户体验
        mIsCached = true;
        Log.i(TAG, "onVideoCached: ");
      }

      @Override
      public void onShow() {
        Log.i(TAG, "onShow: ");
      }

      @Override
      public void onExpose() {
        Log.i(TAG, "onExpose: ");
      }

      @Override
      public void onReward() {
        Log.i(TAG, "onReward: ");
      }

      @Override
      public void onClick() {
        Log.i(TAG, "onClick: ");
      }

      @Override
      public void onVideoComplete() {
        Log.i(TAG, "onVideoComplete: ");
      }

      @Override
      public void onClose() {
        Log.i(TAG, "onClose: ");
      }

      @Override
      public void onError(AdError error) {
        showToast("广告错误: " + error.getErrorMsg());
        Log.i(TAG, "onError: code = " + error.getErrorCode() + " msg = " + error.getErrorMsg());
      }
    });
  }


  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.load_ad_button:
        boolean volumeOn = ((CheckBox) findViewById(R.id.volume_on_checkbox)).isChecked();
        if (mPosId != getPosID()) {
          mPosId = getPosID();
          initAdManager(mPosId);
        }
        mRewardVideoAD.setVolumeOn(volumeOn);
        mRewardVideoAD.loadAD();
        mIsLoaded = false;
        mIsCached = false;
        break;
      case R.id.show_ad_button:
      case R.id.show_ad_button_activity:
        if (mRewardVideoAD == null || !mIsLoaded) {
          showToast("广告未拉取成功！");
          return;
        }
        VideoAdValidity validity = mRewardVideoAD.checkValidity();
        switch (validity) {
          case SHOWED:
          case OVERDUE:
            showToast(validity.getMessage());
            Log.i(TAG, "onClick: " + validity.getMessage());
            return;
          // 在视频缓存成功后展示，以省去用户的等待时间，提升用户体验
          case NONE_CACHE:
            showToast("广告素材未缓存成功！");
//            return;
          case VALID:
            Log.i(TAG, "onClick: " + validity.getMessage());
            // 展示广告
            break;
        }
        // 在视频缓存成功后展示，以省去用户的等待时间，提升用户体验
        mRewardVideoAD
            .showAD(view.getId() == R.id.show_ad_button ? null : ExpressRewardVideoActivity.this);
        break;
    }
  }

  private void showToast(String msg) {
    Toast.makeText(ExpressRewardVideoActivity.this, msg, Toast.LENGTH_SHORT).show();
  }

  private String getPosID() {
    return mPosIdEt.getText().toString();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mRewardVideoAD != null) {
      mRewardVideoAD.destroy();
    }
  }
}
