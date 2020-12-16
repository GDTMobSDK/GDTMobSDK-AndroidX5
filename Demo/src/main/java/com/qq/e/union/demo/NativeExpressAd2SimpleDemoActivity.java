package com.qq.e.union.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.qq.e.ads.nativ.express2.AdEventListener;
import com.qq.e.ads.nativ.express2.MediaEventListener;
import com.qq.e.ads.nativ.express2.NativeExpressAD2;
import com.qq.e.ads.nativ.express2.NativeExpressADData2;
import com.qq.e.comm.util.AdError;

import java.util.List;

import static com.qq.e.union.demo.NativeExpressAd2GuideActivity.getVideoOption;
import static com.qq.e.union.demo.NativeExpressAd2GuideActivity.getLoadAdCount;
import static java.lang.Integer.*;

/**
 * 模板2.0 简单Demo示例
 *
 * @author chonggao
 * Date 2020/5/6
 */
public class NativeExpressAd2SimpleDemoActivity extends Activity implements
    View.OnClickListener, NativeExpressAD2.AdLoadListener {

  private static final String TAG = NativeExpressAd2SimpleDemoActivity.class.getSimpleName();
  private NativeExpressAD2 mNativeExpressAD2;
  private NativeExpressADData2 mNativeExpressADData2;
  private FrameLayout mAdContainer; // 展示广告的广告位

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_express_2_simple_demo);
    findViewById(R.id.btn_load_ad).setOnClickListener(this);
    mAdContainer = findViewById(R.id.express_2_ad_container);
    // 创建广告
    mNativeExpressAD2 = new NativeExpressAD2(this, getPosId(), this);
  }

  private String getPosId() {
    return getIntent().getStringExtra(Constants.POS_ID);
  }


  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.btn_load_ad:
        loadAd();
        break;
    }
  }

  private void loadAd() {
    // 注意不要填超过Integer.MAX_VALUE的值，这里暂且 catch 相关异常
    try {
      mNativeExpressAD2
          .setAdSize(parseInt(((EditText) findViewById(R.id.et_width)).getText().toString()), // 单位 dp
              parseInt(((EditText) findViewById(R.id.et_height)).getText().toString()));
      // 如果您在平台上新建原生模板广告位时，选择了支持视频，那么可以进行个性化设置（可选）
      mNativeExpressAD2.setVideoOption2(getVideoOption(getIntent()));
      mNativeExpressAD2.loadAd(getLoadAdCount(getIntent(), 1));
      destroyAd();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * 广告加载成功回调
   * @param adDataList
   */
  @Override
  public void onLoadSuccess(List<NativeExpressADData2> adDataList) {
    Log.i(TAG, "onLoadSuccess: size " + adDataList.size());
    renderAd(adDataList);
  }

  /**
   * 渲染广告
   * @param adDataList
   */
  private void renderAd(List<NativeExpressADData2> adDataList) {
    if (adDataList.size() > 0) {
      mAdContainer.removeAllViews();
      mNativeExpressADData2 = adDataList.get(0);
      Log.i(TAG, "renderAd: " + "  eCPM level = " +
          mNativeExpressADData2.getECPMLevel() + "  Video duration: " + mNativeExpressADData2.getVideoDuration());
      mNativeExpressADData2.setAdEventListener(new AdEventListener() {
        @Override
        public void onClick() {
          Log.i(TAG, "onClick: " + mNativeExpressADData2);
        }

        @Override
        public void onExposed() {
          Log.i(TAG, "onImpression: " + mNativeExpressADData2);
        }

        @Override
        public void onRenderSuccess() {
          Log.i(TAG, "onRenderSuccess: " + mNativeExpressADData2);
          mAdContainer.removeAllViews();
          if (mNativeExpressADData2.getAdView() != null) {
            mAdContainer.addView(mNativeExpressADData2.getAdView());
          }
        }

        @Override
        public void onRenderFail() {
          Log.i(TAG, "onRenderFail: " + mNativeExpressADData2);
        }

        @Override
        public void onAdClosed() {
          Log.i(TAG, "onAdClosed: " + mNativeExpressADData2);
          mAdContainer.removeAllViews();
          mNativeExpressADData2.destroy();
        }
      });

      mNativeExpressADData2.setMediaListener(new MediaEventListener() {
        @Override
        public void onVideoCache() {
          Log.i(TAG, "onVideoCache: " + mNativeExpressADData2);
        }

        @Override
        public void onVideoStart() {
          Log.i(TAG, "onVideoStart: " + mNativeExpressADData2);
        }

        @Override
        public void onVideoResume() {
          Log.i(TAG, "onVideoResume: " + mNativeExpressADData2);
        }

        @Override
        public void onVideoPause() {
          Log.i(TAG, "onVideoPause: " + mNativeExpressADData2);
        }

        @Override
        public void onVideoComplete() {
          Log.i(TAG, "onVideoComplete: " + mNativeExpressADData2);
        }

        @Override
        public void onVideoError() {
          Log.i(TAG, "onVideoError: " + mNativeExpressADData2);
        }
      });

      mNativeExpressADData2.render();
    }
  }

  /**
   * 广告加载失败回调
   * @param error
   */
  @Override
  public void onNoAD(AdError error) {
    @SuppressLint("DefaultLocale")
    String errorMsg = String
        .format("onNoAD, error code: %d, error msg: %s", error.getErrorCode(), error.getErrorMsg());
    Log.i(TAG, "onNoAD: " + errorMsg);
    Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    // 使用完了每一个 NativeExpressAD2Data 之后都要释放掉资源
    destroyAd();
  }

  /**
   *  释放前一个 NativeExpressAD2Data 的资源
   */
  private void destroyAd() {
    if (mNativeExpressADData2 != null) {
      Log.d(TAG, "destroyAD");
      mNativeExpressADData2.destroy();
    }
  }

}
