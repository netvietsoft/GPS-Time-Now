package com.nextai.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OriginalDialogs {

    private static int dp(Context ctx, float val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, ctx.getResources().getDisplayMetrics());
    }

    private static GradientDrawable rounded(Context ctx, int fillColor, float radiusDp, int strokePx, int strokeColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        gd.setCornerRadius(dp(ctx, radiusDp));
        if (strokePx > 0) gd.setStroke(strokePx, strokeColor);
        return gd;
    }

    private static GradientDrawable topRounded(Context ctx, int fillColor, float radiusDp) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        float r = dp(ctx, radiusDp);
        gd.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return gd;
    }

    // Ensures full-screen Dialogs on Android 15/16 (targetSdkVersion=36) sit cleanly below the status bar
    private static void applyDialogSystemBarInsets(final Activity act, final Dialog dlg, final View root,
                                                   final int statusBarColor, final boolean lightBars) {
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(statusBarColor));
            w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_FULLSCREEN);
            w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            if (android.os.Build.VERSION.SDK_INT >= 23) {
                w.setStatusBarColor(statusBarColor);
                w.setNavigationBarColor(0xFFFFFFFF);
                int flags = lightBars ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0;
                if (android.os.Build.VERSION.SDK_INT >= 26) {
                    flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                }
                w.getDecorView().setSystemUiVisibility(flags);
            }
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        final int baseLeft = root.getPaddingLeft();
        final int baseTop = root.getPaddingTop();
        final int baseRight = root.getPaddingRight();
        final int baseBottom = root.getPaddingBottom();
        root.post(new Runnable() {
            @Override
            public void run() {
                int[] loc = new int[2];
                root.getLocationOnScreen(loc);
                int sbH = 0;
                int resId = act.getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resId > 0) sbH = act.getResources().getDimensionPixelSize(resId);
                if (sbH <= 0) sbH = dp(act, 28);
                int extraTop = (loc[1] < sbH) ? (sbH - loc[1]) : 0;
                if (extraTop > 0) {
                    root.setPadding(baseLeft, baseTop + extraTop, baseRight, baseBottom);
                }
            }
        });
    }

    // Helper to create a clean iOS/Timemark-style Switch view matching @drawable/switch_ios_track
    private static View createOriginalSwitch(final Context ctx, boolean checked) {
        FrameLayout track = new FrameLayout(ctx);
        int w = dp(ctx, 44);
        int h = dp(ctx, 24);
        track.setLayoutParams(new LinearLayout.LayoutParams(w, h));
        track.setBackground(rounded(ctx, checked ? 0xFF0093FF : 0xFFDCDCDC, 12, 0, 0));

        View thumb = new View(ctx);
        thumb.setBackground(rounded(ctx, 0xFFFFFFFF, 10, 0, 0));
        FrameLayout.LayoutParams tLp = new FrameLayout.LayoutParams(dp(ctx, 20), dp(ctx, 20));
        tLp.gravity = Gravity.CENTER_VERTICAL | (checked ? Gravity.END : Gravity.START);
        tLp.setMargins(dp(ctx, 2), 0, dp(ctx, 2), 0);
        thumb.setLayoutParams(tLp);
        track.addView(thumb);
        return track;
    }

    // =========================================================================================
    // 1. TEMPLATES BOTTOM SHEET (1:1 watermark_preview_bottom_sheet.xml + layout_cloud_wm_item.xml)
    // =========================================================================================
    public static void showTemplatesSheet(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(act);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setBackground(topRounded(act, 0xFFF2F1F6, 12));

        // 1A. Top Tab Bar (watermark_preview_layout.xml: #F8F8FA, height 48dp, category tabs + closeIv)
        LinearLayout headerBar = new LinearLayout(act);
        headerBar.setOrientation(LinearLayout.HORIZONTAL);
        headerBar.setGravity(Gravity.CENTER_VERTICAL);
        headerBar.setBackground(topRounded(act, 0xFFF8F8FA, 12));
        headerBar.setPadding(dp(act, 12), 0, dp(act, 12), 0);
        headerBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 48)));

        HorizontalScrollView tabScroll = new HorizontalScrollView(act);
        tabScroll.setHorizontalScrollBarEnabled(false);
        final LinearLayout tabRow = new LinearLayout(act);
        tabRow.setOrientation(LinearLayout.HORIZONTAL);
        tabRow.setGravity(Gravity.CENTER_VERTICAL);

        final String[] categories = new String[]{
            "All", "Time & Location", "Attendance", "Construction", "Service & Delivery", "Cleaning & Security"
        };
        final String[] selectedCategory = new String[]{"All"};
        final String[] currentSearchQuery = new String[]{""};
        final LinearLayout gridContainer = new LinearLayout(act);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 18));

        final Runnable[] rebuildGridRef = new Runnable[1];
        final Runnable rebuildTabs = new Runnable() {
            @Override
            public void run() {
                tabRow.removeAllViews();
                for (int i = 0; i < categories.length; i++) {
                    final String cat = categories[i];
                    boolean sel = cat.equals(selectedCategory[0]);

                    LinearLayout tabItem = new LinearLayout(act);
                    tabItem.setOrientation(LinearLayout.VERTICAL);
                    tabItem.setGravity(Gravity.CENTER);
                    tabItem.setPadding(dp(act, 10), dp(act, 6), dp(act, 10), 0);

                    TextView tv = new TextView(act);
                    tv.setText(cat);
                    tv.setTextSize(14);
                    tv.setSingleLine(true);
                    tv.setTypeface(sel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    tv.setTextColor(sel ? 0xFF222222 : 0xFF757575);
                    tabItem.addView(tv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                    View indicator = new View(act);
                    indicator.setBackground(rounded(act, sel ? 0xFF0093FF : 0x00000000, 1.5f, 0, 0));
                    LinearLayout.LayoutParams indLp = new LinearLayout.LayoutParams(dp(act, 20), dp(act, 3));
                    indLp.setMargins(0, dp(act, 6), 0, 0);
                    tabItem.addView(indicator, indLp);

                    tabItem.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedCategory[0] = cat;
                            run();
                            if (rebuildGridRef[0] != null) rebuildGridRef[0].run();
                        }
                    });
                    tabRow.addView(tabItem);
                }
            }
        };
        rebuildTabs.run();
        tabScroll.addView(tabRow);
        headerBar.addView(tabScroll, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.btn_close_on_light);
        LinearLayout.LayoutParams closeLp = new LinearLayout.LayoutParams(dp(act, 26), dp(act, 26));
        closeLp.setMargins(dp(act, 8), 0, 0, 0);
        closeIv.setLayoutParams(closeLp);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        headerBar.addView(closeIv);
        sheet.addView(headerBar);

        View headerDiv = new View(act);
        headerDiv.setBackgroundColor(0xFFE5E5EA);
        sheet.addView(headerDiv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // 1B. Search Box (fragment_universal_watermark_list_global.xml:llSearchLayout)
        LinearLayout searchBox = new LinearLayout(act);
        searchBox.setOrientation(LinearLayout.HORIZONTAL);
        searchBox.setGravity(Gravity.CENTER_VERTICAL);
        searchBox.setBackground(rounded(act, 0xFFFFFFFF, 8, 0, 0));
        searchBox.setPadding(dp(act, 10), 0, dp(act, 10), 0);
        LinearLayout.LayoutParams sbLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 36));
        sbLp.setMargins(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 4));
        searchBox.setLayoutParams(sbLp);

        ImageView sIcon = new ImageView(act);
        sIcon.setImageResource(R.drawable.tool_border_search_weak);
        searchBox.addView(sIcon, new LinearLayout.LayoutParams(dp(act, 16), dp(act, 16)));

        final EditText searchEt = new EditText(act);
        searchEt.setHint("Search watermark...");
        searchEt.setHintTextColor(0xFF999999);
        searchEt.setTextSize(14);
        searchEt.setTextColor(0xFF222222);
        searchEt.setSingleLine(true);
        searchEt.setBackground(null);
        searchEt.setPadding(dp(act, 8), 0, dp(act, 8), 0);
        searchEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearchQuery[0] = s != null ? s.toString().trim().toLowerCase(Locale.US) : "";
                if (rebuildGridRef[0] != null) rebuildGridRef[0].run();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
        searchBox.addView(searchEt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        sheet.addView(searchBox);

        // 1C. 2-Column Watermark Grid (layout_cloud_wm_item.xml)
        ScrollView scroll = new ScrollView(act);
        rebuildGridRef[0] = new Runnable() {
            @Override
            public void run() {
                gridContainer.removeAllViews();
                List<WatermarkModel.TemplateInfo> filtered = new ArrayList<WatermarkModel.TemplateInfo>();
                for (int i = 0; i < model.templates.size(); i++) {
                    WatermarkModel.TemplateInfo t = model.templates.get(i);
                    boolean catMatch = "All".equals(selectedCategory[0]) || t.category.equalsIgnoreCase(selectedCategory[0]);
                    boolean qMatch = currentSearchQuery[0].isEmpty()
                            || t.name.toLowerCase(Locale.US).contains(currentSearchQuery[0])
                            || t.category.toLowerCase(Locale.US).contains(currentSearchQuery[0]);
                    if (catMatch && qMatch) {
                        filtered.add(t);
                    }
                }

                for (int i = 0; i < filtered.size(); i += 2) {
                    LinearLayout row = new LinearLayout(act);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    row.setWeightSum(2f);
                    LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    rowLp.setMargins(0, dp(act, 4), 0, dp(act, 4));
                    row.setLayoutParams(rowLp);

                    for (int col = 0; col < 2; col++) {
                        int idx = i + col;
                        if (idx >= filtered.size()) {
                            View spacer = new View(act);
                            row.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));
                            continue;
                        }
                        final WatermarkModel.TemplateInfo t = filtered.get(idx);
                        final boolean isActive = (model.activeTemplate != null && model.activeTemplate.id.equals(t.id));

                        // Exact layout_cloud_wm_item.xml card
                        LinearLayout card = new LinearLayout(act);
                        card.setOrientation(LinearLayout.VERTICAL);
                        LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        cardLp.setMargins(dp(act, 4), 0, dp(act, 4), 0);
                        card.setLayoutParams(cardLp);

                        // Top Cover Frame (bg_watermark_cover.xml #7F8690 + full uncropped stamp preview)
                        FrameLayout coverFrame = new FrameLayout(act);
                        coverFrame.setBackground(rounded(act, 0xFF7F8690, 6, isActive ? dp(act, 2) : 0, 0xFF0093FF));
                        coverFrame.setMinimumHeight(dp(act, 100));
                        boolean isCustomCard = "120".equals(t.id) || "20".equals(t.id) || "100".equals(t.id)
                                || "43".equals(t.id) || "60".equals(t.id) || "130".equals(t.id);
                        int pad = isCustomCard ? dp(act, 6) : dp(act, 2);
                        coverFrame.setPadding(pad, pad, pad, pad);
                        coverFrame.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        View fullStampPreview = buildFullTemplatePreviewCard(act, t);
                        FrameLayout.LayoutParams fpLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        fpLp.gravity = Gravity.CENTER_VERTICAL;
                        coverFrame.addView(fullStampPreview, fpLp);

                        card.addView(coverFrame);

                        if (isActive) {
                            // Action row placed cleanly below coverFrame so stamp content is never covered or cut off
                            LinearLayout btnsWrap = new LinearLayout(act);
                            btnsWrap.setOrientation(LinearLayout.HORIZONTAL);
                            btnsWrap.setGravity(Gravity.CENTER);
                            LinearLayout.LayoutParams bwLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 28));
                            bwLp.setMargins(0, dp(act, 4), 0, 0);
                            btnsWrap.setLayoutParams(bwLp);

                            // Share button (#1DC560)
                            LinearLayout shareBtn = new LinearLayout(act);
                            shareBtn.setOrientation(LinearLayout.HORIZONTAL);
                            shareBtn.setGravity(Gravity.CENTER);
                            shareBtn.setBackground(rounded(act, 0xFF1DC560, 4, 0, 0));
                            LinearLayout.LayoutParams sBtnLp = new LinearLayout.LayoutParams(0, dp(act, 26), 1f);
                            sBtnLp.setMargins(0, 0, dp(act, 3), 0);
                            shareBtn.setLayoutParams(sBtnLp);

                            ImageView sIv = new ImageView(act);
                            sIv.setImageResource(R.drawable.ic_share_ltr);
                            sIv.setColorFilter(0xFFFFFFFF);
                            shareBtn.addView(sIv, new LinearLayout.LayoutParams(dp(act, 12), dp(act, 12)));

                            TextView sTv = new TextView(act);
                            sTv.setText(" Share");
                            sTv.setTextSize(11.5f);
                            sTv.setTypeface(Typeface.DEFAULT_BOLD);
                            sTv.setTextColor(0xFFFFFFFF);
                            shareBtn.addView(sTv);
                            shareBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    ClipboardManager cm = (ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);
                                    if (cm != null) {
                                        cm.setPrimaryClip(ClipData.newPlainText("Watermark", t.name));
                                    }
                                    Toast.makeText(act, "Shared template: " + t.name, Toast.LENGTH_SHORT).show();
                                }
                            });
                            btnsWrap.addView(shareBtn);

                            // Edit button (#0093FF)
                            LinearLayout editBtn = new LinearLayout(act);
                            editBtn.setOrientation(LinearLayout.HORIZONTAL);
                            editBtn.setGravity(Gravity.CENTER);
                            editBtn.setBackground(rounded(act, 0xFF0093FF, 4, 0, 0));
                            LinearLayout.LayoutParams eBtnLp = new LinearLayout.LayoutParams(0, dp(act, 26), 1f);
                            eBtnLp.setMargins(dp(act, 3), 0, 0, 0);
                            editBtn.setLayoutParams(eBtnLp);

                            ImageView eIv = new ImageView(act);
                            eIv.setImageResource(R.drawable.tool_border_edit);
                            eIv.setColorFilter(0xFFFFFFFF);
                            editBtn.addView(eIv, new LinearLayout.LayoutParams(dp(act, 12), dp(act, 12)));

                            TextView eTv = new TextView(act);
                            eTv.setText(" Edit");
                            eTv.setTextSize(11.5f);
                            eTv.setTypeface(Typeface.DEFAULT_BOLD);
                            eTv.setTextColor(0xFFFFFFFF);
                            editBtn.addView(eTv);
                            editBtn.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dlg.dismiss();
                                    showEditStampSheet(act, model, onUpdate);
                                }
                            });
                            btnsWrap.addView(editBtn);

                            card.addView(btnsWrap);
                        }

                        // Bottom Name (atvWMName: 30dp height, 14sp #333333 centered)
                        TextView nameTv = new TextView(act);
                        nameTv.setText(t.name);
                        nameTv.setTextSize(14);
                        nameTv.setSingleLine(true);
                        nameTv.setEllipsize(TextUtils.TruncateAt.END);
                        nameTv.setGravity(Gravity.CENTER);
                        nameTv.setTextColor(isActive ? 0xFF0093FF : 0xFF333333);
                        nameTv.setTypeface(isActive ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                        nameTv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 30)));
                        card.addView(nameTv);

                        card.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                model.selectTemplate(t);
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                run();
                            }
                        });

                        row.addView(card);
                    }
                    gridContainer.addView(row);
                }
            }
        };
        rebuildGridRef[0].run();

        scroll.addView(gridContainer);
        sheet.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 380)));

        dlg.setContentView(sheet);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
            w.setDimAmount(0.15f);
        }
        dlg.show();
    }

    // =========================================================================================
    // 2. EDIT STAMP BOTTOM SHEET (1:1 activity_new_edit.xml + layout_watermark_edit_header_global.xml
    //    + item_edit_item_single_line.xml + dialog_edit_input_global.xml)
    // =========================================================================================
    public static void showEditStampSheet(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(topRounded(act, 0xFFFAFAFA, 12));

        // -------------------------------------------------------------------------------------
        // 2A. Header (layout_watermark_edit_header_global.xml: height 60dp, #FAFAFA)
        //     Left: "Edit Stamp" (20sp bold #222222)
        //     Right: ll_share (#F0F0F0 pill "Share") + iv_close (btn_close_on_light)
        // -------------------------------------------------------------------------------------
        RelativeLayout header = new RelativeLayout(act);
        header.setBackground(topRounded(act, 0xFFFAFAFA, 12));
        header.setPadding(dp(act, 16), 0, dp(act, 16), 0);
        header.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 58)));

        TextView titleTv = new TextView(act);
        titleTv.setText("Edit Stamp");
        titleTv.setTextSize(20);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        RelativeLayout.LayoutParams tLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        tLp.addRule(RelativeLayout.CENTER_VERTICAL);
        header.addView(titleTv, tLp);

        LinearLayout rightGroup = new LinearLayout(act);
        rightGroup.setOrientation(LinearLayout.HORIZONTAL);
        rightGroup.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams rgLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rgLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        rgLp.addRule(RelativeLayout.CENTER_VERTICAL);
        rightGroup.setLayoutParams(rgLp);

        // ll_share pill
        LinearLayout llShare = new LinearLayout(act);
        llShare.setOrientation(LinearLayout.HORIZONTAL);
        llShare.setGravity(Gravity.CENTER);
        llShare.setBackground(rounded(act, 0xFFF0F0F0, 13, 0, 0));
        llShare.setPadding(dp(act, 10), dp(act, 4), dp(act, 10), dp(act, 4));
        LinearLayout.LayoutParams shLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(act, 26));
        shLp.setMargins(0, 0, dp(act, 16), 0);
        llShare.setLayoutParams(shLp);

        ImageView shIv = new ImageView(act);
        shIv.setImageResource(R.drawable.ic_share_ltr);
        llShare.addView(shIv, new LinearLayout.LayoutParams(dp(act, 15), dp(act, 15)));

        TextView shTv = new TextView(act);
        shTv.setText(" Share");
        shTv.setTextSize(13);
        shTv.setTextColor(0xFF222222);
        llShare.addView(shTv);
        llShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) act.getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null && model.activeTemplate != null) {
                    cm.setPrimaryClip(ClipData.newPlainText("Stamp", model.activeTemplate.name));
                }
                Toast.makeText(act, "Stamp settings copied to share", Toast.LENGTH_SHORT).show();
            }
        });
        rightGroup.addView(llShare);

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.btn_close_on_light);
        closeIv.setLayoutParams(new LinearLayout.LayoutParams(dp(act, 26), dp(act, 26)));
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        rightGroup.addView(closeIv);
        header.addView(rightGroup);
        root.addView(header);

        View topDiv = new View(act);
        topDiv.setBackgroundColor(0xFFECECEC);
        root.addView(topDiv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // -------------------------------------------------------------------------------------
        // 2B. Clean List of item_edit_item_single_line.xml Rows (rv_edit, #FFFFFF)
        //     Height 50dp per row: [Switch] + [Single-line 17sp Text] + [arrow_right_gray]
        // -------------------------------------------------------------------------------------
        ScrollView listScroll = new ScrollView(act);
        listScroll.setBackgroundColor(0xFFFFFFFF);
        final LinearLayout listContainer = new LinearLayout(act);
        listContainer.setOrientation(LinearLayout.VERTICAL);
        listContainer.setBackgroundColor(0xFFFFFFFF);

        final Runnable[] rebuildListRef = new Runnable[1];
        rebuildListRef[0] = new Runnable() {
            @Override
            public void run() {
                listContainer.removeAllViews();

                // Row 1: Logo
                addOriginalSingleLineEditRow(act, listContainer, model.showBrandLogo,
                        "Logo", "Logo",
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showBrandLogo = !model.showBrandLogo;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Logo", model.brandLogoText, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        model.brandLogoText = newVal;
                                        model.showBrandLogo = !newVal.isEmpty();
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 2: Time
                String timeDisplay = "Time: " + model.getFormattedTime() + ", " + model.getFormattedDateShort();
                addOriginalSingleLineEditRow(act, listContainer, model.showTime,
                        timeDisplay, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showTime = !model.showTime;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showTimeFormatSheet(act, model, new Runnable() {
                                    @Override
                                    public void run() {
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 3: Address
                addOriginalSingleLineEditRow(act, listContainer, model.showAddress,
                        "Address: " + model.addressLine, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showAddress = !model.showAddress;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Address", model.addressLine, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        if (!newVal.isEmpty()) model.addressLine = newVal;
                                        model.showAddress = true;
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 4: Lat/Long
                addOriginalSingleLineEditRow(act, listContainer, model.showLatLong,
                        "Lat/Long: " + model.getFormattedCoordinates(), null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showLatLong = !model.showLatLong;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showLatLongFormatSheet(act, model, new Runnable() {
                                    @Override
                                    public void run() {
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 5: Map
                addOriginalSingleLineEditRow(act, listContainer, model.showMap,
                        "Map: Satellite Map", null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showMap = !model.showMap;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showMap = !model.showMap;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        });

                // Row 6: Weather
                addOriginalSingleLineEditRow(act, listContainer, model.showWeather,
                        "Weather: " + model.weatherLine, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showWeather = !model.showWeather;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Weather", model.weatherLine, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        if (!newVal.isEmpty()) model.weatherLine = newVal;
                                        model.showWeather = true;
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 7: Title
                String titleVal = (model.customTitle != null && !model.customTitle.trim().isEmpty())
                        ? model.customTitle.trim() : "Please enter text";
                addOriginalSingleLineEditRow(act, listContainer, model.showTitle,
                        "Title: " + titleVal, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showTitle = !model.showTitle;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Title", model.customTitle, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        model.customTitle = newVal;
                                        model.showTitle = !newVal.isEmpty();
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 8: Note
                String noteVal = (model.customNote != null && !model.customNote.trim().isEmpty())
                        ? model.customNote.trim() : "Please enter text";
                addOriginalSingleLineEditRow(act, listContainer, model.showNotes,
                        "Note: " + noteVal, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showNotes = !model.showNotes;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Note", model.customNote, model.recentNotes, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        model.customNote = newVal;
                                        model.showNotes = !newVal.isEmpty();
                                        if (!newVal.isEmpty() && !model.recentNotes.contains(newVal)) {
                                            model.recentNotes.add(0, newVal);
                                        }
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 9: Project
                addOriginalSingleLineEditRow(act, listContainer, model.showProject,
                        "Project: " + model.projectName, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showProject = !model.showProject;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Project", model.projectName, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        model.projectName = newVal;
                                        model.showProject = !newVal.isEmpty();
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 10: Area
                addOriginalSingleLineEditRow(act, listContainer, model.showArea,
                        "Area: " + model.areaName, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showArea = !model.showArea;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Area", model.areaName, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        model.areaName = newVal;
                                        model.showArea = !newVal.isEmpty();
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 11: By (Inspector / Worker)
                addOriginalSingleLineEditRow(act, listContainer, model.showInspector,
                        "By: " + model.inspectorName, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showInspector = !model.showInspector;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "By", model.inspectorName, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        model.inspectorName = newVal;
                                        model.showInspector = !newVal.isEmpty();
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 12: Altitude
                addOriginalSingleLineEditRow(act, listContainer, model.showAltitude,
                        "Altitude: " + model.altitudeLine, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showAltitude = !model.showAltitude;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Altitude", model.altitudeLine, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        if (!newVal.isEmpty()) model.altitudeLine = newVal;
                                        model.showAltitude = true;
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 13: Compass
                addOriginalSingleLineEditRow(act, listContainer, model.showCompass,
                        "Compass: " + model.compassLine, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showCompass = !model.showCompass;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Compass", model.compassLine, null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        if (!newVal.isEmpty()) model.compassLine = newVal;
                                        model.showCompass = true;
                                        model.saveState(act);
                                        if (onUpdate != null) onUpdate.run();
                                        rebuildListRef[0].run();
                                    }
                                });
                            }
                        });

                // Row 14: Photo Code (Side)
                addOriginalSingleLineEditRow(act, listContainer, model.showPhotoCode,
                        "Photo Code: " + model.currentPhotoCode, null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showPhotoCode = !model.showPhotoCode;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        null);

                // Row 15: 100% True Time Logo
                addOriginalSingleLineEditRow(act, listContainer, model.showTimeNowLogo,
                        "100% True Time", null,
                        new Runnable() {
                            @Override
                            public void run() {
                                model.showTimeNowLogo = !model.showTimeNowLogo;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                                rebuildListRef[0].run();
                            }
                        },
                        null);

                // Existing Custom Extra Fields
                for (int i = 0; i < model.customExtraFields.size(); i++) {
                    final int fIdx = i;
                    final String raw = model.customExtraFields.get(i);
                    addOriginalSingleLineEditRow(act, listContainer, true,
                            raw, null,
                            new Runnable() {
                                @Override
                                public void run() {
                                    model.customExtraFields.remove(fIdx);
                                    model.saveState(act);
                                    if (onUpdate != null) onUpdate.run();
                                    rebuildListRef[0].run();
                                }
                            },
                            new Runnable() {
                                @Override
                                public void run() {
                                    showOriginalFieldInputDialog(act, "Custom Text", raw, null, new ValueCallback() {
                                        @Override
                                        public void onSave(String newVal) {
                                            if (newVal.isEmpty()) {
                                                model.customExtraFields.remove(fIdx);
                                            } else {
                                                model.customExtraFields.set(fIdx, newVal);
                                            }
                                            model.saveState(act);
                                            if (onUpdate != null) onUpdate.run();
                                            rebuildListRef[0].run();
                                        }
                                    });
                                }
                            });
                }

                // + Custom Text Row
                addOriginalSingleLineEditRow(act, listContainer, false,
                        "Custom Text: Please enter text", null,
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Custom Text", "", null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        if (!newVal.isEmpty()) {
                                            model.customExtraFields.add(newVal);
                                            model.saveState(act);
                                            if (onUpdate != null) onUpdate.run();
                                            rebuildListRef[0].run();
                                        }
                                    }
                                });
                            }
                        },
                        new Runnable() {
                            @Override
                            public void run() {
                                showOriginalFieldInputDialog(act, "Custom Text", "", null, new ValueCallback() {
                                    @Override
                                    public void onSave(String newVal) {
                                        if (!newVal.isEmpty()) {
                                            model.customExtraFields.add(newVal);
                                            model.saveState(act);
                                            if (onUpdate != null) onUpdate.run();
                                            rebuildListRef[0].run();
                                        }
                                    }
                                });
                            }
                        });
            }
        };
        rebuildListRef[0].run();
        listScroll.addView(listContainer);
        root.addView(listScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 300)));

        View botDiv = new View(act);
        botDiv.setBackgroundColor(0xFFECECEC);
        root.addView(botDiv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // -------------------------------------------------------------------------------------
        // 2C. Bottom Action Bar (activity_new_edit.xml: height 68dp, #FAFAFA)
        //     Left: "Color & Size" (#EBEBED radius 5dp, height 44dp)
        //     Right: "Done" (#0093FF radius 5dp, height 44dp)
        // -------------------------------------------------------------------------------------
        LinearLayout bottomBar = new LinearLayout(act);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setBackgroundColor(0xFFFAFAFA);
        bottomBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
        bottomBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 68)));

        TextView colorSizeBtn = new TextView(act);
        colorSizeBtn.setText("Color & Size");
        colorSizeBtn.setTextSize(16);
        colorSizeBtn.setTextColor(0xFF333333);
        colorSizeBtn.setGravity(Gravity.CENTER);
        colorSizeBtn.setBackground(rounded(act, 0xFFEBEBED, 5, 0, 0));
        LinearLayout.LayoutParams csLp = new LinearLayout.LayoutParams(0, dp(act, 44), 1f);
        csLp.setMargins(0, 0, dp(act, 6), 0);
        colorSizeBtn.setLayoutParams(csLp);
        colorSizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showColorAndSizeSheet(act, model, onUpdate);
            }
        });
        bottomBar.addView(colorSizeBtn);

        TextView doneBtn = new TextView(act);
        doneBtn.setText("Done");
        doneBtn.setTextSize(16);
        doneBtn.setTypeface(Typeface.DEFAULT_BOLD);
        doneBtn.setTextColor(0xFFFFFFFF);
        doneBtn.setGravity(Gravity.CENTER);
        doneBtn.setBackground(rounded(act, 0xFF0093FF, 5, 0, 0));
        LinearLayout.LayoutParams dnLp = new LinearLayout.LayoutParams(0, dp(act, 44), 1f);
        dnLp.setMargins(dp(act, 6), 0, 0, 0);
        doneBtn.setLayoutParams(dnLp);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.saveState(act);
                if (onUpdate != null) onUpdate.run();
                dlg.dismiss();
            }
        });
        bottomBar.addView(doneBtn);
        root.addView(bottomBar);

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
            w.setDimAmount(0.15f);
        }
        dlg.show();
    }

    private interface ValueCallback {
        void onSave(String newVal);
    }

    // Complete, uncropped miniature preview card for every template in the Templates chooser sheet
    private static View buildFullTemplatePreviewCard(final Context ctx, final WatermarkModel.TemplateInfo t) {
        String id = (t != null && t.id != null) ? t.id : "34";

        if ("120".equals(id) || "20".equals(id)) {
            boolean isConst = "120".equals(id);
            LinearLayout card = new LinearLayout(ctx);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(rounded(ctx, 0xFFE6ECF2, 4, 0, 0));

            LinearLayout hdr = new LinearLayout(ctx);
            hdr.setOrientation(LinearLayout.VERTICAL);
            hdr.setBackground(topRounded(ctx, isConst ? 0xFF2E47A0 : 0xFFD98A36, 4));
            hdr.setPadding(dp(ctx, 6), dp(ctx, 4), dp(ctx, 6), dp(ctx, 4));

            if (isConst) {
                LinearLayout logosRow = new LinearLayout(ctx);
                logosRow.setOrientation(LinearLayout.HORIZONTAL);
                logosRow.setGravity(Gravity.CENTER);
                String[] lgLabels = new String[]{"🏢 LOGO", "🏗 LOGO", "🏛 LOGO"};
                for (int i = 0; i < 3; i++) {
                    TextView lPill = new TextView(ctx);
                    lPill.setText(lgLabels[i]);
                    lPill.setTextSize(7f);
                    lPill.setTypeface(Typeface.DEFAULT_BOLD);
                    lPill.setTextColor(0xFF1F3A8A);
                    lPill.setGravity(Gravity.CENTER);
                    lPill.setBackground(rounded(ctx, 0xFFF2F5FA, 2, 0, 0));
                    lPill.setPadding(dp(ctx, 3), dp(ctx, 1), dp(ctx, 3), dp(ctx, 1));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    if (i < 2) lp.setMargins(0, 0, dp(ctx, 3), 0);
                    logosRow.addView(lPill, lp);
                }
                hdr.addView(logosRow);
            }

            TextView hTitle = new TextView(ctx);
            hTitle.setText(isConst ? "Construction Record" : "Work Report");
            hTitle.setTextSize(10.5f);
            hTitle.setTypeface(Typeface.DEFAULT_BOLD);
            hTitle.setTextColor(0xFFFFFFFF);
            hTitle.setGravity(isConst ? Gravity.CENTER : Gravity.START);
            if (isConst) hTitle.setPadding(0, dp(ctx, 2), 0, 0);
            hdr.addView(hTitle);
            card.addView(hdr);

            LinearLayout body = new LinearLayout(ctx);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dp(ctx, 6), dp(ctx, 4), dp(ctx, 6), dp(ctx, 5));
            if (isConst) {
                addMiniTableRow(ctx, body, "Project", "Metro Line 1 Station B");
                addMiniTableRow(ctx, body, "Area", "Zone A - Floor 3");
                addMiniTableRow(ctx, body, "Description", "Floor Inspection");
                addMiniTableRow(ctx, body, "Time", "30/01/2026 09:30");
                addMiniTableRow(ctx, body, "Address", "Cupertino, CA, USA");
            } else {
                addMiniTableRow(ctx, body, "Project", "CH396+800 Roadbed");
                addMiniTableRow(ctx, body, "Inspection", "Site Clearing/Remove");
                addMiniTableRow(ctx, body, "By", "Eng. Nguyen Van A");
                addMiniTableRow(ctx, body, "Time", "30/01/2026 09:30");
                addMiniTableRow(ctx, body, "Address", "McClellan Rd, Cupertino");
            }
            card.addView(body);
            return card;

        } else if ("100".equals(id)) {
            int green = 0xFF5CE08A;
            LinearLayout card = new LinearLayout(ctx);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(rounded(ctx, 0xCC263238, 3, dp(ctx, 1), green));

            View topBar = new View(ctx);
            topBar.setBackgroundColor(green);
            card.addView(topBar, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 3)));

            TextView titleTv = new TextView(ctx);
            titleTv.setText("Work Record");
            titleTv.setTextSize(10.5f);
            titleTv.setTypeface(Typeface.DEFAULT_BOLD);
            titleTv.setTextColor(green);
            titleTv.setPadding(dp(ctx, 6), dp(ctx, 3), dp(ctx, 6), dp(ctx, 3));
            card.addView(titleTv);

            addMiniGreenRow(ctx, card, "Time", "14/09/2026 10:56", green);
            addMiniGreenRow(ctx, card, "Project", "Site Structure A", green);
            addMiniGreenRow(ctx, card, "By", "Inspector Team", green);
            addMiniGreenRow(ctx, card, "Address", "121 St. Petersburg, FL", green);
            return card;

        } else if ("43".equals(id)) {
            LinearLayout card = new LinearLayout(ctx);
            card.setOrientation(LinearLayout.VERTICAL);

            LinearLayout hdr = new LinearLayout(ctx);
            hdr.setOrientation(LinearLayout.HORIZONTAL);
            hdr.setGravity(Gravity.CENTER_VERTICAL);
            hdr.setBackground(topRounded(ctx, 0xFF28356E, 4));
            hdr.setPadding(dp(ctx, 6), dp(ctx, 4), dp(ctx, 6), dp(ctx, 4));

            ImageView sh = new ImageView(ctx);
            sh.setImageResource(R.drawable.tool_border_safe_green);
            hdr.addView(sh, new LinearLayout.LayoutParams(dp(ctx, 14), dp(ctx, 14)));

            TextView tTv = new TextView(ctx);
            tTv.setText(" Security Guard");
            tTv.setTextSize(10.5f);
            tTv.setTypeface(Typeface.DEFAULT_BOLD);
            tTv.setTextColor(0xFFFFF15C);
            hdr.addView(tTv);
            card.addView(hdr);

            View yLine = new View(ctx);
            yLine.setBackgroundColor(0xFFFFF15C);
            card.addView(yLine, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 1.5f)));

            LinearLayout body = new LinearLayout(ctx);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setBackground( rounded(ctx, 0xFF3651C9, 3, 0, 0));
            body.setPadding(dp(ctx, 6), dp(ctx, 3), dp(ctx, 6), dp(ctx, 5));

            TextView tm = new TextView(ctx);
            tm.setText("09:30  30 Jan 2026");
            tm.setTextSize(11f);
            tm.setTypeface(Typeface.DEFAULT_BOLD);
            tm.setTextColor(0xFFFFF15C);
            body.addView(tm);

            addMiniBulletRow(ctx, body, "Patrol Checkpoint Clear");
            addMiniBulletRow(ctx, body, "Cupertino, California, USA");
            addMiniBulletRow(ctx, body, "37.31519°N, 122.05233°E");
            card.addView(body);
            return card;

        } else if ("60".equals(id) || "130".equals(id)) {
            boolean isBigMap = "130".equals(id);
            LinearLayout card = new LinearLayout(ctx);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackground(rounded(ctx, 0xCC263238, 4, 0, 0));

            if (isBigMap) {
                card.addView(createSurveyMapBannerView(ctx, 40),
                        new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 40)));
            }

            LinearLayout body = new LinearLayout(ctx);
            body.setOrientation(LinearLayout.VERTICAL);
            body.setPadding(dp(ctx, 6), dp(ctx, 4), dp(ctx, 6), dp(ctx, 4));

            TextView t1 = new TextView(ctx);
            t1.setText(isBigMap ? "Survey Map • 125°SE" : "21490 McClellan Rd");
            t1.setTextSize(10f);
            t1.setTypeface(Typeface.DEFAULT_BOLD);
            t1.setTextColor(0xFFFFFFFF);
            body.addView(t1);

            TextView t2 = new TextView(ctx);
            t2.setText("Cupertino, California, USA");
            t2.setTextSize(8.5f);
            t2.setTextColor(0xEEFFFFFF);
            body.addView(t2);

            TextView t3 = new TextView(ctx);
            t3.setText("37.31519°N, 122.05233°E • 26°C");
            t3.setTextSize(8f);
            t3.setTextColor(0xDDFFFFFF);
            body.addView(t3);

            TextView t4 = new TextView(ctx);
            t4.setText("08/07/2026 09:30 • 18.4m ASL");
            t4.setTextSize(8f);
            t4.setTextColor(0xDDFFFFFF);
            body.addView(t4);

            card.addView(body);
            View botLine = new View(ctx);
            botLine.setBackgroundColor(0xFFFFC233);
            card.addView(botLine, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 2)));
            return card;

        } else if ("dasher01".equals(id)) {
            LinearLayout card = new LinearLayout(ctx);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setPadding(dp(ctx, 4), dp(ctx, 3), dp(ctx, 4), dp(ctx, 3));

            LinearLayout pill = new LinearLayout(ctx);
            pill.setOrientation(LinearLayout.HORIZONTAL);
            pill.setGravity(Gravity.CENTER_VERTICAL);

            TextView badge = new TextView(ctx);
            badge.setText("TIPS & REVIEWS KEEP ME DRIVING");
            badge.setTextSize(7f);
            badge.setTypeface(Typeface.DEFAULT_BOLD);
            badge.setTextColor(0xFFFFFFFF);
            badge.setBackground(rounded(ctx, 0xFFFF1717, 2.5f, 0, 0));
            badge.setPadding(dp(ctx, 4), dp(ctx, 2), dp(ctx, 4), dp(ctx, 2));
            pill.addView(badge);

            TextView clk = new TextView(ctx);
            clk.setText(" 09:35 ");
            clk.setTextSize(10f);
            clk.setTypeface(Typeface.DEFAULT_BOLD);
            clk.setTextColor(0xFF0A2463);
            clk.setBackground(rounded(ctx, 0xFFFFFFFF, 2.5f, 0, 0));
            clk.setPadding(dp(ctx, 3), dp(ctx, 1), dp(ctx, 3), dp(ctx, 1));
            pill.addView(clk);
            card.addView(pill);

            TextView dt = new TextView(ctx);
            dt.setText("30/01/2026 • Cupertino, CA, USA");
            dt.setTextSize(8.5f);
            dt.setTextColor(0xFFFFFFFF);
            dt.setPadding(0, dp(ctx, 3), 0, dp(ctx, 3));
            card.addView(dt);

            LinearLayout biz = new LinearLayout(ctx);
            biz.setOrientation(LinearLayout.HORIZONTAL);
            biz.setGravity(Gravity.CENTER_VERTICAL);
            biz.setBackground(rounded(ctx, 0xE6FFFFFF, 3, 0, 0));
            biz.setPadding(dp(ctx, 5), dp(ctx, 3), dp(ctx, 5), dp(ctx, 3));

            TextView av = new TextView(ctx);
            av.setText("🛵 ");
            av.setTextSize(10f);
            biz.addView(av);

            TextView bTxt = new TextView(ctx);
            bTxt.setText("Delivered By Courier • 5.0 ★");
            bTxt.setTextSize(8f);
            bTxt.setTypeface(Typeface.DEFAULT_BOLD);
            bTxt.setTextColor(0xFF222222);
            biz.addView(bTxt);
            card.addView(biz);
            return card;

        } else {
            // For 34, map01, 21, 10, 80, service01, 90: use crisp ImageView with ADJUST_VIEW_BOUNDS + full lines
            ImageView iv = new ImageView(ctx);
            iv.setImageResource(t.coverResId);
            iv.setAdjustViewBounds(true);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            return iv;
        }
    }

    public static View createSurveyMapBannerView(final Context ctx, final int heightDp) {
        return new View(ctx) {
            private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            private final RectF rf = new RectF();

            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                int w = getWidth();
                int h = getHeight();
                if (w <= 0 || h <= 0) return;

                // Base light map terrain
                p.setStyle(Paint.Style.FILL);
                p.setColor(0xFFEEF2E8);
                canvas.drawRect(0, 0, w, h, p);

                // Green survey blocks / park polygons
                p.setColor(0xFFC5E89B);
                canvas.drawRect(0, 0, w * 0.45f, h * 0.58f, p);
                p.setColor(0xFFB3DC82);
                canvas.drawRect(w * 0.52f, h * 0.15f, w * 0.88f, h * 0.78f, p);
                p.setColor(0xFFD6F0B4);
                canvas.drawRect(w * 0.10f, h * 0.65f, w * 0.48f, h, p);

                // Blue river / canal channel
                p.setColor(0xFF81D4FA);
                p.setStrokeWidth(dp(ctx, 4.5f));
                canvas.drawLine(0, h * 0.82f, w * 0.72f, 0, p);

                // White survey roads
                p.setColor(0xFFFFFFFF);
                p.setStrokeWidth(dp(ctx, 4f));
                canvas.drawLine(0, h * 0.25f, w, h * 0.78f, p);
                canvas.drawLine(w * 0.28f, 0, w * 0.65f, h, p);
                p.setStrokeWidth(dp(ctx, 2.5f));
                canvas.drawLine(0, h * 0.55f, w, h * 0.32f, p);

                // Compass radar direction cone (SE)
                float cx = w * 0.46f;
                float cy = h * 0.52f;
                float coneR = Math.min(w, h) * 0.44f;
                rf.set(cx - coneR, cy - coneR, cx + coneR, cy + coneR);
                p.setStyle(Paint.Style.FILL);
                p.setColor(0x441E88E5);
                canvas.drawArc(rf, 15f, 55f, true, p);

                // Outer white halo + blue GPS beacon dot
                p.setColor(0xFFFFFFFF);
                canvas.drawCircle(cx, cy, dp(ctx, 5.5f), p);
                p.setColor(0xFF1565C0);
                canvas.drawCircle(cx, cy, dp(ctx, 3.8f), p);

                // Top-right dark camera badge
                float bw = dp(ctx, 26);
                float bh = dp(ctx, 16);
                float bx = w - bw - dp(ctx, 6);
                float by = dp(ctx, 4);
                rf.set(bx, by, bx + bw, by + bh);
                p.setColor(0x99263238);
                canvas.drawRoundRect(rf, dp(ctx, 3), dp(ctx, 3), p);
                p.setColor(0xFFFFFFFF);
                canvas.drawCircle(bx + bw / 2f, by + bh / 2f, dp(ctx, 3.2f), p);
            }
        };
    }

    private static void addMiniTableRow(Context ctx, LinearLayout parent, String k, String v) {
        LinearLayout r = new LinearLayout(ctx);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setPadding(0, dp(ctx, 0.5f), 0, dp(ctx, 0.5f));

        TextView kTv = new TextView(ctx);
        kTv.setText(k + ": ");
        kTv.setTextSize(8f);
        kTv.setTextColor(0xFF444444);
        r.addView(kTv, new LinearLayout.LayoutParams(dp(ctx, 48), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView vTv = new TextView(ctx);
        vTv.setText(v);
        vTv.setTextSize(8f);
        vTv.setTypeface(Typeface.DEFAULT_BOLD);
        vTv.setTextColor(0xFF111111);
        r.addView(vTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        parent.addView(r);
    }

    private static void addMiniGreenRow(Context ctx, LinearLayout parent, String k, String v, int green) {
        View div = new View(ctx);
        div.setBackgroundColor(green);
        parent.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        LinearLayout r = new LinearLayout(ctx);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setPadding(dp(ctx, 5), dp(ctx, 2), dp(ctx, 5), dp(ctx, 2));

        TextView kTv = new TextView(ctx);
        kTv.setText(k);
        kTv.setTextSize(8f);
        kTv.setTextColor(0xFFE0F2F1);
        r.addView(kTv, new LinearLayout.LayoutParams(dp(ctx, 38), ViewGroup.LayoutParams.WRAP_CONTENT));

        TextView vTv = new TextView(ctx);
        vTv.setText("|  " + v);
        vTv.setTextSize(8f);
        vTv.setTextColor(0xFFFFFFFF);
        r.addView(vTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        parent.addView(r);
    }

    private static void addMiniBulletRow(Context ctx, LinearLayout parent, String text) {
        TextView tv = new TextView(ctx);
        tv.setText("▪ " + text);
        tv.setTextSize(8f);
        tv.setTextColor(0xFFFFFFFF);
        parent.addView(tv);
    }

    // Exact 1:1 row from item_edit_item_single_line.xml (minHeight 50dp, switch_edit + multi-line tv_title + arrow_right_gray)
    private static void addOriginalSingleLineEditRow(final Context ctx, LinearLayout parent,
                                                     boolean checked, String text, String rightBlueLabel,
                                                     final Runnable onToggle, final Runnable onRowClick) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setBackgroundColor(0xFFFFFFFF);
        row.setMinimumHeight(dp(ctx, 50));
        row.setPadding(dp(ctx, 16), dp(ctx, 8), dp(ctx, 16), dp(ctx, 8));
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        View sw = createOriginalSwitch(ctx, checked);
        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onToggle != null) onToggle.run();
            }
        });
        row.addView(sw);

        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextSize(15.5f);
        tv.setSingleLine(false);
        tv.setMaxLines(3);
        tv.setTextColor(checked ? 0xFF222222 : 0xFF999999);
        tv.setPadding(dp(ctx, 12), 0, dp(ctx, 8), 0);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        if (rightBlueLabel != null) {
            TextView rb = new TextView(ctx);
            rb.setText(rightBlueLabel);
            rb.setTextSize(16);
            rb.setTextColor(0xFF0093FF);
            rb.setPadding(0, 0, dp(ctx, 4), 0);
            row.addView(rb);
        }

        if (onRowClick != null) {
            ImageView arrow = new ImageView(ctx);
            arrow.setImageResource(R.drawable.arrow_right_gray);
            row.addView(arrow, new LinearLayout.LayoutParams(dp(ctx, 16), dp(ctx, 16)));
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onRowClick.run();
                }
            });
        } else {
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onToggle != null) onToggle.run();
                }
            });
        }

        parent.addView(row);

        View div = new View(ctx);
        div.setBackgroundColor(0xFFECECEC);
        LinearLayout.LayoutParams dLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1);
        dLp.setMargins(dp(ctx, 16), 0, dp(ctx, 16), 0);
        parent.addView(div, dLp);
    }

    // Exact 1:1 dialog_edit_input_global.xml input sheet
    private static void showOriginalFieldInputDialog(final Activity act, final String title, String currentVal,
                                                     final List<String> historyItems, final ValueCallback cb) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(topRounded(act, 0xFFFFFFFF, 12));

        // Top Bar (height 52dp: backIv + titleTv)
        RelativeLayout topBar = new RelativeLayout(act);
        topBar.setPadding(dp(act, 12), 0, dp(act, 12), 0);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 52)));

        ImageView backIv = new ImageView(act);
        backIv.setImageResource(R.drawable.ic_back_ltr);
        RelativeLayout.LayoutParams bLp = new RelativeLayout.LayoutParams(dp(act, 28), dp(act, 28));
        bLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        bLp.addRule(RelativeLayout.CENTER_VERTICAL);
        backIv.setLayoutParams(bLp);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(backIv);

        TextView titleTv = new TextView(act);
        titleTv.setText(title);
        titleTv.setTextSize(16);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF333333);
        RelativeLayout.LayoutParams tLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        topBar.addView(titleTv, tLp);
        root.addView(topBar);

        View div = new View(act);
        div.setBackgroundColor(0xFFECECEC);
        root.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // Body (Content label + #EBEBED radius 5dp EditText + optional History list)
        LinearLayout body = new LinearLayout(act);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 8));

        TextView labelTv = new TextView(act);
        labelTv.setText("Content");
        labelTv.setTextSize(12);
        labelTv.setTextColor(0xFF444444);
        labelTv.setPadding(0, 0, 0, dp(act, 6));
        body.addView(labelTv);

        LinearLayout inputBox = new LinearLayout(act);
        inputBox.setOrientation(LinearLayout.HORIZONTAL);
        inputBox.setGravity(Gravity.CENTER_VERTICAL);
        inputBox.setBackground(rounded(act, 0xFFEBEBED, 5, 0, 0));
        inputBox.setPadding(dp(act, 12), dp(act, 6), dp(act, 8), dp(act, 6));

        final EditText et = new EditText(act);
        et.setText(currentVal != null ? currentVal : "");
        et.setHint("Please enter text");
        et.setHintTextColor(0xFF979797);
        et.setTextSize(16);
        et.setTextColor(0xFF333333);
        et.setBackground(null);
        et.setSelection(et.getText().length());
        inputBox.addView(et, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageView clearIv = new ImageView(act);
        clearIv.setImageResource(R.drawable.btn_close_on_light);
        clearIv.setPadding(dp(act, 4), dp(act, 4), dp(act, 4), dp(act, 4));
        clearIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et.setText("");
            }
        });
        inputBox.addView(clearIv, new LinearLayout.LayoutParams(dp(act, 24), dp(act, 24)));
        body.addView(inputBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        if (historyItems != null && !historyItems.isEmpty()) {
            TextView histTitle = new TextView(act);
            histTitle.setText("History");
            histTitle.setTextSize(12);
            histTitle.setTextColor(0xFF757575);
            histTitle.setPadding(0, dp(act, 14), 0, dp(act, 6));
            body.addView(histTitle);

            for (int i = 0; i < Math.min(5, historyItems.size()); i++) {
                final String hVal = historyItems.get(i);
                TextView hRow = new TextView(act);
                hRow.setText(hVal);
                hRow.setTextSize(14);
                hRow.setTextColor(0xFF333333);
                hRow.setBackground(rounded(act, 0xFFF7F7F7, 5, 0, 0));
                hRow.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
                LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                hLp.setMargins(0, 0, 0, dp(act, 6));
                hRow.setLayoutParams(hLp);
                hRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        et.setText(hVal);
                        et.setSelection(hVal.length());
                    }
                });
                body.addView(hRow);
            }
        }
        root.addView(body);

        // Bottom Save Button (#0093FF radius 5dp, height 44dp)
        LinearLayout botWrap = new LinearLayout(act);
        botWrap.setPadding(dp(act, 16), dp(act, 10), dp(act, 16), dp(act, 16));
        TextView saveBtn = new TextView(act);
        saveBtn.setText("Save");
        saveBtn.setTextSize(16);
        saveBtn.setTypeface(Typeface.DEFAULT_BOLD);
        saveBtn.setTextColor(0xFFFFFFFF);
        saveBtn.setGravity(Gravity.CENTER);
        saveBtn.setBackground(rounded(act, 0xFF0093FF, 5, 0, 0));
        saveBtn.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 44)));
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb != null) {
                    cb.onSave(et.getText().toString().trim());
                }
                dlg.dismiss();
            }
        });
        botWrap.addView(saveBtn);
        root.addView(botWrap);

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
            w.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        dlg.show();
    }

    // Clean Color & Size Bottom Sheet (layout_seek_bar_for_watermark_size.xml + Color Swatches)
    private static void showColorAndSizeSheet(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(topRounded(act, 0xFFFFFFFF, 12));
        root.setPadding(dp(act, 16), dp(act, 14), dp(act, 16), dp(act, 20));

        RelativeLayout topBar = new RelativeLayout(act);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 36)));
        TextView titleTv = new TextView(act);
        titleTv.setText("Color & Size");
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        topBar.addView(titleTv);

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.btn_close_on_light);
        RelativeLayout.LayoutParams cLp = new RelativeLayout.LayoutParams(dp(act, 24), dp(act, 24));
        cLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        closeIv.setLayoutParams(cLp);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(closeIv);
        root.addView(topBar);

        // Color Swatches
        TextView cLabel = new TextView(act);
        cLabel.setText("Color");
        cLabel.setTextSize(14);
        cLabel.setTextColor(0xFF555555);
        cLabel.setPadding(0, dp(act, 12), 0, dp(act, 8));
        root.addView(cLabel);

        final LinearLayout colorRow = new LinearLayout(act);
        colorRow.setOrientation(LinearLayout.HORIZONTAL);
        colorRow.setGravity(Gravity.CENTER_VERTICAL);
        final int[] palette = new int[]{
            0xFFFFC233, 0xFF0093FF, 0xFF2E47A0, 0xFFFF1717, 0xFF10B981, 0xFFD98A36, 0xFF7A6EFF, 0xFF263238
        };
        final Runnable rebuildSwatches = new Runnable() {
            @Override
            public void run() {
                colorRow.removeAllViews();
                for (int i = 0; i < palette.length; i++) {
                    final int col = palette[i];
                    boolean sel = (model.customAccentColor == col);
                    View circle = new View(act);
                    circle.setBackground(rounded(act, col, 16, sel ? dp(act, 2.5f) : dp(act, 1), sel ? 0xFF222222 : 0xFFE0E0E0));
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(act, 32), dp(act, 32));
                    lp.setMargins(0, 0, dp(act, 10), 0);
                    circle.setLayoutParams(lp);
                    circle.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            model.customAccentColor = col;
                            model.saveState(act);
                            if (onUpdate != null) onUpdate.run();
                            run();
                        }
                    });
                    colorRow.addView(circle);
                }
            }
        };
        rebuildSwatches.run();
        root.addView(colorRow);

        // Size Slider (layout_seek_bar_for_watermark_size.xml)
        TextView sLabel = new TextView(act);
        sLabel.setText("Size");
        sLabel.setTextSize(14);
        sLabel.setTextColor(0xFF555555);
        sLabel.setPadding(0, dp(act, 18), 0, dp(act, 8));
        root.addView(sLabel);

        SeekBar sizeSeek = new SeekBar(act);
        sizeSeek.setMax(100);
        int curProg = (int) ((model.stampScale - 0.75f) / 0.50f * 100f);
        sizeSeek.setProgress(Math.max(0, Math.min(100, curProg)));
        sizeSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                model.stampScale = 0.75f + (progress / 100f) * 0.50f;
                if (onUpdate != null) onUpdate.run();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                model.saveState(act);
            }
        });
        root.addView(sizeSeek, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
            w.setDimAmount(0.15f);
        }
        dlg.show();
    }

    // Clean Time & Date Format Sheet
    private static void showTimeFormatSheet(final Activity act, final WatermarkModel model, final Runnable onChanged) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(topRounded(act, 0xFFFFFFFF, 12));
        root.setPadding(dp(act, 16), dp(act, 14), dp(act, 16), dp(act, 20));

        TextView titleTv = new TextView(act);
        titleTv.setText("Time Format");
        titleTv.setTextSize(17);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        titleTv.setPadding(0, 0, 0, dp(act, 10));
        root.addView(titleTv);

        addOriginalSingleLineEditRow(act, root, model.use24HourFormat, "24-Hour Time", null, new Runnable() {
            @Override
            public void run() {
                model.use24HourFormat = !model.use24HourFormat;
                model.saveState(act);
                if (onChanged != null) onChanged.run();
                dlg.dismiss();
            }
        }, null);

        addOriginalSingleLineEditRow(act, root, model.showSecondsInClock, "Show Seconds", null, new Runnable() {
            @Override
            public void run() {
                model.showSecondsInClock = !model.showSecondsInClock;
                model.saveState(act);
                if (onChanged != null) onChanged.run();
                dlg.dismiss();
            }
        }, null);

        final String[] dateFormats = new String[]{"Default", "dd/MM/yyyy", "dd MMM yyyy", "yyyy-MM-dd", "MM/dd/yyyy"};
        for (int i = 0; i < dateFormats.length; i++) {
            final int idx = i;
            TextView row = new TextView(act);
            row.setText("Date Format: " + dateFormats[i] + (model.dateFormatIndex == i ? "  ✓" : ""));
            row.setTextSize(16);
            row.setTextColor(model.dateFormatIndex == i ? 0xFF0093FF : 0xFF222222);
            row.setPadding(dp(act, 8), dp(act, 12), dp(act, 8), dp(act, 12));
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.dateFormatIndex = idx;
                    model.saveState(act);
                    if (onChanged != null) onChanged.run();
                    dlg.dismiss();
                }
            });
            root.addView(row);
        }

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
        }
        dlg.show();
    }

    // Clean Lat/Long Format Sheet
    private static void showLatLongFormatSheet(final Activity act, final WatermarkModel model, final Runnable onChanged) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(topRounded(act, 0xFFFFFFFF, 12));
        root.setPadding(dp(act, 16), dp(act, 14), dp(act, 16), dp(act, 20));

        TextView titleTv = new TextView(act);
        titleTv.setText("Lat/Long Format");
        titleTv.setTextSize(17);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        titleTv.setPadding(0, 0, 0, dp(act, 10));
        root.addView(titleTv);

        final String[] labels = new String[]{
            "Decimal (21.04031°N 105.78931°E)",
            "DMS (21°02'25.1\"N 105°47'21.5\"E)",
            "UTM (UTM 48Q 582014E 2326512N)"
        };
        for (int i = 0; i < labels.length; i++) {
            final int idx = i;
            TextView row = new TextView(act);
            row.setText(labels[i] + (model.gpsFormatMode == i ? "  ✓" : ""));
            row.setTextSize(16);
            row.setTextColor(model.gpsFormatMode == i ? 0xFF0093FF : 0xFF222222);
            row.setPadding(dp(act, 8), dp(act, 12), dp(act, 8), dp(act, 12));
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.gpsFormatMode = idx;
                    model.saveState(act);
                    if (onChanged != null) onChanged.run();
                    dlg.dismiss();
                }
            });
            root.addView(row);
        }

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
        }
        dlg.show();
    }

    // =========================================================================================
    // 3. LEFT SETTINGS DRAWER (1:1 setting_dialog_view_new.xml)
    // =========================================================================================
    public static void showLeftSettingsDrawer(final Activity act, final WatermarkModel model,
                                              final Runnable onRatioChanged, final Runnable onWatermarkChanged) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        ScrollView scroll = new ScrollView(act);
        scroll.setBackgroundColor(0xFFF6F6F6);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF6F6F6);
        root.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 24));

        // Top Bar: "Settings" + closeIv
        RelativeLayout topBar = new RelativeLayout(act);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 44)));

        TextView titleTv = new TextView(act);
        titleTv.setText("Settings");
        titleTv.setTextSize(20);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        RelativeLayout.LayoutParams tLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.addRule(RelativeLayout.CENTER_VERTICAL);
        topBar.addView(titleTv, tLp);

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.btn_close_on_light);
        RelativeLayout.LayoutParams cLp = new RelativeLayout.LayoutParams(dp(act, 26), dp(act, 26));
        cLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        cLp.addRule(RelativeLayout.CENTER_VERTICAL);
        closeIv.setLayoutParams(cLp);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(closeIv);
        root.addView(topBar);

        // 1. in_teamspace_header (setting_teamspace_login.xml)
        LinearLayout teamHeader = new LinearLayout(act);
        teamHeader.setOrientation(LinearLayout.HORIZONTAL);
        teamHeader.setGravity(Gravity.CENTER_VERTICAL);
        teamHeader.setBackground(rounded(act, 0xFFFFFFFF, 8, 0, 0));
        teamHeader.setPadding(dp(act, 14), dp(act, 12), dp(act, 14), dp(act, 12));
        LinearLayout.LayoutParams thLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        thLp.setMargins(0, dp(act, 8), 0, 0);
        teamHeader.setLayoutParams(thLp);

        ImageView userIv = new ImageView(act);
        userIv.setImageResource(R.drawable.tool_border_user);
        teamHeader.addView(userIv, new LinearLayout.LayoutParams(dp(act, 36), dp(act, 36)));

        LinearLayout teamTextCol = new LinearLayout(act);
        teamTextCol.setOrientation(LinearLayout.VERTICAL);
        teamTextCol.setPadding(dp(act, 12), 0, dp(act, 8), 0);
        TextView tt1 = new TextView(act);
        tt1.setText("Teamspace");
        tt1.setTextSize(16);
        tt1.setTypeface(Typeface.DEFAULT_BOLD);
        tt1.setTextColor(0xFF222222);
        teamTextCol.addView(tt1);
        TextView tt2 = new TextView(act);
        tt2.setText("Auto-sync work photos & timesheets");
        tt2.setTextSize(12);
        tt2.setTextColor(0xFF757575);
        teamTextCol.addView(tt2);
        teamHeader.addView(teamTextCol, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageView fw1 = new ImageView(act);
        fw1.setImageResource(R.drawable.ic_forward_ltr);
        teamHeader.addView(fw1, new LinearLayout.LayoutParams(dp(act, 20), dp(act, 20)));
        teamHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showTeamspaceDialog(act, model, onWatermarkChanged);
            }
        });
        root.addView(teamHeader);

        // 2. in_vip (setting_dialog_view_new_child_vip.xml: #7A6EFF purple card)
        LinearLayout vipCard = new LinearLayout(act);
        vipCard.setOrientation(LinearLayout.HORIZONTAL);
        vipCard.setGravity(Gravity.CENTER_VERTICAL);
        vipCard.setBackground(rounded(act, 0xFF7A6EFF, 8, 0, 0));
        vipCard.setPadding(dp(act, 16), 0, dp(act, 16), 0);
        LinearLayout.LayoutParams vipLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 52));
        vipLp.setMargins(0, dp(act, 10), 0, 0);
        vipCard.setLayoutParams(vipLp);

        ImageView crownIv = new ImageView(act);
        crownIv.setImageResource(R.drawable.ic_vip_crown);
        vipCard.addView(crownIv, new LinearLayout.LayoutParams(dp(act, 24), dp(act, 24)));

        TextView vipTv = new TextView(act);
        vipTv.setText("Timemark Plans");
        vipTv.setTextSize(16);
        vipTv.setTypeface(Typeface.DEFAULT_BOLD);
        vipTv.setTextColor(0xFFFFFFFF);
        vipTv.setPadding(dp(act, 12), 0, 0, 0);
        vipCard.addView(vipTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageView fw2 = new ImageView(act);
        fw2.setImageResource(R.drawable.ic_forward_ltr);
        fw2.setColorFilter(0xFFFFFFFF);
        vipCard.addView(fw2, new LinearLayout.LayoutParams(dp(act, 20), dp(act, 20)));
        vipCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showPurchaseDialog(act, model, onWatermarkChanged);
            }
        });
        root.addView(vipCard);

        // 3. in_remove_ads (setting_dialog_view_new_child_earn_vip.xml)
        LinearLayout removeAdsCard = new LinearLayout(act);
        removeAdsCard.setOrientation(LinearLayout.HORIZONTAL);
        removeAdsCard.setGravity(Gravity.CENTER_VERTICAL);
        removeAdsCard.setBackground(rounded(act, 0xFFFFFFFF, 8, 0, 0));
        removeAdsCard.setPadding(dp(act, 16), 0, dp(act, 16), 0);
        LinearLayout.LayoutParams raLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 52));
        raLp.setMargins(0, dp(act, 10), 0, 0);
        removeAdsCard.setLayoutParams(raLp);

        ImageView raIv = new ImageView(act);
        raIv.setImageResource(R.drawable.icon_remove_ads);
        removeAdsCard.addView(raIv, new LinearLayout.LayoutParams(dp(act, 28), dp(act, 28)));

        TextView raTv = new TextView(act);
        raTv.setText("Remove Ads");
        raTv.setTextSize(16);
        raTv.setTypeface(Typeface.DEFAULT_BOLD);
        raTv.setTextColor(0xFF222222);
        raTv.setPadding(dp(act, 12), 0, 0, 0);
        removeAdsCard.addView(raTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageView fw3 = new ImageView(act);
        fw3.setImageResource(R.drawable.ic_forward_ltr);
        removeAdsCard.addView(fw3, new LinearLayout.LayoutParams(dp(act, 20), dp(act, 20)));
        removeAdsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showPurchaseDialog(act, model, onWatermarkChanged);
            }
        });
        root.addView(removeAdsCard);

        // 4. in_common (setting_dialog_view_new_child_common.xml: 2x2 grid Ratio, Timer, Resolution, Settings)
        LinearLayout gridRow1 = new LinearLayout(act);
        gridRow1.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams gr1Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 56));
        gr1Lp.setMargins(0, dp(act, 10), 0, 0);
        gridRow1.setLayoutParams(gr1Lp);

        final TextView ratioValTv = new TextView(act);
        String[] rNames = new String[]{"4:3", "16:9", "1:1", "Full"};
        LinearLayout ratioBox = createDrawerGridTile(act, R.drawable.ratio_pop_view_3_4, "Ratio", rNames[model.aspectRatioMode % 4], ratioValTv);
        LinearLayout.LayoutParams b1Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        b1Lp.setMargins(0, 0, dp(act, 5), 0);
        ratioBox.setLayoutParams(b1Lp);
        ratioBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatioPopup(act, model, new Runnable() {
                    @Override
                    public void run() {
                        String[] rn = new String[]{"4:3", "16:9", "1:1", "Full"};
                        ratioValTv.setText(rn[model.aspectRatioMode % 4]);
                        if (onRatioChanged != null) onRatioChanged.run();
                    }
                });
            }
        });
        gridRow1.addView(ratioBox);

        final TextView timerValTv = new TextView(act);
        LinearLayout timerBox = createDrawerGridTile(act, R.drawable.tool_border_time, "Timer", model.timerSeconds == 0 ? "Off" : (model.timerSeconds + "s"), timerValTv);
        LinearLayout.LayoutParams b2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        b2Lp.setMargins(dp(act, 5), 0, 0, 0);
        timerBox.setLayoutParams(b2Lp);
        timerBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.timerSeconds == 0) model.timerSeconds = 3;
                else if (model.timerSeconds == 3) model.timerSeconds = 5;
                else if (model.timerSeconds == 5) model.timerSeconds = 10;
                else model.timerSeconds = 0;
                timerValTv.setText(model.timerSeconds == 0 ? "Off" : (model.timerSeconds + "s"));
            }
        });
        gridRow1.addView(timerBox);
        root.addView(gridRow1);

        LinearLayout gridRow2 = new LinearLayout(act);
        gridRow2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams gr2Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 56));
        gr2Lp.setMargins(0, dp(act, 10), 0, 0);
        gridRow2.setLayoutParams(gr2Lp);

        final TextView resValTv = new TextView(act);
        LinearLayout resBox = createDrawerGridTile(act, R.drawable.tool_border_gallery2, "Resolution", model.resolutionLabel, resValTv);
        resBox.setLayoutParams(b1Lp);
        resBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("FHD 1080P".equals(model.resolutionLabel)) model.resolutionLabel = "UHD 4K";
                else if ("UHD 4K".equals(model.resolutionLabel)) model.resolutionLabel = "HD 720P";
                else model.resolutionLabel = "FHD 1080P";
                resValTv.setText(model.resolutionLabel);
            }
        });
        gridRow2.addView(resBox);

        TextView moreValTv = new TextView(act);
        LinearLayout moreBox = createDrawerGridTile(act, R.drawable.icon_tool_border_setting, "Settings", "More", moreValTv);
        moreBox.setLayoutParams(b2Lp);
        moreBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showFullSettingsDialog(act, model, onWatermarkChanged);
            }
        });
        gridRow2.addView(moreBox);
        root.addView(gridRow2);

        // 5. in_tools (setting_dialog_view_new_child_tools.xml)
        LinearLayout toolsCard = new LinearLayout(act);
        toolsCard.setOrientation(LinearLayout.VERTICAL);
        toolsCard.setBackground(rounded(act, 0xFFFFFFFF, 8, 0, 0));
        toolsCard.setPadding(dp(act, 16), 0, dp(act, 16), 0);
        LinearLayout.LayoutParams tcLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tcLp.setMargins(0, dp(act, 10), 0, 0);
        toolsCard.setLayoutParams(tcLp);

        addDrawerToolRow(act, toolsCard, R.drawable.tool_border_safe_green, "Verify Photo", null, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showVerifyCenterDialog(act, model);
            }
        });

        addDrawerToolSwitchRow(act, toolsCard, R.drawable.tool_border_watermark, "Show Logo", model.showTimeNowLogo, new Runnable() {
            @Override
            public void run() {
                model.showTimeNowLogo = !model.showTimeNowLogo;
                model.saveState(act);
                if (onWatermarkChanged != null) onWatermarkChanged.run();
            }
        });

        addDrawerToolSwitchRow(act, toolsCard, R.drawable.tool_border_gallery2, "Save Original Photo", model.saveOriginalPhoto, new Runnable() {
            @Override
            public void run() {
                model.saveOriginalPhoto = !model.saveOriginalPhoto;
                model.saveState(act);
            }
        });

        addDrawerToolRow(act, toolsCard, R.drawable.tool_border_upload, "Google Drive & OneDrive", null, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(act, "Cloud Auto-Sync Active", Toast.LENGTH_SHORT).show();
            }
        });
        root.addView(toolsCard);

        // 6. in_service (setting_dialog_view_new_child_service.xml)
        LinearLayout serviceCard = new LinearLayout(act);
        serviceCard.setOrientation(LinearLayout.VERTICAL);
        serviceCard.setBackground(rounded(act, 0xFFFFFFFF, 8, 0, 0));
        serviceCard.setPadding(dp(act, 16), 0, dp(act, 16), 0);
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scLp.setMargins(0, dp(act, 10), 0, 0);
        serviceCard.setLayoutParams(scLp);

        addDrawerToolRow(act, serviceCard, R.drawable.tool_border_email, "Contact Us", null, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(act, "Support: support@nextai.global", Toast.LENGTH_SHORT).show();
            }
        });

        addDrawerToolRow(act, serviceCard, R.drawable.tool_border_share, "Share App", null, null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Timemark Camera — 100% True Time & GPS Location Stamp");
                act.startActivity(Intent.createChooser(sendIntent, "Share App"));
            }
        });
        root.addView(serviceCard);

        scroll.addView(root);
        dlg.setContentView(scroll);
        applyDialogSystemBarInsets(act, dlg, root, 0xFFF6F6F6, true);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0xFFF6F6F6));
            int width = (int) (act.getResources().getDisplayMetrics().widthPixels * 0.86f);
            w.setLayout(width, ViewGroup.LayoutParams.MATCH_PARENT);
            w.setGravity(Gravity.START);
        }
        dlg.show();
    }

    private interface RunnableClick {
        void onClick();
    }

    private static LinearLayout createDrawerGridTile(Context ctx, int iconRes, String title, String sub, TextView subTv) {
        LinearLayout box = new LinearLayout(ctx);
        box.setOrientation(LinearLayout.HORIZONTAL);
        box.setGravity(Gravity.CENTER_VERTICAL);
        box.setBackground(rounded(ctx, 0xFFFFFFFF, 8, 0, 0));
        box.setPadding(dp(ctx, 12), 0, dp(ctx, 12), 0);

        ImageView iv = new ImageView(ctx);
        iv.setImageResource(iconRes);
        box.addView(iv, new LinearLayout.LayoutParams(dp(ctx, 26), dp(ctx, 26)));

        LinearLayout col = new LinearLayout(ctx);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(dp(ctx, 10), 0, 0, 0);
        TextView t1 = new TextView(ctx);
        t1.setText(title);
        t1.setTextSize(14);
        t1.setTypeface(Typeface.DEFAULT_BOLD);
        t1.setTextColor(0xFF222222);
        col.addView(t1);

        subTv.setText(sub);
        subTv.setTextSize(12);
        subTv.setTextColor(0xFF757575);
        col.addView(subTv);
        box.addView(col);
        return box;
    }

    private static void addDrawerToolRow(Context ctx, LinearLayout parent, int iconRes, String title, String badge,
                                         RunnableClick unused, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 52)));

        ImageView iv = new ImageView(ctx);
        iv.setImageResource(iconRes);
        if (iconRes == R.drawable.tool_border_email) {
            iv.setColorFilter(0xFF222222);
        }
        row.addView(iv, new LinearLayout.LayoutParams(dp(ctx, 24), dp(ctx, 24)));

        TextView tv = new TextView(ctx);
        tv.setText(title);
        tv.setTextSize(15);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(0xFF222222);
        tv.setPadding(dp(ctx, 12), 0, 0, 0);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageView fw = new ImageView(ctx);
        fw.setImageResource(R.drawable.ic_forward_ltr);
        row.addView(fw, new LinearLayout.LayoutParams(dp(ctx, 20), dp(ctx, 20)));
        row.setOnClickListener(listener);
        parent.addView(row);
    }

    private static void addDrawerToolSwitchRow(final Context ctx, LinearLayout parent, int iconRes, String title,
                                               final boolean initialChecked, final Runnable onToggle) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(ctx, 52)));

        ImageView iv = new ImageView(ctx);
        iv.setImageResource(iconRes);
        if (iconRes == R.drawable.tool_border_email) {
            iv.setColorFilter(0xFF222222);
        }
        row.addView(iv, new LinearLayout.LayoutParams(dp(ctx, 24), dp(ctx, 24)));

        TextView tv = new TextView(ctx);
        tv.setText(title);
        tv.setTextSize(15);
        tv.setTypeface(Typeface.DEFAULT_BOLD);
        tv.setTextColor(0xFF222222);
        tv.setPadding(dp(ctx, 12), 0, 0, 0);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final boolean[] state = new boolean[]{initialChecked};
        final FrameLayout swWrap = new FrameLayout(ctx);
        swWrap.addView(createOriginalSwitch(ctx, state[0]));
        swWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                state[0] = !state[0];
                swWrap.removeAllViews();
                swWrap.addView(createOriginalSwitch(ctx, state[0]));
                if (onToggle != null) onToggle.run();
            }
        });
        row.addView(swWrap);
        parent.addView(row);
    }

    // =========================================================================================
    // 4. FULL SETTINGS DIALOG (new_application_setting_fragment.xml)
    // =========================================================================================
    public static void showFullSettingsDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF6F6F6);

        RelativeLayout topBar = new RelativeLayout(act);
        topBar.setBackgroundColor(0xFFFFFFFF);
        topBar.setPadding(dp(act, 12), 0, dp(act, 12), 0);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 52)));

        ImageView backIv = new ImageView(act);
        backIv.setImageResource(R.drawable.ic_back_ltr);
        RelativeLayout.LayoutParams bLp = new RelativeLayout.LayoutParams(dp(act, 28), dp(act, 28));
        bLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        bLp.addRule(RelativeLayout.CENTER_VERTICAL);
        backIv.setLayoutParams(bLp);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(backIv);

        TextView titleTv = new TextView(act);
        titleTv.setText("Camera Settings");
        titleTv.setTextSize(17);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        RelativeLayout.LayoutParams tLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        topBar.addView(titleTv, tLp);
        root.addView(topBar);

        LinearLayout card = new LinearLayout(act);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(rounded(act, 0xFFFFFFFF, 8, 0, 0));
        card.setPadding(dp(act, 16), 0, dp(act, 16), 0);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cLp.setMargins(dp(act, 16), dp(act, 16), dp(act, 16), 0);
        card.setLayoutParams(cLp);

        addDrawerToolSwitchRow(act, card, R.drawable.tool_border_gallery2, "Grid Lines", model.showGridLines, new Runnable() {
            @Override
            public void run() {
                model.showGridLines = !model.showGridLines;
                model.saveState(act);
                if (onUpdate != null) onUpdate.run();
            }
        });
        addDrawerToolSwitchRow(act, card, R.drawable.tool_border_watermark, "Leveler", model.showLeveler, new Runnable() {
            @Override
            public void run() {
                model.showLeveler = !model.showLeveler;
                model.saveState(act);
                if (onUpdate != null) onUpdate.run();
            }
        });
        addDrawerToolSwitchRow(act, card, R.drawable.tool_border_takephoto, "Shutter Sound", model.shutterSoundEnabled, new Runnable() {
            @Override
            public void run() {
                model.shutterSoundEnabled = !model.shutterSoundEnabled;
                model.saveState(act);
            }
        });
        addDrawerToolSwitchRow(act, card, R.drawable.tool_border_safe_green, "Photo Code (Side)", model.showPhotoCode, new Runnable() {
            @Override
            public void run() {
                model.showPhotoCode = !model.showPhotoCode;
                model.saveState(act);
                if (onUpdate != null) onUpdate.run();
            }
        });
        root.addView(card);

        dlg.setContentView(root);
        applyDialogSystemBarInsets(act, dlg, root, 0xFFFFFFFF, true);
        dlg.show();
    }

    public static void showEarnRoboticsDialog(final Activity act, final WatermarkModel model) {
        showEarnRoboticsDialog(act, model, null);
    }

    public static void showEarnRoboticsDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        showPurchaseDialog(act, model, onUpdate);
    }

    // =========================================================================================
    // 5. RATIO POPUP (ratio_setting_dialog_global.xml)
    // =========================================================================================
    public static void showRatioPopup(final Activity act, final WatermarkModel model, final Runnable onRatioChanged) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout row = new LinearLayout(act);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setBackground(rounded(act, 0xFFFFFFFF, 12, 0, 0));
        row.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

        final String[] labels = new String[]{"4:3", "16:9", "1:1", "Full"};
        final int[] icons = new int[]{
            R.drawable.ratio_pop_view_3_4, R.drawable.ratio_pop_view_9_16, R.drawable.ratio_pop_view_1_1, R.drawable.ratio_pop_view_full
        };
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            boolean sel = (model.aspectRatioMode == i);
            LinearLayout col = new LinearLayout(act);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER);
            col.setPadding(dp(act, 14), dp(act, 8), dp(act, 14), dp(act, 8));

            ImageView iv = new ImageView(act);
            iv.setImageResource(icons[i]);
            if (sel) iv.setColorFilter(0xFF0093FF);
            LinearLayout.LayoutParams ivLp = new LinearLayout.LayoutParams(dp(act, 28), dp(act, 28));
            ivLp.gravity = Gravity.CENTER_HORIZONTAL;
            col.addView(iv, ivLp);

            TextView tv = new TextView(act);
            tv.setText(labels[i]);
            tv.setTextSize(13);
            tv.setGravity(Gravity.CENTER);
            tv.setTypeface(sel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tv.setTextColor(sel ? 0xFF0093FF : 0xFF222222);
            tv.setPadding(0, dp(act, 4), 0, 0);
            LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvLp.gravity = Gravity.CENTER_HORIZONTAL;
            col.addView(tv, tvLp);

            col.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.aspectRatioMode = idx;
                    if (onRatioChanged != null) onRatioChanged.run();
                    dlg.dismiss();
                }
            });
            row.addView(col);
        }

        dlg.setContentView(row);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.CENTER);
        }
        dlg.show();
    }

    // =========================================================================================
    // 6. VERIFY CENTER DIALOG (1:1 layout_fragment_photo_verify.xml)
    // =========================================================================================
    public static void showVerifyCenterDialog(final Activity act, final WatermarkModel model) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF154978);

        // Top Bar
        RelativeLayout topBar = new RelativeLayout(act);
        topBar.setPadding(dp(act, 12), 0, dp(act, 12), 0);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 52)));

        ImageView backIv = new ImageView(act);
        backIv.setImageResource(R.drawable.ic_back_ltr);
        backIv.setColorFilter(0xFFFFFFFF);
        RelativeLayout.LayoutParams bLp = new RelativeLayout.LayoutParams(dp(act, 28), dp(act, 28));
        bLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        bLp.addRule(RelativeLayout.CENTER_VERTICAL);
        backIv.setLayoutParams(bLp);
        backIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(backIv);

        TextView titleTv = new TextView(act);
        titleTv.setText("Verify Center");
        titleTv.setTextSize(17);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFFFFFFFF);
        RelativeLayout.LayoutParams tLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        topBar.addView(titleTv, tLp);
        root.addView(topBar);

        ScrollView scroll = new ScrollView(act);
        LinearLayout body = new LinearLayout(act);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setGravity(Gravity.CENTER_HORIZONTAL);
        body.setPadding(dp(act, 20), dp(act, 12), dp(act, 20), dp(act, 24));

        ImageView heroIv = new ImageView(act);
        heroIv.setImageResource(R.drawable.verification_center_icon);
        heroIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        body.addView(heroIv, new LinearLayout.LayoutParams(dp(act, 220), dp(act, 150)));

        TextView h1 = new TextView(act);
        h1.setText("Photo Authenticity Verification");
        h1.setTextSize(20);
        h1.setTypeface(Typeface.DEFAULT_BOLD);
        h1.setTextColor(0xFFFFFFFF);
        h1.setGravity(Gravity.CENTER);
        h1.setPadding(0, dp(act, 8), 0, dp(act, 4));
        body.addView(h1);

        TextView sub = new TextView(act);
        sub.setText("Verify original time & location by Photo Code");
        sub.setTextSize(13);
        sub.setTextColor(0xCCFFFFFF);
        sub.setGravity(Gravity.CENTER);
        body.addView(sub);

        // White Card for Photo Code Verification
        LinearLayout card = new LinearLayout(act);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(rounded(act, 0xFFFFFFFF, 12, 0, 0));
        card.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cLp.setMargins(0, dp(act, 18), 0, 0);
        card.setLayoutParams(cLp);

        TextView codeLabel = new TextView(act);
        codeLabel.setText("Enter Photo Code");
        codeLabel.setTextSize(14);
        codeLabel.setTypeface(Typeface.DEFAULT_BOLD);
        codeLabel.setTextColor(0xFF222222);
        card.addView(codeLabel);

        final EditText codeEt = new EditText(act);
        codeEt.setText(model.currentPhotoCode);
        codeEt.setHint("Enter photo code...");
        codeEt.setTextSize(16);
        codeEt.setTextColor(0xFF222222);
        codeEt.setBackground(rounded(act, 0xFFF2F4F7, 8, 0, 0));
        codeEt.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));
        LinearLayout.LayoutParams ceLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ceLp.setMargins(0, dp(act, 8), 0, dp(act, 12));
        card.addView(codeEt, ceLp);

        final TextView resultTv = new TextView(act);
        resultTv.setTextSize(13);
        resultTv.setTextColor(0xFF166534);
        resultTv.setBackground(rounded(act, 0xFFF0FDF4, 8, 0, 0));
        resultTv.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));
        resultTv.setVisibility(View.GONE);
        card.addView(resultTv);

        TextView verifyBtn = new TextView(act);
        verifyBtn.setText("Verify Photo Code");
        verifyBtn.setTextSize(16);
        verifyBtn.setTypeface(Typeface.DEFAULT_BOLD);
        verifyBtn.setTextColor(0xFFFFFFFF);
        verifyBtn.setGravity(Gravity.CENTER);
        verifyBtn.setBackground(rounded(act, 0xFF0070F4, 8, 0, 0));
        LinearLayout.LayoutParams vbLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 46));
        vbLp.setMargins(0, dp(act, 8), 0, 0);
        verifyBtn.setLayoutParams(vbLp);
        verifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeEt.getText().toString().trim();
                File latestFile = model.capturedPhotos.isEmpty() ? null : model.capturedPhotos.get(0).compositeFile;
                String trailerInfo = WatermarkModel.inspectJpegPackerTrailer(latestFile);
                resultTv.setVisibility(View.VISIBLE);
                resultTv.setText("✓ 100% True Time & GPS Verified\nPhoto Code: " + code
                        + "\nTime: " + model.getFormattedTime() + ", " + model.getFormattedDateShort()
                        + "\nGPS: " + model.getFormattedCoordinates()
                        + "\nAddress: " + model.addressLine
                        + "\nContainer: " + trailerInfo);
            }
        });
        card.addView(verifyBtn);
        body.addView(card);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        dlg.setContentView(root);
        applyDialogSystemBarInsets(act, dlg, root, 0xFF154978, false);
        dlg.show();
    }

    public static void showExportFileInspectorDialog(final Activity act, final String formatName, final File outFile) {
        if (outFile == null || !outFile.exists()) {
            Toast.makeText(act, "Export failed", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(act, "Exported " + formatName + ": " + outFile.getName(), Toast.LENGTH_SHORT).show();
    }

    // =========================================================================================
    // 7. TEAMSPACE HOME DIALOG (1:1 fragment_layout_teamspace_home.xml)
    // =========================================================================================
    public static void showTeamspaceDialog(final Activity act, final WatermarkModel model) {
        showTeamspaceDialog(act, model, null);
    }

    public static void showTeamspaceDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout frameRoot = new FrameLayout(act);
        frameRoot.setBackgroundColor(0xFFFFFFFF);

        LinearLayout mainCol = new LinearLayout(act);
        mainCol.setOrientation(LinearLayout.VERTICAL);
        mainCol.setBackgroundColor(0xFFFFFFFF);

        // Top Bar (height 54dp: menuIv + "Teamspace" + ivWebPortal + ivClose)
        RelativeLayout topBar = new RelativeLayout(act);
        topBar.setBackgroundColor(0xFFFFFFFF);
        topBar.setPadding(dp(act, 16), 0, dp(act, 16), 0);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 54)));

        LinearLayout leftTitleRow = new LinearLayout(act);
        leftTitleRow.setOrientation(LinearLayout.HORIZONTAL);
        leftTitleRow.setGravity(Gravity.CENTER_VERTICAL);
        RelativeLayout.LayoutParams ltLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ltLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        ltLp.addRule(RelativeLayout.CENTER_VERTICAL);
        leftTitleRow.setLayoutParams(ltLp);

        ImageView menuIv = new ImageView(act);
        menuIv.setImageResource(R.drawable.home_tool_border_menu);
        leftTitleRow.addView(menuIv, new LinearLayout.LayoutParams(dp(act, 24), dp(act, 24)));

        TextView titleTv = new TextView(act);
        titleTv.setText("Teamspace");
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        titleTv.setPadding(dp(act, 10), 0, 0, 0);
        leftTitleRow.addView(titleTv);
        topBar.addView(leftTitleRow);

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.global_border_close_strong);
        RelativeLayout.LayoutParams cLp = new RelativeLayout.LayoutParams(dp(act, 24), dp(act, 24));
        cLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        cLp.addRule(RelativeLayout.CENTER_VERTICAL);
        closeIv.setLayoutParams(cLp);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(closeIv);
        mainCol.addView(topBar);

        View div = new View(act);
        div.setBackgroundColor(0xFFEFEFEF);
        mainCol.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        ScrollView scroll = new ScrollView(act);
        LinearLayout content = new LinearLayout(act);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 90));

        // Auto-save to Teamspace Banner (#E8F2FF radius 8dp)
        LinearLayout autoSaveBar = new LinearLayout(act);
        autoSaveBar.setOrientation(LinearLayout.HORIZONTAL);
        autoSaveBar.setGravity(Gravity.CENTER_VERTICAL);
        autoSaveBar.setBackground(rounded(act, 0xFFE8F2FF, 8, 0, 0));
        autoSaveBar.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));

        ImageView tsPhotoIv = new ImageView(act);
        tsPhotoIv.setImageResource(R.drawable.image_teamspace_photo);
        autoSaveBar.addView(tsPhotoIv, new LinearLayout.LayoutParams(dp(act, 42), dp(act, 42)));

        TextView asTv = new TextView(act);
        asTv.setText("Auto-save to Teamspace");
        asTv.setTextSize(14);
        asTv.setTypeface(Typeface.DEFAULT_BOLD);
        asTv.setTextColor(0xFF333333);
        asTv.setPadding(dp(act, 12), 0, dp(act, 8), 0);
        autoSaveBar.addView(asTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final FrameLayout asSwWrap = new FrameLayout(act);
        asSwWrap.addView(createOriginalSwitch(act, model.autoSaveTeamspace));
        asSwWrap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.autoSaveTeamspace = !model.autoSaveTeamspace;
                model.saveState(act);
                asSwWrap.removeAllViews();
                asSwWrap.addView(createOriginalSwitch(act, model.autoSaveTeamspace));
            }
        });
        autoSaveBar.addView(asSwWrap);
        content.addView(autoSaveBar);

        // Members Section
        RelativeLayout memHeader = new RelativeLayout(act);
        memHeader.setPadding(0, dp(act, 18), 0, dp(act, 8));
        TextView memLabel = new TextView(act);
        memLabel.setText("Members (" + model.teamMembers.size() + ")");
        memLabel.setTextSize(13);
        memLabel.setTypeface(Typeface.DEFAULT_BOLD);
        memLabel.setTextColor(0xFF555555);
        memHeader.addView(memLabel);
        content.addView(memHeader);

        for (int i = 0; i < model.teamMembers.size(); i++) {
            LinearLayout mRow = new LinearLayout(act);
            mRow.setOrientation(LinearLayout.HORIZONTAL);
            mRow.setGravity(Gravity.CENTER_VERTICAL);
            mRow.setPadding(0, dp(act, 6), 0, dp(act, 6));
            ImageView uIv = new ImageView(act);
            uIv.setImageResource(R.drawable.tool_border_user);
            mRow.addView(uIv, new LinearLayout.LayoutParams(dp(act, 24), dp(act, 24)));
            TextView mTv = new TextView(act);
            mTv.setText(model.teamMembers.get(i));
            mTv.setTextSize(14);
            mTv.setTextColor(0xFF222222);
            mTv.setPadding(dp(act, 10), 0, 0, 0);
            mRow.addView(mTv);
            content.addView(mRow);
        }

        // Projects Section
        TextView projLabel = new TextView(act);
        projLabel.setText("Projects (" + model.projects.size() + ")");
        projLabel.setTextSize(13);
        projLabel.setTypeface(Typeface.DEFAULT_BOLD);
        projLabel.setTextColor(0xFF555555);
        projLabel.setPadding(0, dp(act, 16), 0, dp(act, 8));
        content.addView(projLabel);

        for (int i = 0; i < model.projects.size(); i++) {
            final WatermarkModel.ProjectItem p = model.projects.get(i);
            LinearLayout pRow = new LinearLayout(act);
            pRow.setOrientation(LinearLayout.HORIZONTAL);
            pRow.setGravity(Gravity.CENTER_VERTICAL);
            pRow.setBackground(rounded(act, 0xFFF7F8FA, 8, 0, 0));
            pRow.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));
            LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            pLp.setMargins(0, 0, 0, dp(act, 6));
            pRow.setLayoutParams(pLp);

            ImageView fIv = new ImageView(act);
            fIv.setImageResource(R.drawable.icon_preview_folder);
            pRow.addView(fIv, new LinearLayout.LayoutParams(dp(act, 24), dp(act, 24)));

            TextView pTv = new TextView(act);
            pTv.setText(p.name + "  •  " + p.photoCount + " photos");
            pTv.setTextSize(14);
            pTv.setTypeface(Typeface.DEFAULT_BOLD);
            pTv.setTextColor(0xFF222222);
            pTv.setPadding(dp(act, 10), 0, 0, 0);
            pRow.addView(pTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            content.addView(pRow);
        }

        // All Photos + Export Section
        TextView allPhotosLabel = new TextView(act);
        allPhotosLabel.setText("All Photos (" + model.capturedPhotos.size() + ")");
        allPhotosLabel.setTextSize(16);
        allPhotosLabel.setTypeface(Typeface.DEFAULT_BOLD);
        allPhotosLabel.setTextColor(0xFF222222);
        allPhotosLabel.setPadding(0, dp(act, 16), 0, dp(act, 8));
        content.addView(allPhotosLabel);

        LinearLayout exportRow = new LinearLayout(act);
        exportRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] expNames = new String[]{"Export PDF", "Export Excel", "Export KMZ"};
        for (int i = 0; i < expNames.length; i++) {
            final int idx = i;
            TextView eb = new TextView(act);
            eb.setText(expNames[i]);
            eb.setTextSize(12);
            eb.setTypeface(Typeface.DEFAULT_BOLD);
            eb.setTextColor(0xFF0070F4);
            eb.setGravity(Gravity.CENTER);
            eb.setBackground(rounded(act, 0xFFE8F2FF, 6, 0, 0));
            eb.setPadding(dp(act, 10), dp(act, 8), dp(act, 10), dp(act, 8));
            LinearLayout.LayoutParams eLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            if (i < 2) eLp.setMargins(0, 0, dp(act, 6), 0);
            eb.setLayoutParams(eLp);
            eb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File out = (idx == 0) ? model.exportPdfWorkReport(act)
                            : ((idx == 1) ? model.exportExcelSpreadsheet(act) : model.exportKmzGoogleEarth(act));
                    showExportFileInspectorDialog(act, expNames[idx], out);
                }
            });
            exportRow.addView(eb);
        }
        content.addView(exportRow);

        scroll.addView(content);
        mainCol.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        frameRoot.addView(mainCol);

        // Floating Bottom-Right Camera Button (teamspace_shoot)
        ImageView shootFab = new ImageView(act);
        shootFab.setImageResource(R.drawable.teamspace_shoot);
        FrameLayout.LayoutParams fabLp = new FrameLayout.LayoutParams(dp(act, 56), dp(act, 56));
        fabLp.gravity = Gravity.BOTTOM | Gravity.END;
        fabLp.setMargins(0, 0, dp(act, 20), dp(act, 24));
        shootFab.setLayoutParams(fabLp);
        shootFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        frameRoot.addView(shootFab);

        dlg.setContentView(frameRoot);
        applyDialogSystemBarInsets(act, dlg, frameRoot, 0xFFFFFFFF, true);
        dlg.show();
    }

    // =========================================================================================
    // 8. LOCAL PHOTO & VIDEO PREVIEW DIALOG (1:1 fragment_local_pic_preview.xml + item_pic_preview.xml)
    //    Bottom 80dp white bar with 5 exact centered columns: WhatsApp | More | Share | Note | Delete
    // =========================================================================================
    public static void showLocalPreviewDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        showLocalPreviewDialog(act, model, onUpdate, 0);
    }

    public static void showLocalPreviewDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate, int initialIndex) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFFFFF);

        final int[] curIdx = new int[]{Math.max(0, Math.min(initialIndex, Math.max(0, model.capturedPhotos.size() - 1)))};
        final Handler animHandler = new Handler(Looper.getMainLooper());
        final boolean[] isPlayingVideo = new boolean[]{false};
        final int[] frameCursor = new int[]{0};

        // Top Bar (height 52dp, #FFFFFF: ivClose + title + "Multi-Share")
        RelativeLayout topBar = new RelativeLayout(act);
        topBar.setBackgroundColor(0xFFFFFFFF);
        topBar.setPadding(dp(act, 10), 0, dp(act, 16), 0);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 52)));

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.global_border_close_ultrastrong);
        closeIv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        closeIv.setPadding(dp(act, 6), dp(act, 6), dp(act, 6), dp(act, 6));
        RelativeLayout.LayoutParams cLp = new RelativeLayout.LayoutParams(dp(act, 38), dp(act, 38));
        cLp.addRule(RelativeLayout.ALIGN_PARENT_START);
        cLp.addRule(RelativeLayout.CENTER_VERTICAL);
        closeIv.setLayoutParams(cLp);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isPlayingVideo[0] = false;
                animHandler.removeCallbacksAndMessages(null);
                dlg.dismiss();
            }
        });
        topBar.addView(closeIv);

        final TextView titleTv = new TextView(act);
        titleTv.setTextSize(17);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF111111);
        titleTv.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams tLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.addRule(RelativeLayout.CENTER_IN_PARENT);
        topBar.addView(titleTv, tLp);

        TextView multiShareTv = new TextView(act);
        multiShareTv.setText("Multi-Share");
        multiShareTv.setTextSize(15.5f);
        multiShareTv.setTypeface(Typeface.DEFAULT_BOLD);
        multiShareTv.setTextColor(0xFF0070F4);
        multiShareTv.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams msLp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        msLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        msLp.addRule(RelativeLayout.CENTER_VERTICAL);
        multiShareTv.setLayoutParams(msLp);
        multiShareTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_TEXT, "Timemark Verified Photo Code: " + model.currentPhotoCode);
                act.startActivity(Intent.createChooser(sendIntent, "Multi-Share"));
            }
        });
        topBar.addView(multiShareTv);
        root.addView(topBar);

        View topDiv = new View(act);
        topDiv.setBackgroundColor(0xFFEFEFEF);
        root.addView(topDiv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // Center Media Preview Container (item_pic_preview.xml)
        FrameLayout centerFrame = new FrameLayout(act);
        centerFrame.setBackgroundColor(0xFFF5F6F8);
        centerFrame.setPadding(dp(act, 8), dp(act, 8), dp(act, 8), dp(act, 8));
        centerFrame.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        final ImageView photoIv = new ImageView(act);
        photoIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        FrameLayout.LayoutParams pIvLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pIvLp.gravity = Gravity.CENTER;
        centerFrame.addView(photoIv, pIvLp);

        // Video Badge Pill (top-left when viewing a recorded video)
        final TextView videoBadgeTv = new TextView(act);
        videoBadgeTv.setTextSize(12);
        videoBadgeTv.setTypeface(Typeface.DEFAULT_BOLD);
        videoBadgeTv.setTextColor(0xFFFFFFFF);
        videoBadgeTv.setBackground(rounded(act, 0xAA000000, 14, dp(act, 1), 0x44FFFFFF));
        videoBadgeTv.setPadding(dp(act, 12), dp(act, 4), dp(act, 12), dp(act, 4));
        videoBadgeTv.setVisibility(View.GONE);
        FrameLayout.LayoutParams vbLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        vbLp.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        vbLp.setMargins(0, dp(act, 12), 0, 0);
        centerFrame.addView(videoBadgeTv, vbLp);

        // Center Video Play/Pause Overlay Button
        final TextView playBtnTv = new TextView(act);
        playBtnTv.setText("▶");
        playBtnTv.setTextSize(24);
        playBtnTv.setTypeface(Typeface.DEFAULT_BOLD);
        playBtnTv.setTextColor(0xFFFFFFFF);
        playBtnTv.setGravity(Gravity.CENTER);
        playBtnTv.setBackground(rounded(act, 0x99000000, 32, dp(act, 2), 0xFFFFFFFF));
        playBtnTv.setVisibility(View.GONE);
        FrameLayout.LayoutParams pbLp = new FrameLayout.LayoutParams(dp(act, 64), dp(act, 64));
        pbLp.gravity = Gravity.CENTER;
        centerFrame.addView(playBtnTv, pbLp);

        // Left / Right Navigation Arrows for Multiple Photos/Videos
        final TextView prevArrowTv = new TextView(act);
        prevArrowTv.setText("‹");
        prevArrowTv.setTextSize(26);
        prevArrowTv.setTypeface(Typeface.DEFAULT_BOLD);
        prevArrowTv.setTextColor(0xFFFFFFFF);
        prevArrowTv.setGravity(Gravity.CENTER);
        prevArrowTv.setIncludeFontPadding(false);
        prevArrowTv.setBackground(rounded(act, 0x66000000, 20, 0, 0));
        FrameLayout.LayoutParams paLp = new FrameLayout.LayoutParams(dp(act, 38), dp(act, 38));
        paLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        paLp.setMargins(dp(act, 6), 0, 0, 0);
        centerFrame.addView(prevArrowTv, paLp);

        final TextView nextArrowTv = new TextView(act);
        nextArrowTv.setText("›");
        nextArrowTv.setTextSize(26);
        nextArrowTv.setTypeface(Typeface.DEFAULT_BOLD);
        nextArrowTv.setTextColor(0xFFFFFFFF);
        nextArrowTv.setGravity(Gravity.CENTER);
        nextArrowTv.setIncludeFontPadding(false);
        nextArrowTv.setBackground(rounded(act, 0x66000000, 20, 0, 0));
        FrameLayout.LayoutParams naLp = new FrameLayout.LayoutParams(dp(act, 38), dp(act, 38));
        naLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        naLp.setMargins(0, 0, dp(act, 6), 0);
        centerFrame.addView(nextArrowTv, naLp);

        final TextView emptyTv = new TextView(act);
        emptyTv.setText("No photos yet. Take a photo first.");
        emptyTv.setTextSize(15);
        emptyTv.setTextColor(0xFF757575);
        emptyTv.setGravity(Gravity.CENTER);
        centerFrame.addView(emptyTv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final Runnable videoFrameStepper = new Runnable() {
            @Override
            public void run() {
                if (!isPlayingVideo[0] || model.capturedPhotos.isEmpty()) return;
                WatermarkModel.CapturedPhoto item = model.capturedPhotos.get(curIdx[0]);
                if (!item.isVideo || item.videoFrames == null || item.videoFrames.isEmpty()) {
                    isPlayingVideo[0] = false;
                    playBtnTv.setText("▶");
                    playBtnTv.setAlpha(1f);
                    return;
                }
                Bitmap f = item.videoFrames.get(frameCursor[0] % item.videoFrames.size());
                if (f != null) photoIv.setImageBitmap(f);
                frameCursor[0]++;
                if (frameCursor[0] >= item.videoFrames.size()) {
                    frameCursor[0] = 0;
                    isPlayingVideo[0] = false;
                    playBtnTv.setText("▶");
                    playBtnTv.setAlpha(1f);
                } else {
                    animHandler.postDelayed(this, 140);
                }
            }
        };

        final Runnable updatePhotoView = new Runnable() {
            @Override
            public void run() {
                isPlayingVideo[0] = false;
                animHandler.removeCallbacksAndMessages(null);
                if (model.capturedPhotos.isEmpty()) {
                    titleTv.setText("Preview");
                    photoIv.setImageDrawable(null);
                    videoBadgeTv.setVisibility(View.GONE);
                    playBtnTv.setVisibility(View.GONE);
                    prevArrowTv.setVisibility(View.GONE);
                    nextArrowTv.setVisibility(View.GONE);
                    emptyTv.setVisibility(View.VISIBLE);
                    return;
                }
                emptyTv.setVisibility(View.GONE);
                int total = model.capturedPhotos.size();
                curIdx[0] = Math.max(0, Math.min(curIdx[0], total - 1));
                WatermarkModel.CapturedPhoto item = model.capturedPhotos.get(curIdx[0]);
                titleTv.setText((curIdx[0] + 1) + "/" + total);

                prevArrowTv.setVisibility(total > 1 && curIdx[0] > 0 ? View.VISIBLE : View.GONE);
                nextArrowTv.setVisibility(total > 1 && curIdx[0] < total - 1 ? View.VISIBLE : View.GONE);

                if (item.previewBitmap != null) {
                    photoIv.setImageBitmap(item.previewBitmap);
                } else if (item.compositeFile != null && item.compositeFile.exists()) {
                    photoIv.setImageBitmap(BitmapFactory.decodeFile(item.compositeFile.getAbsolutePath()));
                }

                if (item.isVideo) {
                    videoBadgeTv.setVisibility(View.VISIBLE);
                    videoBadgeTv.setText(String.format(Locale.US, "● VIDEO  00:%02d", Math.max(1, item.durationSec)));
                    playBtnTv.setVisibility(View.VISIBLE);
                    playBtnTv.setText("▶");
                    playBtnTv.setAlpha(1f);
                } else {
                    videoBadgeTv.setVisibility(View.GONE);
                    playBtnTv.setVisibility(View.GONE);
                }
            }
        };

        View.OnClickListener toggleVideoPlay = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.capturedPhotos.isEmpty()) return;
                WatermarkModel.CapturedPhoto item = model.capturedPhotos.get(curIdx[0]);
                if (!item.isVideo) return;
                if (isPlayingVideo[0]) {
                    isPlayingVideo[0] = false;
                    animHandler.removeCallbacksAndMessages(null);
                    playBtnTv.setText("▶");
                    playBtnTv.setAlpha(1f);
                } else {
                    isPlayingVideo[0] = true;
                    frameCursor[0] = 0;
                    playBtnTv.setText("❚❚");
                    playBtnTv.setAlpha(0.45f);
                    animHandler.post(videoFrameStepper);
                }
            }
        };
        playBtnTv.setOnClickListener(toggleVideoPlay);
        photoIv.setOnClickListener(toggleVideoPlay);

        prevArrowTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curIdx[0] > 0) {
                    curIdx[0]--;
                    updatePhotoView.run();
                }
            }
        });
        nextArrowTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (curIdx[0] < model.capturedPhotos.size() - 1) {
                    curIdx[0]++;
                    updatePhotoView.run();
                }
            }
        });

        updatePhotoView.run();
        root.addView(centerFrame);

        View botDiv = new View(act);
        botDiv.setBackgroundColor(0xFFEFEFEF);
        root.addView(botDiv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // Bottom Bar (bottomLayoutGlobalNew: height 80dp, #FFFFFF, 5 centered columns)
        LinearLayout bottomBar = new LinearLayout(act);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setBackgroundColor(0xFFFFFFFF);
        bottomBar.setPadding(dp(act, 10), dp(act, 4), dp(act, 10), dp(act, 4));
        bottomBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 80)));

        int[] botIcons = new int[]{
            R.drawable.social_share_wa_fill,
            R.drawable.tool_border_more2,
            R.drawable.upload2,
            R.drawable.tool_border_edit,
            R.drawable.del
        };
        String[] botLabels = new String[]{"WhatsApp", "More", "Share", "Note", "Delete"};
        for (int i = 0; i < 5; i++) {
            final int actionIdx = i;
            LinearLayout col = new LinearLayout(act);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setGravity(Gravity.CENTER);
            col.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));

            FrameLayout iconWrap = new FrameLayout(act);
            ImageView iv = new ImageView(act);
            iv.setImageResource(botIcons[i]);
            iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
            FrameLayout.LayoutParams ivLp = new FrameLayout.LayoutParams(dp(act, 28), dp(act, 28));
            ivLp.gravity = Gravity.CENTER;
            iconWrap.addView(iv, ivLp);

            if (i == 1) {
                View moreDot = new View(act);
                moreDot.setBackground(rounded(act, 0xFF0093FF, 4.5f, dp(act, 1), 0xFFFFFFFF));
                FrameLayout.LayoutParams mdLp = new FrameLayout.LayoutParams(dp(act, 9), dp(act, 9));
                mdLp.gravity = Gravity.TOP | Gravity.END;
                iconWrap.addView(moreDot, mdLp);
            }

            LinearLayout.LayoutParams iwLp = new LinearLayout.LayoutParams(dp(act, 34), dp(act, 32));
            iwLp.gravity = Gravity.CENTER_HORIZONTAL;
            col.addView(iconWrap, iwLp);

            TextView tv = new TextView(act);
            tv.setText(botLabels[i]);
            tv.setSingleLine(true);
            tv.setGravity(Gravity.CENTER);
            tv.setTextSize(11.5f);
            tv.setTextColor(0xFF222222);
            LinearLayout.LayoutParams tvLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tvLp.gravity = Gravity.CENTER_HORIZONTAL;
            tvLp.setMargins(0, dp(act, 4), 0, 0);
            col.addView(tv, tvLp);

            col.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (model.capturedPhotos.isEmpty()) return;
                    final WatermarkModel.CapturedPhoto cur = model.capturedPhotos.get(curIdx[0]);
                    if (actionIdx == 0 || actionIdx == 2) {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Verified Photo Code: " + cur.photoCode + " • " + cur.timestampText);
                        act.startActivity(Intent.createChooser(sendIntent, "Share Photo"));
                    } else if (actionIdx == 1) {
                        File pdf = model.exportPdfWorkReport(act);
                        showExportFileInspectorDialog(act, "PDF Report", pdf);
                    } else if (actionIdx == 3) {
                        showOriginalFieldInputDialog(act, "Note", cur.noteText, model.recentNotes, new ValueCallback() {
                            @Override
                            public void onSave(String newVal) {
                                cur.noteText = newVal;
                                model.customNote = newVal;
                                model.saveState(act);
                                if (onUpdate != null) onUpdate.run();
                            }
                        });
                    } else if (actionIdx == 4) {
                        model.capturedPhotos.remove(curIdx[0]);
                        updatePhotoView.run();
                    }
                }
            });
            bottomBar.addView(col);
        }
        root.addView(bottomBar);

        dlg.setContentView(root);
        applyDialogSystemBarInsets(act, dlg, root, 0xFFFFFFFF, true);
        dlg.show();
    }

    // =========================================================================================
    // 9. TIMEMARK PLANS / PLUS PAYWALL DIALOG
    // =========================================================================================
    public static void showPurchaseDialog(final Activity act) {
        showPurchaseDialog(act, null, null);
    }

    public static void showPurchaseDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(topRounded(act, 0xFFFFFFFF, 16));
        root.setPadding(dp(act, 20), dp(act, 18), dp(act, 20), dp(act, 24));

        RelativeLayout topBar = new RelativeLayout(act);
        topBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 36)));
        TextView titleTv = new TextView(act);
        titleTv.setText("Timemark Plus");
        titleTv.setTextSize(20);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        topBar.addView(titleTv);

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.btn_close_on_light);
        RelativeLayout.LayoutParams cLp = new RelativeLayout.LayoutParams(dp(act, 26), dp(act, 26));
        cLp.addRule(RelativeLayout.ALIGN_PARENT_END);
        closeIv.setLayoutParams(cLp);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(closeIv);
        root.addView(topBar);

        String[] perks = new String[]{
            "✓ Remove all ads & unlock all watermark templates",
            "✓ Custom company logo & unlimited cloud backup",
            "✓ Export PDF Work Reports, Excel & KMZ maps"
        };
        for (int i = 0; i < perks.length; i++) {
            TextView pTv = new TextView(act);
            pTv.setText(perks[i]);
            pTv.setTextSize(14);
            pTv.setTextColor(0xFF444444);
            pTv.setPadding(0, dp(act, 8), 0, 0);
            root.addView(pTv);
        }

        TextView activateBtn = new TextView(act);
        activateBtn.setText((model != null && model.isVipPlus) ? "Plus Active ✓" : "Continue");
        activateBtn.setTextSize(16);
        activateBtn.setTypeface(Typeface.DEFAULT_BOLD);
        activateBtn.setTextColor(0xFFFFFFFF);
        activateBtn.setGravity(Gravity.CENTER);
        activateBtn.setBackground(rounded(act, 0xFF7A6EFF, 8, 0, 0));
        LinearLayout.LayoutParams abLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 48));
        abLp.setMargins(0, dp(act, 20), 0, 0);
        activateBtn.setLayoutParams(abLp);
        activateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model != null) {
                    model.isVipPlus = true;
                    model.saveState(act);
                }
                if (onUpdate != null) onUpdate.run();
                dlg.dismiss();
            }
        });
        root.addView(activateBtn);

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(0x00000000));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
        }
        dlg.show();
    }

    public static void showPaywallExitSurveyDialog(final Activity act) {}
    public static void showPaywallExitSurveyDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate) {}
}
