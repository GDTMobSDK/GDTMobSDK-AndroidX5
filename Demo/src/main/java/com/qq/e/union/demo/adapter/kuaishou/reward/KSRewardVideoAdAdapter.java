package com.qq.e.union.demo.adapter.kuaishou.reward;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.text.format.DateUtils;
import android.util.Log;

import com.kwad.sdk.KsAdSDK;
import com.kwad.sdk.export.i.IAdRequestManager;
import com.kwad.sdk.export.i.KsRewardVideoAd;
import com.kwad.sdk.protocol.model.AdScene;
import com.kwad.sdk.viedo.VideoPlayConfig;
import com.qq.e.comm.adevent.ADEvent;
import com.qq.e.comm.adevent.ADListener;
import com.qq.e.mediation.interfaces.BaseRewardAd;
import com.qq.e.union.demo.adapter.kuaishou.util.KSSDKInitUtil;
import com.qq.e.union.demo.adapter.util.Constant;
import com.qq.e.union.demo.adapter.util.ContextUtils;
import com.qq.e.union.demo.adapter.util.ErrorCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * 快手激励视频Adapter实现demo
 *
 * @author chonggao
 * Date 2019/10/31
 */
public class KSRewardVideoAdAdapter extends BaseRewardAd {

  private KsRewardVideoAd mRewardVideoAd;
  private Activity mActivity;
  private ADListener mListener;
  private boolean mIsShown;
  private long mExpireTime;
  private int mPosId;
  private boolean mIsLoadOvertime; // 快手SDK拉广告很慢，这里加一个超时限制，超过10s后就不触发回调了，因为默认配置超时时机是10s。开发者可自行调整，
  private boolean mIsShowLandscape;

  private static final String KEY_APPID = "appId";
  private static final String KEY_SHOW_LANDSCAPE= "showLandscape";
  private static final String TAG = KSRewardVideoAdAdapter.class.getSimpleName();
  private static final int LOAD_COST_TIME = 10 * 1000;

  public KSRewardVideoAdAdapter(Context context, String posID, String ext) {
    super(context, posID, ext);
    mPosId = Integer.valueOf(posID);
    try {
      JSONObject json = new JSONObject(ext);
      String appId = json.optString(KEY_APPID);
      // ext 配置样例"{\"appId\": \"90120\",\"showLandscape\":true }"，开发者根据需求自行调整
      mIsShowLandscape = json.optBoolean(KEY_SHOW_LANDSCAPE, false);
      KSSDKInitUtil.initSDK(context, appId);
    } catch (JSONException e) {
      e.printStackTrace();
      Log.e(TAG, "KSRewardVideoAdAdapter: do not get app id");
    }
    mActivity = ContextUtils.getActivity(context);
  }

  @Override
  public void setAdListener(ADListener listener) {
    mListener = listener;
  }

  @Override
  public void loadAD() {
    requestRewardVideoAd();
  }

  @Override
  public void showAD() {
    showRewardVideoAd(buildConfigHPShowScene());
    mIsShown = true;
  }

  @Override
  public long getExpireTimestamp() {
    return mExpireTime;
  }

  @Override
  public boolean hasShown() {
    return mIsShown;
  }

    @Override
    public int getECPM() {
        return mRewardVideoAd != null ? mRewardVideoAd.getECPM() : Constant.VALUE_NO_ECPM;
    }

  @Override
  public String getECPMLevel() {
    return null;
  }

  @Override
  public int getVideoDuration() {
    // 暂不支持
    return 0;
  }

  @Override
  public void setVolumOn(boolean volumOn) {
   // 暂不支持
  }

  // 1.请求激励视频广告，获取广告对象，KsRewardVideoAd
  private void requestRewardVideoAd() {
    mRewardVideoAd = null;
    AdScene scene = new AdScene(mPosId); // 90009001 此为快手测试posId，请联系快手平台申请正式posId
    scene.adNum = 1; // 参数可选，默认为1
    KsAdSDK.getAdManager().loadRewardVideoAd(scene, new IAdRequestManager.RewardVideoAdListener() {
      @Override
      public void onError(int code, String msg) {
        if (mIsLoadOvertime) {
          return;
        }
        Log.e(TAG, "onError: code : " + code + "  msg: " + msg);
        onAdError(ErrorCode.NO_AD_FILL);
      }

      @Override
      public void onRewardVideoAdLoad(@Nullable List<KsRewardVideoAd> adList) {
        if (mIsLoadOvertime) {
          return;
        }
        if (adList != null && adList.size() > 0) {
          mRewardVideoAd = adList.get(0);
          mExpireTime = SystemClock.elapsedRealtime() + 30 * DateUtils.MINUTE_IN_MILLIS;
          if (mListener != null) {
            mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_AD_LOADED));
            // 快手没有缓存回调，这里一同回调
            mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_VIDEO_CACHED));
          }
        }
      }
    });
    new Handler(Looper.getMainLooper()).postDelayed(() -> mIsLoadOvertime = true, LOAD_COST_TIME);
  }

  // 2.展示激励视频广告，通过步骤1获取的KsRewardVideoAd对象，判断缓存有效，则设置监听并展示
  private void showRewardVideoAd(VideoPlayConfig videoPlayConfig) {
    if (mRewardVideoAd != null) {
      mRewardVideoAd
          .setRewardAdInteractionListener(new KsRewardVideoAd.RewardAdInteractionListener() {
            @Override
            public void onAdClicked() {
              if (mListener != null) {
                mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_AD_CLICK, new Object[]{""}));
              }
            }

            @Override
            public void onPageDismiss() {
              if (mListener != null) {
                mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_AD_CLOSE));
              }
            }

            @Override
            public void onVideoPlayError(int code, int extra) {
              Log.d(TAG, "code = "+ code + "  extra = " + extra);
              onAdError(ErrorCode.VIDEO_PLAY_ERROR);
            }

            @Override
            public void onVideoPlayEnd() {
              if (mListener != null) {
                mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_VIDEO_COMPLETE));
              }
            }

            @Override
            public void onVideoPlayStart() {
              if (mListener != null) {
                mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_AD_SHOW));
                // 由于快手没有曝光回调，所以曝光和 show 一块回调
                mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_AD_EXPOSE));
              }
            }

            @Override
            public void onRewardVerify() {
              if (mListener != null) {
                mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_REWARD));
              }
            }
          });
      mRewardVideoAd.showRewardVideoAd(mActivity, videoPlayConfig);
    } else {
      Log.d(TAG, "showRewardVideoAd: 暂无可用激励视频广告，请等待缓存加载或者重新刷新");
    }
  }

  // 此处需要开发者自行配置，相关参数可以写在本地，或是通过构造函数中ext参数进行解析
  private VideoPlayConfig buildConfigHPShowScene() {
    return new VideoPlayConfig.Builder()
        .showLandscape(mIsShowLandscape) // 竖屏播放
        .skipThirtySecond(true) // 30s可关闭
        .showScene("") // 拓展场景参数，可选 同一posId在不同场景位置展示时，使用此参数区分场景位置。
        .build();
  }

  /**
   * @param errorCode 错误码
   */
  private void onAdError(int errorCode) {
    if (mListener != null) {
      mListener.onADEvent(new ADEvent(EVENT_TYPE_ON_ERROR, new Object[]{errorCode}));
    }
  }
}
