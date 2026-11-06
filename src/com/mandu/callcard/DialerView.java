package com.mandu.callcard;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class DialerView extends LinearLayout {

    public interface Host {
        Theme theme();
        void startCall(String number, String name);
        void addContact(String number);
    }

    private final Host host;
    private final Theme t;
    private final Context c;
    private TextView display;
    private TextView match;
    private TextView simChip;
    private final StringBuilder input = new StringBuilder();
    private List<Data.C> cache;
    private ToneGenerator tone;
    private long lastPress;

    private static final String[][] KEYS = {
            {"1", ""}, {"2", "ABC"}, {"3", "DEF"},
            {"4", "GHI"}, {"5", "JKL"}, {"6", "MNO"},
            {"7", "PQRS"}, {"8", "TUV"}, {"9", "WXYZ"},
            {"*", ""}, {"0", "+"}, {"#", ""}
    };

    public DialerView(Context c, Host host) {
        super(c);
        this.c = c;
        this.host = host;
        this.t = host.theme();
        setOrientation(VERTICAL);
        build();
    }

    private void build() {
        Ui.pad(this, c, 18, 6, 18, 10);

        LinearLayout head = Ui.row(c);
        TextView title = Ui.tv(c, "Keypad", 22, t.text, true);
        head.addView(title, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        simChip = Ui.tv(c, "SIM", 11, t.accent, true);
        simChip.setLetterSpacing(0.06f);
        simChip.setGravity(Gravity.CENTER);
        Ui.pad(simChip, c, 12, 6, 12, 6);
        simChip.setBackground(Ui.strokeBg(Theme.alpha(t.accent, 0.10f), Theme.alpha(t.accent, 0.45f), 40, c));
        simChip.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { chooseSim(); }
        });
        head.addView(simChip);
        addView(head, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        display = Ui.tv(c, "", 34, t.text, true);
        display.setGravity(Gravity.CENTER);
        display.setSingleLine(true);
        display.setEllipsize(TextUtils.TruncateAt.START);
        display.setLetterSpacing(0.02f);
        LinearLayout.LayoutParams dlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 62));
        dlp.topMargin = Ui.dp(c, 12);
        addView(display, dlp);

        match = Ui.tv(c, "", 12.5f, t.dim, false);
        match.setGravity(Gravity.CENTER);
        match.setSingleLine(true);
        LinearLayout.LayoutParams mlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(match, mlp);

        LinearLayout grid = Ui.col(c);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
        glp.topMargin = Ui.dp(c, 8);
        addView(grid, glp);

        for (int r = 0; r < 4; r++) {
            LinearLayout row = Ui.row(c);
            row.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);
            for (int col = 0; col < 3; col++) {
                int idx = r * 3 + col;
                View k = key(KEYS[idx][0], KEYS[idx][1]);
                LinearLayout.LayoutParams klp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
                int m = Ui.dp(c, 5);
                klp.setMargins(m, m, m, m);
                row.addView(k, klp);
            }
            grid.addView(row, rlp);
        }

        LinearLayout actions = Ui.row(c);
        actions.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams alp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Ui.dp(c, 76));
        alp.topMargin = Ui.dp(c, 10);
        addView(actions, alp);

        View addBtn = iconButton("person_add", 52, t.text, t.surface2);
        addBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (input.length() > 0) host.addContact(input.toString());
                else Ui.toast(c, "Type a number first");
            }
        });

        final LinearLayout callBtn = Ui.col(c);
        callBtn.setGravity(Gravity.CENTER);
        GradientDrawable g = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{t.green, Theme.alpha(t.accent, 1f)});
        g.setShape(GradientDrawable.OVAL);
        callBtn.setBackground(new RippleDrawable(ColorStateList.valueOf(0x44FFFFFF), g, null));
        ImageView phone = Ui.icon(c, "phone", 30, 0xFF06120F);
        callBtn.addView(phone);
        callBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { placeCall(); }
        });
        callBtn.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (input.length() > 0) {
                    host.addContact(input.toString());
                    return true;
                }
                return false;
            }
        });

        View back = iconButton("backspace", 52, t.text, 0x00000000);
        back.setOnClickListener(new OnClickListener() {
            public void onClick(View v) { backspace(); }
        });
        back.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                if (input.length() > 0) {
                    input.setLength(0);
                    render();
                    return true;
                }
                return false;
            }
        });

        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0, Ui.dp(c, 1), 1f);
        actions.addView(addBtn);
        actions.addView(new View(c), sp);
        actions.addView(callBtn, new LinearLayout.LayoutParams(Ui.dp(c, 72), Ui.dp(c, 72)));
        actions.addView(new View(c), new LinearLayout.LayoutParams(0, Ui.dp(c, 1), 1f));
        actions.addView(back);

        refreshSimChip();
    }

    private View iconButton(String icon, int size, int color, int bg) {
        LinearLayout l = Ui.col(c);
        l.setGravity(Gravity.CENTER);
        l.setLayoutParams(new LinearLayout.LayoutParams(Ui.dp(c, size), Ui.dp(c, size)));
        ImageView iv = Ui.icon(c, icon, size * 0.46f, color);
        l.addView(iv);
        if (bg == 0x00000000) {
            GradientDrawable mask = new GradientDrawable();
            mask.setShape(GradientDrawable.OVAL);
            l.setBackground(new RippleDrawable(ColorStateList.valueOf(Theme.alpha(t.text, 0.12f)), null, mask));
        } else {
            Ui.pressable(l, Ui.oval(bg), Theme.alpha(t.text, 0.12f));
        }
        return l;
    }

    private View key(final String digit, String letters) {
        LinearLayout k = Ui.col(c);
        k.setGravity(Gravity.CENTER);
        k.setClickable(true);
        k.setFocusable(true);
        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        k.setBackground(new RippleDrawable(ColorStateList.valueOf(Theme.alpha(t.accent, 0.22f)), null, mask));

        TextView d = Ui.tv(c, digit, 25, t.text, true);
        d.setGravity(Gravity.CENTER);
        k.addView(d);
        if (!letters.isEmpty()) {
            TextView l = Ui.tv(c, letters, 8.5f, t.dim, true);
            l.setLetterSpacing(0.18f);
            l.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.topMargin = Ui.dp(c, 1);
            k.addView(l, lp);
        }
        k.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                tap(digit);
            }
        });
        if (digit.equals("0")) {
            k.setOnLongClickListener(new OnLongClickListener() {
                public boolean onLongClick(View v) {
                    if (input.length() == 0 || !input.toString().endsWith("+")) {
                        input.append('+');
                        render();
                        feedback("0");
                    }
                    return true;
                }
            });
        }
        return k;
    }

    private void tap(String digit) {
        input.append(digit);
        render();
        feedback(digit);
    }

    private void feedback(String digit) {
        if (App.p(c).getBoolean("vibrate", true)) {
            try {
                Vibrator v = (Vibrator) c.getSystemService(Context.VIBRATOR_SERVICE);
                if (v != null) {
                    if (Build.VERSION.SDK_INT >= 26) {
                        v.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE));
                    } else {
                        v.vibrate(18);
                    }
                }
            } catch (Exception ignored) {
            }
        }
        if (App.p(c).getBoolean("tones", true)) {
            try {
                if (tone == null) tone = new ToneGenerator(AudioManager.STREAM_MUSIC, 45);
                tone.startTone(ToneGenerator.TONE_DTMF_0 + Math.max(0, "0123456789*#".indexOf(digit)), 90);
            } catch (Exception ignored) {
            }
        }
    }

    private void backspace() {
        if (input.length() > 0) {
            input.deleteCharAt(input.length() - 1);
            render();
        }
    }

    public void setText(String number) {
        input.setLength(0);
        if (number != null) input.append(number);
        render();
    }

    public String text() {
        return input.toString();
    }

    private void render() {
        display.setText(input.length() == 0 ? "" : input.toString());
        if (input.length() == 0) {
            display.setHint("Enter a number");
            display.setHintTextColor(Theme.alpha(t.dim, 0.65f));
            display.setTextSize(26);
        } else {
            display.setTextSize(34);
        }
        match.setText(lookup(input.toString()));
        match.setVisibility(match.getText().length() == 0 ? View.INVISIBLE : View.VISIBLE);
    }

    private String lookup(String q) {
        if (q == null || q.isEmpty() || !App.p(c).getBoolean("t9", true)) return "";
        String d = Data.digits(q);
        if (d.length() >= 3 && cache != null) {
            for (Data.C cc : cache) {
                if (cc.t9.startsWith(d) && d.length() >= 3) return cc.name;
            }
        }
        String n = Data.nameOf(c, q);
        return n == null ? "" : n;
    }

    public void loadContacts() {
        Ui.BG.execute(new Runnable() {
            public void run() {
                final List<Data.C> l = Data.contacts(c);
                Ui.MAIN.post(new Runnable() {
                    public void run() {
                        cache = l;
                        render();
                    }
                });
            }
        });
    }

    private void placeCall() {
        if (input.length() == 0) {
            Ui.toast(c, "Enter a number to call");
            return;
        }
        String num = input.toString();
        host.startCall(num, match.getText().toString());
    }

    public void refreshSimChip() {
        if (simChip == null) return;
        if (Sims.handles(c).size() >= 2) {
            simChip.setVisibility(View.VISIBLE);
            simChip.setText(Sims.savedLabel(c).toUpperCase());
        } else {
            simChip.setVisibility(View.GONE);
        }
    }

    private void chooseSim() {
        final List<android.telecom.PhoneAccountHandle> hs = Sims.handles(c);
        if (hs.size() < 2) {
            Ui.toast(c, "Only one SIM available");
            return;
        }
        String[] items = new String[hs.size() + 1];
        items[0] = "Ask every call";
        for (int i = 0; i < hs.size(); i++) items[i + 1] = Sims.label(c, hs.get(i));
        new android.app.AlertDialog.Builder(c)
                .setTitle("Call using")
                .setItems(items, new android.content.DialogInterface.OnClickListener() {
                    public void onClick(android.content.DialogInterface d, int which) {
                        Sims.saveId(c, which == 0 ? null : hs.get(which - 1).getId());
                        refreshSimChip();
                    }
                }).show();
    }

    public void onShow() {
        refreshSimChip();
    }
}
