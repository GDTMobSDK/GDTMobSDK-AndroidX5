package com.qq.e.union.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADZoomOutListener;
import com.qq.e.union.demo.util.SplashZoomOutManager;
import com.qq.e.comm.constants.LoadAdParams;
import com.qq.e.union.demo.adapter.PosIdArrayAdapter;

import com.qq.e.comm.util.AdError;
import com.qq.e.union.demo.view.ViewUtils;

/**
 * @author tysche
 */

public class SplashADActivity extends Activity implements View.OnClickListener,
    AdapterView.OnItemSelectedListener, SplashADZoomOutListener {
  private static final String TAG = "AD_DEMO_SPLASH_ZOOMOUT";
  private Spinner spinner;
  private EditText posIdEdt;

  private PosIdArrayAdapter arrayAdapter;

  private View splashView;
  private SplashAD splashAD;
  private ViewGroup zoomOutView;
  private boolean isZoomOuted;
  private static final String SKIP_TEXT = "点击跳过 %d";
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash_ad);
    posIdEdt = findViewById(R.id.posId);

    findViewById(R.id.splashADPreloadButton).setOnClickListener(this);
    findViewById(R.id.splashADDemoButton).setOnClickListener(this);
    findViewById(R.id.splashFetchAdOnly).setOnClickListener(this);
    findViewById(R.id.splashShowInView).setOnClickListener(this);

    spinner = findViewById(R.id.id_spinner);
    arrayAdapter = new PosIdArrayAdapter(this, android.R.layout.simple_spinner_item, getResources().getStringArray(R.array.splash_ad));
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(arrayAdapter);
    spinner.setOnItemSelectedListener(this);

  }

  private String getPosID() {
    String posId = ((EditText) findViewById(R.id.posId)).getText().toString();
    return TextUtils.isEmpty(posId) ? PositionId.SPLASH_POS_ID : posId;
  }

  private boolean needLogo() {
    return ((CheckBox) findViewById(R.id.checkBox)).isChecked();
  }

  private boolean customSkipBtn(){
    return ((CheckBox)findViewById(R.id.checkCustomSkp)).isChecked();
  }

  @Override
  public void onClick(View v) {
    cleanZoomOut();
    switch (v.getId()) {
      case R.id.splashADPreloadButton:
        SplashAD splashAD = new SplashAD(this, getPosID(), null);
        LoadAdParams params = new LoadAdParams();
        params.setLoginAppId("testAppId");
        params.setLoginOpenid("testOpenId");
        params.setUin("testUin");
        splashAD.setLoadAdParams(params);
        splashAD.preLoad();
        break;
      case R.id.splashADDemoButton:
        startActivity(getSplashActivityIntent(SplashActivity.class));
        break;
      case R.id.splashFetchAdOnly:
        Intent intent = getSplashActivityIntent(SplashActivity.class);
        intent.putExtra("load_ad_only", true);
        startActivity(intent);
        break;
      case R.id.splashShowInView:
        showSplashInThisActivity();
        break;
    }
  }

  private Intent getSplashActivityIntent(Class<?> cls) {
    Intent intent = new Intent(SplashADActivity.this, cls);
    intent.putExtra("pos_id", getPosID());
    intent.putExtra("need_logo", needLogo());
    intent.putExtra("need_start_demo_list", false);
    intent.putExtra("custom_skip_btn", customSkipBtn());
    intent.putExtra("support_zoom_out", false);
    return intent;
  }

  @Override
  public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
    arrayAdapter.setSelectedPos(position);
    posIdEdt.setText(getResources().getStringArray(R.array.splash_ad_value)[position]);
  }

  @Override
  public void onNothingSelected(AdapterView<?> parent) {

  }

  /**
   * 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费
   */
//  @Override
//  public boolean onKeyDown(int keyCode, KeyEvent event) {
//    if (keyCode == KeyEvent.KEYCODE_BACK) {
//      if(keyCode == KeyEvent.KEYCODE_BACK && loadAdOnlyView.getVisibility() == View.VISIBLE){
//        return super.onKeyDown(keyCode, event);
//      }
//      return true;
//    }
//    return super.onKeyDown(keyCode, event);
//  }

  private void showSplashInThisActivity() {
    Log.d(TAG, "showSplashInThisActivity");

    DemoUtil.hideSoftInput(this);
    if (splashView == null) {
      ViewGroup contentView = findViewById(android.R.id.content);
      LayoutInflater inflater = getLayoutInflater();
      splashView = inflater.inflate(R.layout.activity_splash, null);
      splashView.setClickable(true);//显示出来的时候阻止点击事件传递到后面的按钮
      contentView.addView(splashView);
      Log.d(TAG, "contentView child count:" + contentView.getChildCount());
    } else if (splashView.getVisibility() == View.VISIBLE) {
      Toast.makeText(SplashADActivity.this.getApplicationContext(), "有开屏正在展示，忽略本次拉取",
          Toast.LENGTH_SHORT).show();
      Log.d(TAG, "有开屏正在展示，忽略本次拉取");
      return;
    } else {
      splashView.setVisibility(View.VISIBLE);
    }
    if (!needLogo()) {
      splashView.findViewById(R.id.app_logo).setVisibility(View.GONE);
    } else {
      splashView.findViewById(R.id.app_logo).setVisibility(View.VISIBLE);
    }
    TextView skipView = null;
    if (customSkipBtn()) {
      skipView = splashView.findViewById(R.id.skip_view);
      skipView.setVisibility(View.VISIBLE);
    } else {
      splashView.findViewById(R.id.skip_view).setVisibility(View.GONE);
    }
    ViewGroup container = splashView.findViewById(R.id.splash_container);
    container.removeAllViews();

    //因为SplashAD是和广告位绑定的，在广告位变化时需要重新创建
    splashAD = new SplashAD(this, skipView, getPosID(), this, 0);

    Log.d(TAG, "fetchAndShowIn");
    isZoomOuted = false;
    splashAD.fetchAndShowIn(container);
  }

  private void cleanZoomOut() {
    if (zoomOutView != null) {
      ViewUtils.removeFromParent(zoomOutView);
      zoomOutView = null;
    }
  }

  @Override
  public void onADDismissed() {
    splashView.setVisibility(View.GONE);
    if (isZoomOuted && zoomOutView != null) {
      ViewUtils.removeFromParent(zoomOutView);
    }
    Log.d(TAG, "onADDismissed");
  }

  @Override
  public void onNoAD(AdError error) {
    splashView.setVisibility(View.GONE);
    String str = String.format("LoadSplashADFail, eCode=%d, errorMsg=%s", error.getErrorCode(),
        error.getErrorMsg());
    Log.i("AD_DEMO", str);
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(SplashADActivity.this.getApplicationContext(), str, Toast.LENGTH_SHORT).show();
      }
    });
  }

  @Override
  public void onADPresent() {
    Log.d(TAG, "onADPresent");
  }

  @Override
  public void onADClicked() {
    Log.d(TAG, "onADClicked");
  }

  @Override
  public void onADTick(long millisUntilFinished) {
    Log.d(TAG, "onADTick");
    if (splashView != null) {
      TextView skipView = splashView.findViewById(R.id.skip_view);
      if (skipView != null && skipView.getVisibility() == View.VISIBLE)
        skipView.setText(String.format(SKIP_TEXT, Math.round(millisUntilFinished / 1000f)));
    }
  }

  @Override
  public void onADExposure() {
    Log.d(TAG, "onADExposure");
  }

  @Override
  public void onADLoaded(long expireTimestamp) {
    Log.d(TAG, "onADLoaded");
  }

  @Override
  public void onZoomOut() {
    Log.d(TAG, "onZoomOut");
    isZoomOuted = true;
    SplashZoomOutManager splashZoomOutManager = SplashZoomOutManager.getInstance();
    View splash = ((ViewGroup) splashView.findViewById(R.id.splash_container)).getChildAt(0);
    if (splash == null) {
      Log.e(TAG, "在开屏展示的过程中进行了新的拉取，导致广告View被清空了");
      return;
    }
    splash.setVisibility(View.VISIBLE);
    ViewGroup content = findViewById(android.R.id.content);
    zoomOutView = splashZoomOutManager.startZoomOut(splash, content, content,
        new SplashZoomOutManager.AnimationCallBack() {
      @Override
      public void animationStart(int animationTime) {
        Log.d(TAG, "animationStart:" + animationTime);
      }

      @Override
      public void animationEnd() {
        Log.d(TAG, "animationEnd");
        splashAD.zoomOutAnimationFinish();
      }
    });
    splashView.setVisibility(View.GONE);
  }

  @Override
  public void onZoomOutPlayFinish() {
    Log.d(TAG, "onPlayFinish");
  }

  @Override
  public boolean isSupportZoomOut() {
    return true;
  }
}
