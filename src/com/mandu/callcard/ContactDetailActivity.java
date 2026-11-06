package com.mandu.callcard;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class ContactDetailActivity extends Activity {

    private Theme t;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        t = Theme.get(this);
        getWindow().setStatusBarColor(t.bg);
        getWindow().setNavigationBarColor(t.bg);

        final String name = getIntent().getStringExtra("name");
        final String number = getIntent().getStringExtra("number");

        LinearLayout root = Ui.col(this);
        root.setBackgroundColor(t.bg);
        root.setPadding(0, Ui.statusBarHeight(this), 0, 0);

        LinearLayout top = Ui.row(this);
        Ui.pad(top, this, 12, 8, 12, 8);
        View back = Ui.icon(this, "arrow_back", 24, t.text);
        back.setPadding(Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10));
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { finish(); }
        });
        top.addView(back, new LinearLayout.LayoutParams(Ui.dp(this, 44), Ui.dp(this, 44)));
        root.addView(top);

        ScrollView sv = new ScrollView(this);
        LinearLayout body = Ui.col(this);
        Ui.pad(body, this, 22, 10, 22, 24);
        sv.addView(body);
        root.addView(sv, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        TextView av = Ui.avatar(this, name == null ? "" : name, number, 96,
                Theme.alpha(Ui.avatarColor(number), 0.9f), 0xFFFFFFFF);
        LinearLayout.LayoutParams avp = new LinearLayout.LayoutParams(Ui.dp(this, 96), Ui.dp(this, 96));
        avp.gravity = Gravity.CENTER_HORIZONTAL;
        avp.topMargin = Ui.dp(this, 14);
        body.addView(av, avp);

        TextView nm = Ui.tv(this, name == null || name.isEmpty() ? Data.pretty(number) : name, 24, t.text, true);
        nm.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.topMargin = Ui.dp(this, 14);
        body.addView(nm, nlp);

        TextView num = Ui.tv(this, Data.pretty(number), 15, t.dim, false);
        num.setGravity(Gravity.CENTER);
        body.addView(num);

        LinearLayout actions = Ui.row(this);
        actions.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        alp.topMargin = Ui.dp(this, 22);
        body.addView(actions, alp);

        actions.addView(action("Call", "phone", t.green, new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(ContactDetailActivity.this, CallActivity.class);
                i.putExtra("number", number);
                i.putExtra("name", name);
                startActivity(i);
            }
        }));
        actions.addView(action("Message", "message", t.accent, new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number)));
                } catch (Exception e) {
                    Ui.toast(ContactDetailActivity.this, "No messaging app found");
                }
            }
        }));
        actions.addView(action("Edit", "person", t.accent2, new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
                    i.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
                    i.putExtra(ContactsContract.Intents.Insert.PHONE, number);
                    if (name != null) i.putExtra(ContactsContract.Intents.Insert.NAME, name);
                    startActivity(i);
                } catch (Exception e) {
                    Ui.toast(ContactDetailActivity.this, "No contacts app found");
                }
            }
        }));

        LinearLayout card = Ui.card(this, t);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = Ui.dp(this, 26);
        body.addView(card, clp);

        card.addView(optionRow("Copy number", new View.OnClickListener() {
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (cm != null) cm.setPrimaryClip(ClipData.newPlainText("number", number));
                Ui.toast(ContactDetailActivity.this, "Number copied");
            }
        }));
        card.addView(Ui.divider(this, t));
        card.addView(optionRow("Share number", new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, (name == null ? "" : name + " - ") + number);
                startActivity(Intent.createChooser(i, "Share"));
            }
        }));
        card.addView(Ui.divider(this, t));
        card.addView(optionRow("Block number", new View.OnClickListener() {
            public void onClick(View v) {
                Data.block(ContactDetailActivity.this, number);
                Ui.toast(ContactDetailActivity.this, "Blocked " + Data.pretty(number));
            }
        }));
        card.addView(Ui.divider(this, t));
        card.addView(optionRow("Call history", new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("content://call_log/calls")));
                } catch (Exception e) {
                    Ui.toast(ContactDetailActivity.this, "Not available");
                }
            }
        }));

        setContentView(root);
    }

    private View action(String label, String icon, int color, View.OnClickListener l) {
        LinearLayout v = Ui.col(this);
        v.setGravity(Gravity.CENTER);
        v.setClickable(true);
        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        v.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(Theme.alpha(color, 0.25f)), null, mask));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(Ui.dp(this, 74), Ui.dp(this, 74));
        lp.setMargins(Ui.dp(this, 6), 0, Ui.dp(this, 6), 0);
        v.setLayoutParams(lp);
        ImageView iv = Ui.icon(this, icon, 24, color);
        v.addView(iv);
        TextView tv = Ui.tv(this, label, 10.5f, t.dim, true);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.topMargin = Ui.dp(this, 6);
        v.addView(tv, tlp);
        v.setOnClickListener(l);
        return v;
    }

    private View optionRow(String label, View.OnClickListener l) {
        LinearLayout row = Ui.row(this);
        Ui.pad(row, this, 0, 14, 0, 14);
        row.setClickable(true);
        TextView tv = Ui.tv(this, label, 14.5f, t.text, false);
        row.addView(tv, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.addView(Ui.icon(this, "arrow_back", 16, t.dim));
        row.setOnClickListener(l);
        return row;
    }
}
