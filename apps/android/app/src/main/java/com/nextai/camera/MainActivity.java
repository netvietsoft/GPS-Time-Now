package com.nextai.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SurfaceTexture;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {

    private final WatermarkModel mModel = new WatermarkModel();
    private final Handler mUiHandler = new Handler(Looper.getMainLooper());

    // Original Fonts from assets/fonts/
    private Typeface mFontBebas;
    private Typeface mFontSegment;
    private Typeface mFontMono;

    // UI References matching activity_camera_preview.xml
    private LinearLayout mRootLayout;
    private FrameLayout mCenterHost;
    private FrameLayout mBottomActionHost;
    private FrameLayout mViewfinderContainer;
    private TextureView mTextureView;
    private int mPreviewBufferWidth = 1280;
    private int mPreviewBufferHeight = 960;
    private View mShutterFlashView;
    private View mGridLevelerView;
    private TextView mTimerCountdownTv;
    private LinearLayout mWatermarkCardContainer;
    private TextView mAntiCodeVerticalTv;
    private ImageView mFlashIv;
    private ImageView mPreviewThumbIv;
    private ImageView mShootIv;
    private LinearLayout mLayoutPlus;
    private TextView mPlusTv;
    private final TextView[] mZoomPills = new TextView[3];
    private final TextView[] mModeTabs = new TextView[3];
    private final View[] mModeDots = new View[3];

    // Original Bottom Action Bar Layouts (layout_preview_global_bottom.xml)
    private RelativeLayout mNormalActionBar;
    private LinearLayout mAttendActionBar;
    private RelativeLayout mVideoRecordingActionBar;
    private View mWorkStatusDot;
    private TextView mWorkStatusTv;
    private TextView mClockTypeTv;
    private TextView mStartBreakTv;
    private TextView mRecordDurationTv;
    private boolean mIsWorkingState = false;

    // Live Clock Ticker References
    private TextView mLiveBigClockTv;
    private TextView mLiveDateTv;
    private TextView mLiveWeatherTv;
    private String mDatePattern = "dd MMM yyyy";

    // Camera2 Hardware & Video Recording State
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private boolean mUseFrontCamera = false;
    private Rect mSensorActiveArraySize;
    private boolean mIsRecordingVideo = false;
    private long mVideoStartTimeMs = 0L;
    private File mCurrentVideoFile = null;
    private final List<Bitmap> mRecordedVideoFrames = new ArrayList<Bitmap>();
    private boolean mCountdownActive = false;

    private final Runnable mVideoFrameSampler = new Runnable() {
        @Override
        public void run() {
            if (!mIsRecordingVideo) return;
            if (mRecordedVideoFrames.size() < 75) {
                Bitmap wf = captureFastWatermarkedVideoFrame();
                if (wf != null) {
                    mRecordedVideoFrames.add(wf);
                }
            }
            if (mIsRecordingVideo) {
                mUiHandler.postDelayed(this, 140);
            }
        }
    };

    private final Runnable mClockTicker = new Runnable() {
        @Override
        public void run() {
            updateClockTexts();
            if (mIsRecordingVideo && mRecordDurationTv != null) {
                int sec = Math.max(1, (int) ((System.currentTimeMillis() - mVideoStartTimeMs) / 1000L));
                mRecordDurationTv.setText(String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60));
            }
            mUiHandler.postDelayed(this, 1000);
        }
    };

    private int dp(float val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, getResources().getDisplayMetrics());
    }

    private GradientDrawable roundedRect(int fillColor, float radiusDp, int strokePx, int strokeColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        gd.setCornerRadius(dp(radiusDp));
        if (strokePx > 0) gd.setStroke(strokePx, strokeColor);
        return gd;
    }

    private GradientDrawable roundedCorners(int fillColor, float topLeftDp, float topRightDp, float bottomRightDp, float bottomLeftDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        float tl = dp(topLeftDp);
        float tr = dp(topRightDp);
        float br = dp(bottomRightDp);
        float bl = dp(bottomLeftDp);
        gd.setCornerRadii(new float[]{tl, tl, tr, tr, br, br, bl, bl});
        return gd;
    }

    private void configureAutoFitText(TextView tv, String text, float baseSp, float minSp, int maxWidthDp) {
        String safe = (text != null) ? text : "";
        tv.setText(safe);
        tv.setMaxWidth(dp(maxWidthDp));
        tv.setEllipsize(null);
        Paint paint = new Paint(tv.getPaint());
        float pxAtBase = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, baseSp, getResources().getDisplayMetrics());
        paint.setTextSize(pxAtBase);
        float measured = paint.measureText(safe);
        float availPx = dp(maxWidthDp - 14);
        if (measured <= availPx || measured <= 0f) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp);
            tv.setSingleLine(false);
            tv.setMaxLines(4);
        } else {
            float scaledSp = baseSp * (availPx / measured);
            if (scaledSp >= minSp) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSp);
                tv.setSingleLine(false);
                tv.setMaxLines(4);
            } else {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, minSp);
                tv.setSingleLine(false);
                tv.setMaxLines(6);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        Window w = getWindow();
        if (w != null && Build.VERSION.SDK_INT >= 23) {
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            w.setStatusBarColor(0xFFFFFFFF);
            w.setNavigationBarColor(0xFFFFFFFF);
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= 26) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            w.getDecorView().setSystemUiVisibility(flags);
        }

        loadOriginalFonts();
        mModel.loadState(this);
        if (mModel.cameraMode < 0 || mModel.cameraMode > 2) {
            mModel.cameraMode = 1;
        }
        setContentView(buildOriginalPreviewActivityLayout());

        requestCameraAndLocationPermissions();
        initLiveGpsListener();
    }

    private void loadOriginalFonts() {
        try {
            mFontBebas = Typeface.createFromAsset(getAssets(), "fonts/BebasNeue.otf");
        } catch (Throwable t) {
            mFontBebas = Typeface.DEFAULT_BOLD;
        }
        try {
            mFontSegment = Typeface.createFromAsset(getAssets(), "fonts/Segment7Standard.otf");
        } catch (Throwable t) {
            mFontSegment = Typeface.MONOSPACE;
        }
        try {
            mFontMono = Typeface.createFromAsset(getAssets(), "fonts/RobotoMono-Bold.ttf");
        } catch (Throwable t) {
            mFontMono = Typeface.MONOSPACE;
        }
    }

    // Builds the exact layout hierarchy of activity_camera_preview.xml
    private View buildOriginalPreviewActivityLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFFFFF);
        root.setFitsSystemWindows(true);
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // =================================================================================
        // 1. TOP BAR: view_preview_title_normal_global.xml (height 52dp, white #FFFFFF)
        // =================================================================================
        RelativeLayout topBar = new RelativeLayout(this);
        topBar.setBackgroundColor(0xFFFFFFFF);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        // Left: menuIv (home_tool_border_menu, 40dp x 40dp, marginStart 12dp)
        ImageView menuIv = new ImageView(this);
        menuIv.setId(View.generateViewId());
        menuIv.setImageResource(R.drawable.home_tool_border_menu);
        menuIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        menuIv.setPadding(dp(8), dp(8), dp(8), dp(8));
        RelativeLayout.LayoutParams menuLp = new RelativeLayout.LayoutParams(dp(40), dp(40));
        menuLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        menuLp.addRule(RelativeLayout.CENTER_VERTICAL);
        menuLp.setMarginStart(dp(12));
        menuIv.setLayoutParams(menuLp);
        menuIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showLeftSettingsDrawer(MainActivity.this, mModel, new Runnable() {
                    @Override
                    public void run() {
                        applyAspectRatioLayout();
                    }
                }, new Runnable() {
                    @Override
                    public void run() {
                        rebuildWatermarkOverlay();
                    }
                });
            }
        });
        topBar.addView(menuIv);

        // Left-2: flashIv (flash_off_dark, 40dp x 40dp, marginStart 60dp)
        mFlashIv = new ImageView(this);
        mFlashIv.setImageResource(R.drawable.flash_off_dark);
        mFlashIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mFlashIv.setPadding(dp(8), dp(8), dp(8), dp(8));
        RelativeLayout.LayoutParams flashLp = new RelativeLayout.LayoutParams(dp(40), dp(40));
        flashLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        flashLp.addRule(RelativeLayout.CENTER_VERTICAL);
        flashLp.setMarginStart(dp(60));
        mFlashIv.setLayoutParams(flashLp);
        mFlashIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mModel.flashMode = (mModel.flashMode + 1) % 3;
                if (mModel.flashMode == 0) {
                    mFlashIv.setImageResource(R.drawable.flash_off_dark);
                    Toast.makeText(MainActivity.this, "Flash: Off", Toast.LENGTH_SHORT).show();
                } else if (mModel.flashMode == 1) {
                    mFlashIv.setImageResource(R.drawable.flash_auto_dark);
                    Toast.makeText(MainActivity.this, "Flash: Auto", Toast.LENGTH_SHORT).show();
                } else {
                    mFlashIv.setImageResource(R.drawable.flash_on_dark);
                    Toast.makeText(MainActivity.this, "Flash: On", Toast.LENGTH_SHORT).show();
                }
                updateCameraPreviewSession();
            }
        });
        topBar.addView(mFlashIv);

        // Center: operation_layout (bg_operation_layout_black: 1dp #444444 stroke pill with Verify Center)
        LinearLayout opLayout = new LinearLayout(this);
        opLayout.setOrientation(LinearLayout.HORIZONTAL);
        opLayout.setGravity(Gravity.CENTER_VERTICAL);
        opLayout.setBackground(roundedRect(0xFFFFFFFF, 18, dp(1), 0xFF444444));
        opLayout.setPadding(dp(10), dp(4), dp(10), dp(4));
        RelativeLayout.LayoutParams opLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        opLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        opLayout.setLayoutParams(opLp);

        ImageView opIcon = new ImageView(this);
        opIcon.setImageResource(R.drawable.tool_border_safe_green);
        opLayout.addView(opIcon, new LinearLayout.LayoutParams(dp(15), dp(15)));

        TextView opText = new TextView(this);
        opText.setText(" Verify Center");
        opText.setTextSize(13);
        opText.setTextColor(0xFF444444);
        opLayout.addView(opText);
        opLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showVerifyCenterDialog(MainActivity.this, mModel);
            }
        });
        topBar.addView(opLayout);

        // Right Container: layoutPlus (#282828 pill with ic_vip_crown + "Plus") + ivSwitchCamera
        LinearLayout rightContainer = new LinearLayout(this);
        rightContainer.setOrientation(LinearLayout.HORIZONTAL);
        rightContainer.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams rcLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rcLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        rcLp.addRule(RelativeLayout.CENTER_VERTICAL);
        rcLp.setMarginEnd(dp(16));
        rightContainer.setLayoutParams(rcLp);

        mLayoutPlus = new LinearLayout(this);
        mLayoutPlus.setOrientation(LinearLayout.HORIZONTAL);
        mLayoutPlus.setGravity(Gravity.CENTER_VERTICAL);
        mLayoutPlus.setBackground(roundedRect(0xFF282828, 12, 0, 0));
        mLayoutPlus.setPadding(dp(8), dp(3), dp(8), dp(3));
        LinearLayout.LayoutParams plusLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(24));
        plusLp.setMarginEnd(dp(12));
        mLayoutPlus.setLayoutParams(plusLp);

        ImageView crownIv = new ImageView(this);
        crownIv.setImageResource(R.drawable.ic_vip_crown);
        mLayoutPlus.addView(crownIv, new LinearLayout.LayoutParams(dp(14), dp(14)));

        mPlusTv = new TextView(this);
        mPlusTv.setText(" Plus");
        mPlusTv.setTextSize(12);
        mPlusTv.setTypeface(Typeface.DEFAULT_BOLD);
        mPlusTv.setTextColor(0xFFFFFFFF);
        mLayoutPlus.addView(mPlusTv);
        mLayoutPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showPurchaseDialog(MainActivity.this, mModel, new Runnable() {
                    @Override
                    public void run() {
                        rebuildWatermarkOverlay();
                    }
                });
            }
        });
        rightContainer.addView(mLayoutPlus);

        ImageView switchCamIv = new ImageView(this);
        switchCamIv.setImageResource(R.drawable.img_camera_switch);
        switchCamIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        switchCamIv.setLayoutParams(new LinearLayout.LayoutParams(dp(30), dp(30)));
        switchCamIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUseFrontCamera = !mUseFrontCamera;
                closeCameraHardware();
                openCameraHardware();
            }
        });
        rightContainer.addView(switchCamIv);

        topBar.addView(rightContainer);
        root.addView(topBar);

        // =================================================================================
        // 2. CAMERA VIEWFINDER: view_camera / fl_water_container (4:3 default)
        // =================================================================================
        mRootLayout = root;
        FrameLayout centerHost = new FrameLayout(this);
        mCenterHost = centerHost;
        centerHost.setBackgroundColor(0xFF000000);
        centerHost.setClipChildren(false);
        centerHost.setClipToPadding(false);
        int screenW = getResources().getDisplayMetrics().widthPixels;
        int initialVfH = Math.round(screenW * 4f / 3f);
        centerHost.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, initialVfH));

        mViewfinderContainer = new FrameLayout(this);
        mViewfinderContainer.setBackgroundColor(0xFF101820);
        mViewfinderContainer.setClipChildren(false);
        mViewfinderContainer.setClipToPadding(false);
        FrameLayout.LayoutParams vfLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vfLp.gravity = Gravity.CENTER;
        mViewfinderContainer.setLayoutParams(vfLp);

        // Fallback Scene Canvas underneath TextureView
        View fallbackScene = new View(this) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override
            protected void onDraw(Canvas c) {
                super.onDraw(c);
                int w = getWidth();
                int h = getHeight();
                p.setShader(new LinearGradient(0, 0, 0, h * 0.58f, 0xFF4A90E2, 0xFF87CEEB, Shader.TileMode.CLAMP));
                c.drawRect(0, 0, w, h * 0.58f, p);
                p.setShader(null);
                p.setColor(0xFF566573);
                c.drawRect(0, h * 0.58f, w, h, p);
                p.setColor(0xFF2C3E50);
                c.drawRect(w * 0.12f, h * 0.18f, w * 0.52f, h * 0.58f, p);
                p.setColor(0xFF34495E);
                c.drawRect(w * 0.56f, h * 0.25f, w * 0.88f, h * 0.58f, p);
            }
        };
        mViewfinderContainer.addView(fallbackScene, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Live Hardware Camera2 TextureView
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                configureTextureViewTransform(width, height);
                openCameraHardware();
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                configureTextureViewTransform(width, height);
            }
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                closeCameraHardware();
                return true;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
        });
        mTextureView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int w = right - left;
                int h = bottom - top;
                if (w > 0 && h > 0 && (w != (oldRight - oldLeft) || h != (oldBottom - oldTop))) {
                    configureTextureViewTransform(w, h);
                }
            }
        });
        mViewfinderContainer.addView(mTextureView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Right-Edge Vertical Anti-Fake Photo Code (anti_code_view in activity_camera_preview.xml)
        mAntiCodeVerticalTv = new TextView(this);
        mAntiCodeVerticalTv.setTypeface(mFontMono, Typeface.BOLD);
        mAntiCodeVerticalTv.setTextSize(10);
        mAntiCodeVerticalTv.setTextColor(0xCCFFFFFF);
        mAntiCodeVerticalTv.setShadowLayer(3f, 0f, 1f, 0xCC000000);
        mAntiCodeVerticalTv.setRotation(-90f);
        FrameLayout.LayoutParams acLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        acLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        acLp.setMarginEnd(dp(-48));
        mAntiCodeVerticalTv.setLayoutParams(acLp);
        mViewfinderContainer.addView(mAntiCodeVerticalTv);

        // Right-Side Zoom Scale Widget (layout_camera_scale_widget.xml: 2 | 1x | .6)
        LinearLayout zoomCol = new LinearLayout(this);
        zoomCol.setOrientation(LinearLayout.VERTICAL);
        zoomCol.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams zColLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        zColLp.gravity = Gravity.BOTTOM | Gravity.END;
        zColLp.setMargins(0, 0, dp(12), dp(18));
        zoomCol.setLayoutParams(zColLp);

        final float[] zoomVals = new float[]{2.0f, 1.0f, 0.6f};
        final String[] zoomLabels = new String[]{"2", "1x", ".6"};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            TextView zTv = new TextView(this);
            zTv.setText(zoomLabels[i]);
            zTv.setTextSize(10);
            zTv.setTypeface(Typeface.DEFAULT_BOLD);
            zTv.setTextColor(0xFFFFFFFF);
            zTv.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams zLp = new LinearLayout.LayoutParams(dp(28), dp(28));
            zLp.setMargins(0, dp(5), 0, dp(5));
            zTv.setLayoutParams(zLp);
            zTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mModel.zoomLevel = zoomVals[idx];
                    updateZoomPillsUI();
                    updateCameraPreviewSession();
                }
            });
            mZoomPills[i] = zTv;
            zoomCol.addView(zTv);
        }
        updateZoomPillsUI();
        mViewfinderContainer.addView(zoomCol);

        // Optional Grid Lines (3x3) & Leveler Overlay
        mGridLevelerView = new View(this) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override
            protected void onDraw(Canvas c) {
                super.onDraw(c);
                int w = getWidth();
                int h = getHeight();
                if (mModel.showGridLines) {
                    p.setColor(0x66FFFFFF);
                    p.setStrokeWidth(dp(1));
                    c.drawLine(w / 3f, 0, w / 3f, h, p);
                    c.drawLine(2f * w / 3f, 0, 2f * w / 3f, h, p);
                    c.drawLine(0, h / 3f, w, h / 3f, p);
                    c.drawLine(0, 2f * h / 3f, w, 2f * h / 3f, p);
                }
                if (mModel.showLeveler) {
                    p.setColor(0xFF00E676);
                    p.setStrokeWidth(dp(2));
                    float cy = h * 0.45f;
                    c.drawLine(w * 0.32f, cy, w * 0.68f, cy, p);
                    p.setStyle(Paint.Style.STROKE);
                    c.drawCircle(w * 0.5f, cy, dp(10), p);
                    p.setStyle(Paint.Style.FILL);
                }
            }
        };
        mGridLevelerView.setClickable(false);
        mViewfinderContainer.addView(mGridLevelerView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Center Timer Countdown Overlay (3... 2... 1...)
        mTimerCountdownTv = new TextView(this);
        mTimerCountdownTv.setTextSize(72);
        mTimerCountdownTv.setTypeface(mFontBebas, Typeface.BOLD);
        mTimerCountdownTv.setTextColor(0xFFFFFFFF);
        mTimerCountdownTv.setShadowLayer(8f, 0f, 3f, 0xCC000000);
        mTimerCountdownTv.setVisibility(View.GONE);
        FrameLayout.LayoutParams cdLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cdLp.gravity = Gravity.CENTER;
        mTimerCountdownTv.setLayoutParams(cdLp);
        mViewfinderContainer.addView(mTimerCountdownTv);

        // Bottom-Left Draggable & Clickable Live Watermark Overlay (waterMarkLayoutRl)
        mWatermarkCardContainer = new LinearLayout(this);
        mWatermarkCardContainer.setOrientation(LinearLayout.VERTICAL);
        mWatermarkCardContainer.setClipChildren(false);
        mWatermarkCardContainer.setClipToPadding(false);
        FrameLayout.LayoutParams wmLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wmLp.gravity = Gravity.BOTTOM | Gravity.START;
        wmLp.setMargins(dp(10), dp(10), dp(42), dp(12));
        mWatermarkCardContainer.setLayoutParams(wmLp);
        setupDraggableAndClickableWatermark(mWatermarkCardContainer);
        mViewfinderContainer.addView(mWatermarkCardContainer);

        // Shutter Flash Overlay
        mShutterFlashView = new View(this);
        mShutterFlashView.setBackgroundColor(0xFFFFFFFF);
        mShutterFlashView.setAlpha(0f);
        mShutterFlashView.setClickable(false);
        mViewfinderContainer.addView(mShutterFlashView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        centerHost.addView(mViewfinderContainer);
        root.addView(centerHost);

        // =================================================================================
        // 3. BOTTOM MODE TABS: layout_preview_bottom_fun_global.xml (white #FFFFFF)
        //    Exact 3 Original Modes: Video | Photo | Attendance
        // =================================================================================
        LinearLayout modeSection = new LinearLayout(this);
        modeSection.setOrientation(LinearLayout.VERTICAL);
        modeSection.setGravity(Gravity.CENTER_HORIZONTAL);
        modeSection.setBackgroundColor(0xFFFFFFFF);
        modeSection.setPadding(0, dp(8), 0, dp(2));
        modeSection.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout modeRow = new LinearLayout(this);
        modeRow.setOrientation(LinearLayout.HORIZONTAL);
        modeRow.setGravity(Gravity.CENTER);
        modeRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        String[] modes = new String[]{"Video", "Photo", "Attendance"};
        for (int i = 0; i < 3; i++) {
            final int idx = i;
            LinearLayout tabCol = new LinearLayout(this);
            tabCol.setOrientation(LinearLayout.VERTICAL);
            tabCol.setGravity(Gravity.CENTER_HORIZONTAL);
            tabCol.setPadding(dp(18), dp(2), dp(18), dp(2));

            TextView tv = new TextView(this);
            tv.setText(modes[i]);
            tv.setSingleLine(true);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(14f);
            tabCol.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            View dot = new View(this);
            dot.setBackground(roundedRect(0xFF222222, 2.5f, 0, 0));
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(4), dp(4));
            dotLp.setMargins(0, dp(3), 0, 0);
            dot.setLayoutParams(dotLp);
            tabCol.addView(dot);

            mModeTabs[i] = tv;
            mModeDots[i] = dot;

            tabCol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsRecordingVideo) {
                        stopVideoRecording();
                    }
                    mModel.cameraMode = idx;
                    if (idx == 2) {
                        // Attendance mode switches to Clock In watermark automatically
                        mModel.selectTemplate(mModel.templates.get(2));
                    } else if (idx == 1 && "21".equals(mModel.activeTemplate.id)) {
                        mModel.selectTemplate(mModel.templates.get(0));
                    }
                    rebuildWatermarkOverlay();
                    updateModeTabsUI();
                }
            });
            modeRow.addView(tabCol, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        modeSection.addView(modeRow);
        root.addView(modeSection);

        // =================================================================================
        // 4. BOTTOM CAMERA ACTION HOST: layout_preview_global_bottom.xml
        //    Contains:
        //    - view_preview_bottom_camera_action_global.xml (Photo / Video idle)
        //    - view_preview_bottom_camera_action_attend_global.xml (Attendance mode)
        //    - view_preview_bottom_camera_video_action_global.xml (Video recording active)
        // =================================================================================
        FrameLayout bottomActionHost = new FrameLayout(this);
        mBottomActionHost = bottomActionHost;
        bottomActionHost.setBackgroundColor(0xFFFFFFFF);
        bottomActionHost.setMinimumHeight(dp(104));
        bottomActionHost.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // 4A. Normal Action Bar (view_preview_bottom_camera_action_global.xml)
        mNormalActionBar = new RelativeLayout(this);
        mNormalActionBar.setBackgroundColor(0xFFFFFFFF);
        mNormalActionBar.setPadding(dp(16), dp(4), dp(16), dp(14));
        mNormalActionBar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Center: shootIv (80dp x 80dp, @drawable/cam_shoot)
        mShootIv = new ImageView(this);
        mShootIv.setId(View.generateViewId());
        mShootIv.setImageResource(R.drawable.cam_shoot);
        mShootIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams shootLp = new RelativeLayout.LayoutParams(dp(80), dp(80));
        shootLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mShootIv.setLayoutParams(shootLp);
        mShootIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShutterButtonClicked();
            }
        });
        mNormalActionBar.addView(mShootIv);

        // Left: clPreview (36dp thumbnail + "Preview") + clFolder (36dp icon_preview_folder + "Teamspace")
        LinearLayout leftGroup = new LinearLayout(this);
        leftGroup.setOrientation(LinearLayout.HORIZONTAL);
        leftGroup.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams lgLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lgLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        lgLp.addRule(RelativeLayout.CENTER_VERTICAL);
        lgLp.setMarginStart(dp(8));
        leftGroup.setLayoutParams(lgLp);

        // clPreview
        LinearLayout clPreview = new LinearLayout(this);
        clPreview.setOrientation(LinearLayout.VERTICAL);
        clPreview.setGravity(Gravity.CENTER_HORIZONTAL);
        clPreview.setMinimumWidth(dp(56));
        clPreview.setPadding(dp(6), dp(2), dp(6), dp(2));

        mPreviewThumbIv = new ImageView(this);
        mPreviewThumbIv.setImageResource(R.drawable.gallery_light);
        mPreviewThumbIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
        mPreviewThumbIv.setBackground(roundedRect(0xFFF0F0F0, 6, dp(1), 0xFFDEDEDE));
        clPreview.addView(mPreviewThumbIv, new LinearLayout.LayoutParams(dp(36), dp(36)));

        TextView previewTitle = new TextView(this);
        previewTitle.setText("Preview");
        previewTitle.setSingleLine(true);
        previewTitle.setTextSize(12);
        previewTitle.setTypeface(Typeface.DEFAULT_BOLD);
        previewTitle.setTextColor(0xFF666666);
        previewTitle.setPadding(0, dp(3), 0, 0);
        clPreview.addView(previewTitle);
        clPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showLocalPreviewDialog(MainActivity.this, mModel, new Runnable() {
                    @Override
                    public void run() {
                        rebuildWatermarkOverlay();
                    }
                });
            }
        });
        leftGroup.addView(clPreview);

        // clFolder (Teamspace)
        LinearLayout clFolder = new LinearLayout(this);
        clFolder.setOrientation(LinearLayout.VERTICAL);
        clFolder.setGravity(Gravity.CENTER_HORIZONTAL);
        clFolder.setPadding(dp(8), dp(2), dp(8), dp(2));

        FrameLayout folderIconWrap = new FrameLayout(this);
        ImageView folderImg = new ImageView(this);
        folderImg.setImageResource(R.drawable.icon_preview_folder);
        folderImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        folderIconWrap.addView(folderImg, new FrameLayout.LayoutParams(dp(36), dp(36)));

        View blueNotifyDot = new View(this);
        blueNotifyDot.setBackground(roundedRect(0xFF0093FF, 5, dp(1), 0xFFFFFFFF));
        FrameLayout.LayoutParams bnLp = new FrameLayout.LayoutParams(dp(9), dp(9));
        bnLp.gravity = Gravity.TOP | Gravity.END;
        folderIconWrap.addView(blueNotifyDot, bnLp);
        clFolder.addView(folderIconWrap);

        TextView folderTitle = new TextView(this);
        folderTitle.setText("Teamspace");
        folderTitle.setTextSize(12);
        folderTitle.setTypeface(Typeface.DEFAULT_BOLD);
        folderTitle.setTextColor(0xFF666666);
        folderTitle.setPadding(0, dp(3), 0, 0);
        clFolder.addView(folderTitle);
        clFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showTeamspaceDialog(MainActivity.this, mModel, new Runnable() {
                    @Override
                    public void run() {
                        rebuildWatermarkOverlay();
                    }
                });
            }
        });
        leftGroup.addView(clFolder);
        mNormalActionBar.addView(leftGroup);

        // Right: clTemplate (ic_watermark_ltr + red dot + "Templates")
        LinearLayout clTemplate = new LinearLayout(this);
        clTemplate.setOrientation(LinearLayout.VERTICAL);
        clTemplate.setGravity(Gravity.CENTER_HORIZONTAL);
        clTemplate.setPadding(dp(8), dp(2), dp(8), dp(2));
        RelativeLayout.LayoutParams tmplLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmplLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        tmplLp.addRule(RelativeLayout.CENTER_VERTICAL);
        tmplLp.setMarginEnd(dp(20));
        clTemplate.setLayoutParams(tmplLp);

        FrameLayout wmIconWrap = new FrameLayout(this);
        ImageView wmImg = new ImageView(this);
        wmImg.setImageResource(R.drawable.ic_watermark_ltr);
        wmImg.setScaleType(ImageView.ScaleType.FIT_CENTER);
        wmIconWrap.addView(wmImg, new FrameLayout.LayoutParams(dp(36), dp(36)));

        View redDot = new View(this);
        redDot.setBackground(roundedRect(0xFFFF004D, 5, dp(1), 0xFFFFFFFF));
        FrameLayout.LayoutParams rdLp = new FrameLayout.LayoutParams(dp(9), dp(9));
        rdLp.gravity = Gravity.TOP | Gravity.END;
        wmIconWrap.addView(redDot, rdLp);
        clTemplate.addView(wmIconWrap);

        TextView wmTv = new TextView(this);
        wmTv.setText("Templates");
        wmTv.setTextSize(12);
        wmTv.setTypeface(Typeface.DEFAULT_BOLD);
        wmTv.setTextColor(0xFF666666);
        wmTv.setPadding(0, dp(3), 0, 0);
        clTemplate.addView(wmTv);
        clTemplate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showTemplatesSheet(MainActivity.this, mModel, new Runnable() {
                    @Override
                    public void run() {
                        rebuildWatermarkOverlay();
                    }
                });
            }
        });
        mNormalActionBar.addView(clTemplate);
        bottomActionHost.addView(mNormalActionBar);

        // 4B. Attendance Action Bar (view_preview_bottom_camera_action_attend_global.xml)
        mAttendActionBar = new LinearLayout(this);
        mAttendActionBar.setOrientation(LinearLayout.VERTICAL);
        mAttendActionBar.setGravity(Gravity.CENTER_HORIZONTAL);
        mAttendActionBar.setBackgroundColor(0xFFFFFFFF);
        mAttendActionBar.setPadding(dp(16), dp(2), dp(16), dp(10));
        mAttendActionBar.setVisibility(View.GONE);
        mAttendActionBar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Top Work Status Row (dot + tvWorkStatus)
        LinearLayout statusRow = new LinearLayout(this);
        statusRow.setOrientation(LinearLayout.HORIZONTAL);
        statusRow.setGravity(Gravity.CENTER);
        mWorkStatusDot = new View(this);
        mWorkStatusDot.setBackground(roundedRect(0xFFB5B5B8, 4, 0, 0));
        LinearLayout.LayoutParams wsDotLp = new LinearLayout.LayoutParams(dp(8), dp(8));
        wsDotLp.setMargins(0, 0, dp(5), 0);
        statusRow.addView(mWorkStatusDot, wsDotLp);

        mWorkStatusTv = new TextView(this);
        mWorkStatusTv.setText("Off Work");
        mWorkStatusTv.setTextSize(12);
        mWorkStatusTv.setTextColor(0xFF444444);
        statusRow.addView(mWorkStatusTv);
        mAttendActionBar.addView(statusRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Bottom Attendance Row: Left Timesheet | Center Clock In Pill | Right Start Break
        LinearLayout attendBottomRow = new LinearLayout(this);
        attendBottomRow.setOrientation(LinearLayout.HORIZONTAL);
        attendBottomRow.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams abLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(64));
        abLp.setMargins(0, dp(6), 0, 0);
        attendBottomRow.setLayoutParams(abLp);

        // Left: clTimeSheet (width 68dp)
        LinearLayout clTimeSheet = new LinearLayout(this);
        clTimeSheet.setOrientation(LinearLayout.VERTICAL);
        clTimeSheet.setGravity(Gravity.CENTER);
        clTimeSheet.setLayoutParams(new LinearLayout.LayoutParams(dp(68), ViewGroup.LayoutParams.WRAP_CONTENT));

        ImageView tsIv = new ImageView(this);
        tsIv.setImageResource(R.drawable.attendance_timesheet_calendar);
        tsIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        clTimeSheet.addView(tsIv, new LinearLayout.LayoutParams(dp(36), dp(36)));

        TextView tsTv = new TextView(this);
        tsTv.setText("Timesheet");
        tsTv.setTextSize(12);
        tsTv.setTypeface(Typeface.DEFAULT_BOLD);
        tsTv.setTextColor(0xFF666666);
        tsTv.setSingleLine(true);
        clTimeSheet.addView(tsTv);
        clTimeSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showTeamspaceDialog(MainActivity.this, mModel, new Runnable() {
                    @Override
                    public void run() {
                        rebuildWatermarkOverlay();
                    }
                });
            }
        });
        attendBottomRow.addView(clTimeSheet);

        // Center: clAttendClock (#0070F4 pill height 58dp, radius 29dp)
        LinearLayout clAttendClock = new LinearLayout(this);
        clAttendClock.setOrientation(LinearLayout.HORIZONTAL);
        clAttendClock.setGravity(Gravity.CENTER);
        clAttendClock.setBackground(roundedRect(0xFF0070F4, 30, 0, 0));
        LinearLayout.LayoutParams acBtnLp = new LinearLayout.LayoutParams(0, dp(58), 1f);
        acBtnLp.setMargins(dp(16), 0, dp(16), 0);
        clAttendClock.setLayoutParams(acBtnLp);

        ImageView camIcon = new ImageView(this);
        camIcon.setImageResource(R.drawable.tool_border_takephoto);
        camIcon.setColorFilter(0xFFFFFFFF);
        LinearLayout.LayoutParams ciLp = new LinearLayout.LayoutParams(dp(24), dp(24));
        ciLp.setMargins(0, 0, dp(6), 0);
        clAttendClock.addView(camIcon, ciLp);

        mClockTypeTv = new TextView(this);
        mClockTypeTv.setText("Clock In");
        mClockTypeTv.setTextSize(22);
        mClockTypeTv.setTypeface(Typeface.DEFAULT_BOLD);
        mClockTypeTv.setTextColor(0xFFFFFFFF);
        mClockTypeTv.setSingleLine(true);
        clAttendClock.addView(mClockTypeTv);
        clAttendClock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShutterButtonClicked();
                mIsWorkingState = !mIsWorkingState;
                updateAttendanceStatusUI();
            }
        });
        attendBottomRow.addView(clAttendClock);

        // Right: tvStartBreak (width 68dp, height 56dp, #F0F0F0 radius 8dp)
        mStartBreakTv = new TextView(this);
        mStartBreakTv.setText("Start Break");
        mStartBreakTv.setTextSize(12);
        mStartBreakTv.setTypeface(Typeface.DEFAULT_BOLD);
        mStartBreakTv.setTextColor(0xFF222222);
        mStartBreakTv.setGravity(Gravity.CENTER);
        mStartBreakTv.setBackground(roundedRect(0xFFF0F0F0, 8, 0, 0));
        mStartBreakTv.setPadding(dp(4), dp(4), dp(4), dp(4));
        mStartBreakTv.setLayoutParams(new LinearLayout.LayoutParams(dp(68), dp(56)));
        mStartBreakTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("Start Break".equals(mModel.customTitle)) {
                    mModel.customTitle = "End Break";
                    mStartBreakTv.setText("Clock Out");
                } else if ("End Break".equals(mModel.customTitle)) {
                    mModel.customTitle = "Clock Out";
                    mStartBreakTv.setText("Clock In");
                } else if ("Clock Out".equals(mModel.customTitle)) {
                    mModel.customTitle = "Clock In";
                    mStartBreakTv.setText("Start Break");
                } else {
                    mModel.customTitle = "Start Break";
                    mStartBreakTv.setText("End Break");
                }
                mModel.showTitle = true;
                mClockTypeTv.setText(mModel.customTitle);
                rebuildWatermarkOverlay();
            }
        });
        attendBottomRow.addView(mStartBreakTv);
        mAttendActionBar.addView(attendBottomRow);
        bottomActionHost.addView(mAttendActionBar);

        // 4C. Video Recording Action Bar (view_preview_bottom_camera_video_action_global.xml)
        mVideoRecordingActionBar = new RelativeLayout(this);
        mVideoRecordingActionBar.setBackgroundColor(0xFFFFFFFF);
        mVideoRecordingActionBar.setPadding(dp(16), dp(2), dp(16), dp(8));
        mVideoRecordingActionBar.setVisibility(View.GONE);
        mVideoRecordingActionBar.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Top recording duration pill
        LinearLayout recTopPill = new LinearLayout(this);
        recTopPill.setOrientation(LinearLayout.HORIZONTAL);
        recTopPill.setGravity(Gravity.CENTER);
        recTopPill.setBackground(roundedRect(0xFFF0F0F0, 4, 0, 0));
        recTopPill.setPadding(dp(8), dp(2), dp(8), dp(2));
        RelativeLayout.LayoutParams rtpLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(20));
        rtpLp.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        rtpLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        recTopPill.setLayoutParams(rtpLp);

        View recRedDot = new View(this);
        recRedDot.setBackground(roundedRect(0xFFFF004D, 3, 0, 0));
        LinearLayout.LayoutParams rrdLp = new LinearLayout.LayoutParams(dp(6), dp(6));
        rrdLp.setMargins(0, 0, dp(5), 0);
        recTopPill.addView(recRedDot, rrdLp);

        mRecordDurationTv = new TextView(this);
        mRecordDurationTv.setText("00:01");
        mRecordDurationTv.setTextSize(12);
        mRecordDurationTv.setTextColor(0xFF000000);
        recTopPill.addView(mRecordDurationTv);
        mVideoRecordingActionBar.addView(recTopPill);

        // Center stop button (video_stop_normal, 58dp x 58dp inside 76dp ring)
        FrameLayout stopWrap = new FrameLayout(this);
        stopWrap.setBackground(roundedRect(0xFFF0F0F0, 38, dp(3), 0xFFFF004D));
        RelativeLayout.LayoutParams swLp = new RelativeLayout.LayoutParams(dp(74), dp(74));
        swLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
        swLp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        stopWrap.setLayoutParams(swLp);

        ImageView stopIv = new ImageView(this);
        stopIv.setImageResource(R.drawable.cam_shooting);
        FrameLayout.LayoutParams sIvLp = new FrameLayout.LayoutParams(dp(54), dp(54));
        sIvLp.gravity = Gravity.CENTER;
        stopWrap.addView(stopIv, sIvLp);
        stopWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopVideoRecording();
            }
        });
        mVideoRecordingActionBar.addView(stopWrap);

        // Right snapshot-while-recording button (cam_shoot, 40dp x 40dp)
        ImageView snapDuringVideoIv = new ImageView(this);
        snapDuringVideoIv.setImageResource(R.drawable.cam_shoot);
        RelativeLayout.LayoutParams sdvLp = new RelativeLayout.LayoutParams(dp(40), dp(40));
        sdvLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        sdvLp.addRule(RelativeLayout.CENTER_VERTICAL);
        sdvLp.setMarginEnd(dp(36));
        snapDuringVideoIv.setLayoutParams(sdvLp);
        snapDuringVideoIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureWatermarkedPhoto();
            }
        });
        mVideoRecordingActionBar.addView(snapDuringVideoIv);
        bottomActionHost.addView(mVideoRecordingActionBar);

        root.addView(bottomActionHost);

        root.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int w = right - left;
                int h = bottom - top;
                if (w > 0 && h > 0 && (w != (oldRight - oldLeft) || h != (oldBottom - oldTop))) {
                    root.post(new Runnable() {
                        @Override
                        public void run() {
                            applyAspectRatioLayout();
                        }
                    });
                }
            }
        });
        root.post(new Runnable() {
            @Override
            public void run() {
                applyAspectRatioLayout();
            }
        });

        updateModeTabsUI();
        rebuildWatermarkOverlay();
        return root;
    }

    private void updateAttendanceStatusUI() {
        if (mWorkStatusDot == null || mWorkStatusTv == null || mClockTypeTv == null) return;
        if (mIsWorkingState) {
            mWorkStatusDot.setBackground(roundedRect(0xFF10B981, 4, 0, 0));
            mWorkStatusTv.setText("Working");
            mModel.customTitle = "Clock Out";
            mClockTypeTv.setText("Clock Out");
        } else {
            mWorkStatusDot.setBackground(roundedRect(0xFFB5B5B8, 4, 0, 0));
            mWorkStatusTv.setText("Off Work");
            mModel.customTitle = "Clock In";
            mClockTypeTv.setText("Clock In");
        }
        rebuildWatermarkOverlay();
    }

    // =========================================================================================
    // 1:1 AUTHENTIC WATERMARK OVERLAY BUILDER MATCHING DECOMPILED XML LAYOUTS
    // (100% Uncut Content, Multi-Line Wrapping, & Smart Viewfinder Auto-Fit)
    // =========================================================================================
    private void rebuildWatermarkOverlay() {
        if (mWatermarkCardContainer == null) return;
        mWatermarkCardContainer.removeAllViews();
        mWatermarkCardContainer.setTranslationX(0f);
        mWatermarkCardContainer.setTranslationY(0f);
        mWatermarkCardContainer.setBackground(null);
        mLiveBigClockTv = null;
        mLiveDateTv = null;
        mLiveWeatherTv = null;
        mDatePattern = "dd MMM yyyy";

        // Apply user-selected Stamp Position (0=Bottom-Left, 1=Bottom-Right, 2=Top-Left, 3=Top-Right)
        FrameLayout.LayoutParams wmLp = (FrameLayout.LayoutParams) mWatermarkCardContainer.getLayoutParams();
        if (wmLp != null) {
            if (mModel.stampPositionIndex == 1) {
                wmLp.gravity = Gravity.BOTTOM | Gravity.END;
                wmLp.setMargins(dp(10), dp(10), dp(42), dp(12));
            } else if (mModel.stampPositionIndex == 2) {
                wmLp.gravity = Gravity.TOP | Gravity.START;
                wmLp.setMargins(dp(10), dp(12), dp(42), dp(10));
            } else if (mModel.stampPositionIndex == 3) {
                wmLp.gravity = Gravity.TOP | Gravity.END;
                wmLp.setMargins(dp(10), dp(12), dp(42), dp(10));
            } else {
                wmLp.gravity = Gravity.BOTTOM | Gravity.START;
                wmLp.setMargins(dp(10), dp(10), dp(42), dp(12));
            }
            mWatermarkCardContainer.setLayoutParams(wmLp);
        }

        mAntiCodeVerticalTv.setVisibility(mModel.showPhotoCode ? View.VISIBLE : View.GONE);
        mAntiCodeVerticalTv.setText("Photo Code: " + mModel.currentPhotoCode);
        if (mPlusTv != null && mLayoutPlus != null) {
            mPlusTv.setText(mModel.isVipPlus ? " VIP" : " Plus");
            mLayoutPlus.setBackground(roundedRect(mModel.isVipPlus ? 0xFF7A6EFF : 0xFF282828, 12, 0, 0));
        }

        WatermarkModel.TemplateInfo tmpl = mModel.activeTemplate;
        String id = (tmpl != null) ? tmpl.id : "34";

        if ("21".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 21: CLOCK IN (1:1 watermark_cover_id21.xml)
            // ---------------------------------------------------------------------------------
            mDatePattern = "dd/MM/yyyy";
            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);
            appendOptionalBrandLogoHeader(wrap);

            LinearLayout topPill = new LinearLayout(this);
            topPill.setOrientation(LinearLayout.HORIZONTAL);
            topPill.setGravity(Gravity.CENTER_VERTICAL);
            topPill.setBackground(roundedRect(0xFFFFFFFF, 4, 0, 0));
            topPill.setPadding(dp(3), dp(3), dp(8), dp(3));

            if (mModel.showTitle) {
                TextView badgeTv = new TextView(this);
                String titleText = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : "Clock In";
                badgeTv.setTypeface(Typeface.DEFAULT_BOLD);
                badgeTv.setTextColor(0xFF111111);
                badgeTv.setGravity(Gravity.CENTER);
                badgeTv.setMinimumHeight(dp(24));
                badgeTv.setBackground(roundedRect(mModel.customAccentColor, 2.5f, 0, 0));
                badgeTv.setPadding(dp(7), dp(2), dp(7), dp(2));
                configureAutoFitText(badgeTv, titleText, 14f, 8.5f, 135);
                topPill.addView(badgeTv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            if (mModel.showTime) {
                mLiveBigClockTv = new TextView(this);
                mLiveBigClockTv.setTextSize(23);
                mLiveBigClockTv.setSingleLine(true);
                mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
                mLiveBigClockTv.setTextColor(0xFF0A2246);
                mLiveBigClockTv.setIncludeFontPadding(false);
                mLiveBigClockTv.setPadding(dp(7), 0, dp(2), 0);
                topPill.addView(mLiveBigClockTv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            if (topPill.getChildCount() > 0) {
                wrap.addView(topPill, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            LinearLayout rightCol = createVerticalBarContainer(mModel.customAccentColor, 3, 9);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            midLp.setMargins(0, dp(7), 0, 0);
            rightCol.setLayoutParams(midLp);

            if (mModel.showTime) {
                mLiveDateTv = createShadowText("", 13.5f, true, 265);
                rightCol.addView(mLiveDateTv);
            }
            if (mModel.showWeather && mModel.weatherLine != null && !mModel.weatherLine.trim().isEmpty()) {
                TextView wTv = createShadowText(mModel.weatherLine.trim(), 13f, false, 265);
                wTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(wTv);
            }
            if (mModel.showAddress) {
                TextView addrTv = createShadowText(mModel.addressLine, 13.5f, false, 265);
                addrTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(addrTv);
            }
            appendOptionalGeoAndNoteLines(rightCol, 265);
            wrap.addView(rightCol);

            appendOptionalMiniMapRow(wrap);
            appendTimeNowVerifiedFooter(wrap);
            mWatermarkCardContainer.addView(wrap);

        } else if ("80".equals(id) || "dasher01".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 80 (COMPLETED) & DASHER01 (DELIVERY SERVICE) (watermark_cover_id80.xml)
            // ---------------------------------------------------------------------------------
            boolean isDasher = "dasher01".equals(id);
            mDatePattern = "MM/dd/yyyy";
            int pillBg = isDasher ? 0xFFFF1717 : 0xFF391A13;
            int barColor = isDasher ? 0xFFFF1717 : mModel.customAccentColor;

            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);
            appendOptionalBrandLogoHeader(wrap);

            if (mModel.showTime) {
                LinearLayout topPill = new LinearLayout(this);
                topPill.setOrientation(LinearLayout.HORIZONTAL);
                topPill.setGravity(Gravity.CENTER_VERTICAL);
                topPill.setBackground(roundedRect(pillBg, 6, 0, 0));
                topPill.setPadding(dp(10), dp(4), dp(10), dp(4));

                mLiveBigClockTv = new TextView(this);
                mLiveBigClockTv.setTextSize(25);
                mLiveBigClockTv.setSingleLine(true);
                mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
                mLiveBigClockTv.setTextColor(0xFFFFFFFF);
                mLiveBigClockTv.setIncludeFontPadding(false);
                topPill.addView(mLiveBigClockTv);

                TextView chkBadge = new TextView(this);
                chkBadge.setText("✓");
                chkBadge.setTextSize(13);
                chkBadge.setTypeface(Typeface.DEFAULT_BOLD);
                chkBadge.setGravity(Gravity.CENTER);
                chkBadge.setTextColor(isDasher ? 0xFFFF1717 : 0xFF391A13);
                chkBadge.setBackground(roundedRect(isDasher ? 0xFFFFFFFF : mModel.customAccentColor, isDasher ? 4 : 11, 0, 0));
                LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(dp(22), dp(22));
                cbLp.setMargins(dp(8), 0, 0, 0);
                topPill.addView(chkBadge, cbLp);
                wrap.addView(topPill, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            LinearLayout rightCol = createVerticalBarContainer(barColor, 3, 10);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            midLp.setMargins(0, dp(6), 0, 0);
            rightCol.setLayoutParams(midLp);

            if (isDasher) {
                TextView starsTv = createShadowText("★ ★ ★ ★ ★", 13, true, 265);
                starsTv.setTextColor(0xFFFFD700);
                rightCol.addView(starsTv);
            }

            if (mModel.showTitle) {
                String titleText = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : (isDasher ? "TIPS & REVIEWS KEEP ME DRIVING" : "Completed");
                TextView titleTv = createShadowText(titleText, 15, true, 265);
                configureAutoFitText(titleTv, titleText, isDasher ? 13.5f : 15.5f, 10f, 265);
                rightCol.addView(titleTv);
            }

            // Authentic Delivery Business Card in dasher01 (matching cover_wm80_dasher01.webp & watermark_cover_id80.xml)
            if (isDasher) {
                LinearLayout bizCard = new LinearLayout(this);
                bizCard.setOrientation(LinearLayout.HORIZONTAL);
                bizCard.setGravity(Gravity.CENTER_VERTICAL);
                bizCard.setBackground(roundedRect(0xFFFFFFFF, 3, 0, 0));
                LinearLayout.LayoutParams bzLp = new LinearLayout.LayoutParams(dp(235), ViewGroup.LayoutParams.WRAP_CONTENT);
                bzLp.setMargins(0, dp(4), 0, dp(2));
                bizCard.setLayoutParams(bzLp);

                LinearLayout leftBiz = new LinearLayout(this);
                leftBiz.setOrientation(LinearLayout.VERTICAL);
                leftBiz.setPadding(dp(8), dp(4), dp(6), dp(4));
                String courierName = (mModel.inspectorName != null && !mModel.inspectorName.trim().isEmpty())
                        ? mModel.inspectorName.trim() : "Courier";
                TextView delivByTv = new TextView(this);
                delivByTv.setText("Delivered By: " + courierName);
                delivByTv.setTextSize(10.5f);
                delivByTv.setTypeface(Typeface.DEFAULT_BOLD);
                delivByTv.setTextColor(0xFF222222);
                leftBiz.addView(delivByTv);

                TextView thxTv = new TextView(this);
                thxTv.setText("Thanks & Enjoy • Delivery Service");
                thxTv.setTextSize(9.5f);
                thxTv.setTextColor(0xFF666666);
                leftBiz.addView(thxTv);
                bizCard.addView(leftBiz, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                TextView rightRedBadge = new TextView(this);
                rightRedBadge.setText("📦✓");
                rightRedBadge.setTextSize(12);
                rightRedBadge.setGravity(Gravity.CENTER);
                rightRedBadge.setTextColor(0xFFFFFFFF);
                rightRedBadge.setBackground(roundedCorners(0xFFFF1717, 0, 3, 3, 0));
                rightRedBadge.setPadding(dp(10), dp(8), dp(10), dp(8));
                bizCard.addView(rightRedBadge, new LinearLayout.LayoutParams(dp(56), ViewGroup.LayoutParams.MATCH_PARENT));

                rightCol.addView(bizCard);
            }

            if (mModel.showTime) {
                mLiveDateTv = createShadowText("", 13f, false, 265);
                mLiveDateTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(mLiveDateTv);
            }
            if (mModel.showWeather && mModel.weatherLine != null && !mModel.weatherLine.trim().isEmpty()) {
                TextView wTv = createShadowText(mModel.weatherLine.trim(), 12.5f, false, 265);
                wTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(wTv);
            }
            if (mModel.showAddress) {
                TextView addrTv = createShadowText(mModel.addressLine, 13f, false, 265);
                addrTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(addrTv);
            }
            appendOptionalGeoAndNoteLines(rightCol, 265);
            wrap.addView(rightCol);

            appendOptionalMiniMapRow(wrap);
            appendTimeNowVerifiedFooter(wrap);
            mWatermarkCardContainer.addView(wrap);

        } else if ("90".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 90: CLEANED & SANITIZED (watermark_cover_id90.xml)
            // ---------------------------------------------------------------------------------
            mDatePattern = "MM/dd/yyyy";
            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);
            appendOptionalBrandLogoHeader(wrap);

            TextView stars = createShadowText("★ ★ ★ ★ ★", 13, true, 260);
            stars.setTextColor(0xFFFFEB3B);
            stars.setPadding(dp(2), 0, 0, dp(2));
            wrap.addView(stars);

            LinearLayout twoTone = new LinearLayout(this);
            twoTone.setOrientation(LinearLayout.HORIZONTAL);
            twoTone.setGravity(Gravity.CENTER_VERTICAL);

            if (mModel.showTitle) {
                TextView leftBox = new TextView(this);
                String cleanTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : "Cleaned";
                leftBox.setTypeface(Typeface.DEFAULT_BOLD);
                leftBox.setTextColor(0xFFE1F5FE);
                leftBox.setGravity(Gravity.CENTER);
                leftBox.setMinimumHeight(dp(34));
                leftBox.setBackground(roundedCorners(mModel.customAccentColor, 4, mModel.showTime ? 0 : 4, mModel.showTime ? 0 : 4, 4));
                leftBox.setPadding(dp(10), dp(4), dp(10), dp(4));
                configureAutoFitText(leftBox, cleanTitle, 16f, 10f, 160);
                twoTone.addView(leftBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }

            if (mModel.showTime) {
                mLiveBigClockTv = new TextView(this);
                mLiveBigClockTv.setTextSize(20);
                mLiveBigClockTv.setSingleLine(true);
                mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
                mLiveBigClockTv.setTextColor(0xFFFFFFFF);
                mLiveBigClockTv.setGravity(Gravity.CENTER);
                mLiveBigClockTv.setMinimumHeight(dp(34));
                mLiveBigClockTv.setBackground(roundedCorners(0xFF229CD7, mModel.showTitle ? 0 : 4, 4, 4, mModel.showTitle ? 0 : 4));
                mLiveBigClockTv.setPadding(dp(10), dp(4), dp(10), dp(4));
                twoTone.addView(mLiveBigClockTv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            if (twoTone.getChildCount() > 0) {
                wrap.addView(twoTone);
            }

            LinearLayout rightCol = createVerticalBarContainer(mModel.customAccentColor, 3, 10);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            midLp.setMargins(0, dp(6), 0, 0);
            rightCol.setLayoutParams(midLp);

            if (mModel.showTime) {
                mLiveDateTv = createShadowText("", 13.5f, true, 265);
                rightCol.addView(mLiveDateTv);
            }
            if (mModel.showWeather && mModel.weatherLine != null && !mModel.weatherLine.trim().isEmpty()) {
                TextView wTv = createShadowText(mModel.weatherLine.trim(), 12.5f, false, 265);
                wTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(wTv);
            }
            if (mModel.showAddress) {
                TextView addrTv = createShadowText(mModel.addressLine, 13f, false, 265);
                addrTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(addrTv);
            }
            appendOptionalGeoAndNoteLines(rightCol, 265);
            wrap.addView(rightCol);

            appendOptionalMiniMapRow(wrap);
            appendTimeNowVerifiedFooter(wrap);
            mWatermarkCardContainer.addView(wrap);

        } else if ("43".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 43: SECURITY GUARD (watermark_cover_id43.xml & new_watermark_cover_id43.xml)
            // ---------------------------------------------------------------------------------
            mDatePattern = "dd MMM yyyy";
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setLayoutParams(new LinearLayout.LayoutParams(dp(266), ViewGroup.LayoutParams.WRAP_CONTENT));
            appendOptionalBrandLogoHeader(card);

            if (mModel.showTitle) {
                LinearLayout header = new LinearLayout(this);
                header.setOrientation(LinearLayout.HORIZONTAL);
                header.setGravity(Gravity.CENTER_VERTICAL);
                header.setBackground(roundedCorners(0xE628356E, 6, 6, 0, 0));
                header.setPadding(dp(10), dp(6), dp(10), dp(6));

                ImageView shieldIv = new ImageView(this);
                shieldIv.setImageResource(R.drawable.tool_border_safe_green);
                header.addView(shieldIv, new LinearLayout.LayoutParams(dp(22), dp(22)));

                TextView titleTv = new TextView(this);
                String secTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : "Security Guard";
                titleTv.setTypeface(Typeface.DEFAULT_BOLD);
                titleTv.setTextColor(0xFFFFF15C);
                titleTv.setPadding(dp(8), 0, 0, 0);
                configureAutoFitText(titleTv, secTitle, 16.5f, 11f, 215);
                header.addView(titleTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                card.addView(header);

                View yellowLine = new View(this);
                yellowLine.setBackgroundColor(mModel.customAccentColor);
                card.addView(yellowLine, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2)));
            }

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setBackground(roundedCorners(0xE63651C9, mModel.showTitle ? 0 : 6, mModel.showTitle ? 0 : 6, 6, 6));
            body.setPadding(dp(10), dp(4), dp(10), dp(8));

            TextView chevrons = new TextView(this);
            chevrons.setText(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            chevrons.setTextSize(8);
            chevrons.setSingleLine(true);
            chevrons.setTextColor(0xAAFFF15C);
            body.addView(chevrons);

            if (mModel.showTime) {
                LinearLayout timeRow = new LinearLayout(this);
                timeRow.setOrientation(LinearLayout.HORIZONTAL);
                timeRow.setGravity(Gravity.BOTTOM);

                mLiveBigClockTv = new TextView(this);
                mLiveBigClockTv.setTextSize(26);
                mLiveBigClockTv.setSingleLine(true);
                mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
                mLiveBigClockTv.setTextColor(0xFFFFF15C);
                mLiveBigClockTv.setIncludeFontPadding(false);
                timeRow.addView(mLiveBigClockTv);

                mLiveDateTv = new TextView(this);
                mLiveDateTv.setTextSize(12.5f);
                mLiveDateTv.setSingleLine(false);
                mLiveDateTv.setTextColor(0xFFFFFFFF);
                mLiveDateTv.setPadding(dp(8), 0, 0, dp(2));
                timeRow.addView(mLiveDateTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                body.addView(timeRow);
            }

            if (mModel.showProject && mModel.projectName != null && !mModel.projectName.trim().isEmpty()) {
                body.addView(createRedBulletRow("Project: " + mModel.projectName.trim()));
            }
            if (mModel.showArea && mModel.areaName != null && !mModel.areaName.trim().isEmpty()) {
                body.addView(createRedBulletRow("Area: " + mModel.areaName.trim()));
            }
            if (mModel.showInspector && mModel.inspectorName != null && !mModel.inspectorName.trim().isEmpty()) {
                body.addView(createRedBulletRow("By: " + mModel.inspectorName.trim()));
            }
            if (mModel.showAddress) {
                body.addView(createRedBulletRow(mModel.addressLine));
            }
            if (mModel.showLatLong) {
                body.addView(createRedBulletRow(mModel.getFormattedCoordinates()));
            }
            if (mModel.showWeather && mModel.weatherLine != null && !mModel.weatherLine.trim().isEmpty()) {
                body.addView(createRedBulletRow(mModel.weatherLine.trim()));
            }
            if (mModel.showAltitude && mModel.altitudeLine != null && !mModel.altitudeLine.trim().isEmpty()) {
                body.addView(createRedBulletRow("Altitude: " + mModel.altitudeLine.trim()));
            }
            if (mModel.showCompass && mModel.compassLine != null && !mModel.compassLine.trim().isEmpty()) {
                body.addView(createRedBulletRow("Compass: " + mModel.compassLine.trim()));
            }
            if (mModel.showNotes && mModel.customNote != null && !mModel.customNote.trim().isEmpty()) {
                body.addView(createRedBulletRow("Note: " + mModel.customNote.trim()));
            }
            for (int i = 0; i < mModel.customExtraFields.size(); i++) {
                body.addView(createRedBulletRow(mModel.customExtraFields.get(i)));
            }
            card.addView(body);
            appendOptionalMiniMapRow(card);
            appendTimeNowVerifiedFooter(card);
            mWatermarkCardContainer.addView(card);

        } else if ("10".equals(id) || "service01".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 10 (CUSTOM TEXT) & SERVICE01 (FIELD SERVICE) (watermark_cover_id10.xml)
            // ---------------------------------------------------------------------------------
            boolean isService = "service01".equals(id);
            mDatePattern = "dd/MM/yyyy HH:mm";

            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);
            appendOptionalBrandLogoHeader(wrap);

            if (mModel.showTitle) {
                String topTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : (isService ? "Service Name" : "Work Record");
                TextView titleTv = createShadowText(topTitle, 16.5f, true, 268);
                configureAutoFitText(titleTv, topTitle, 16.5f, 11f, 268);
                wrap.addView(titleTv);
            }

            LinearLayout rightCol = createVerticalBarContainer(mModel.customAccentColor, 3, 10);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            midLp.setMargins(0, dp(5), 0, 0);
            rightCol.setLayoutParams(midLp);

            if (mModel.showTime) {
                mLiveDateTv = createShadowText("", 13f, true, 265);
                rightCol.addView(mLiveDateTv);
            }
            if (isService) {
                if (mModel.showProject && mModel.projectName != null && !mModel.projectName.trim().isEmpty()) {
                    TextView sv1 = createShadowText("👉 Service: " + mModel.projectName.trim(), 13f, false, 265);
                    sv1.setPadding(0, dp(2), 0, 0);
                    rightCol.addView(sv1);
                }
                if (mModel.showNotes && mModel.customNote != null && !mModel.customNote.trim().isEmpty()) {
                    TextView sv2 = createShadowText("👉 Details: " + mModel.customNote.trim(), 13f, false, 265);
                    sv2.setPadding(0, dp(2), 0, 0);
                    rightCol.addView(sv2);
                }
                if (mModel.showInspector && mModel.inspectorName != null && !mModel.inspectorName.trim().isEmpty()) {
                    TextView sv3 = createShadowText("☎️ Staff/Tel: " + mModel.inspectorName.trim(), 13f, false, 265);
                    sv3.setPadding(0, dp(2), 0, 0);
                    rightCol.addView(sv3);
                }
            }
            if (mModel.showWeather && mModel.weatherLine != null && !mModel.weatherLine.trim().isEmpty()) {
                TextView wTv = createShadowText(mModel.weatherLine.trim(), 12.5f, false, 265);
                wTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(wTv);
            }
            if (mModel.showAddress) {
                TextView addrTv = createShadowText(mModel.addressLine, 13f, false, 265);
                addrTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(addrTv);
            }
            appendOptionalGeoAndNoteLines(rightCol, 265, !isService, !isService);
            wrap.addView(rightCol);

            appendOptionalMiniMapRow(wrap);
            appendTimeNowVerifiedFooter(wrap);
            mWatermarkCardContainer.addView(wrap);

        } else if ("120".equals(id) || "20".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 120 (CONSTRUCTION RECORD) & TEMPLATE 20 (WORK REPORT)
            // (watermark_cover_id120.xml & watermark_cover_id20.xml)
            // ---------------------------------------------------------------------------------
            boolean isConst = "120".equals(id);
            mDatePattern = "dd/MM/yyyy HH:mm";

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setLayoutParams(new LinearLayout.LayoutParams(dp(268), ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.VERTICAL);
            header.setBackground(roundedCorners(mModel.customAccentColor, 5, 5, 0, 0));
            header.setPadding(dp(10), dp(6), dp(10), dp(6));

            if (isConst || mModel.showBrandLogo) {
                LinearLayout logosRow = new LinearLayout(this);
                logosRow.setOrientation(LinearLayout.HORIZONTAL);
                logosRow.setGravity(Gravity.CENTER);
                logosRow.setPadding(0, 0, 0, dp(4));
                if (mModel.showBrandLogo && mModel.brandLogoText != null && !mModel.brandLogoText.trim().isEmpty()) {
                    TextView lPill = new TextView(this);
                    lPill.setText("🏢 " + mModel.brandLogoText.trim());
                    lPill.setTextSize(10f);
                    lPill.setTypeface(Typeface.DEFAULT_BOLD);
                    lPill.setTextColor(0xFF1F3A8A);
                    lPill.setGravity(Gravity.CENTER);
                    lPill.setBackground(roundedRect(0xFFF2F5FA, 3, 0, 0));
                    lPill.setPadding(dp(8), dp(2), dp(8), dp(2));
                    logosRow.addView(lPill, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                } else {
                    String[] lg = new String[]{"🏢 LOGO", "🏗 LOGO", "🏛 LOGO"};
                    for (int i = 0; i < 3; i++) {
                        TextView lPill = new TextView(this);
                        lPill.setText(lg[i]);
                        lPill.setTextSize(9f);
                        lPill.setTypeface(Typeface.DEFAULT_BOLD);
                        lPill.setTextColor(0xFF1F3A8A);
                        lPill.setGravity(Gravity.CENTER);
                        lPill.setBackground(roundedRect(0xFFF2F5FA, 2.5f, 0, 0));
                        lPill.setPadding(dp(4), dp(2), dp(4), dp(2));
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        if (i < 2) lp.setMargins(0, 0, dp(4), 0);
                        logosRow.addView(lPill, lp);
                    }
                }
                header.addView(logosRow);
            }

            if (mModel.showTitle || !isConst) {
                TextView hTitle = new TextView(this);
                String titleStr = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : (isConst ? "Construction Record" : "Work Report");
                hTitle.setTypeface(Typeface.DEFAULT_BOLD);
                hTitle.setTextColor(0xFFFFFFFF);
                hTitle.setGravity(isConst ? Gravity.CENTER : Gravity.START);
                configureAutoFitText(hTitle, titleStr, 15.5f, 11f, 246);
                header.addView(hTitle, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            card.addView(header);

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setBackground(roundedCorners(0xEED9E2EC, 0, 0, 5, 5));
            body.setPadding(dp(8), dp(6), dp(8), dp(6));

            if (mModel.showProject && mModel.projectName != null && !mModel.projectName.trim().isEmpty()) {
                addDarkTableRow(body, "Project", mModel.projectName.trim());
            }
            if (mModel.showArea && mModel.areaName != null && !mModel.areaName.trim().isEmpty()) {
                addDarkTableRow(body, "Area", mModel.areaName.trim());
            }
            if (mModel.showInspector && mModel.inspectorName != null && !mModel.inspectorName.trim().isEmpty()) {
                addDarkTableRow(body, isConst ? "By" : "Inspector", mModel.inspectorName.trim());
            }
            if (mModel.showNotes && mModel.customNote != null && !mModel.customNote.trim().isEmpty()) {
                addDarkTableRow(body, isConst ? "Description" : "Note", mModel.customNote.trim());
            }
            if (mModel.showTime) {
                mLiveDateTv = addDarkTableRow(body, "Time", "");
            }
            if (mModel.showWeather && mModel.weatherLine != null && !mModel.weatherLine.trim().isEmpty()) {
                addDarkTableRow(body, "Weather", mModel.weatherLine.trim());
            }
            if (mModel.showAddress) {
                addDarkTableRow(body, "Address", mModel.addressLine);
            }
            if (mModel.showLatLong) {
                addDarkTableRow(body, "Lat/Long", mModel.getFormattedCoordinates());
            }
            if (mModel.showAltitude && mModel.altitudeLine != null && !mModel.altitudeLine.trim().isEmpty()) {
                addDarkTableRow(body, "Altitude", mModel.altitudeLine.trim());
            }
            if (mModel.showCompass && mModel.compassLine != null && !mModel.compassLine.trim().isEmpty()) {
                addDarkTableRow(body, "Compass", mModel.compassLine.trim());
            }
            for (int i = 0; i < mModel.customExtraFields.size(); i++) {
                String raw = mModel.customExtraFields.get(i);
                int idx = raw.indexOf(':');
                if (idx > 0 && idx < raw.length() - 1) {
                    addDarkTableRow(body, raw.substring(0, idx).trim(), raw.substring(idx + 1).trim());
                } else {
                    addDarkTableRow(body, "Custom", raw);
                }
            }
            card.addView(body);
            appendOptionalMiniMapRow(card);
            appendTimeNowVerifiedFooter(card);
            mWatermarkCardContainer.addView(card);

        } else if ("100".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 100: WORK SHEET (watermark_cover_id100.xml)
            // ---------------------------------------------------------------------------------
            mDatePattern = "dd/MM/yyyy HH:mm";
            int sheetGreen = mModel.customAccentColor;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(roundedRect(0xCC263238, 2, dp(1.5f), sheetGreen));
            card.setLayoutParams(new LinearLayout.LayoutParams(dp(268), ViewGroup.LayoutParams.WRAP_CONTENT));

            View topGreenBar = new View(this);
            topGreenBar.setBackgroundColor(sheetGreen);
            card.addView(topGreenBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(4)));

            if (mModel.showBrandLogo && mModel.brandLogoText != null && !mModel.brandLogoText.trim().isEmpty()) {
                addGreenSheetRow(card, "Company", mModel.brandLogoText.trim(), sheetGreen);
            }

            if (mModel.showTitle) {
                TextView titleTv = new TextView(this);
                String sheetTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : "Work Record";
                titleTv.setTypeface(Typeface.DEFAULT_BOLD);
                titleTv.setTextColor(sheetGreen);
                titleTv.setPadding(dp(8), dp(5), dp(8), dp(5));
                configureAutoFitText(titleTv, sheetTitle, 15f, 11f, 250);
                card.addView(titleTv);
                addGreenSheetDivider(card, sheetGreen);
            }

            if (mModel.showTime) {
                mLiveDateTv = addGreenSheetRow(card, "Time", "", sheetGreen);
            }
            if (mModel.showProject && mModel.projectName != null && !mModel.projectName.trim().isEmpty()) {
                addGreenSheetRow(card, "Project", mModel.projectName.trim(), sheetGreen);
            }
            if (mModel.showArea && mModel.areaName != null && !mModel.areaName.trim().isEmpty()) {
                addGreenSheetRow(card, "Area", mModel.areaName.trim(), sheetGreen);
            }
            if (mModel.showInspector && mModel.inspectorName != null && !mModel.inspectorName.trim().isEmpty()) {
                addGreenSheetRow(card, "By", mModel.inspectorName.trim(), sheetGreen);
            }
            if (mModel.showWeather && mModel.weatherLine != null && !mModel.weatherLine.trim().isEmpty()) {
                addGreenSheetRow(card, "Weather", mModel.weatherLine.trim(), sheetGreen);
            }
            if (mModel.showAddress) {
                addGreenSheetRow(card, "Address", mModel.addressLine, sheetGreen);
            }
            if (mModel.showLatLong) {
                addGreenSheetRow(card, "Lat/Long", mModel.getFormattedCoordinates(), sheetGreen);
            }
            if (mModel.showAltitude && mModel.altitudeLine != null && !mModel.altitudeLine.trim().isEmpty()) {
                addGreenSheetRow(card, "Altitude", mModel.altitudeLine.trim(), sheetGreen);
            }
            if (mModel.showCompass && mModel.compassLine != null && !mModel.compassLine.trim().isEmpty()) {
                addGreenSheetRow(card, "Compass", mModel.compassLine.trim(), sheetGreen);
            }
            if (mModel.showNotes && mModel.customNote != null && !mModel.customNote.trim().isEmpty()) {
                addGreenSheetRow(card, "Note", mModel.customNote.trim(), sheetGreen);
            }
            for (int i = 0; i < mModel.customExtraFields.size(); i++) {
                String raw = mModel.customExtraFields.get(i);
                int idx = raw.indexOf(':');
                if (idx > 0 && idx < raw.length() - 1) {
                    addGreenSheetRow(card, raw.substring(0, idx).trim(), raw.substring(idx + 1).trim(), sheetGreen);
                } else {
                    addGreenSheetRow(card, "Custom", raw, sheetGreen);
                }
            }

            appendOptionalMiniMapRow(card);
            appendTimeNowVerifiedFooter(card);
            mWatermarkCardContainer.addView(card);

        } else if ("60".equals(id) || "130".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 60 (LOCATION STAMP) & TEMPLATE 130 (BIG MAP & COMPASS)
            // (watermark_cover_id60.xml & watermark_cover_id130.xml)
            // ---------------------------------------------------------------------------------
            boolean isBigMap = "130".equals(id) || mModel.showMap;
            mDatePattern = "dd/MM/yyyy HH:mm";

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setLayoutParams(new LinearLayout.LayoutParams(dp(274), ViewGroup.LayoutParams.WRAP_CONTENT));
            appendOptionalBrandLogoHeader(card);

            if (isBigMap) {
                card.addView(OriginalDialogs.createSurveyMapBannerView(this, 68),
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(68)));
            }

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setBackgroundColor(0xBF263238);
            body.setPadding(dp(10), dp(7), dp(10), dp(7));

            if (mModel.showTitle) {
                String locTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : (isBigMap ? "Survey Map" : "Location Verified");
                TextView tTv = new TextView(this);
                tTv.setTypeface(Typeface.DEFAULT_BOLD);
                tTv.setTextColor(0xFFFFFFFF);
                configureAutoFitText(tTv, locTitle, 15.5f, 11f, 254);
                body.addView(tTv);
            }

            if (!isBigMap && mModel.showAddress) {
                TextView aTv = new TextView(this);
                aTv.setText(mModel.addressLine);
                aTv.setTextSize(12.5f);
                aTv.setTextColor(0xEEFFFFFF);
                aTv.setPadding(0, dp(3), 0, 0);
                body.addView(aTv);
            }

            LinearLayout twoCol = new LinearLayout(this);
            twoCol.setOrientation(LinearLayout.HORIZONTAL);
            twoCol.setGravity(Gravity.CENTER_VERTICAL);
            twoCol.setPadding(0, dp(4), 0, 0);

            LinearLayout leftMeta = new LinearLayout(this);
            leftMeta.setOrientation(LinearLayout.VERTICAL);
            if (mModel.showLatLong) {
                TextView gTv = new TextView(this);
                gTv.setText(mModel.getFormattedCoordinates());
                gTv.setTextSize(11.5f);
                gTv.setTextColor(0xFFFFFFFF);
                leftMeta.addView(gTv);
            }
            if (mModel.showAltitude && mModel.altitudeLine != null && !mModel.altitudeLine.trim().isEmpty()) {
                TextView altTv = new TextView(this);
                altTv.setText("Altitude: " + mModel.altitudeLine.trim());
                altTv.setTextSize(11.5f);
                altTv.setTextColor(0xEEFFFFFF);
                leftMeta.addView(altTv);
            }
            if (mModel.showTime) {
                mLiveDateTv = new TextView(this);
                mLiveDateTv.setTextSize(11.5f);
                mLiveDateTv.setTextColor(0xEEFFFFFF);
                leftMeta.addView(mLiveDateTv);
            }
            twoCol.addView(leftMeta, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            LinearLayout rightMeta = new LinearLayout(this);
            rightMeta.setOrientation(LinearLayout.VERTICAL);
            rightMeta.setGravity(Gravity.END);
            rightMeta.setPadding(dp(6), 0, 0, 0);
            if (mModel.showCompass || "60".equals(id) || "130".equals(id)) {
                TextView cTv = new TextView(this);
                cTv.setText("🧭 " + mModel.compassLine);
                cTv.setTextSize(11.5f);
                cTv.setTextColor(0xFFFFFFFF);
                rightMeta.addView(cTv);
            }
            if (mModel.showWeather && mModel.weatherLine != null && !mModel.weatherLine.trim().isEmpty()) {
                TextView wTv = new TextView(this);
                wTv.setText("☀ " + mModel.weatherLine.trim());
                wTv.setTextSize(11.5f);
                wTv.setTextColor(0xFFFFFFFF);
                rightMeta.addView(wTv);
            }
            twoCol.addView(rightMeta);
            body.addView(twoCol);

            if (isBigMap && mModel.showAddress) {
                TextView aTv = new TextView(this);
                aTv.setText(mModel.addressLine);
                aTv.setTextSize(12f);
                aTv.setTextColor(0xEEFFFFFF);
                aTv.setPadding(0, dp(4), 0, 0);
                body.addView(aTv);
            }

            appendOptionalWorkAndNoteLinesOnly(body, 254);
            card.addView(body);

            View bottomAccent = new View(this);
            bottomAccent.setBackgroundColor(mModel.customAccentColor);
            card.addView(bottomAccent, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2.5f)));

            appendTimeNowVerifiedFooter(card);
            mWatermarkCardContainer.addView(card);

        } else {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 34 (TIME & LOCATION) & MAP01 (GPS MAP STAMP)
            // Classic watermark_cover_id1034.xml / new_watermark_cover_id34.xml
            // ---------------------------------------------------------------------------------
            boolean isMap01 = "map01".equals(id) || mModel.showMap;
            mDatePattern = "dd MMM yyyy";

            LinearLayout outerRow = new LinearLayout(this);
            outerRow.setOrientation(LinearLayout.HORIZONTAL);
            outerRow.setGravity(Gravity.BOTTOM);

            LinearLayout leftBlock = new LinearLayout(this);
            leftBlock.setOrientation(LinearLayout.VERTICAL);
            int maxAddrW = isMap01 ? 202 : 276;
            appendOptionalBrandLogoHeader(leftBlock);

            if (mModel.showTitle && mModel.customTitle != null && !mModel.customTitle.trim().isEmpty()) {
                TextView badge = new TextView(this);
                badge.setTypeface(Typeface.DEFAULT_BOLD);
                badge.setTextColor(0xFF111111);
                badge.setBackground(roundedRect(mModel.customAccentColor, 4, 0, 0));
                badge.setPadding(dp(8), dp(2), dp(8), dp(2));
                configureAutoFitText(badge, mModel.customTitle.trim(), 12.5f, 9f, maxAddrW);
                LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                bLp.setMargins(0, 0, 0, dp(4));
                leftBlock.addView(badge, bLp);
            }

            if (mModel.showTime) {
                LinearLayout middleRow = new LinearLayout(this);
                middleRow.setOrientation(LinearLayout.HORIZONTAL);
                middleRow.setGravity(Gravity.CENTER_VERTICAL);

                mLiveBigClockTv = new TextView(this);
                mLiveBigClockTv.setTextSize(isMap01 ? 30 : 36);
                mLiveBigClockTv.setSingleLine(true);
                mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
                mLiveBigClockTv.setTextColor(0xFFFFFFFF);
                mLiveBigClockTv.setShadowLayer(4f, 0f, 1.5f, 0xCC000000);
                mLiveBigClockTv.setIncludeFontPadding(false);
                middleRow.addView(mLiveBigClockTv);

                View blockBar = new View(this);
                blockBar.setBackground(roundedRect(mModel.customAccentColor, 1.5f, 0, 0));
                LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(3), dp(32));
                barLp.setMargins(dp(7), 0, dp(7), 0);
                middleRow.addView(blockBar, barLp);

                LinearLayout dateCol = new LinearLayout(this);
                dateCol.setOrientation(LinearLayout.VERTICAL);

                int maxDateColW = isMap01 ? 102 : 148;
                mLiveDateTv = new TextView(this);
                mLiveDateTv.setTextSize(isMap01 ? 12f : 13.5f);
                mLiveDateTv.setSingleLine(false);
                mLiveDateTv.setMaxWidth(dp(maxDateColW));
                mLiveDateTv.setTypeface(Typeface.DEFAULT_BOLD);
                mLiveDateTv.setTextColor(0xFFFFFFFF);
                mLiveDateTv.setShadowLayer(3f, 0f, 1f, 0xCC000000);
                dateCol.addView(mLiveDateTv);

                mLiveWeatherTv = new TextView(this);
                mLiveWeatherTv.setTextSize(isMap01 ? 11.5f : 12.5f);
                mLiveWeatherTv.setSingleLine(false);
                mLiveWeatherTv.setMaxWidth(dp(maxDateColW));
                mLiveWeatherTv.setTextColor(0xFFFFFFFF);
                mLiveWeatherTv.setShadowLayer(3f, 0f, 1f, 0xCC000000);
                dateCol.addView(mLiveWeatherTv);

                middleRow.addView(dateCol);
                leftBlock.addView(middleRow);
            }

            if (mModel.showAddress) {
                TextView addrTv = createShadowText(mModel.addressLine, 13.5f, false, maxAddrW);
                addrTv.setPadding(0, dp(4), 0, 0);
                leftBlock.addView(addrTv);
            }
            appendOptionalGeoAndNoteLines(leftBlock, maxAddrW);
            appendTimeNowVerifiedFooter(leftBlock);
            outerRow.addView(leftBlock);

            if (isMap01) {
                outerRow.addView(createMiniGpsMapCard(68));
            }

            mWatermarkCardContainer.addView(outerRow);
        }

        updateClockTexts();
        applySafeStampScaleAndPivot();
    }

    private void applySafeStampScaleAndPivot() {
        if (mWatermarkCardContainer == null) return;
        mWatermarkCardContainer.post(new Runnable() {
            @Override
            public void run() {
                int cardW = mWatermarkCardContainer.getWidth();
                int cardH = mWatermarkCardContainer.getHeight();
                int vfW = (mViewfinderContainer != null && mViewfinderContainer.getWidth() > 0)
                        ? mViewfinderContainer.getWidth() : getResources().getDisplayMetrics().widthPixels;
                int vfH = (mViewfinderContainer != null && mViewfinderContainer.getHeight() > 0)
                        ? mViewfinderContainer.getHeight() : Math.round(vfW * 4f / 3f);

                float maxAvailW = Math.max(dp(200), vfW - dp(52));
                float maxAvailH = Math.max(dp(180), vfH - dp(24));

                float effectiveScale = mModel.stampScale;
                if (cardW > 0 && cardW * effectiveScale > maxAvailW) {
                    effectiveScale = Math.min(effectiveScale, maxAvailW / (float) cardW);
                }
                if (cardH > 0 && cardH * effectiveScale > maxAvailH) {
                    effectiveScale = Math.min(effectiveScale, maxAvailH / (float) cardH);
                }

                boolean rightAnchored = (mModel.stampPositionIndex == 1 || mModel.stampPositionIndex == 3);
                boolean topAnchored = (mModel.stampPositionIndex == 2 || mModel.stampPositionIndex == 3);
                mWatermarkCardContainer.setPivotX(rightAnchored ? cardW : 0f);
                mWatermarkCardContainer.setPivotY(topAnchored ? 0f : cardH);
                mWatermarkCardContainer.setScaleX(effectiveScale);
                mWatermarkCardContainer.setScaleY(effectiveScale);
            }
        });
    }

    // Custom vertical-bar container that draws the left accent bar via dispatchDraw so it never collapses inside WRAP_CONTENT
    private LinearLayout createVerticalBarContainer(final int barColor, final int barWidthDp, int leftPadDp) {
        LinearLayout col = new LinearLayout(this) {
            private final Paint barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF barRect = new RectF();
            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                int h = getHeight();
                if (h > dp(4)) {
                    barPaint.setColor(barColor);
                    float bw = dp(barWidthDp);
                    float r = bw / 2f;
                    barRect.set(0f, dp(2), bw, h - dp(2));
                    canvas.drawRoundRect(barRect, r, r, barPaint);
                }
            }
        };
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(leftPadDp), 0, 0, 0);
        return col;
    }

    private void appendOptionalBrandLogoHeader(LinearLayout parent) {
        if (!mModel.showBrandLogo || mModel.brandLogoText == null || mModel.brandLogoText.trim().isEmpty()) return;
        LinearLayout logoPill = new LinearLayout(this);
        logoPill.setOrientation(LinearLayout.HORIZONTAL);
        logoPill.setGravity(Gravity.CENTER_VERTICAL);
        logoPill.setBackground(roundedRect(0xAA000000, 4, dp(1), mModel.customAccentColor));
        logoPill.setPadding(dp(7), dp(2), dp(8), dp(2));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, dp(4));
        logoPill.setLayoutParams(lp);

        ImageView shieldIv = new ImageView(this);
        shieldIv.setImageResource(R.drawable.watermark_shield);
        logoPill.addView(shieldIv, new LinearLayout.LayoutParams(dp(12), dp(12)));

        TextView tv = new TextView(this);
        tv.setText(" " + mModel.brandLogoText.trim());
        tv.setTextSize(11.5f);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(0xFFFFFFFF);
        logoPill.addView(tv);
        parent.addView(logoPill);
    }

    private View createMiniGpsMapCard(int sizeDp) {
        FrameLayout mapCard = new FrameLayout(this);
        mapCard.setBackground(roundedRect(0xFFFFFFFF, 6, dp(1.5f), 0xFFFFFFFF));
        mapCard.setPadding(dp(2), dp(2), dp(2), dp(2));
        LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp));
        mLp.setMargins(dp(8), 0, 0, 0);
        mapCard.setLayoutParams(mLp);

        View mapCanvasView = new View(this) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override
            protected void onDraw(Canvas c) {
                super.onDraw(c);
                int w = getWidth();
                int h = getHeight();
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFFEEF2F6);
                c.drawRect(0, 0, w, h, p);
                p.setColor(0xFFDCE6EE);
                c.drawRect(0, h * 0.15f, w * 0.42f, h * 0.55f, p);
                c.drawRect(w * 0.58f, h * 0.45f, w, h * 0.85f, p);
                p.setColor(0xFFFFFFFF);
                p.setStrokeWidth(dp(3));
                c.drawLine(0, h * 0.35f, w, h * 0.35f, p);
                c.drawLine(w * 0.48f, 0, w * 0.48f, h, p);
                c.drawLine(0, h * 0.72f, w, h * 0.55f, p);
                // Blue GPS Location Pin
                float cx = w * 0.5f;
                float cy = h * 0.40f;
                p.setColor(0x330093FF);
                c.drawCircle(cx, cy + dp(6), dp(8), p);
                p.setColor(0xFF0093FF);
                c.drawCircle(cx, cy, dp(8), p);
                p.setColor(0xFFFFFFFF);
                c.drawCircle(cx, cy, dp(3.5f), p);
            }
        };
        mapCard.addView(mapCanvasView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView cityTv = new TextView(this);
        cityTv.setText("GPS Map");
        cityTv.setTextSize(8.5f);
        cityTv.setTypeface(Typeface.DEFAULT_BOLD);
        cityTv.setTextColor(0xFF333333);
        cityTv.setGravity(Gravity.CENTER);
        cityTv.setBackgroundColor(0xCCFFFFFF);
        FrameLayout.LayoutParams cLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cLp.gravity = Gravity.BOTTOM;
        mapCard.addView(cityTv, cLp);
        return mapCard;
    }

    private void appendOptionalMiniMapRow(LinearLayout parent) {
        if (!mModel.showMap) return;
        LinearLayout mapRow = new LinearLayout(this);
        mapRow.setOrientation(LinearLayout.HORIZONTAL);
        mapRow.setGravity(Gravity.CENTER_VERTICAL);
        mapRow.setPadding(0, dp(4), 0, 0);
        View card = createMiniGpsMapCard(54);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(54), dp(54));
        card.setLayoutParams(lp);
        mapRow.addView(card);
        parent.addView(mapRow);
    }

    private TextView createShadowText(String text, float sp, boolean bold, int maxWidthDp) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        tv.setSingleLine(false);
        tv.setEllipsize(null);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(0xFFFFFFFF);
        tv.setShadowLayer(3.5f, 0f, 1.2f, 0xDD000000);
        tv.setMaxWidth(dp(maxWidthDp));
        return tv;
    }

    private void appendOptionalGeoAndNoteLines(LinearLayout parent, int maxWidthDp) {
        appendOptionalGeoAndNoteLines(parent, maxWidthDp, true, true);
    }

    private void appendOptionalGeoAndNoteLines(LinearLayout parent, int maxWidthDp, boolean includeProjectLine, boolean includeNoteLine) {
        if (mModel.showLatLong || mModel.showCompass || mModel.showAltitude) {
            StringBuilder geo = new StringBuilder();
            if (mModel.showLatLong) geo.append(mModel.getFormattedCoordinates());
            if (mModel.showCompass) {
                if (geo.length() > 0) geo.append("  ");
                geo.append(mModel.compassLine);
            }
            if (mModel.showAltitude) {
                if (geo.length() > 0) geo.append("  ");
                geo.append(mModel.altitudeLine);
            }
            TextView geoTv = createShadowText(geo.toString(), 12.5f, false, maxWidthDp);
            geoTv.setPadding(0, dp(2), 0, 0);
            parent.addView(geoTv);
        }
        appendOptionalWorkAndNoteLines(parent, maxWidthDp, includeProjectLine, includeNoteLine);
    }

    private void appendOptionalWorkAndNoteLinesOnly(LinearLayout parent, int maxWidthDp) {
        appendOptionalWorkAndNoteLines(parent, maxWidthDp, true, true);
    }

    private void appendOptionalWorkAndNoteLines(LinearLayout parent, int maxWidthDp, boolean includeProjectLine, boolean includeNoteLine) {
        if (includeProjectLine && mModel.showProject && mModel.projectName != null && !mModel.projectName.trim().isEmpty()) {
            TextView projTv = createShadowText("Project: " + mModel.projectName.trim(), 12.5f, false, maxWidthDp);
            projTv.setPadding(0, dp(2), 0, 0);
            parent.addView(projTv);
        }
        if (mModel.showArea && mModel.areaName != null && !mModel.areaName.trim().isEmpty()) {
            TextView areaTv = createShadowText("Area: " + mModel.areaName.trim(), 12.5f, false, maxWidthDp);
            areaTv.setPadding(0, dp(2), 0, 0);
            parent.addView(areaTv);
        }
        String activeId = mModel.activeTemplate != null ? mModel.activeTemplate.id : "";
        if (!"dasher01".equals(activeId) && !"service01".equals(activeId)
                && mModel.showInspector && mModel.inspectorName != null && !mModel.inspectorName.trim().isEmpty()) {
            TextView inspTv = createShadowText("By: " + mModel.inspectorName.trim(), 12.5f, false, maxWidthDp);
            inspTv.setPadding(0, dp(2), 0, 0);
            parent.addView(inspTv);
        }
        if (includeNoteLine && mModel.showNotes && mModel.customNote != null && !mModel.customNote.trim().isEmpty()) {
            TextView noteTv = createShadowText("Note: " + mModel.customNote.trim(), 12.5f, false, maxWidthDp);
            noteTv.setPadding(0, dp(2), 0, 0);
            parent.addView(noteTv);
        }
        for (int i = 0; i < mModel.customExtraFields.size(); i++) {
            TextView exTv = createShadowText(mModel.customExtraFields.get(i), 12.5f, false, maxWidthDp);
            exTv.setPadding(0, dp(2), 0, 0);
            parent.addView(exTv);
        }
    }

    // Exact original watermark_cover_id1034.xml / watermark_cover_id21.xml footer:
    // @drawable/watermark_shield (12dp) + "100% True Time" (12sp #B3FFFFFF)
    private void appendTimeNowVerifiedFooter(LinearLayout parent) {
        if (!mModel.showTimeNowLogo) return;
        LinearLayout verRow = new LinearLayout(this);
        verRow.setOrientation(LinearLayout.HORIZONTAL);
        verRow.setGravity(Gravity.CENTER_VERTICAL);
        verRow.setPadding(0, dp(4), 0, 0);

        ImageView shieldIv = new ImageView(this);
        shieldIv.setImageResource(R.drawable.watermark_shield);
        verRow.addView(shieldIv, new LinearLayout.LayoutParams(dp(12), dp(12)));

        TextView verTv = new TextView(this);
        verTv.setText(" 100% True Time");
        verTv.setTextSize(11.5f);
        verTv.setTextColor(0xB3FFFFFF);
        verTv.setShadowLayer(2f, 0f, 1f, 0x80000000);
        verRow.addView(verTv);

        parent.addView(verRow);
    }

    private LinearLayout createRedBulletRow(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(2.5f), 0, 0);

        View bullet = new View(this);
        bullet.setBackgroundColor(0xFFFF2D20);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(dp(4.5f), dp(10));
        bLp.setMargins(0, dp(3), dp(6), 0);
        row.addView(bullet, bLp);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12f);
        tv.setSingleLine(false);
        tv.setTextColor(0xFFFFFFFF);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        return row;
    }

    private TextView addDarkTableRow(LinearLayout parent, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(1.5f), 0, dp(1.5f));

        TextView lTv = new TextView(this);
        lTv.setText(label);
        lTv.setTextSize(11.5f);
        lTv.setTextColor(0xFF444444);
        row.addView(lTv, new LinearLayout.LayoutParams(dp(68), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView colon = new TextView(this);
        colon.setText(": ");
        colon.setTextSize(11.5f);
        colon.setTextColor(0xFF444444);
        row.addView(colon);

        TextView vTv = new TextView(this);
        vTv.setText(value);
        vTv.setTextSize(11.5f);
        vTv.setSingleLine(false);
        vTv.setTypeface(Typeface.DEFAULT_BOLD);
        vTv.setTextColor(0xFF111111);
        row.addView(vTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        parent.addView(row);
        return vTv;
    }

    private void addGreenSheetDivider(LinearLayout parent, int color) {
        View div = new View(this);
        div.setBackgroundColor(color);
        parent.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(0.8f))));
    }

    private TextView addGreenSheetRow(LinearLayout parent, String label, String value, final int greenColor) {
        final int divX = dp(66);
        LinearLayout row = new LinearLayout(this) {
            private final Paint divPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override
            protected void dispatchDraw(Canvas canvas) {
                super.dispatchDraw(canvas);
                divPaint.setColor(greenColor);
                divPaint.setStrokeWidth(Math.max(1f, dp(0.8f)));
                canvas.drawLine(divX, 0, divX, getHeight(), divPaint);
            }
        };
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView lTv = new TextView(this);
        lTv.setText(label);
        lTv.setTextSize(11.5f);
        lTv.setTextColor(0xFFE0F2F1);
        lTv.setPadding(dp(6), dp(3.5f), dp(4), dp(3.5f));
        row.addView(lTv, new LinearLayout.LayoutParams(divX, ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView vTv = new TextView(this);
        vTv.setText(value);
        vTv.setTextSize(11.5f);
        vTv.setSingleLine(false);
        vTv.setTextColor(0xFFFFFFFF);
        vTv.setPadding(dp(6), dp(3.5f), dp(6), dp(3.5f));
        row.addView(vTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        parent.addView(row);
        addGreenSheetDivider(parent, greenColor);
        return vTv;
    }

    private void updateClockTexts() {
        Date now = new Date();
        if (mLiveBigClockTv != null) {
            Typeface tf = (mModel.clockFontIndex == 1) ? mFontSegment
                    : ((mModel.clockFontIndex == 2) ? mFontMono : mFontBebas);
            mLiveBigClockTv.setTypeface(tf, Typeface.BOLD);
            String clockStr = new SimpleDateFormat(mModel.getClockPattern(), Locale.US).format(now);
            mLiveBigClockTv.setText(clockStr);

            // Auto-size big clock when seconds / AM-PM are active so adjacent date/map columns never get pushed off-screen
            String id = (mModel.activeTemplate != null) ? mModel.activeTemplate.id : "34";
            boolean isMap01 = "map01".equals(id) || ("34".equals(id) && mModel.showMap);
            if ("34".equals(id) || "map01".equals(id)) {
                if (clockStr.length() > 8) {
                    mLiveBigClockTv.setTextSize(isMap01 ? 21f : 26f);
                } else if (clockStr.length() > 5) {
                    mLiveBigClockTv.setTextSize(isMap01 ? 24f : 30f);
                } else {
                    mLiveBigClockTv.setTextSize(isMap01 ? 30f : 36f);
                }
            } else if ("43".equals(id) && clockStr.length() > 8) {
                mLiveBigClockTv.setTextSize(21f);
            } else if ("21".equals(id) && clockStr.length() > 8) {
                mLiveBigClockTv.setTextSize(19.5f);
            }
        }
        if (mLiveDateTv != null) {
            String effectivePattern = mModel.getEffectiveDatePattern(mDatePattern);
            mLiveDateTv.setText(new SimpleDateFormat(effectivePattern, Locale.US).format(now));
        }
        if (mLiveWeatherTv != null) {
            String day = new SimpleDateFormat("EEEE", Locale.US).format(now);
            mLiveWeatherTv.setText(mModel.showWeather ? (day + "  " + mModel.weatherLine) : day);
        }
        if (mGridLevelerView != null) {
            mGridLevelerView.invalidate();
        }
    }

    private void setupDraggableAndClickableWatermark(final View wmView) {
        wmView.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private float downRawX, downRawY;
            private boolean dragged;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = view.getTranslationX() - event.getRawX();
                        dY = view.getTranslationY() - event.getRawY();
                        downRawX = event.getRawX();
                        downRawY = event.getRawY();
                        dragged = false;
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        float moveX = Math.abs(event.getRawX() - downRawX);
                        float moveY = Math.abs(event.getRawY() - downRawY);
                        if (moveX > dp(8) || moveY > dp(8)) {
                            dragged = true;
                            view.setTranslationX(event.getRawX() + dX);
                            view.setTranslationY(event.getRawY() + dY);
                        }
                        return true;
                    case MotionEvent.ACTION_UP:
                        if (!dragged) {
                            OriginalDialogs.showEditStampSheet(MainActivity.this, mModel, new Runnable() {
                                @Override
                                public void run() {
                                    rebuildWatermarkOverlay();
                                }
                            });
                        }
                        return true;
                }
                return false;
            }
        });
    }

    private void updateZoomPillsUI() {
        float[] zoomVals = new float[]{2.0f, 1.0f, 0.6f};
        String[] baseLabels = new String[]{"2", "1", ".6"};
        for (int i = 0; i < 3; i++) {
            if (mZoomPills[i] == null) continue;
            boolean sel = Math.abs(mModel.zoomLevel - zoomVals[i]) < 0.05f;
            mZoomPills[i].setText(sel ? (baseLabels[i] + "x") : baseLabels[i]);
            mZoomPills[i].setBackground(roundedRect(0x66000000, 14, sel ? dp(1.5f) : 0, 0xFFFFFFFF));
        }
    }

    private void updateModeTabsUI() {
        for (int i = 0; i < 3; i++) {
            if (mModeTabs[i] == null) continue;
            boolean sel = (mModel.cameraMode == i);
            mModeTabs[i].setTypeface(sel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            mModeTabs[i].setTextColor(sel ? 0xFF222222 : 0xFF848484);
            mModeDots[i].setVisibility(sel ? View.VISIBLE : View.INVISIBLE);
        }

        if (mNormalActionBar != null && mAttendActionBar != null && mVideoRecordingActionBar != null) {
            if (mIsRecordingVideo && mModel.cameraMode == 0) {
                mNormalActionBar.setVisibility(View.GONE);
                mAttendActionBar.setVisibility(View.GONE);
                mVideoRecordingActionBar.setVisibility(View.VISIBLE);
            } else if (mModel.cameraMode == 2) {
                mNormalActionBar.setVisibility(View.GONE);
                mAttendActionBar.setVisibility(View.VISIBLE);
                mVideoRecordingActionBar.setVisibility(View.GONE);
            } else {
                mNormalActionBar.setVisibility(View.VISIBLE);
                mAttendActionBar.setVisibility(View.GONE);
                mVideoRecordingActionBar.setVisibility(View.GONE);
            }
        }

        if (mShootIv != null) {
            if (mModel.cameraMode == 0) {
                mShootIv.setImageResource(R.drawable.cam_shoot_video);
            } else {
                mShootIv.setImageResource(R.drawable.cam_shoot);
            }
        }
    }

    private void applyAspectRatioLayout() {
        if (mRootLayout == null || mCenterHost == null || mViewfinderContainer == null) return;
        int rootW = mRootLayout.getWidth();
        int rootH = mRootLayout.getHeight();
        if (rootW <= 0 || rootH <= 0) return;

        int topH = dp(56);
        int modeH = dp(38);
        int minBottomH = dp(104);
        int maxAvailVfH = Math.max(dp(240), rootH - topH - modeH - minBottomH);

        int targetVfW = rootW;
        int targetVfH;
        if (mModel.aspectRatioMode == 2) {
            // 1:1 Square
            targetVfH = Math.min(maxAvailVfH, rootW);
        } else if (mModel.aspectRatioMode == 0) {
            // 4:3 Standard (portrait 3:4 -> height = width * 4 / 3)
            targetVfH = Math.min(maxAvailVfH, Math.round(rootW * 4f / 3f));
        } else {
            // 16:9 or Full
            targetVfH = maxAvailVfH;
        }

        LinearLayout.LayoutParams chLp = (LinearLayout.LayoutParams) mCenterHost.getLayoutParams();
        if (chLp.height != targetVfH || chLp.weight != 0f) {
            chLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            chLp.height = targetVfH;
            chLp.weight = 0f;
            mCenterHost.setLayoutParams(chLp);
        }

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mViewfinderContainer.getLayoutParams();
        if (lp.width != targetVfW || lp.height != targetVfH) {
            lp.width = targetVfW;
            lp.height = targetVfH;
            lp.gravity = Gravity.CENTER;
            mViewfinderContainer.setLayoutParams(lp);
        }

        configureTextureViewTransform(targetVfW, targetVfH);
        if (mGridLevelerView != null) mGridLevelerView.invalidate();
    }

    private void configureTextureViewTransform(int viewWidth, int viewHeight) {
        if (mTextureView == null || viewWidth <= 0 || viewHeight <= 0) return;
        int uprightBufW = Math.min(mPreviewBufferWidth, mPreviewBufferHeight);
        int uprightBufH = Math.max(mPreviewBufferWidth, mPreviewBufferHeight);
        if (uprightBufW <= 0 || uprightBufH <= 0) return;

        float bufferRatio = (float) uprightBufH / (float) uprightBufW;
        float viewRatio = (float) viewHeight / (float) viewWidth;

        android.graphics.Matrix matrix = new android.graphics.Matrix();
        float centerX = viewWidth / 2f;
        float centerY = viewHeight / 2f;
        if (Math.abs(viewRatio - bufferRatio) < 0.01f) {
            matrix.reset();
        } else if (viewRatio > bufferRatio) {
            float scaleX = viewRatio / bufferRatio;
            matrix.postScale(scaleX, 1.0f, centerX, centerY);
        } else {
            float scaleY = bufferRatio / viewRatio;
            matrix.postScale(1.0f, scaleY, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    private boolean drawUnstretchedCameraFrame(Canvas destCanvas, int targetW, int targetH) {
        if (mTextureView == null || !mTextureView.isAvailable()) return false;
        Bitmap raw = mTextureView.getBitmap(targetW, targetH);
        if (raw == null) return false;

        int uprightBufW = Math.min(mPreviewBufferWidth, mPreviewBufferHeight);
        int uprightBufH = Math.max(mPreviewBufferWidth, mPreviewBufferHeight);
        float bufferRatio = (uprightBufW > 0 && uprightBufH > 0)
                ? ((float) uprightBufH / (float) uprightBufW) : (4f / 3f);
        float targetRatio = (float) targetH / (float) Math.max(1, targetW);

        android.graphics.Matrix m = new android.graphics.Matrix();
        float cx = targetW / 2f;
        float cy = targetH / 2f;
        if (Math.abs(targetRatio - bufferRatio) >= 0.01f) {
            if (targetRatio > bufferRatio) {
                m.postScale(targetRatio / bufferRatio, 1.0f, cx, cy);
            } else {
                m.postScale(1.0f, bufferRatio / targetRatio, cx, cy);
            }
        }
        Paint p = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
        destCanvas.drawBitmap(raw, m, p);
        return true;
    }

    // =========================================================================================
    // SHUTTER DISPATCHER: VIDEO RECORDING (MODE 0), PHOTO (MODE 1), ATTENDANCE (MODE 2)
    // =========================================================================================
    private void onShutterButtonClicked() {
        if (mCountdownActive) return;

        if (mModel.cameraMode == 0) {
            if (!mIsRecordingVideo) {
                startVideoRecording();
            } else {
                stopVideoRecording();
            }
            return;
        }

        if (mModel.timerSeconds > 0) {
            mCountdownActive = true;
            runCountdownStep(mModel.timerSeconds);
        } else {
            captureWatermarkedPhoto();
        }
    }

    private void runCountdownStep(final int remaining) {
        if (remaining <= 0) {
            mCountdownActive = false;
            if (mTimerCountdownTv != null) mTimerCountdownTv.setVisibility(View.GONE);
            captureWatermarkedPhoto();
            return;
        }
        if (mTimerCountdownTv != null) {
            mTimerCountdownTv.setVisibility(View.VISIBLE);
            mTimerCountdownTv.setText(String.valueOf(remaining));
        }
        mUiHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                runCountdownStep(remaining - 1);
            }
        }, 1000);
    }

    private Bitmap captureFastWatermarkedVideoFrame() {
        try {
            int outW = 480;
            int outH = 640;
            Bitmap frame = Bitmap.createBitmap(outW, outH, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(frame);

            if (!drawUnstretchedCameraFrame(c, outW, outH) && mViewfinderContainer != null && mViewfinderContainer.getChildCount() > 0) {
                int vw = Math.max(1, mViewfinderContainer.getWidth());
                int vh = Math.max(1, mViewfinderContainer.getHeight());
                c.save();
                c.scale((float) outW / vw, (float) outH / vh);
                mViewfinderContainer.getChildAt(0).draw(c);
                c.restore();
            }

            if (mViewfinderContainer != null && mWatermarkCardContainer != null) {
                int vw = Math.max(1, mViewfinderContainer.getWidth());
                int vh = Math.max(1, mViewfinderContainer.getHeight());
                float sx = (float) outW / vw;
                float sy = (float) outH / vh;

                c.save();
                c.scale(sx, sy);

                c.save();
                c.translate(mWatermarkCardContainer.getX(), mWatermarkCardContainer.getY());
                c.scale(mModel.stampScale, mModel.stampScale, mWatermarkCardContainer.getPivotX(), mWatermarkCardContainer.getPivotY());
                mWatermarkCardContainer.draw(c);
                c.restore();

                if (mModel.showPhotoCode) {
                    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                    p.setColor(0xDDFFFFFF);
                    p.setTextSize(dp(11));
                    p.setTypeface(mFontMono);
                    p.setShadowLayer(3f, 0f, 1f, 0xCC000000);
                    c.save();
                    c.translate(vw - dp(12), vh * 0.65f);
                    c.rotate(-90f);
                    c.drawText("Photo Code: " + mModel.currentPhotoCode, 0, 0, p);
                    c.restore();
                }
                c.restore();
            }

            return frame;
        } catch (Throwable t) {
            return null;
        }
    }

    private void startVideoRecording() {
        playShutterSound();
        mIsRecordingVideo = true;
        mVideoStartTimeMs = System.currentTimeMillis();
        mRecordedVideoFrames.clear();

        if (mRecordDurationTv != null) {
            mRecordDurationTv.setText("00:01");
        }
        updateModeTabsUI();

        File dir = new File(getFilesDir(), "timenow_photos");
        if (!dir.exists()) dir.mkdirs();
        mCurrentVideoFile = new File(dir, "TN_VID_" + mVideoStartTimeMs + ".mp4");

        mUiHandler.removeCallbacks(mVideoFrameSampler);
        mUiHandler.post(mVideoFrameSampler);
    }

    private void stopVideoRecording() {
        if (!mIsRecordingVideo) return;
        mIsRecordingVideo = false;
        mUiHandler.removeCallbacks(mVideoFrameSampler);
        playShutterSound();

        final int durationSec = Math.max(1, (int) ((System.currentTimeMillis() - mVideoStartTimeMs) / 1000L));
        updateModeTabsUI();

        while (mRecordedVideoFrames.size() < 6) {
            Bitmap extra = captureFastWatermarkedVideoFrame();
            if (extra != null) {
                mRecordedVideoFrames.add(extra);
            } else {
                break;
            }
        }

        final List<Bitmap> framesCopy = new ArrayList<Bitmap>(mRecordedVideoFrames);
        Bitmap thumbBmp = !framesCopy.isEmpty()
                ? framesCopy.get(framesCopy.size() - 1).copy(Bitmap.Config.RGB_565, true)
                : captureFastWatermarkedVideoFrame();

        if (thumbBmp != null) {
            WatermarkModel.CapturedPhoto saved = mModel.saveCapturedVideoWithFrames(
                    this, thumbBmp, framesCopy, mCurrentVideoFile, durationSec);
            if (saved != null && mPreviewThumbIv != null) {
                mPreviewThumbIv.setImageBitmap(thumbBmp);
                mAntiCodeVerticalTv.setText("Photo Code: " + mModel.currentPhotoCode);
            }
        }

        final File outMp4 = mCurrentVideoFile;
        new Thread(new Runnable() {
            @Override
            public void run() {
                encodeWatermarkedMp4InBackground(framesCopy, outMp4);
            }
        }).start();
    }

    private void encodeWatermarkedMp4InBackground(List<Bitmap> frames, File outFile) {
        if (outFile == null || frames == null || frames.isEmpty()) return;
        MediaCodec encoder = null;
        MediaMuxer muxer = null;
        Surface inputSurface = null;
        boolean muxerStarted = false;
        try {
            int width = 480;
            int height = 640;
            MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, 2000000);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, 8);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

            encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            inputSurface = encoder.createInputSurface();
            encoder.start();

            muxer = new MediaMuxer(outFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
            int trackIndex = -1;
            Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);

            for (int i = 0; i < frames.size(); i++) {
                Bitmap b = frames.get(i);
                if (b != null) {
                    Canvas sc = inputSurface.lockCanvas(null);
                    sc.drawBitmap(b, 0, 0, paint);
                    inputSurface.unlockCanvasAndPost(sc);
                }
                trackIndex = drainEncoder(encoder, muxer, bufferInfo, trackIndex, muxerStarted, false);
                if (trackIndex >= 0) muxerStarted = true;
            }
            encoder.signalEndOfInputStream();
            drainEncoder(encoder, muxer, bufferInfo, trackIndex, muxerStarted, true);
        } catch (Throwable t) {
            try {
                if (!outFile.exists() || outFile.length() == 0) {
                    FileOutputStream fos = new FileOutputStream(outFile);
                    frames.get(0).compress(Bitmap.CompressFormat.JPEG, 90, fos);
                    fos.close();
                }
            } catch (Throwable ignored) {}
        } finally {
            try { if (encoder != null) { encoder.stop(); encoder.release(); } } catch (Throwable ignored) {}
            try { if (inputSurface != null) inputSurface.release(); } catch (Throwable ignored) {}
            try { if (muxer != null) { if (muxerStarted) muxer.stop(); muxer.release(); } } catch (Throwable ignored) {}
        }
    }

    private int drainEncoder(MediaCodec encoder, MediaMuxer muxer, MediaCodec.BufferInfo bufferInfo,
                             int trackIndex, boolean muxerStarted, boolean endOfStream) {
        final int TIMEOUT_USEC = 10000;
        while (true) {
            int status = encoder.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
            if (status == MediaCodec.INFO_TRY_AGAIN_LATER) {
                if (!endOfStream) break;
            } else if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                MediaFormat newFormat = encoder.getOutputFormat();
                trackIndex = muxer.addTrack(newFormat);
                muxer.start();
                muxerStarted = true;
            } else if (status >= 0) {
                ByteBuffer encodedData = encoder.getOutputBuffer(status);
                if (encodedData != null) {
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {
                        bufferInfo.size = 0;
                    }
                    if (bufferInfo.size != 0 && muxerStarted && trackIndex >= 0) {
                        encodedData.position(bufferInfo.offset);
                        encodedData.limit(bufferInfo.offset + bufferInfo.size);
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo);
                    }
                    encoder.releaseOutputBuffer(status, false);
                    if ((bufferInfo.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
                        break;
                    }
                }
            }
        }
        return trackIndex;
    }

    private Bitmap renderCurrentCompositeBitmap(int w, int h, Bitmap customBaseBmp) {
        Bitmap baseBmp = customBaseBmp;
        if (baseBmp == null) {
            baseBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas cleanCanvas = new Canvas(baseBmp);
            if (!drawUnstretchedCameraFrame(cleanCanvas, w, h)) {
                mViewfinderContainer.getChildAt(0).draw(cleanCanvas);
            }
        }
        Bitmap compositeBmp = baseBmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas compCanvas = new Canvas(compositeBmp);

        compCanvas.save();
        compCanvas.translate(mWatermarkCardContainer.getX(), mWatermarkCardContainer.getY());
        compCanvas.scale(mWatermarkCardContainer.getScaleX(), mWatermarkCardContainer.getScaleY(),
                mWatermarkCardContainer.getPivotX(), mWatermarkCardContainer.getPivotY());
        mWatermarkCardContainer.draw(compCanvas);
        compCanvas.restore();

        if (mModel.showPhotoCode) {
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setColor(0xDDFFFFFF);
            p.setTextSize(dp(11));
            p.setTypeface(mFontMono);
            p.setShadowLayer(3f, 0f, 1f, 0xCC000000);
            compCanvas.save();
            compCanvas.translate(w - dp(12), h * 0.65f);
            compCanvas.rotate(-90f);
            compCanvas.drawText("Photo Code: " + mModel.currentPhotoCode, 0, 0, p);
            compCanvas.restore();
        }
        return compositeBmp;
    }

    private void captureWatermarkedPhoto() {
        playShutterSound();

        mShutterFlashView.setAlpha(0.85f);
        mShutterFlashView.animate().alpha(0f).setDuration(220).start();

        int w = Math.max(720, mViewfinderContainer.getWidth());
        int h = Math.max(960, mViewfinderContainer.getHeight());

        Bitmap cleanBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cleanCanvas = new Canvas(cleanBmp);

        if (!drawUnstretchedCameraFrame(cleanCanvas, w, h)) {
            mViewfinderContainer.getChildAt(0).draw(cleanCanvas);
        }

        Bitmap compositeBmp = renderCurrentCompositeBitmap(w, h, cleanBmp);

        WatermarkModel.CapturedPhoto saved = mModel.saveCapturedPhoto(this, compositeBmp, cleanBmp);
        if (saved != null && mPreviewThumbIv != null) {
            mPreviewThumbIv.setImageBitmap(compositeBmp);
            mAntiCodeVerticalTv.setText("Photo Code: " + mModel.currentPhotoCode);
        }
    }

    private void playShutterSound() {
        if (!mModel.shutterSoundEnabled) return;
        try {
            MediaPlayer mp = MediaPlayer.create(this, R.raw.camera);
            if (mp != null) {
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer player) {
                        player.release();
                    }
                });
                mp.start();
            }
        } catch (Throwable ignored) {}
    }

    // =================================================================================
    // LIVE HARDWARE CAMERA2 ENGINE
    // =================================================================================
    private void requestCameraAndLocationPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, 101);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        openCameraHardware();
        initLiveGpsListener();
    }

    private void openCameraHardware() {
        if (mTextureView == null || !mTextureView.isAvailable()) return;
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            CameraManager mgr = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            if (mgr == null) return;
            String selectedCamId = null;
            int targetFacing = mUseFrontCamera ? CameraCharacteristics.LENS_FACING_FRONT : CameraCharacteristics.LENS_FACING_BACK;
            for (String id : mgr.getCameraIdList()) {
                CameraCharacteristics cc = mgr.getCameraCharacteristics(id);
                Integer facing = cc.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == targetFacing) {
                    selectedCamId = id;
                    mSensorActiveArraySize = cc.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
                    selectOptimalPreviewSize(cc);
                    break;
                }
            }
            if (selectedCamId == null && mgr.getCameraIdList().length > 0) {
                selectedCamId = mgr.getCameraIdList()[0];
                selectOptimalPreviewSize(mgr.getCameraCharacteristics(selectedCamId));
            }
            if (selectedCamId == null) return;

            mgr.openCamera(selectedCamId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(CameraDevice camera) {
                    mCameraDevice = camera;
                    startCameraPreviewSession();
                }
                @Override
                public void onDisconnected(CameraDevice camera) {
                    camera.close();
                    mCameraDevice = null;
                }
                @Override
                public void onError(CameraDevice camera, int error) {
                    camera.close();
                    mCameraDevice = null;
                }
            }, mUiHandler);
        } catch (Throwable ignored) {}
    }

    private void selectOptimalPreviewSize(CameraCharacteristics cc) {
        try {
            android.hardware.camera2.params.StreamConfigurationMap map =
                    cc.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (map == null) return;
            android.util.Size[] sizes = map.getOutputSizes(SurfaceTexture.class);
            if (sizes == null || sizes.length == 0) return;

            android.util.Size best4x3 = null;
            long best4x3Area = 0;
            android.util.Size bestFallback = null;
            long bestFallbackArea = 0;

            for (android.util.Size s : sizes) {
                int w = s.getWidth();
                int h = s.getHeight();
                if (w > 1920 || h > 1920) continue;
                long area = (long) w * (long) h;
                float ratio = (float) Math.max(w, h) / (float) Math.max(1, Math.min(w, h));
                if (Math.abs(ratio - (4f / 3f)) < 0.03f) {
                    if (area > best4x3Area) {
                        best4x3Area = area;
                        best4x3 = s;
                    }
                }
                if (area > bestFallbackArea) {
                    bestFallbackArea = area;
                    bestFallback = s;
                }
            }

            android.util.Size chosen = (best4x3 != null) ? best4x3 : (bestFallback != null ? bestFallback : sizes[0]);
            mPreviewBufferWidth = chosen.getWidth();
            mPreviewBufferHeight = chosen.getHeight();
        } catch (Throwable ignored) {}
    }

    private void startCameraPreviewSession() {
        if (mCameraDevice == null || mTextureView == null || !mTextureView.isAvailable()) return;
        try {
            SurfaceTexture st = mTextureView.getSurfaceTexture();
            if (st == null) return;
            st.setDefaultBufferSize(mPreviewBufferWidth, mPreviewBufferHeight);
            configureTextureViewTransform(mTextureView.getWidth(), mTextureView.getHeight());
            Surface surface = new Surface(st);

            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);
            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    mCaptureSession = session;
                    updateCameraPreviewSession();
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {}
            }, mUiHandler);
        } catch (Throwable ignored) {}
    }

    private void updateCameraPreviewSession() {
        if (mCaptureSession == null || mPreviewRequestBuilder == null) return;
        try {
            if (mModel.flashMode == 2) {
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                mPreviewRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
            if (mSensorActiveArraySize != null && mModel.zoomLevel > 1.0f) {
                int w = mSensorActiveArraySize.width();
                int h = mSensorActiveArraySize.height();
                int cropW = (int) (w / mModel.zoomLevel);
                int cropH = (int) (h / mModel.zoomLevel);
                int left = (w - cropW) / 2;
                int top = (h - cropH) / 2;
                mPreviewRequestBuilder.set(CaptureRequest.SCALER_CROP_REGION, new Rect(left, top, left + cropW, top + cropH));
            }
            mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mUiHandler);
        } catch (Throwable ignored) {}
    }

    private void closeCameraHardware() {
        try {
            if (mCaptureSession != null) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (Throwable ignored) {}
    }

    private void initLiveGpsListener() {
        try {
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (lm == null) return;
            Location last = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (last == null) last = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (last != null) {
                mModel.currentLat = last.getLatitude();
                mModel.currentLon = last.getLongitude();
                mModel.latLongLine = String.format(Locale.US, "%.5f°N  %.5f°E", last.getLatitude(), last.getLongitude());
                if (last.hasAltitude() && last.getAltitude() != 0) {
                    mModel.altitudeLine = String.format(Locale.US, "%.1fm ASL", last.getAltitude());
                }
                try {
                    android.location.Geocoder geocoder = new android.location.Geocoder(this, Locale.getDefault());
                    java.util.List<android.location.Address> addrs = geocoder.getFromLocation(last.getLatitude(), last.getLongitude(), 1);
                    if (addrs != null && !addrs.isEmpty()) {
                        String line = addrs.get(0).getAddressLine(0);
                        if (line != null && !line.isEmpty()) {
                            mModel.addressLine = line;
                        }
                    }
                } catch (Throwable ignoredGeo) {}
                rebuildWatermarkOverlay();
            }
        } catch (Throwable ignored) {}
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUiHandler.post(mClockTicker);
        if (mTextureView != null && mTextureView.isAvailable()) {
            openCameraHardware();
        }
    }

    @Override
    protected void onPause() {
        mUiHandler.removeCallbacks(mClockTicker);
        closeCameraHardware();
        super.onPause();
    }
}
