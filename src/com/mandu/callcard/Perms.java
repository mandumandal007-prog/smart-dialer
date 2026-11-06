package com.mandu.callcard;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

public class Perms {

    public static class P {
        public String key;
        public String title;
        public String why;
        public P(String k, String t, String w) {
            key = k;
            title = t;
            why = w;
        }
    }

    public static List<P> list() {
        List<P> l = new ArrayList<>();
        l.add(new P(Manifest.permission.CALL_PHONE, "Make phone calls", "Place calls straight from the dialer."));
        l.add(new P(Manifest.permission.READ_PHONE_STATE, "Phone state", "Detect when the call connects or ends."));
        if (Build.VERSION.SDK_INT >= 26) {
            l.add(new P(Manifest.permission.READ_PHONE_NUMBERS, "Phone numbers", "Show your own number and SIM details."));
        }
        l.add(new P(Manifest.permission.READ_CONTACTS, "Read contacts", "Show names instead of raw numbers."));
        l.add(new P(Manifest.permission.WRITE_CONTACTS, "Edit contacts", "Save an unknown number as a new contact."));
        l.add(new P(Manifest.permission.READ_CALL_LOG, "Read call history", "Fill the Recents tab."));
        l.add(new P(Manifest.permission.WRITE_CALL_LOG, "Edit call history", "Delete single entries or clear the log."));
        if (Build.VERSION.SDK_INT >= 28) {
            l.add(new P(Manifest.permission.ANSWER_PHONE_CALLS, "Answer / end calls", "End a call from the calling card."));
        }
        l.add(new P(Manifest.permission.SEND_SMS, "Send SMS", "Quick reply to a number from the card."));
        return l;
    }

    public static boolean has(Context c, String perm) {
        return c.checkSelfPermission(perm) == PackageManager.PERMISSION_GRANTED;
    }

    public static List<String> missing(Context c) {
        List<String> out = new ArrayList<>();
        for (P p : list()) if (!has(c, p.key)) out.add(p.key);
        return out;
    }

    public static void ask(Activity a, int code) {
        List<String> m = missing(a);
        if (m.isEmpty()) return;
        a.requestPermissions(m.toArray(new String[0]), code);
    }

    public static void askOne(Activity a, String perm, int code) {
        if (!has(a, perm)) a.requestPermissions(new String[]{perm}, code);
    }
}
