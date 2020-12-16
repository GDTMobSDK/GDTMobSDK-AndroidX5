package com.qq.e.union.demo;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.util.AdError;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class UnifiedBannerActivity extends Activity implements OnClickListener,
    UnifiedBannerADListener, CompoundButton.OnCheckedChangeListener {

  private static final String TAG = UnifiedBannerActivity.class.getSimpleName();
  ViewGroup bannerContainer;
  UnifiedBannerView bv;
  String posId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_unified_banner);
    bannerContainer = (ViewGroup) this.findViewById(R.id.bannerContainer);
    ((EditText) findViewById(R.id.posId)).setText(PositionId.UNIFIED_BANNER_POS_ID);
    this.findViewById(R.id.refreshBanner).setOnClickListener(this);
    this.findViewById(R.id.closeBanner).setOnClickListener(this);
    ((CheckBox) findViewById(R.id.cbRefreshInterval)).setOnCheckedChangeListener(this);
    this.getBanner().loadAD();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (bv != null) {
      bv.destroy();
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (bv != null) {
      bv.setLayoutParams(getUnifiedBannerLayoutParams());
    }
  }

  private UnifiedBannerView getBanner() {
    if(this.bv != null){
      bannerContainer.removeView(bv);
      bv.destroy();
    }
    String posId = getPosID();
    this.posId = posId;
    this.bv = new UnifiedBannerView(this, posId, this);
    if (((CheckBox) findViewById(R.id.cbRefreshInterval)).isChecked()) {
      try {
        int refreshInterval = Integer.parseInt(((EditText) findViewById(R.id.etRefreshInterval))
            .getText().toString());
        this.bv.setRefresh(refreshInterval);
      } catch (NumberFormatException e) {
        Toast.makeText(this, "请输入合法的轮播时间间隔!", Toast.LENGTH_LONG).show();
      }
    }
    // 不需要传递tags使用下面构造函数
    // this.bv = new UnifiedBannerView(this, Constants.APPID, posId, this);
    bannerContainer.addView(bv, getUnifiedBannerLayoutParams());
    return this.bv;
  }

  /**
   * banner2.0规定banner宽高比应该为6.4:1 , 开发者可自行设置符合规定宽高比的具体宽度和高度值
   *
   * @return
   */
  private FrameLayout.LayoutParams getUnifiedBannerLayoutParams() {
    Point screenSize = new Point();
    getWindowManager().getDefaultDisplay().getSize(screenSize);
    return new FrameLayout.LayoutParams(screenSize.x,  Math.round(screenSize.x / 6.4F));
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.refreshBanner:
        doRefreshBanner();
        break;
      case R.id.closeBanner:
        doCloseBanner();
        break;
      default:
        break;
    }
  }

  private void doRefreshBanner() {
    DemoUtil.hideSoftInput(this);
    getBanner().loadAD();
  }

  private void doCloseBanner() {
    bannerContainer.removeAllViews();
    if (bv != null) {
      bv.destroy();
      bv = null;
    }
  }

  private String getPosID() {
    String posId = ((EditText) findViewById(R.id.posId)).getText().toString();
    return TextUtils.isEmpty(posId) ? PositionId.BANNER_POS_ID : posId;
  }

  @Override
  public void onNoAD(AdError adError) {
    String msg = String.format(Locale.getDefault(), "onNoAD, error code: %d, error msg: %s",
        adError.getErrorCode(), adError.getErrorMsg());
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
  }

  @Override
  public void onADReceive() {
    Log.i(TAG, "onADReceive");
  }

  @Override
  public void onADExposure() {
    Log.i(TAG, "onADExposure");
  }

  @Override
  public void onADClosed() {
    Log.i(TAG, "onADClosed");
  }

  @Override
  public void onADClicked() {
    Log.i(TAG, "onADClicked : " + (bv.getExt() != null? bv.getExt().get("clickUrl") : ""));
  }

  @Override
  public void onADLeftApplication() {
    Log.i(TAG, "onADLeftApplication");
  }

  @Override
  public void onADOpenOverlay() {
    Log.i(TAG, "onADOpenOverlay");
  }

  @Override
  public void onADCloseOverlay() {
    Log.i(TAG, "onADCloseOverlay");
  }

  @Override
  public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    if (this.bv != null) {
      if (isChecked) {
        try {
          int refreshInterval = Integer.parseInt(((EditText) findViewById(R.id.etRefreshInterval))
              .getText().toString());
          this.bv.setRefresh(refreshInterval);
          Toast.makeText(this, "轮播时间间隔设置为:" + refreshInterval, Toast.LENGTH_LONG).show();
        } catch (NumberFormatException e) {
          Toast.makeText(this, "请输入合法的轮播时间间隔!", Toast.LENGTH_LONG).show();
        }
      } else {
        this.bv.setRefresh(30);
        Toast.makeText(this, "轮播时间间隔恢复默认", Toast.LENGTH_LONG).show();
      }
    } else {
      Toast.makeText(this, "轮播时间间隔设置失败!", Toast.LENGTH_LONG).show();
    }
  }
}
