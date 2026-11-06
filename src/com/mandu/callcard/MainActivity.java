package com.mandu.callcard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity implements DialerView.Host {

    public static final int REQ_PERMS = 1001;
    public static final int REQ_CALL = 1002;

    private Theme t;
    private FrameLayout host;
    private LinearLayout nav;
    private View[] tabs = new View[4];
    private View[] contents = new View[4];
    private DialerView dialer;
    private RecentsView recents;
    private ContactsView contacts;
    private SettingsView settings;
    private int current = -1;
    private String pendingNumber;

    private static final String[] ICONS = {"dialpad", "history", "person", "settings"};
    private static final String[] LABELS = {"Keypad", "Recents", "Contacts", "Settings"};

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        t = Theme.get(this);
        getWindow().setStatusBarColor(t.bg);
        getWindow().setNavigationBarColor(t.navBg);

        LinearLayout root = Ui.col(this);
        root.setBackgroundColor(t.bg);

        host = new FrameLayout(this);
        root.addView(host, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        nav = Ui.row(this);
        nav.setBackgroundColor(t.navBg);
        nav.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 8));
        root.addView(nav, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        for (int i = 0; i < 4; i++) {
            final int idx = i;
            LinearLayout item = Ui.col(this);
            item.setGravity(Gravity.CENTER);
            item.setPadding(0, Ui.dp(this, 6), 0, Ui.dp(this, 4));
            ImageView iv = Ui.icon(this, ICONS[i], 21, t.dim);
            item.addView(iv);
            TextView tv = Ui.tv(this, LABELS[i], 10.5f, t.dim, true);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Ui.dp(this, 4);
            item.addView(tv, lp);
            GradientDrawable mask = new GradientDrawable();
            mask.setCornerRadius(Ui.dp(this, 16));
            item.setBackground(new android.graphics.drawable.RippleDrawable(
                    android.content.res.ColorStateList.valueOf(Theme.alpha(t.text, 0.10f)), null, mask));
            item.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) { show(idx); }
            });
            nav.addView(item, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            tabs[i] = item;
        }

        setContentView(root);
        show(0);

        if (Perms.missing(this).size() > 0 && !App.p(this).getBoolean("asked", false)) {
            App.p(this).edit().putBoolean("asked", true).apply();
            host.postDelayed(new Runnable() {
                public void run() { Perms.ask(MainActivity.this, REQ_PERMS); }
            }, 600);
        }

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent == null) return;
        Uri data = intent.getData();
        if (data != null && "tel".equals(data.getScheme())) {
            String num = data.getSchemeSpecificPart();
            if (num != null && dialer != null) {
                show(0);
                dialer.setText(num);
            }
        }
    }

    private void show(int idx) {
        if (idx == current) return;
        host.removeAllViews();
        if (contents[idx] == null) {
            contents[idx] = create(idx);
        }
        host.addView(contents[idx], new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        for (int i = 0; i < 4; i++) {
            LinearLayout item = (LinearLayout) tabs[i];
            ImageView iv = (ImageView) item.getChildAt(0);
            TextView tv = (TextView) item.getChildAt(1);
            boolean on = i == idx;
            iv.setColorFilter(on ? t.accent : t.dim);
            tv.setTextColor(on ? t.accent : t.dim);
        }
        current = idx;
        if (idx == 1 && recents != null) recents.onShow();
        if (idx == 2 && contacts != null) contacts.onShow();
        if (idx == 3 && settings != null) settings.onShow();
        if (idx == 0 && dialer != null) dialer.onShow();
    }

    private View create(int idx) {
        if (idx == 0) {
            dialer = new DialerView(this, this);
            dialer.loadContacts();
            return dialer;
        }
        if (idx == 1) {
            recents = new RecentsView(this, this);
            recents.refresh();
            return recents;
        }
        if (idx == 2) {
            contacts = new ContactsView(this, this);
            contacts.refresh();
            return contacts;
        }
        settings = new SettingsView(this, this);
        return settings;
    }

    /* ---------- host ---------- */

    public Theme theme() {
        return t;
    }

    public void startCall(String number, String name) {
        if (number == null || number.trim().isEmpty()) return;
        String n = number.trim();
        if (Data.isBlocked(this, n)) {
            final String blockedNum = n;
            final String blockedName = name;
            new android.app.AlertDialog.Builder(this)
                    .setTitle("Blocked number")
                    .setMessage(Data.pretty(n) + " is on your block list.")
                    .setPositiveButton("Unblock and call", new android.content.DialogInterface.OnClickListener() {
                        public void onClick(android.content.DialogInterface d, int w) {
                            Data.unblock(MainActivity.this, blockedNum);
                            startCall(blockedNum, blockedName);
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }
        if (App.p(this).getBoolean("confirm", true)) {
            Intent i = new Intent(this, CallActivity.class);
            i.putExtra("number", n);
            i.putExtra("name", name == null ? "" : name);
            startActivity(i);
        } else {
            dialDirect(this, n);
        }
    }

    public void addContact(String number) {
        try {
            Intent i = new Intent(Intent.ACTION_INSERT_OR_EDIT);
            i.setType(ContactsContract.Contacts.CONTENT_ITEM_TYPE);
            i.putExtra(ContactsContract.Intents.Insert.PHONE, number);
            startActivity(i);
        } catch (Exception e) {
            Ui.toast(this, "No contacts app found");
        }
    }

    public void recreateForTheme() {
        recreate();
    }

    public static void dialDirect(Activity a, String number) {
        if (!Perms.has(a, Manifest.permission.CALL_PHONE)) {
            Perms.askOne(a, Manifest.permission.CALL_PHONE, REQ_CALL);
            return;
        }
        try {
            Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
            PhoneAccountHandle h = Sims.find(a, Sims.savedId(a));
            if (h != null) i.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, h);
            a.startActivity(i);
        } catch (Exception e) {
            Ui.toast(a, "Could not place the call");
        }
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == REQ_CALL && res.length > 0 && res[0] == 0 && pendingNumber != null) {
            dialDirect(this, pendingNumber);
            pendingNumber = null;
        }
        if (code == REQ_PERMS) {
            if (dialer != null) dialer.loadContacts();
            if (recents != null) recents.refresh();
            if (contacts != null) contacts.refresh();
            if (settings != null) settings.onShow();
        }
    }

    @Override
    public void onBackPressed() {
        if (current != 0) {
            show(0);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (current == 1 && recents != null) recents.refresh();
        if (current == 3 && settings != null) settings.onShow();
    }
}
