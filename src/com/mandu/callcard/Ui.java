package com.mandu.callcard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Ui {

    public static final ExecutorService BG = Executors.newFixedThreadPool(3);
    public static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static int dp(Context c, float v) {
        return Math.round(v * c.getResources().getDisplayMetrics().density);
    }

    public static void size(View v, Context c, float w, float h) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(dp(c, w), dp(c, h));
        } else {
            lp.width = dp(c, w);
            lp.height = dp(c, h);
        }
        v.setLayoutParams(lp);
    }

    public static void pad(View v, Context c, float l, float t, float r, float b) {
        v.setPadding(dp(c, l), dp(c, t), dp(c, r), dp(c, b));
    }

    public static GradientDrawable bg(int color, float radius, Context c) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(c, radius));
        return g;
    }

    public static GradientDrawable strokeBg(int fill, int strokeColor, float radius, Context c) {
        GradientDrawable g = bg(fill, radius, c);
        g.setStroke(Math.max(1, dp(c, 1f)), strokeColor);
        return g;
    }

    public static GradientDrawable gradient(int a, int b, float radius, Context c) {
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{a, b});
        g.setCornerRadius(dp(c, radius));
        return g;
    }

    public static GradientDrawable oval(int color) {
        GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        return g;
    }

    public static void pressable(View v, GradientDrawable shape, int rippleColor) {
        v.setBackground(new RippleDrawable(ColorStateList.valueOf(rippleColor), shape, null));
    }

    public static void ripple(View v, float radius, int fill, int rippleColor, Context c) {
        pressable(v, bg(fill, radius, c), rippleColor);
    }

    public static TextView tv(Context c, String s, float size, int color, boolean medium) {
        TextView t = new TextView(c);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.create(medium ? "sans-serif-medium" : "sans-serif", Typeface.NORMAL));
        t.setIncludeFontPadding(false);
        return t;
    }

    public static TextView title(Context c, String s, Theme t) {
        TextView v = tv(c, s, 22, t.text, true);
        v.setLetterSpacing(-0.02f);
        return v;
    }

    public static TextView label(Context c, String s, Theme t) {
        TextView v = tv(c, s, 11, t.dim, true);
        v.setLetterSpacing(0.12f);
        v.setAllCaps(true);
        return v;
    }

    public static ImageView icon(Context c, String name, float sizeDp, int color) {
        ImageView iv = new ImageView(c);
        iv.setImageResource(iconRes(c, name));
        iv.setColorFilter(color);
        iv.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int px = dp(c, sizeDp);
        iv.setLayoutParams(new LinearLayout.LayoutParams(px, px));
        return iv;
    }

    public static int iconRes(Context c, String name) {
        return c.getResources().getIdentifier("ic_" + name, "drawable", c.getPackageName());
    }

    /** Circular avatar with initials. */
    public static TextView avatar(Context c, String name, String number, float sizeDp, int bgColor, int textColor) {
        TextView v = new TextView(c);
        v.setText(Data.initials(name, number));
        v.setTextSize(sizeDp * 0.36f);
        v.setTextColor(textColor);
        v.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
        v.setGravity(Gravity.CENTER);
        v.setBackground(oval(bgColor));
        v.setLayoutParams(new LinearLayout.LayoutParams(dp(c, sizeDp), dp(c, sizeDp)));
        return v;
    }

    public static int avatarColor(String key) {
        int[] palette = {0xFF2C6BED, 0xFF8B5CF6, 0xFF0EA5A5, 0xFFE0692B, 0xFFC0392B,
                0xFF1F8A70, 0xFFB7791F, 0xFF5B21B6, 0xFF0F766E, 0xFFBE185D};
        int h = key == null ? 0 : key.hashCode();
        return palette[Math.abs(h) % palette.length];
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        return l;
    }

    public static LinearLayout col(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        return l;
    }

    public static View divider(Context c, Theme t) {
        View v = new View(c);
        v.setBackgroundColor(t.divider);
        v.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, dp(c, 0.6f))));
        return v;
    }

    public static LinearLayout card(Context c, Theme t) {
        LinearLayout l = col(c);
        l.setBackground(strokeBg(t.surface, t.divider, 18, c));
        pad(l, c, 16, 14, 16, 14);
        return l;
    }

    public static int statusBarHeight(Context c) {
        int id = c.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? c.getResources().getDimensionPixelSize(id) : dp(c, 24);
    }

    public static void topInset(View v, Context c) {
        v.setPadding(v.getPaddingLeft(), statusBarHeight(c), v.getPaddingRight(), v.getPaddingBottom());
    }

    public static void toast(Context c, String s) {
        android.widget.Toast.makeText(c, s, android.widget.Toast.LENGTH_SHORT).show();
    }
}
