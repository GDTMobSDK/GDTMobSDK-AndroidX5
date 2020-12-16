package com.qq.e.union.demo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.androidquery.callback.BitmapAjaxCallback;
import com.qq.e.comm.managers.status.SDKStatus;


/**
 * Created by hechao on 2018/2/8.
 */

public class DemoUtil {

  public static final void hideSoftInput(Activity activity) {
    InputMethodManager imm =
        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    View focusView = activity.getCurrentFocus();
    if (focusView != null) {
      imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0); //强制隐藏键盘
    }
  }

  public static void setAQueryImageUserAgent(){
    BitmapAjaxCallback.setAgent("GDTMobSDK-AQuery-"+ SDKStatus.getIntegrationSDKVersion());
  }

}
