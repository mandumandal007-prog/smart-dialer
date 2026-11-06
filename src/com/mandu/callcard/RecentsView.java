package com.mandu.callcard;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.CallLog;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecentsView extends LinearLayout {

    private final DialerView.Host host;
    private final Theme t;
    private final Context c;
    private LinearLayout list;
    private TextView empty;
    private List<Data.Log> data = new ArrayList<>();

    public RecentsView(Context c, DialerView.Host host) {
        super(c);
        this.c = c;
        this.host = host;
        this.t = host.theme();
        setOrientation(VERTICAL);
        Ui.pad(this, c, 18, 6, 18, 0);
        build();
    }

    private void build() {
        LinearLayout head = Ui.row(c);
        head.addView(Ui.title(c, "Recents", t), new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        View clear = roundIcon("delete");
        clear.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { confirmClear(); }
        });
        head.addView(clear);
        addView(head);

        ScrollView sv = new ScrollView(c);
        sv.setVerticalScrollBarEnabled(false);
        sv.setClipToPadding(false);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        slp.topMargin = Ui.dp(c, 8);
        addView(sv, slp);

        list = Ui.col(c);
        list.setPadding(0, 0, 0, Ui.dp(c, 90));
        sv.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        empty = Ui.tv(c, "No calls yet.\nYour call history will appear here.", 14, t.dim, false);
        empty.setGravity(Gravity.CENTER);
        empty.setVisibility(GONE);
        Ui.pad(empty, c, 20, 60, 20, 20);
        addView(empty);
    }

    private View roundIcon(String icon) {
        LinearLayout l = Ui.col(c);
        l.setGravity(Gravity.CENTER);
        l.addView(Ui.icon(c, icon, 20, t.dim));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(c, 40), Ui.dp(c, 40));
        l.setLayoutParams(lp);
        android.graphics.drawable.GradientDrawable mask = new android.graphics.drawable.GradientDrawable();
        mask.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        l.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(Theme.alpha(t.text, 0.12f)), null, mask));
        l.setClickable(true);
        return l;
    }

    public void refresh() {
        Ui.BG.execute(new Runnable() {
            public void run() {
                final List<Data.Log> l = Data.logs(c, 250);
                Ui.MAIN.post(new Runnable() {
                    public void run() {
                        data = l;
                        render();
                    }
                });
            }
        });
    }

    private void render() {
        list.removeAllViews();
        if (data.isEmpty()) {
            empty.setVisibility(VISIBLE);
            return;
        }
        empty.setVisibility(GONE);
        for (int i = 0; i < data.size(); i++) {
            list.addView(rowFor(data.get(i)));
            if (i < data.size() - 1) {
                View d = Ui.divider(c, t);
                LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, Math.max(1, Ui.dp(c, 0.6f)));
                dlp.leftMargin = Ui.dp(c, 58);
                list.addView(d, dlp);
            }
        }
    }

    private View rowFor(final Data.Log l) {
        LinearLayout row = Ui.row(c);
        Ui.pad(row, c, 4, 10, 4, 10);
        android.graphics.drawable.GradientDrawable mask = new android.graphics.drawable.GradientDrawable();
        mask.setCornerRadius(Ui.dp(c, 14));
        row.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(Theme.alpha(t.text, 0.08f)), null, mask));
        row.setClickable(true);

        String name = l.name == null || l.name.isEmpty() ? Data.pretty(l.number) : l.name;
        boolean known = l.name != null && !l.name.isEmpty();
        row.addView(Ui.avatar(c, known ? l.name : "", l.number, 42,
                Theme.alpha(Ui.avatarColor(l.number), 0.85f), 0xFFFFFFFF));

        LinearLayout mid = Ui.col(c);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        mlp.leftMargin = Ui.dp(c, 12);
        TextView nm = Ui.tv(c, name, 15, l.type == CallLog.Calls.MISSED_TYPE ? t.danger : t.text, true);
        nm.setSingleLine(true);
        nm.setEllipsize(TextUtils.TruncateAt.END);
        mid.addView(nm);
        LinearLayout sub = Ui.row(c);
        ImageView dir = Ui.icon(c, typeIcon(l.type), 13, typeColor(l.type));
        sub.addView(dir);
        TextView meta = Ui.tv(c, Data.pretty(l.number), 12, t.dim, false);
        meta.setSingleLine(true);
        meta.setEllipsize(TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams mlp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mlp2.leftMargin = Ui.dp(c, 4);
        sub.addView(meta, mlp2);
        mid.addView(sub);
        row.addView(mid, mlp);

        LinearLayout right = Ui.col(c);
        right.setGravity(Gravity.END);
        TextView time = Ui.tv(c, Data.when(l.date), 11.5f, t.dim, false);
        right.addView(time);
        if (l.dur > 0) {
            TextView dur = Ui.tv(c, Data.duration(l.dur), 11.5f, Theme.alpha(t.dim, 0.8f), false);
            dur.setGravity(Gravity.END);
            right.addView(dur);
        }
        row.addView(right);

        row.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { host.startCall(l.number, l.name); }
        });
        row.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                options(l);
                return true;
            }
        });
        return row;
    }

    private String typeIcon(int type) {
        if (type == CallLog.Calls.INCOMING_TYPE) return "call_received";
        if (type == CallLog.Calls.OUTGOING_TYPE) return "call_made";
        if (type == CallLog.Calls.MISSED_TYPE) return "call_missed";
        if (type == CallLog.Calls.REJECTED_TYPE) return "block";
        return "call_made";
    }

    private int typeColor(int type) {
        if (type == CallLog.Calls.MISSED_TYPE || type == CallLog.Calls.REJECTED_TYPE) return t.danger;
        if (type == CallLog.Calls.INCOMING_TYPE) return t.green;
        return t.accent;
    }

    private void options(final Data.Log l) {
        String[] items = {"Call", "Send SMS", "Copy number", "Save to contacts", "Block number", "Delete from log"};
        new AlertDialog.Builder(c)
                .setTitle(TextUtils.isEmpty(l.name) ? Data.pretty(l.number) : l.name)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int which) {
                        switch (which) {
                            case 0:
                                host.startCall(l.number, l.name);
                                break;
                            case 1:
                                sms(l.number);
                                break;
                            case 2:
                                ClipboardManager cm = (ClipboardManager) c.getSystemService(Context.CLIPBOARD_SERVICE);
                                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("number", l.number));
                                Ui.toast(c, "Number copied");
                                break;
                            case 3:
                                host.addContact(l.number);
                                break;
                            case 4:
                                Data.block(c, l.number);
                                Ui.toast(c, "Blocked " + Data.pretty(l.number));
                                break;
                            case 5:
                                if (Data.deleteLog(c, l.number, l.date)) {
                                    Ui.toast(c, "Deleted");
                                    refresh();
                                } else {
                                    Ui.toast(c, "Grant call log permission first");
                                }
                                break;
                        }
                    }
                }).show();
    }

    private void sms(String number) {
        try {
            Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number));
            c.startActivity(i);
        } catch (Exception e) {
            Ui.toast(c, "No messaging app found");
        }
    }

    private void confirmClear() {
        new AlertDialog.Builder(c)
                .setTitle("Clear call history?")
                .setMessage("This removes every entry from the device call log.")
                .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        if (Data.clearLog(c)) {
                            Ui.toast(c, "Call log cleared");
                            refresh();
                        } else {
                            Ui.toast(c, "Grant call log permission first");
                        }
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void onShow() {
        refresh();
    }
}
