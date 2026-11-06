package com.mandu.callcard;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SettingsView extends LinearLayout {

    private final DialerView.Host host;
    private final Theme t;
    private final Context c;
    private LinearLayout permsCard;
    private TextView simValue;

    public SettingsView(Context c, DialerView.Host host) {
        super(c);
        this.c = c;
        this.host = host;
        this.t = host.theme();
        setOrientation(VERTICAL);
        Ui.pad(this, c, 18, 6, 18, 0);
        build();
    }

    private void build() {
        addView(Ui.title(c, "Settings", t));

        ScrollView sv = new ScrollView(c);
        sv.setVerticalScrollBarEnabled(false);
        sv.setClipToPadding(false);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        slp.topMargin = Ui.dp(c, 8);
        addView(sv, slp);

        LinearLayout body = Ui.col(c);
        body.setPadding(0, 0, 0, Ui.dp(c, 100));
        sv.addView(body);

        /* Appearance */
        body.addView(section("Appearance"));
        LinearLayout app = Ui.card(c, t);
        body.addView(app, cardLp(app));
        LinearLayout chips = Ui.row(c);
        chips.setGravity(Gravity.CENTER);
        Ui.pad(chips, c, 0, 4, 0, 2);
        app.addView(chips, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        chips.addView(themeChip("Dark", Theme.DARK));
        chips.addView(themeChip("Light", Theme.LIGHT));
        chips.addView(themeChip("AMOLED", Theme.AMOLED));

        /* Dial pad */
        body.addView(section("Dial pad"));
        LinearLayout dial = Ui.card(c, t);
        body.addView(dial, cardLp(dial));
        dial.addView(switchRow("Touch tones", "DTMF beep on every key", "tones", true));
        dial.addView(Ui.divider(c, t));
        dial.addView(switchRow("Vibrate on key", "Haptic feedback while typing", "vibrate", true));
        dial.addView(Ui.divider(c, t));
        dial.addView(switchRow("Smart search (T9)", "Match names while you type digits", "t9", true));

        /* Calling */
        body.addView(section("Calling"));
        LinearLayout call = Ui.card(c, t);
        body.addView(call, cardLp(call));
        call.addView(switchRow("Show calling card", "Preview the card before the call connects", "confirm", true));
        call.addView(Ui.divider(c, t));
        LinearLayout simRow = Ui.row(c);
        Ui.pad(simRow, c, 0, 14, 0, 14);
        simRow.setClickable(true);
        simRow.addView(Ui.tv(c, "Preferred SIM", 14.5f, t.text, false),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        simValue = Ui.tv(c, Sims.savedLabel(c), 13, t.accent, true);
        simRow.addView(simValue);
        simRow.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { chooseSim(); }
        });
        call.addView(simRow);

        /* Privacy */
        body.addView(section("Privacy"));
        LinearLayout priv = Ui.card(c, t);
        body.addView(priv, cardLp(priv));
        priv.addView(linkRow("Blocked numbers", blockedCount(), new OnClickListener() {
            public void onClick(View v) { manageBlocked(); }
        }));
        priv.addView(Ui.divider(c, t));
        priv.addView(linkRow("Clear call history", "Delete every entry in the device log", new OnClickListener() {
            public void onClick(View v) { confirmClear(); }
        }));

        /* Permissions */
        body.addView(section("Permissions"));
        permsCard = Ui.card(c, t);
        body.addView(permsCard, cardLp(permsCard));
        refreshPerms();

        /* About */
        body.addView(section("About"));
        LinearLayout about = Ui.card(c, t);
        body.addView(about, cardLp(about));
        LinearLayout brand = Ui.row(c);
        Ui.pad(brand, c, 0, 4, 0, 6);
        brand.addView(Ui.avatar(c, "CallCard", "", 44, Theme.alpha(t.accent, 0.18f), t.accent));
        LinearLayout bcol = Ui.col(c);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        blp.leftMargin = Ui.dp(c, 12);
        bcol.addView(Ui.tv(c, "CallCard Dialer", 15, t.text, true));
        bcol.addView(Ui.tv(c, "Version 1.0  •  build 1", 12, t.dim, false));
        brand.addView(bcol, blp);
        about.addView(brand);
        about.addView(Ui.divider(c, t));
        about.addView(Ui.tv(c, "A lightweight dialer with a calling-card screen, dual SIM choice, T9 search, call log tools and a local block list. No data leaves your phone.",
                12.5f, t.dim, false));
    }

    private LinearLayout.LayoutParams cardLp(View v) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Ui.dp(c, 4);
        return lp;
    }

    private View section(String s) {
        TextView v = Ui.label(c, s, t);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(c, 18);
        lp.bottomMargin = Ui.dp(c, 8);
        lp.leftMargin = Ui.dp(c, 4);
        v.setLayoutParams(lp);
        return v;
    }

    private View themeChip(String label, final int mode) {
        final boolean active = App.p(c).getInt("theme", Theme.DARK) == mode;
        TextView v = Ui.tv(c, label, 13, active ? t.bg : t.dim, active);
        v.setGravity(Gravity.CENTER);
        Ui.pad(v, c, 16, 10, 16, 10);
        v.setBackground(active
                ? Ui.bg(t.accent, 30, c)
                : Ui.strokeBg(0x00000000, t.divider, 30, c));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        lp.setMargins(Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 4), Ui.dp(c, 4));
        v.setLayoutParams(lp);
        v.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                App.p(c).edit().putInt("theme", mode).apply();
                if (c instanceof MainActivity) ((MainActivity) c).recreateForTheme();
            }
        });
        return v;
    }

    private View switchRow(String title, String sub, final String key, boolean def) {
        LinearLayout row = Ui.row(c);
        Ui.pad(row, c, 0, 12, 0, 12);
        LinearLayout col = Ui.col(c);
        col.addView(Ui.tv(c, title, 14.5f, t.text, false));
        if (sub != null) col.addView(Ui.tv(c, sub, 11.5f, t.dim, false));
        row.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final Switch sw = new Switch(c);
        sw.setChecked(App.p(c).getBoolean(key, def));
        sw.setButtonTintList(android.content.res.ColorStateList.valueOf(t.accent));
        sw.setThumbTintList(android.content.res.ColorStateList.valueOf(
                sw.isChecked() ? t.accent : t.dim));
        sw.setTrackTintList(android.content.res.ColorStateList.valueOf(
                Theme.alpha(t.accent, sw.isChecked() ? 0.45f : 0.22f)));
        sw.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(android.widget.CompoundButton b, boolean checked) {
                App.p(c).edit().putBoolean(key, checked).apply();
                sw.setThumbTintList(android.content.res.ColorStateList.valueOf(checked ? t.accent : t.dim));
                sw.setTrackTintList(android.content.res.ColorStateList.valueOf(
                        Theme.alpha(t.accent, checked ? 0.45f : 0.22f)));
            }
        });
        row.addView(sw);
        return row;
    }

    private View linkRow(String title, String sub, OnClickListener l) {
        LinearLayout row = Ui.row(c);
        Ui.pad(row, c, 0, 14, 0, 14);
        row.setClickable(true);
        LinearLayout col = Ui.col(c);
        col.addView(Ui.tv(c, title, 14.5f, t.text, false));
        if (sub != null) col.addView(Ui.tv(c, sub, 11.5f, t.dim, false));
        row.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(Ui.icon(c, "arrow_back", 16, t.dim));
        row.setOnClickListener(l);
        return row;
    }

    private String blockedCount() {
        Set<String> s = Data.blocked(c);
        return s.isEmpty() ? "No numbers blocked" : s.size() + " number" + (s.size() > 1 ? "s" : "") + " blocked";
    }

    private void refreshPerms() {
        if (permsCard == null) return;
        permsCard.removeAllViews();
        List<Perms.P> all = Perms.list();
        int granted = 0;
        for (Perms.P p : all) if (Perms.has(c, p.key)) granted++;

        LinearLayout head = Ui.row(c);
        Ui.pad(head, c, 0, 2, 0, 10);
        head.addView(Ui.tv(c, granted + " of " + all.size() + " granted", 13, granted == all.size() ? t.green : t.dim, true),
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView allBtn = Ui.tv(c, "GRANT ALL", 11.5f, t.accent, true);
        allBtn.setLetterSpacing(0.08f);
        allBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (c instanceof Activity) Perms.ask((Activity) c, MainActivity.REQ_PERMS);
            }
        });
        head.addView(allBtn);
        permsCard.addView(head);

        for (int i = 0; i < all.size(); i++) {
            final Perms.P p = all.get(i);
            boolean ok = Perms.has(c, p.key);
            LinearLayout row = Ui.row(c);
            Ui.pad(row, c, 0, 10, 0, 10);
            LinearLayout col = Ui.col(c);
            col.addView(Ui.tv(c, p.title, 13.5f, t.text, false));
            col.addView(Ui.tv(c, p.why, 11, t.dim, false));
            row.addView(col, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            if (ok) {
                row.addView(Ui.icon(c, "check", 18, t.green));
            } else {
                TextView grant = Ui.tv(c, "Allow", 12, t.accent, true);
                Ui.pad(grant, c, 12, 6, 12, 6);
                grant.setBackground(Ui.strokeBg(0, Theme.alpha(t.accent, 0.5f), 20, c));
                grant.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (c instanceof Activity) Perms.askOne((Activity) c, p.key, MainActivity.REQ_PERMS);
                    }
                });
                row.addView(grant);
            }
            permsCard.addView(row);
            if (i < all.size() - 1) permsCard.addView(Ui.divider(c, t));
        }
    }

    private void chooseSim() {
        final List<android.telecom.PhoneAccountHandle> hs = Sims.handles(c);
        if (hs.size() < 2) {
            Ui.toast(c, "Only one SIM found on this device");
            return;
        }
        String[] items = new String[hs.size() + 1];
        items[0] = "Ask every call";
        for (int i = 0; i < hs.size(); i++) items[i + 1] = Sims.label(c, hs.get(i));
        new AlertDialog.Builder(c).setTitle("Preferred SIM").setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int which) {
                Sims.saveId(c, which == 0 ? null : hs.get(which - 1).getId());
                simValue.setText(Sims.savedLabel(c));
            }
        }).show();
    }

    private void manageBlocked() {
        final List<String> l = new ArrayList<>(Data.blocked(c));
        String[] items;
        if (l.isEmpty()) {
            items = new String[]{"Add a number"};
        } else {
            items = new String[l.size() + 1];
            for (int i = 0; i < l.size(); i++) items[i] = "Unblock " + Data.pretty(l.get(i));
            items[l.size()] = "Add a number";
        }
        new AlertDialog.Builder(c).setTitle("Blocked numbers").setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface d, int which) {
                if (which < l.size()) {
                    Data.unblock(c, l.get(which));
                    Ui.toast(c, "Unblocked");
                } else {
                    final android.widget.EditText e = new android.widget.EditText(c);
                    e.setHint("Phone number");
                    new AlertDialog.Builder(c).setTitle("Block a number").setView(e)
                            .setPositiveButton("Block", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dd, int w) {
                                    String n = e.getText().toString().trim();
                                    if (!n.isEmpty()) {
                                        Data.block(c, n);
                                        Ui.toast(c, "Blocked " + Data.pretty(n));
                                    }
                                }
                            }).setNegativeButton("Cancel", null).show();
                }
            }
        }).show();
    }

    private void confirmClear() {
        new AlertDialog.Builder(c)
                .setTitle("Clear call history?")
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (Data.clearLog(c)) Ui.toast(c, "Call log cleared");
                        else Ui.toast(c, "Grant call log permission first");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void onShow() {
        refreshPerms();
        if (simValue != null) simValue.setText(Sims.savedLabel(c));
    }
}
