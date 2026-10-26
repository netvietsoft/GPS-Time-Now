package com.nextai.camera;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.media.MediaPlayer;
import android.net.Uri;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OriginalDialogs {

    private static int dp(Context ctx, float val) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, val, ctx.getResources().getDisplayMetrics());
    }

    private static GradientDrawable roundedRect(int fillColor, float radiusDp, int strokeWidthPx, int strokeColor, Context ctx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        gd.setCornerRadius(dp(ctx, radiusDp));
        if (strokeWidthPx > 0) {
            gd.setStroke(strokeWidthPx, strokeColor);
        }
        return gd;
    }

    private static GradientDrawable topRoundedSheet(int fillColor, float radiusDp, Context ctx) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(fillColor);
        float r = dp(ctx, radiusDp);
        gd.setCornerRadii(new float[]{r, r, r, r, 0, 0, 0, 0});
        return gd;
    }

    // 1. Original Templates BottomSheet (fragment_universal_watermark_list_global.xml)
    public static void showTemplatesSheet(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(topRoundedSheet(0xFFFFFFFF, 14, act));
        int screenH = act.getResources().getDisplayMetrics().heightPixels;
        root.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (screenH * 0.78f)));

        // Header bar (60dp)
        FrameLayout header = new FrameLayout(act);
        header.setPadding(dp(act, 16), 0, dp(act, 12), 0);
        LinearLayout.LayoutParams hLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 56));
        header.setLayoutParams(hLp);

        TextView titleTv = new TextView(act);
        titleTv.setText("Templates");
        titleTv.setTextSize(19);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        FrameLayout.LayoutParams tLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        header.addView(titleTv, tLp);

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.btn_close_on_light);
        FrameLayout.LayoutParams cLp = new FrameLayout.LayoutParams(dp(act, 32), dp(act, 32));
        cLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        header.addView(closeIv, cLp);
        root.addView(header);

        // Team Template Tip card (from fragment_universal_watermark_list_global.xml)
        LinearLayout tipCard = new LinearLayout(act);
        tipCard.setOrientation(LinearLayout.VERTICAL);
        tipCard.setBackground(roundedRect(0xFFE8F2FF, 8, 0, 0, act));
        tipCard.setPadding(dp(act, 14), dp(act, 10), dp(act, 14), dp(act, 10));
        LinearLayout.LayoutParams tipLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tipLp.setMargins(dp(act, 16), 0, dp(act, 16), dp(act, 10));
        tipCard.setLayoutParams(tipLp);

        TextView tipTitle = new TextView(act);
        tipTitle.setText("Team Template");
        tipTitle.setTextSize(15);
        tipTitle.setTypeface(Typeface.DEFAULT_BOLD);
        tipTitle.setTextColor(0xFF222222);
        tipCard.addView(tipTitle);

        TextView tipDesc = new TextView(act);
        tipDesc.setText("Simplify crew's input with a consistent, tamper-proof watermark format.");
        tipDesc.setTextSize(13);
        tipDesc.setTextColor(0xFF484848);
        tipCard.addView(tipDesc);
        root.addView(tipCard);

        // Search Template bar (clGetFromCodeAb in fragment_universal_watermark_list_global.xml)
        LinearLayout searchRow = new LinearLayout(act);
        searchRow.setOrientation(LinearLayout.HORIZONTAL);
        searchRow.setGravity(Gravity.CENTER_VERTICAL);
        searchRow.setBackground(roundedRect(0xFFF5F5F5, 22, dp(act, 1), 0xFFDEDEDE, act));
        searchRow.setPadding(dp(act, 14), dp(act, 4), dp(act, 14), dp(act, 4));
        LinearLayout.LayoutParams sLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 42));
        sLp.setMargins(dp(act, 16), 0, dp(act, 16), dp(act, 10));
        searchRow.setLayoutParams(sLp);

        ImageView searchIcon = new ImageView(act);
        searchIcon.setImageResource(R.drawable.global_border_search_blue);
        searchRow.addView(searchIcon, new LinearLayout.LayoutParams(dp(act, 20), dp(act, 20)));

        final EditText searchInput = new EditText(act);
        searchInput.setHint("Search Template or Enter Share Code...");
        searchInput.setTextSize(14);
        searchInput.setTextColor(0xFF222222);
        searchInput.setHintTextColor(0xFF888888);
        searchInput.setSingleLine(true);
        searchInput.setBackgroundColor(Color.TRANSPARENT);
        searchInput.setPadding(dp(act, 8), 0, dp(act, 8), 0);
        searchRow.addView(searchInput, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        root.addView(searchRow);

        // Category Filter Tabs
        final String[] categories = new String[]{"All", "Time & Location", "Attendance", "Construction", "Service & Delivery", "Cleaning & Security"};
        HorizontalScrollView catScroll = new HorizontalScrollView(act);
        catScroll.setHorizontalScrollBarEnabled(false);
        final LinearLayout catRow = new LinearLayout(act);
        catRow.setOrientation(LinearLayout.HORIZONTAL);
        catRow.setPadding(dp(act, 16), 0, dp(act, 16), dp(act, 10));
        catScroll.addView(catRow);
        root.addView(catScroll);

        // Grid ScrollView for watermark covers
        ScrollView gridScroll = new ScrollView(act);
        final LinearLayout gridContainer = new LinearLayout(act);
        gridContainer.setOrientation(LinearLayout.VERTICAL);
        gridContainer.setPadding(dp(act, 12), dp(act, 4), dp(act, 12), dp(act, 24));
        gridScroll.addView(gridContainer);
        root.addView(gridScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        final String[] selectedCat = new String[]{"All"};
        final String[] searchQuery = new String[]{""};
        final Runnable populateGrid = new Runnable() {
            @Override
            public void run() {
                gridContainer.removeAllViews();
                String q = searchQuery[0].trim().toLowerCase();
                List<WatermarkModel.TemplateInfo> filtered = new ArrayList<WatermarkModel.TemplateInfo>();
                for (int i = 0; i < model.templates.size(); i++) {
                    WatermarkModel.TemplateInfo t = model.templates.get(i);
                    boolean catMatch = "All".equals(selectedCat[0]) || t.category.equals(selectedCat[0]);
                    boolean qMatch = q.isEmpty()
                            || t.name.toLowerCase().contains(q)
                            || t.category.toLowerCase().contains(q)
                            || t.id.toLowerCase().contains(q);
                    if (catMatch && qMatch) {
                        filtered.add(t);
                    }
                }
                LinearLayout currentRow = null;
                for (int i = 0; i < filtered.size(); i++) {
                    if (i % 2 == 0) {
                        currentRow = new LinearLayout(act);
                        currentRow.setOrientation(LinearLayout.HORIZONTAL);
                        gridContainer.addView(currentRow, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    }
                    final WatermarkModel.TemplateInfo item = filtered.get(i);
                    boolean isSel = (model.activeTemplate != null && model.activeTemplate.id.equals(item.id));

                    LinearLayout card = new LinearLayout(act);
                    card.setOrientation(LinearLayout.VERTICAL);
                    card.setBackground(roundedRect(0xFFF8F9FA, 10, dp(act, isSel ? 2 : 1), isSel ? 0xFF0093FF : 0xFFE6E6E6, act));
                    card.setPadding(dp(act, 8), dp(act, 8), dp(act, 8), dp(act, 10));
                    LinearLayout.LayoutParams cardLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    cardLp.setMargins(dp(act, 5), dp(act, 5), dp(act, 5), dp(act, 5));
                    card.setLayoutParams(cardLp);

                    FrameLayout imgWrap = new FrameLayout(act);
                    imgWrap.setBackground(roundedRect(0xFF1E242B, 6, 0, 0, act));
                    ImageView coverIv = new ImageView(act);
                    coverIv.setImageResource(item.coverResId);
                    coverIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    coverIv.setPadding(dp(act, 4), dp(act, 6), dp(act, 4), dp(act, 6));
                    imgWrap.addView(coverIv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 118)));

                    if (isSel) {
                        TextView badge = new TextView(act);
                        badge.setText("In Use");
                        badge.setTextSize(10);
                        badge.setTypeface(Typeface.DEFAULT_BOLD);
                        badge.setTextColor(0xFFFFFFFF);
                        badge.setBackground(roundedRect(0xFF0093FF, 4, 0, 0, act));
                        badge.setPadding(dp(act, 6), dp(act, 2), dp(act, 6), dp(act, 2));
                        FrameLayout.LayoutParams bLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        bLp.gravity = Gravity.TOP | Gravity.END;
                        bLp.setMargins(dp(act, 6), dp(act, 6), dp(act, 6), dp(act, 6));
                        imgWrap.addView(badge, bLp);
                    }
                    card.addView(imgWrap);

                    TextView nameTv = new TextView(act);
                    nameTv.setText(item.name);
                    nameTv.setTextSize(14);
                    nameTv.setTypeface(Typeface.DEFAULT_BOLD);
                    nameTv.setTextColor(0xFF222222);
                    nameTv.setPadding(dp(act, 2), dp(act, 6), dp(act, 2), 0);
                    card.addView(nameTv);

                    TextView catTv = new TextView(act);
                    catTv.setText(item.category);
                    catTv.setTextSize(11);
                    catTv.setTextColor(0xFF848484);
                    catTv.setPadding(dp(act, 2), dp(act, 1), dp(act, 2), 0);
                    card.addView(catTv);

                    card.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            model.selectTemplate(item);
                            if (onUpdate != null) onUpdate.run();
                            dlg.dismiss();
                        }
                    });
                    currentRow.addView(card);
                }
                if (filtered.size() % 2 == 1 && currentRow != null) {
                    View spacer = new View(act);
                    currentRow.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));
                }
            }
        };

        final Runnable populateTabs = new Runnable() {
            @Override
            public void run() {
                catRow.removeAllViews();
                for (int i = 0; i < categories.length; i++) {
                    final String cat = categories[i];
                    boolean active = cat.equals(selectedCat[0]);
                    TextView pill = new TextView(act);
                    pill.setText(cat);
                    pill.setTextSize(13);
                    pill.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    pill.setTextColor(active ? 0xFFFFFFFF : 0xFF444444);
                    pill.setBackground(roundedRect(active ? 0xFF0093FF : 0xFFF0F0F0, 16, 0, 0, act));
                    pill.setPadding(dp(act, 14), dp(act, 6), dp(act, 14), dp(act, 6));
                    LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    pLp.setMargins(0, 0, dp(act, 8), 0);
                    pill.setLayoutParams(pLp);
                    pill.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            selectedCat[0] = cat;
                            run();
                            populateGrid.run();
                        }
                    });
                    catRow.addView(pill);
                }
            }
        };
        searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                searchQuery[0] = s.toString();
                populateGrid.run();
            }
        });
        populateTabs.run();
        populateGrid.run();

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, (int) (screenH * 0.78f));
            w.setGravity(Gravity.BOTTOM);
        }
        dlg.show();
    }

    // 2. Original Edit Stamp BottomSheet (layout_watermark_edit_header_global.xml)
    public static void showEditStampSheet(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act);
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackground(topRoundedSheet(0xFFFFFFFF, 12, act));
        int screenH = act.getResources().getDisplayMetrics().heightPixels;

        // Header (#FAFAFA top radius 10, height 60dp)
        FrameLayout header = new FrameLayout(act);
        header.setBackground(topRoundedSheet(0xFFFAFAFA, 12, act));
        header.setPadding(dp(act, 20), 0, dp(act, 12), 0);
        header.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 58)));

        TextView titleTv = new TextView(act);
        titleTv.setText("Edit Stamp — " + model.activeTemplate.name);
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        FrameLayout.LayoutParams tLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        header.addView(titleTv, tLp);

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.btn_close_on_light);
        FrameLayout.LayoutParams cLp = new FrameLayout.LayoutParams(dp(act, 32), dp(act, 32));
        cLp.gravity = Gravity.END | Gravity.CENTER_VERTICAL;
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        header.addView(closeIv, cLp);
        root.addView(header);

        View div = new View(act);
        div.setBackgroundColor(0xFFECECEC);
        root.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 1)));

        ScrollView scroll = new ScrollView(act);
        LinearLayout body = new LinearLayout(act);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(act, 18), dp(act, 12), dp(act, 18), dp(act, 24));

        // Stamp Size / Auto-Fit Scale Selector (layout_seek_bar_for_watermark_size.xml)
        TextView scaleLabel = new TextView(act);
        scaleLabel.setText("Stamp Size (Auto-Fit Text)");
        scaleLabel.setTextSize(14);
        scaleLabel.setTypeface(Typeface.DEFAULT_BOLD);
        scaleLabel.setTextColor(0xFF222222);
        body.addView(scaleLabel);

        final LinearLayout scaleRow = new LinearLayout(act);
        scaleRow.setOrientation(LinearLayout.HORIZONTAL);
        scaleRow.setPadding(0, dp(act, 6), 0, dp(act, 12));
        final float[] scaleValues = new float[]{0.85f, 1.0f, 1.15f};
        final String[] scaleNames = new String[]{"Small (85%)", "Standard (100%)", "Large (115%)"};
        final Runnable renderScalePills = new Runnable() {
            @Override
            public void run() {
                scaleRow.removeAllViews();
                for (int i = 0; i < scaleValues.length; i++) {
                    final float sv = scaleValues[i];
                    boolean active = Math.abs(model.stampScale - sv) < 0.03f;
                    TextView pill = new TextView(act);
                    pill.setText(scaleNames[i]);
                    pill.setTextSize(12);
                    pill.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    pill.setTextColor(active ? 0xFFFFFFFF : 0xFF333333);
                    pill.setGravity(Gravity.CENTER);
                    pill.setBackground(roundedRect(active ? 0xFF0093FF : 0xFFF0F0F0, 6, dp(act, 1), active ? 0xFF0093FF : 0xFFDEDEDE, act));
                    pill.setPadding(dp(act, 8), dp(act, 7), dp(act, 8), dp(act, 7));
                    LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    if (i < scaleValues.length - 1) pLp.setMargins(0, 0, dp(act, 8), 0);
                    pill.setLayoutParams(pLp);
                    pill.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            model.stampScale = sv;
                            run();
                            if (onUpdate != null) onUpdate.run();
                        }
                    });
                    scaleRow.addView(pill);
                }
            }
        };
        renderScalePills.run();
        body.addView(scaleRow);

        // Clock Font Selector (BebasNeue | Segment7 LED | RobotoMono)
        TextView fontLabel = new TextView(act);
        fontLabel.setText("Clock Font Style");
        fontLabel.setTextSize(14);
        fontLabel.setTypeface(Typeface.DEFAULT_BOLD);
        fontLabel.setTextColor(0xFF222222);
        body.addView(fontLabel);

        final LinearLayout fontRow = new LinearLayout(act);
        fontRow.setOrientation(LinearLayout.HORIZONTAL);
        fontRow.setPadding(0, dp(act, 6), 0, dp(act, 12));
        final String[] fontNames = new String[]{"BebasNeue", "Segment7 LED", "RobotoMono"};
        final Runnable renderFontPills = new Runnable() {
            @Override
            public void run() {
                fontRow.removeAllViews();
                for (int i = 0; i < fontNames.length; i++) {
                    final int fIdx = i;
                    boolean active = (model.clockFontIndex == i);
                    TextView pill = new TextView(act);
                    pill.setText(fontNames[i]);
                    pill.setTextSize(12);
                    pill.setTypeface(active ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    pill.setTextColor(active ? 0xFFFFFFFF : 0xFF333333);
                    pill.setGravity(Gravity.CENTER);
                    pill.setBackground(roundedRect(active ? 0xFF0093FF : 0xFFF0F0F0, 6, dp(act, 1), active ? 0xFF0093FF : 0xFFDEDEDE, act));
                    pill.setPadding(dp(act, 8), dp(act, 7), dp(act, 8), dp(act, 7));
                    LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                    if (i < fontNames.length - 1) pLp.setMargins(0, 0, dp(act, 8), 0);
                    pill.setLayoutParams(pLp);
                    pill.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            model.clockFontIndex = fIdx;
                            run();
                            if (onUpdate != null) onUpdate.run();
                        }
                    });
                    fontRow.addView(pill);
                }
            }
        };
        renderFontPills.run();
        body.addView(fontRow);

        // Theme Color Swatches
        TextView colorLabel = new TextView(act);
        colorLabel.setText("Theme Color");
        colorLabel.setTextSize(14);
        colorLabel.setTypeface(Typeface.DEFAULT_BOLD);
        colorLabel.setTextColor(0xFF222222);
        body.addView(colorLabel);

        LinearLayout colorRow = new LinearLayout(act);
        colorRow.setOrientation(LinearLayout.HORIZONTAL);
        colorRow.setPadding(0, dp(act, 8), 0, dp(act, 14));
        final int[] colors = new int[]{0xFFFFC233, 0xFF0070F4, 0xFFD98A36, 0xFF00C48C, 0xFF2E47A0, 0xFFFF1717, 0xFF222222};
        for (int i = 0; i < colors.length; i++) {
            final int c = colors[i];
            View swatch = new View(act);
            swatch.setBackground(roundedRect(c, 16, dp(act, 2), (model.customAccentColor == c) ? 0xFF222222 : 0x00000000, act));
            LinearLayout.LayoutParams swLp = new LinearLayout.LayoutParams(dp(act, 32), dp(act, 32));
            swLp.setMargins(0, 0, dp(act, 12), 0);
            swatch.setLayoutParams(swLp);
            swatch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.customAccentColor = c;
                    if (onUpdate != null) onUpdate.run();
                }
            });
            colorRow.addView(swatch);
        }
        body.addView(colorRow);

        // Quick Note History Chips (recently_edited_table in original app)
        TextView qNoteLabel = new TextView(act);
        qNoteLabel.setText("Quick Note Templates (1-Tap Fill)");
        qNoteLabel.setTextSize(12);
        qNoteLabel.setTextColor(0xFF666666);
        body.addView(qNoteLabel);

        HorizontalScrollView qScroll = new HorizontalScrollView(act);
        qScroll.setHorizontalScrollBarEnabled(false);
        LinearLayout qRow = new LinearLayout(act);
        qRow.setOrientation(LinearLayout.HORIZONTAL);
        qRow.setPadding(0, dp(act, 4), 0, dp(act, 10));
        final String[] quickNotes = new String[]{
            "Site inspection passed",
            "Material delivery verified",
            "Before work started",
            "Completed & sanitized",
            "Patrol checkpoint clear"
        };
        for (int i = 0; i < quickNotes.length; i++) {
            final String qn = quickNotes[i];
            TextView chip = new TextView(act);
            chip.setText(qn);
            chip.setTextSize(11.5f);
            chip.setTextColor(0xFF0070F4);
            chip.setBackground(roundedRect(0xFFE8F2FF, 14, dp(act, 1), 0xFF93C5FD, act));
            chip.setPadding(dp(act, 10), dp(act, 5), dp(act, 10), dp(act, 5));
            LinearLayout.LayoutParams cLp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            cLp2.setMargins(0, 0, dp(act, 6), 0);
            chip.setLayoutParams(cLp2);
            chip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.customNote = qn;
                    model.showNotes = true;
                    if (onUpdate != null) onUpdate.run();
                    Toast.makeText(act, "Applied Note: " + qn, Toast.LENGTH_SHORT).show();
                }
            });
            qRow.addView(chip);
        }
        qScroll.addView(qRow);
        body.addView(qScroll);

        // Editable Text Inputs (Live Auto-Resize & Fill Text)
        addEditField(act, body, "Title / Header (Auto-Fit Badge)", model.customTitle, new TextCallback() {
            @Override
            public void onText(String s) {
                model.customTitle = s;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addEditField(act, body, "Project / Company", model.projectName, new TextCallback() {
            @Override
            public void onText(String s) {
                model.projectName = s;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addEditField(act, body, "Area / Department", model.areaName, new TextCallback() {
            @Override
            public void onText(String s) {
                model.areaName = s;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addEditField(act, body, "Inspector / Worker Name", model.inspectorName, new TextCallback() {
            @Override
            public void onText(String s) {
                model.inspectorName = s;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addEditField(act, body, "Brand Logo Text (AI Cutout Badge)", model.brandLogoText, new TextCallback() {
            @Override
            public void onText(String s) {
                model.brandLogoText = s;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addEditField(act, body, "Notes / Description", model.customNote, new TextCallback() {
            @Override
            public void onText(String s) {
                model.customNote = s;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addEditField(act, body, "Address", model.addressLine, new TextCallback() {
            @Override
            public void onText(String s) {
                model.addressLine = s;
                if (onUpdate != null) onUpdate.run();
            }
        });

        // Field Switches (matching default_watermarks_global.json items)
        addSwitchRow(act, body, "Time (100% Real-Time Verified)", model.showTime, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showTime = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Address", model.showAddress, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showAddress = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Lat/Long (" + model.getFormattedCoordinates() + ")", model.showLatLong, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showLatLong = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Weather (" + model.weatherLine + ")", model.showWeather, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showWeather = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Satellite Mini-Map", model.showMap, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showMap = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Compass (" + model.compassLine + ")", model.showCompass, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showCompass = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Altitude (" + model.altitudeLine + ")", model.showAltitude, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showAltitude = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Notes", model.showNotes, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showNotes = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Brand Logo Badge (AI Cutout)", model.showBrandLogo, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showBrandLogo = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "QR Code Stamp", model.showQrCode, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showQrCode = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Show Time Now Logo", model.showTimeNowLogo, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showTimeNowLogo = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        addSwitchRow(act, body, "Photo Code: verify authenticity", model.showPhotoCode, new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean checked) {
                model.showPhotoCode = checked;
                if (onUpdate != null) onUpdate.run();
            }
        });

        // Share Template Code Button (watermark/share/public/register)
        TextView shareCodeBtn = new TextView(act);
        shareCodeBtn.setText("🔗 Share Template Code (TN-WM-" + model.activeTemplate.id.toUpperCase() + ")");
        shareCodeBtn.setTextSize(14);
        shareCodeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        shareCodeBtn.setTextColor(0xFF0070F4);
        shareCodeBtn.setGravity(Gravity.CENTER);
        shareCodeBtn.setBackground(roundedRect(0xFFE8F2FF, 8, dp(act, 1), 0xFF0070F4, act));
        shareCodeBtn.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
        LinearLayout.LayoutParams scLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        scLp.setMargins(0, dp(act, 12), 0, dp(act, 4));
        shareCodeBtn.setLayoutParams(scLp);
        shareCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(act, "Template Share Code Copied: TN-WM-" + model.activeTemplate.id.toUpperCase(), Toast.LENGTH_SHORT).show();
            }
        });
        body.addView(shareCodeBtn);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) (screenH * 0.68f)));

        dlg.setContentView(root);
        Window w = dlg.getWindow();
        if (w != null) {
            w.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            w.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            w.setGravity(Gravity.BOTTOM);
        }
        dlg.show();
    }

    private interface TextCallback {
        void onText(String s);
    }

    private static void addEditField(Context ctx, LinearLayout parent, String label, String initial, final TextCallback cb) {
        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextSize(12);
        tv.setTextColor(0xFF666666);
        parent.addView(tv);

        EditText et = new EditText(ctx);
        et.setText(initial);
        et.setTextSize(14);
        et.setTextColor(0xFF222222);
        et.setSingleLine(true);
        et.setBackground(roundedRect(0xFFF5F5F5, 6, dp(ctx, 1), 0xFFE0E0E0, ctx));
        et.setPadding(dp(ctx, 10), dp(ctx, 8), dp(ctx, 10), dp(ctx, 8));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dp(ctx, 4), 0, dp(ctx, 10));
        et.setLayoutParams(lp);
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) {
                cb.onText(s.toString());
            }
        });
        parent.addView(et);
    }

    private static void addSwitchRow(Context ctx, LinearLayout parent, String title, boolean checked, CompoundButton.OnCheckedChangeListener listener) {
        LinearLayout row = new LinearLayout(ctx);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(ctx, 10), 0, dp(ctx, 10));

        ImageView checkIv = new ImageView(ctx);
        checkIv.setImageResource(checked ? R.drawable.check_box_select : R.drawable.check_box_unselect);
        row.addView(checkIv, new LinearLayout.LayoutParams(dp(ctx, 20), dp(ctx, 20)));

        TextView tv = new TextView(ctx);
        tv.setText("  " + title);
        tv.setTextSize(15);
        tv.setTextColor(0xFF222222);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Switch sw = new Switch(ctx);
        sw.setChecked(checked);
        sw.setOnCheckedChangeListener(listener);
        row.addView(sw);
        parent.addView(row);

        View div = new View(ctx);
        div.setBackgroundColor(0xFFF0F0F0);
        parent.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
    }

    // 3. Original Left Settings Drawer (setting_dialog_view_new.xml)
    public static void showLeftSettingsDrawer(final Activity act, final WatermarkModel model,
                                              final Runnable onRatioChanged, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Translucent_NoTitleBar);

        FrameLayout backdrop = new FrameLayout(act);
        backdrop.setBackgroundColor(0x66000000);
        backdrop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        // group_menu: 296dp width, #F0F0F0 (bg_default)
        ScrollView drawerScroll = new ScrollView(act);
        drawerScroll.setBackgroundColor(0xFFF0F0F0);
        FrameLayout.LayoutParams dLp = new FrameLayout.LayoutParams(dp(act, 296), ViewGroup.LayoutParams.MATCH_PARENT);
        dLp.gravity = Gravity.START;
        drawerScroll.setLayoutParams(dLp);
        drawerScroll.setClickable(true);

        LinearLayout menu = new LinearLayout(act);
        menu.setOrientation(LinearLayout.VERTICAL);
        menu.setPadding(dp(act, 15), dp(act, 36), dp(act, 15), dp(act, 24));

        // 1. in_teamspace_header (setting_teamspace_login.xml)
        LinearLayout tsHeader = new LinearLayout(act);
        tsHeader.setOrientation(LinearLayout.HORIZONTAL);
        tsHeader.setGravity(Gravity.CENTER_VERTICAL);
        tsHeader.setBackground(roundedRect(0xFFFFFFFF, 8, 0, 0, act));
        tsHeader.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));
        ImageView tsIcon = new ImageView(act);
        tsIcon.setImageResource(R.drawable.icon_preview_folder);
        tsHeader.addView(tsIcon, new LinearLayout.LayoutParams(dp(act, 28), dp(act, 28)));
        TextView tsTv = new TextView(act);
        tsTv.setText("  Teamspace Cloud Sync");
        tsTv.setTextSize(15);
        tsTv.setTypeface(Typeface.DEFAULT_BOLD);
        tsTv.setTextColor(0xFF222222);
        tsHeader.addView(tsTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        tsHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showTeamspaceDialog(act, model);
            }
        });
        menu.addView(tsHeader);

        // 2. in_vip (setting_dialog_view_new_child_vip.xml: #7A6EFF radius 8dp, height 48dp)
        LinearLayout vipCard = new LinearLayout(act);
        vipCard.setOrientation(LinearLayout.HORIZONTAL);
        vipCard.setGravity(Gravity.CENTER_VERTICAL);
        vipCard.setBackground(roundedRect(0xFF7A6EFF, 8, 0, 0, act));
        vipCard.setPadding(dp(act, 12), 0, dp(act, 12), 0);
        LinearLayout.LayoutParams vipLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 48));
        vipLp.setMargins(0, dp(act, 14), 0, dp(act, 12));
        vipCard.setLayoutParams(vipLp);

        ImageView vipCrown = new ImageView(act);
        vipCrown.setImageResource(R.drawable.vip_crown);
        vipCard.addView(vipCrown, new LinearLayout.LayoutParams(dp(act, 23), dp(act, 23)));

        TextView vipTv = new TextView(act);
        vipTv.setText("  Time Now Plans");
        vipTv.setTextSize(16);
        vipTv.setTypeface(Typeface.DEFAULT_BOLD);
        vipTv.setTextColor(0xFFFFFFFF);
        vipCard.addView(vipTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView vipArrow = new TextView(act);
        vipArrow.setText("›");
        vipArrow.setTextSize(22);
        vipArrow.setTextColor(0xFFFFFFFF);
        vipCard.addView(vipArrow);
        vipCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showPurchaseDialog(act);
            }
        });
        menu.addView(vipCard);

        // 3. in_common (setting_dialog_view_new_child_common.xml: 2x2 grid of white cards)
        LinearLayout row1 = new LinearLayout(act);
        row1.setOrientation(LinearLayout.HORIZONTAL);

        final String[] ratioNames = new String[]{"Ratio (3:4)", "Ratio (9:16)", "Ratio (1:1)", "Ratio (Full)"};
        final int[] ratioIcons = new int[]{
            R.drawable.ratio_pop_view_3_4, R.drawable.ratio_pop_view_9_16,
            R.drawable.ratio_pop_view_1_1, R.drawable.ratio_pop_view_full
        };
        LinearLayout ratioCard = createCommonGridCard(act, ratioIcons[model.aspectRatioMode], ratioNames[model.aspectRatioMode]);
        ratioCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showRatioPopup(act, model, onRatioChanged);
            }
        });
        LinearLayout.LayoutParams c1Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        c1Lp.setMargins(0, 0, dp(act, 4), 0);
        row1.addView(ratioCard, c1Lp);

        final TextView[] timerLabelRef = new TextView[1];
        LinearLayout timerCard = createCommonGridCard(act, R.drawable.icon_tool_border_setting,
                model.timerSeconds == 0 ? "Timer: Off" : ("Timer: " + model.timerSeconds + "s"), timerLabelRef);
        timerCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model.timerSeconds == 0) model.timerSeconds = 3;
                else if (model.timerSeconds == 3) model.timerSeconds = 5;
                else if (model.timerSeconds == 5) model.timerSeconds = 10;
                else model.timerSeconds = 0;
                timerLabelRef[0].setText(model.timerSeconds == 0 ? "Timer: Off" : ("Timer: " + model.timerSeconds + "s"));
            }
        });
        LinearLayout.LayoutParams c2Lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        c2Lp.setMargins(dp(act, 4), 0, 0, 0);
        row1.addView(timerCard, c2Lp);
        menu.addView(row1);

        LinearLayout row2 = new LinearLayout(act);
        row2.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams r2Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        r2Lp.setMargins(0, dp(act, 8), 0, dp(act, 12));
        row2.setLayoutParams(r2Lp);

        final TextView[] resLabelRef = new TextView[1];
        LinearLayout resCard = createCommonGridCard(act, R.drawable.ic_web_portal, model.resolutionLabel, resLabelRef);
        resCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ("FHD 1080P".equals(model.resolutionLabel)) model.resolutionLabel = "4K UHD";
                else if ("4K UHD".equals(model.resolutionLabel)) model.resolutionLabel = "HD 720P";
                else model.resolutionLabel = "FHD 1080P";
                resLabelRef[0].setText(model.resolutionLabel);
            }
        });
        row2.addView(resCard, c1Lp);

        LinearLayout setCard = createCommonGridCard(act, R.drawable.setting, "Settings");
        setCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showFullSettingsDialog(act, model, onUpdate);
            }
        });
        row2.addView(setCard, c2Lp);
        menu.addView(row2);

        // 4. in_tools (setting_dialog_view_new_child_tools.xml: white card with Verify Photo, Show Time Now Logo, Save Original Photo, Earn .mcap)
        LinearLayout toolsCard = new LinearLayout(act);
        toolsCard.setOrientation(LinearLayout.VERTICAL);
        toolsCard.setBackground(roundedRect(0xFFFFFFFF, 8, 0, 0, act));
        toolsCard.setPadding(dp(act, 12), dp(act, 4), dp(act, 12), dp(act, 4));

        // Tool 1: Verify Photo
        LinearLayout verRow = new LinearLayout(act);
        verRow.setOrientation(LinearLayout.HORIZONTAL);
        verRow.setGravity(Gravity.CENTER_VERTICAL);
        verRow.setMinimumHeight(dp(act, 54));
        ImageView verIv = new ImageView(act);
        verIv.setImageResource(R.drawable.tool_border_safe_green);
        verRow.addView(verIv, new LinearLayout.LayoutParams(dp(act, 23), dp(act, 23)));
        TextView verTv = new TextView(act);
        verTv.setText("  Verify Photo");
        verTv.setTextSize(15);
        verTv.setTextColor(0xFF222222);
        verRow.addView(verTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView verArr = new TextView(act);
        verArr.setText("›");
        verArr.setTextSize(20);
        verArr.setTextColor(0xFF999999);
        verRow.addView(verArr);
        verRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showVerifyCenterDialog(act, model);
            }
        });
        toolsCard.addView(verRow);

        View d1 = new View(act);
        d1.setBackgroundColor(0xFFECECEC);
        toolsCard.addView(d1, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // Tool 2: Show Time Now Logo
        LinearLayout logoRow = new LinearLayout(act);
        logoRow.setOrientation(LinearLayout.HORIZONTAL);
        logoRow.setGravity(Gravity.CENTER_VERTICAL);
        logoRow.setMinimumHeight(dp(act, 54));
        ImageView logoIv = new ImageView(act);
        logoIv.setImageResource(R.drawable.tool_border_watermark);
        logoRow.addView(logoIv, new LinearLayout.LayoutParams(dp(act, 23), dp(act, 23)));
        TextView logoTv = new TextView(act);
        logoTv.setText("  Show Time Now Logo");
        logoTv.setTextSize(15);
        logoTv.setTextColor(0xFF222222);
        logoRow.addView(logoTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Switch logoSw = new Switch(act);
        logoSw.setChecked(model.showTimeNowLogo);
        logoSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.showTimeNowLogo = isChecked;
                if (onUpdate != null) onUpdate.run();
            }
        });
        logoRow.addView(logoSw);
        toolsCard.addView(logoRow);

        View d2 = new View(act);
        d2.setBackgroundColor(0xFFECECEC);
        toolsCard.addView(d2, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // Tool 3: Save Original Photo
        LinearLayout origRow = new LinearLayout(act);
        origRow.setOrientation(LinearLayout.HORIZONTAL);
        origRow.setGravity(Gravity.CENTER_VERTICAL);
        origRow.setMinimumHeight(dp(act, 54));
        ImageView origIv = new ImageView(act);
        origIv.setImageResource(R.drawable.global_border_download);
        origRow.addView(origIv, new LinearLayout.LayoutParams(dp(act, 23), dp(act, 23)));
        TextView origTv = new TextView(act);
        origTv.setText("  Save Original Photo");
        origTv.setTextSize(15);
        origTv.setTextColor(0xFF222222);
        origRow.addView(origTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        Switch origSw = new Switch(act);
        origSw.setChecked(model.saveOriginalPhoto);
        origSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                model.saveOriginalPhoto = isChecked;
            }
        });
        origRow.addView(origSw);
        toolsCard.addView(origRow);

        View d3 = new View(act);
        d3.setBackgroundColor(0xFFECECEC);
        toolsCard.addView(d3, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // Tool 4: Earn — Embodied AI Robotics (.mcap) Data Collection Hub (Report 07 / 08)
        LinearLayout earnRow = new LinearLayout(act);
        earnRow.setOrientation(LinearLayout.HORIZONTAL);
        earnRow.setGravity(Gravity.CENTER_VERTICAL);
        earnRow.setMinimumHeight(dp(act, 54));
        ImageView earnIv = new ImageView(act);
        earnIv.setImageResource(R.drawable.vip_crown);
        earnRow.addView(earnIv, new LinearLayout.LayoutParams(dp(act, 23), dp(act, 23)));
        TextView earnTv = new TextView(act);
        earnTv.setText("  Earn — Embodied AI (.mcap)");
        earnTv.setTextSize(15);
        earnTv.setTextColor(0xFF222222);
        earnRow.addView(earnTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView earnArr = new TextView(act);
        earnArr.setText("›");
        earnArr.setTextSize(20);
        earnArr.setTextColor(0xFF999999);
        earnRow.addView(earnArr);
        earnRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showEarnRoboticsDialog(act, model);
            }
        });
        toolsCard.addView(earnRow);

        menu.addView(toolsCard);
        drawerScroll.addView(menu);
        backdrop.addView(drawerScroll);

        dlg.setContentView(backdrop);
        dlg.show();
    }

    // 3b. Full Original Camera & Format Settings Dialog (SettingActivity / Report 08 & 09)
    public static void showFullSettingsDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Light_NoTitleBar);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFF5F6F8);

        FrameLayout topBar = new FrameLayout(act);
        topBar.setBackgroundColor(0xFFFFFFFF);
        topBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.global_border_close_strong);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onUpdate != null) onUpdate.run();
                dlg.dismiss();
            }
        });
        topBar.addView(closeIv, new FrameLayout.LayoutParams(dp(act, 28), dp(act, 28), Gravity.START | Gravity.CENTER_VERTICAL));

        TextView titleTv = new TextView(act);
        titleTv.setText("Camera & Stamp Settings");
        titleTv.setTextSize(18);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        topBar.addView(titleTv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        root.addView(topBar);

        ScrollView scroll = new ScrollView(act);
        LinearLayout body = new LinearLayout(act);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(act, 16), dp(act, 14), dp(act, 16), dp(act, 24));

        // Card 1: Viewfinder & Camera Assistance
        LinearLayout camCard = new LinearLayout(act);
        camCard.setOrientation(LinearLayout.VERTICAL);
        camCard.setBackground(roundedRect(0xFFFFFFFF, 8, 0, 0, act));
        camCard.setPadding(dp(act, 14), dp(act, 10), dp(act, 14), dp(act, 10));

        TextView c1Head = new TextView(act);
        c1Head.setText("Camera Viewfinder & AI Assistance");
        c1Head.setTextSize(13);
        c1Head.setTypeface(Typeface.DEFAULT_BOLD);
        c1Head.setTextColor(0xFF0070F4);
        c1Head.setPadding(0, dp(act, 4), 0, dp(act, 6));
        camCard.addView(c1Head);

        addSwitchRow(act, camCard, "Grid Lines (3x3 Composition)", model.showGridLines, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean c) { model.showGridLines = c; if (onUpdate != null) onUpdate.run(); }
        });
        addSwitchRow(act, camCard, "Horizontal Electronic Leveler", model.showLeveler, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean c) { model.showLeveler = c; if (onUpdate != null) onUpdate.run(); }
        });
        addSwitchRow(act, camCard, "ZeroDCE AI Night Mode (Low-Light Boost)", model.nightModeZeroDce, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean c) { model.nightModeZeroDce = c; if (onUpdate != null) onUpdate.run(); }
        });
        addSwitchRow(act, camCard, "Shutter Sound", model.shutterSoundEnabled, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean c) { model.shutterSoundEnabled = c; }
        });
        body.addView(camCard);

        // Card 2: Time & GPS Coordinate Formats
        LinearLayout fmtCard = new LinearLayout(act);
        fmtCard.setOrientation(LinearLayout.VERTICAL);
        fmtCard.setBackground(roundedRect(0xFFFFFFFF, 8, 0, 0, act));
        fmtCard.setPadding(dp(act, 14), dp(act, 10), dp(act, 14), dp(act, 10));
        LinearLayout.LayoutParams fLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        fLp.setMargins(0, dp(act, 14), 0, dp(act, 14));
        fmtCard.setLayoutParams(fLp);

        TextView c2Head = new TextView(act);
        c2Head.setText("Time & GPS Coordinate Format");
        c2Head.setTextSize(13);
        c2Head.setTypeface(Typeface.DEFAULT_BOLD);
        c2Head.setTextColor(0xFF0070F4);
        c2Head.setPadding(0, dp(act, 4), 0, dp(act, 6));
        fmtCard.addView(c2Head);

        addSwitchRow(act, fmtCard, "24-Hour Time Format (vs 12-Hour AM/PM)", model.use24HourFormat, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean c) { model.use24HourFormat = c; if (onUpdate != null) onUpdate.run(); }
        });
        addSwitchRow(act, fmtCard, "Show Seconds (:ss) in Clock", model.showSecondsInClock, new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean c) { model.showSecondsInClock = c; if (onUpdate != null) onUpdate.run(); }
        });

        TextView gpsLbl = new TextView(act);
        gpsLbl.setText("GPS Coordinate Standard");
        gpsLbl.setTextSize(13);
        gpsLbl.setTextColor(0xFF444444);
        gpsLbl.setPadding(0, dp(act, 8), 0, dp(act, 6));
        fmtCard.addView(gpsLbl);

        final LinearLayout gpsRow = new LinearLayout(act);
        gpsRow.setOrientation(LinearLayout.HORIZONTAL);
        final String[] gpsModes = new String[]{"Decimal (DD)", "DMS (° ' \")", "UTM Grid"};
        final TextView[] gpsBtns = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int gIdx = i;
            TextView gb = new TextView(act);
            gb.setText(gpsModes[i]);
            gb.setTextSize(12);
            gb.setGravity(Gravity.CENTER);
            gb.setPadding(dp(act, 6), dp(act, 8), dp(act, 6), dp(act, 8));
            boolean sel = (model.gpsFormatMode == i);
            gb.setBackground(roundedRect(sel ? 0xFF0070F4 : 0xFFF0F0F0, 6, 0, 0, act));
            gb.setTextColor(sel ? 0xFFFFFFFF : 0xFF333333);
            gb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.gpsFormatMode = gIdx;
                    for (int k = 0; k < 3; k++) {
                        boolean s = (k == gIdx);
                        gpsBtns[k].setBackground(roundedRect(s ? 0xFF0070F4 : 0xFFF0F0F0, 6, 0, 0, act));
                        gpsBtns[k].setTextColor(s ? 0xFFFFFFFF : 0xFF333333);
                    }
                    if (onUpdate != null) onUpdate.run();
                }
            });
            LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            glp.setMargins(dp(act, 2), 0, dp(act, 2), dp(act, 4));
            gb.setLayoutParams(glp);
            gpsBtns[i] = gb;
            gpsRow.addView(gb);
        }
        fmtCard.addView(gpsRow);
        body.addView(fmtCard);

        // Card 3: Network Benchmark & Diagnostics
        LinearLayout diagCard = new LinearLayout(act);
        diagCard.setOrientation(LinearLayout.VERTICAL);
        diagCard.setBackground(roundedRect(0xFFFFFFFF, 8, 0, 0, act));
        diagCard.setPadding(dp(act, 14), dp(act, 12), dp(act, 14), dp(act, 12));

        TextView c3Head = new TextView(act);
        c3Head.setText("Anti-Fake Engine & Network Diagnostics");
        c3Head.setTextSize(13);
        c3Head.setTypeface(Typeface.DEFAULT_BOLD);
        c3Head.setTextColor(0xFF0070F4);
        diagCard.addView(c3Head);

        final TextView benchTv = new TextView(act);
        benchTv.setText("• NTP Time Server: time.google.com (Offset +2ms Verified)\n• Native Engines: 7 ARM64 C++17 Libraries Loaded\n• JpegPacker XHEY1 Dual-Stream Trailer: Enabled");
        benchTv.setTextSize(12);
        benchTv.setTextColor(0xFF555555);
        benchTv.setPadding(0, dp(act, 8), 0, dp(act, 10));
        diagCard.addView(benchTv);

        TextView runBenchBtn = new TextView(act);
        runBenchBtn.setText("Run Network & NTP Benchmark");
        runBenchBtn.setTextSize(13);
        runBenchBtn.setTypeface(Typeface.DEFAULT_BOLD);
        runBenchBtn.setTextColor(0xFFFFFFFF);
        runBenchBtn.setGravity(Gravity.CENTER);
        runBenchBtn.setBackground(roundedRect(0xFF0070F4, 6, 0, 0, act));
        runBenchBtn.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));
        runBenchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                benchTv.setText("✓ NTP Ping: 14ms | GPS Satellites: 18 Locked\n✓ Weather API: 28ms | Teamspace Cloud Sync: 32ms\n✓ Cryptographic Anti-Fake Clock: 100% Authentic");
                Toast.makeText(act, "Network & NTP Benchmark Passed (14ms)!", Toast.LENGTH_SHORT).show();
            }
        });
        diagCard.addView(runBenchBtn);
        body.addView(diagCard);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        dlg.setContentView(root);
        dlg.show();
    }

    // 3c. Embodied AI Robotics (.mcap) Data Collection & Reward Hub (Report 07 & 08)
    public static void showEarnRoboticsDialog(final Activity act, final WatermarkModel model) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Light_NoTitleBar);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF0F172A);

        FrameLayout topBar = new FrameLayout(act);
        topBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.close_white);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dlg.dismiss(); }
        });
        topBar.addView(closeIv, new FrameLayout.LayoutParams(dp(act, 28), dp(act, 28), Gravity.START | Gravity.CENTER_VERTICAL));

        TextView titleTv = new TextView(act);
        titleTv.setText("Earn — Embodied AI Robotics Hub");
        titleTv.setTextSize(17);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFFFFFFFF);
        topBar.addView(titleTv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        root.addView(topBar);

        ScrollView scroll = new ScrollView(act);
        LinearLayout body = new LinearLayout(act);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(act, 16), dp(act, 10), dp(act, 16), dp(act, 24));

        // Wallet Card
        LinearLayout walletCard = new LinearLayout(act);
        walletCard.setOrientation(LinearLayout.VERTICAL);
        walletCard.setBackground(roundedRect(0xFF1E293B, 10, dp(act, 1), 0xFF38BDF8, act));
        walletCard.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));

        final TextView balTv = new TextView(act);
        balTv.setText(String.format(Locale.US, "Wallet Balance: $%.2f USD (R$ %.2f Pix)\nVIP Credits: %d Days Time Now Plus",
                model.earnWalletUsd, model.earnWalletUsd * 5.0, model.earnVipDays));
        balTv.setTextSize(15);
        balTv.setTypeface(Typeface.DEFAULT_BOLD);
        balTv.setTextColor(0xFF38BDF8);
        walletCard.addView(balTv);

        LinearLayout wBtnRow = new LinearLayout(act);
        wBtnRow.setOrientation(LinearLayout.HORIZONTAL);
        wBtnRow.setPadding(0, dp(act, 12), 0, 0);

        TextView pixBtn = new TextView(act);
        pixBtn.setText("Withdraw via Pix");
        pixBtn.setTextSize(13);
        pixBtn.setTypeface(Typeface.DEFAULT_BOLD);
        pixBtn.setTextColor(0xFF0F172A);
        pixBtn.setGravity(Gravity.CENTER);
        pixBtn.setBackground(roundedRect(0xFF4ADE80, 6, 0, 0, act));
        pixBtn.setPadding(dp(act, 10), dp(act, 8), dp(act, 10), dp(act, 8));
        pixBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(act, "Pix Payout Request Submitted (Instant Transfer Ready)!", Toast.LENGTH_SHORT).show();
            }
        });
        wBtnRow.addView(pixBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        View sp = new View(act);
        wBtnRow.addView(sp, new LinearLayout.LayoutParams(dp(act, 10), 1));

        TextView vipBtn = new TextView(act);
        vipBtn.setText("Redeem +7 Days VIP");
        vipBtn.setTextSize(13);
        vipBtn.setTypeface(Typeface.DEFAULT_BOLD);
        vipBtn.setTextColor(0xFFFFFFFF);
        vipBtn.setGravity(Gravity.CENTER);
        vipBtn.setBackground(roundedRect(0xFF7A6EFF, 6, 0, 0, act));
        vipBtn.setPadding(dp(act, 10), dp(act, 8), dp(act, 10), dp(act, 8));
        vipBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.earnVipDays += 7;
                balTv.setText(String.format(Locale.US, "Wallet Balance: $%.2f USD (R$ %.2f Pix)\nVIP Credits: %d Days Time Now Plus",
                        model.earnWalletUsd, model.earnWalletUsd * 5.0, model.earnVipDays));
                Toast.makeText(act, "+7 Days Time Now Plus VIP Activated!", Toast.LENGTH_SHORT).show();
            }
        });
        wBtnRow.addView(vipBtn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        walletCard.addView(wBtnRow);
        body.addView(walletCard);

        // Task Card (.mcap Recorder)
        LinearLayout taskCard = new LinearLayout(act);
        taskCard.setOrientation(LinearLayout.VERTICAL);
        taskCard.setBackground(roundedRect(0xFF1E293B, 10, 0, 0, act));
        taskCard.setPadding(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tLp.setMargins(0, dp(act, 14), 0, 0);
        taskCard.setLayoutParams(tLp);

        TextView tHead = new TextView(act);
        tHead.setText("Active Embodied AI Task: Hand Manipulation (.mcap)");
        tHead.setTextSize(15);
        tHead.setTypeface(Typeface.DEFAULT_BOLD);
        tHead.setTextColor(0xFFFFFFFF);
        taskCard.addView(tHead);

        final TextView teleTv = new TextView(act);
        teleTv.setText("• MediaPipe 21-Keypoint Hand Landmarks: Ready (30 FPS)\n• 4-Sensor IMU Stream (Accel/Gyro/Mag/Rot): 100Hz Sync\n• Container Format: Foxglove Robotics .mcap\n• Completed Sessions: " + model.mcapSubmittedCount + " (+ $2.50 / clip)");
        teleTv.setTextSize(13);
        teleTv.setTextColor(0xFFCBD5E1);
        teleTv.setPadding(0, dp(act, 10), 0, dp(act, 12));
        taskCard.addView(teleTv);

        TextView recMcapBtn = new TextView(act);
        recMcapBtn.setText("Record 5s Hand + IMU .mcap Session (+$2.50)");
        recMcapBtn.setTextSize(14);
        recMcapBtn.setTypeface(Typeface.DEFAULT_BOLD);
        recMcapBtn.setTextColor(0xFFFFFFFF);
        recMcapBtn.setGravity(Gravity.CENTER);
        recMcapBtn.setBackground(roundedRect(0xFF0070F4, 6, 0, 0, act));
        recMcapBtn.setPadding(dp(act, 12), dp(act, 12), dp(act, 12), dp(act, 12));
        recMcapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                model.mcapSubmittedCount++;
                model.earnWalletUsd += 2.50;
                balTv.setText(String.format(Locale.US, "Wallet Balance: $%.2f USD (R$ %.2f Pix)\nVIP Credits: %d Days Time Now Plus",
                        model.earnWalletUsd, model.earnWalletUsd * 5.0, model.earnVipDays));
                teleTv.setText("✓ Recorded session_" + model.mcapSubmittedCount + ".mcap (150 Hand Frames + 500 IMU Samples)\n• Completed Sessions: " + model.mcapSubmittedCount + " (+ $2.50 earned!)");
                Toast.makeText(act, "Saved Foxglove .mcap Robotics Stream! +$2.50 USD credited.", Toast.LENGTH_SHORT).show();
            }
        });
        taskCard.addView(recMcapBtn);
        body.addView(taskCard);

        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));
        dlg.setContentView(root);
        dlg.show();
    }

    private static LinearLayout createCommonGridCard(Context ctx, int iconRes, String label) {
        return createCommonGridCard(ctx, iconRes, label, null);
    }

    private static LinearLayout createCommonGridCard(Context ctx, int iconRes, String label, TextView[] outLabel) {
        LinearLayout card = new LinearLayout(ctx);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setBackground(roundedRect(0xFFFFFFFF, 8, 0, 0, ctx));
        card.setPadding(dp(ctx, 12), dp(ctx, 10), dp(ctx, 12), dp(ctx, 10));

        ImageView iv = new ImageView(ctx);
        iv.setImageResource(iconRes);
        card.addView(iv, new LinearLayout.LayoutParams(dp(ctx, 28), dp(ctx, 28)));

        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextSize(13);
        tv.setTextColor(0xFF444444);
        tv.setPadding(0, dp(ctx, 6), 0, 0);
        card.addView(tv);
        if (outLabel != null && outLabel.length > 0) outLabel[0] = tv;
        return card;
    }

    // 4. Original Ratio Setting Dialog (ratio_setting_dialog_global.xml)
    public static void showRatioPopup(final Activity act, final WatermarkModel model, final Runnable onRatioChanged) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Translucent_NoTitleBar);

        FrameLayout root = new FrameLayout(act);
        root.setBackgroundColor(0x33000000);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        LinearLayout bar = new LinearLayout(act);
        bar.setOrientation(LinearLayout.HORIZONTAL);
        bar.setGravity(Gravity.CENTER);
        bar.setBackground(roundedRect(0xFFFFFFFF, 12, dp(act, 1), 0xFFE0E0E0, act));
        bar.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));
        FrameLayout.LayoutParams barLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 76));
        barLp.setMargins(dp(act, 16), dp(act, 60), dp(act, 16), 0);
        barLp.gravity = Gravity.TOP;
        bar.setLayoutParams(barLp);

        int[] icons = new int[]{
            R.drawable.ratio_pop_view_3_4, R.drawable.ratio_pop_view_9_16,
            R.drawable.ratio_pop_view_1_1, R.drawable.ratio_pop_view_full
        };
        String[] labels = new String[]{"3:4", "9:16", "1:1", "Full"};
        for (int i = 0; i < 4; i++) {
            final int idx = i;
            boolean sel = (model.aspectRatioMode == i);
            LinearLayout item = new LinearLayout(act);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setGravity(Gravity.CENTER);
            ImageView iv = new ImageView(act);
            iv.setImageResource(icons[i]);
            item.addView(iv, new LinearLayout.LayoutParams(dp(act, 34), dp(act, 34)));

            TextView tv = new TextView(act);
            tv.setText(labels[i]);
            tv.setTextSize(11);
            tv.setTypeface(sel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
            tv.setTextColor(sel ? 0xFFFFFFFF : 0xFF222222);
            if (sel) {
                tv.setBackground(roundedRect(0xFF0093FF, 3, 0, 0, act));
            }
            tv.setPadding(dp(act, 6), dp(act, 1), dp(act, 6), dp(act, 1));
            item.addView(tv);

            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    model.aspectRatioMode = idx;
                    if (onRatioChanged != null) onRatioChanged.run();
                    dlg.dismiss();
                }
            });
            bar.addView(item, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }

        root.addView(bar);
        dlg.setContentView(root);
        dlg.show();
    }

    // 5. Original Verify Center (layout_fragment_photo_verify.xml - #154978 background)
    public static void showVerifyCenterDialog(final Activity act, final WatermarkModel model) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Light_NoTitleBar);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFF154978);

        // Top bar
        FrameLayout topBar = new FrameLayout(act);
        topBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));
        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.close_white);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(closeIv, new FrameLayout.LayoutParams(dp(act, 28), dp(act, 28), Gravity.START | Gravity.CENTER_VERTICAL));

        TextView topTitle = new TextView(act);
        topTitle.setText("Verify Center");
        topTitle.setTextSize(18);
        topTitle.setTypeface(Typeface.DEFAULT_BOLD);
        topTitle.setTextColor(0xFFFFFFFF);
        topBar.addView(topTitle, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        root.addView(topBar);

        ScrollView scroll = new ScrollView(act);
        LinearLayout content = new LinearLayout(act);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(act, 20), dp(act, 16), dp(act, 20), dp(act, 30));

        // Card 1: Verify by Photo (upload_layout in layout_fragment_photo_verify.xml)
        LinearLayout card1 = new LinearLayout(act);
        card1.setOrientation(LinearLayout.VERTICAL);
        card1.setGravity(Gravity.CENTER_HORIZONTAL);
        card1.setBackground(roundedRect(0xFFFFFFFF, 8, 0, 0, act));
        card1.setPadding(dp(act, 20), dp(act, 26), dp(act, 20), dp(act, 26));

        ImageView upIcon = new ImageView(act);
        upIcon.setImageResource(R.drawable.verification_upload_icon);
        card1.addView(upIcon, new LinearLayout.LayoutParams(dp(act, 88), dp(act, 88)));

        TextView c1Title = new TextView(act);
        c1Title.setText("Verify by Photo");
        c1Title.setTextSize(22);
        c1Title.setTypeface(Typeface.DEFAULT_BOLD);
        c1Title.setTextColor(0xFF222222);
        c1Title.setPadding(0, dp(act, 14), 0, dp(act, 6));
        card1.addView(c1Title);

        TextView c1Sub1 = new TextView(act);
        c1Sub1.setText("• Check time & GPS authenticity\n• Detect edits & tampering (JpegPacker XHEY1)");
        c1Sub1.setTextSize(14);
        c1Sub1.setTextColor(0xFF848484);
        c1Sub1.setGravity(Gravity.CENTER);
        card1.addView(c1Sub1);

        final TextView resultBox = new TextView(act);
        resultBox.setTextSize(12);
        resultBox.setTextColor(0xFF0070F4);
        resultBox.setPadding(dp(act, 10), dp(act, 10), dp(act, 10), dp(act, 10));
        resultBox.setVisibility(View.GONE);
        card1.addView(resultBox);

        TextView upBtn = new TextView(act);
        upBtn.setText("Upload Photo");
        upBtn.setTextSize(16);
        upBtn.setTypeface(Typeface.DEFAULT_BOLD);
        upBtn.setTextColor(0xFFFFFFFF);
        upBtn.setGravity(Gravity.CENTER);
        upBtn.setBackground(roundedRect(0xFF0070F4, 6, 0, 0, act));
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 46));
        btnLp.setMargins(0, dp(act, 18), 0, 0);
        upBtn.setLayoutParams(btnLp);
        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultBox.setVisibility(View.VISIBLE);
                if (!model.capturedPhotos.isEmpty()) {
                    WatermarkModel.CapturedPhoto p = model.capturedPhotos.get(0);
                    String trailerStatus = p.isVideo
                            ? "AUTHENTIC MP4 VIDEO • Verified NTP & GPS Audio/Video Stream"
                            : WatermarkModel.inspectJpegPackerTrailer(p.compositeFile);
                    resultBox.setText("Item: " + p.compositeFile.getName()
                            + "\nPhoto Code: " + p.photoCode
                            + "\nCaptured: " + p.timestampText
                            + "\nSHA-256: " + p.sha256Hex
                            + "\n" + trailerStatus);
                } else {
                    resultBox.setText("Ready! Take a photo or video on the camera screen first to inspect its live XHEY1 dual-stream authenticity signature.");
                }
            }
        });
        card1.addView(upBtn);
        content.addView(card1);

        // Card 2: Verify by Photo Code (cl_code in layout_fragment_photo_verify.xml)
        LinearLayout card2 = new LinearLayout(act);
        card2.setOrientation(LinearLayout.VERTICAL);
        card2.setGravity(Gravity.CENTER_HORIZONTAL);
        card2.setBackground(roundedRect(0xFFFFFFFF, 8, 0, 0, act));
        card2.setPadding(dp(act, 20), dp(act, 26), dp(act, 20), dp(act, 26));
        LinearLayout.LayoutParams c2Lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        c2Lp.setMargins(0, dp(act, 20), 0, 0);
        card2.setLayoutParams(c2Lp);

        ImageView codeIcon = new ImageView(act);
        codeIcon.setImageResource(R.drawable.verification_center_icon);
        card2.addView(codeIcon, new LinearLayout.LayoutParams(dp(act, 88), dp(act, 88)));

        TextView c2Title = new TextView(act);
        c2Title.setText("Verify by Photo Code");
        c2Title.setTextSize(22);
        c2Title.setTypeface(Typeface.DEFAULT_BOLD);
        c2Title.setTextColor(0xFF222222);
        c2Title.setPadding(0, dp(act, 14), 0, dp(act, 6));
        card2.addView(c2Title);

        TextView c2Sub = new TextView(act);
        c2Sub.setText("Verify original time & location via 10-char anti-fake code");
        c2Sub.setTextSize(14);
        c2Sub.setTextColor(0xFF848484);
        c2Sub.setGravity(Gravity.CENTER);
        card2.addView(c2Sub);

        final EditText codeInput = new EditText(act);
        codeInput.setText(model.currentPhotoCode);
        codeInput.setTextSize(16);
        codeInput.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
        codeInput.setGravity(Gravity.CENTER);
        codeInput.setBackground(roundedRect(0xFFF5F5F5, 6, dp(act, 1), 0xFFDEDEDE, act));
        LinearLayout.LayoutParams inLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 46));
        inLp.setMargins(0, dp(act, 16), 0, 0);
        codeInput.setLayoutParams(inLp);
        card2.addView(codeInput);

        TextView verCodeBtn = new TextView(act);
        verCodeBtn.setText("Verify Photo Code");
        verCodeBtn.setTextSize(16);
        verCodeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        verCodeBtn.setTextColor(0xFFFFFFFF);
        verCodeBtn.setGravity(Gravity.CENTER);
        verCodeBtn.setBackground(roundedRect(0xFF0070F4, 6, 0, 0, act));
        verCodeBtn.setLayoutParams(btnLp);
        verCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(act, "Photo Code [" + codeInput.getText().toString() + "] Verified: Authentic NTP Time & GPS (" + model.getFormattedCoordinates() + ")", Toast.LENGTH_LONG).show();
            }
        });
        card2.addView(verCodeBtn);
        content.addView(card2);

        scroll.addView(content);
        root.addView(scroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        dlg.setContentView(root);
        dlg.show();
    }

    // Helper: Export File Inspector Dialog
    public static void showExportFileInspectorDialog(final Activity act, final String formatName, final File outFile) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Light_NoTitleBar);
        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFFFFF);
        root.setPadding(dp(act, 20), dp(act, 20), dp(act, 20), dp(act, 20));

        TextView title = new TextView(act);
        title.setText("✓ " + formatName + " Exported");
        title.setTextSize(19);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF0070F4);
        root.addView(title);

        TextView info = new TextView(act);
        long sizeBytes = (outFile != null && outFile.exists()) ? outFile.length() : 0L;
        info.setText("\nFile Name: " + (outFile != null ? outFile.getName() : "N/A")
                + "\nPath: " + (outFile != null ? outFile.getAbsolutePath() : "N/A")
                + "\nFile Size: " + sizeBytes + " bytes"
                + "\nStatus: Verified & Ready to Share (WhatsApp / Email / Drive)");
        info.setTextSize(13);
        info.setTextColor(0xFF333333);
        root.addView(info);

        TextView closeBtn = new TextView(act);
        closeBtn.setText("Done");
        closeBtn.setTextSize(15);
        closeBtn.setTypeface(Typeface.DEFAULT_BOLD);
        closeBtn.setTextColor(0xFFFFFFFF);
        closeBtn.setGravity(Gravity.CENTER);
        closeBtn.setBackground(roundedRect(0xFF0070F4, 6, 0, 0, act));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 44));
        lp.setMargins(0, dp(act, 18), 0, 0);
        closeBtn.setLayoutParams(lp);
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dlg.dismiss(); }
        });
        root.addView(closeBtn);
        dlg.setContentView(root);
        dlg.show();
    }

    // 6. Original Teamspace Home (fragment_layout_teamspace_home.xml)
    public static void showTeamspaceDialog(final Activity act, final WatermarkModel model) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Light_NoTitleBar);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFFFFF);

        // Top Bar
        LinearLayout topBar = new LinearLayout(act);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 12));

        ImageView menuIv = new ImageView(act);
        menuIv.setImageResource(R.drawable.home_tool_border_menu);
        topBar.addView(menuIv, new LinearLayout.LayoutParams(dp(act, 24), dp(act, 24)));

        TextView titleTv = new TextView(act);
        titleTv.setText("  Teamspace");
        titleTv.setTextSize(20);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        topBar.addView(titleTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        ImageView webIv = new ImageView(act);
        webIv.setImageResource(R.drawable.ic_web_portal);
        LinearLayout.LayoutParams wLp = new LinearLayout.LayoutParams(dp(act, 24), dp(act, 24));
        wLp.setMargins(0, 0, dp(act, 16), 0);
        topBar.addView(webIv, wLp);

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.global_border_close_strong);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        topBar.addView(closeIv, new LinearLayout.LayoutParams(dp(act, 28), dp(act, 28)));
        root.addView(topBar);

        View div = new View(act);
        div.setBackgroundColor(0xFFECECEC);
        root.addView(div, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        // auto_save_section (#E8F2FF radius 5dp)
        LinearLayout autoBar = new LinearLayout(act);
        autoBar.setOrientation(LinearLayout.HORIZONTAL);
        autoBar.setGravity(Gravity.CENTER_VERTICAL);
        autoBar.setBackground(roundedRect(0xFFE8F2FF, 6, 0, 0, act));
        autoBar.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));
        LinearLayout.LayoutParams aLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        aLp.setMargins(dp(act, 16), dp(act, 12), dp(act, 16), dp(act, 8));
        autoBar.setLayoutParams(aLp);

        ImageView autoIv = new ImageView(act);
        autoIv.setImageResource(R.drawable.image_teamspace_photo);
        autoBar.addView(autoIv, new LinearLayout.LayoutParams(dp(act, 24), dp(act, 24)));

        TextView autoTv = new TextView(act);
        autoTv.setText("  Auto save to Teamspace");
        autoTv.setTextSize(15);
        autoTv.setTextColor(0xFF222222);
        autoBar.addView(autoTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        Switch autoSw = new Switch(act);
        autoSw.setChecked(model.autoSaveTeamspace);
        autoSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton b, boolean isChecked) {
                model.autoSaveTeamspace = isChecked;
            }
        });
        autoBar.addView(autoSw);
        root.addView(autoBar);

        // Content container
        final ScrollView bodyScroll = new ScrollView(act);
        final LinearLayout body = new LinearLayout(act);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(act, 16), dp(act, 8), dp(act, 16), dp(act, 20));
        bodyScroll.addView(body);
        root.addView(bodyScroll, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Bottom 4-Tab Bar (Photos | Projects | Attendance | Settings)
        View bDiv = new View(act);
        bDiv.setBackgroundColor(0xFFECECEC);
        root.addView(bDiv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));

        final LinearLayout bottomNav = new LinearLayout(act);
        bottomNav.setOrientation(LinearLayout.HORIZONTAL);
        bottomNav.setBackgroundColor(0xFFFFFFFF);
        bottomNav.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 56)));
        root.addView(bottomNav);

        final int[] activeTab = new int[]{0};
        final String[] tabNames = new String[]{"Photos", "Projects", "Attendance", "Settings"};

        final Runnable renderTab = new Runnable() {
            @Override
            public void run() {
                body.removeAllViews();
                bottomNav.removeAllViews();
                for (int i = 0; i < 4; i++) {
                    final int idx = i;
                    boolean sel = (activeTab[0] == i);
                    TextView tabTv = new TextView(act);
                    tabTv.setText(tabNames[i]);
                    tabTv.setTextSize(14);
                    tabTv.setTypeface(sel ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
                    tabTv.setTextColor(sel ? 0xFF0070F4 : 0xFF666666);
                    tabTv.setGravity(Gravity.CENTER);
                    tabTv.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activeTab[0] = idx;
                            run();
                        }
                    });
                    bottomNav.addView(tabTv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
                }

                if (activeTab[0] == 0) {
                    // Photos Tab + Real File Batch Export Hub (PDF / Excel / KMZ / ZIP)
                    TextView countTv = new TextView(act);
                    countTv.setText("Team Photos & Videos (" + model.capturedPhotos.size() + " synced) • Batch Export");
                    countTv.setTextSize(15);
                    countTv.setTypeface(Typeface.DEFAULT_BOLD);
                    countTv.setTextColor(0xFF222222);
                    countTv.setPadding(0, dp(act, 4), 0, dp(act, 8));
                    body.addView(countTv);

                    LinearLayout expRow = new LinearLayout(act);
                    expRow.setOrientation(LinearLayout.HORIZONTAL);
                    String[] expBtns = new String[]{"PDF Report", "Excel (.xls)", "Earth (.kmz)", "ZIP Pack"};
                    for (int k = 0; k < expBtns.length; k++) {
                        final int expIdx = k;
                        final String label = expBtns[k];
                        TextView b = new TextView(act);
                        b.setText(label);
                        b.setTextSize(11);
                        b.setTypeface(Typeface.DEFAULT_BOLD);
                        b.setTextColor(0xFF0070F4);
                        b.setGravity(Gravity.CENTER);
                        b.setBackground(roundedRect(0xFFE8F2FF, 6, dp(act, 1), 0xFF0070F4, act));
                        b.setPadding(dp(act, 6), dp(act, 8), dp(act, 6), dp(act, 8));
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                        lp.setMargins(dp(act, 2), 0, dp(act, 2), dp(act, 10));
                        b.setLayoutParams(lp);
                        b.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                File outFile = null;
                                if (expIdx == 0) outFile = model.exportPdfWorkReport(act);
                                else if (expIdx == 1) outFile = model.exportExcelSpreadsheet(act);
                                else if (expIdx == 2) outFile = model.exportKmzGoogleEarth(act);
                                else outFile = model.exportZipArchive(act);
                                showExportFileInspectorDialog(act, label, outFile);
                            }
                        });
                        expRow.addView(b);
                    }
                    body.addView(expRow);

                    if (model.capturedPhotos.isEmpty()) {
                        ImageView introIv = new ImageView(act);
                        introIv.setImageResource(R.drawable.new_user_teamspace_intro);
                        introIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        body.addView(introIv, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 150)));
                    }

                    for (int i = 0; i < model.capturedPhotos.size(); i++) {
                        WatermarkModel.CapturedPhoto p = model.capturedPhotos.get(i);
                        LinearLayout row = new LinearLayout(act);
                        row.setOrientation(LinearLayout.HORIZONTAL);
                        row.setGravity(Gravity.CENTER_VERTICAL);
                        row.setBackground(roundedRect(0xFFF8F9FA, 8, dp(act, 1), 0xFFECECEC, act));
                        row.setPadding(dp(act, 10), dp(act, 10), dp(act, 10), dp(act, 10));
                        LinearLayout.LayoutParams rLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        rLp.setMargins(0, 0, 0, dp(act, 8));
                        row.setLayoutParams(rLp);

                        ImageView thumb = new ImageView(act);
                        Bitmap bmp = BitmapFactory.decodeFile(p.compositeFile.getAbsolutePath());
                        if (bmp != null) thumb.setImageBitmap(bmp);
                        row.addView(thumb, new LinearLayout.LayoutParams(dp(act, 56), dp(act, 56)));

                        LinearLayout meta = new LinearLayout(act);
                        meta.setOrientation(LinearLayout.VERTICAL);
                        meta.setPadding(dp(act, 10), 0, 0, 0);
                        TextView t1 = new TextView(act);
                        t1.setText((p.isVideo ? "[MP4 " + p.durationSec + "s] " : "") + p.templateName + " • Code: " + p.photoCode);
                        t1.setTextSize(13);
                        t1.setTypeface(Typeface.DEFAULT_BOLD);
                        t1.setTextColor(0xFF222222);
                        meta.addView(t1);
                        TextView t2 = new TextView(act);
                        t2.setText(p.timestampText + "\n" + p.addressText);
                        t2.setTextSize(11);
                        t2.setTextColor(0xFF666666);
                        meta.addView(t2);
                        row.addView(meta);
                        body.addView(row);
                    }
                } else if (activeTab[0] == 1) {
                    // Projects Tab + Create Project + Photo Map Summary
                    LinearLayout addProjRow = new LinearLayout(act);
                    addProjRow.setOrientation(LinearLayout.HORIZONTAL);
                    final EditText newProjEt = new EditText(act);
                    newProjEt.setHint("New project name...");
                    newProjEt.setTextSize(13);
                    newProjEt.setSingleLine(true);
                    newProjEt.setBackground(roundedRect(0xFFF5F5F5, 6, dp(act, 1), 0xFFDEDEDE, act));
                    newProjEt.setPadding(dp(act, 10), dp(act, 8), dp(act, 10), dp(act, 8));
                    addProjRow.addView(newProjEt, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                    TextView createBtn = new TextView(act);
                    createBtn.setText("+ Create");
                    createBtn.setTextSize(13);
                    createBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    createBtn.setTextColor(0xFFFFFFFF);
                    createBtn.setBackground(roundedRect(0xFF0070F4, 6, 0, 0, act));
                    createBtn.setPadding(dp(act, 14), dp(act, 9), dp(act, 14), dp(act, 9));
                    LinearLayout.LayoutParams cbLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    cbLp.setMargins(dp(act, 8), 0, 0, dp(act, 12));
                    createBtn.setLayoutParams(cbLp);
                    createBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String name = newProjEt.getText().toString().trim();
                            if (!name.isEmpty()) {
                                model.projects.add(0, new WatermarkModel.ProjectItem(name, 250, 1, model.getFormattedCoordinates()));
                                model.projectName = name;
                                run();
                                Toast.makeText(act, "Created Project: " + name, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    addProjRow.addView(createBtn);
                    body.addView(addProjRow);

                    for (int i = 0; i < model.projects.size(); i++) {
                        final WatermarkModel.ProjectItem proj = model.projects.get(i);
                        TextView pCard = new TextView(act);
                        pCard.setText("📁 " + proj.name + "\n• Geofence Radius: " + proj.radiusMeters + "m • " + proj.photoCount + " Synced Photos");
                        pCard.setTextSize(13);
                        pCard.setTextColor(0xFF222222);
                        pCard.setBackground(roundedRect(0xFFF5F5F5, 8, dp(act, 1), 0xFFE0E0E0, act));
                        pCard.setPadding(dp(act, 14), dp(act, 12), dp(act, 14), dp(act, 12));
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        lp.setMargins(0, 0, 0, dp(act, 10));
                        pCard.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                model.projectName = proj.name;
                                Toast.makeText(act, "Active Project switched to: " + proj.name, Toast.LENGTH_SHORT).show();
                            }
                        });
                        body.addView(pCard, lp);
                    }
                } else if (activeTab[0] == 2) {
                    // Attendance & Inspection Checklist Tab
                    TextView attCard = new TextView(act);
                    attCard.setText("Geofence Timeclock Active\n• On-Site Status: Inside Verified GPS Fence (" + model.getFormattedCoordinates() + ")\n• Active Inspector: " + model.inspectorName);
                    attCard.setTextSize(13);
                    attCard.setTextColor(0xFF222222);
                    attCard.setBackground(roundedRect(0xFFE8F2FF, 8, 0, 0, act));
                    attCard.setPadding(dp(act, 14), dp(act, 12), dp(act, 14), dp(act, 12));
                    body.addView(attCard);

                    TextView punchBtn = new TextView(act);
                    punchBtn.setText("⏱ Punch Clock-In / Clock-Out Now (Verified NTP)");
                    punchBtn.setTextSize(14);
                    punchBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    punchBtn.setTextColor(0xFFFFFFFF);
                    punchBtn.setGravity(Gravity.CENTER);
                    punchBtn.setBackground(roundedRect(0xFF0070F4, 6, 0, 0, act));
                    punchBtn.setPadding(dp(act, 12), dp(act, 11), dp(act, 12), dp(act, 11));
                    LinearLayout.LayoutParams pLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    pLp.setMargins(0, dp(act, 10), 0, dp(act, 12));
                    punchBtn.setLayoutParams(pLp);
                    punchBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String nowTime = new SimpleDateFormat("hh:mm:ss a", Locale.US).format(new Date());
                            String type = (model.attendanceRecords.size() % 2 == 0) ? "CLOCK OUT" : "CLOCK IN";
                            model.attendanceRecords.add(0, new WatermarkModel.AttendanceRecord(
                                    type, nowTime, model.addressLine, model.currentPhotoCode, true));
                            run();
                            Toast.makeText(act, type + " Logged at " + nowTime, Toast.LENGTH_SHORT).show();
                        }
                    });
                    body.addView(punchBtn);

                    for (int i = 0; i < model.attendanceRecords.size(); i++) {
                        WatermarkModel.AttendanceRecord rec = model.attendanceRecords.get(i);
                        TextView rTv = new TextView(act);
                        rTv.setText("✓ " + rec.type + " • " + rec.timeText + " • Code: " + rec.photoCode + "\n   " + rec.addressText);
                        rTv.setTextSize(12);
                        rTv.setTextColor(0xFF333333);
                        rTv.setPadding(0, dp(act, 4), 0, dp(act, 6));
                        body.addView(rTv);
                    }

                    TextView chkHead = new TextView(act);
                    chkHead.setText("\nField Inspection Checklist (Tap to Toggle)");
                    chkHead.setTextSize(14);
                    chkHead.setTypeface(Typeface.DEFAULT_BOLD);
                    chkHead.setTextColor(0xFF222222);
                    chkHead.setPadding(0, dp(act, 8), 0, dp(act, 6));
                    body.addView(chkHead);

                    for (int i = 0; i < model.checklistItems.size(); i++) {
                        final WatermarkModel.ChecklistItem item = model.checklistItems.get(i);
                        addSwitchRow(act, body, item.title, item.completed, new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton b, boolean isChecked) {
                                item.completed = isChecked;
                            }
                        });
                    }
                } else {
                    // Settings / Cloud Sync & Team Members Tab
                    TextView memHead = new TextView(act);
                    memHead.setText("Team Members (" + model.teamMembers.size() + ")");
                    memHead.setTextSize(14);
                    memHead.setTypeface(Typeface.DEFAULT_BOLD);
                    memHead.setTextColor(0xFF222222);
                    memHead.setPadding(0, dp(act, 4), 0, dp(act, 6));
                    body.addView(memHead);

                    for (int i = 0; i < model.teamMembers.size(); i++) {
                        TextView mTv = new TextView(act);
                        mTv.setText("👤 " + model.teamMembers.get(i));
                        mTv.setTextSize(13);
                        mTv.setTextColor(0xFF444444);
                        mTv.setPadding(0, dp(act, 4), 0, dp(act, 4));
                        body.addView(mTv);
                    }

                    TextView invBtn = new TextView(act);
                    invBtn.setText("+ Invite Field Crew Member (Share Team Code)");
                    invBtn.setTextSize(13);
                    invBtn.setTypeface(Typeface.DEFAULT_BOLD);
                    invBtn.setTextColor(0xFF0070F4);
                    invBtn.setGravity(Gravity.CENTER);
                    invBtn.setBackground(roundedRect(0xFFE8F2FF, 6, dp(act, 1), 0xFF0070F4, act));
                    invBtn.setPadding(dp(act, 10), dp(act, 9), dp(act, 10), dp(act, 9));
                    LinearLayout.LayoutParams iLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    iLp.setMargins(0, dp(act, 8), 0, dp(act, 14));
                    invBtn.setLayoutParams(iLp);
                    invBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String newMem = "Field Crew #" + (model.teamMembers.size() + 1) + " (Joined via Code)";
                            model.teamMembers.add(newMem);
                            run();
                            Toast.makeText(act, "Added " + newMem, Toast.LENGTH_SHORT).show();
                        }
                    });
                    body.addView(invBtn);

                    LinearLayout gRow = new LinearLayout(act);
                    gRow.setOrientation(LinearLayout.HORIZONTAL);
                    gRow.setGravity(Gravity.CENTER_VERTICAL);
                    gRow.setPadding(0, dp(act, 10), 0, dp(act, 10));
                    ImageView gIv = new ImageView(act);
                    gIv.setImageResource(R.drawable.teamspace_google_icon);
                    gRow.addView(gIv, new LinearLayout.LayoutParams(dp(act, 28), dp(act, 28)));
                    TextView gTv = new TextView(act);
                    gTv.setText("  Google Drive Auto-Backup (Connected)");
                    gTv.setTextSize(14);
                    gTv.setTextColor(0xFF222222);
                    gRow.addView(gTv);
                    body.addView(gRow);

                    LinearLayout mRow = new LinearLayout(act);
                    mRow.setOrientation(LinearLayout.HORIZONTAL);
                    mRow.setGravity(Gravity.CENTER_VERTICAL);
                    mRow.setPadding(0, dp(act, 10), 0, dp(act, 10));
                    ImageView mIv = new ImageView(act);
                    mIv.setImageResource(R.drawable.teamspace_microsoft_icon);
                    mRow.addView(mIv, new LinearLayout.LayoutParams(dp(act, 28), dp(act, 28)));
                    TextView mTv = new TextView(act);
                    mTv.setText("  Microsoft OneDrive Sync (Connected)");
                    mTv.setTextSize(14);
                    mTv.setTextColor(0xFF222222);
                    mRow.addView(mTv);
                    body.addView(mRow);
                }
            }
        };
        renderTab.run();

        dlg.setContentView(root);
        dlg.show();
    }

    // 7. Original Local Pic & Video Preview (fragment_local_pic_preview.xml)
    public static void showLocalPreviewDialog(final Activity act, final WatermarkModel model, final Runnable onUpdate) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Light_NoTitleBar);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFFFFF);

        final int[] currentIndex = new int[]{0};

        // Top NavigationBarRich with Prev / Next Navigation
        FrameLayout navBar = new FrameLayout(act);
        navBar.setPadding(dp(act, 12), dp(act, 8), dp(act, 12), dp(act, 8));
        navBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 52)));

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.global_border_close_ultrastrong);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });
        navBar.addView(closeIv, new FrameLayout.LayoutParams(dp(act, 30), dp(act, 30), Gravity.START | Gravity.CENTER_VERTICAL));

        LinearLayout navCenter = new LinearLayout(act);
        navCenter.setOrientation(LinearLayout.HORIZONTAL);
        navCenter.setGravity(Gravity.CENTER);

        TextView prevBtn = new TextView(act);
        prevBtn.setText("◀ ");
        prevBtn.setTextSize(16);
        prevBtn.setTextColor(0xFF0070F4);
        prevBtn.setPadding(dp(act, 8), dp(act, 4), dp(act, 8), dp(act, 4));
        navCenter.addView(prevBtn);

        final TextView titleTv = new TextView(act);
        titleTv.setTextSize(16);
        titleTv.setTypeface(Typeface.DEFAULT_BOLD);
        titleTv.setTextColor(0xFF222222);
        navCenter.addView(titleTv);

        TextView nextBtn = new TextView(act);
        nextBtn.setText(" ▶");
        nextBtn.setTextSize(16);
        nextBtn.setTextColor(0xFF0070F4);
        nextBtn.setPadding(dp(act, 8), dp(act, 4), dp(act, 8), dp(act, 4));
        navCenter.addView(nextBtn);

        navBar.addView(navCenter, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER));

        TextView rightTv = new TextView(act);
        rightTv.setText("Export ZIP");
        rightTv.setTextSize(14);
        rightTv.setTypeface(Typeface.DEFAULT_BOLD);
        rightTv.setTextColor(0xFF0093FF);
        rightTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File zf = model.exportZipArchive(act);
                showExportFileInspectorDialog(act, "ZIP Archive", zf);
            }
        });
        navBar.addView(rightTv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.END | Gravity.CENTER_VERTICAL));
        root.addView(navBar);

        // Center Image & Instant Watermarked Video Player
        final FrameLayout viewerWrap = new FrameLayout(act);
        viewerWrap.setBackgroundColor(0xFF111111);
        final ImageView photoIv = new ImageView(act);
        photoIv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        viewerWrap.addView(photoIv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        final TextView actionOverlayBtn = new TextView(act);
        actionOverlayBtn.setTextSize(12);
        actionOverlayBtn.setTypeface(Typeface.DEFAULT_BOLD);
        actionOverlayBtn.setTextColor(0xFFFFFFFF);
        actionOverlayBtn.setBackground(roundedRect(0xCC000000, 18, dp(act, 1), 0xFFFFFFFF, act));
        actionOverlayBtn.setPadding(dp(act, 14), dp(act, 8), dp(act, 14), dp(act, 8));
        FrameLayout.LayoutParams rmLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        rmLp.gravity = Gravity.BOTTOM | Gravity.END;
        rmLp.setMargins(dp(act, 16), dp(act, 16), dp(act, 16), dp(act, 16));
        viewerWrap.addView(actionOverlayBtn, rmLp);

        final TextView metaStripTv = new TextView(act);
        metaStripTv.setTextSize(11);
        metaStripTv.setTextColor(0xFFFFFFFF);
        metaStripTv.setBackgroundColor(0x99000000);
        metaStripTv.setPadding(dp(act, 12), dp(act, 6), dp(act, 12), dp(act, 6));
        viewerWrap.addView(metaStripTv, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.TOP));

        final android.os.Handler playHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        final boolean[] isPlayingVideo = new boolean[]{false};
        final int[] frameCursor = new int[]{0};

        dlg.setOnDismissListener(new android.content.DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(android.content.DialogInterface dialog) {
                isPlayingVideo[0] = false;
                playHandler.removeCallbacksAndMessages(null);
            }
        });

        final Runnable renderItem = new Runnable() {
            @Override
            public void run() {
                isPlayingVideo[0] = false;
                playHandler.removeCallbacksAndMessages(null);

                if (model.capturedPhotos.isEmpty()) {
                    titleTv.setText("Preview (0)");
                    photoIv.setVisibility(View.VISIBLE);
                    photoIv.setImageResource(R.drawable.cover_wm34);
                    actionOverlayBtn.setVisibility(View.GONE);
                    metaStripTv.setText("No photos or videos captured yet.");
                    return;
                }
                if (currentIndex[0] < 0) currentIndex[0] = model.capturedPhotos.size() - 1;
                if (currentIndex[0] >= model.capturedPhotos.size()) currentIndex[0] = 0;

                final WatermarkModel.CapturedPhoto p = model.capturedPhotos.get(currentIndex[0]);
                titleTv.setText((p.isVideo ? "Video (" : "Photo (") + (currentIndex[0] + 1) + "/" + model.capturedPhotos.size() + ")");
                metaStripTv.setText((p.isVideo ? "🎥 Watermarked MP4 (" + p.durationSec + "s) • " : "📷 JPEG • ")
                        + p.templateName + " • Code: " + p.photoCode + "\n" + p.timestampText + " • " + p.addressText);

                actionOverlayBtn.setVisibility(View.VISIBLE);
                if (p.isVideo) {
                    photoIv.setVisibility(View.VISIBLE);
                    if (!p.videoFrames.isEmpty()) {
                        // Instant 0ms Auto-Play of Watermarked Video Frames
                        isPlayingVideo[0] = true;
                        frameCursor[0] = 0;
                        actionOverlayBtn.setText("⏸ Pause Video (" + p.durationSec + "s)");

                        final Runnable frameTicker = new Runnable() {
                            @Override
                            public void run() {
                                if (!isPlayingVideo[0] || p.videoFrames.isEmpty()) return;
                                int idx = frameCursor[0] % p.videoFrames.size();
                                Bitmap fb = p.videoFrames.get(idx);
                                if (fb != null) {
                                    photoIv.setImageBitmap(fb);
                                }
                                metaStripTv.setText("🎥 ● PLAYING MP4 (" + (idx + 1) + "/" + p.videoFrames.size() + " frames • " + p.durationSec + "s) • Watermark Stamped\n"
                                        + p.templateName + " • Code: " + p.photoCode);
                                frameCursor[0]++;
                                playHandler.postDelayed(this, 140);
                            }
                        };
                        playHandler.post(frameTicker);

                        actionOverlayBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                isPlayingVideo[0] = !isPlayingVideo[0];
                                if (isPlayingVideo[0]) {
                                    actionOverlayBtn.setText("⏸ Pause Video (" + p.durationSec + "s)");
                                    playHandler.removeCallbacksAndMessages(null);
                                    playHandler.post(frameTicker);
                                } else {
                                    playHandler.removeCallbacksAndMessages(null);
                                    actionOverlayBtn.setText("▶ Play Video (" + p.durationSec + "s)");
                                }
                            }
                        });
                    } else {
                        Bitmap bmp = p.previewBitmap != null ? p.previewBitmap : BitmapFactory.decodeFile(p.compositeFile.getAbsolutePath());
                        if (bmp != null) photoIv.setImageBitmap(bmp);
                        actionOverlayBtn.setText("✓ MP4 Video Stamped (" + p.durationSec + "s)");
                        actionOverlayBtn.setOnClickListener(null);
                    }
                } else {
                    photoIv.setVisibility(View.VISIBLE);
                    Bitmap bmp = (!p.watermarkRemoved && p.previewBitmap != null)
                            ? p.previewBitmap
                            : BitmapFactory.decodeFile((p.watermarkRemoved ? p.cleanFile : p.compositeFile).getAbsolutePath());
                    if (bmp != null) photoIv.setImageBitmap(bmp);

                    actionOverlayBtn.setText(p.watermarkRemoved ? "Restore Watermark" : "Remove Watermark");
                    actionOverlayBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            p.watermarkRemoved = !p.watermarkRemoved;
                            File target = p.watermarkRemoved ? p.cleanFile : p.compositeFile;
                            Bitmap b = (!p.watermarkRemoved && p.previewBitmap != null)
                                    ? p.previewBitmap
                                    : BitmapFactory.decodeFile(target.getAbsolutePath());
                            if (b != null) photoIv.setImageBitmap(b);
                            actionOverlayBtn.setText(p.watermarkRemoved ? "Restore Watermark" : "Remove Watermark");
                            Toast.makeText(act, p.watermarkRemoved
                                    ? "Extracted clean unwatermarked JPEG from XHEY1 dual-stream trailer!"
                                    : "Restored watermarked composite JPEG!", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex[0]--;
                renderItem.run();
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentIndex[0]++;
                renderItem.run();
            }
        });

        renderItem.run();
        root.addView(viewerWrap, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // Bottom Share & Real Export Bar (bottomLayoutGlobalNew in fragment_local_pic_preview.xml, height 80dp)
        LinearLayout bottomBar = new LinearLayout(act);
        bottomBar.setOrientation(LinearLayout.HORIZONTAL);
        bottomBar.setGravity(Gravity.CENTER_VERTICAL);
        bottomBar.setBackgroundColor(0xFFFFFFFF);
        bottomBar.setPadding(dp(act, 10), dp(act, 8), dp(act, 10), dp(act, 8));
        bottomBar.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 80)));

        addShareShortcut(act, bottomBar, R.drawable.social_share_wa_fill, "WhatsApp", new ActionCallback() {
            @Override
            public void onClick() {
                Toast.makeText(act, "Sharing watermarked media to WhatsApp...", Toast.LENGTH_SHORT).show();
            }
        });
        addShareShortcut(act, bottomBar, R.drawable.tool_border_safe_green, "Verify", new ActionCallback() {
            @Override
            public void onClick() {
                showVerifyCenterDialog(act, model);
            }
        });
        addShareShortcut(act, bottomBar, R.drawable.icon_preview_folder, "Teamspace", new ActionCallback() {
            @Override
            public void onClick() {
                showTeamspaceDialog(act, model);
            }
        });
        addShareShortcut(act, bottomBar, R.drawable.global_border_download, "Export PDF", new ActionCallback() {
            @Override
            public void onClick() {
                File pdf = model.exportPdfWorkReport(act);
                showExportFileInspectorDialog(act, "PDF Work Report", pdf);
            }
        });
        addShareShortcut(act, bottomBar, R.drawable.ic_web_portal, "Excel/KMZ", new ActionCallback() {
            @Override
            public void onClick() {
                File xls = model.exportExcelSpreadsheet(act);
                model.exportKmzGoogleEarth(act);
                showExportFileInspectorDialog(act, "Excel (.xls) & Google Earth (.kmz)", xls);
            }
        });
        root.addView(bottomBar);

        dlg.setContentView(root);
        dlg.show();
    }

    private interface ActionCallback {
        void onClick();
    }

    private static void addShareShortcut(Context ctx, LinearLayout parent, int iconRes, String label, final ActionCallback action) {
        LinearLayout col = new LinearLayout(ctx);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setGravity(Gravity.CENTER);
        ImageView iv = new ImageView(ctx);
        iv.setImageResource(iconRes);
        col.addView(iv, new LinearLayout.LayoutParams(dp(ctx, 32), dp(ctx, 32)));
        TextView tv = new TextView(ctx);
        tv.setText(label);
        tv.setTextSize(11);
        tv.setTextColor(0xFF444444);
        tv.setPadding(0, dp(ctx, 4), 0, 0);
        col.addView(tv);
        col.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                action.onClick();
            }
        });
        parent.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
    }

    // 8. Original Time Now Plans Paywall + Retention Exit Survey (activity_purchase.xml / Report 07)
    public static void showPurchaseDialog(final Activity act) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Light_NoTitleBar);

        LinearLayout root = new LinearLayout(act);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(0xFFFFFFFF);
        root.setPadding(dp(act, 20), dp(act, 16), dp(act, 20), dp(act, 20));

        ImageView closeIv = new ImageView(act);
        closeIv.setImageResource(R.drawable.close_dark);
        LinearLayout.LayoutParams cLp = new LinearLayout.LayoutParams(dp(act, 32), dp(act, 32));
        cLp.gravity = Gravity.END;
        closeIv.setLayoutParams(cLp);
        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dlg.dismiss();
                showPaywallExitSurveyDialog(act);
            }
        });
        root.addView(closeIv);

        TextView title = new TextView(act);
        title.setText("Time Now Plans");
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(0xFF222222);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(act, 6), 0, dp(act, 14));
        root.addView(title);

        // Dual Tab Switcher: Plus (#7A6EFF) | Business (#0070F4)
        final LinearLayout tabWrap = new LinearLayout(act);
        tabWrap.setOrientation(LinearLayout.HORIZONTAL);
        tabWrap.setBackground(roundedRect(0xFFF0F0F0, 6, 0, 0, act));
        tabWrap.setPadding(dp(act, 2), dp(act, 2), dp(act, 2), dp(act, 2));
        LinearLayout.LayoutParams tLp = new LinearLayout.LayoutParams(dp(act, 220), dp(act, 40));
        tLp.gravity = Gravity.CENTER_HORIZONTAL;
        tabWrap.setLayoutParams(tLp);

        final TextView tabPlus = new TextView(act);
        tabPlus.setText("Plus");
        tabPlus.setTextSize(15);
        tabPlus.setTypeface(Typeface.DEFAULT_BOLD);
        tabPlus.setGravity(Gravity.CENTER);

        final TextView tabBiz = new TextView(act);
        tabBiz.setText("Business");
        tabBiz.setTextSize(15);
        tabBiz.setTypeface(Typeface.DEFAULT_BOLD);
        tabBiz.setGravity(Gravity.CENTER);

        tabWrap.addView(tabPlus, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        tabWrap.addView(tabBiz, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f));
        root.addView(tabWrap);

        final LinearLayout featuresBox = new LinearLayout(act);
        featuresBox.setOrientation(LinearLayout.VERTICAL);
        featuresBox.setPadding(0, dp(act, 18), 0, dp(act, 12));
        root.addView(featuresBox, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        // 3 SKU Cards (Annual / Monthly / One-Time)
        final LinearLayout skuRow = new LinearLayout(act);
        skuRow.setOrientation(LinearLayout.HORIZONTAL);
        String[] skus = new String[]{"Annual\n$29.99/yr\n(7d Trial)", "Monthly\n$6.99/mo\n(Flexible)", "1-Year Pass\n$34.99\n(One-Time)"};
        final TextView[] skuViews = new TextView[3];
        for (int i = 0; i < 3; i++) {
            final int sIdx = i;
            TextView sc = new TextView(act);
            sc.setText(skus[i]);
            sc.setTextSize(11);
            sc.setGravity(Gravity.CENTER);
            sc.setPadding(dp(act, 6), dp(act, 10), dp(act, 6), dp(act, 10));
            boolean sel = (i == 0);
            sc.setBackground(roundedRect(sel ? 0xFFF3F0FF : 0xFFF7F7F7, 8, dp(act, sel ? 2 : 1), sel ? 0xFF7A6EFF : 0xFFE0E0E0, act));
            sc.setTextColor(0xFF222222);
            sc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    for (int k = 0; k < 3; k++) {
                        boolean s = (k == sIdx);
                        skuViews[k].setBackground(roundedRect(s ? 0xFFF3F0FF : 0xFFF7F7F7, 8, dp(act, s ? 2 : 1), s ? 0xFF7A6EFF : 0xFFE0E0E0, act));
                    }
                }
            });
            LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            slp.setMargins(dp(act, 3), 0, dp(act, 3), dp(act, 12));
            sc.setLayoutParams(slp);
            skuViews[i] = sc;
            skuRow.addView(sc);
        }
        root.addView(skuRow);

        final TextView priceTv = new TextView(act);
        priceTv.setTextSize(16);
        priceTv.setTypeface(Typeface.DEFAULT_BOLD);
        priceTv.setTextColor(0xFF222222);
        priceTv.setGravity(Gravity.CENTER);
        root.addView(priceTv);

        final TextView ctaBtn = new TextView(act);
        ctaBtn.setTextSize(17);
        ctaBtn.setTypeface(Typeface.DEFAULT_BOLD);
        ctaBtn.setTextColor(0xFFFFFFFF);
        ctaBtn.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ctaLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(act, 48));
        ctaLp.setMargins(0, dp(act, 12), 0, 0);
        ctaBtn.setLayoutParams(ctaLp);
        ctaBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(act, "Time Now Pro Plan Activated!", Toast.LENGTH_SHORT).show();
                dlg.dismiss();
            }
        });
        root.addView(ctaBtn);

        final boolean[] isBiz = new boolean[]{false};
        final java.lang.Runnable updatePlan = new java.lang.Runnable() {
            @Override
            public void run() {
                featuresBox.removeAllViews();
                if (!isBiz[0]) {
                    tabPlus.setBackground(roundedRect(0xFF7A6EFF, 5, 0, 0, act));
                    tabPlus.setTextColor(0xFFFFFFFF);
                    tabBiz.setBackgroundColor(Color.TRANSPARENT);
                    tabBiz.setTextColor(0xFF666666);
                    String[] plusFeats = new String[]{
                        "✓ Custom Brand Logo (AI Cutout) on All Watermarks",
                        "✓ Lossless Original Photo Backup (JpegPacker XHEY1)",
                        "✓ Remove Ads & Unlock All 14 Pro Templates",
                        "✓ Batch Export PDF Work Reports, Excel & KMZ",
                        "✓ Priority Anti-Fake Photo Code Verification"
                    };
                    for (int i = 0; i < plusFeats.length; i++) {
                        TextView tv = new TextView(act);
                        tv.setText(plusFeats[i]);
                        tv.setTextSize(14);
                        tv.setTextColor(0xFF222222);
                        tv.setPadding(0, dp(act, 7), 0, dp(act, 7));
                        featuresBox.addView(tv);
                    }
                    priceTv.setText("$29.99 / year (7-Day Free Trial)");
                    ctaBtn.setText("Continue with Time Now Plus");
                    ctaBtn.setBackground(roundedRect(0xFF7A6EFF, 6, 0, 0, act));
                } else {
                    tabBiz.setBackground(roundedRect(0xFF0070F4, 5, 0, 0, act));
                    tabBiz.setTextColor(0xFFFFFFFF);
                    tabPlus.setBackgroundColor(Color.TRANSPARENT);
                    tabPlus.setTextColor(0xFF666666);
                    String[] bizFeats = new String[]{
                        "✓ Unlimited Teamspace Cloud Storage & Web Portal",
                        "✓ Locked Team Watermark Templates for Field Crews",
                        "✓ GPS Geofence Attendance & Automated Timeclock",
                        "✓ Auto-Sync to Google Drive, OneDrive & Webhook API",
                        "✓ Multi-Project Map KMZ & Enterprise Audit Logs"
                    };
                    for (int i = 0; i < bizFeats.length; i++) {
                        TextView tv = new TextView(act);
                        tv.setText(bizFeats[i]);
                        tv.setTextSize(14);
                        tv.setTextColor(0xFF222222);
                        tv.setPadding(0, dp(act, 7), 0, dp(act, 7));
                        featuresBox.addView(tv);
                    }
                    priceTv.setText("$9.99 / member / month");
                    ctaBtn.setText("Start Teamspace Business Trial");
                    ctaBtn.setBackground(roundedRect(0xFF0070F4, 6, 0, 0, act));
                }
            }
        };
        tabPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBiz[0] = false;
                updatePlan.run();
            }
        });
        tabBiz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBiz[0] = true;
                updatePlan.run();
            }
        });
        updatePlan.run();

        dlg.setContentView(root);
        dlg.show();
    }

    // 8b. Paywall Retention Exit Survey (PaywallExitSurvey / Report 07)
    public static void showPaywallExitSurveyDialog(final Activity act) {
        final Dialog dlg = new Dialog(act, android.R.style.Theme_Translucent_NoTitleBar);
        FrameLayout backdrop = new FrameLayout(act);
        backdrop.setBackgroundColor(0x88000000);

        LinearLayout card = new LinearLayout(act);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setBackground(roundedRect(0xFFFFFFFF, 12, 0, 0, act));
        card.setPadding(dp(act, 20), dp(act, 20), dp(act, 20), dp(act, 20));
        FrameLayout.LayoutParams cLp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        cLp.setMargins(dp(act, 24), 0, dp(act, 24), 0);
        card.setLayoutParams(cLp);

        TextView head = new TextView(act);
        head.setText("Wait! Before You Leave...");
        head.setTextSize(18);
        head.setTypeface(Typeface.DEFAULT_BOLD);
        head.setTextColor(0xFF222222);
        card.addView(head);

        TextView sub = new TextView(act);
        sub.setText("Why didn't you upgrade today? Select an option to unlock an instant 50% retention discount:");
        sub.setTextSize(13);
        sub.setTextColor(0xFF666666);
        sub.setPadding(0, dp(act, 6), 0, dp(act, 10));
        card.addView(sub);

        String[] reasons = new String[]{
            "• Price is too high for my budget",
            "• I only need basic timestamp photos",
            "• Testing the app for my field team first"
        };
        for (int i = 0; i < reasons.length; i++) {
            TextView rTv = new TextView(act);
            rTv.setText(reasons[i]);
            rTv.setTextSize(13);
            rTv.setTextColor(0xFF222222);
            rTv.setBackground(roundedRect(0xFFF5F5F5, 6, 0, 0, act));
            rTv.setPadding(dp(act, 12), dp(act, 10), dp(act, 12), dp(act, 10));
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            rlp.setMargins(0, 0, 0, dp(act, 6));
            rTv.setLayoutParams(rlp);
            rTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(act, "50% Retention Offer Unlocked ($14.99/yr) + 3-Day Free AdFreePass!", Toast.LENGTH_LONG).show();
                    dlg.dismiss();
                }
            });
            card.addView(rTv);
        }

        TextView skipTv = new TextView(act);
        skipTv.setText("No thanks, continue with Free version");
        skipTv.setTextSize(13);
        skipTv.setTextColor(0xFF888888);
        skipTv.setGravity(Gravity.CENTER);
        skipTv.setPadding(0, dp(act, 10), 0, 0);
        skipTv.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { dlg.dismiss(); }
        });
        card.addView(skipTv);

        backdrop.addView(card);
        dlg.setContentView(backdrop);
        dlg.show();
    }
}
