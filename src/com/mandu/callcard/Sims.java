package com.mandu.callcard;

import android.content.Context;
import android.telecom.PhoneAccount;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;

import java.util.ArrayList;
import java.util.List;

public class Sims {

    public static List<PhoneAccountHandle> handles(Context c) {
        List<PhoneAccountHandle> out = new ArrayList<>();
        try {
            TelecomManager tm = (TelecomManager) c.getSystemService(Context.TELECOM_SERVICE);
            if (tm != null) {
                List<PhoneAccountHandle> all = tm.getCallCapablePhoneAccounts();
                if (all != null) out.addAll(all);
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    public static String label(Context c, PhoneAccountHandle h) {
        try {
            TelecomManager tm = (TelecomManager) c.getSystemService(Context.TELECOM_SERVICE);
            if (tm == null || h == null) return "SIM";
            PhoneAccount pa = tm.getPhoneAccount(h);
            if (pa != null && pa.getLabel() != null) return pa.getLabel().toString();
        } catch (Exception ignored) {
        }
        return "SIM";
    }

    public static PhoneAccountHandle find(Context c, String id) {
        if (id == null) return null;
        for (PhoneAccountHandle h : handles(c)) {
            if (h.getId().equals(id)) return h;
        }
        return null;
    }

    public static String savedId(Context c) {
        return App.p(c).getString("sim", null);
    }

    public static void saveId(Context c, String id) {
        App.p(c).edit().putString("sim", id).apply();
    }

    public static String savedLabel(Context c) {
        List<PhoneAccountHandle> hs = handles(c);
        if (hs.size() < 2) return hs.isEmpty() ? "SIM" : label(c, hs.get(0));
        PhoneAccountHandle h = find(c, savedId(c));
        return h == null ? "Ask every call" : label(c, h);
    }
}
