package com.mandu.callcard;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class Data {

    public static class C {
        public String name = "";
        public String number = "";
        public String t9 = "";
        public long id;
    }

    public static class Log {
        public String name = "";
        public String number = "";
        public int type;
        public long date;
        public long dur;
    }

    /* ---------- text helpers ---------- */

    public static String t9(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (char ch : s.toUpperCase(Locale.US).toCharArray()) {
            if (ch >= 'A' && ch <= 'C') b.append('2');
            else if (ch >= 'D' && ch <= 'F') b.append('3');
            else if (ch >= 'G' && ch <= 'I') b.append('4');
            else if (ch >= 'J' && ch <= 'L') b.append('5');
            else if (ch >= 'M' && ch <= 'O') b.append('6');
            else if (ch >= 'P' && ch <= 'S') b.append('7');
            else if (ch >= 'T' && ch <= 'V') b.append('8');
            else if (ch >= 'W' && ch <= 'Z') b.append('9');
            else if (ch >= '0' && ch <= '9') b.append(ch);
        }
        return b.toString();
    }

    public static String digits(String s) {
        if (s == null) return "";
        StringBuilder b = new StringBuilder();
        for (char ch : s.toCharArray()) if (ch >= '0' && ch <= '9') b.append(ch);
        return b.toString();
    }

    public static String initials(String name, String number) {
        String n = name == null ? "" : name.trim();
        if (n.isEmpty()) {
            String d = digits(number);
            return d.length() >= 2 ? d.substring(d.length() - 2) : (d.isEmpty() ? "#" : d);
        }
        String[] parts = n.split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase(Locale.US);
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase(Locale.US);
    }

    public static String pretty(String number) {
        if (number == null) return "";
        String n = number.trim();
        if (n.startsWith("+91") && n.length() == 13) {
            return "+91 " + n.substring(3, 8) + " " + n.substring(8);
        }
        return n;
    }

    public static String duration(long sec) {
        if (sec <= 0) return "";
        long m = sec / 60, s = sec % 60, h = m / 60;
        m = m % 60;
        if (h > 0) return String.format(Locale.US, "%d:%02d:%02d", h, m, s);
        return String.format(Locale.US, "%d:%02d", m, s);
    }

    public static String when(long millis) {
        Calendar now = Calendar.getInstance();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        SimpleDateFormat hm = new SimpleDateFormat("h:mm a", Locale.US);
        if (now.get(Calendar.YEAR) == c.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR)) {
            return hm.format(c.getTime());
        }
        now.add(Calendar.DAY_OF_YEAR, -1);
        if (now.get(Calendar.YEAR) == c.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == c.get(Calendar.DAY_OF_YEAR)) {
            return "Yesterday";
        }
        return new SimpleDateFormat("dd/MM/yy", Locale.US).format(c.getTime());
    }

    /* ---------- contacts ---------- */

    public static List<C> contacts(Context ctx) {
        List<C> out = new ArrayList<>();
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] proj = {
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID
        };
        Cursor cur = null;
        try {
            cur = ctx.getContentResolver().query(uri, proj, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC");
            if (cur != null) {
                Set<String> seen = new HashSet<>();
                while (cur.moveToNext()) {
                    String name = cur.getString(0);
                    String num = cur.getString(1);
                    long cid = cur.getLong(2);
                    if (num == null) continue;
                    String key = digits(num) + "#" + cid;
                    if (!seen.add(key)) continue;
                    C c = new C();
                    c.name = name == null ? "" : name;
                    c.number = num;
                    c.id = cid;
                    c.t9 = t9(c.name);
                    out.add(c);
                    if (out.size() > 3000) break;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        Collections.sort(out, new Comparator<C>() {
            public int compare(C a, C b) {
                return a.name.compareToIgnoreCase(b.name);
            }
        });
        return out;
    }

    public static String nameOf(Context ctx, String number) {
        if (number == null || number.trim().isEmpty()) return "";
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(number));
        Cursor cur = null;
        try {
            cur = ctx.getContentResolver().query(uri,
                    new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
            if (cur != null && cur.moveToFirst()) return cur.getString(0);
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        return "";
    }

    /* ---------- call log ---------- */

    public static List<Log> logs(Context ctx, int limit) {
        List<Log> out = new ArrayList<>();
        Cursor cur = null;
        try {
            cur = ctx.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                    new String[]{
                            CallLog.Calls.CACHED_NAME,
                            CallLog.Calls.NUMBER,
                            CallLog.Calls.TYPE,
                            CallLog.Calls.DATE,
                            CallLog.Calls.DURATION},
                    null, null, CallLog.Calls.DATE + " DESC");
            if (cur != null) {
                while (cur.moveToNext()) {
                    Log l = new Log();
                    l.name = cur.getString(0) == null ? "" : cur.getString(0);
                    l.number = cur.getString(1) == null ? "" : cur.getString(1);
                    l.type = cur.getInt(2);
                    l.date = cur.getLong(3);
                    l.dur = cur.getLong(4);
                    out.add(l);
                    if (out.size() >= limit) break;
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cur != null) cur.close();
        }
        return out;
    }

    public static boolean deleteLog(Context ctx, String number, long date) {
        try {
            int n = ctx.getContentResolver().delete(CallLog.Calls.CONTENT_URI,
                    CallLog.Calls.NUMBER + "=? AND " + CallLog.Calls.DATE + "=?",
                    new String[]{number, String.valueOf(date)});
            return n > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean clearLog(Context ctx) {
        try {
            ctx.getContentResolver().delete(CallLog.Calls.CONTENT_URI, null, null);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /* ---------- blocked numbers ---------- */

    public static Set<String> blocked(Context ctx) {
        SharedPreferences p = App.p(ctx);
        String raw = p.getString("blocked", "");
        Set<String> s = new HashSet<>();
        if (raw != null && !raw.isEmpty()) {
            for (String x : raw.split(",")) if (!x.trim().isEmpty()) s.add(x.trim());
        }
        return s;
    }

    public static void block(Context ctx, String number) {
        Set<String> s = blocked(ctx);
        s.add(digits(number));
        save(ctx, s);
    }

    public static void unblock(Context ctx, String number) {
        Set<String> s = blocked(ctx);
        s.remove(digits(number));
        save(ctx, s);
    }

    private static void save(Context ctx, Set<String> s) {
        StringBuilder b = new StringBuilder();
        for (String x : s) {
            if (b.length() > 0) b.append(',');
            b.append(x);
        }
        App.p(ctx).edit().putString("blocked", b.toString()).apply();
    }

    public static boolean isBlocked(Context ctx, String number) {
        String d = digits(number);
        if (d.isEmpty()) return false;
        for (String b : blocked(ctx)) {
            if (d.equals(b) || d.endsWith(b) || b.endsWith(d)) return true;
        }
        return false;
    }
}
