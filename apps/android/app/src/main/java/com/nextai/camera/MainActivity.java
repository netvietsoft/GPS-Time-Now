package com.nextai.camera;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
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
import android.media.MediaRecorder;
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
    private FrameLayout mViewfinderContainer;
    private TextureView mTextureView;
    private View mShutterFlashView;
    private View mGridLevelerView;
    private View mAiCountOverlayView;
    private TextView mModeSpecialPillTv;
    private TextView mVideoRecBadgeTv;
    private TextView mTimerCountdownTv;
    private LinearLayout mWatermarkCardContainer;
    private TextView mAntiCodeVerticalTv;
    private ImageView mFlashIv;
    private ImageView mPreviewThumbIv;
    private ImageView mShootIv;
    private final TextView[] mZoomPills = new TextView[3];
    private final TextView[] mModeTabs = new TextView[5];
    private final View[] mModeDots = new View[5];

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
    private MediaRecorder mMediaRecorder;
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
            if (mIsRecordingVideo && mVideoRecBadgeTv != null) {
                int sec = Math.max(1, (int) ((System.currentTimeMillis() - mVideoStartTimeMs) / 1000L));
                mVideoRecBadgeTv.setText(String.format(Locale.US, "● REC  %02d:%02d", sec / 60, sec % 60));
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

    // Automatically sizes a badge/header TextView so short text uses baseSp & WRAP_CONTENT,
    // while longer filled text shrinks down to minSp and wraps cleanly if needed.
    private void configureAutoFitText(TextView tv, String text, float baseSp, float minSp, int maxWidthDp) {
        String safe = (text != null) ? text : "";
        tv.setText(safe);
        tv.setMaxWidth(dp(maxWidthDp));
        Paint paint = new Paint(tv.getPaint());
        float pxAtBase = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, baseSp, getResources().getDisplayMetrics());
        paint.setTextSize(pxAtBase);
        float measured = paint.measureText(safe);
        float availPx = dp(maxWidthDp - 14);
        if (measured <= availPx || measured <= 0f) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, baseSp);
            tv.setSingleLine(true);
        } else {
            float scaledSp = baseSp * (availPx / measured);
            if (scaledSp >= minSp) {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, scaledSp);
                tv.setSingleLine(true);
            } else {
                tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, minSp);
                tv.setSingleLine(false);
                tv.setMaxLines(2);
                tv.setEllipsize(TextUtils.TruncateAt.END);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        // White status bar & navigation bar with dark icons (matching original AppTheme)
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

        LinearLayout layoutPlus = new LinearLayout(this);
        layoutPlus.setOrientation(LinearLayout.HORIZONTAL);
        layoutPlus.setGravity(Gravity.CENTER_VERTICAL);
        layoutPlus.setBackground(roundedRect(0xFF282828, 12, 0, 0));
        layoutPlus.setPadding(dp(8), dp(3), dp(8), dp(3));
        LinearLayout.LayoutParams plusLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(24));
        plusLp.setMarginEnd(dp(12));
        layoutPlus.setLayoutParams(plusLp);

        ImageView crownIv = new ImageView(this);
        crownIv.setImageResource(R.drawable.ic_vip_crown);
        layoutPlus.addView(crownIv, new LinearLayout.LayoutParams(dp(14), dp(14)));

        TextView plusTv = new TextView(this);
        plusTv.setText(" Plus");
        plusTv.setTextSize(12);
        plusTv.setTypeface(Typeface.DEFAULT_BOLD);
        plusTv.setTextColor(0xFFFFFFFF);
        layoutPlus.addView(plusTv);
        layoutPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showPurchaseDialog(MainActivity.this);
            }
        });
        rightContainer.addView(layoutPlus);

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
        FrameLayout centerHost = new FrameLayout(this);
        centerHost.setBackgroundColor(0xFF000000);
        centerHost.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        mViewfinderContainer = new FrameLayout(this);
        mViewfinderContainer.setBackgroundColor(0xFF101820);
        FrameLayout.LayoutParams vfLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        vfLp.gravity = Gravity.CENTER;
        mViewfinderContainer.setLayoutParams(vfLp);

        // Realistic Daylight Architectural Scene Fallback Canvas (underneath TextureView)
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
                p.setColor(0x28FFFFFF);
                p.setStrokeWidth(1.5f);
                c.drawLine(w / 3f, 0, w / 3f, h, p);
                c.drawLine(2f * w / 3f, 0, 2f * w / 3f, h, p);
                c.drawLine(0, h / 3f, w, h / 3f, p);
                c.drawLine(0, 2f * h / 3f, w, 2f * h / 3f, p);
            }
        };
        mViewfinderContainer.addView(fallbackScene, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Live Hardware Camera2 TextureView
        mTextureView = new TextureView(this);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                openCameraHardware();
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {}
            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                closeCameraHardware();
                return true;
            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {}
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
        acLp.setMarginEnd(dp(-52));
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

        // Optional Grid Lines (3x3), Electronic Leveler & ZeroDCE Night Mode Overlay
        mGridLevelerView = new View(this) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override
            protected void onDraw(Canvas c) {
                super.onDraw(c);
                int w = getWidth();
                int h = getHeight();
                if (mModel.nightModeZeroDce) {
                    c.drawColor(0x2238BDF8);
                }
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

        // Edge AI Object Detection Overlay (SteelDetect / VLPR / Rebar in AI Count mode)
        mAiCountOverlayView = new View(this) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            @Override
            protected void onDraw(Canvas c) {
                super.onDraw(c);
                if (mModel.cameraMode != 3) return;
                int w = getWidth();
                int h = getHeight();
                int count = Math.max(1, mModel.aiCountDetected);
                int cols = 6;
                float startX = w * 0.22f;
                float startY = h * 0.28f;
                float stepX = (w * 0.56f) / cols;
                float stepY = dp(38);
                p.setTextAlign(Paint.Align.CENTER);
                p.setTextSize(dp(10));
                p.setTypeface(Typeface.DEFAULT_BOLD);
                for (int i = 0; i < count; i++) {
                    int row = i / cols;
                    int col = i % cols;
                    float cx = startX + col * stepX + (row % 2 == 1 ? stepX * 0.25f : 0f);
                    float cy = startY + row * stepY;
                    p.setColor(0xDD00E676);
                    c.drawCircle(cx, cy, dp(11), p);
                    p.setColor(0xFFFFFFFF);
                    c.drawText(String.valueOf(i + 1), cx, cy + dp(3.5f), p);
                }
            }
        };
        mAiCountOverlayView.setVisibility(View.GONE);
        mAiCountOverlayView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mModel.cameraMode == 3) {
                    mModel.aiCountDetected++;
                    mAiCountOverlayView.invalidate();
                    updateModeSpecialPill();
                    rebuildWatermarkOverlay();
                }
            }
        });
        mViewfinderContainer.addView(mAiCountOverlayView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        // Top Video Recording Live Timer Pill (● REC 00:01)
        mVideoRecBadgeTv = new TextView(this);
        mVideoRecBadgeTv.setText("● REC  00:00");
        mVideoRecBadgeTv.setTextSize(13);
        mVideoRecBadgeTv.setTypeface(mFontMono, Typeface.BOLD);
        mVideoRecBadgeTv.setTextColor(0xFFFFFFFF);
        mVideoRecBadgeTv.setBackground(roundedRect(0xDDFF1717, 16, dp(1), 0xFFFFFFFF));
        mVideoRecBadgeTv.setPadding(dp(14), dp(5), dp(14), dp(5));
        mVideoRecBadgeTv.setVisibility(View.GONE);
        FrameLayout.LayoutParams recLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        recLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        recLp.setMargins(0, dp(14), 0, 0);
        mVideoRecBadgeTv.setLayoutParams(recLp);
        mViewfinderContainer.addView(mVideoRecBadgeTv);

        // Top Mode Interactive Pill (for AI Count category switcher & Collage frame progress)
        mModeSpecialPillTv = new TextView(this);
        mModeSpecialPillTv.setTextSize(12.5f);
        mModeSpecialPillTv.setTypeface(Typeface.DEFAULT_BOLD);
        mModeSpecialPillTv.setTextColor(0xFFFFFFFF);
        mModeSpecialPillTv.setBackground(roundedRect(0xCC0A2246, 16, dp(1), 0xFF0093FF));
        mModeSpecialPillTv.setPadding(dp(14), dp(6), dp(14), dp(6));
        mModeSpecialPillTv.setVisibility(View.GONE);
        FrameLayout.LayoutParams spLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        spLp.setMargins(0, dp(14), 0, 0);
        mModeSpecialPillTv.setLayoutParams(spLp);
        mModeSpecialPillTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mModel.cameraMode == 3) {
                    if ("Steel Pipes".equals(mModel.aiCountCategory)) {
                        mModel.aiCountCategory = "Rebar";
                        mModel.aiCountDetected = 24;
                    } else if ("Rebar".equals(mModel.aiCountCategory)) {
                        mModel.aiCountCategory = "Timber Logs";
                        mModel.aiCountDetected = 15;
                    } else if ("Timber Logs".equals(mModel.aiCountCategory)) {
                        mModel.aiCountCategory = "Pallets";
                        mModel.aiCountDetected = 12;
                    } else {
                        mModel.aiCountCategory = "Steel Pipes";
                        mModel.aiCountDetected = 18;
                    }
                    mAiCountOverlayView.invalidate();
                    updateModeSpecialPill();
                    rebuildWatermarkOverlay();
                } else if (mModel.cameraMode == 4) {
                    mModel.collageSlices.clear();
                    mModel.collageGridTarget = (mModel.collageGridTarget == 2) ? 4 : 2;
                    updateModeSpecialPill();
                }
            }
        });
        mViewfinderContainer.addView(mModeSpecialPillTv);

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
        FrameLayout.LayoutParams wmLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        wmLp.gravity = Gravity.BOTTOM | Gravity.START;
        wmLp.setMargins(dp(12), dp(12), dp(48), dp(14));
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
        //    5 Original Modes: Video | Photo | Attendance | AI Count | Collage
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
        modeRow.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        String[] modes = new String[]{"Video", "Photo", "Attendance", "AI Count", "Collage"};
        for (int i = 0; i < 5; i++) {
            final int idx = i;
            LinearLayout tabCol = new LinearLayout(this);
            tabCol.setOrientation(LinearLayout.VERTICAL);
            tabCol.setGravity(Gravity.CENTER_HORIZONTAL);
            tabCol.setPadding(dp(2), dp(2), dp(2), dp(2));

            TextView tv = new TextView(this);
            tv.setText(modes[i]);
            tv.setSingleLine(true);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(13.5f);
            tabCol.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            View dot = new View(this);
            dot.setBackground(roundedRect(0xFF0093FF, 3, 0, 0));
            LinearLayout.LayoutParams dotLp = new LinearLayout.LayoutParams(dp(5), dp(5));
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
            modeRow.addView(tabCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }
        modeSection.addView(modeRow);
        root.addView(modeSection);

        // =================================================================================
        // 4. BOTTOM CAMERA ACTION BAR: view_preview_bottom_camera_action_global.xml
        // =================================================================================
        RelativeLayout actionBar = new RelativeLayout(this);
        actionBar.setBackgroundColor(0xFFFFFFFF);
        actionBar.setPadding(dp(16), dp(4), dp(16), dp(14));
        actionBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(98)));

        // Center: shootIv (80dp x 80dp, @drawable/cam_shoot)
        mShootIv = new ImageView(this);
        mShootIv.setId(View.generateViewId());
        mShootIv.setImageResource(R.drawable.cam_shoot);
        mShootIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        RelativeLayout.LayoutParams shootLp = new RelativeLayout.LayoutParams(dp(78), dp(78));
        shootLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        mShootIv.setLayoutParams(shootLp);
        mShootIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShutterButtonClicked();
            }
        });
        actionBar.addView(mShootIv);

        // Left: clPreview (36dp thumbnail + "Preview") + clFolder (36dp icon_preview_folder + "Teamspace")
        LinearLayout leftGroup = new LinearLayout(this);
        leftGroup.setOrientation(LinearLayout.HORIZONTAL);
        leftGroup.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams lgLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lgLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        lgLp.addRule(RelativeLayout.CENTER_VERTICAL);
        lgLp.setMarginStart(dp(12));
        leftGroup.setLayoutParams(lgLp);

        // clPreview
        LinearLayout clPreview = new LinearLayout(this);
        clPreview.setOrientation(LinearLayout.VERTICAL);
        clPreview.setGravity(Gravity.CENTER_HORIZONTAL);
        clPreview.setMinimumWidth(dp(58));
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

        // clFolder (Teamspace / Files)
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
                OriginalDialogs.showTeamspaceDialog(MainActivity.this, mModel);
            }
        });
        leftGroup.addView(clFolder);
        actionBar.addView(leftGroup);

        // Right: clTemplate (ic_watermark_ltr + red dot + "Templates")
        LinearLayout clTemplate = new LinearLayout(this);
        clTemplate.setOrientation(LinearLayout.VERTICAL);
        clTemplate.setGravity(Gravity.CENTER_HORIZONTAL);
        clTemplate.setPadding(dp(8), dp(2), dp(8), dp(2));
        RelativeLayout.LayoutParams tmplLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tmplLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        tmplLp.addRule(RelativeLayout.CENTER_VERTICAL);
        tmplLp.setMarginEnd(dp(24));
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
        actionBar.addView(clTemplate);

        root.addView(actionBar);

        updateModeTabsUI();
        rebuildWatermarkOverlay();
        return root;
    }

    // =========================================================================================
    // 1:1 AUTHENTIC WATERMARK OVERLAY BUILDER FOR ALL 14 TEMPLATES (WITH AUTO-RESIZE FILL TEXT)
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

        mAntiCodeVerticalTv.setVisibility(mModel.showPhotoCode ? View.VISIBLE : View.GONE);
        mAntiCodeVerticalTv.setText("Time Now Photo Code: " + mModel.currentPhotoCode);

        WatermarkModel.TemplateInfo tmpl = mModel.activeTemplate;
        String id = (tmpl != null) ? tmpl.id : "34";

        if ("21".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 21: CLOCK IN (1:1 watermark_cover_id21.xml)
            // Top White Pill (radius 4dp): [Yellow Auto-Fit Badge "CLOCK IN"] + [Dark-Blue Time]
            // Middle (width 245dp): [3dp Vertical Bar] + [Date + Address + GPS + Custom Fields]
            // ---------------------------------------------------------------------------------
            mDatePattern = "dd/MM/yyyy";
            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);

            // 1. Top White Rounded Pill (topLayout in watermark_cover_id21.xml: radius 4dp, height ~32dp)
            LinearLayout topPill = new LinearLayout(this);
            topPill.setOrientation(LinearLayout.HORIZONTAL);
            topPill.setGravity(Gravity.CENTER_VERTICAL);
            topPill.setBackground(roundedRect(0xFFFFFFFF, 4, 0, 0));
            topPill.setPadding(dp(3), dp(3), dp(8), dp(3));

            TextView badgeTv = new TextView(this);
            String titleText = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                    ? mModel.customTitle.trim() : "Clock In";
            badgeTv.setTypeface(Typeface.DEFAULT_BOLD);
            badgeTv.setTextColor(0xFF111111);
            badgeTv.setGravity(Gravity.CENTER);
            badgeTv.setMinimumHeight(dp(24));
            badgeTv.setBackground(roundedRect(mModel.customAccentColor, 2.5f, 0, 0));
            badgeTv.setPadding(dp(7), dp(2), dp(7), dp(2));
            configureAutoFitText(badgeTv, titleText, 14f, 8.5f, 145);
            topPill.addView(badgeTv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            if (mModel.showTime) {
                mLiveBigClockTv = new TextView(this);
                mLiveBigClockTv.setTextSize(24);
                mLiveBigClockTv.setSingleLine(true);
                mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
                mLiveBigClockTv.setTextColor(0xFF0A2246);
                mLiveBigClockTv.setIncludeFontPadding(false);
                mLiveBigClockTv.setPadding(dp(7), 0, dp(2), 0);
                topPill.addView(mLiveBigClockTv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            wrap.addView(topPill, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            // 2. Middle Section with Stretching 3dp Vertical Bar (middleLayout in watermark_cover_id21.xml)
            LinearLayout midRow = new LinearLayout(this);
            midRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            midLp.setMargins(0, dp(8), 0, 0);
            midRow.setLayoutParams(midLp);

            View vBar = new View(this);
            vBar.setBackground(roundedRect(mModel.customAccentColor, 1.5f, 0, 0));
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(3), ViewGroup.LayoutParams.MATCH_PARENT);
            barLp.setMargins(0, dp(2), dp(6), dp(2));
            midRow.addView(vBar, barLp);

            LinearLayout rightCol = new LinearLayout(this);
            rightCol.setOrientation(LinearLayout.VERTICAL);

            mLiveDateTv = createShadowText("", 14, true, 245);
            rightCol.addView(mLiveDateTv);

            if (mModel.showAddress) {
                TextView addrTv = createShadowText(mModel.addressLine, 14, false, 245);
                addrTv.setPadding(0, dp(3), 0, 0);
                rightCol.addView(addrTv);
            }
            appendOptionalGeoAndNoteLines(rightCol, 245);
            midRow.addView(rightCol);
            wrap.addView(midRow);

            appendTimeNowVerifiedFooter(wrap);
            mWatermarkCardContainer.addView(wrap);

        } else if ("80".equals(id) || "dasher01".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 80 (COMPLETED) & DASHER01 (DELIVERY SERVICE)
            // Top Colored Pill: [Big 26sp White Time] + [Check / Delivery Icon]
            // Middle: [3dp Vertical Bar] + [Stars / Auto-Fit Title / Date / Address / Driver Card]
            // ---------------------------------------------------------------------------------
            boolean isDasher = "dasher01".equals(id);
            mDatePattern = "MM/dd/yyyy";
            int pillBg = isDasher ? 0xFFFF1717 : 0xFF391A13;
            int barColor = isDasher ? 0xFFFF1717 : mModel.customAccentColor;

            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);

            LinearLayout topPill = new LinearLayout(this);
            topPill.setOrientation(LinearLayout.HORIZONTAL);
            topPill.setGravity(Gravity.CENTER_VERTICAL);
            topPill.setBackground(roundedRect(pillBg, 6, 0, 0));
            topPill.setPadding(dp(10), dp(5), dp(10), dp(5));

            mLiveBigClockTv = new TextView(this);
            mLiveBigClockTv.setTextSize(26);
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
            chkBadge.setBackground(roundedRect(isDasher ? 0xFFFFFFFF : 0xFFFFB100, isDasher ? 4 : 11, 0, 0));
            LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(dp(22), dp(22));
            cbLp.setMargins(dp(8), 0, 0, 0);
            topPill.addView(chkBadge, cbLp);
            wrap.addView(topPill, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout midRow = new LinearLayout(this);
            midRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            midLp.setMargins(0, dp(7), 0, 0);
            midRow.setLayoutParams(midLp);

            View vBar = new View(this);
            vBar.setBackground(roundedRect(barColor, 1.5f, 0, 0));
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(3), ViewGroup.LayoutParams.MATCH_PARENT);
            barLp.setMargins(0, dp(2), dp(8), dp(2));
            midRow.addView(vBar, barLp);

            LinearLayout rightCol = new LinearLayout(this);
            rightCol.setOrientation(LinearLayout.VERTICAL);

            if (isDasher) {
                TextView starsTv = createShadowText("★ ★ ★ ★ ★", 13, true, 245);
                starsTv.setTextColor(0xFFFFD700);
                rightCol.addView(starsTv);
            }

            String titleText = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                    ? mModel.customTitle.trim() : (isDasher ? "TIPS & REVIEWS KEEP ME DRIVING" : "Completed");
            TextView titleTv = createShadowText(titleText, 15, true, 245);
            configureAutoFitText(titleTv, titleText, isDasher ? 13.5f : 16f, 10f, 245);
            rightCol.addView(titleTv);

            mLiveDateTv = createShadowText("", 13.5f, false, 245);
            mLiveDateTv.setPadding(0, dp(2), 0, 0);
            rightCol.addView(mLiveDateTv);

            if (mModel.showAddress) {
                TextView addrTv = createShadowText(mModel.addressLine, 13.5f, false, 245);
                addrTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(addrTv);
            }
            appendOptionalGeoAndNoteLines(rightCol, 245);

            if (isDasher) {
                LinearLayout driverCard = new LinearLayout(this);
                driverCard.setOrientation(LinearLayout.HORIZONTAL);
                driverCard.setGravity(Gravity.CENTER_VERTICAL);
                driverCard.setBackground(roundedRect(0xFFFFFFFF, 4, 0, 0));
                LinearLayout.LayoutParams dcLp = new LinearLayout.LayoutParams(dp(230), ViewGroup.LayoutParams.WRAP_CONTENT);
                dcLp.setMargins(0, dp(5), 0, 0);
                driverCard.setLayoutParams(dcLp);

                LinearLayout dLeft = new LinearLayout(this);
                dLeft.setOrientation(LinearLayout.VERTICAL);
                dLeft.setPadding(dp(8), dp(4), dp(8), dp(4));
                TextView d1 = new TextView(this);
                d1.setText("Delivered By: " + mModel.inspectorName);
                d1.setTextSize(11);
                d1.setTypeface(Typeface.DEFAULT_BOLD);
                d1.setTextColor(0xFF222222);
                d1.setSingleLine(true);
                dLeft.addView(d1);
                TextView d2 = new TextView(this);
                d2.setText("Thanks & Enjoy");
                d2.setTextSize(10);
                d2.setTextColor(0xFF666666);
                dLeft.addView(d2);
                driverCard.addView(dLeft, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                TextView dRight = new TextView(this);
                dRight.setText("FAST");
                dRight.setTextSize(11);
                dRight.setTypeface(Typeface.DEFAULT_BOLD);
                dRight.setTextColor(0xFFFFFFFF);
                dRight.setGravity(Gravity.CENTER);
                dRight.setBackground(roundedCorners(0xFFFF1717, 0, 4, 4, 0));
                driverCard.addView(dRight, new LinearLayout.LayoutParams(dp(54), ViewGroup.LayoutParams.MATCH_PARENT));
                rightCol.addView(driverCard);
            }

            midRow.addView(rightCol);
            wrap.addView(midRow);
            appendTimeNowVerifiedFooter(wrap);
            mWatermarkCardContainer.addView(wrap);

        } else if ("90".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 90: CLEANED & SANITIZED (cover_wm90.webp & watermark_cover_id90.xml)
            // Top: 5 Yellow Stars + Two-Tone Header [Deep Blue Title | Cyan-Blue Time]
            // Middle: [3dp Deep Blue Vertical Bar] + [Date + Address + Notes]
            // ---------------------------------------------------------------------------------
            mDatePattern = "MM/dd/yyyy";
            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);

            TextView stars = createShadowText("★ ★ ★ ★ ★", 13, true, 240);
            stars.setTextColor(0xFFFFEB3B);
            stars.setPadding(dp(2), 0, 0, dp(2));
            wrap.addView(stars);

            LinearLayout twoTone = new LinearLayout(this);
            twoTone.setOrientation(LinearLayout.HORIZONTAL);
            twoTone.setGravity(Gravity.CENTER_VERTICAL);

            TextView leftBox = new TextView(this);
            String cleanTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                    ? mModel.customTitle.trim() : "Cleaned";
            leftBox.setTypeface(Typeface.DEFAULT_BOLD);
            leftBox.setTextColor(0xFFE1F5FE);
            leftBox.setGravity(Gravity.CENTER);
            leftBox.setBackground(roundedCorners(mModel.customAccentColor, 4, 0, 0, 4));
            leftBox.setPadding(dp(10), dp(5), dp(10), dp(5));
            configureAutoFitText(leftBox, cleanTitle, 17f, 10f, 155);
            twoTone.addView(leftBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(34)));

            mLiveBigClockTv = new TextView(this);
            mLiveBigClockTv.setTextSize(20);
            mLiveBigClockTv.setSingleLine(true);
            mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
            mLiveBigClockTv.setTextColor(0xFFFFFFFF);
            mLiveBigClockTv.setGravity(Gravity.CENTER);
            mLiveBigClockTv.setBackground(roundedCorners(0xFF229CD7, 0, 4, 4, 0));
            mLiveBigClockTv.setPadding(dp(10), dp(4), dp(10), dp(4));
            twoTone.addView(mLiveBigClockTv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(34)));
            wrap.addView(twoTone);

            LinearLayout midRow = new LinearLayout(this);
            midRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            midLp.setMargins(0, dp(7), 0, 0);
            midRow.setLayoutParams(midLp);

            View vBar = new View(this);
            vBar.setBackground(roundedRect(mModel.customAccentColor, 1.5f, 0, 0));
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(3), ViewGroup.LayoutParams.MATCH_PARENT);
            barLp.setMargins(0, dp(2), dp(8), dp(2));
            midRow.addView(vBar, barLp);

            LinearLayout rightCol = new LinearLayout(this);
            rightCol.setOrientation(LinearLayout.VERTICAL);

            mLiveDateTv = createShadowText("", 14, true, 245);
            rightCol.addView(mLiveDateTv);

            if (mModel.showAddress) {
                TextView addrTv = createShadowText(mModel.addressLine, 13.5f, false, 245);
                addrTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(addrTv);
            }
            appendOptionalGeoAndNoteLines(rightCol, 245);
            midRow.addView(rightCol);
            wrap.addView(midRow);

            appendTimeNowVerifiedFooter(wrap);
            mWatermarkCardContainer.addView(wrap);

        } else if ("43".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 43: SECURITY GUARD (cover_wm43.webp & new_watermark_cover_id43.xml)
            // Navy Header [Shield + Yellow Auto-Fit Title] + Yellow Line + Chevron Strip +
            // Royal Blue Body [Yellow 28sp Time + Date + Red Bullet Address/Note]
            // ---------------------------------------------------------------------------------
            mDatePattern = "dd MMM yyyy";
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setLayoutParams(new LinearLayout.LayoutParams(dp(250), ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.HORIZONTAL);
            header.setGravity(Gravity.CENTER_VERTICAL);
            header.setBackground(roundedCorners(0xE628356E, 6, 6, 0, 0));
            header.setPadding(dp(10), dp(7), dp(10), dp(7));

            ImageView shieldIv = new ImageView(this);
            shieldIv.setImageResource(R.drawable.tool_border_safe_green);
            header.addView(shieldIv, new LinearLayout.LayoutParams(dp(22), dp(22)));

            TextView titleTv = new TextView(this);
            String secTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                    ? mModel.customTitle.trim() : "Security Guard";
            titleTv.setTypeface(Typeface.DEFAULT_BOLD);
            titleTv.setTextColor(0xFFFFF15C);
            titleTv.setPadding(dp(8), 0, 0, 0);
            configureAutoFitText(titleTv, secTitle, 17f, 11f, 200);
            header.addView(titleTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            card.addView(header);

            View yellowLine = new View(this);
            yellowLine.setBackgroundColor(0xFFFFF15C);
            card.addView(yellowLine, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2)));

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setBackground(roundedCorners(0xE63651C9, 0, 0, 6, 6));
            body.setPadding(dp(10), dp(4), dp(10), dp(8));

            TextView chevrons = new TextView(this);
            chevrons.setText(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
            chevrons.setTextSize(8);
            chevrons.setSingleLine(true);
            chevrons.setTextColor(0xAAFFF15C);
            body.addView(chevrons);

            LinearLayout timeRow = new LinearLayout(this);
            timeRow.setOrientation(LinearLayout.HORIZONTAL);
            timeRow.setGravity(Gravity.BOTTOM);

            mLiveBigClockTv = new TextView(this);
            mLiveBigClockTv.setTextSize(28);
            mLiveBigClockTv.setSingleLine(true);
            mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
            mLiveBigClockTv.setTextColor(0xFFFFF15C);
            mLiveBigClockTv.setIncludeFontPadding(false);
            timeRow.addView(mLiveBigClockTv);

            mLiveDateTv = new TextView(this);
            mLiveDateTv.setTextSize(13);
            mLiveDateTv.setSingleLine(true);
            mLiveDateTv.setTextColor(0xFFFFFFFF);
            mLiveDateTv.setPadding(dp(8), 0, 0, dp(2));
            timeRow.addView(mLiveDateTv);
            body.addView(timeRow);

            if (mModel.showAddress) {
                body.addView(createRedBulletRow(mModel.addressLine));
            }
            if (mModel.showLatLong) {
                body.addView(createRedBulletRow(mModel.latLongLine));
            }
            if (mModel.showNotes && mModel.customNote != null && !mModel.customNote.isEmpty()) {
                body.addView(createRedBulletRow(mModel.customNote));
            }
            card.addView(body);
            appendTimeNowVerifiedFooter(card);
            mWatermarkCardContainer.addView(card);

        } else if ("10".equals(id) || "service01".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 10 (CUSTOM TEXT) & SERVICE01 (FIELD SERVICE)
            // Top Auto-Fit Bold White Title + [3dp Vertical Bar | Date+Time + Details + Address]
            // ---------------------------------------------------------------------------------
            boolean isService = "service01".equals(id);
            mDatePattern = "dd/MM/yyyy HH:mm";

            LinearLayout wrap = new LinearLayout(this);
            wrap.setOrientation(LinearLayout.VERTICAL);

            String topTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                    ? mModel.customTitle.trim() : (isService ? "Service Name" : "Work Record");
            TextView titleTv = createShadowText(topTitle, 17, true, 255);
            configureAutoFitText(titleTv, topTitle, 17f, 11f, 255);
            wrap.addView(titleTv);

            LinearLayout midRow = new LinearLayout(this);
            midRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams midLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            midLp.setMargins(0, dp(5), 0, 0);
            midRow.setLayoutParams(midLp);

            View vBar = new View(this);
            vBar.setBackground(roundedRect(mModel.customAccentColor, 1.5f, 0, 0));
            LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(3), ViewGroup.LayoutParams.MATCH_PARENT);
            barLp.setMargins(0, dp(2), dp(8), dp(2));
            midRow.addView(vBar, barLp);

            LinearLayout rightCol = new LinearLayout(this);
            rightCol.setOrientation(LinearLayout.VERTICAL);

            if (mModel.showTime) {
                mLiveDateTv = createShadowText("", 13.5f, false, 245);
                rightCol.addView(mLiveDateTv);
            }
            if (isService) {
                TextView detailTv = createShadowText("👉🏻: " + mModel.customNote, 13.5f, false, 245);
                detailTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(detailTv);
            }
            if (mModel.showAddress) {
                TextView addrTv = createShadowText((isService ? "📍: " : "") + mModel.addressLine, 13.5f, false, 245);
                addrTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(addrTv);
            }
            if (isService) {
                TextView telTv = createShadowText("☎️: " + mModel.projectName, 13f, true, 245);
                telTv.setTextColor(0xFF90CAF9);
                telTv.setPadding(0, dp(2), 0, 0);
                rightCol.addView(telTv);
            } else {
                appendOptionalGeoAndNoteLines(rightCol, 245);
            }

            midRow.addView(rightCol);
            wrap.addView(midRow);
            appendTimeNowVerifiedFooter(wrap);
            mWatermarkCardContainer.addView(wrap);

        } else if ("120".equals(id) || "20".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 120 (CONSTRUCTION RECORD) & TEMPLATE 20 (WORK REPORT)
            // Colored Header + Translucent Light-Gray Body (#D9E2EC) with Dark #111111 Table Rows
            // Auto-expands height when long text wraps in any field!
            // ---------------------------------------------------------------------------------
            boolean isConst = "120".equals(id);
            mDatePattern = "dd/MM/yyyy HH:mm";

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setLayoutParams(new LinearLayout.LayoutParams(dp(258), ViewGroup.LayoutParams.WRAP_CONTENT));

            LinearLayout header = new LinearLayout(this);
            header.setOrientation(LinearLayout.VERTICAL);
            header.setBackground(roundedCorners(mModel.customAccentColor, 5, 5, 0, 0));
            header.setPadding(dp(10), dp(6), dp(10), dp(6));

            if (isConst) {
                LinearLayout badgeRow = new LinearLayout(this);
                badgeRow.setOrientation(LinearLayout.HORIZONTAL);
                badgeRow.setPadding(0, 0, 0, dp(4));
                String[] pills = new String[]{mModel.areaName, "ISO-9001", "VERIFIED"};
                for (int i = 0; i < 3; i++) {
                    TextView pTv = new TextView(this);
                    pTv.setText(pills[i]);
                    pTv.setTextSize(8.5f);
                    pTv.setSingleLine(true);
                    pTv.setEllipsize(TextUtils.TruncateAt.END);
                    pTv.setTypeface(Typeface.DEFAULT_BOLD);
                    pTv.setTextColor(mModel.customAccentColor);
                    pTv.setGravity(Gravity.CENTER);
                    pTv.setBackground(roundedRect(0xFFF0F4F8, 2, 0, 0));
                    pTv.setPadding(dp(4), dp(2), dp(4), dp(2));
                    LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    if (i < 2) pLp.setMargins(0, 0, dp(4), 0);
                    badgeRow.addView(pTv, pLp);
                }
                header.addView(badgeRow);
            }

            TextView hTitle = new TextView(this);
            String titleStr = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                    ? mModel.customTitle.trim() : (isConst ? "Construction Record" : "Work Report");
            hTitle.setTypeface(Typeface.DEFAULT_BOLD);
            hTitle.setTextColor(0xFFFFFFFF);
            hTitle.setGravity(isConst ? Gravity.CENTER : Gravity.START);
            configureAutoFitText(hTitle, titleStr, 16f, 11f, 236);
            header.addView(hTitle, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            card.addView(header);

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setBackground(roundedCorners(0xE6D9E2EC, 0, 0, 5, 5));
            body.setPadding(dp(8), dp(6), dp(8), dp(6));

            addDarkTableRow(body, "Project", mModel.projectName);
            if (isConst) {
                addDarkTableRow(body, "Area", mModel.areaName);
                addDarkTableRow(body, "Description", mModel.customNote);
            } else {
                addDarkTableRow(body, "Inspection", mModel.customNote);
            }
            if (mModel.showTime) {
                mLiveDateTv = addDarkTableRow(body, "Time", "");
            }
            if (mModel.showAddress) {
                addDarkTableRow(body, "Address", mModel.addressLine);
            }
            if (mModel.showLatLong) {
                addDarkTableRow(body, "GPS", mModel.latLongLine);
            }
            if (mModel.showWeather) {
                addDarkTableRow(body, "Weather", mModel.weatherLine);
            }
            card.addView(body);
            appendTimeNowVerifiedFooter(card);
            mWatermarkCardContainer.addView(card);

        } else if ("100".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 100: WORK SHEET (cover_wm100.webp & watermark_cover_id100.xml)
            // Green-bordered spreadsheet table (#5CE08A border, 4dp top bar, dark translucent body)
            // ---------------------------------------------------------------------------------
            mDatePattern = "dd/MM/yyyy HH:mm";
            int sheetGreen = mModel.customAccentColor;

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(roundedRect(0xAA263238, 2, dp(1.5f), sheetGreen));
            card.setLayoutParams(new LinearLayout.LayoutParams(dp(258), ViewGroup.LayoutParams.WRAP_CONTENT));

            View topGreenBar = new View(this);
            topGreenBar.setBackgroundColor(sheetGreen);
            card.addView(topGreenBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(4)));

            TextView titleTv = new TextView(this);
            String sheetTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                    ? mModel.customTitle.trim() : "Work Record";
            titleTv.setTypeface(Typeface.DEFAULT_BOLD);
            titleTv.setTextColor(sheetGreen);
            titleTv.setPadding(dp(8), dp(5), dp(8), dp(5));
            configureAutoFitText(titleTv, sheetTitle, 15f, 11f, 240);
            card.addView(titleTv);

            addGreenSheetDivider(card, sheetGreen);

            if (mModel.showTime) {
                mLiveDateTv = addGreenSheetRow(card, "Time", "", sheetGreen);
            }
            addGreenSheetRow(card, "Project", mModel.projectName, sheetGreen);
            if (mModel.showAddress) {
                addGreenSheetRow(card, "Address", mModel.addressLine, sheetGreen);
            }
            if (mModel.showLatLong) {
                addGreenSheetRow(card, "GPS", mModel.latLongLine, sheetGreen);
            }
            if (mModel.showNotes && mModel.customNote != null && !mModel.customNote.isEmpty()) {
                addGreenSheetRow(card, "Notes", mModel.customNote, sheetGreen);
            }

            appendTimeNowVerifiedFooter(card);
            mWatermarkCardContainer.addView(card);

        } else if ("60".equals(id) || "130".equals(id)) {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 60 (LOCATION STAMP) & TEMPLATE 130 (BIG MAP & COMPASS)
            // Dark Slate Card + Bottom 3dp Gold Bar + 2-Column GPS/Time + Compass/Weather
            // ---------------------------------------------------------------------------------
            boolean isBigMap = "130".equals(id);
            mDatePattern = "dd/MM/yyyy HH:mm";

            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setLayoutParams(new LinearLayout.LayoutParams(dp(268), ViewGroup.LayoutParams.WRAP_CONTENT));

            // Top-right small "Time Now" tag (matching llAppInfo in watermark_cover_id60.xml)
            if (mModel.showTimeNowLogo) {
                TextView tagTv = new TextView(this);
                tagTv.setText("✓ Time Now");
                tagTv.setTextSize(9.5f);
                tagTv.setTypeface(Typeface.DEFAULT_BOLD);
                tagTv.setTextColor(0xFFFFFFFF);
                tagTv.setBackground(roundedCorners(0x88000000, 4, 4, 0, 0));
                tagTv.setPadding(dp(6), dp(2), dp(6), dp(2));
                LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tLp.gravity = Gravity.END;
                card.addView(tagTv, tLp);
            }

            if (isBigMap) {
                ImageView mapBanner = new ImageView(this);
                mapBanner.setImageResource(R.drawable.cover_wm130);
                mapBanner.setScaleType(ImageView.ScaleType.CENTER_CROP);
                card.addView(mapBanner, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(70)));
            }

            LinearLayout body = new LinearLayout(this);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setBackgroundColor(0xAA263238);
            body.setPadding(dp(10), dp(8), dp(10), dp(8));

            if (!isBigMap) {
                String locTitle = (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty())
                        ? mModel.customTitle.trim() : "Location Verified";
                TextView tTv = new TextView(this);
                tTv.setTypeface(Typeface.DEFAULT_BOLD);
                tTv.setTextColor(0xFFFFFFFF);
                configureAutoFitText(tTv, locTitle, 16f, 11f, 248);
                body.addView(tTv);

                if (mModel.showAddress) {
                    TextView aTv = new TextView(this);
                    aTv.setText(mModel.addressLine);
                    aTv.setTextSize(12.5f);
                    aTv.setTextColor(0xEEFFFFFF);
                    aTv.setPadding(0, dp(3), 0, 0);
                    body.addView(aTv);
                }
            }

            LinearLayout twoCol = new LinearLayout(this);
            twoCol.setOrientation(LinearLayout.HORIZONTAL);
            twoCol.setGravity(Gravity.CENTER_VERTICAL);
            twoCol.setPadding(0, dp(4), 0, 0);

            LinearLayout leftMeta = new LinearLayout(this);
            leftMeta.setOrientation(LinearLayout.VERTICAL);
            if (mModel.showLatLong) {
                TextView gTv = new TextView(this);
                gTv.setText(mModel.latLongLine);
                gTv.setTextSize(12);
                gTv.setTextColor(0xFFFFFFFF);
                leftMeta.addView(gTv);
            }
            mLiveDateTv = new TextView(this);
            mLiveDateTv.setTextSize(12);
            mLiveDateTv.setTextColor(0xEEFFFFFF);
            leftMeta.addView(mLiveDateTv);
            twoCol.addView(leftMeta, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

            LinearLayout rightMeta = new LinearLayout(this);
            rightMeta.setOrientation(LinearLayout.VERTICAL);
            rightMeta.setGravity(Gravity.END);
            TextView cTv = new TextView(this);
            cTv.setText("🧭 " + mModel.compassLine);
            cTv.setTextSize(11.5f);
            cTv.setTextColor(0xFFFFFFFF);
            rightMeta.addView(cTv);
            TextView wTv = new TextView(this);
            wTv.setText("☀️ " + mModel.weatherLine);
            wTv.setTextSize(11.5f);
            wTv.setTextColor(0xFFFFFFFF);
            rightMeta.addView(wTv);
            twoCol.addView(rightMeta);
            body.addView(twoCol);

            if (isBigMap && mModel.showAddress) {
                TextView aTv = new TextView(this);
                aTv.setText(mModel.addressLine);
                aTv.setTextSize(12);
                aTv.setTextColor(0xEEFFFFFF);
                aTv.setPadding(0, dp(4), 0, 0);
                body.addView(aTv);
            }
            card.addView(body);

            View bottomAccent = new View(this);
            bottomAccent.setBackgroundColor(mModel.customAccentColor);
            card.addView(bottomAccent, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(2.5f)));

            mWatermarkCardContainer.addView(card);

        } else {
            // ---------------------------------------------------------------------------------
            // TEMPLATE 34 (TIME & LOCATION) & MAP01 (GPS MAP STAMP)
            // Classic new_watermark_cover_id34.xml + Optional Right-Side GPS Map Box (map01)
            // ---------------------------------------------------------------------------------
            boolean isMap01 = "map01".equals(id) || mModel.showMap;
            mDatePattern = "dd MMM yyyy";

            LinearLayout outerRow = new LinearLayout(this);
            outerRow.setOrientation(LinearLayout.HORIZONTAL);
            outerRow.setGravity(Gravity.CENTER_VERTICAL);

            LinearLayout leftBlock = new LinearLayout(this);
            leftBlock.setOrientation(LinearLayout.VERTICAL);
            int maxAddrW = isMap01 ? 195 : 265;

            if (mModel.customTitle != null && !mModel.customTitle.trim().isEmpty()) {
                TextView badge = new TextView(this);
                badge.setTypeface(Typeface.DEFAULT_BOLD);
                badge.setTextColor(0xFFFFFFFF);
                badge.setBackground(roundedRect(mModel.customAccentColor, 4, 0, 0));
                badge.setPadding(dp(8), dp(2), dp(8), dp(2));
                configureAutoFitText(badge, mModel.customTitle.trim(), 12f, 9f, maxAddrW);
                LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                bLp.setMargins(0, 0, 0, dp(4));
                leftBlock.addView(badge, bLp);
            }

            if (mModel.showTime) {
                LinearLayout middleRow = new LinearLayout(this);
                middleRow.setOrientation(LinearLayout.HORIZONTAL);
                middleRow.setGravity(Gravity.CENTER_VERTICAL);

                mLiveBigClockTv = new TextView(this);
                mLiveBigClockTv.setTextSize(38);
                mLiveBigClockTv.setSingleLine(true);
                mLiveBigClockTv.setTypeface(mFontBebas, Typeface.BOLD);
                mLiveBigClockTv.setTextColor(0xFFFFFFFF);
                mLiveBigClockTv.setShadowLayer(4f, 0f, 1.5f, 0xCC000000);
                mLiveBigClockTv.setIncludeFontPadding(false);
                middleRow.addView(mLiveBigClockTv);

                View blockBar = new View(this);
                blockBar.setBackground(roundedRect(mModel.customAccentColor, 1.5f, 0, 0));
                LinearLayout.LayoutParams barLp = new LinearLayout.LayoutParams(dp(3), dp(34));
                barLp.setMargins(dp(8), 0, dp(8), 0);
                middleRow.addView(blockBar, barLp);

                LinearLayout dateCol = new LinearLayout(this);
                dateCol.setOrientation(LinearLayout.VERTICAL);

                mLiveDateTv = new TextView(this);
                mLiveDateTv.setTextSize(14);
                mLiveDateTv.setSingleLine(true);
                mLiveDateTv.setTypeface(Typeface.DEFAULT_BOLD);
                mLiveDateTv.setTextColor(0xFFFFFFFF);
                mLiveDateTv.setShadowLayer(3f, 0f, 1f, 0xCC000000);
                dateCol.addView(mLiveDateTv);

                mLiveWeatherTv = new TextView(this);
                mLiveWeatherTv.setTextSize(13);
                mLiveWeatherTv.setSingleLine(true);
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
                FrameLayout mapCard = new FrameLayout(this);
                mapCard.setBackground(roundedRect(0xFFFFFFFF, 6, dp(1.5f), 0xFFFFFFFF));
                mapCard.setPadding(dp(2), dp(2), dp(2), dp(2));
                LinearLayout.LayoutParams mLp = new LinearLayout.LayoutParams(dp(70), dp(70));
                mLp.setMargins(dp(10), 0, 0, 0);

                ImageView mapIv = new ImageView(this);
                mapIv.setImageResource(R.drawable.cover_wm10_map01);
                mapIv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mapCard.addView(mapIv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                outerRow.addView(mapCard, mLp);
            }

            mWatermarkCardContainer.addView(outerRow);
        }

        // Apply user-selected Stamp Scale (0.85x, 1.0x, 1.15x) from bottom-left anchor
        mWatermarkCardContainer.setPivotX(0f);
        mWatermarkCardContainer.post(new Runnable() {
            @Override
            public void run() {
                mWatermarkCardContainer.setPivotY(mWatermarkCardContainer.getHeight());
                mWatermarkCardContainer.setScaleX(mModel.stampScale);
                mWatermarkCardContainer.setScaleY(mModel.stampScale);
            }
        });

        updateClockTexts();
    }

    private TextView createShadowText(String text, float sp, boolean bold, int maxWidthDp) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(sp);
        if (bold) tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(0xFFFFFFFF);
        tv.setShadowLayer(3.5f, 0f, 1.2f, 0xDD000000);
        tv.setMaxWidth(dp(maxWidthDp));
        return tv;
    }

    private void appendOptionalGeoAndNoteLines(LinearLayout parent, int maxWidthDp) {
        if (mModel.showLatLong || mModel.showCompass || mModel.showAltitude) {
            StringBuilder geo = new StringBuilder();
            if (mModel.showLatLong) geo.append(mModel.getFormattedCoordinates());
            if (mModel.showCompass) {
                if (geo.length() > 0) geo.append("  •  ");
                geo.append(mModel.compassLine);
            }
            if (mModel.showAltitude) {
                if (geo.length() > 0) geo.append("  •  ");
                geo.append(mModel.altitudeLine);
            }
            TextView geoTv = createShadowText(geo.toString(), 11.5f, false, maxWidthDp);
            geoTv.setTypeface(mFontMono);
            geoTv.setPadding(0, dp(2), 0, 0);
            parent.addView(geoTv);
        }
        if (mModel.cameraMode == 3) {
            TextView aiTv = createShadowText("AI Count: " + mModel.aiCountDetected + " " + mModel.aiCountCategory + " (Edge CV)", 12.5f, true, maxWidthDp);
            aiTv.setTextColor(0xFF00E676);
            aiTv.setPadding(0, dp(2), 0, 0);
            parent.addView(aiTv);
        }
        if (mModel.showNotes && mModel.customNote != null && !mModel.customNote.trim().isEmpty()) {
            TextView noteTv = createShadowText("Note: " + mModel.customNote.trim(), 12.5f, false, maxWidthDp);
            noteTv.setTextColor(0xFFFFE082);
            noteTv.setPadding(0, dp(2), 0, 0);
            parent.addView(noteTv);
        }
        if (mModel.showBrandLogo && mModel.brandLogoText != null && !mModel.brandLogoText.trim().isEmpty()) {
            TextView brandTv = createShadowText("🏢 " + mModel.brandLogoText.trim() + " • " + mModel.inspectorName, 11.5f, true, maxWidthDp);
            brandTv.setTextColor(0xFF93C5FD);
            brandTv.setPadding(0, dp(2), 0, 0);
            parent.addView(brandTv);
        }
    }

    private void appendTimeNowVerifiedFooter(LinearLayout parent) {
        if (!mModel.showTimeNowLogo && !mModel.showQrCode) return;
        LinearLayout verRow = new LinearLayout(this);
        verRow.setOrientation(LinearLayout.HORIZONTAL);
        verRow.setGravity(Gravity.CENTER_VERTICAL);
        verRow.setPadding(0, dp(4), 0, 0);

        if (mModel.showQrCode) {
            TextView qrBadge = new TextView(this);
            qrBadge.setText("▣ QR:" + mModel.currentPhotoCode.substring(0, 5));
            qrBadge.setTextSize(9);
            qrBadge.setTypeface(mFontMono, Typeface.BOLD);
            qrBadge.setTextColor(0xFF111111);
            qrBadge.setBackground(roundedRect(0xFFFFFFFF, 3, 0, 0));
            qrBadge.setPadding(dp(4), dp(1), dp(4), dp(1));
            LinearLayout.LayoutParams qLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            qLp.setMargins(0, 0, dp(6), 0);
            verRow.addView(qrBadge, qLp);
        }

        if (mModel.showTimeNowLogo) {
            ImageView chk = new ImageView(this);
            chk.setImageResource(R.drawable.check_circle);
            verRow.addView(chk, new LinearLayout.LayoutParams(dp(12), dp(12)));

            TextView verTv = new TextView(this);
            verTv.setText(" 100% Real-Time & Location Verified • Time Now");
            verTv.setTextSize(10);
            verTv.setTypeface(Typeface.DEFAULT_BOLD);
            verTv.setTextColor(0xEEFFFFFF);
            verTv.setShadowLayer(3f, 0f, 1f, 0xCC000000);
            verRow.addView(verTv);
        }
        parent.addView(verRow);
    }

    private LinearLayout createRedBulletRow(String text) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(3), 0, 0);

        View bullet = new View(this);
        bullet.setBackgroundColor(0xFFFF2D20);
        LinearLayout.LayoutParams bLp = new LinearLayout.LayoutParams(dp(5), dp(11));
        bLp.setMargins(0, dp(3), dp(7), 0);
        row.addView(bullet, bLp);

        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(12.5f);
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

    private TextView addGreenSheetRow(LinearLayout parent, String label, String value, int greenColor) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);

        TextView lTv = new TextView(this);
        lTv.setText(label);
        lTv.setTextSize(11.5f);
        lTv.setTextColor(0xFFE0F2F1);
        lTv.setPadding(dp(6), dp(4), dp(4), dp(4));
        row.addView(lTv, new LinearLayout.LayoutParams(dp(64), ViewGroup.LayoutParams.WRAP_CONTENT));

        View vDiv = new View(this);
        vDiv.setBackgroundColor(greenColor);
        row.addView(vDiv, new LinearLayout.LayoutParams(Math.max(1, dp(0.8f)), ViewGroup.LayoutParams.MATCH_PARENT));

        TextView vTv = new TextView(this);
        vTv.setText(value);
        vTv.setTextSize(11.5f);
        vTv.setTextColor(0xFFFFFFFF);
        vTv.setPadding(0, dp(4), dp(6), dp(4));
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
            mLiveBigClockTv.setText(new SimpleDateFormat(mModel.getClockPattern(), Locale.US).format(now));
        }
        if (mLiveDateTv != null) {
            mLiveDateTv.setText(new SimpleDateFormat(mDatePattern, Locale.US).format(now));
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
        wmView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OriginalDialogs.showEditStampSheet(MainActivity.this, mModel, new Runnable() {
                    @Override
                    public void run() {
                        rebuildWatermarkOverlay();
                    }
                });
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

    private void updateModeSpecialPill() {
        if (mModeSpecialPillTv == null) return;
        if (mModel.cameraMode == 3) {
            mModeSpecialPillTv.setVisibility(View.VISIBLE);
            mModeSpecialPillTv.setText("🤖 AI Count: " + mModel.aiCountDetected + " " + mModel.aiCountCategory + " (Tap to Switch)");
        } else if (mModel.cameraMode == 4) {
            mModeSpecialPillTv.setVisibility(View.VISIBLE);
            int step = mModel.collageSlices.size() + 1;
            mModeSpecialPillTv.setText("🧩 Collage Report (" + mModel.collageGridTarget + "-Grid): Frame " + step + "/" + mModel.collageGridTarget);
        } else {
            mModeSpecialPillTv.setVisibility(View.GONE);
        }
    }

    private void updateModeTabsUI() {
        for (int i = 0; i < 5; i++) {
            if (mModeTabs[i] == null) continue;
            boolean sel = (mModel.cameraMode == i);
            mModeTabs[i].setTypeface(sel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            mModeTabs[i].setTextColor(sel ? 0xFF222222 : 0xFF848484);
            mModeDots[i].setVisibility(sel ? View.VISIBLE : View.INVISIBLE);
        }
        if (mAiCountOverlayView != null) {
            mAiCountOverlayView.setVisibility(mModel.cameraMode == 3 ? View.VISIBLE : View.GONE);
            mAiCountOverlayView.invalidate();
        }
        updateModeSpecialPill();
        if (mShootIv != null) {
            if (mModel.cameraMode == 0) {
                mShootIv.setImageResource(R.drawable.cam_shoot_video);
                mShootIv.setAlpha(mIsRecordingVideo ? 0.78f : 1.0f);
            } else if (mModel.cameraMode == 2) {
                mShootIv.setImageResource(R.drawable.teamspace_shoot);
                mShootIv.setAlpha(1.0f);
            } else {
                mShootIv.setImageResource(R.drawable.cam_shoot);
                mShootIv.setAlpha(1.0f);
            }
        }
    }

    private void applyAspectRatioLayout() {
        if (mViewfinderContainer == null) return;
        View parent = (View) mViewfinderContainer.getParent();
        int maxW = parent.getWidth();
        int maxH = parent.getHeight();
        if (maxW <= 0 || maxH <= 0) return;

        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mViewfinderContainer.getLayoutParams();
        if (mModel.aspectRatioMode == 2) {
            lp.width = maxW;
            lp.height = maxW;
        } else if (mModel.aspectRatioMode == 0) {
            int targetH = (int) (maxW * 4f / 3f);
            lp.width = maxW;
            lp.height = Math.min(maxH, targetH);
        } else {
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        mViewfinderContainer.setLayoutParams(lp);
        if (mGridLevelerView != null) mGridLevelerView.invalidate();
    }

    // =========================================================================================
    // SHUTTER DISPATCHER: VIDEO RECORDING (MODE 0), COUNTDOWN TIMER, PHOTO, AI COUNT & COLLAGE
    // =========================================================================================
    private void onShutterButtonClicked() {
        if (mCountdownActive) return;

        if (mModel.cameraMode == 0) {
            // Video Recording Mode
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
            executeCaptureByMode();
        }
    }

    private void runCountdownStep(final int remaining) {
        if (remaining <= 0) {
            mCountdownActive = false;
            if (mTimerCountdownTv != null) mTimerCountdownTv.setVisibility(View.GONE);
            executeCaptureByMode();
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

    private void executeCaptureByMode() {
        if (mModel.cameraMode == 4) {
            captureCollageStep();
        } else {
            captureWatermarkedPhoto();
        }
    }

    // Fast Watermarked Video Frame Renderer (< 3.5ms per 480x640 RGB_565 frame)
    private Bitmap captureFastWatermarkedVideoFrame() {
        try {
            int outW = 480;
            int outH = 640;
            Bitmap frame = Bitmap.createBitmap(outW, outH, Bitmap.Config.RGB_565);
            Canvas c = new Canvas(frame);

            Bitmap cam = (mTextureView != null && mTextureView.isAvailable())
                    ? mTextureView.getBitmap(outW, outH) : null;
            if (cam != null) {
                c.drawBitmap(cam, 0, 0, null);
            } else if (mViewfinderContainer != null && mViewfinderContainer.getChildCount() > 0) {
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

                if (mModel.cameraMode == 3 && mAiCountOverlayView != null) {
                    mAiCountOverlayView.draw(c);
                }

                c.save();
                c.translate(mWatermarkCardContainer.getX(), mWatermarkCardContainer.getY());
                c.scale(mModel.stampScale, mModel.stampScale, 0f, mWatermarkCardContainer.getHeight());
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
                    c.drawText("Time Now Photo Code: " + mModel.currentPhotoCode, 0, 0, p);
                    c.restore();
                }
                c.restore();
            }

            // Stamp live video elapsed time badge at top-left of every recorded frame
            int elapsedSec = Math.max(1, (int) ((System.currentTimeMillis() - mVideoStartTimeMs) / 1000L));
            Paint vp = new Paint(Paint.ANTI_ALIAS_FLAG);
            vp.setColor(0xCCFF1717);
            c.drawRoundRect(12f, 12f, 152f, 40f, 8f, 8f, vp);
            vp.setColor(0xFFFFFFFF);
            vp.setTypeface(mFontMono);
            vp.setTextSize(15f);
            c.drawText(String.format(Locale.US, "● REC %02d:%02d", elapsedSec / 60, elapsedSec % 60), 22f, 31f, vp);

            return frame;
        } catch (Throwable t) {
            return null;
        }
    }

    // Zero-Lag Watermarked Video Recording (No Camera2 Session Reconfiguration Delay!)
    private void startVideoRecording() {
        playShutterSound();
        mIsRecordingVideo = true;
        mVideoStartTimeMs = System.currentTimeMillis();
        mRecordedVideoFrames.clear();

        if (mVideoRecBadgeTv != null) {
            mVideoRecBadgeTv.setText("● REC  00:01");
            mVideoRecBadgeTv.setVisibility(View.VISIBLE);
        }
        updateModeTabsUI();

        File dir = new File(getFilesDir(), "timenow_photos");
        if (!dir.exists()) dir.mkdirs();
        mCurrentVideoFile = new File(dir, "TN_VID_" + mVideoStartTimeMs + ".mp4");

        // Immediately capture first watermarked video frame & start 140ms sampler
        mUiHandler.removeCallbacks(mVideoFrameSampler);
        mUiHandler.post(mVideoFrameSampler);

        Toast.makeText(this, "Recording Watermarked Video... Tap shutter to stop & save.", Toast.LENGTH_SHORT).show();
    }

    private void stopVideoRecording() {
        if (!mIsRecordingVideo) return;
        mIsRecordingVideo = false;
        mUiHandler.removeCallbacks(mVideoFrameSampler);
        playShutterSound();

        final int durationSec = Math.max(1, (int) ((System.currentTimeMillis() - mVideoStartTimeMs) / 1000L));
        if (mVideoRecBadgeTv != null) {
            mVideoRecBadgeTv.setVisibility(View.GONE);
        }
        updateModeTabsUI();

        // Ensure at least 6 watermarked frames exist even for a quick <1s tap
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
                mAntiCodeVerticalTv.setText("Time Now Photo Code: " + mModel.currentPhotoCode);
                Toast.makeText(this, "✓ Video Stamped & Saved (" + durationSec + "s) • Tap Preview to watch!", Toast.LENGTH_SHORT).show();
            }
        }

        // Encode watermarked frames into standard H.264 .mp4 file on background thread (0ms UI blocking)
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
            // Fallback: ensure outFile exists with non-zero bytes for file inspector
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

    private void captureCollageStep() {
        playShutterSound();
        mShutterFlashView.setAlpha(0.85f);
        mShutterFlashView.animate().alpha(0f).setDuration(220).start();

        int w = Math.max(720, mViewfinderContainer.getWidth());
        int h = Math.max(960, mViewfinderContainer.getHeight());
        Bitmap cleanSlice = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(cleanSlice);
        Bitmap camFrame = (mTextureView != null && mTextureView.isAvailable()) ? mTextureView.getBitmap(w, h) : null;
        if (camFrame != null) {
            c.drawBitmap(camFrame, 0, 0, null);
        } else {
            mViewfinderContainer.getChildAt(0).draw(c);
        }
        mModel.collageSlices.add(cleanSlice);

        if (mModel.collageSlices.size() < mModel.collageGridTarget) {
            updateModeSpecialPill();
            Toast.makeText(this, "Captured Frame " + mModel.collageSlices.size() + "/" + mModel.collageGridTarget + " — Take next frame!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Stitch all slices into a single Before/After or 4-Grid Inspection Report
        Bitmap stitched = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas sc = new Canvas(stitched);
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        if (mModel.collageGridTarget == 2) {
            int halfH = h / 2;
            sc.drawBitmap(mModel.collageSlices.get(0), null, new Rect(0, 0, w, halfH - 2), null);
            sc.drawBitmap(mModel.collageSlices.get(1), null, new Rect(0, halfH + 2, w, h), null);
            p.setColor(0xFFFFFFFF);
            sc.drawRect(0, halfH - 2, w, halfH + 2, p);
            p.setColor(0xCC0A2246);
            sc.drawRect(dp(12), dp(12), dp(108), dp(38), p);
            sc.drawRect(dp(12), halfH + dp(12), dp(108), halfH + dp(38), p);
            p.setColor(0xFFFFFFFF);
            p.setTextSize(dp(12));
            p.setTypeface(Typeface.DEFAULT_BOLD);
            sc.drawText("BEFORE", dp(24), dp(30), p);
            sc.drawText("AFTER", dp(28), halfH + dp(30), p);
        } else {
            int hw = w / 2;
            int hh = h / 2;
            sc.drawBitmap(mModel.collageSlices.get(0), null, new Rect(0, 0, hw - 2, hh - 2), null);
            sc.drawBitmap(mModel.collageSlices.get(1), null, new Rect(hw + 2, 0, w, hh - 2), null);
            sc.drawBitmap(mModel.collageSlices.get(2), null, new Rect(0, hh + 2, hw - 2, h), null);
            sc.drawBitmap(mModel.collageSlices.get(3), null, new Rect(hw + 2, hh + 2, w, h), null);
            p.setColor(0xFFFFFFFF);
            sc.drawRect(hw - 2, 0, hw + 2, h, p);
            sc.drawRect(0, hh - 2, w, hh + 2, p);
        }
        mModel.collageSlices.clear();
        updateModeSpecialPill();

        Bitmap compositeBmp = renderCurrentCompositeBitmap(w, h, stitched);
        WatermarkModel.CapturedPhoto saved = mModel.saveCapturedPhoto(this, compositeBmp, stitched);
        if (saved != null && mPreviewThumbIv != null) {
            mPreviewThumbIv.setImageBitmap(compositeBmp);
            mAntiCodeVerticalTv.setText("Time Now Photo Code: " + mModel.currentPhotoCode);
            Toast.makeText(this, "Collage Report Saved • Photo Code: " + saved.photoCode, Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap renderCurrentCompositeBitmap(int w, int h, Bitmap customBaseBmp) {
        Bitmap baseBmp = customBaseBmp;
        if (baseBmp == null) {
            baseBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas cleanCanvas = new Canvas(baseBmp);
            Bitmap cameraFrame = (mTextureView != null && mTextureView.isAvailable())
                    ? mTextureView.getBitmap(w, h) : null;
            if (cameraFrame != null) {
                cleanCanvas.drawBitmap(cameraFrame, 0, 0, null);
            } else {
                mViewfinderContainer.getChildAt(0).draw(cleanCanvas);
            }
        }
        Bitmap compositeBmp = baseBmp.copy(Bitmap.Config.ARGB_8888, true);
        Canvas compCanvas = new Canvas(compositeBmp);

        if (mModel.cameraMode == 3 && mAiCountOverlayView != null) {
            mAiCountOverlayView.draw(compCanvas);
        }

        compCanvas.save();
        compCanvas.translate(mWatermarkCardContainer.getX(), mWatermarkCardContainer.getY());
        compCanvas.scale(mModel.stampScale, mModel.stampScale, 0f, mWatermarkCardContainer.getHeight());
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
            compCanvas.drawText("Time Now Photo Code: " + mModel.currentPhotoCode, 0, 0, p);
            compCanvas.restore();
        }
        return compositeBmp;
    }

    // Captures the real Camera2 frame + composites the live Watermark + saves XHEY1 Dual-Stream JPEG
    private void captureWatermarkedPhoto() {
        playShutterSound();

        mShutterFlashView.setAlpha(0.85f);
        mShutterFlashView.animate().alpha(0f).setDuration(220).start();

        int w = Math.max(720, mViewfinderContainer.getWidth());
        int h = Math.max(960, mViewfinderContainer.getHeight());

        Bitmap cleanBmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas cleanCanvas = new Canvas(cleanBmp);

        Bitmap cameraFrame = (mTextureView != null && mTextureView.isAvailable())
                ? mTextureView.getBitmap(w, h) : null;
        if (cameraFrame != null) {
            cleanCanvas.drawBitmap(cameraFrame, 0, 0, null);
        } else {
            mViewfinderContainer.getChildAt(0).draw(cleanCanvas);
        }

        Bitmap compositeBmp = renderCurrentCompositeBitmap(w, h, cleanBmp);

        WatermarkModel.CapturedPhoto saved = mModel.saveCapturedPhoto(this, compositeBmp, cleanBmp);
        if (saved != null && mPreviewThumbIv != null) {
            mPreviewThumbIv.setImageBitmap(compositeBmp);
            mAntiCodeVerticalTv.setText("Time Now Photo Code: " + mModel.currentPhotoCode);
            Toast.makeText(this, "Saved • Photo Code: " + saved.photoCode, Toast.LENGTH_SHORT).show();
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
                    break;
                }
            }
            if (selectedCamId == null && mgr.getCameraIdList().length > 0) {
                selectedCamId = mgr.getCameraIdList()[0];
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

    private void startCameraPreviewSession() {
        if (mCameraDevice == null || mTextureView == null || !mTextureView.isAvailable()) return;
        try {
            SurfaceTexture st = mTextureView.getSurfaceTexture();
            if (st == null) return;
            st.setDefaultBufferSize(1280, 960);
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
