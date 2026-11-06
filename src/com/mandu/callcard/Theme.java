package com.mandu.callcard;

import android.content.Context;

public class Theme {

    public static final int DARK = 0;
    public static final int LIGHT = 1;
    public static final int AMOLED = 2;

    public int mode;
    public int bg;
    public int surface;
    public int surface2;
    public int accent;
    public int accent2;
    public int text;
    public int dim;
    public int danger;
    public int green;
    public int divider;
    public int navBg;

    public static Theme get(Context c) {
        Theme t = new Theme();
        t.mode = App.p(c).getInt("theme", DARK);
        if (t.mode == LIGHT) {
            t.bg = 0xFFF2F5FA;
            t.surface = 0xFFFFFFFF;
            t.surface2 = 0xFFE8EEF6;
            t.accent = 0xFF0E9F92;
            t.accent2 = 0xFF5B4BE0;
            t.text = 0xFF0F1720;
            t.dim = 0xFF6B7A90;
            t.danger = 0xFFE33F52;
            t.green = 0xFF12A66A;
            t.divider = 0x14000000;
            t.navBg = 0xFFFFFFFF;
        } else if (t.mode == AMOLED) {
            t.bg = 0xFF000000;
            t.surface = 0xFF0B0B0D;
            t.surface2 = 0xFF16161A;
            t.accent = 0xFF4DD0C4;
            t.accent2 = 0xFF7C6CFF;
            t.text = 0xFFF4F7FB;
            t.dim = 0xFF7F8A9C;
            t.danger = 0xFFFF5C6C;
            t.green = 0xFF34D399;
            t.divider = 0x1FFFFFFF;
            t.navBg = 0xFF000000;
        } else {
            t.bg = 0xFF0A0E17;
            t.surface = 0xFF131A26;
            t.surface2 = 0xFF1B2433;
            t.accent = 0xFF4DD0C4;
            t.accent2 = 0xFF7C6CFF;
            t.text = 0xFFF2F5FA;
            t.dim = 0xFF8A97AB;
            t.danger = 0xFFFF5C6C;
            t.green = 0xFF34D399;
            t.divider = 0x1FFFFFFF;
            t.navBg = 0xFF0D131E;
        }
        return t;
    }

    public static int alpha(int color, float a) {
        int al = (int) (255 * a);
        return (color & 0x00FFFFFF) | (al << 24);
    }
}
