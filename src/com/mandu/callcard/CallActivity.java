package com.mandu.callcard;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.animation.ValueAnimator;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CallActivity extends Activity {

    private Theme t;
    private String number;
    private String name;

    private TextView statusView;
    private TextView timerView;
    private LinearLayout callBtn;
    private ImageView callIcon;
    private TextView callLabel;

    private boolean inCall = false;
    private boolean ended = false;
    private boolean muted = false;
    private boolean speaker = false;
    private long startAt = 0;

    private TelephonyManager tm;
    private PhoneStateListener listener;
    private final Handler h = new Handler(Looper.getMainLooper());

    private boolean muteBtnOn = false;
    private boolean spkBtnOn = false;
    private LinearLayout muteBtn, spkBtn;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        t = Theme.get(this);
        number = getIntent().getStringExtra("number");
        name = getIntent().getStringExtra("name");
        if (number == null) number = "";
        if (name == null) name = "";

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setStatusBarColor(0x00000000);
        getWindow().setNavigationBarColor(0x00000000);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);

        buildUi();
        tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);

        if (Perms.has(this, Manifest.permission.CALL_PHONE)) {
            h.postDelayed(new Runnable() {
                public void run() { beginCall(); }
            }, 450);
        }
    }

    private void buildUi() {
        LinearLayout root = Ui.col(this);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        GradientDrawable bg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xFF111C33, 0xFF0A1020, 0xFF05070C});
        root.setBackground(bg);
        root.setPadding(0, Ui.statusBarHeight(this) + Ui.dp(this, 8), 0, Ui.dp(this, 18));

        LinearLayout top = Ui.row(this);
        top.setPadding(Ui.dp(this, 14), 0, Ui.dp(this, 14), 0);
        View close = Ui.icon(this, "close", 22, 0xCCFFFFFF);
        close.setPadding(Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10), Ui.dp(this, 10));
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (inCall) {
                    moveTaskToBack(true);
                } else {
                    finish();
                }
            }
        });
        top.addView(close, new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42)));

        TextView tag = Ui.tv(this, "CALLING CARD", 10.5f, 0x99FFFFFF, true);
        tag.setLetterSpacing(0.22f);
        tag.setGravity(Gravity.CENTER);
        top.addView(tag, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        top.addView(new View(this), new LinearLayout.LayoutParams(Ui.dp(this, 42), Ui.dp(this, 42)));
        root.addView(top, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        root.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 0.35f));

        FrameLayout avBox = new FrameLayout(this);
        LinearLayout.LayoutParams abp = new LinearLayout.LayoutParams(Ui.dp(this, 230), Ui.dp(this, 230));
        root.addView(avBox, abp);
        PulseView pulse = new PulseView(this);
        avBox.addView(pulse, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView av = Ui.avatar(this, name, number, 116, Theme.alpha(Ui.avatarColor(number), 0.92f), 0xFFFFFFFF);
        av.setTextSize(40);
        FrameLayout.LayoutParams avlp = new FrameLayout.LayoutParams(Ui.dp(this, 116), Ui.dp(this, 116));
        avlp.gravity = Gravity.CENTER;
        avBox.addView(av, avlp);

        TextView nm = Ui.tv(this, name.isEmpty() ? Data.pretty(number) : name, 26, 0xFFF7FAFF, true);
        nm.setGravity(Gravity.CENTER);
        nm.setSingleLine(true);
        nm.setEllipsize(android.text.TextUtils.TruncateAt.END);
        LinearLayout.LayoutParams nlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        nlp.leftMargin = Ui.dp(this, 30);
        nlp.rightMargin = Ui.dp(this, 30);
        nlp.topMargin = Ui.dp(this, 6);
        root.addView(nm, nlp);

        if (!name.isEmpty()) {
            TextView num = Ui.tv(this, Data.pretty(number), 14, 0x99FFFFFF, false);
            num.setGravity(Gravity.CENTER);
            root.addView(num);
        }

        statusView = Ui.tv(this, "Ready to call", 13, 0xCC4DD0C4, true);
        statusView.setGravity(Gravity.CENTER);
        statusView.setLetterSpacing(0.06f);
        LinearLayout.LayoutParams slp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        slp.topMargin = Ui.dp(this, 14);
        root.addView(statusView, slp);

        timerView = Ui.tv(this, "", 15, 0x99FFFFFF, false);
        timerView.setGravity(Gravity.CENTER);
        root.addView(timerView);

        root.addView(new View(this), new LinearLayout.LayoutParams(1, 0, 1f));

        LinearLayout tools = Ui.row(this);
        tools.setGravity(Gravity.CENTER);
        muteBtn = tool("mic", "Mute", new View.OnClickListener() {
            public void onClick(View v) { toggleMute(); }
        });
        spkBtn = tool("volume_up", "Speaker", new View.OnClickListener() {
            public void onClick(View v) { toggleSpeaker(); }
        });
        LinearLayout msgBtn = tool("message", "Message", new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    startActivity(new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + number)));
                } catch (Exception e) {
                    Ui.toast(CallActivity.this, "No messaging app found");
                }
            }
        });
        LinearLayout blkBtn = tool("block", "Block", new View.OnClickListener() {
            public void onClick(View v) {
                Data.block(CallActivity.this, number);
                Ui.toast(CallActivity.this, "Blocked " + Data.pretty(number));
            }
        });
        tools.addView(muteBtn);
        tools.addView(spkBtn);
        tools.addView(msgBtn);
        tools.addView(blkBtn);
        LinearLayout.LayoutParams tlp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tlp.bottomMargin = Ui.dp(this, 26);
        root.addView(tools, tlp);

        callBtn = Ui.col(this);
        callBtn.setGravity(Gravity.CENTER);
        GradientDrawable cg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{0xFF2FD08A, 0xFF4DD0C4});
        cg.setShape(GradientDrawable.OVAL);
        callBtn.setBackground(new android.graphics.drawable.RippleDrawable(
                ColorStateList.valueOf(0x55FFFFFF), cg, null));
        callIcon = Ui.icon(this, "phone", 32, 0xFF06120F);
        callBtn.addView(callIcon);
        callBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) { onCallButton(); }
        });
        root.addView(callBtn, new LinearLayout.LayoutParams(Ui.dp(this, 78), Ui.dp(this, 78)));

        callLabel = Ui.tv(this, "CALL", 10.5f, 0x99FFFFFF, true);
        callLabel.setLetterSpacing(0.2f);
        callLabel.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams clp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        clp.topMargin = Ui.dp(this, 8);
        root.addView(callLabel, clp);

        setContentView(root);

        if (!Perms.has(this, Manifest.permission.CALL_PHONE)) {
            statusView.setText("Call permission needed");
            callLabel.setText("ALLOW");
        }
    }

    private LinearLayout tool(String icon, String label, View.OnClickListener l) {
        LinearLayout v = Ui.col(this);
        v.setGravity(Gravity.CENTER);
        v.setClickable(true);
        v.setTag(label);
        ImageView iv = Ui.icon(this, icon, 22, 0xFFEAF2FF);
        v.addView(iv);
        TextView tv = Ui.tv(this, label, 10.5f, 0x99FFFFFF, false);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.topMargin = Ui.dp(this, 7);
        v.addView(tv, lp);
        LinearLayout.LayoutParams vp = new LinearLayout.LayoutParams(Ui.dp(this, 74), Ui.dp(this, 74));
        vp.setMargins(Ui.dp(this, 5), 0, Ui.dp(this, 5), 0);
        v.setLayoutParams(vp);
        GradientDrawable mask = new GradientDrawable();
        mask.setShape(GradientDrawable.OVAL);
        v.setBackground(new android.graphics.drawable.RippleDrawable(
                ColorStateList.valueOf(0x33FFFFFF), null, mask));
        v.setOnClickListener(l);
        return v;
    }

    private void paintTool(LinearLayout v, boolean on) {
        if (v == null) return;
        if (on) {
            v.setBackground(Ui.oval(0x33FFFFFF));
        } else {
            GradientDrawable mask = new GradientDrawable();
            mask.setShape(GradientDrawable.OVAL);
            v.setBackground(new android.graphics.drawable.RippleDrawable(
                    ColorStateList.valueOf(0x33FFFFFF), null, mask));
        }
    }

    private void onCallButton() {
        if (ended) {
            finish();
            return;
        }
        if (!Perms.has(this, Manifest.permission.CALL_PHONE)) {
            requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 900);
            return;
        }
        if (!inCall) beginCall();
        else endCall();
    }

    @Override
    public void onRequestPermissionsResult(int code, String[] perms, int[] res) {
        super.onRequestPermissionsResult(code, perms, res);
        if (code == 900 && res.length > 0 && res[0] == 0) {
            callLabel.setText("CALL");
            beginCall();
        }
    }

    private void beginCall() {
        if (inCall) return;
        try {
            Intent i = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(number)));
            PhoneAccountHandle hd = Sims.find(this, Sims.savedId(this));
            if (hd != null) i.putExtra(TelecomManager.EXTRA_PHONE_ACCOUNT_HANDLE, hd);
            i.putExtra(TelecomManager.EXTRA_START_CALL_WITH_SPEAKERPHONE, speaker);
            startActivity(i);
            inCall = true;
            statusView.setText("Dialing\u2026");
            callIcon.setImageResource(Ui.iconRes(this, "call_end"));
            callLabel.setText("END CALL");
            GradientDrawable cg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{0xFFFF6B7A, 0xFFE23E52});
            cg.setShape(GradientDrawable.OVAL);
            callBtn.setBackground(new android.graphics.drawable.RippleDrawable(
                    ColorStateList.valueOf(0x55FFFFFF), cg, null));
            listen();
        } catch (SecurityException se) {
            Ui.toast(this, "Call permission denied");
        } catch (Exception e) {
            Ui.toast(this, "Could not place the call");
        }
    }

    private void endCall() {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                TelecomManager tcm = (TelecomManager) getSystemService(TELECOM_SERVICE);
                if (tcm != null && Perms.has(this, Manifest.permission.ANSWER_PHONE_CALLS)) {
                    tcm.endCall();
                    statusView.setText("Ending\u2026");
                    return;
                }
            }
            Ui.toast(this, "Use the system call screen to end this call");
        } catch (Exception e) {
            Ui.toast(this, "Could not end the call");
        }
    }

    private void listen() {
        try {
            if (tm == null) return;
            listener = new PhoneStateListener() {
                @Override
                public void onCallStateChanged(int state, String incoming) {
                    if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                        if (!inCall) return;
                        statusView.setText("In call");
                        if (startAt == 0) {
                            startAt = System.currentTimeMillis();
                            h.post(tick);
                        }
                    } else if (state == TelephonyManager.CALL_STATE_IDLE) {
                        if (startAt > 0 && !ended) {
                            ended = true;
                            inCall = false;
                            h.removeCallbacks(tick);
                            long secs = (System.currentTimeMillis() - startAt) / 1000;
                            statusView.setText("Call ended");
                            timerView.setText(Data.duration(secs));
                            callIcon.setImageResource(Ui.iconRes(CallActivity.this, "check"));
                            callLabel.setText("DONE");
                            GradientDrawable cg = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                                    new int[]{0xFF2FD08A, 0xFF4DD0C4});
                            cg.setShape(GradientDrawable.OVAL);
                            callBtn.setBackground(new android.graphics.drawable.RippleDrawable(
                                    ColorStateList.valueOf(0x55FFFFFF), cg, null));
                        } else if (inCall) {
                            inCall = false;
                            ended = true;
                            statusView.setText("Call not connected");
                            callLabel.setText("DONE");
                            callIcon.setImageResource(Ui.iconRes(CallActivity.this, "check"));
                        }
                    }
                }
            };
            tm.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
        } catch (Exception ignored) {
        }
    }

    private final Runnable tick = new Runnable() {
        public void run() {
            if (startAt == 0) return;
            long secs = (System.currentTimeMillis() - startAt) / 1000;
            timerView.setText(Data.duration(secs));
            h.postDelayed(this, 1000);
        }
    };

    private void toggleMute() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        muted = !muted;
        try {
            if (am != null) am.setMicrophoneMute(muted);
        } catch (Exception e) {
            Ui.toast(this, "Mic control not allowed on this Android version");
        }
        muteBtnOn = muted;
        paintTool(muteBtn, muteBtnOn);
        ((ImageView) muteBtn.getChildAt(0)).setImageResource(
                Ui.iconRes(this, muted ? "mic_off" : "mic"));
        Ui.toast(this, muted ? "Microphone muted" : "Microphone on");
    }

    private void toggleSpeaker() {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        speaker = !speaker;
        try {
            if (am != null) am.setSpeakerphoneOn(speaker);
        } catch (Exception e) {
            Ui.toast(this, "Speaker control blocked");
        }
        spkBtnOn = speaker;
        paintTool(spkBtn, spkBtnOn);
        ((ImageView) spkBtn.getChildAt(0)).setImageResource(
                Ui.iconRes(this, speaker ? "volume_up" : "volume_off"));
        Ui.toast(this, speaker ? "Speaker on" : "Speaker off");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        h.removeCallbacks(tick);
        try {
            if (tm != null && listener != null) tm.listen(listener, PhoneStateListener.LISTEN_NONE);
        } catch (Exception ignored) {
        }
    }

    /** Animated rings behind the avatar. */
    public static class PulseView extends View {
        private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        private float phase = 0f;
        private ValueAnimator anim;

        public PulseView(android.content.Context c) {
            super(c);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(Ui.dp(c, 1.4f));
            anim = ValueAnimator.ofFloat(0f, 1f);
            anim.setDuration(2600);
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator a) {
                    phase = (float) a.getAnimatedValue();
                    invalidate();
                }
            });
            anim.start();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float base = Ui.dp(getContext(), 62);
            for (int i = 0; i < 3; i++) {
                float pr = (phase + i / 3f) % 1f;
                float r = base + pr * Ui.dp(getContext(), 52);
                int alpha = (int) (70 * (1f - pr));
                p.setColor((alpha << 24) | 0x004DD0C4);
                canvas.drawCircle(cx, cy, r, p);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (anim != null) anim.cancel();
        }
    }
}
