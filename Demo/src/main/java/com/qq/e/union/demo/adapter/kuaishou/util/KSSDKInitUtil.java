package com.qq.e.union.demo.adapter.kuaishou.util;

import android.content.Context;

import com.kwad.sdk.KsAdSDK;
import com.kwad.sdk.SdkConfig;

/**
 * 快手SDK 初始化工具类
 *
 * @author chonggao
 * Date 2019/10/31
 */
public class KSSDKInitUtil {

  private static boolean mIsInit;

  public static void initSDK(Context appContext, String appId) {
    if (appContext != null && !mIsInit) {
      synchronized (KSSDKInitUtil.class) {
        if (!mIsInit) {
          KsAdSDK.init(appContext.getApplicationContext(),
              new SdkConfig.Builder().appId(appId) // 90009 为快手测试aapId，请联系快手平台申请正式AppId，必填
              .appName("test-android-sdk") // 测试appName，请填写您应用的名称，非必填
              .showNotification(true) // 是否展示下载通知栏
              .debug(true).build());
          mIsInit = true;
        }
      }
    }
  }
}
