package com.qq.e.union.demo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.androidquery.callback.BitmapAjaxCallback;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.MediaView;
import com.qq.e.ads.nativ.NativeADEventListener;
import com.qq.e.ads.nativ.NativeADMediaListener;
import com.qq.e.ads.nativ.NativeADUnifiedListener;
import com.qq.e.ads.nativ.NativeUnifiedAD;
import com.qq.e.ads.nativ.NativeUnifiedADData;
import com.qq.e.ads.nativ.widget.NativeAdContainer;
import com.qq.e.comm.constants.AdPatternType;
import com.qq.e.comm.util.AdError;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NativeADUnifiedFullScreenFeedActivity extends Activity implements NativeADUnifiedListener {

  private static final String TAG = NativeADUnifiedFullScreenFeedActivity.class.getSimpleName();

  private NativeUnifiedAD mAdManager;
  private List<NativeUnifiedADData> mAds = new ArrayList<>();

  private ItemAdapter mAdapter;

  private NativeADUnifiedFullScreenFeedActivity.H mHandler = new NativeADUnifiedFullScreenFeedActivity.H();

  private static final int INIT_ITEM_COUNT = 2;
  private static final int ITEM_COUNT = 5;
  private static final int AD_COUNT = 3;
  private static final int MSG_REFRESH_LIST = 1;

  private static final int TYPE_DATA = 0;
  private static final int TYPE_AD = 1;

  private ViewPagerLayoutManager mLayoutManager;
  private RecyclerView mRecyclerView;

  private int mCurrentPage = -1;
  private int mVideoViewCurrentPosition=-1;
  private VideoView mCurrentVideoView;
  private boolean videoIsPaused=false;

  private int[] mVideoIds = new int[]{R.raw.v1, R.raw.v2, R.raw.v3, R.raw.v4, R.raw.v5};
  private int[] mImageIds = new int[]{R.raw.p1, R.raw.p2, R.raw.p3, R.raw.p4, R.raw.p5};
  private boolean mBindToCustomView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_native_unified_ad_recyclerview);
    initView();

    mBindToCustomView = getIntent().getBooleanExtra(Constants.BUTTON_BIND_TO_CUSTOM_VIEW, false);

    mAdManager = new NativeUnifiedAD(this, getPosId(), this);
    mAdManager.setMinVideoDuration(getMinVideoDuration());
    mAdManager.setMaxVideoDuration(getMaxVideoDuration());

    /**
     * 如果广告位支持视频广告，强烈建议在调用loadData请求广告前，调用下面两个方法，有助于提高视频广告的eCPM值 <br/>
     * 如果广告位仅支持图文广告，则无需调用
     */

    /**
     * 设置本次拉取的视频广告，从用户角度看到的视频播放策略<p/>
     *
     * "用户角度"特指用户看到的情况，并非SDK是否自动播放，与自动播放策略AutoPlayPolicy的取值并非一一对应 <br/>
     *
     * 例如开发者设置了VideoOption.AutoPlayPolicy.NEVER，表示从不自动播放 <br/>
     * 但满足某种条件(如晚上10点)时，开发者调用了startVideo播放视频，这在用户看来仍然是自动播放的
     */
    mAdManager.setVideoPlayPolicy(NativeADUnifiedSampleActivity.getVideoPlayPolicy(getIntent(), this)); // 本次拉回的视频广告，在用户看来是否为自动播放的

    /**
     * 设置在视频广告播放前，用户看到显示广告容器的渲染者是SDK还是开发者 <p/>
     *
     * 一般来说，用户看到的广告容器都是SDK渲染的，但存在下面这种特殊情况： <br/>
     *
     * 1. 开发者将广告拉回后，未调用bindMediaView，而是用自己的ImageView显示视频的封面图 <br/>
     * 2. 用户点击封面图后，打开一个新的页面，调用bindMediaView，此时才会用到SDK的容器 <br/>
     * 3. 这种情形下，用户先看到的广告容器就是开发者自己渲染的，其值为VideoADContainerRender.DEV
     * 4. 如果觉得抽象，可以参考NativeADUnifiedDevRenderContainerActivity的实现
     */
    mAdManager.setVideoADContainerRender(VideoOption.VideoADContainerRender.SDK); // 视频播放前，用户看到的广告容器是由SDK渲染的

    mAdManager.loadData(AD_COUNT);
  }

  private String getPosId() {
    return getIntent().getStringExtra(Constants.POS_ID);
  }

  private int getMinVideoDuration() {
    return getIntent().getIntExtra(Constants.MIN_VIDEO_DURATION, 0);
  }

  private int getMaxVideoDuration() {
    return getIntent().getIntExtra(Constants.MAX_VIDEO_DURATION, 0);
  }

  private void initView() {
    mRecyclerView = findViewById(R.id.recycler_view);
    mLayoutManager = new ViewPagerLayoutManager(this, LinearLayoutManager.VERTICAL);
    mLayoutManager.setOnViewPagerListener(new OnViewPagerListener() {
      @Override
      public void onInitComplete() {
        if(mAdapter.getItem(0).type == TYPE_DATA){
          play();
        }
        mCurrentPage = 0;
      }

      @Override
      public void onPageRelease(boolean isNext, int position) {
        if(mAdapter.getItem(position).type == TYPE_DATA){
          releaseVideo(isNext ? 0 : 1);
        }
      }

      @Override
      public void onPageSelected(int position, boolean isBottom) {
        if(mAdapter.getItem(position).type == TYPE_DATA){
          play();
        }
        mCurrentPage = position;
      }
    });

    mRecyclerView.setLayoutManager(mLayoutManager);

    List<NativeADUnifiedFullScreenFeedActivity.Item> list = new ArrayList<>();
    // 初始视频，防止拉取广告网络异常时页面空白
    for (int i = 0; i < INIT_ITEM_COUNT; ++i) {
      list.add(new Item(i));
    }
    mAdapter = new ItemAdapter(this, list);
    mRecyclerView.setAdapter(mAdapter);
  }

  @Override
  public void onADLoaded(List<NativeUnifiedADData> ads) {
    // 防止在onDestory后网络回包
    if(mAds != null){
      Toast.makeText(this,  "拉取到 " + ads.size() + " 条广告", Toast.LENGTH_SHORT).show();
      mAds.addAll(ads);
      Message msg = mHandler.obtainMessage(MSG_REFRESH_LIST, ads);
      mHandler.sendMessage(msg);
    }
  }

  @Override
  public void onNoAD(AdError error) {
    Toast.makeText(this,  "没有拉到广告!", Toast.LENGTH_SHORT).show();
    Log.d(TAG, "onNoAd error code: " + error.getErrorCode()
        + ", error msg: " + error.getErrorMsg());
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (mAds != null) {
      for (NativeUnifiedADData ad : mAds) {
        ad.resume();
      }
    }
    if(videoIsPaused){
      mCurrentVideoView.seekTo(mVideoViewCurrentPosition);
      mCurrentVideoView.start();
      videoIsPaused=false;
    }
  }

  @Override
  protected void onPause(){
    super.onPause();
    Item item =mAdapter.getItem(mCurrentPage);
    if(item.type==TYPE_DATA){
      if (mLayoutManager.findViewByPosition(mCurrentPage) == null) {
        return;
      }
      mCurrentVideoView = mLayoutManager.findViewByPosition(mCurrentPage)
              .findViewById(R.id.video_view);
      mVideoViewCurrentPosition=mCurrentVideoView.getCurrentPosition();
      mCurrentVideoView.pause();
      videoIsPaused=true;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mAds != null) {
      for (NativeUnifiedADData ad : mAds) {
        ad.destroy();
      }
    }
    mAds = null;
  }

  class ItemAdapter extends RecyclerView.Adapter<ItemHolder> {

    private List<Item> mData;
    private Context mContext;

    public ItemAdapter(Context context, List list) {
      mContext = context;
      mData = list;
    }

    public Item getItem(int position){
      return mData.get(position);
    }

    public void addItem(NativeADUnifiedFullScreenFeedActivity.Item item){
      mData.add(item);
    }

    public void addItemToPosition(Item item, int position) {
      if (position >= 0 && position < mData.size()) {
        mData.add(position, item);
      }
    }

    @Override
    public int getItemViewType(int position) {
      return mData.get(position).type;
    }

    @Override
    public ItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view;
      switch (viewType) {
        case TYPE_AD:
          view = LayoutInflater.from(mContext).inflate(R.layout.activity_native_unified_ad_full_screen, parent, false);
          break;

        case TYPE_DATA:
          view = LayoutInflater.from(mContext).inflate(R.layout.item_full_screen_video_feed, parent, false);
          break;

        default:
          view = null;
      }
      return new ItemHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(final ItemHolder holder, int position) {
      switch (getItemViewType(position)) {
        case TYPE_AD:
          initADItemView(position, holder);
          break;
        case TYPE_DATA:
          holder.title.setText(mData.get(position).title);
          holder.videoView.setVideoURI(mData.get(position).videoUri);
          holder.coverImage.setImageURI(mData.get(position).imageUri);
          break;
      }
    }

    private void initADItemView(int position, final ItemHolder holder) {
      Item item = mData.get(position);
      final NativeUnifiedADData ad = item.ad;
      AQuery logoAQ = holder.logoAQ;
      logoAQ.id(R.id.img_logo).image(
          TextUtils.isEmpty(ad.getIconUrl()) ? ad.getImgUrl() : ad.getIconUrl(), false, true);
      holder.name.setText(ad.getTitle());
      holder.desc.setText(ad.getDesc());
      // 视频广告
      if (ad.getAdPatternType() == 2) {
        holder.poster.setVisibility(View.INVISIBLE);
        holder.mediaView.setVisibility(View.VISIBLE);
        holder.adInfoContainer.setBackgroundColor(Color.parseColor("#00000000"));
        holder.adInfoContainer.setVisibility(View.GONE);
      } else {
        holder.poster.setVisibility(View.VISIBLE);
        holder.mediaView.setVisibility(View.INVISIBLE);
        holder.adInfoContainer.setBackgroundColor(Color.parseColor("#999999"));
        holder.adInfoContainer.setVisibility(View.VISIBLE);
      }
      List<View> clickableViews = new ArrayList<>();
      List<View> customClickableViews = new ArrayList<>();
      if (mBindToCustomView) {
        customClickableViews.add(holder.download);
      } else {
        clickableViews.add(holder.download);
      }
      if(ad.getAdPatternType() == AdPatternType.NATIVE_2IMAGE_2TEXT ||
          ad.getAdPatternType() == AdPatternType.NATIVE_1IMAGE_2TEXT){
        // 双图双文、单图双文：注册mImagePoster的点击事件
        clickableViews.add(holder.poster);
      }
      //作为customClickableViews传入，点击不进入详情页，直接下载或进入落地页，图文、视频广告均生效，
      ad.bindAdToView(NativeADUnifiedFullScreenFeedActivity.this, holder.container, null,
          clickableViews, customClickableViews);
      logoAQ.id(R.id.img_poster).image(ad.getImgUrl(), false, true, 0, 0,
          new BitmapAjaxCallback() {
            @Override
            protected void callback(String url, ImageView iv, Bitmap bm, AjaxStatus status) {
              if (iv.getVisibility() == View.VISIBLE) {
                iv.setImageBitmap(bm);
              }
            }
          });

      setAdListener(holder, ad);

      NativeADUnifiedSampleActivity.updateAdAction(holder.download, ad);
    }

    private void setAdListener(final ItemHolder holder, final NativeUnifiedADData ad) {
      ad.setNativeAdEventListener(new NativeADEventListener() {
        @Override
        public void onADExposed() {
          Log.d(TAG, "onADExposed: " + ad.getTitle());
        }

        @Override
        public void onADClicked() {
          Log.d(TAG, "onADClicked: " + ad.getTitle());
        }

        @Override
        public void onADError(AdError error) {
          Log.d(TAG, "onADError error code :" + error.getErrorCode()
              + "  error msg: " + error.getErrorMsg());
        }

        @Override
        public void onADStatusChanged() {
          NativeADUnifiedSampleActivity.updateAdAction(holder.download, ad);
        }
      });
      // 视频广告
      if (ad.getAdPatternType() == AdPatternType.NATIVE_VIDEO) {
        VideoOption videoOption = NativeADUnifiedSampleActivity.getVideoOption(getIntent());
        ad.bindMediaView(holder.mediaView, videoOption, new NativeADMediaListener() {
          @Override
          public void onVideoInit() {
            Log.d(TAG, "onVideoInit: ");
          }

          @Override
          public void onVideoLoading() {
            Log.d(TAG, "onVideoLoading: ");
          }

          @Override
          public void onVideoReady() {
            Log.d(TAG, "onVideoReady ");
          }

          @Override
          public void onVideoLoaded(int videoDuration) {
            Log.d(TAG, "onVideoLoaded: ");
          }

          @Override
          public void onVideoStart() {
            Log.d(TAG, "onVideoStart ");
            holder.adInfoContainer.setVisibility(View.VISIBLE);
          }

          @Override
          public void onVideoPause() {
            Log.d(TAG, "onVideoPause: ");
          }

          @Override
          public void onVideoResume() {
            Log.d(TAG, "onVideoResume: ");
          }

          @Override
          public void onVideoCompleted() {
            Log.d(TAG, "onVideoCompleted: ");
          }

          @Override
          public void onVideoError(AdError error) {
            Log.d(TAG, "onVideoError: ");
          }

          @Override
          public void onVideoStop() {
            Log.d(TAG, "onVideoStop");
          }

          @Override
          public void onVideoClicked() {
            Log.d(TAG, "onVideoClicked");
          }
        });
      }
    }

    @Override
    public int getItemCount() {
      return mData.size();
    }
  }

  private void play(){
    View itemView = mRecyclerView.getChildAt(0);
    final VideoView videoView = itemView.findViewById(R.id.video_view);
    final View coverImage = itemView.findViewById(R.id.cover_image);
    if (videoView != null) {
      videoView.start();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
        videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
          @Override
          public boolean onInfo(MediaPlayer mp, int what, int extra) {
            Log.d(TAG, "onInfo");
            mp.setLooping(true);
            coverImage.animate().alpha(0).setDuration(200).start();
            return false;
          }
        });
      }
      videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(MediaPlayer mp) {
          Log.d(TAG, "onPrepared");
        }
      });
    }
  }

  private void releaseVideo(int index) {
    View itemView = mRecyclerView.getChildAt(index);
    if (itemView != null) {
      final View coverImage = itemView.findViewById(R.id.cover_image);
      final VideoView videoView = itemView.findViewById(R.id.video_view);
      if (videoView != null) {
        videoView.stopPlayback();
      }
      coverImage.animate().alpha(1).start();
    }
  }

  class ItemHolder extends RecyclerView.ViewHolder {

    public TextView title;
    public VideoView videoView;
    public ImageView coverImage;
    public MediaView mediaView;
    public RelativeLayout adInfoContainer;
    public TextView name;
    public TextView desc;
    public ImageView logo;
    public ImageView poster;
    public Button download;
    public NativeAdContainer container;
    public AQuery logoAQ;
    public CheckBox btnMute;

    public ItemHolder(View itemView, int adType) {
      super(itemView);
      switch (adType) {
        case TYPE_AD:
          mediaView = itemView.findViewById(R.id.gdt_media_view);
          adInfoContainer = itemView.findViewById(R.id.ad_info_container);
          logo = itemView.findViewById(R.id.img_logo);
          poster = itemView.findViewById(R.id.img_poster);
          name = itemView.findViewById(R.id.text_title);
          desc = itemView.findViewById(R.id.text_desc);
          download = itemView.findViewById(R.id.btn_download);
          container = itemView.findViewById(R.id.native_ad_container);
          btnMute = itemView.findViewById(R.id.btn_mute);
          logoAQ = new AQuery(itemView);

        case TYPE_DATA:
          title = itemView.findViewById(R.id.title);
          videoView = itemView.findViewById(R.id.video_view);
          coverImage = itemView.findViewById(R.id.cover_image);
          break;

      }
    }
  }

  private class Item {

    public int type;
    public int position;

    public Uri imageUri;
    public Uri videoUri;
    public String title;

    public NativeUnifiedADData ad;

    public Item(int position){
      this.type = TYPE_DATA;
      this.title = "第 " + (position + 1) + " 个普通视频";
      this.videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + mVideoIds[position % mVideoIds.length]);
      this.imageUri = Uri.parse("android.resource://" + getPackageName() + "/" + mImageIds[position % mImageIds.length]);
    }

    public Item(NativeUnifiedADData ad){
      this.type = TYPE_AD;
      this.ad = ad;
    }

  }

  private class H extends Handler {
    @Override
    public void handleMessage(Message msg) {
      switch (msg.what) {
        case MSG_REFRESH_LIST:

          for(int i = INIT_ITEM_COUNT; i < ITEM_COUNT; i++){
            mAdapter.addItem(new Item(i));
          }

          List<NativeUnifiedADData> ads = (List<NativeUnifiedADData>) msg.obj;
          if (ads != null && ads.size() > 0 && mAdapter != null) {
            Random random = new Random();
            for (int i = 0; i < ads.size(); i++) {
              int index = Math.abs(random.nextInt()) % ITEM_COUNT;

              while(index == mCurrentPage){
                index = Math.abs(random.nextInt()) % ITEM_COUNT;
              }

              mAdapter.addItemToPosition(new Item(ads.get(i)), index);

              Log.d(TAG,
                  i + ": eCPMLevel = " + ads.get(i).getECPMLevel() + " , videoDuration = " + ads.get(i).getVideoDuration());
            }
          }
          mAdapter.notifyDataSetChanged();
          break;

        default:
      }
    }
  }

  private interface OnViewPagerListener {
    void onInitComplete();

    void onPageRelease(boolean isNext, int position);

    void onPageSelected(int position, boolean isBottom);
  }

  private class ViewPagerLayoutManager extends LinearLayoutManager {
    private PagerSnapHelper mPagerSnapHelper;
    private OnViewPagerListener mOnViewPagerListener;
    private RecyclerView mRecyclerView;
    private int mDeltaY;

    private RecyclerView.OnChildAttachStateChangeListener mChildAttachStateChangeListener = new RecyclerView.OnChildAttachStateChangeListener() {
      public void onChildViewAttachedToWindow(View view) {
        if (mOnViewPagerListener != null && getChildCount() == 1) {
          mOnViewPagerListener.onInitComplete();
        }
      }

      public void onChildViewDetachedFromWindow(View view) {
        if (mDeltaY >= 0) {
          if (mOnViewPagerListener != null) {
            mOnViewPagerListener.onPageRelease(true, getPosition(view));
          }
        } else if (mOnViewPagerListener != null) {
          mOnViewPagerListener.onPageRelease(false, getPosition(view));
        }
      }
    };

    public ViewPagerLayoutManager(Context context, int orientation) {
      super(context, orientation, false);
      mPagerSnapHelper = new PagerSnapHelper();
    }

    public void onAttachedToWindow(RecyclerView view) {
      super.onAttachedToWindow(view);
      mPagerSnapHelper.attachToRecyclerView(view);
      mRecyclerView = view;
      mRecyclerView.addOnChildAttachStateChangeListener(mChildAttachStateChangeListener);
    }

    public void onScrollStateChanged(int state) {
      if(state == RecyclerView.SCROLL_STATE_IDLE){
        View curView = mPagerSnapHelper.findSnapView(this);
        int curPos = getPosition(curView);
        if (mOnViewPagerListener != null && getChildCount() == 1) {
          mOnViewPagerListener.onPageSelected(curPos, curPos == getItemCount() - 1);
        }
      }
    }

    public int scrollVerticallyBy(int dy, RecyclerView.Recycler recycler, RecyclerView.State state) {
      mDeltaY = dy;
      return super.scrollVerticallyBy(dy, recycler, state);
    }

    public void setOnViewPagerListener(OnViewPagerListener listener) {
      mOnViewPagerListener = listener;
    }
  }

}
