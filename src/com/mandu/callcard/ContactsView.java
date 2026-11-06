package com.mandu.callcard;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactsView extends LinearLayout {

    private final DialerView.Host host;
    private final Theme t;
    private final Context c;
    private LinearLayout list;
    private TextView empty;
    private EditText search;
    private List<Data.C> all = new ArrayList<>();
    private String query = "";

    public ContactsView(Context c, DialerView.Host host) {
        super(c);
        this.c = c;
        this.host = host;
        this.t = host.theme();
        setOrientation(VERTICAL);
        Ui.pad(this, c, 18, 6, 18, 0);
        build();
    }

    private void build() {
        addView(Ui.title(c, "Contacts", t));

        LinearLayout box = Ui.row(c);
        box.setBackground(Ui.bg(t.surface, 14, c));
        Ui.pad(box, c, 12, 2, 12, 2);
        LinearLayout.LayoutParams blp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 46));
        blp.topMargin = Ui.dp(c, 10);
        addView(box, blp);

        box.addView(Ui.icon(c, "search", 18, t.dim));
        search = new EditText(c);
        search.setHint("Search name or number");
        search.setHintTextColor(Theme.alpha(t.dim, 0.7f));
        search.setTextColor(t.text);
        search.setTextSize(14);
        search.setSingleLine(true);
        search.setBackground(null);
        search.setPadding(Ui.dp(c, 10), 0, 0, 0);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
        box.addView(search, slp);
        search.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int d) { }
            public void onTextChanged(CharSequence s, int a, int b, int d) { }
            public void afterTextChanged(Editable s) {
                query = s.toString().trim();
                render();
            }
        });

        ScrollView sv = new ScrollView(c);
        sv.setVerticalScrollBarEnabled(false);
        sv.setClipToPadding(false);
        LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        vlp.topMargin = Ui.dp(c, 6);
        addView(sv, vlp);

        list = Ui.col(c);
        list.setPadding(0, 0, 0, Ui.dp(c, 90));
        sv.addView(list);

        empty = Ui.tv(c, "No contacts to show.", 14, t.dim, false);
        empty.setGravity(Gravity.CENTER);
        empty.setVisibility(GONE);
        Ui.pad(empty, c, 20, 60, 20, 20);
        addView(empty);
    }

    public void refresh() {
        Ui.BG.execute(new Runnable() {
            public void run() {
                final List<Data.C> l = Data.contacts(c);
                Ui.MAIN.post(new Runnable() {
                    public void run() {
                        all = l;
                        render();
                    }
                });
            }
        });
    }

    private void render() {
        list.removeAllViews();
        List<Data.C> filtered = new ArrayList<>();
        String q = query.toLowerCase();
        String qd = Data.digits(query);
        for (Data.C cc : all) {
            if (q.isEmpty()) {
                filtered.add(cc);
            } else if (cc.name.toLowerCase().contains(q)
                    || Data.digits(cc.number).contains(qd) && !qd.isEmpty()) {
                filtered.add(cc);
            } else if (cc.t9.startsWith(Data.t9(query)) && !query.isEmpty()) {
                filtered.add(cc);
            }
        }
        if (filtered.isEmpty()) {
            empty.setVisibility(VISIBLE);
            return;
        }
        empty.setVisibility(GONE);

        String letter = "";
        int count = 0;
        for (Data.C cc : filtered) {
            String first = cc.name.isEmpty() ? "#" : cc.name.substring(0, 1).toUpperCase();
            if (!first.equals(letter)) {
                letter = first;
                TextView h = Ui.tv(c, letter, 11.5f, t.accent, true);
                h.setLetterSpacing(0.14f);
                Ui.pad(h, c, 6, 14, 6, 6);
                list.addView(h);
            }
            list.addView(rowFor(cc));
            count++;
            if (count > 800) break;
        }
    }

    private View rowFor(final Data.C cc) {
        LinearLayout row = Ui.row(c);
        Ui.pad(row, c, 4, 9, 4, 9);
        GradientDrawable mask = new GradientDrawable();
        mask.setCornerRadius(Ui.dp(c, 14));
        row.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(Theme.alpha(t.text, 0.08f)), null, mask));
        row.setClickable(true);

        row.addView(Ui.avatar(c, cc.name, cc.number, 42,
                Theme.alpha(Ui.avatarColor(cc.number), 0.85f), 0xFFFFFFFF));

        LinearLayout mid = Ui.col(c);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        mlp.leftMargin = Ui.dp(c, 12);
        TextView nm = Ui.tv(c, cc.name.isEmpty() ? Data.pretty(cc.number) : cc.name, 15, t.text, true);
        nm.setSingleLine(true);
        nm.setEllipsize(TextUtils.TruncateAt.END);
        mid.addView(nm);
        TextView num = Ui.tv(c, Data.pretty(cc.number), 12, t.dim, false);
        num.setSingleLine(true);
        mid.addView(num);
        row.addView(mid, mlp);

        LinearLayout callBtn = Ui.col(c);
        callBtn.setGravity(Gravity.CENTER);
        callBtn.addView(Ui.icon(c, "phone", 18, t.accent));
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(Ui.dp(c, 40), Ui.dp(c, 40));
        callBtn.setLayoutParams(clp);
        GradientDrawable om = new GradientDrawable();
        om.setShape(GradientDrawable.OVAL);
        callBtn.setBackground(new android.graphics.drawable.RippleDrawable(
                android.content.res.ColorStateList.valueOf(Theme.alpha(t.accent, 0.25f)), null, om));
        callBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { host.startCall(cc.number, cc.name); }
        });
        row.addView(callBtn);

        row.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                android.content.Intent i = new android.content.Intent(c, ContactDetailActivity.class);
                i.putExtra("name", cc.name);
                i.putExtra("number", cc.number);
                c.startActivity(i);
            }
        });
        return row;
    }

    public void onShow() {
        if (all.isEmpty()) refresh();
    }
}
