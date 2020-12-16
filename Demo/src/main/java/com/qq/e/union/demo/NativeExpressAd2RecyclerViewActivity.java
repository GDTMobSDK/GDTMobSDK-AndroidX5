package com.qq.e.union.demo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qq.e.ads.nativ.express2.AdEventListener;
import com.qq.e.ads.nativ.express2.MediaEventListener;
import com.qq.e.ads.nativ.express2.NativeExpressAD2;
import com.qq.e.ads.nativ.express2.NativeExpressADData2;
import com.qq.e.ads.nativ.express2.VideoOption2;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import static com.qq.e.union.demo.NativeExpressAd2GuideActivity.getVideoOption;
import static com.qq.e.union.demo.NativeExpressAd2GuideActivity.getLoadAdCount;

public class NativeExpressAd2RecyclerViewActivity extends Activity
    implements NativeExpressAD2.AdLoadListener {
  public static final int ITEMS_COUNT = 50;
  public static final int AD_COUNT = 5;    // 加载广告的条数，取值范围为[1, 10]
  public static int FIRST_AD_POSITION = 1; // 第一条广告的位置
  public static int ITEMS_PER_AD = 5;     // 每间隔5个条目插入一条广告

  private static final String TAG =
      NativeExpressAd2RecyclerViewActivity.class.getSimpleName();

  private boolean mIsLoading = true;
  private RecyclerView mRecyclerView;
  private CustomAdapter mAdapter;
  private List<NormalItem> mNormalDataList = new ArrayList<>();
  private Map<NativeExpressADData2, Integer> mAdViewPositionMap = new HashMap<>();
  private NativeExpressAD2 mADManager;
  private List<NativeExpressADData2> mAdDataList = new ArrayList<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_native_express_2_reycler_view);
    mRecyclerView = findViewById(R.id.recycler_view);
    mRecyclerView.setHasFixedSize(true);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
      @Override
      public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
        super.onScrollStateChanged(recyclerView, newState);
        //滚动到底再次加载广告
        if (!mIsLoading && newState == SCROLL_STATE_IDLE && !recyclerView.canScrollVertically(1)) {
          mIsLoading = true;
          mADManager.loadAd(getLoadAdCount(getIntent(), AD_COUNT));
        }
      }
    });

    initData();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    if (mAdDataList != null) {
      for (NativeExpressADData2 nativeExpressADData2 : mAdDataList) {
        nativeExpressADData2.destroy();
      }
    }
  }

  private void initData() {
    mAdapter = new CustomAdapter(mNormalDataList);
    mRecyclerView.setAdapter(mAdapter);
    initNativeExpressAD2();
  }

  private void initNativeExpressAD2() {
    mADManager = new NativeExpressAD2(this, getPosId(), this);
    mADManager.setAdSize(340, 0); // 单位 dp
    VideoOption2 options = getVideoOption(getIntent());
    if (options != null) {
      // setVideoOption是可选的，开发者可根据需要选择是否配置
      mADManager.setVideoOption2(options);
    }
    mADManager.loadAd(getLoadAdCount(getIntent(), AD_COUNT));
  }

  @Override
  public void onLoadSuccess(List<NativeExpressADData2> adDataList) {
    Log.i(TAG, "onLoadSuccess: dataSize = " + adDataList.size());

    mIsLoading = false;
    int itemCount = mAdapter.getItemCount();
    int adSize = mAdDataList.size();

    for (int i = 0; i < ITEMS_COUNT; i++) {
      mNormalDataList.add(new NormalItem("No." + (itemCount + i - adSize) + " Normal Data"));
      mAdapter.notifyItemInserted(itemCount + i);
    }

    Iterator<NativeExpressADData2> iterator = adDataList.iterator();
    processAdData(itemCount, iterator, 0);

    mAdDataList.addAll(adDataList);
  }

  /**
   * 因为模板2.0 的广告是渲染成功后，才有广告的 View，进而添加到UI中显示。
   * 如果多条广告同时开始渲染，渲染成功的回调顺序是不确定的，有可能第 2 条先渲染成功，然后第 1 条才渲染成功，
   * 这样导致的结果可能就是列表被滚动到第 2 条广告第位置，但其实用户并没有滑动。
   * 所以这里采用一条一条的渲染广告的方式，当前广告渲染成功或失败后再去渲染下一条广告。
   */
  private void processAdData(int startPosition, Iterator<NativeExpressADData2> iterator, int i) {
    if (iterator.hasNext()) {
      NativeExpressADData2 data = iterator.next();
      int position = startPosition + FIRST_AD_POSITION + ITEMS_PER_AD * i + 1;
      if (position < mNormalDataList.size()) {
        data.setAdEventListener(new AdEventListener() {
          @Override
          public void onClick() {
            Log.i(TAG, "onClick, position:" + position);
          }

          @Override
          public void onExposed() {
            Log.i(TAG, "onImpression, position:" + position);
          }

          @Override
          public void onRenderSuccess() {
            Log.i(TAG, "onRenderSuccess, position:" + position);
            mAdViewPositionMap.put(data, position);
            mAdapter.addAdToPosition(position, data);
            mAdapter.notifyItemInserted(position);
            // 当前广告渲染成功，开始渲染下一条广告
            processAdData(startPosition, iterator, i + 1);
          }

          @Override
          public void onRenderFail() {
            Log.i(TAG, "onRenderFail, position:" + position);
            // 当前广告渲染失败，开始渲染下一条广告
            processAdData(startPosition, iterator, i + 1);
          }

          @Override
          public void onAdClosed() {
            data.destroy();
            Log.i(TAG, "onAdClosed, position:" + position);
            if (mAdapter != null) {
              int position = mAdViewPositionMap.get(data);
              mAdapter.removeADView(position, data);
            }
          }
        });
        data.setMediaListener(new MediaEventListener() {
          @Override
          public void onVideoCache() {
            Log.i(TAG, "onVideoCache, position:" + position);
          }

          @Override
          public void onVideoStart() {
            Log.i(TAG, "onVideoStart, position:" + position);
          }

          @Override
          public void onVideoResume() {
            Log.i(TAG, "onVideoResume, position:" + position);
          }

          @Override
          public void onVideoPause() {
            Log.i(TAG, "onVideoPause, position:" + position);
          }

          @Override
          public void onVideoComplete() {
            Log.i(TAG, "onVideoComplete, position:" + position);
          }

          @Override
          public void onVideoError() {
            Log.i(TAG, "onVideoError, position:" + position);
          }
        });
        Log.i(TAG, data + "  eCPM level = " + data.getECPMLevel() +
            "  Video duration: " + data.getVideoDuration());
        // 开始渲染广告
        data.render();
      }
    }
  }

  @Override
  public void onNoAD(AdError error) {
    mIsLoading = false;
    Log.i(TAG, String.format("onNoAD: error code : %d, error msg %s", error.getErrorCode(),
        error.getErrorMsg()));
  }

  private String getPosId() {
    return getIntent().getStringExtra(Constants.POS_ID);
  }

  static class NormalItem {
    private String title;

    public NormalItem(String title) {
      this.title = title;
    }

    public String getTitle() {
      return title;
    }

    public void setTitle(String title) {
      this.title = title;
    }
  }

  class CustomAdapter extends RecyclerView.Adapter<CustomViewHolder> {

    private static final int TYPE_DATA = 0;
    private static final int TYPE_AD = 1;
    private List<Object> dateList;

    CustomAdapter(List dateList) {
      this.dateList = dateList;
    }

    // 把返回的 NativeExpressAD2Data 添加到数据集里面去
    void addAdToPosition(int position, NativeExpressADData2 nativeExpressADData2) {
      if (position >= 0 && position < dateList.size() && nativeExpressADData2 != null) {
        dateList.add(position, nativeExpressADData2);
      }
    }

    // 移除 NativeExpressAD2Data 的时候是一条一条移除的
    void removeADView(int position, NativeExpressADData2 nativeExpressADData2) {
      dateList.remove(position);
      mAdapter.notifyItemRemoved(position);
      mAdapter.notifyItemRangeChanged(0, dateList.size() - 1);
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
      int layoutId = viewType == TYPE_AD ? R.layout.item_express2_ad : R.layout.item_data;
      View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, null);
      return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int position) {
      int viewType = getItemViewType(position);
      if (TYPE_AD == viewType) {
        NativeExpressADData2 adData = (NativeExpressADData2) dateList.get(position);
        mAdViewPositionMap.put(adData, position); // 广告在列表中的位置是可以被更新的
        View adView = adData.getAdView();
        if (customViewHolder.container.getChildCount() > 0 && customViewHolder.container.getChildAt(0) == adView) {
          return;
        }
        if (customViewHolder.container.getChildCount() > 0) {
          customViewHolder.container.removeAllViews();
        }
        if (adView != null && adView.getParent() != null) {
          ((ViewGroup) adView.getParent()).removeView(adView);
        }
        customViewHolder.container.addView(adView);
      } else {
        customViewHolder.title.setText(((NormalItem) dateList.get(position)).title);
      }
    }

    @Override
    public int getItemViewType(int position) {
      Object data = dateList.get(position);
      if (data instanceof NativeExpressADData2) {
        return TYPE_AD;
      } else {
        return TYPE_DATA;
      }
    }

    @Override
    public int getItemCount() {
      if (dateList != null) {
        return dateList.size();
      } else {
        return 0;
      }
    }
  }

  static class CustomViewHolder extends RecyclerView.ViewHolder {
    public TextView title;
    public ViewGroup container;

    CustomViewHolder(View view) {
      super(view);
      title = view.findViewById(R.id.title);
      container = view.findViewById(R.id.express_ad_container);
    }
  }
}
