
package com.sample;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.BackgroundColorSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.Menu;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.sample.custom.CustomGunPower;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.util.SystemClock;
import master.flame.danmaku.extensions.CustomCacheStuffer;
import master.flame.danmaku.extensions.ICustomPower;
import master.flame.danmaku.ui.widget.DanmakuGLSurfaceView;
import master.flame.danmaku.ui.widget.DanmakuSurfaceView;
import master.flame.danmaku.ui.widget.DanmakuTextureView;
import master.flame.danmaku.ui.widget.DanmakuView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

public class BiliDanmaActivity extends Activity implements View.OnClickListener {
    public static final String TYPE = "danmaku_type";
    public static final int TYPE_DANMAKU_VIEW = 1;
    public static final int TYPE_DANMAKU_GL_VIEW = 2;
    public static final int TYPE_DANMAKU_TEXTURE_VIEW = 3;
    public static final int TYPE_DANMAKU_SURFACE_VIEW = 4;

    private ViewGroup rootView;
    private IDanmakuView mDanmakuView;

    private View mMediaController;

    private Button mBtnRotate;

    private Button mBtnHideDanmaku;

    private Button mBtnShowDanmaku;

    private BaseDanmakuParser mParser;

    private Button mBtnPauseDanmaku;

    private Button mBtnResumeDanmaku;

    private Button mBtnSendDanmaku;

    private Button mBtnSendDanmakuTextAndImage;

    private Button mBtnSendDanmakus;

    private DanmakuContext mDanmakuContext;
    private int mDanmakuType;

    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {

        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            if (danmaku.tag instanceof ICustomPower) {
                ((ICustomPower) danmaku.tag).clear();
            }
        }
    };

    /**
     * 绘制背景(自定义弹幕样式)
     */
    // private static class BackgroundCacheStuffer extends SpannedCacheStuffer {
    //     // 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
    //     final Paint paint = new Paint();
    //
    //     @Override
    //     public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
    //         danmaku.padding = 10;  // 在背景绘制模式下增加padding
    //         super.measure(danmaku, paint, fromWorkerThread);
    //     }
    //
    //     @Override
    //     public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {
    //         paint.setColor(0x8125309b);
    //         canvas.drawRect(left + 2, top + 2, left + danmaku.paintWidth - 2,
    //                 top + danmaku.paintHeight - 2, paint);
    //     }
    //
    //     @Override
    //     public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left,
    //                            float top, Paint paint) {
    //         // 禁用描边绘制
    //     }
    // }
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        //     // 获取系统window支持的模式
        //     val modes = getWindow().getWindowManager().getDefaultDisplay().getSupportedModes()
        //     // 对获取的模式，基于刷新率的大小进行排序，从小到大排序
        //     modes.sortBy {
        //         it.refreshRate
        //     }
        //
        //     window.let {
        //         val lp = it.attributes
        //         // 取出最小的那一个刷新率，直接设置给window
        //         lp.preferredDisplayModeId = modes.first().modeId
        //         it.attributes = lp
        //     }
        // }
        super.onCreate(savedInstanceState);
        mDanmakuType = getIntent().getIntExtra(TYPE, TYPE_DANMAKU_GL_VIEW);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        rootView = findViewById(R.id.danma_rootview);
        findViews();
    }

    private BaseDanmakuParser createParser(InputStream stream) {

        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new BiliDanmakuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;

    }

    private void findViews() {
        mMediaController = findViewById(R.id.media_controller);
        mBtnRotate = (Button) findViewById(R.id.rotate);
        mBtnHideDanmaku = (Button) findViewById(R.id.btn_hide);
        mBtnShowDanmaku = (Button) findViewById(R.id.btn_show);
        mBtnPauseDanmaku = (Button) findViewById(R.id.btn_pause);
        mBtnResumeDanmaku = (Button) findViewById(R.id.btn_resume);
        mBtnSendDanmaku = (Button) findViewById(R.id.btn_send);
        mBtnSendDanmakuTextAndImage = (Button) findViewById(R.id.btn_send_image_text);
        mBtnSendDanmakus = (Button) findViewById(R.id.btn_send_danmakus);
        mBtnRotate.setOnClickListener(this);
        mBtnHideDanmaku.setOnClickListener(this);
        mMediaController.setOnClickListener(this);
        mBtnShowDanmaku.setOnClickListener(this);
        mBtnPauseDanmaku.setOnClickListener(this);
        mBtnResumeDanmaku.setOnClickListener(this);
        mBtnSendDanmaku.setOnClickListener(this);
        mBtnSendDanmakuTextAndImage.setOnClickListener(this);
        mBtnSendDanmakus.setOnClickListener(this);
        TextView viewById = findViewById(R.id.sv_image);
        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "SSS", Toast.LENGTH_SHORT).show();
            }
        });

        // VideoView
        VideoView mVideoView = (VideoView) findViewById(R.id.videoview);

        // DanmakuView
        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 5); // 滚动弹幕最大显示5行
        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        if (mDanmakuType == TYPE_DANMAKU_VIEW) {
            mDanmakuView = new DanmakuView(getApplicationContext());
            viewById.setText("DanmakuView");
        } else if (mDanmakuType == TYPE_DANMAKU_GL_VIEW) {
            mDanmakuView = new DanmakuGLSurfaceView(getApplicationContext());
            viewById.setText("DanmakuGLSurfaceView");
        } else if (mDanmakuType == TYPE_DANMAKU_SURFACE_VIEW) {
            mDanmakuView = new DanmakuSurfaceView(getApplicationContext());
            viewById.setText("DanmakuSurfaceView");
        } else if (mDanmakuType == TYPE_DANMAKU_TEXTURE_VIEW) {
            mDanmakuView = new DanmakuTextureView(getApplicationContext());
            viewById.setText("DanmakuTextureView");
        }

        rootView.addView((View) mDanmakuView, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
        //mDanmakuView = (IDanmakuView) findViewById(R.id.sv_danmaku);
        mDanmakuContext = DanmakuContext.create(mDanmakuView);
        mDanmakuContext.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false).setScrollSpeedFactor(1.2f).setScaleTextSize(1.2f)
                .setCacheStuffer(new CustomCacheStuffer(getApplicationContext(), mDanmakuView),
                        mCacheStufferAdapter)
                // 图文混排使用SpannedCacheStuffer
                //.setCacheStuffer(new BackgroundCacheStuffer())
                // 绘制背景使用BackgroundCacheStuffer
                .setMaximumLines(maxLinesPair)
                .setAllowDelayInCacheModel(false)
                .alignBottom(true)
                .preventOverlapping(overlappingEnablePair)
                .setDanmakuMargin(40);

        if (mDanmakuView != null) {
            if (mDanmakuView instanceof SurfaceView) {
                ((SurfaceView) mDanmakuView).setZOrderOnTop(true);
            } else if (mDanmakuView instanceof DanmakuTextureView) {
                ((DanmakuTextureView) mDanmakuView).setWillNotDraw(false);
            }
            mDanmakuView.enableDanmakuDrawingCache(true);
            mParser = createParser(this.getResources().openRawResource(R.raw.comments));
            mDanmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
                }

                @Override
                public void prepared() {
                    Log.d("DFM", "prepared");
                    mDanmakuView.start();
                }
            });
            mDanmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {

                @Override
                public boolean onDanmakuClick(IDanmakus danmakus) {
                    Log.d("DFM", "onDanmakuClick: danmakus size:" + danmakus.size());
                    BaseDanmaku latest = danmakus.last();
                    if (null != latest) {
                        Toast.makeText(getApplicationContext(), latest.text, Toast.LENGTH_SHORT)
                                .show();
                        Log.d("DFM", "onDanmakuClick: text of latest danmaku:" + latest.text);
                        return true;
                    }
                    return false;
                }

                @Override
                public boolean onDanmakuLongClick(IDanmakus danmakus) {
                    return false;
                }

                @Override
                public boolean onViewClick(IDanmakuView view) {
                    Toast.makeText(getApplicationContext(), "onViewClick", Toast.LENGTH_SHORT)
                            .show();
                    // mMediaController.setVisibility(View.VISIBLE);
                    return false;
                }
            });
            mDanmakuView.prepare(mParser, mDanmakuContext);
            mDanmakuView.showFPS(true);
        }

        if (mVideoView != null) {
            mVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mediaPlayer.start();
                }
            });
            mVideoView.setVideoURI(
                    Uri.parse(
                            "http://lxcode.bs2cdn.yy.com/28ccd4f7-d651-4f6d-b72b-dd3a1f5bf3f6.mp4"));
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            Log.d("DFM", "pause");
            mDanmakuView.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            Log.d("DFM", "resume");
            mDanmakuView.resume();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mDanmakuView != null) {
            // dont forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onClick(View v) {
        // if (v == mMediaController) {
        //     mMediaController.setVisibility(View.GONE);
        // }
        if (mDanmakuView == null || !mDanmakuView.isPrepared()) {
            return;
        }
        if (v == mBtnRotate) {
            setRequestedOrientation(
                    getRequestedOrientation() == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE ?
                            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT :
                            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else if (v == mBtnHideDanmaku) {
            mDanmakuView.hide();
            // mPausedPosition = mDanmakuView.hideAndPauseDrawTask();
        } else if (v == mBtnShowDanmaku) {
            mDanmakuView.show();
            // mDanmakuView.showAndResumeDrawTask(mPausedPosition); // sync to the video time in your practice
        } else if (v == mBtnPauseDanmaku) {
            mDanmakuView.pause();
        } else if (v == mBtnResumeDanmaku) {
            mDanmakuView.resume();
        } else if (v == mBtnSendDanmaku) {
            addDanmaku(false);
        } else if (v == mBtnSendDanmakuTextAndImage) {
            addCustomGunPower(false);
           // addDanmaKuShowTextAndImage(false);
        } else if (v == mBtnSendDanmakus) {
            Boolean b = (Boolean) mBtnSendDanmakus.getTag();
            timer.cancel();
            if (b == null || !b) {
                mBtnSendDanmakus.setText(R.string.cancel_sending_danmakus);
                timer = new Timer();
                timer.schedule(new AsyncAddTask(), 0, 1000);
                mBtnSendDanmakus.setTag(true);
            } else {
                mBtnSendDanmakus.setText(R.string.send_danmakus);
                mBtnSendDanmakus.setTag(false);
            }
        }
    }

    Timer timer = new Timer();

    class AsyncAddTask extends TimerTask {

        @Override
        public void run() {
            for (int i = 0; i < 20; i++) {
                addDanmaku(true);
                SystemClock.sleep(20);
            }
        }
    }

    ;

    private void addDanmaku(boolean islive) {
        BaseDanmaku danmaku =
                mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        // for(int i=0;i<100;i++){
        // }
        danmaku.text = "这是一条弹幕" + System.nanoTime();
        danmaku.padding = 5;
        danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = Color.WHITE;
        // danmaku.underlineColor = Color.GREEN;
        danmaku.borderColor = Color.GREEN;
        mDanmakuView.addDanmaku(danmaku);

    }

    private void addDanmaKuShowTextAndImage(boolean islive) {
        BaseDanmaku danmaku =
                mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_FIX_BOTTOM);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
        drawable.setBounds(0, 0, 100, 100);
        SpannableStringBuilder spannable = createSpannable(drawable);
        danmaku.text = spannable;
        danmaku.padding = 5;
        danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
        danmaku.underlineColor = Color.GREEN;
        mDanmakuView.addDanmaku(danmaku);
    }

    public void addCustomGunPower(boolean islive) {
        BaseDanmaku danmaku =
                mDanmakuContext.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        CustomGunPower dm = new CustomGunPower();
        dm.content = " 你司法所解放碑算开发你上课妇女客服呢上课呢罚款～";
        dm.nickname = "小明";
        danmaku.tag = dm;
        danmaku.text = dm.content;
        danmaku.padding = 5;
        danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = islive;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 1200);
        danmaku.textSize = 0;
        mDanmakuView.addDanmaku(danmaku);
    }

    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        ImageSpan span = new ImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 0, text.length(), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("图文混排");
        spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0,
                spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mDanmakuView.getConfig().setDanmakuMargin(20);
        } else if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mDanmakuView.getConfig().setDanmakuMargin(40);
        }
    }
}
